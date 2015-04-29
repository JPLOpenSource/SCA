//////////////////////////////////////////////////////////////////////////////
// Product: QF/C++
// Last Updated for Version: 4.0.01
// Date of the Last Update:  Jun 09, 2008
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
#ifndef qf_pkg_h
#define qf_pkg_h

/// \file
/// \ingroup qf
/// \brief Internal (package scope) QF/C++ interface.

#include "qf_port.h"                                                // QF port

                                    // QF-specific interrupt locking/unlocking
#ifndef QF_INT_KEY_TYPE

    /// \brief This is an internal macro for defining the interrupt lock key.
    ///
    /// The purpose of this macro is to enable writing the same code for the
    /// case when interrupt key is defined and when it is not. If the macro
    /// #QF_INT_KEY_TYPE is defined, this internal macro provides the
    /// definition of the lock key variable. Otherwise this macro is empty.
    /// \sa #QF_INT_KEY_TYPE
    #define QF_INT_LOCK_KEY_

    /// \brief This is an internal macro for locking interrupts.
    ///
    /// The purpose of this macro is to enable writing the same code for the
    /// case when interrupt key is defined and when it is not. If the macro
    /// #QF_INT_KEY_TYPE is defined, this internal macro invokes #QF_INT_LOCK
    /// passing the key variable as the parameter. Otherwise #QF_INT_LOCK
    /// is invoked with a dummy parameter.
    /// \sa #QF_INT_LOCK, #QK_INT_LOCK
    #define QF_INT_LOCK_()      QF_INT_LOCK(dummy)

    /// \brief This is an internal macro for unlocking interrupts.
    ///
    /// The purpose of this macro is to enable writing the same code for the
    /// case when interrupt key is defined and when it is not. If the macro
    /// #QF_INT_KEY_TYPE is defined, this internal macro invokes
    /// #QF_INT_UNLOCK passing the key variable as the parameter.
    /// Otherwise #QF_INT_UNLOCK is invoked with a dummy parameter.
    /// \sa #QF_INT_UNLOCK, #QK_INT_UNLOCK
    #define QF_INT_UNLOCK_()    QF_INT_UNLOCK(dummy)
#else
    #define QF_INT_LOCK_KEY_    QF_INT_KEY_TYPE intLockKey_;
    #define QF_INT_LOCK_()      QF_INT_LOCK(intLockKey_)
    #define QF_INT_UNLOCK_()    QF_INT_UNLOCK(intLockKey_)
#endif

// package-scope objects -----------------------------------------------------
extern QTimeEvt *QF_timeEvtListHead_;  ///< head of linked list of time events
extern QF_EPOOL_TYPE_ QF_pool_[3];                 ///< allocate 3 event pools
extern uint8_t QF_maxPool_;                  ///< # of initialized event pools
extern QSubscrList *QF_subscrList_;             ///< the subscriber list array
extern QSignal QF_maxSignal_;                ///< the maximum published signal

//............................................................................
/// \brief Structure representing a free block in the Native QF Memory Pool
/// \sa ::QMPool
struct QFreeBlock {
    QFreeBlock *m_next;
};

#endif                                                             // qf_pkg_h

