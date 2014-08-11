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

/*..........................................................................*/
/** \ingroup qep
* \file qfsm_dis.c
* \brief QFsm_dispatch() implementation.
*/
void QFsm_dispatch(QFsm *me, QEvent const *e) {
    QState s = me->state__.fsm;
    (*s)(me, e);          /* process the event in the current state handler */
    if (me->tran__ != Q_TRAN_NONE_TYPE) {
        (*s)(me, &QEP_reservedEvt_[Q_EXIT_SIG]);         /* exit the source */
                                                        /* enter the target */
        (*me->state__.fsm)(me, &QEP_reservedEvt_[Q_ENTRY_SIG]);

        me->tran__ = Q_TRAN_NONE_TYPE;     /* get ready for next transition */

        QS_BEGIN_(QS_QEP_TRAN, QS_smObj_, me);
            QS_TIME_();                                       /* time stamp */
            QS_SIG_(e->sig);                     /* the signal of the event */
            QS_OBJ_(me);                       /* this state machine object */
            QS_FUN_(s);                     /* the source of the transition */
            QS_FUN_(me->state__.fsm);               /* the new active state */
        QS_END_();

    }
    else {
        QS_BEGIN_(QS_QEP_INTERN_TRAN, QS_smObj_, me);
            QS_TIME_();                                       /* time stamp */
            QS_SIG_(e->sig);                     /* the signal of the event */
            QS_OBJ_(me);                       /* this state machine object */
            QS_FUN_(s);                 /* the state that handled the event */
        QS_END_();
    }
}
