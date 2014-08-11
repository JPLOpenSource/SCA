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

Q_DEFINE_THIS_MODULE(qte_arm)

/*..........................................................................*/
/** \ingroup qf
* \file qte_arm.c
* \brief QF_timeEvtListHead_ definition and QTimeEvt_arm__() implementation.
*/

/* Package-scope objects ---------------------------------------------------*/
QTimeEvt *QF_timeEvtListHead_;        /* head of linked list of time events */

/*..........................................................................*/
void QTimeEvt_arm__(QTimeEvt *me, QActive *act, QTimeEvtCtr nTicks) {
    QF_INT_LOCK_KEY_
    Q_REQUIRE((nTicks > (QTimeEvtCtr)0)  /* cannot arm a timer with 0 ticks */
              && (((QEvent *)me)->sig >= (QSignal)Q_USER_SIG)/*valid signal */
              && (me->prev__ == (QTimeEvt *)0));/*time evt must NOT be used */
    me->ctr__ = nTicks;
    me->prev__ = me;                               /* mark the timer in use */
    me->act__ = act;

    QF_INT_LOCK_();

    QS_BEGIN_NOLOCK_(QS_QF_TIMEEVT_ARM, QS_teObj_, me);
        QS_TIME_();                                            /* timestamp */
        QS_OBJ_(me);                              /* this time event object */
        QS_OBJ_(act);                                  /* the active object */
        QS_TEC_(nTicks);                             /* the number of ticks */
        QS_TEC_(me->interval__);                            /* the interval */
    QS_END_NOLOCK_();

    me->next__ = QF_timeEvtListHead_;
    if (QF_timeEvtListHead_ != (QTimeEvt *)0) {
        QF_timeEvtListHead_->prev__ = me;
    }
    QF_timeEvtListHead_ = me;
    QF_INT_UNLOCK_();
}
