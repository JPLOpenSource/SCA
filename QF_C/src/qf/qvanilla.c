/*****************************************************************************
* Product: QF/C
* Last Updated for Version: 4.1.05
* Date of the Last Update:  Oct 26, 2010
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
*
* Copyright (C) 2002-2010 Quantum Leaps, LLC. All rights reserved.
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
#include "qassert.h"

Q_DEFINE_THIS_MODULE(qvanilla)

/**
* \file
* \ingroup qf
* \brief "vanilla" cooperative kernel,
* QActive_start(), QActive_stop(), and QF_run() implementation.
*/

/* Package-scope objects ---------------------------------------------------*/
#if (QF_MAX_ACTIVE <= 8)
    QPSet8  volatile QF_readySet_;        /* QF-ready set of active objects */
#else
    QPSet64 volatile QF_readySet_;        /* QF-ready set of active objects */
#endif

/*..........................................................................*/
char const Q_ROM * Q_ROM_VAR QF_getPortVersion(void) {
    static char const Q_ROM Q_ROM_VAR version[] =  "4.1.05";
    return version;
}
/*..........................................................................*/
void QF_init(void) {
    /* nothing to do for the "vanilla" kernel */
}
/*..........................................................................*/
void QF_stop(void) {
    QF_onCleanup();                                     /* cleanup callback */
    /* nothing else to do for the "vanilla" kernel */
}
/*..........................................................................*/
// [SWC] Like GW in QF-C, make this routine return if all events are consumed.
//       Remove QF::onIdle().
void QF_run(void) {
    uint8_t p;
    QActive *a;
    QEvent const *e;
    QF_INT_LOCK_KEY_

    QF_onStartup();                                     /* startup callback */

    for (;;) {                                       /* the background loop */
        QF_INT_LOCK_();

#if (QF_MAX_ACTIVE <= 8)
        if (QPSet8_notEmpty(&QF_readySet_)) {
            QPSet8_findMax(&QF_readySet_, p);
#else
        if (QPSet64_notEmpty(&QF_readySet_)) {
            QPSet64_findMax(&QF_readySet_, p);
#endif
            a = QF_active_[p];
            QF_INT_UNLOCK_();

            e = QActive_get_(a);          /* get the next event for this AO */
            QF_ACTIVE_DISPATCH_(&a->super, e);        /* dispatch to the AO */
            QF_gc(e); /* determine if event is garbage and collect it if so */
        }
        else {
            QF_INT_UNLOCK_();
            break;
        }
    }
}
/*..........................................................................*/
void QActive_start(QActive *me, uint8_t prio,
                   QEvent const *qSto[], uint32_t qLen,
                   void *stkSto, uint32_t stkSize,
                   QEvent const *ie)
{
    Q_REQUIRE(((uint8_t)0 < prio) && (prio <= (uint8_t)QF_MAX_ACTIVE)
              && (stkSto == (void *)0));   /* does not need per-actor stack */

    (void)stkSize;         /* avoid the "unused parameter" compiler warning */
    QEQueue_init(&me->eQueue, qSto, (QEQueueCtr)qLen);/* initialize QEQueue */
    me->prio = prio;           /* set the QF priority of this active object */
    QF_add_(me);                     /* make QF aware of this active object */
    QF_ACTIVE_INIT_(&me->super, ie);          /* execute initial transition */

    QS_FLUSH();                       /* flush the trace buffer to the host */
}
/*..........................................................................*/
void QActive_stop(QActive *me) {
    QF_remove_(me);
}

/*****************************************************************************
* NOTE01:
* QF_onIdle() must be called with interrupts LOCKED because the determination
* of the idle condition (no events in the queues) can change at any time by
* an interrupt posting events to a queue. The QF_onIdle() MUST enable
* interrups internally, perhaps at the same time as putting the CPU into
* a power-saving mode.
*/
