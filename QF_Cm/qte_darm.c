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

/*..........................................................................*/
/** \ingroup qf
* \file qte_darm.c
* \brief QTimeEvt_disarm() implementation.
*/

/*..........................................................................*/
/* NOTE: disarm a timer (no harm in disarming an already disarmed timer)    */
uint8_t QTimeEvt_disarm(QTimeEvt *me) {
    uint8_t wasArmed;
    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();
    if (me->prev__ != (QTimeEvt *)0) { /* is the time event actually armed? */
        wasArmed = (uint8_t)1;
        if (me == QF_timeEvtListHead_) {
            QF_timeEvtListHead_ = me->next__;
        }
        else {
            if (me->next__ != (QTimeEvt *)0) { /* not the last in the list? */
                me->next__->prev__ = me->prev__;
            }
            me->prev__->next__ = me->next__;
        }
        me->prev__ = (QTimeEvt *)0;      /* mark the time event as disarmed */

        QS_BEGIN_NOLOCK_(QS_QF_TIMEEVT_DISARM, QS_teObj_, me);
            QS_TIME_();                                        /* timestamp */
            QS_OBJ_(me);                          /* this time event object */
            QS_OBJ_(me->act__);                        /* the active object */
            QS_TEC_(me->ctr__);                      /* the number of ticks */
            QS_TEC_(me->interval__);                        /* the interval */
        QS_END_NOLOCK_();
    }
    else {                                  /* the time event was not armed */
        wasArmed = (uint8_t)0;

        QS_BEGIN_NOLOCK_(QS_QF_TIMEEVT_DISARM_ATTEMPT, QS_teObj_, me);
            QS_TIME_();                                        /* timestamp */
            QS_OBJ_(me);                          /* this time event object */
            QS_OBJ_(me->act__);                        /* the active object */
        QS_END_NOLOCK_();
    }
    QF_INT_UNLOCK_();
    return wasArmed;
}
