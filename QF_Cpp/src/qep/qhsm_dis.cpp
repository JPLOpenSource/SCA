//////////////////////////////////////////////////////////////////////////////
// Product: QEP/C++
// Last Updated for Version: 4.0.00
// Date of the Last Update:  Apr 06, 2008
//
//                    Q u a n t u m     L e a P s
//                    ---------------------------
//                    innovating embedded systems
//
// Copyright (C) 2002-2008 Quantum Leaps, LLC. All rights reserved.
//
// This software may be distributed and modified under the terms of the GNU
// General Public License version 2 (GPL) as published by the Free Software
// Foundation and appearing in the file GPL.TXT included in the packaging of
// this file. Please note that GPL Section 2[b] requires that all works based
// on this software must also be made publicly available under the terms of
// the GPL ("Copyleft").
//
// Alternatively, this software may be distributed and modified under the
// terms of Quantum Leaps commercial licenses, which expressly supersede
// the GPL and are specifically designed for licensees interested in
// retaining the proprietary status of their code.
//
// Contact information:
// Quantum Leaps Web site:  http://www.quantum-leaps.com
// e-mail:                  info@quantum-leaps.com
//////////////////////////////////////////////////////////////////////////////
#include "qep_pkg.h"
#include "qassert.h"

Q_DEFINE_THIS_MODULE(qhsm_dis)

/// \file
/// \ingroup qep
/// \brief QHsm::dispatch() implementation.

//............................................................................
void QHsm::dispatch(QEvent const *e) {
    QStateHandler path[QEP_MAX_NEST_DEPTH_];
    QStateHandler s;
    QStateHandler t;
    QState r;
    QS_INT_LOCK_KEY_

    /* [SWC 2013.02.13] Added logic to set a new QHsm flag indicating whether
     * dispatched event was handled, and to check input flag 'ignore_dropped'
     * to decide whether to assert on dropped event.
     */
    handled = false;                               // assume event NOT handled

    t = m_state;                                     // save the current state

    do {                                // process the event hierarchically...
        s = m_state;
        r = (*s)(this, e);                           // invoke state handler s
    } while (r == Q_RET_SUPER);

    if (r == Q_RET_TRAN) {                                // transition taken?
#ifdef Q_SPY
        QStateHandler src = s;       // save the transition source for tracing
#endif
        int8_t ip = (int8_t)(-1);               // transition entry path index
        int8_t iq;                       // helper transition entry path index

        path[0] = m_state;                // save the target of the transition
        path[1] = t;

        while (t != s) {       // exit current state to transition source s...
            if (QEP_TRIG_(t, Q_EXIT_SIG) == Q_RET_HANDLED) {   //exit handled?
                QS_BEGIN_(QS_QEP_STATE_EXIT, QS::smObj_, this)
                    QS_OBJ_(this);                // this state machine object
                    QS_FUN_(t);                            // the exited state
                QS_END_()

                (void)QEP_TRIG_(t, QEP_EMPTY_SIG_);    // find superstate of t
            }
            t = m_state;                       // m_state holds the superstate
        }

        t = path[0];                               // target of the transition

        if (s == t) {         // (a) check source==target (transition to self)
            QEP_EXIT_(s)                                    // exit the source
            ip = (int8_t)0;                                // enter the target
        }
        else {
            (void)QEP_TRIG_(t, QEP_EMPTY_SIG_);        // superstate of target
            t = m_state;
            if (s == t) {                   // (b) check source==target->super
                ip = (int8_t)0;                            // enter the target
            }
            else {
                (void)QEP_TRIG_(s, QEP_EMPTY_SIG_);       // superstate of src
                                     // (c) check source->super==target->super
                if (m_state == t) {
                    QEP_EXIT_(s)                            // exit the source
                    ip = (int8_t)0;                        // enter the target
                }
                else {
                                            // (d) check source->super==target
                    if (m_state == path[0]) {
                        QEP_EXIT_(s)                        // exit the source
                    }
                    else { // (e) check rest of source==target->super->super..
                           // and store the entry path along the way
                           //
                        iq = (int8_t)0;         // indicate that LCA not found
                        ip = (int8_t)1;     // enter target and its superstate
                        path[1] = t;          // save the superstate of target
                        t = m_state;                     // save source->super
                                                  // find target->super->super
                        r = QEP_TRIG_(path[1], QEP_EMPTY_SIG_);
                        while (r == Q_RET_SUPER) {
                            path[++ip] = m_state;      // store the entry path
                            if (m_state == s) {           // is it the source?
                                iq = (int8_t)1;     // indicate that LCA found
                                               // entry path must not overflow
                                Q_ASSERT(ip < (int8_t)QEP_MAX_NEST_DEPTH_);
                                --ip;               // do not enter the source
                                r = Q_RET_HANDLED;       // terminate the loop
                            }
                            else {      // it is not the source, keep going up
                                r = QEP_TRIG_(m_state, QEP_EMPTY_SIG_);
                            }
                        }
                        if (iq == (int8_t)0) {       // the LCA not found yet?

                                               // entry path must not overflow
                            Q_ASSERT(ip < (int8_t)QEP_MAX_NEST_DEPTH_);

                            QEP_EXIT_(s)                   // exit the source

                            // (f) check the rest of source->super
                            //                  == target->super->super...
                            //
                            iq = ip;
                            r = Q_RET_IGNORED;       // indicate LCA NOT found
                            do {
                                if (t == path[iq]) {       // is this the LCA?
                                    r = Q_RET_HANDLED;   // indicate LCA found
                                    ip = (int8_t)(iq - 1); // do not enter LCA
                                    iq = (int8_t)(-1);   // terminate the loop
                                }
                                else {
                                    --iq;    // try lower superstate of target
                                }
                            } while (iq >= (int8_t)0);

                            if (r != Q_RET_HANDLED) {    // LCA not found yet?
                                // (g) check each source->super->...
                                // for each target->super...
                                //
                                r = Q_RET_IGNORED;             // keep looping
                                do {
                                                          // exit t unhandled?
                                    if (QEP_TRIG_(t, Q_EXIT_SIG)
                                        == Q_RET_HANDLED)
                                    {
                                        QS_BEGIN_(QS_QEP_STATE_EXIT,
                                                  QS::smObj_, this)
                                            QS_OBJ_(this);
                                            QS_FUN_(t);
                                        QS_END_()

                                        (void)QEP_TRIG_(t, QEP_EMPTY_SIG_);
                                    }
                                    t = m_state;         //  set to super of t
                                    iq = ip;
                                    do {
                                        if (t == path[iq]) {   // is this LCA?
                                                           // do not enter LCA
                                            ip = (int8_t)(iq - 1);
                                            iq = (int8_t)(-1);   //break inner
                                            r = Q_RET_HANDLED;   //break outer
                                        }
                                        else {
                                            --iq;
                                        }
                                    } while (iq >= (int8_t)0);
                                } while (r != Q_RET_HANDLED);
                            }
                        }
                    }
                }
            }
        }
                       // retrace the entry path in reverse (desired) order...
        for (; ip >= (int8_t)0; --ip) {
            QEP_ENTER_(path[ip])                             // enter path[ip]
        }
        t = path[0];                         // stick the target into register
        m_state = t;                               // update the current state

                                         // drill into the target hierarchy...
        while (QEP_TRIG_(t, Q_INIT_SIG) == Q_RET_TRAN) {

            QS_BEGIN_(QS_QEP_STATE_INIT, QS::smObj_, this)
                QS_OBJ_(this);                    // this state machine object
                QS_FUN_(t);                        // the source (pseudo)state
                QS_FUN_(m_state);              // the target of the transition
            QS_END_()

            ip = (int8_t)0;
            path[0] = m_state;
            (void)QEP_TRIG_(m_state, QEP_EMPTY_SIG_);       // find superstate
            while (m_state != t) {
                path[++ip] = m_state;
                (void)QEP_TRIG_(m_state, QEP_EMPTY_SIG_);   // find superstate
            }
            m_state = path[0];
                                               // entry path must not overflow
            Q_ASSERT(ip < (int8_t)QEP_MAX_NEST_DEPTH_);

            do {       // retrace the entry path in reverse (correct) order...
                QEP_ENTER_(path[ip])                         // enter path[ip]
            } while ((--ip) >= (int8_t)0);

            t = path[0];
        }

        QS_BEGIN_(QS_QEP_TRAN, QS::smObj_, this)
            QS_TIME_();                                          // time stamp
            QS_SIG_(e->sig);                        // the signal of the event
            QS_OBJ_(this);                        // this state machine object
            QS_FUN_(src);                      // the source of the transition
            QS_FUN_(t);                                // the new active state
        QS_END_()

    }
    else {                                             // transition not taken
#ifdef Q_SPY
        if (r == Q_RET_IGNORED) {                            // event ignored?

            QS_BEGIN_(QS_QEP_IGNORED, QS::smObj_, this)
                QS_TIME_();                                      // time stamp
                QS_SIG_(e->sig);                    // the signal of the event
                QS_OBJ_(this);                    // this state machine object
                QS_FUN_(t);                               // the current state
            QS_END_()

        }
        else {                                                // event handled

            QS_BEGIN_(QS_QEP_INTERN_TRAN, QS::smObj_, this)
                QS_TIME_();                                      // time stamp
                QS_SIG_(e->sig);                    // the signal of the event
                QS_OBJ_(this);                    // this state machine object
                QS_FUN_(s);                // the state that handled the event
            QS_END_()

        }
#endif
    }
    /* [SWC 2013.02.13] Update 'handled' flag and reset 'ignore_dropped' flag
     */
    if (s != ((QStateHandler )&QHsm::top)) {             // Reached TOP state!
        handled = true;                   // dispatched event has been handled
    }
    ignore_dropped = false;           // reinstate detection for dropped event
    m_state = t;                 // set new state or restore the current state
}
