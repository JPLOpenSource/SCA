//////////////////////////////////////////////////////////////////////////////
// Product: QF/C++ port to Lint, Generic C++ compiler
// Last Updated for Version: 4.0.03
// Date of the Last Update:  Dec 27, 2008
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
#ifndef qf_port_h
#define qf_port_h

/// \file
/// \ingroup qf qk
/// \brief QF port to QK for a "generic" C++ compiler.
///
/// \note This is just an example of a QF port used for "linting" the QF.
/// Ports of QF are located in the directory &lt;qpcpp_3&gt;/ports.

/// \brief The maximum number of active objects in the application.
///
/// This macro should be defined in the QF ports and should be in range
/// of 1..63, inclusive. The value of this macro determines the maximum
/// priority level of an active object in the system. Not all priority
/// levels must be used, but the maximum priority cannot exceed
/// #QF_MAX_ACTIVE.
///
/// If the macro is not defined, the default value is 63, which is the
/// maximum. Defining the value below the maximum saves some memory,
/// mostly for the subscriber-lists.
/// \sa ::QSubscrList.
///
/// \note Once you choose a certain value of #QF_MAX_ACTIVE, you must
/// consistently use the same value in building all the QP component libraries
/// and your own application code. The consistency is guaranteed if you define
/// this macro only once in the qf_port.h header file and henceforth include
/// this header file in all builds.
#define QF_MAX_ACTIVE               63

/// \brief The size (in bytes) of the event-size representation in the QF.
/// Valid values: 1, 2, or 4; default 2
///
/// This macro can be defined in the QF ports to configure the ::QEventSize
/// type. If the macro is not defined, the default of 2 byte will be chosen in
/// qf.h. The valid #QF_EVENT_SIZ_SIZE values of 1, 2, or 4, correspond
/// to ::QEventSize of uint8_t, uint16_t, and uint32_t, respectively. The
/// ::QEventSize data type determines the dynamic range of event-sizes in
/// your application.
/// \sa QF::poolInit(), QF::new_()
///
/// \note Once you choose a certain value of #QF_EVENT_SIZ_SIZE, you must
/// consistently use the same value in building all the QP component libraries
/// and your own application code. The consistency is guaranteed if you define
/// this macro only once in the qf_port.h header file and henceforth include
/// this header file in all builds.
#define QF_EVENT_SIZ_SIZE           2

/// \brief The size (in bytes) of the ring-buffer counters used in the
/// native QF event queue implementation. Valid values: 1, 2, or 4; default 1
///
/// This macro can be defined in the QF ports to configure the ::QEQueueCtr
/// type. If the macro is not defined, the default of 1 byte will be chosen in
/// qequeue.h. The valid #QF_EQUEUE_CTR_SIZE values of 1, 2, or 4, correspond
/// to ::QEQueueCtr of uint8_t, uint16_t, and uint32_t, respectively. The
/// ::QEQueueCtr data type determines the dynamic range of numerical values of
/// ring-buffer counters inside event queues, or, in other words, the maximum
/// number of events that the native QF event queue can manage.
/// \sa ::QEQueue
///
/// \note Once you choose a certain value of #QF_EQUEUE_CTR_SIZE, you must
/// consistently use the same value in building all the QP component libraries
/// and your own application code. The consistency is guaranteed if you define
/// this macro only once in the qf_port.h header file and henceforth include
/// this header file in all builds.
#define QF_EQUEUE_CTR_SIZE          1

/// \brief The size (in bytes) of the block-size representation in the
/// native QF event pool. Valid values: 1, 2, or 4; default
/// #QF_EVENT_SIZ_SIZE.
///
/// This macro can be defined in the QF ports to configure the ::QMPoolSize
/// type. If the macro is not defined, the default of #QF_EVENT_SIZ_SIZE
/// will be chosen in qmpool.h, because the memory pool is primarily used for
/// implementing event pools.
///
/// The valid #QF_MPOOL_SIZ_SIZE values of 1, 2, or 4, correspond to
/// ::QMPoolSize of uint8_t, uint16_t, and uint32_t, respectively. The
/// ::QMPoolSize data type determines the dynamic range of block-sizes that
/// the native ::QMPool can hanle.
/// \sa #QF_EVENT_SIZ_SIZE, ::QMPool
///
/// \note Once you choose a certain value of #QF_MPOOL_SIZ_SIZE, you must
/// consistently use the same value in building all the QP component libraries
/// and your own application code. The consistency is guaranteed if you define
/// this macro only once in the qf_port.h header file and henceforth include
/// this header file in all builds.
#define QF_MPOOL_SIZ_SIZE           2

/// \brief The size (in bytes) of the block-counter representation in the
/// native QF event pool. Valid values: 1, 2, or 4; default 2.
///
/// This macro can be defined in the QF ports to configure the ::QMPoolCtr
/// type. If the macro is not defined, the default of 2 bytes will be chosen
/// in qmpool.h. The valid #QF_MPOOL_CTR_SIZE values of 1, 2, or 4, correspond
/// to ::QMPoolSize of uint8_t, uint16_t, and uint32_t, respectively. The
/// ::QMPoolCtr data type determines the dynamic range of block-counters that
/// the native ::QMPool can handle, or, in other words, the maximum number
/// of blocks that the native QF event pool can manage.
/// \sa ::QMPool
///
/// \note Once you choose a certain value of #QF_MPOOL_CTR_SIZE, you must
/// consistently use the same value in building all the QP component libraries
/// and your own application code. The consistency is guaranteed if you define
/// this macro only once in the qf_port.h header file and henceforth include
/// this header file in all builds.
#define QF_MPOOL_CTR_SIZE           2

/// \brief The size (in bytes) of the time event -counter representation
/// in the ::QTimeEvt class. Valid values: 1, 2, or 4; default 2.
///
/// This macro can be defined in the QF ports to configure the internal tick
/// counters of Time Events. If the macro is not defined, the default of 2
/// bytes will be chosen in qf.h. The valid #QF_TIMEEVT_CTR_SIZE values of 1,
/// 2, or 4, correspond to tick counters of uint8_t, uint16_t, and uint32_t,
/// respectively. The tick counter representation determines the dynamic range
/// of time delays that a Time Event can handle.
/// \sa ::QTimeEvt
///
/// \note Once you choose a certain value of #QF_TIMEEVT_CTR_SIZE, you must
/// consistently use the same value in building all the QP component libraries
/// and your own application code. The consistency is guaranteed if you define
/// this macro only once in the qf_port.h header file and henceforth include
/// this header file in all builds.
#define QF_TIMEEVT_CTR_SIZE         4

//////////////////////////////////////////////////////////////////////////
/// \brief The macro defining the base class for QActive.
///
/// By default, the ::QActive class is derived from ::QHsm. However,
/// if the macro QF_ACTIVE_BASE is defined, QActive is derived from
/// QF_ACTIVE_BASE.
///
/// Clients might choose, for example, to define QF_ACTIVE_BASE as QFsm
/// to avoide the 1-2KB overhead of the hierarchical event processor.
///
/// Clients might also choose to define QF_ACTIVE_BASE as their own
/// completely customized class that has nothing to do with QHsm or QFsm.
/// The QF_ACTIVE_BASE class must provide member functions init() and
/// dispatch(), consistent with the signatures of QHsm and QFsm. But
/// the implementatin of these functions is completely open.
///
/// \note Once you define #QF_ACTIVE_BASE, you must consistently it in
/// building all the QP component libraries and your own application code.
/// The consistency is guaranteed if you define this macro only once in the
/// qf_port.h header file and henceforth include this header file in all
/// builds.
#define QF_ACTIVE_BASE              QHsm

/// \brief Define the type of the interrupt lock key.
///
/// Defining this macro configures the "saving and restoring interrupt status"
/// policy of locking and unlocking interrupts. Coversely, if this macro is not
/// defined, the simple "unconditional interrupt locking and unlocking" is used.
/*lint -e970 */
#define QF_INT_KEY_TYPE             int

/// \brief Define the interrupt locking policy.
///
/// This macro establishes a critical section (typically by locking interrupts).
/// When the "saving and restoring interrupt status" policy is used, the macro
/// sets the "interrupt key" to the interrupt status just before locking
/// interrupts. When the policy of "unconditional interrupt unlocking" is used,
/// the macro does not use the "interrupt key" parameter.
///
/// \note the #QF_INT_LOCK macro should always be used in pair with the
/// macro #QF_INT_UNLOCK.
#define QF_INT_LOCK(key_)           ((key_) = intLock())

/// \brief Define the interrupt unlocking policy.
///
/// This macro leaves a critical section (typically by unlocking interrupts).
/// When the "saving and restoring interrupt status" policy is used, the macro
/// restores the interrupt status from the "interrupt key" parameter.
/// When the policy of "unconditional interrupt unlocking" is used, the macro
/// unconditionally unlocks interrupts ignoring the "interrupt key" parameter.
///
/// \note the #QF_INT_LOCK macro should always be used in pair with the
/// macro #QF_INT_UNLOCK.
#define QF_INT_UNLOCK(key_) intUnlock(key_)

QF_INT_KEY_TYPE intLock(void);
void intUnlock(QF_INT_KEY_TYPE intLockKey);


#include "qep_port.h"                                              // QEP port
#include "qk_port.h"                                                // QK port
#include "qf.h"                    // QF platform-independent public interface

#endif                                                            // qf_port_h
