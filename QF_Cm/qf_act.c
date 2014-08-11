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

Q_DEFINE_THIS_MODULE(qf_act)

/*..........................................................................*/
/** \ingroup qf
* \file qf_act.c
* \brief QF_active_[], QF_getVersion(), and QF_add()/QF_remove()
* implementation.
*/

/* public objects ----------------------------------------------------------*/
QActive *QF_active_[QF_MAX_ACTIVE + 1];      /* to be used by QF ports only */
uint8_t QF_intLockNest_;                    /* interrupt-lock nesting level */
uint8_t QF_isrNest_;                              /* ISR-call nesting level */

/*..........................................................................*/
/*lint -e970 -e971            ignore MISRA rules 13 and 14 in this function */
const char Q_ROM * Q_ROM_VAR QF_getVersion(void) {
    static char const Q_ROM Q_ROM_VAR version[] = "3.3.00";
    return version;
}
/*..........................................................................*/
void QF_add_(QActive *a) {
    uint8_t p = a->prio__;
    Q_REQUIRE(((uint8_t)0 < p)
              && (p <= (uint8_t)QF_MAX_ACTIVE)
              && (QF_active_[p] == (QActive *)0));

    QS_BEGIN_(QS_QF_ACTIVE_ADD, QS_aoObj_, a);
        QS_TIME_();                                            /* timestamp */
        QS_OBJ_(a);                                    /* the active object */
        QS_U8_(p);                     /* the priority of the active object */
    QS_END_();

    QF_active_[p] = a;
}
/*..........................................................................*/
void QF_remove_(QActive const *a) {
    uint8_t p = a->prio__;
    Q_REQUIRE(((uint8_t)0 < p) && (p <= (uint8_t)QF_MAX_ACTIVE));

    QS_BEGIN_(QS_QF_ACTIVE_REMOVE, QS_aoObj_, a);
        QS_TIME_();                                            /* timestamp */
        QS_OBJ_(a);                                    /* the active object */
        QS_U8_(p);                     /* the priority of the active object */
    QS_END_();

    QF_active_[p] = (QActive *)0;             /* free-up the priority level */
}
