/*****************************************************************************
* Product: QEP/C
* Last Updated for Version: 4.0.00
* Date of the Last Update:  Apr 06, 2008
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
#include "qep_pkg.h"

/**
* \file
* \ingroup qep
* \brief QFsm_dispatch() implementation.
*/

/*..........................................................................*/
void QFsm_dispatch(QFsm *me, QEvent const *e) {
    QStateHandler s = me->state;                  /* save the current state */
    QS_INT_LOCK_KEY_
    QState r = (*s)(me, e);                       /* call the event handler */

    if (r == Q_RET_TRAN) {                             /* transition taken? */

        QS_BEGIN_(QS_QEP_TRAN, QS_smObj_, me)
            QS_TIME_();                                       /* time stamp */
            QS_SIG_(e->sig);                     /* the signal of the event */
            QS_OBJ_(me);                       /* this state machine object */
            QS_FUN_(s);                     /* the source of the transition */
            QS_FUN_(me->state);                     /* the new active state */
        QS_END_()

        (void)QEP_TRIG_(s, Q_EXIT_SIG);                 /* exit the source */
        (void)QEP_TRIG_(me->state, Q_ENTRY_SIG);        /* enter the target */
    }
    else {                                          /* transition not taken */
#ifdef Q_SPY
        if (r == Q_RET_HANDLED) {

            QS_BEGIN_(QS_QEP_INTERN_TRAN, QS_smObj_, me)
                QS_TIME_();                                   /* time stamp */
                QS_SIG_(e->sig);                 /* the signal of the event */
                QS_OBJ_(me);                   /* this state machine object */
                QS_FUN_(s);             /* the state that handled the event */
            QS_END_()
        }
        else {

            QS_BEGIN_(QS_QEP_IGNORED, QS_smObj_, me)
                QS_TIME_();                                   /* time stamp */
                QS_SIG_(e->sig);                 /* the signal of the event */
                QS_OBJ_(me);                   /* this state machine object */
                QS_FUN_(me->state);                    /* the current state */
            QS_END_()

        }
#endif
    }
}
