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

Q_DEFINE_THIS_MODULE(qhsm_ini)

/*..........................................................................*/
/** \ingroup qep
* \file qhsm_ini.c
* \brief QHsm_init() implementation.
*/
/*..........................................................................*/
void QHsm_init(QHsm *me, QEvent const *e) {
    QHsmState s;
                /* this state machine must be initialized with QHsm_ctor_() */
    Q_REQUIRE(((QFsm *)me)->state__.fsm != (QState)0);

    s = &QHsm_top;                        /* an HSM starts in the top state */
                                          /* trigger the initial transition */
    (*((QFsm *)me)->state__.fsm)((QFsm *)me, e);

    do {                                        /* drill into the target... */
        QHsmState path[QEP_MAX_NEST_DEPTH_];
        int8_t ip = (int8_t)0;               /* transition entry path index */
        QHsmState t = ((QFsm *)me)->state__.hsm;

        QS_BEGIN_(QS_QEP_STATE_INIT, QS_smObj_, me);
            QS_OBJ_(me);                       /* this state machine object */
            QS_FUN_(s);                                 /* the source state */
            QS_FUN_(((QFsm *)me)->state__.hsm);               /* the target */
        QS_END_();

        path[0] = t;
        for (t = QEP_TRIG_(t, QEP_EMPTY_SIG_); t != s;
             t = QEP_TRIG_(t, QEP_EMPTY_SIG_))
        {
            path[++ip] = t;
        }
                                            /* entry path must not overflow */
        Q_ASSERT(ip < (int8_t)QEP_MAX_NEST_DEPTH_);

        do {        /* retrace the entry path in reverse (desired) order... */
                                                          /* enter path[ip] */
            if (QEP_TRIG_(path[ip], Q_ENTRY_SIG) == (QHsmState)0) {

                QS_BEGIN_(QS_QEP_STATE_ENTRY, QS_smObj_, me);
                    QS_OBJ_(me);               /* this state machine object */
                    QS_FUN_(path[ip]);                 /* the entered state */
                QS_END_();
            }
        } while (--ip >= (int8_t)0);

        s = ((QFsm *)me)->state__.hsm;
    } while (QEP_TRIG_(s, Q_INIT_SIG) == (QHsmState)0);

    QS_BEGIN_(QS_QEP_INIT_TRAN, QS_smObj_, me);
        QS_TIME_();                                           /* time stamp */
        QS_OBJ_(me);                           /* this state machine object */
        QS_FUN_(((QFsm *)me)->state__.hsm);         /* the new active state */
    QS_END_();
}
