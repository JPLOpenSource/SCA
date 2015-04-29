//////////////////////////////////////////////////////////////////////////////
// Product: QF/C++
// Last Updated for Version: 4.0.00
// Date of the Last Update:  Apr 07, 2008
//
//                    Q u a n t u m     L e a P s
//                    ---------------------------
//                    innovating embedded systems
//
// Copyright (C) 2002-2008 Quantum Leaps, LLC. All rights reserved.
//
// This software may be distributed and modified under the terms of the GNU
// General Public License version 2 (GPL) as published by the Free Software
// Foundation and appearing in the file GPL.TXT included in the packaging of
// this file. Please note that GPL Section 2[b] requires that all works based
// on this software must also be made publicly available under the terms of
// the GPL ("Copyleft").
//
// Alternatively, this software may be distributed and modified under the
// terms of Quantum Leaps commercial licenses, which expressly supersede
// the GPL and are specifically designed for licensees interested in
// retaining the proprietary status of their code.
//
// Contact information:
// Quantum Leaps Web site:  http://www.quantum-leaps.com
// e-mail:                  info@quantum-leaps.com
//////////////////////////////////////////////////////////////////////////////
#include "qf_pkg.h"
#include "qassert.h"

Q_DEFINE_THIS_MODULE(qte_ctor)

/// \file
/// \ingroup qf
/// \brief QTimeEvt::QTimeEvt() implementation.

//............................................................................
QTimeEvt::QTimeEvt(QSignal s)
    : m_prev((QTimeEvt *)0),
      m_next((QTimeEvt *)0),
      m_act((QActive *)0),
      m_ctr((QTimeEvtCtr)0),
      m_interval((QTimeEvtCtr)0)
{
    Q_REQUIRE(s >= (QSignal)Q_USER_SIG);                       // valid signal
    sig = s;
    dynamic_ = (uint8_t)0;            // time event must be static, see NOTE01
}

//////////////////////////////////////////////////////////////////////////////
// NOTE01:
// Setting the dynamic_ attribute to zero is correct only for events not
// allocated from event pools. In the future releases of QF, time events
// actually could be allocated dynamically. However, for simplicity in this
// release of QF, time events are limited to be statically allocated.
//
