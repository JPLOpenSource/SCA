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

Q_DEFINE_THIS_MODULE(qf_gc)

/*..........................................................................*/
/** \ingroup qf
* \file qf_gc.c
* \brief QF_gc() implementation.
*/

/*..........................................................................*/
void QF_gc(QEvent const *e) {
    if (e->attrQF__ != (uint8_t)0) {                 /* is it a pool event? */
        QF_INT_LOCK_KEY_
        QF_INT_LOCK_();

        if ((e->attrQF__ & 0x3F) > 1) {   /* isn't this the last reference? */

            /*lint -e1773                        Attempt to cast away const */
            --((QEvent *)e)->attrQF__;   /* decrement the reference counter */
                   /* NOTE: cast the 'const' away, which is legitimate because
                   * it's a pool event
                   */

            QS_BEGIN_NOLOCK_(QS_QF_GC_ATTEMPT, (void *)0, (void *)0);
                QS_TIME_();                                    /* timestamp */
                QS_SIG_(e->sig);                 /* the signal of the event */
                QS_U8_(e->attrQF__);         /* the attributes of the event */
            QS_END_NOLOCK_();

            QF_INT_UNLOCK_();
        }
        else {      /* this is the last reference to this event, recycle it */
                                                      /* cannot wrap around */
            uint8_t id = (uint8_t)((e->attrQF__ >> 6) - 1);

            QS_BEGIN_NOLOCK_(QS_QF_GC, (void *)0, (void *)0);
                QS_TIME_();                                    /* timestamp */
                QS_SIG_(e->sig);                 /* the signal of the event */
                QS_U8_(e->attrQF__);         /* the attributes of the event */
            QS_END_NOLOCK_();

            QF_INT_UNLOCK_();

            Q_ASSERT(id < QF_maxPool_);

            /*lint -e1773                        Attempt to cast away const */
            QF_EPOOL_PUT_(QF_pool_[id], (QEvent *)e); /* cast 'const' away, */
                          /* which is legitimate, because it's a pool event */
        }
    }
}
