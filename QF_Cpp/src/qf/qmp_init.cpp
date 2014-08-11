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

Q_DEFINE_THIS_MODULE(qmp_init)

/// \file
/// \ingroup qf
/// \brief QMPool::init() implementation.

//............................................................................
void QMPool::init(void *poolSto, uint32_t poolSize, QMPoolSize blockSize) {
    // The memory block must be valid
    // and the poolSize must fit at least one free block
    // and the blockSize must not be too close to the top of the dynamic range
    Q_REQUIRE((poolSto != (void *)0)
              && (poolSize >= (uint32_t)sizeof(QFreeBlock))
              && ((QMPoolSize)(blockSize + (QMPoolSize)sizeof(QFreeBlock))
                    > blockSize));

    //lint -e923                       ignore MISRA Rule 45 in this expression
    uint32_t corr = ((uint32_t)poolSto
                      & ((uint32_t)sizeof(QFreeBlock) - (uint32_t)1));
    if (corr != (uint32_t)0) {                            // alignment needed?
        corr = (uint32_t)sizeof(QFreeBlock) - corr; // amount to align poolSto
        poolSize -= corr;                    // reduce the available pool size
    }
    //lint -e826   align the head of free list at the free block-size boundary
    m_free = (void *)((uint8_t *)poolSto + corr);

                // round up the blockSize to fit an integer number of pointers
    m_blockSize = (QMPoolSize)sizeof(QFreeBlock);       // start with just one
    uint32_t nblocks = (uint32_t)1;// # free blocks that fit in a memory block
    while (m_blockSize < blockSize) {
        m_blockSize += (QMPoolSize)sizeof(QFreeBlock);
        ++nblocks;
    }
    blockSize = m_blockSize;          // use the rounded-up value from here on

               // the whole pool buffer must fit at least one rounded-up block
    Q_ASSERT(poolSize >= (uint32_t)blockSize);

                                // chain all blocks together in a free-list...
    poolSize -= (uint32_t)blockSize;             // don't chain the last block
    m_nTot     = (QMPoolCtr)1;             // one (the last) block in the pool
    QFreeBlock *fb = (QFreeBlock *)m_free;//start at the head of the free list
    while (poolSize >= (uint32_t)blockSize) {
        fb->m_next = &fb[nblocks];                      // setup the next link
        fb = fb->m_next;                              // advance to next block
        poolSize -= (uint32_t)blockSize;     // reduce the available pool size
        ++m_nTot;                     // increment the number of blocks so far
    }

    fb->m_next = (QFreeBlock *)0;              // the last link points to NULL
    m_nFree    = m_nTot;                                // all blocks are free
    m_nMin     = m_nTot;                  // the minimum number of free blocks
    m_start    = poolSto;               // the original start this pool buffer
    m_end      = fb;                            // the last block in this pool

    QS_INT_LOCK_KEY_
    QS_BEGIN_(QS_QF_MPOOL_INIT, QS::mpObj_, m_start)
        QS_OBJ_(m_start);                   // the memory managed by this pool
        QS_MPC_(m_nTot);                         // the total number of blocks
    QS_END_()
}
