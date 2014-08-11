/*****************************************************************************
* Product: QF/C
* Last Updated for Version: 4.2.00
* Date of the Last Update:  Jul 13, 2011
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
*
* Copyright (C) 2002-2011 Quantum Leaps, LLC. All rights reserved.
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
* e-mail:                  info@quantum-leaps.com
*****************************************************************************/
#include "qf_pkg.h"
/* #include "qassert.h" */

/* Q_DEFINE_THIS_MODULE(qf_tick) */

/**
* \file
* \ingroup qf
* \brief QF_tick() implementation.
*/

/*..........................................................................*/
#ifndef Q_SPY
void QF_tick(void) {                                          /* see NOTE01 */
#else
void QF_tick(void const *sender) {
#endif

    QTimeEvt *t;
    QF_INT_LOCK_KEY_

    QF_INT_LOCK_();

    QS_BEGIN_NOLOCK_(QS_QF_TICK, (void *)0, (void *)0)
        QS_TEC_(++QS_tickCtr_);                         /* the tick counter */
    QS_END_NOLOCK_()

    t = QF_timeEvtListHead_;
    while (t != (QTimeEvt *)0) {
        --t->ctr;
        if (t->ctr == (QTimeEvtCtr)0) {     /* is time evt about to expire? */
            if (t->interval != (QTimeEvtCtr)0) { /* is it periodic timeout? */
                t->ctr = t->interval;               /* rearm the time event */
            }
            else { /* one-shot timeout, disarm by removing it from the list */
                if (t == QF_timeEvtListHead_) {
                    QF_timeEvtListHead_ = t->next;
                }
                else {
                    if (t->next != (QTimeEvt *)0) {  /* not the last event? */
                        t->next->prev = t->prev;
                    }
                    t->prev->next = t->next;
                }
                t->prev = (QTimeEvt *)0;         /* mark the event disarmed */

                QS_BEGIN_NOLOCK_(QS_QF_TIMEEVT_AUTO_DISARM, QS_teObj_, t)
                    QS_OBJ_(t);                   /* this time event object */
                    QS_OBJ_(t->act);                   /* the active object */
                QS_END_NOLOCK_()
            }

            QS_BEGIN_NOLOCK_(QS_QF_TIMEEVT_POST, QS_teObj_, t)
                QS_TIME_();                                    /* timestamp */
                QS_OBJ_(t);                        /* the time event object */
                QS_SIG_(t->super.sig);         /* signal of this time event */
                QS_OBJ_(t->act);                       /* the active object */
            QS_END_NOLOCK_()

            QF_INT_UNLOCK_();/* unlock interrupts before calling QF service */

                /* QACTIVE_POST() asserts internally if the queue overflows */
            QACTIVE_POST(t->act, (QEvent *)t, sender);
        }
        else {
            static uint8_t volatile dummy;
            QF_INT_UNLOCK_();
            dummy = (uint8_t)0;   /* execute a few instructions, see NOTE02 */
        }

        QF_INT_LOCK_();        /* lock interrupts again to advance the link */
        t = t->next;
    }
    QF_INT_UNLOCK_();
}

/*****************************************************************************
* NOTE01:
* QF_tick() must always run to completion and never preempt itself.
* In particular, if QF_tick() runs in an ISR, the ISR is not allowed to
* preempt itself. Also, QF_tick() should not be called from two different
* ISRs, which potentially could preempt each other.
*
* NOTE02:
* On many CPUs, the interrupt unlocking takes only effect on the next
* machine instruction, which happens to be here another interrupt lock.
* The assignment of a volatile variable requires a few instructions, which
* the compiler cannot optimize away. This ensures that the interrupts get
* actually unlocked, so that the interrupt latency stays low.
*/
