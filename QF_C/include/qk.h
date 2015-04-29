/*****************************************************************************
* Product: QK/C platform-independent public interface
* Last Updated for Version: 4.0.03
* Date of the Last Update:  Dec 26, 2008
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
*
* Copyright (C) 2002-2008 Quantum Leaps, LLC. All rights reserved.
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
* e-mail:                  info@quantum-leaps.com
*****************************************************************************/
#ifndef qk_h
#define qk_h

/**
* \file
* \ingroup qk qf
* \brief QK/C platform-independent public interface.
*
* This header file must be included directly or indirectly
* in all modules (*.c files) that use QK/C.
*/

#include "qequeue.h"       /* The QK kernel uses the native QF event queue  */
#include "qmpool.h"        /* The QK kernel uses the native QF memory pool  */
#include "qpset.h"         /* The QK kernel uses the native QF priority set */

/****************************************************************************/
/* QF configuration for QK */

/** \brief This macro defines the type of the event queue used for the
* active objects.
*
* \note This is just an example of the macro definition. Typically, you need
* to define it in the specific QF port file (qf_port.h). In case of QK, which
* always depends on the native QF queue, this macro is defined at the level
* of the platform-independent interface qk.h.
*/
#define QF_EQUEUE_TYPE             QEQueue

#if defined(QK_TLS) || defined(QK_EXT_SAVE)
    /** \brief This macro defines the type of the OS-Object used for blocking
    * the native QF event queue when the queue is empty
    *
    * In QK, the OS object is used to hold the per-thread flags, which might
    * be used, for example, to rembember the thread attributes (e.g.,
    * if the thread uses a floating point co-processor). The OS object value
    * is set on per-thread basis in QActive_start(). Later, the extended
    * context switch macros (QK_EXT_SAVE() and QK_EXT_RESTORE()) might use
    * the per-thread flags to determine what kind of extended context switch
    * this particular thread needs (e.g., the thread might not be using the
    * coprocessor or might be using a different one).
    */
    #define QF_OS_OBJECT_TYPE      uint8_t

    /** \brief This macro defines the type of the thread handle used for the
    * active objects.
    *
    * The thread type in QK is the pointer to the thread-local storage (TLS).
    * This thread-local storage can be set on per-thread basis in
    * QActive_start(). Later, the QK scheduler uses this pointer for extended
    * context switch and passes the pointer to the macro #QK_TLS.
    */
    #define QF_THREAD_TYPE         void *
#endif                                             /* QK_TLS || QK_EXT_SAVE */

#if (QF_MAX_ACTIVE <= 8)
    extern QPSet8  volatile QK_readySet_;                 /**< QK ready-set */
#else
    extern QPSet64 volatile QK_readySet_;                 /**< QK ready-set */
#endif

/* public-scope objects */
extern uint8_t volatile QK_currPrio_;  /**< current task/interrupt priority */
extern uint8_t volatile QK_intNest_;           /**< interrupt nesting level */

/* QK active object queue implementation ...................................*/

/** \brief Platform-dependent macro defining how QF should block the calling
* task when the QF native queue is empty
*
* \note This is just an example of QACTIVE_EQUEUE_WAIT_ for the QK-port
* of QF. QK never activates a task that has no events to process, so in this
* case the macro asserts that the queue is not empty. In other QF ports you
* need to define the macro appropriately for the underlying kernel/OS you're
* using.
*/
#define QACTIVE_EQUEUE_WAIT_(me_) \
    Q_ASSERT((me_)->eQueue.frontEvt != (QEvent *)0)

#if (QF_MAX_ACTIVE <= 8)
    #define QACTIVE_EQUEUE_SIGNAL_(me_) \
        QPSet8_insert(&QK_readySet_, (me_)->prio); \
        if (QK_intNest_ == (uint8_t)0) { \
            QK_SCHEDULE_(); \
        } \
        else ((void)0)

    #define QACTIVE_EQUEUE_ONEMPTY_(me_) \
        QPSet8_remove(&QK_readySet_, (me_)->prio)
#else
    /** \brief Platform-dependent macro defining how QF should signal the
    * active object task that an event has just arrived.
    *
    * The macro is necessary only when the native QF event queue is used.
    * The signaling of task involves unblocking the task if it is blocked.
    *
    * \note QACTIVE_EQUEUE_SIGNAL_ is called from a critical section.
    * It might leave the critical section internally, but must restore
    * the critical section before exiting to the caller.
    *
    * \note This is just an example of QACTIVE_EQUEUE_SIGNAL_ for the
    * QK-port of QF. In other QF ports you need to define the macro
    * appropriately for the underlying kernel/OS you're using.
    */
    #define QACTIVE_EQUEUE_SIGNAL_(me_) \
        QPSet64_insert(&QK_readySet_, (me_)->prio); \
        if (QK_intNest_ == (uint8_t)0) { \
            QK_SCHEDULE_(); \
        } \
        else ((void)0)

    /** \brief Platform-dependent macro defining the action QF should take
    * when the native QF event queue becomes empty.
    *
    * The macro is necessary only when the native QF event queue is used.
    * The signaling of task involves unblocking the task if it is blocked.
    *
    * \note QACTIVE_EQUEUE_ONEMPTY_ is called from a critical section.
    * It should not leave the critical section.
    *
    * \note This is just an example of QACTIVE_EQUEUE_ONEMPTY_ for the
    * QK-port of QF. In other QF ports you need to define the macro
    * appropriately for the underlying kernel/OS you're using.
    */
    #define QACTIVE_EQUEUE_ONEMPTY_(me_) \
        QPSet64_remove(&QK_readySet_, (me_)->prio)
#endif


/* QK event pool operations ................................................*/

/** \brief This macro defines the type of the event pool used in this QF port.
*
* \note This is just an example of the macro definition. Typically, you need
* to define it in the specific QF port file (qf_port.h). In case of QK, which
* always depends on the native QF memory pool, this macro is defined at the
* level of the platform-independent interface qk.h.
*/
#define QF_EPOOL_TYPE_              QMPool

/** \brief Platform-dependent macro defining the event pool initialization
*
* \note This is just an example of QF_EPOOL_INIT_ for the QK-port of QF.
* In other QF ports you need to define the macro appropriately for the
* underlying kernel/OS you're using.
*/
#define QF_EPOOL_INIT_(p_, poolSto_, poolSize_, evtSize_) \
    QMPool_init(&(p_), poolSto_, poolSize_, evtSize_)

/** \brief Platform-dependent macro defining how QF should obtain the
* event pool block-size
*
* \note This is just an example of QF_EPOOL_EVENT_SIZE_ for the QK-port
* of QF. In other QF ports you need to define the macro appropriately for the
* underlying kernel/OS you're using.
*/
#define QF_EPOOL_EVENT_SIZE_(p_)    ((p_).blockSize)

/** \brief Platform-dependent macro defining how QF should obtain an event
* \a e_ from the event pool \a p_
*
* \note This is just an example of QF_EPOOL_GET_ for the QK-port of QF.
* In other QF ports you need to define the macro appropriately for the
* underlying kernel/OS you're using.
*/
#define QF_EPOOL_GET_(p_, e_)       ((e_) = (QEvent *)QMPool_get(&(p_)))

/** \brief Platform-dependent macro defining how QF should return an event
* \a e_ to the event pool \a p_
*
* \note This is just an example of QF_EPOOL_PUT_ for the QK-port of QF.
* In other QF ports you need to define the macro appropriately for the
* underlying kernel/OS you're using.
*/
#define QF_EPOOL_PUT_(p_, e_)       (QMPool_put(&(p_), (e_)))

/** \brief QK initialization
*
* QK_init() is called from QF_init() in qk.c. This function is
* defined in the QK ports.
*/
void QK_init(void);

/** \brief QK idle callback (customized in BSPs for QK)
*
* QK_onIdle() is called continously by the QK idle loop. This callback
* gives the application an opportunity to enter a power-saving CPU mode,
* or perform some other idle processing.
*
* \note QK_onIdle() is invoked with interrupts unlocked and must also
* return with interrupts unlocked.
*
* \sa QF_onIdle()
*/
void QK_onIdle(void);

/** \brief get the current QK version number string
*
* \return version of the QK as a constant 6-character string of the form
* x.y.zz, where x is a 1-digit major version number, y is a 1-digit minor
* version number, and zz is a 2-digit release number.
*
* \sa QK_getPortVersion()
*/
char const Q_ROM * Q_ROM_VAR QK_getVersion(void);

/** \brief Returns the QK-port version.
*
* This function returns constant version string in the format x.y.zz,
* where x (one digit) is the major version, y (one digit) is the minor
* version, and zz (two digits) is the maintenance release version.
* An example of the QK-port version string is "1.1.03".
*
* \sa QK_getVersion()
*/
char const Q_ROM * Q_ROM_VAR QK_getPortVersion(void);

#ifndef QK_NO_MUTEX

    /** \brief QK Mutex type.
    *
    * QMutex represents the priority-ceiling mutex available in QK.
    * \sa QK_mutexLock()
    * \sa QK_mutexUnlock()
    */
    typedef uint8_t QMutex;

    /** \brief QK priority-ceiling mutex lock
    *
    * Lock the QK scheduler up to the priority level \a prioCeiling.
    *
    * \note This function should be always paired with QK_mutexUnlock(). The
    * code between QK_mutexLock() and QK_mutexUnlock() should be kept to the
    * minimum.
    *
    * \include qk_mux.c
    */
    QMutex QK_mutexLock(uint8_t prioCeiling);

    /** \brief QK priority-ceiling mutex unlock
    *
    * \note This function should be always paired with QK_mutexLock(). The
    * code between QK_mutexLock() and QK_mutexUnlock() should be kept to the
    * minimum.
    *
    * \include qk_mux.c
    */
    void QK_mutexUnlock(QMutex mutex);

#endif                                                          /* QK_MUTEX */

/* QK scheduler and extended scheduler */
#ifndef QF_INT_KEY_TYPE
    void QK_schedule_(void);
    void QK_scheduleExt_(void);                    /* QK extended scheduler */

    #define QK_SCHEDULE_()      QK_schedule_()
#else

    /** \brief The QK scheduler
    *
    * \note The QK scheduler must be always called with the interrupts locked
    * and unlocks interrupts internally.
    *
    * The signature of QK_schedule_() depends on the policy of locking and
    * unlocking interrupts. When the interrupt lock key is not used
    * (QF_INT_KEY_TYPE undefined), the signature is as follows: \n
    * void QK_schedule_(void); \n
    *
    * However, when the interrupt key lock is used (QF_INT_KEY_TYPE defined),
    * the signature is different: \n
    * void QK_schedule_(QF_INT_KEY_TYPE intLockKey); \n
    *
    * For the internal use, these differences are hidden by the macro
    * QK_SCHEDULE_().
    */
    void QK_schedule_(QF_INT_KEY_TYPE intLockKey);

    /** \brief The QK extended scheduler for interrupt context
    *
    * \note The QK extended exscheduler must be always called with the
    * interrupts locked and unlocks interrupts internally.
    *
    * The signature of QK_scheduleExt_() depends on the policy of locking
    * and unlocking interrupts. When the interrupt lock key is not used
    * (QF_INT_KEY_TYPE undefined), the signature is as follows: \n
    * void QK_scheduleExt_(void); \n
    *
    * However, when the interrupt key lock is used (QF_INT_KEY_TYPE
    * defined), the signature is different: \n
    * void QK_scheduleExt_(QF_INT_KEY_TYPE intLockKey); \n
    */
    void QK_scheduleExt_(QF_INT_KEY_TYPE intLockKey); /* extended scheduler */

    /** \brief This is an internal macro for invoking QK scheduler with
    * interrupts locked.
    *
    * The purpose of this macro is to enable writing the same code for the
    * case when interrupt key is defined and when it is not. If the macro
    * QF_INT_KEY_TYPE is defined, this internal macro invokes QK_schedule_()
    * passing the key variable as the parameter. Otherwise QK_schedule_()
    * is invoked without parameters.
    */
    #define QK_SCHEDULE_()      QK_schedule_(intLockKey_)
#endif

/****************************************************************************/
/* QS software tracing integration, only if enabled                         */
#ifdef Q_SPY                                /* QS software tracing enabled? */
    #ifndef qs_h
    #include "qs_port.h"                                 /* include QS port */
    #endif
#else
    #ifndef qs_dummy_h
    #include "qs_dummy.h"                /* disable the QS software tracing */
    #endif
#endif                                                             /* Q_SPY */

#endif                                                              /* qk_h */
