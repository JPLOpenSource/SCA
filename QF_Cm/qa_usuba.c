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

Q_DEFINE_THIS_MODULE(qa_usuba)

/*..........................................................................*/
/** \ingroup qf
* \file qa_usuba.c
* \brief QActive_unsubscribeAll_() definition.
*/

/*..........................................................................*/
void QActive_unsubscribeAll_(QActive const *me) {
    uint8_t p = me->prio__;
    uint8_t i;
    QSignal sig;

    Q_REQUIRE(((uint8_t)0 < p) && (p <= (uint8_t)QF_MAX_ACTIVE)
              && (QF_active_[p] == me));

    i = QF_div8Lkup[p];
    for (sig = (QSignal)Q_USER_SIG; sig < QF_maxSignal_; ++sig) {
        QF_INT_LOCK_KEY_
        QF_INT_LOCK_();
        if ((QF_subscrList_[sig].bits[i] & Q_ROM_BYTE(QF_pwr2Lkup[p])) != 0) {

            QS_BEGIN_NOLOCK_(QS_QF_ACTIVE_UNSUBSCRIBE, QS_aoObj_, me);
                QS_TIME_();                                    /* timestamp */
                QS_SIG_(sig);                   /* the signal of this event */
                QS_OBJ_(me);                          /* this active object */
            QS_END_NOLOCK_();
                                                  /* clear the priority bit */
            QF_subscrList_[sig].bits[i] &= Q_ROM_BYTE(QF_invPwr2Lkup[p]);
        }
        QF_INT_UNLOCK_();
    }
}
