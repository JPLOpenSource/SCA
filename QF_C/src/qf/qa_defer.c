/*****************************************************************************
* Product: QF/C
* Last Updated for Version: 4.2.00
* Date of the Last Update:  Jul 08, 2011
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

Q_DEFINE_THIS_MODULE(qa_defer)

/**
* \file
* \ingroup qf
* \brief QActive_defer() and QActive_recall() implementation.
*/

/*..........................................................................*/
void QActive_defer(QActive *me, QEQueue *eq, QEvent const *e) {
    (void)me;                 /* avoid compiler warning about 'me' not used */
    QEQueue_postFIFO(eq, e);
}
/*..........................................................................*/
uint8_t QActive_recall(QActive *me, QEQueue *eq) {
    QEvent const *e = QEQueue_get(eq);  /* get an event from deferred queue */
    if (e != (QEvent *)0) {                             /* event available? */
        QF_INT_LOCK_KEY_

        QActive_postLIFO(me, e);  /* post it to the front of the AO's queue */

        QF_INT_LOCK_();

        if (EVT_POOL_ID(e) != (uint8_t)0) {       /* is it a dynamic event? */

            /* after posting to the AO's queue the event must be referenced
            * at least twice: once in the deferred event queue (eq->get()
            * did NOT decrement the reference counter) and once in the
            * AO's event queue.
            */
            Q_ASSERT(EVT_REF_CTR(e) > (uint8_t)1);

            /* we need to decrement the reference counter once, to account
            * for removing the event from the deferred event queue.
            */
            EVT_DEC_REF_CTR(e);          /* decrement the reference counter */
        }

        QF_INT_UNLOCK_();

        return (uint8_t)1;                                /* event recalled */
    }
    else {
        return (uint8_t)0;                            /* event not recalled */
    }
}
