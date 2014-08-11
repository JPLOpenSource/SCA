/*****************************************************************************
* Product: QF/C
* Last Updated for Version: 4.0.00
* Date of the Last Update:  Apr 07, 2008
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
*
* Copyright (C) 2002-2008 Quantum Leaps, LLC. All rights reserved.
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

Q_DEFINE_THIS_MODULE(qte_rarm)

/**
* \file
* \ingroup qf
* \brief QTimeEvt_rearm() implementation.
*/

/*..........................................................................*/
uint8_t QTimeEvt_rearm(QTimeEvt *me, QTimeEvtCtr nTicks) {
    uint8_t isArmed;
    QF_INT_LOCK_KEY_

    Q_REQUIRE((nTicks > (QTimeEvtCtr)0)        /* cannot rearm with 0 ticks */
              && (me->super.sig >= (QSignal)Q_USER_SIG)     /* valid signal */
              && (me->act != (QActive *)0)); /* active object must be valid */

    QF_INT_LOCK_();
    me->ctr = nTicks;
    if (me->prev == (QTimeEvt *)0) {        /* is this time event disarmed? */
        isArmed = (uint8_t)0;
        me->next = QF_timeEvtListHead_;
        if (QF_timeEvtListHead_ != (QTimeEvt *)0) {
            QF_timeEvtListHead_->prev = me;
        }
        QF_timeEvtListHead_ = me;
        me->prev = me;                        /* mark the time event in use */
    }
    else {                                       /* the time event is armed */
        isArmed = (uint8_t)1;
    }

    QS_BEGIN_NOLOCK_(QS_QF_TIMEEVT_REARM, QS_teObj_, me)
        QS_TIME_();                                            /* timestamp */
        QS_OBJ_(me);                              /* this time event object */
        QS_OBJ_(me->act);                              /* the active object */
        QS_TEC_(me->ctr);                            /* the number of ticks */
        QS_TEC_(me->interval);                              /* the interval */
        QS_U8_(isArmed);                            /* was the timer armed? */
    QS_END_NOLOCK_()

    QF_INT_UNLOCK_();
    return isArmed;
}
