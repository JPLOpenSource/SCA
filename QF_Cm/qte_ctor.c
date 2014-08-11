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

Q_DEFINE_THIS_MODULE(qte_ctor)

/*..........................................................................*/
/** \ingroup qf
* \file qte_ctor.c
* \brief QTimeEvt_ctor() implementation.
*/

/*..........................................................................*/
void QTimeEvt_ctor(QTimeEvt *me, QSignal sig) {
    Q_REQUIRE(sig >= (QSignal)Q_USER_SIG);                  /* valid signal */
    me->prev__ = (QTimeEvt *)0;
    me->next__ = (QTimeEvt *)0;
    me->act__  = (QActive *)0;
    me->ctr__  = (QTimeEvtCtr)0;
    me->interval__ = (QTimeEvtCtr)0;
    me->super_.sig = sig;
    me->super_.attrQF__ = (uint8_t)0;/*static event not from a pool, NOTE01 */
}

/*****************************************************************************
* NOTE01:
* Setting attrQF__ to zero is correct only for events not allocated from
* event pools. In the future releases of QF, time events actually could be
* allocated dynamically. However, for simplicity in this release of QF, time
* events are limited to be statically allocated.
*****************************************************************************/
