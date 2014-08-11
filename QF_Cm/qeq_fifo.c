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

Q_DEFINE_THIS_MODULE(qeq_fifo)

/*..........................................................................*/
/** \ingroup qf
* \file qeq_fifo.c
* \brief QEQueue_postFIFO() definition. NOTE: this function is used for
* the "raw" thread-safe queues and NOT for the queues of active objects.
*/

/*..........................................................................*/
void QEQueue_postFIFO(QEQueue *me, QEvent const *e) {
    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();

    QS_BEGIN_NOLOCK_(QS_QF_EQUEUE_POST_FIFO, QS_eqObj_, me);
        QS_TIME_();                                            /* timestamp */
        QS_SIG_(e->sig);                        /* the signal of this event */
        QS_OBJ_(me);                                   /* this queue object */
        QS_U8_(e->attrQF__);               /* the QF attribute of the event */
        QS_EQC_(me->nUsed__);                     /* number of used entries */
        QS_EQC_(me->nMax__);                  /* max number of used entries */
    QS_END_NOLOCK_();

    if (e->attrQF__ != (uint8_t)0) {                 /* is it a pool event? */
        ++((QEvent *)e)->attrQF__;       /* increment the reference counter */
                   /* NOTE: cast the 'const' away, which is legitimate because
                    * it's a pool event
                    */
    }

    if (me->frontEvt__ == (QEvent const *)0) {       /* is the queue empty? */
        me->frontEvt__ = e;                       /* deliver event directly */
    }
    else {            /* queue is not empty, leave event in the ring-buffer */
            /* the queue must be able to accept the event (cannot overflow) */
        Q_ASSERT(me->nUsed__ < me->nTot__);
        me->ring__[me->head__] = e;  /* insert event into the buffer (FIFO) */
        ++me->head__;
        if (me->head__ == me->end__) {
            me->head__ = (QEQueueCtr)0;                    /* wrap the head */
        }

        ++me->nUsed__;                           /* update number of events */
        if (me->nUsed__ > me->nMax__) {
            me->nMax__ = me->nUsed__;          /* store maximum used so far */
        }
    }
    QF_INT_UNLOCK_();
}
