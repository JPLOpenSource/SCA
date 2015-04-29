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
#include "qassert.h"

Q_DEFINE_THIS_MODULE(qa_lifo)

/**
* \file
* \ingroup qf
* \brief QActive_postLIFO() definition.
*
* \note this source file is only included in the QF library when the native
* QF active object queue is used (instead of a message queue of an RTOS).
*/

/*..........................................................................*/
void QActive_postLIFO(QActive *me, QEvent const *e) {
    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();

    QS_BEGIN_NOLOCK_(QS_QF_ACTIVE_POST_LIFO, QS_aoObj_, me)
        QS_TIME_();                                            /* timestamp */
        QS_SIG_(e->sig);                        /* the signal of this event */
        QS_OBJ_(me);                                  /* this active object */
        QS_U8_(EVT_POOL_ID(e));                 /* the pool Id of the event */
        QS_U8_(EVT_REF_CTR(e));               /* the ref count of the event */
        QS_EQC_(me->eQueue.nFree);                /* number of free entries */
        QS_EQC_(me->eQueue.nMin);             /* min number of free entries */
    QS_END_NOLOCK_()

    if (EVT_POOL_ID(e) != (uint8_t)0) {              /* is it a pool event? */
        EVT_INC_REF_CTR(e);              /* increment the reference counter */
    }

    if (me->eQueue.frontEvt == (QEvent *)0) {               /* empty queue? */
        me->eQueue.frontEvt = e;                  /* deliver event directly */
        QACTIVE_EQUEUE_SIGNAL_(me);               /* signal the event queue */
    }
    else {            /* queue is not empty, leave event in the ring-buffer */
            /* the queue must be able to accept the event (cannot overflow) */
        Q_ASSERT(me->eQueue.nFree != (QEQueueCtr)0);

        ++me->eQueue.tail;
        if (me->eQueue.tail == me->eQueue.end) {  /* need to wrap the tail? */
            me->eQueue.tail = (QEQueueCtr)0;                 /* wrap around */
        }

        me->eQueue.ring[me->eQueue.tail] = me->eQueue.frontEvt;
        me->eQueue.frontEvt = e;                      /* put event to front */

        --me->eQueue.nFree;                 /* update number of free events */
        if (me->eQueue.nMin > me->eQueue.nFree) {
            me->eQueue.nMin = me->eQueue.nFree;    /* update minimum so far */
        }
    }
    QF_INT_UNLOCK_();
}

