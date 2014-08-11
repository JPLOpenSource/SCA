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

Q_DEFINE_THIS_MODULE(qmp_put)

/// \file
/// \ingroup qf
/// \brief QMPool::put() implementation.

//............................................................................
void QMPool::put(void *b) {
    //lint -e946 -e1904             ignore MISRA Rule 103 in this precondition
    Q_REQUIRE((m_start <= b) && (b <= m_end)           /*  must be in range */
              && (m_nFree <= m_nTot));        // # free blocks must be < total

    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();

    ((QFreeBlock *)b)->m_next = (QFreeBlock *)m_free;//link into the free list
    m_free = b;                            // set as new head of the free list
    ++m_nFree;                             // one more free block in this pool

    QS_BEGIN_NOLOCK_(QS_QF_MPOOL_PUT, QS::mpObj_, m_start)
        QS_TIME_();                                               // timestamp
        QS_OBJ_(m_start);                   // the memory managed by this pool
        QS_MPC_(m_nFree);             // the number of free blocks in the pool
    QS_END_NOLOCK_()

    QF_INT_UNLOCK_();
}
    //lint -e946 -e1904             ignore MISRA Rule 103 in this precondition
