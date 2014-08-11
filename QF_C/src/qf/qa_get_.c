/*****************************************************************************
* Product: QF/C
* Last Updated for Version: 4.2.00
* Date of the Last Update:  Jul 02, 2011
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
#include "qassert.h"

Q_DEFINE_THIS_MODULE(qa_get_)

/**
* \file
* \ingroup qf
* \brief QActive_get_() and QF_getQueueMargin() definitions.
*
* \note this source file is only included in the QF library when the native
* QF active object queue is used (instead of a message queue of an RTOS).
*/

/*..........................................................................*/
QEvent const *QActive_get_(QActive *me) {
    QEvent const *e;
    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();

    QACTIVE_EQUEUE_WAIT_(me);          /* wait for event to arrive directly */

    e = me->eQueue.frontEvt;

    if (me->eQueue.nFree != me->eQueue.end) {  /* any events in the buffer? */
                                              /* remove event from the tail */
        me->eQueue.frontEvt = me->eQueue.ring[me->eQueue.tail];
        if (me->eQueue.tail == (QEQueueCtr)0) {   /* need to wrap the tail? */
            me->eQueue.tail = me->eQueue.end;                /* wrap around */
        }
        --me->eQueue.tail;

        ++me->eQueue.nFree;       /* one more free event in the ring buffer */

        QS_BEGIN_NOLOCK_(QS_QF_ACTIVE_GET, QS_aoObj_, me)
            QS_TIME_();                                        /* timestamp */
            QS_SIG_(e->sig);                    /* the signal of this event */
            QS_OBJ_(me);                              /* this active object */
            QS_U8_(EVT_POOL_ID(e));             /* the pool Id of the event */
            QS_U8_(EVT_REF_CTR(e));           /* the ref count of the event */
            QS_EQC_(me->eQueue.nFree);            /* number of free entries */
        QS_END_NOLOCK_()
    }
    else {
        me->eQueue.frontEvt = (QEvent *)0;           /* queue becomes empty */
        QACTIVE_EQUEUE_ONEMPTY_(me);

        QS_BEGIN_NOLOCK_(QS_QF_ACTIVE_GET_LAST, QS_aoObj_, me)
            QS_TIME_();                                        /* timestamp */
            QS_SIG_(e->sig);                    /* the signal of this event */
            QS_OBJ_(me);                              /* this active object */
            QS_U8_(EVT_POOL_ID(e));             /* the pool Id of the event */
            QS_U8_(EVT_REF_CTR(e));           /* the ref count of the event */
        QS_END_NOLOCK_()
    }
    QF_INT_UNLOCK_();
    return e;
}
/*..........................................................................*/
uint32_t QF_getQueueMargin(uint8_t prio) {
    uint32_t margin;
    QF_INT_LOCK_KEY_

    Q_REQUIRE((prio <= (uint8_t)QF_MAX_ACTIVE)
              && (QF_active_[prio] != (QActive *)0));

    QF_INT_LOCK_();
    margin = (uint32_t)QF_active_[prio]->eQueue.nMin;
    QF_INT_UNLOCK_();

    return margin;
}
