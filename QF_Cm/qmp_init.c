/*****************************************************************************
* Product: QF/C
* Last Updated for Version: 3.3.00
* Date of the Last Update:  Jan 22, 2007
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
*
* Copyright (C) 2002-2007 Quantum Leaps, LLC. All rights reserved.
*
* This software may be distributed and modified under the terms of the GNU
* General Public License version 2 (GPL) as published by the Free Software
* Foundation and appearing in the file GPL.TXT included in the packaging of
* this file. Please note that GPL Section 2[b] requires that all works based
* on this software must also be made publicly available under the terms of
* the GPL ("Copyleft").
*
* Alternatively, this software may be distributed and modified under the
* terms of Quantum Leaps commercial licenses, which expressly supersede
* the GPL and are specifically designed for licensees interested in
* retaining the proprietary status of their code.
*
* Contact information:
* Quantum Leaps Web site:  http://www.quantum-leaps.com
* e-mail:                  sales@quantum-leaps.com
*****************************************************************************/
#include "qf_pkg.h"
#include "qassert.h"

Q_DEFINE_THIS_MODULE(qmp_init)

/*..........................................................................*/
/** \ingroup qf
* \file qmp_init.c
* \brief QMPool_init() implementation.
*/

/*..........................................................................*/
void QMPool_init(QMPool *me, void *poolSto,
                 uint32_t poolSize, QMPoolSize blockSize)
{
    QFreeBlock *fb = (QFreeBlock *)poolSto;

         /* the number of QFreeBlock* pointers that fit in one memory block */
    QMPoolSize n = (QMPoolSize)1;

     /* the blockSize must not be too close to the top of the dynamic range */
    Q_REQUIRE((QMPoolSize)(blockSize + (QMPoolSize)sizeof(QFreeBlock))
              > blockSize);

             /* round up the blockSize to fit an integer number of pointers */
    me->blockSize__ = (QMPoolSize)sizeof(QFreeBlock);/* start with just one */
    while (me->blockSize__ < blockSize) {
        me->blockSize__ += (QEventSize)sizeof(QFreeBlock);
        ++n;
    }
    blockSize = me->blockSize__;   /* use the rounded-up value from here on */

                       /* the whole pool buffer must fit at least one block */
    Q_ASSERT(poolSize >= (uint32_t)blockSize);

    me->start__ = poolSto;      /* the start of memory managed by this pool */
    me->free__ = poolSto;     /* set the head of linked-list of free blocks */

                             /* chain all blocks together in a free-list... */
    poolSize -= (uint32_t)blockSize;          /* don't chain the last block */
    me->nTot__ = (QMPoolCtr)1;          /* one (the last) block in the pool */
    while (poolSize >= (uint32_t)blockSize) {
        fb->next = &fb[n];                           /* setup the next link */
        fb = fb->next;                             /* advance to next block */
        poolSize -= (uint32_t)blockSize;
        ++me->nTot__;
    }
    me->end__ = fb;  /* end of memory managed by this pool (the last block) */

    fb->next = (QFreeBlock *)0;                /* the last link points to 0 */
    me->nFree__ = me->nTot__;                /* store number of free blocks */
    me->nMin__ = me->nTot__;           /* the minimum number of free blocks */

    QS_BEGIN_(QS_QF_MPOOL_INIT, QS_mpObj_, me->start__);
        QS_OBJ_(me->start__);            /* the memory managed by this pool */
        QS_MPC_(me->nTot__);                  /* the total number of blocks */
    QS_END_();
}
