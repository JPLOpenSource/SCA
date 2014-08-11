/*****************************************************************************
* Product: QF/C
* Last Updated for Version: 3.3.00
* Date of the Last Update:  Jan 22, 2007
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
*
* Copyright (C) 2002-2006 Quantum Leaps, LLC. All rights reserved.
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

Q_DEFINE_THIS_MODULE(qeq_get)

/*..........................................................................*/
/** \ingroup qf
* \file qeq_get.c
* \brief QEQueue_get() definition. NOTE: this function is used for
* the "raw" thread-safe queues and NOT for the queues of active objects.
*/

/*..........................................................................*/
QEvent const *QEQueue_get(QEQueue *me) {
    QEvent const *e;
    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();
    if (me->frontEvt__ == (QEvent const *)0) {       /* is the queue empty? */
        e = (QEvent const *)0;           /* no event available at this time */
    }
    else {                                        /* the queue is not empty */
        e = me->frontEvt__;

        if (me->nUsed__ != (QEQueueCtr)0) {/*any events in the ring buffer? */
            me->frontEvt__ = me->ring__[me->tail__];/* remove from the tail */
            ++me->tail__;
            if (me->tail__ == me->end__) {          /* need to wrap around? */
                me->tail__ = (QEQueueCtr)0;                /* wrap the tail */
            }

            --me->nUsed__;             /* one less event in the ring buffer */

            QS_BEGIN_NOLOCK_(QS_QF_EQUEUE_GET, QS_eqObj_, me);
                QS_TIME_();                                    /* timestamp */
                QS_SIG_(e->sig);                /* the signal of this event */
                QS_OBJ_(me);                           /* this queue object */
                QS_U8_(e->attrQF__);       /* the QF attribute of the event */
                QS_EQC_(me->nUsed__);             /* number of used entries */
            QS_END_NOLOCK_();
        }
        else {
            me->frontEvt__ = (QEvent const *)0;  /* the queue becomes empty */

            QS_BEGIN_NOLOCK_(QS_QF_EQUEUE_GET_LAST, QS_eqObj_, me);
                QS_TIME_();                                    /* timestamp */
                QS_SIG_(e->sig);                /* the signal of this event */
                QS_OBJ_(me);                           /* this queue object */
                QS_U8_(e->attrQF__);       /* the QF attribute of the event */
            QS_END_NOLOCK_();
        }
    }
    QF_INT_UNLOCK_();
    return e;
}
