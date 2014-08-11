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

Q_DEFINE_THIS_MODULE(qa_usub)

/// \file
/// \ingroup qf
/// \brief QActive::unsubscribe() implementation.

//............................................................................
void QActive::unsubscribe(QSignal sig) const {
    uint8_t p = m_prio;
    Q_REQUIRE(((QSignal)Q_USER_SIG <= sig)
              && (sig < QF_maxSignal_)
              && ((uint8_t)0 < p) && (p <= (uint8_t)QF_MAX_ACTIVE)
              && (QF::active_[p] == this));

    uint8_t i = Q_ROM_BYTE(QF_div8Lkup[p]);

    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();

    QS_BEGIN_NOLOCK_(QS_QF_ACTIVE_UNSUBSCRIBE, QS::aoObj_, this)
        QS_TIME_();                                               // timestamp
        QS_SIG_(sig);                              // the signal of this event
        QS_OBJ_(this);                                   // this active object
    QS_END_NOLOCK_()
                                                     // clear the priority bit
    QF_subscrList_[sig].m_bits[i] &= Q_ROM_BYTE(QF_invPwr2Lkup[p]);
    QF_INT_UNLOCK_();
}
