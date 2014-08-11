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

Q_DEFINE_THIS_MODULE(qf_run)

/*..........................................................................*/
/** \ingroup qf
* \file qf_run.c
* \brief "vanilla" QF port: QF_run() non-preemptive scheduler,
* QActive_start(), QActive_stop(), and QF_getPortvesrion() implementation.
*/

/* Package-scope objects ---------------------------------------------------*/
QPSet QF_readySet_;

/*..........................................................................*/
const char Q_ROM * Q_ROM_VAR QF_getPortVersion(void) {
    static const char Q_ROM Q_ROM_VAR version[] =  "1.2.06";
    return version;
}
/*..........................................................................*/

// GW Make this routine return if all events are consumed.
//  Remove the onIdle

void QF_run(void) {                                           /* see NOTE01 */
    QF_start();                                              /* enable ISRs */

    for (;;) {                                       /* the background loop */
        QF_INT_LOCK_KEY_
        QF_INT_LOCK_();
        if (!QPSet_isEmpty(&QF_readySet_)) {
            uint8_t p;
            QActive *a;
            QEvent const *e;
            QPSet_findMax(&QF_readySet_, p);
            QF_INT_UNLOCK_();

            a = QF_active_[p];
            e = QActive_get_(a);          /* get the next event for this AO */
            QF_ACTIVE_DISPATCH_(a, e); /* dispatch evt to the state machine */
            QF_gc(e); /* determine if event is garbage and collect it if so */
        }
        else {
          QF_INT_UNLOCK_();
          break;
        }
    }
}

// GW Add a routine to run only 1 state-machine.
// This is similar to QF_run except the events from 1 active object are
// dispatched to the state-machine
void QF_runSM(QActive *me)
{
      QF_INT_LOCK_KEY_
      QF_INT_LOCK_();
      // Dispatch all events in the active objects input queue.
      while (me->eQueue__.frontEvt__ != (QEvent*)0)
      {
         QEvent const *e = QActive_get_(me);          /* get the next event for this AO */
         QF_ACTIVE_DISPATCH_((QFsm *)me, e); /* dispatch evt to the state machine */
         QF_gc(e); /* determine if event is garbage and collect it if so */
       }
       QF_INT_UNLOCK_();
}


/*..........................................................................*/
void QActive_start(QActive *me, uint8_t prio,
                   QEvent const *qSto[], uint32_t qLen,
                   void *stkSto, uint32_t stkSize,
                   QEvent const *ie)
{
    Q_REQUIRE((0 < prio) && (prio <= QF_MAX_ACTIVE)
              && (stkSto == (void *)0));   /* does not need per-actor stack */
    (void)stkSto;
    (void)stkSize;
    me->prio__ = prio;
    QEQueue_init(&me->eQueue__, qSto, (QEQueueCtr)qLen);
    me->osObject__ = prio;                      /* initialize the OS object */
    QF_add_(me);                     /* make QF aware of this active object */
    QF_ACTIVE_INIT_(me, ie);                  /* execute initial transition */
}
/*..........................................................................*/
void QActive_stop_(QActive *me) {
    QActive_unsubscribeAll_(me);
    QF_remove_(me);
}

/*****************************************************************************
* NOTE01:
* This implemenation of QF_run() represents the non-preeemptive scheduer
* built into QF. This implementation is used only in the "vanilla" QF ports
* to "bare metal" target systems without any underlying RTOS or kernel.
*
* NOTE02:
* QF_onIdle() must be called with interrupts LOCKED because the determination
* of the idle condition (no events in the queues) can change at any time by
* an interrupt posting events to a queue. The QF_onIdle() MUST enable
* interrups internally, perhaps at the same time as putting the CPU into
* a power-saving mode.
*/
