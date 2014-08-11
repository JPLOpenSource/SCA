//////////////////////////////////////////////////////////////////////////////
// Product: QF/C++
// Last Updated for Version: 4.0.00
// Date of the Last Update:  Apr 07, 2008
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
#include "qf_pkg.h"

// #include "qassert.h"
// Q_DEFINE_THIS_MODULE(qf_tick)

/// \file
/// \ingroup qf
/// \brief QF::tick() implementation.

//............................................................................
void QF::tick(void) {                                            // see NOTE01
    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();

    QS_BEGIN_NOLOCK_(QS_QF_TICK, (void *)0, (void *)0)
        QS_TEC_(++QS::tickCtr_);                           // the tick counter
    QS_END_NOLOCK_()

    QTimeEvt *t = QF_timeEvtListHead_;
    while (t != (QTimeEvt *)0) {
        if (--t->m_ctr == (QTimeEvtCtr)0) {// is the time evt about to expire?
            if (t->m_interval != (QTimeEvtCtr)0) {//is it a periodic time evt?
                t->m_ctr = t->m_interval;                // rearm the time evt
            }
            else {   // one-shot time evt, disarm by removing it from the list
                if (t == QF_timeEvtListHead_) {
                    QF_timeEvtListHead_ = t->m_next;
                }
                else {
                    if (t->m_next != (QTimeEvt *)0) {// not the last time evt?
                        t->m_next->m_prev = t->m_prev;
                    }
                    t->m_prev->m_next = t->m_next;
                }
                t->m_prev = (QTimeEvt *)0;     // mark the time event disarmed

                QS_BEGIN_NOLOCK_(QS_QF_TIMEEVT_AUTO_DISARM, QS::teObj_, t)
                    QS_OBJ_(t);                      // this time event object
                    QS_OBJ_(t->m_act);                     // the active object
                QS_END_NOLOCK_()
            }

            QS_BEGIN_NOLOCK_(QS_QF_TIMEEVT_POST, QS::teObj_, t)
                QS_TIME_();                                       // timestamp
                QS_OBJ_(t);                           // the time event object
                QS_SIG_(t->sig);              // the signal of this time event
                QS_OBJ_(t->m_act);                        // the active object
            QS_END_NOLOCK_()

            QF_INT_UNLOCK_();   // unlock interrupts before calling QF service

                  // postFIFO() asserts internally that the event was accepted
            t->m_act->postFIFO(t);
        }
        else {
            QF_INT_UNLOCK_();
            static uint8_t volatile dummy;
            dummy = (uint8_t)0;      // execute a few instructions, see NOTE02
        }

        QF_INT_LOCK_();           // lock interrupts again to advance the link
        t = t->m_next;
    }
    QF_INT_UNLOCK_();
}

//////////////////////////////////////////////////////////////////////////////
// NOTE01:
// QF::tick() must always run to completion and never preempt itself.
// In particular, if QF::tick() runs in an ISR, the ISR is not allowed to
// preempt itself. Also, QF::tick() should not be called from two different
// ISRs, which potentially could preempt each other.
//
// NOTE02:
// On many CPUs, the interrupt unlocking takes only effect on the next
// machine instruction, which happens to be here another interrupt lock.
// The assignment of a volatile variable requires a few instructions, which
// the compiler cannot optimize away. This ensures that the interrupts get
// actually unlocked, so that the interrupt latency stays low.
//
