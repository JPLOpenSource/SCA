/*****************************************************************************
* Product:  QK/C, port to 80x86, QK, Open Watcom
* Last Updated for Version: 4.1.01
* Date of the Last Update:  Nov 02, 2009
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
*
* Copyright (C) 2002-2009 Quantum Leaps, LLC. All rights reserved.
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
#include "qk_pkg.h"
#include "qassert.h"

/*..........................................................................*/
/*lint -e970 -e971 */      /* ignore MISRA rules 13 and 14 in this function */
char const *QK_getPortVersion(void) {
    return "4.1.01";
}
/*..........................................................................*/
void QK_init(void) {
    /* nothing to be done for 80x86/DOS */
}
