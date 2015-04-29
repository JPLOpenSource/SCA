/*****************************************************************************
* Product: QF/C
* Last Updated for Version: 4.2.00
* Date of the Last Update:  Jul 05, 2011
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
*
* Copyright (C) 2002-2011 Quantum Leaps, LLC. All rights reserved.
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

Q_DEFINE_THIS_MODULE(qf_act)

/**
* \file
* \ingroup qf
* \brief QF_active_[], QF_getVersion(), and QF_add_()/QF_remove_()
* implementation.
*/

/* public objects ----------------------------------------------------------*/
QActive *QF_active_[QF_MAX_ACTIVE + 1];      /* to be used by QF ports only */
uint8_t QF_intLockNest_;                    /* interrupt-lock nesting level */

/*..........................................................................*/
/*lint -e970 -e971            ignore MISRA rules 13 and 14 in this function */
char const Q_ROM * Q_ROM_VAR QF_getVersion(void) {
    static char const Q_ROM Q_ROM_VAR version[] = {
        (char)(((QP_VERSION >> 12U) & 0xFU) + (uint8_t)'0'),
        '.',
        (char)(((QP_VERSION >>  8U) & 0xFU) + (uint8_t)'0'),
        '.',
        (char)(((QP_VERSION >>  4U) & 0xFU) + (uint8_t)'0'),
        (char)((QP_VERSION          & 0xFU) + (uint8_t)'0'),
        '\0'
    };
    return version;
}
/*..........................................................................*/
void QF_add_(QActive *a) {
    uint8_t p = a->prio;
    QF_INT_LOCK_KEY_

    Q_REQUIRE(((uint8_t)0 < p) && (p <= (uint8_t)QF_MAX_ACTIVE)
              && (QF_active_[p] == (QActive *)0));

    QF_INT_LOCK_();

    QF_active_[p] = a;      /* registger the active object at this priority */

    QS_BEGIN_NOLOCK_(QS_QF_ACTIVE_ADD, QS_aoObj_, a)
        QS_TIME_();                                            /* timestamp */
        QS_OBJ_(a);                                    /* the active object */
        QS_U8_(p);                     /* the priority of the active object */
    QS_END_NOLOCK_()

    QF_INT_UNLOCK_();
}
/*..........................................................................*/
void QF_remove_(QActive const *a) {
    uint8_t p = a->prio;
    QF_INT_LOCK_KEY_

    Q_REQUIRE(((uint8_t)0 < p) && (p <= (uint8_t)QF_MAX_ACTIVE)
              && (QF_active_[p] == a));

    QF_INT_LOCK_();

    QF_active_[p] = (QActive *)0;             /* free-up the priority level */

    QS_BEGIN_NOLOCK_(QS_QF_ACTIVE_REMOVE, QS_aoObj_, a)
        QS_TIME_();                                            /* timestamp */
        QS_OBJ_(a);                                    /* the active object */
        QS_U8_(p);                     /* the priority of the active object */
    QS_END_NOLOCK_()

    QF_INT_UNLOCK_();
}
