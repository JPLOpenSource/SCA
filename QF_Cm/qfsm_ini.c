/*****************************************************************************
* Product: QEP/C
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
#include "qep_pkg.h"
#include "qassert.h"

Q_DEFINE_THIS_MODULE(qfsm_ini)

/*..........................................................................*/
/** \ingroup qep
* \file qfsm_ini.c
* \brief QFsm_init() implementation.
*/
/*..........................................................................*/
void QFsm_init(QFsm *me, QEvent const *e) {
    QState initial;
    Q_REQUIRE(me->state__.fsm != (QState)0);         /* must be initialized */
    initial = me->state__.fsm;              /* save the initial pseudostate */

    QS_BEGIN_(QS_QEP_STATE_INIT, QS_smObj_, me);
        QS_OBJ_(me);                           /* this state machine object */
        QS_FUN_((QState)0);           /* the source (not defined for a FSM) */
        QS_FUN_(me->state__.fsm);           /* the target of the transition */
    QS_END_();

    (*initial)(me, e);                    /* trigger the initial transition */
    Q_ASSERT(initial != me->state__.fsm);     /* cannot stay in the initial */
    (*me->state__.fsm)(me, &QEP_reservedEvt_[Q_ENTRY_SIG]); /* enter target */

    QS_BEGIN_(QS_QEP_INIT_TRAN, QS_smObj_, me);
        QS_TIME_();                                           /* time stamp */
        QS_OBJ_(me);                           /* this state machine object */
        QS_FUN_(me->state__.fsm);                   /* the new active state */
    QS_END_();
}
