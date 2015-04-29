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

Q_DEFINE_THIS_MODULE(qf_pspub)

/*..........................................................................*/
/** \ingroup qf
* \file qf_pspub.c
* \brief QF_publish() implementation.
*/

/*..........................................................................*/
void QF_publish(QEvent const *e) {
    uint8_t n = (uint8_t)0;        /* for computing # subscribers to e->sig */

    QF_SCHED_LOCK();/*perform multicasting with the scheduler locked, NOTE01*/

      /* make sure that the published signal is within the configured range */
    Q_REQUIRE(e->sig < QF_maxSignal_);

#if (QF_MAX_ACTIVE <= 8)
    {
        uint8_t tmp = QF_subscrList_[e->sig].bits[0];
        while (tmp != (uint8_t)0) {
            uint8_t p = Q_ROM_BYTE(QF_log2Lkup[tmp]);
            tmp &= Q_ROM_BYTE(QF_invPwr2Lkup[p]);   /* clear subscriber bit */
            Q_ASSERT(QF_active_[p] != (QActive *)0);  /* must be registered */

                               /* internally asserts if the queue overflows */
            QActive_postFIFO(QF_active_[p], e);
            ++n;                           /* one more reference to event e */
        }
    }
#else
    {
        uint8_t i = Q_DIM(QF_subscrList_[0].bits);
        do {               /* go through all bytes in the subscription list */
            uint8_t tmp;
            --i;
            tmp = QF_subscrList_[e->sig].bits[i];
            while (tmp != (uint8_t)0) {
                uint8_t p = Q_ROM_BYTE(QF_log2Lkup[tmp]);
                tmp &= Q_ROM_BYTE(QF_invPwr2Lkup[p]);/*clear subscriber bit */
                p = (uint8_t)(p + (i << 3));         /* adjust the priority */
                Q_ASSERT(QF_active_[p] != (QActive *)0);/*must be registered*/

                               /* internally asserts if the queue overflows */
                QActive_postFIFO(QF_active_[p], e);
                ++n;                       /* one more reference to event e */
            }
        } while (i != (uint8_t)0);
    }
#endif

    if (n != (uint8_t)0) {                              /* any subscribers? */

        QS_BEGIN_(QS_QF_PUBLISH, (void *)0, (void *)0);
            QS_TIME_();                                    /* the timestamp */
            QS_SIG_(e->sig);                     /* the signal of the event */
            QS_U8_(e->attrQF__);             /* the attributes of the event */
            QS_U8_(n);            /* the number of subscribers to the event */
        QS_END_();

        QF_SCHED_UNLOCK();
    }
    else {               /* no subscribers -- recycle the event immediately */

        QS_BEGIN_(QS_QF_PUBLISH_ATTEMPT, (void *)0, (void *)0);
            QS_TIME_();                                    /* the timestamp */
            QS_SIG_(e->sig);                     /* the signal of the event */
            QS_U8_(e->attrQF__);             /* the attributes of the event */
        QS_END_();

        QF_SCHED_UNLOCK();
        QF_gc(e); /* determine if the event is garbage and collect it if so */
    }
}

/*****************************************************************************
* NOTE01:
* QF_publish() uses scheduler locking to prevent other active objects from
* running while the event multicasting is in progress. During the publication
* the subscription lists are assumed not to be changing. The latter
* assumption is true as long as QActive_subscribe_()/unsubscribe_()/
* unsubscribeAll_() are NOT called from interrupts, because only interrupts
* can preempt the current task wile the scheduler is locked.
*
*/
