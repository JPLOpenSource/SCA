/*****************************************************************************
* Product:  QF/C
* Last Updated for Version: 3.3.00
* Date of the Last Update:  Jan 22, 2007
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
*
* Copyright (C) 2002-2006 Quantum Leaps, LLC. All rights reserved.
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
#ifndef qmpool_h
#define qmpool_h

/** \ingroup qf
* \file qmpool.h
* \brief platform-independent memory pool interface.
*
* This header file must be included in all QF ports that use native QF
* memory pool implementation.
*/

/****************************************************************************/
#ifndef QF_MPOOL_SIZ_SIZE
    /** \brief macro to override the default ::QMPoolSize size.
    * Valid values 1, 2, or 4; default 2
    */
    #define QF_MPOOL_SIZ_SIZE 2
#endif
#if (QF_MPOOL_SIZ_SIZE == 1)

    /** \brief The data type to store the block-size based on the macro
    * #QF_MPOOL_SIZ_SIZE.
    *
    * The dynamic range of this data type determines the maximum size
    * of blocks that can be managed by the native QF event pool.
    */
    typedef uint8_t QMPoolSize;
#elif (QF_MPOOL_SIZ_SIZE == 2)

    typedef uint16_t QMPoolSize;
#elif (QF_MPOOL_SIZ_SIZE == 4)
    typedef uint32_t QMPoolSize;
#else
    #error "QF_MPOOL_SIZ_SIZE defined incorrectly, expected 1, 2, or 4"
#endif

/****************************************************************************/
#ifndef QF_MPOOL_CTR_SIZE
    /** \brief macro to override the default ::QMPoolCtr size.
    * Valid values 1, 2, or 4; default 2
    */
    #define QF_MPOOL_CTR_SIZE 2
#endif
#if (QF_MPOOL_CTR_SIZE == 1)

    /** \brief The data type to store the block-counter based on the macro
    * #QF_MPOOL_CTR_SIZE.
    *
    * The dynamic range of this data type determines the maximum number
    * of blocks that can be stored in the pool.
    */
    typedef uint8_t QMPoolCtr;
#elif (QF_MPOOL_CTR_SIZE == 2)
    typedef uint16_t QMPoolCtr;
#elif (QF_MPOOL_CTR_SIZE == 4)
    typedef uint32_t QMPoolCtr;
#else
    #error "QF_MPOOL_CTR_SIZE defined incorrectly, expected 1, 2, or 4"
#endif

/****************************************************************************/
/** \brief a name for QMPoolTag struct
*
* This structure describes the native QF memory pool, which can be used as
* the event pool for dynamic event allocation, or as a fast, deterministic
* fixed block-size heap for any other objects in your application.
*
* The ::QMPool structure contains only data members for managing a memory
* pool, but does not contain the pool storage, which must be provided
* externally during the pool initialization.
*
* The native QF event pool is configured by defining the macro
* #QF_EPOOL_TYPE_ as QEQueue in the specific QF port header file.
*
* \sa ::QMPoolTag for the description of the data members
*/
typedef struct QMPoolTag QMPool;
/** \brief Native QF Memory Pool
* \sa QMPool
*/
struct QMPoolTag {
/* private attributes */

    /** start of the memory managed by this memory pool
    */
    void *start__;

    /** end of the memory managed by this memory pool
    */
    void *end__;

    /** The head of the linked list of free blocks
    */
    void *free__;

    /**  maximum block size (in bytes)
    */
    QMPoolSize blockSize__;

    /** total number of blocks
    */
    QMPoolCtr nTot__;

    /** number of free blocks remaining
    */
    QMPoolCtr nFree__;

    /** minimum number of free blocks ever present in this pool
    *
    * \note this attribute remembers the low watermark of the pool,
    * which provides a valuable information for sizing event pools.
    * \sa QF_getPoolMargin().
    */
    QMPoolCtr nMin__;
};

/* public functions: */

/** \brief Initializes the native QF event pool
*
* The parameters are as follows: \a me points to the ::QMPool struct to be
* initialized, \a poolSto is the pool storage, \a poolSize is the size
* of the pool storage in bytes, and \a blockSize is the block size of this
* pool.
*
* The caller of this method must make sure that the \a poolSto pointer
* is properly aligned. In particular, it must be possible to efficiently
* store a pointer at the location pointed to by \a poolSto.
* Internally, the QMPool_init() function rounds up the block size
* \a blockSize so that it can fit an integer number of pointers.
* This is done to achieve proper alignment of the blocks within the pool.
*
* \note Due to the rounding of block size the actual capacity of the pool
* might be less than (\a poolSize / \a blockSize). You can check the capacity
* of the pool by calling the QF_getPoolMargin() function.
*/
void QMPool_init(QMPool *me, void *poolSto,
                 uint32_t poolSize, QMPoolSize blockSize);

/** \brief Obtains a memory block from a memory pool.
*
* The only parameter \a me is a pointer to the ::QMPool from which the
* block is requested. The function returns a pointer to the allocated
* memory block or NULL if no free blocks are available.
*
* A allocated block must be returned to the same pool from which it has
* been allocated.
*
* This function can be called from any task level or ISR level.
*
* \note The memory pool \a me must be initialized before any events can
* be requested from it. Also, the QMPool_get() function uses internally a
* QF critical section, so you should be careful not to call it from within
* a critical section when nesting of critical section is not supported.
*
* \sa QMPool_put()
*/
void *QMPool_get(QMPool *me);

/** \brief Returns a memory block back to a memory pool.
*
* The first parameter \a me is a pointer to the ::QMPool to which the
* block is returned. The second parameter is the pointer to the returned
* memory block.
*
* This function can be called from any task level or ISR level.
*
* \note The block must be allocated from the same memory pool to which it
* is returned. The QMPool_put() function raises an assertion if the returned
* pointer to the block points outside of the original memory buffer managed
* by the memory pool. Also, the QMPool_put() function uses internally a
* QF critical section, so you should be careful not to call it from within
* a critical section when nesting of critical section is not supported.
*
* \sa QMPool_get()
*/
void QMPool_put(QMPool *me, void *b);

/* friend class QF; */

#endif                                                          /* qmpool_h */

