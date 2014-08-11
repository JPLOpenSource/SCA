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

Q_DEFINE_THIS_MODULE(qmp_get)

/// \file
/// \ingroup qf
/// \brief QMPool::get() and QF::getPoolMargin() implementation.

//............................................................................
void *QMPool::get(void) {
    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();

    QFreeBlock *fb = (QFreeBlock *)m_free;         // get a free block or NULL
    if (fb != (QFreeBlock *)0) {                      // free block available?
        m_free = fb->m_next;          // adjust list head to the next free block
        --m_nFree;                                      // one free block less
        if (m_nMin > m_nFree) {
            m_nMin = m_nFree;                   // remember the minimum so far
        }
    }

    QS_BEGIN_NOLOCK_(QS_QF_MPOOL_GET, QS::mpObj_, m_start)
        QS_TIME_();                                               // timestamp
        QS_OBJ_(m_start);                   // the memory managed by this pool
        QS_MPC_(m_nFree);             // the number of free blocks in the pool
        QS_MPC_(m_nMin);     // the mninimum number of free blocks in the pool
    QS_END_NOLOCK_()

    QF_INT_UNLOCK_();
    return fb;               // return the block or NULL pointer to the caller
}
//............................................................................
uint32_t QF::getPoolMargin(uint8_t poolId) {
    Q_REQUIRE(((uint8_t)1 <= poolId) && (poolId <= QF_maxPool_));

    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();
    uint32_t margin = (uint32_t)QF_pool_[poolId - (uint8_t)1].m_nMin;
    QF_INT_UNLOCK_();

    return margin;
}
