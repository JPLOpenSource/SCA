/*****************************************************************************
* Product: QEP/C
* Last Updated for Version: 4.2.00
* Date of the Last Update:  Jun 29, 2011
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
#include "qep_pkg.h"

/**
* \file
* \ingroup qep
* \brief QEP_reservedEvt_ definition and QEP_getVersion() implementation.
*/

/* Package-scope objects ---------------------------------------------------*/
QEvent const QEP_reservedEvt_[] = {
    { (QSignal)QEP_EMPTY_SIG_, (uint8_t)0, (uint8_t)0 },
    { (QSignal)Q_ENTRY_SIG,    (uint8_t)0, (uint8_t)0 },
    { (QSignal)Q_EXIT_SIG,     (uint8_t)0, (uint8_t)0 },
    { (QSignal)Q_INIT_SIG,     (uint8_t)0, (uint8_t)0 }
};

/*..........................................................................*/
/*lint -e970 -e971 */      /* ignore MISRA rules 13 and 14 in this function */
char const Q_ROM * Q_ROM_VAR QEP_getVersion(void) {
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
