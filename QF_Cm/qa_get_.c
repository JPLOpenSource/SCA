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

Q_DEFINE_THIS_MODULE(qa_get_)

/*..........................................................................*/
/** \ingroup qf
* \file qa_get_.c
* \brief QActive_get_() definition. NOTE: this function is linked
* when the native QF event queue is used (rather than a message queue
* of an RTOS).
*/

/*..........................................................................*/
QEvent const *QActive_get_(QActive *me) {
    QEvent const *e;
    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();

    QACTIVE_OSOBJECT_WAIT_(me);        /* wait for event to arrive directly */

    e = me->eQueue__.frontEvt__;

    if (me->eQueue__.nUsed__ != (QEQueueCtr)0) {/*any events in the buffer? */
                                                    /* remove from the tail */
        me->eQueue__.frontEvt__ = me->eQueue__.ring__[me->eQueue__.tail__];
        ++me->eQueue__.tail__;
        if (me->eQueue__.tail__ == me->eQueue__.end__) {    /* wrap around? */
            me->eQueue__.tail__ = (QEQueueCtr)0;           /* wrap the tail */
        }

        --me->eQueue__.nUsed__;        /* one less event in the ring buffer */

        QS_BEGIN_NOLOCK_(QS_QF_ACTIVE_GET, QS_aoObj_, me);
            QS_TIME_();                                        /* timestamp */
            QS_SIG_(e->sig);                    /* the signal of this event */
            QS_OBJ_(me);                              /* this active object */
            QS_U8_(e->attrQF__);           /* the QF attribute of the event */
            QS_EQC_(me->eQueue__.nUsed__);        /* number of used entries */
        QS_END_NOLOCK_();
    }
    else {
        me->eQueue__.frontEvt__ = (QEvent const *)0; /* queue becomes empty */
        QACTIVE_OSOBJECT_ONIDLE_(me);

        QS_BEGIN_NOLOCK_(QS_QF_ACTIVE_GET_LAST, QS_aoObj_, me);
            QS_TIME_();                                        /* timestamp */
            QS_SIG_(e->sig);                    /* the signal of this event */
            QS_OBJ_(me);                              /* this active object */
            QS_U8_(e->attrQF__);           /* the QF attribute of the event */
        QS_END_NOLOCK_();
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
    margin = (uint32_t)(QF_active_[prio]->eQueue__.nTot__
                        - QF_active_[prio]->eQueue__.nMax__);
    QF_INT_UNLOCK_();

    return margin;
}
