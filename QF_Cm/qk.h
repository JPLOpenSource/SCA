/*****************************************************************************
* Product: QK/C platform-independent public interface
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
#ifndef qk_h
#define qk_h

/** \ingroup qk qf
* \file qk.h
* \brief QK/C platform-independent public interface.
*
* This header file must be included directly or indirectly
* in all modules (*.c files) that use QK/C.
*/

/****************************************************************************/
/* QK-specific interrupt locking/unlocking */
#ifndef QK_INT_KEY_TYPE
    void QK_schedule_(void);
    void QK_scheduleExt_(void);                    /* QK extended scheduler */

    #define QK_SCHEDULE_()    QK_schedule_()
#else

    /** \brief The QK scheduler
    *
    * \note The QK scheduler must be always called with the interrupts locked
    * and unlocks interrupts internally.
    *
    * The signature of QK_schedule_() depends on the policy of locking and
    * unlocking interrupts. When the interrupt lock key is not used
    * (#QK_INT_KEY_TYPE undefined), the signature is as follows: \n
    * void QK_schedule_(void); \n
    *
    * However, when the interrupt key lock is used (#QK_INT_KEY_TYPE defined),
    * the signature is different: \n
    * void QK_schedule_(QK_INT_KEY_TYPE intLockKey); \n
    *
    * For the internal use, these differences are hidden by the macro
    * #QK_SCHEDULE_.
    */
    void QK_schedule_(QK_INT_KEY_TYPE intLockKey);

    /** \brief The QK extended scheduler for interrupt context
    *
    * \note The QK extended exscheduler must be always called with the
    * interrupts locked and unlocks interrupts internally.
    *
    * The signature of QK_scheduleExt_() depends on the policy of locking
    * and unlocking interrupts. When the interrupt lock key is not used
    * (#QK_INT_KEY_TYPE undefined), the signature is as follows: \n
    * void QK_scheduleExt_(void); \n
    *
    * However, when the interrupt key lock is used (#QK_INT_KEY_TYPE
    * defined), the signature is different: \n
    * void QK_scheduleExt_(QK_INT_KEY_TYPE intLockKey); \n
    */
    void QK_scheduleExt_(QK_INT_KEY_TYPE intLockKey); // QK extended scheduler

    /** \brief This is an internal macro for invoking QK scheduler with
    * interrupts locked.
    *
    * The purpose of this macro is to enable writing the same code for the
    * case when interrupt key is defined and when it is not. If the macro
    * #QK_INT_KEY_TYPE is defined, this internal macro invokes QK_schedule_()
    * passing the key variable as the parameter. Otherwise QK_schedule_()
    * is invoked without parameters.
    * \sa #QK_INT_LOCK, #QK_INT_UNLOCK
    */
    #define QK_SCHEDULE_()    QK_schedule_(intLockKey__)
#endif

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
* \note This function should be always paired with QK_mutexUnlock(). The code
* between QK_mutexLock() and QK_mutexUnlock() should be kept to the minimum.
*
* \include qk_mux.c
*/
QMutex QK_mutexLock(uint8_t prioCeiling);

/** \brief QK priority-ceiling mutex unlock
*
* \note This function should be always paired with QK_mutexLock(). The code
* between QK_mutexLock() and QK_mutexUnlock() should be kept to the minimum.
*
* \include qk_mux.c
*/
void QK_mutexUnlock(QMutex mutex);

/* public-scope objects */
extern uint8_t QK_currPrio_;    /**< current priority of the executing task */
extern struct QPSetTag QK_readySet_;                      /**< QK ready-set */

/** The highest priority level (above any task) for setting ::QK_currPrio_
* inside an ISR.
*/
#define QK_ISR_PRIO                 ((uint8_t)0xFF)

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
#define QF_EQUEUE_TYPE              QEQueue

/** \brief This macro defines the type of the OS-Object used for blocking
* the native QF event queue when the queue is empty
*
* In QK, the OS object is used to hold the per-thread flags, which might
* be used, for example, to rembember if the thread needs an extended
* context switch (e.g., uses a floating point co-processor).
*/
#define QF_OS_OBJECT_TYPE           uint8_t

/** \brief This macro defines the type of the thread handle used for the
* active objects.
*
* In QK, the thread type is the pointer to the thread-local storage (TLS).
* This thread-local storage can be set on per-thread basis in
* QActive::start(). Later, the QK scheduler, passes the pointer to the
* thread-local storage to the macro #QK_TLS.
*/
#define QF_THREAD_TYPE             void *

                                  /* set interrupt locking/unlocking for QF */
#ifdef QK_INT_KEY_TYPE
    /** \brief If defined, this macro specifies the type of the interrupt
    * lock key.
    *
    * The interrupt lock key is a temporary variable that holds the interrupt
    * status. The status is stored upon locking the interrupts in
    * #QF_INT_LOCK and then restored from the key in #QF_INT_UNLOCK.
    *
    * Leaving the interrupt lock key undefined defaults to the simplest
    * interrupt locking policy, which unconditionally locks interrupts
    * in #QF_INT_LOCK and unconditionally unlocks them in #QF_INT_UNLOCK.
    * This simple policy does not support nesting of critical sections.
    *
    * \note This is just an example of #QF_INT_KEY_TYPE for the QK-port of QF.
    * In other QF ports you need to define the macro appropriately for the
    * CPU/compiler you're using.
    *
    * \note The interrupt locking/unlocking policy must be CONSISTENT across
    * all QP components. (NOTE: QS defines a separate interrupt locking policy
    * because it might also be used independently from the other QP
    * components.) The consistent policy is established in the QP port files,
    * such as qep_port.h, qf_port.h, qk_port.h, and qs_port.h, which are all
    * grouped in the same directory for the port.
    */
    #define QF_INT_KEY_TYPE         QK_INT_KEY_TYPE
#endif

/** \brief The macro defines the mechanism of locking the interrupts.
*
* The argument \a key_ is the optional interrupt key that might be used
* to save the interrupt status before locking. The \a key_ argument must
* be used if the macro #QF_INT_KEY_TYPE is defined, and must not be used
* if the macro is undefined.
*
* \note This is just an example of #QF_INT_LOCK for the QK-port of QF.
* In other QF ports you need to define the macro appropriately for the
* CPU/compiler you're using.
*
* \note The #QF_INT_LOCK macro must be always paired with #QF_INT_UNLOCK.
* You should keep the code between these two macros to the minimum.
*/
#define QF_INT_LOCK(key_) do { \
    QK_INT_LOCK(key_); \
    QF_QS_INT_LOCK(); \
} while (0)

/** \brief The macro defines the mechanism of unlocking the interrupts.
*
* The argument \a key_ is the optional interrupt key that might be used
* to restore the interrupt status. The \a key_ argument must be used if the
* macro #QF_INT_KEY_TYPE is defined, and must not be used if the macro is
* undefined.
*
* \note This is just an example of #QF_INT_UNLOCK for the QK-port of QF.
* In other QF ports you need to define the macro appropriately for the
* CPU/compiler you're using.
*
* \note The #QF_INT_UNLOCK macro must be always paired with #QF_INT_LOCK.
* You should keep the code between these two macros to the minimum.
*/
#define QF_INT_UNLOCK(key_) do { \
    QF_QS_INT_UNLOCK(); \
    QK_INT_UNLOCK(key_); \
} while (0)

/* QK scheduler locking/unlocking ..........................................*/

/** \brief This macro defines how QF should lock the scheduler
*
* Locking the scheduler prevents task rescheduling until the scheduler is
* unlocked again in the macro #QF_SCHED_UNLOCK. The task that locks the
* scheduler keeps control of the CPU even though other higher-priority
* tasks might be ready to run. However, interrupts are still serviced.
*
* \note This is just an example of #QF_SCHED_LOCK for the QK-port of QF.
* In other QF ports you need to define the macro appropriately for the
* CPU/compiler you're using.
*
* \note The #QF_SCHED_LOCK macro must be always paired with #QF_SCHED_UNLOCK.
* You should keep the code between these two macros to the minimum.
*/
#define QF_SCHED_LOCK()     \
   QMutex mutex__ = QK_mutexLock((uint8_t)(QF_MAX_ACTIVE + 1))

/** \brief This macro defines how QF should unlock the scheduler
*
* \sa Please see description for #QF_SCHED_LOCK.
*
* \note This is just an example of #QF_SCHED_LOCK for the QK-port of QF.
* In other ports of QF you need to define the macro appropriately for the
* CPU/compiler you're using.
*/
#define QF_SCHED_UNLOCK()   QK_mutexUnlock(mutex__)

/* QK active object queue implementation ...................................*/

/** \brief Platform-dependent macro defining how QF should block the calling
* task when the QF native queue is empty
*
* \note This is just an example of #QACTIVE_OSOBJECT_WAIT_ for the QK-port
* of QF. QK never activates a task that has no events to process, so in this
* case the macro asserts that the queue is not empty. In other QF ports you
* need to define the macro appropriately for the underlying kernel/OS you're
* using.
*/
#define QACTIVE_OSOBJECT_WAIT_(me_) \
    Q_ASSERT((me_)->eQueue__.frontEvt__ != (QEvent *)0)

/** \brief Platform-dependent macro defining how QF should signal the
* active object task that an event has just arrived.
*
* The macro is necessary only when the native QF event queue is used.
* The signaling of task involves unblocking the task if it is blocked.
*
* \note This is just an example of #QACTIVE_OSOBJECT_SIGNAL_ for the QK-port
* of QF. In other QF ports you need to define the macro appropriately for the
* underlying kernel/OS you're using.
*/
#define QACTIVE_OSOBJECT_SIGNAL_(me_) do { \
    QPSet_insert(&QK_readySet_, (me_)->prio__); \
    if (QK_currPrio_ <= (uint8_t)QF_MAX_ACTIVE) { \
        QK_SCHEDULE_(); \
    } \
    QF_INT_UNLOCK_(); \
} while (0)

/** \brief Platform-dependent macro defining the action QF should take
* when the native QF event queue becomes empty.
*
* \note This is just an example of #QACTIVE_OSOBJECT_ONIDLE_ for the QK-port
* of QF. In other QF ports you need to define the macro appropriately for the
* underlying kernel/OS you're using.
*/
#define QACTIVE_OSOBJECT_ONIDLE_(me_) \
    QPSet_remove(&QK_readySet_, (me_)->prio__)


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
* \note This is just an example of #QF_EPOOL_INIT_ for the QK-port of QF.
* In other QF ports you need to define the macro appropriately for the
* underlying kernel/OS you're using.
*/
#define QF_EPOOL_INIT_(p_, poolSto_, poolSize_, evtSize_) \
    QMPool_init(&(p_), poolSto_, poolSize_, evtSize_)

/** \brief Platform-dependent macro defining how QF should obtain the
* event pool block-size
*
* \note This is just an example of #QF_EPOOL_EVENT_SIZE_ for the QK-port
* of QF. In other QF ports you need to define the macro appropriately for the
* underlying kernel/OS you're using.
*/
#define QF_EPOOL_EVENT_SIZE_(p_)    ((p_).blockSize__)

/** \brief Platform-dependent macro defining how QF should obtain an event
* \a e_ from the event pool \a p_
*
* \note This is just an example of #QF_EPOOL_GET_ for the QK-port of QF.
* In other QF ports you need to define the macro appropriately for the
* underlying kernel/OS you're using.
*/
#define QF_EPOOL_GET_(p_, e_)       ((e_) = (QEvent *)QMPool_get(&(p_)))

/** \brief Platform-dependent macro defining how QF should return an event
* \a e_ to the event pool \a p_
*
* \note This is just an example of #QF_EPOOL_PUT_ for the QK-port of QF.
* In other QF ports you need to define the macro appropriately for the
* underlying kernel/OS you're using.
*/
#define QF_EPOOL_PUT_(p_, e_)       (QMPool_put(&(p_), (e_)))

/****************************************************************************/
/**
\page qk_rev QK/C Revision History

\section qk_3_3_00 Version 3.3.00 (Product)
Release date: Jan 22, 2007\n

The main change in this release is adding the macro #Q_ROM_VAR in QEP
for all constant objects allocated in ROM. The #Q_ROM_VAR macro has been
added for the compilers like Freescale HC(S)08, which require far pointers
to access the objects in ROM. Please note that specifying the pointer size
for accessing a ROM objects is syntactically different than specifying
that the object is allocated in ROM (see macro #Q_ROM).\n
\n
The other significant change in release 3.3.00 is simplification of the
build strategy for QP ports. Instead of separate Makefile for every QP
component, such as QEP, QF, QK, and QS, not the "ports" directory contains
a batch file "make.bat" that builds all the libraries at once.
\n

-# in file qk.h added macro #Q_ROM_VAR for objects allocated in ROM
and to signatures of functions accessing these objects.
-# In file qk.c added #Q_ROM_VAR to the signature of QK_getVersion().
-# In file qk.c updated version number to 3.3.00
-# Updated the "QP Programmer's Manaul" to Revision E


\section qk_3_2_04 Version 3.2.04
Release date: Dec 01, 2006\n

This QK release adds two new features in QK.\n

The first feature added is the extended context switch for CPUs with
co-processors, such as the x87 FPU accompanying the x86 CPU. As a fully-
preemptive kernel, QK needs to save and restore the context of the co-
processor accrosss the asynchronous preemption. This QK release adds
a generic mechanism for saving and restoring extened context in the
extended scheduler (QK_scheduleExt_()), which is used only at the
exit from the interrupts (asynchronous preemptions).\n

The second feature added is the Thread-Local Storage (TLS) for reentrant
libraries, such as the NewLib. This feature allows assigning per-thread
memory and providing a hook (callback) activated at every context switch.

-# In file qk.h updated revision history and added the prototype for the
extended scheduler QK_scheduleExt_(). This scheduler implements the
generic extended context via macros #QK_EXT_TYPE, #QK_EXT_SAVE, and
#QK_EXT_RESTORE.
-# In file qk.h removed definitions of the obsolete macros
#QACTIVE_POST_FIFO_, #QACTIVE_POST_LIFO_, and QACTIVE_GET_.
-# In file qk_sched.c added logic for handling the TLS via the macro
#QK_TLS.
-# Added new file qk_ext.c with the definition of the extended scheduler
QK_scheduleExt_().
-# Added the file qk_ext.c to the Makefile for QK port to 80x86 with
Turbo C++ 1.01.
-# Extended the QK port to 80x86 with Turbo C++ 1.01 to handle the
x87 FPU context.
-# Extended the QDPP example for QK with Turbo C++ 1.01 to demonstrate
threads that use the FPU and require the extended context switch.
-# In file qk.c updated version number to 3.2.04
-# Updated the "QP Programmer's Manaul" to Revision C


\section qk_3_2_01 Version 3.2.01 (Product)
Release date: Sep 01, 2006\n

-# In file qk.c updated version number to 3.2.01
-# Added makefiles for building ports of all QP/C libraries at once.
-# Created the consolidated manual "QP/C Programmer's Manual".


\section qk_3_2_00 Version 3.2.00 (Product Release)
Release date: Aug 04, 2006\n

-# in file qk.h added new idle callback QK_onIdle(), which in contrast
to QF_onIdle() is invoked with interrupts unlocked.
-# in file qk.h removed QK_schedLock()/QK_schedUnlock() and replaced them
with QK_mutexLock()/QK_mutexUnlock(), with the semantics of returning the
mutex.
-# in file qk.h changed the definitions of macros #QF_SCHED_LOCK/
#QF_SCHED_UNLOCK to use the new QK mutex API.
-# In file qk.h used the macro Q_ROM to allocate constant objects
to ROM (\sa qep.h).
-# in file qk.h added the typedef for QMutex.
-# in file qk.c replaced the callback QF_onIdle() with the new one
QK_onIdle().
-# removed source file qk_lock.c
-# added source file qk_mutex.c
-# in file qk.c changed the version number to 3.2.00
-# Updated "QK/C Programmer's Manual"


\section qk_3_1_06 Version 3.1.06 (Product Release)
Release date: May 11, 2006\n

-# In file qk_sched.c removed unlocking of interrupts upon exit from
QK_schedule_(). Now QK_schedule_() enters and exits with interrupts LOCKED.
-# In file qk.h modified macro QACTIVE_OSOBJECT_SIGNAL_() to always unlock
the interrupts, regardless if QK_schedule_() has been called or not.
-# In file qk.c added unlocking interrupts after the call to QK_SCHEDULE_()
in the function QF_run().
-# In file qk_lock.c modified the function QK_schedUnlock() to always unlock
the interrupts upon exit.


\section qk_3_1_05 Version 3.1.05 (Product Release)
Release date: Feb 08, 2006\n

-# In file qk.h removed extern QK_intLockNest_ and QK_isrNest_. These
counters have been moved to QF and renamed in the process to QF_intLockNest_
and QF_isrNest_, respectively.
-# In file qk.h added QS instrumentatin to #QF_INT_LOCK and #QF_INT_UNLOCK
macros for tracing interrupt locking/unlocking. The QS interrupt locking/
unlocking instrumentation has been previously added at the QK port level.
-# In file qk.h removed macros QK_QS_INT_LOCK()/ QK_QS_INT_UNLOCK(),
QK_QS_ISR_ENTRY()/ QK_QS_ISR_EXIT(). These macros have been moved to QF and
renamed in the process to QF_QS_INT_LOCK()/ QF_QS_INT_UNLOCK(),
QF_QS_ISR_ENTRY()/ QF_QS_ISR_EXIT(), respectively.
-# In file ports/80x86/qk/tcpp101/l/qk_port.h simplified the definition
of the macros #QK_INT_LOCK/ #QK_INT_UNLOCK to NOT contain the QS
instrumenation.
-# In file ports/80x86/qk/tcpp101/l/qk_port.h changed the definitions of
#QK_ISR_ENTRY and #QK_ISR_EXIT to use #QF_QS_ISR_ENTRY/ #QF_QS_ISR_EXIT.
-# In file qk.c added the Revision History Doxygen comment
-# In file qk_pkg.h changed the definition of internal QK macros
#QK_INT_LOCK_/ #QK_INT_UNLOCK_ to use the QS-instrumented #QF_INT_LOCK/
#QF_INT_UNLOCK.
-# In file qk_lock.c corrected a comment
-# In file qk_sched.c corrected a comment


\section qk_3_1_04 Version 3.1.04 (Product Release)
Release date: Dec 28, 2005\n

-# Provided "QK/C Programmer's Manual" in PDF.
-# In file qk.h removed callbacks QK_init(), QK_start(), QK_idle(),
QK_exit(), because they duplicate the QF callbacks.
-# Modified qk.c to define the following QF "callbacks": QF_getPortVersion(),
QF_run(), QActive_start(), and QActive_stop_().
-# Added an argument to the signature of QK_schedLock() to allow selective
QK scheduler locking up to the specified priority level.
-# Changed the implementation of QK_schedLock() in file qk_lock.c.
-# Eliminated the need for qf_port.c in the QF/C ports for QK.
-# Simplified elements that go into qk_port.c in the QK/C ports.
-# Added the ARM-Simulator port to the standard QK/C distribution.
-# Cleaned-up the 80x86 QK port.


\section qk_3_1_03 Version 3.1.03 (Beta Release)
Release date: Nov 18, 2005\n

-# Added Doxygen documentation to the source code
*/

/** \defgroup qk Quantum Kernel in C (QK/C)
* \image html logo_qk_TM.jpg
*
* Quantum Kernel (QK) is a tiny preemptive real-time kernel specifically
* designed for executing independent tasks in a run-to-completion (RTC)
* fashion. As it turns out, the RTC-style processing universally applied
* in the Quantum Framework™ (QF), and no need for blocking of active
* objects, allow QK to be extremely simple and utilize only a single stack
* for all tasks and interrupts.
*
* QK is not a standalone product but rather it requires a compatible
* version of \ref qf. QK doesn't provide event queues,
* active objects, or even lookup tables used by the scheduler, because
* these elements are already part of the QF.
*
* \sa <A HREF="http://www.quantum-leaps.com/doc/QP_Manual.pdf">
*      QP Programmer's Manual</A> \n
*      \ref qk_rev
*/

#endif                                                              /* qk_h */
