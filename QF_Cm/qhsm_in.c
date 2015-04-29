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
* \file qhsm_in.c
* \brief QHsm_isIn() implementation.
*/
/*..........................................................................*/
uint8_t QHsm_isIn(QHsm *me, QHsmState state) {
    QHsmState s = ((QFsm *)me)->state__.hsm;
    uint8_t isIn = (uint8_t)0;    /* assume that this HSM is not in 'state' */
    do {
        if (s == state) {                           /* do the states match? */
            isIn = (uint8_t)1;                  /* match found, return TRUE */
            s = (QHsmState)0;                      /* break out of the loop */
        }
        else {
            s = QEP_TRIG_(s, QEP_EMPTY_SIG_);
        }
    } while (s != (QHsmState)0);
    return isIn;                                       /* return the status */
}
