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

Q_DEFINE_THIS_MODULE(qa_lifo)

/*..........................................................................*/
/** \ingroup qf
* \file qa_lifo.c
* \brief QActive_postLIFO() definition.
*/

/*..........................................................................*/
void QActive_postLIFO(QActive *me, QEvent const *e) {
    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();

    QS_BEGIN_NOLOCK_(QS_QF_ACTIVE_POST_LIFO, QS_aoObj_, me);
        QS_TIME_();                                            /* timestamp */
        QS_SIG_(e->sig);                        /* the signal of this event */
        QS_OBJ_(me);                                  /* this active object */
        QS_U8_(e->attrQF__);               /* the QF attribute of the event */
        QS_EQC_(me->eQueue__.nUsed__);            /* number of used entries */
        QS_EQC_(me->eQueue__.nMax__);         /* max number of used entries */
    QS_END_NOLOCK_();

    if (e->attrQF__ != (uint8_t)0) {                 /* is it a pool event? */
        /*lint -e1773                            Attempt to cast away const */
        ++((QEvent *)e)->attrQF__;       /* increment the reference counter */
                /* NOTE: cast the 'const' away, which is legitimate because */
                /* it's a pool event */
    }

    if (me->eQueue__.frontEvt__ == (QEvent const *)0) {     /* empty queue? */
        me->eQueue__.frontEvt__ = e;              /* deliver event directly */
        QACTIVE_OSOBJECT_SIGNAL_(me);   /* unblock the active object thread */
                   /* NOTE: the critical section is exited within the macro */
    }
    else {            /* queue is not empty, leave event in the ring-buffer */
            /* the queue must be able to accept the event (cannot overflow) */
        Q_ASSERT(me->eQueue__.nUsed__ < me->eQueue__.nTot__);
        if (me->eQueue__.tail__ == (QEQueueCtr)0) {/*need to wrap the tail? */
            me->eQueue__.tail__ = (QEQueueCtr)(me->eQueue__.end__ - 1);
        }
        else {
            --me->eQueue__.tail__;
        }
        me->eQueue__.ring__[me->eQueue__.tail__] = me->eQueue__.frontEvt__;
        me->eQueue__.frontEvt__ = e;                  /* put event to front */

        ++me->eQueue__.nUsed__;                  /* update number of events */
        if (me->eQueue__.nUsed__ > me->eQueue__.nMax__) {
            me->eQueue__.nMax__ = me->eQueue__.nUsed__; /* store max so far */
        }
        QF_INT_UNLOCK_();
    }
}

