//////////////////////////////////////////////////////////////////////////////
// Product: QEP/C++
// Last Updated for Version: 4.0.04
// Date of the Last Update:  Apr 08, 2009
//
//                    Q u a n t u m     L e a P s
//                    ---------------------------
//                    innovating embedded systems
//
// Copyright (C) 2002-2009 Quantum Leaps, LLC. All rights reserved.
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
#include "qep_pkg.h"
#include "qassert.h"

Q_DEFINE_THIS_MODULE(qhsm_ini)

/// \file
/// \ingroup qep
/// \brief QHsm::init() implementation.

//............................................................................
QHsm::~QHsm() {
}
//............................................................................
void QHsm::init(QEvent const *e) {
    QStateHandler t;
    QS_INT_LOCK_KEY_

                              // the top-most initial transition must be taken
    Q_ALLEGE((*m_state)(this, e) == Q_RET_TRAN);

    t = (QStateHandler)&QHsm::top;              // HSM starts in the top state
    do {                                           // drill into the target...
        QStateHandler path[QEP_MAX_NEST_DEPTH_];
        int8_t ip = (int8_t)0;                  // transition entry path index


        QS_BEGIN_(QS_QEP_STATE_INIT, QS::smObj_, this)
            QS_OBJ_(this);                        // this state machine object
            QS_FUN_(t);                                    // the source state
            QS_FUN_(m_state);          // the target of the initial transition
        QS_END_()

        path[0] = m_state;
        (void)QEP_TRIG_(m_state, QEP_EMPTY_SIG_);
        while (m_state != t) {
            path[++ip] = m_state;
            (void)QEP_TRIG_(m_state, QEP_EMPTY_SIG_);
        }
        m_state = path[0];
                                               // entry path must not overflow
        Q_ASSERT(ip < (int8_t)QEP_MAX_NEST_DEPTH_);

        do {           // retrace the entry path in reverse (desired) order...
            QEP_ENTER_(path[ip]);                            // enter path[ip]
        } while ((--ip) >= (int8_t)0);

        t = path[0];                   // current state becomes the new source
    } while (QEP_TRIG_(t, Q_INIT_SIG) == Q_RET_TRAN);
    m_state = t;

    QS_BEGIN_(QS_QEP_INIT_TRAN, QS::smObj_, this)
        QS_TIME_();                                              // time stamp
        QS_OBJ_(this);                            // this state machine object
        QS_FUN_(m_state);                              // the new active state
    QS_END_()
}
