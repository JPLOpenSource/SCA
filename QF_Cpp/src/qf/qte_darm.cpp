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

/// \file
/// \ingroup qf
/// \brief QTimeEvt::disarm() implementation.

//............................................................................
// NOTE: disarm a time evt (no harm in disarming an already disarmed time evt)
uint8_t QTimeEvt::disarm(void) {
    uint8_t wasArmed;
    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();
    if (m_prev != (QTimeEvt *)0) {        // is the time event actually armed?
        wasArmed = (uint8_t)1;
        if (this == QF_timeEvtListHead_) {
            QF_timeEvtListHead_ = m_next;
        }
        else {
            if (m_next != (QTimeEvt *)0) {        // not the last in the list?
                m_next->m_prev = m_prev;
            }
            m_prev->m_next = m_next;
        }
        m_prev = (QTimeEvt *)0;             // mark the time event as disarmed

        QS_BEGIN_NOLOCK_(QS_QF_TIMEEVT_DISARM, QS::teObj_, this)
            QS_TIME_();                                           // timestamp
            QS_OBJ_(this);                           // this time event object
            QS_OBJ_(m_act);                               // the active object
            QS_TEC_(m_ctr);                             // the number of ticks
            QS_TEC_(m_interval);                               // the interval
        QS_END_NOLOCK_()
    }
    else {                                     // the time event was not armed
        wasArmed = (uint8_t)0;

        QS_BEGIN_NOLOCK_(QS_QF_TIMEEVT_DISARM_ATTEMPT, QS::teObj_, this)
            QS_TIME_();                                           // timestamp
            QS_OBJ_(this);                           // this time event object
            QS_OBJ_(m_act);                               // the active object
        QS_END_NOLOCK_()
    }
    QF_INT_UNLOCK_();
    return wasArmed;
}
