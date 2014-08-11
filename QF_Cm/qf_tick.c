/*****************************************************************************
* Product: QF/C
* Last Updated for Version: 3.3.00
* Date of the Last Update:  Jan 22, 2007
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
*
* Copyright (C) 2002-2007 Quantum Leaps, LLC. All rights reserved.
*
* This software may be distributed and modified under the terms of the GNU
* General Public License version 2 (GPL) as published by the Free Software
* Foundation and appearing in the file GPL.TXT included in the packaging of
* this file. Please note that GPL Section 2[b] requires that all works based
* on this software must also be made publicly available under the terms of
* the GPL ("Copyleft").
*
* Alternatively, this software may be distributed and modified under the
* terms of Quantum Leaps commercial licenses, which expressly supersede
* the GPL and are specifically designed for licensees interested in
* retaining the proprietary status of their code.
*
* Contact information:
* Quantum Leaps Web site:  http://www.quantum-leaps.com
* e-mail:                  sales@quantum-leaps.com
*****************************************************************************/
#include "qf_pkg.h"
#include "qassert.h"

Q_DEFINE_THIS_MODULE(qf_tick)

/*..........................................................................*/
/** \ingroup qf
* \file qf_tick.c
* \brief QF_tick() implementation.
*/

/*..........................................................................*/
void QF_tick(void) {                                          /* see NOTE01 */
    QTimeEvt *t;

    QS_BEGIN_NOLOCK_(QS_QF_TICK, (void *)0, (void *)0);
        QS_TEC_(++QS_tickCtr_);                         /* the tick counter */
    QS_END_NOLOCK_();

    for (t = QF_timeEvtListHead_; t != (QTimeEvt *)0; t = t->next__) {
        if (--t->ctr__ == (QTimeEvtCtr)0) { /* is time evt about to expire? */
            if (t->interval__ != (QTimeEvtCtr)0) {/*is it periodic timeout? */
                t->ctr__ = t->interval__;           /* rearm the time event */
            }
            else { /* one-shot timeout, disarm by removing it from the list */
                if (t == QF_timeEvtListHead_) {
                    QF_timeEvtListHead_ = t->next__;
                }
                else {
                    if (t->next__ != (QTimeEvt *)0) {/* not the last event? */
                        t->next__->prev__ = t->prev__;
                    }
                    t->prev__->next__ = t->next__;
                }
                t->prev__ = (QTimeEvt *)0;       /* mark the event disarmed */

                QS_BEGIN_NOLOCK_(QS_QF_TIMEEVT_AUTO_DISARM, QS_teObj_, t);
                    QS_OBJ_(t);                   /* this time event object */
                    QS_OBJ_(t->act__);                 /* the active object */
                QS_END_NOLOCK_();
            }
            if (t->act__ != (QActive *)0) {

                QS_BEGIN_NOLOCK_(QS_QF_TIMEEVT_POST, QS_teObj_, t);
                    QS_TIME_();                                /* timestamp */
                    QS_OBJ_(t);                    /* the time event object */
                    QS_SIG_(((QEvent *)t)->sig);/*signal of this time event */
                    QS_OBJ_(t->act__);                 /* the active object */
                QS_END_NOLOCK_();

                          /* asserts internally that the event was accepted */
                QActive_postFIFO(t->act__, (QEvent const *)t);
            }
            else {

                QS_BEGIN_NOLOCK_(QS_QF_TIMEEVT_PUBLISH, QS_teObj_, t);
                    QS_TIME_();                                /* timestamp */
                    QS_OBJ_(t);                    /* the time event object */
                    QS_SIG_(((QEvent *)t)->sig);/*signal of this time event */
                QS_END_NOLOCK_();

                QF_publish((QEvent const *)t);
            }
        }
    }
}

/*****************************************************************************
* NOTE01:
* QF_tick() is not reentrant, which means that the "clock tick" ISR invoking
* QF_tick() function must finish (run to completion) before the "clock tick"
* interrupt fires again. It's also assmed here that QF_tick() is invoked from
* only one ISR (the "clock tick" interrupt).
*/
