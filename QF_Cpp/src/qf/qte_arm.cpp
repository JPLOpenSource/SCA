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

Q_DEFINE_THIS_MODULE(qte_arm)

/// \file
/// \ingroup qf
/// \brief QF_timeEvtListHead_ definition and QTimeEvt::arm_() implementation.

// Package-scope objects -----------------------------------------------------
QTimeEvt *QF_timeEvtListHead_;           // head of linked list of time events

//............................................................................
void QTimeEvt::arm_(QActive *act, QTimeEvtCtr nTicks) {
    Q_REQUIRE((nTicks > (QTimeEvtCtr)0)          /* cannot arm with 0 ticks */
              && (sig >= (QSignal)Q_USER_SIG)               /* valid signal */
              && (m_prev == (QTimeEvt *)0)   /* time event must NOT be used */
              && (act != (QActive *)0));  /* active object must be provided */
    m_ctr = nTicks;
    m_prev = this;                                    // mark the timer in use
    m_act = act;
    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();

    QS_BEGIN_NOLOCK_(QS_QF_TIMEEVT_ARM, QS::teObj_, this)
        QS_TIME_();                                               // timestamp
        QS_OBJ_(this);                               // this time event object
        QS_OBJ_(act);                                     // the active object
        QS_TEC_(nTicks);                                // the number of ticks
        QS_TEC_(m_interval);                                   // the interval
    QS_END_NOLOCK_()

    m_next = QF_timeEvtListHead_;
    if (QF_timeEvtListHead_ != (QTimeEvt *)0) {
        QF_timeEvtListHead_->m_prev = this;
    }
    QF_timeEvtListHead_ = this;
    QF_INT_UNLOCK_();
}
