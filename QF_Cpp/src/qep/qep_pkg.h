//////////////////////////////////////////////////////////////////////////////
// Product: QEP/C++
// Last Updated for Version: 4.0.00
// Date of the Last Update:  Apr 06, 2008
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
#ifndef qep_pkg_h
#define qep_pkg_h

/// \file
/// \ingroup qep
/// \brief Internal (package scope) QEP/C++ interface.

#include "qep_port.h"                                              // QEP port

extern QEvent const QEP_reservedEvt_[];      ///< preallocated reserved events

/// internal QEP constants
enum QEPConst {
    QEP_EMPTY_SIG_ = 0,                ///< empty signal for internal use only

    /// maximum depth of state nesting (including the top level), must be >= 3
    QEP_MAX_NEST_DEPTH_ = 6
};

/// helper macro to trigger internal event in an HSM
#define QEP_TRIG_(state_, sig_) \
    ((*(state_))(this, &QEP_reservedEvt_[sig_]))

/// helper macro to trigger entry action in an HSM
#define QEP_EXIT_(state_) \
    if (QEP_TRIG_(state_, Q_EXIT_SIG) == Q_RET_HANDLED) { \
        QS_BEGIN_(QS_QEP_STATE_EXIT, QS::smObj_, this) \
            QS_OBJ_(this); \
            QS_FUN_(state_); \
        QS_END_() \
    }

/// helper macro to trigger exit action in an HSM
#define QEP_ENTER_(state_) \
    if (QEP_TRIG_(state_, Q_ENTRY_SIG) == Q_RET_HANDLED) { \
        QS_BEGIN_(QS_QEP_STATE_ENTRY, QS::smObj_, this) \
            QS_OBJ_(this); \
            QS_FUN_(state_); \
        QS_END_() \
    }

#endif                                                            // qep_pkg_h
