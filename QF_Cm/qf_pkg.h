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
#ifndef qf_pkg_h
#define qf_pkg_h

/** \ingroup qf
* \file qf_pkg.h
* \brief Internal (package scope) QF/C interface.
*/

#include "qf_port.h"                                             /* QF port */

#ifdef Q_SPY
    #include "qs_port.h"                                         /* QS port */
#else
    #define QS_BEGIN_(rec_, refObj_, obj_)  if (0) { ((void)0)
    #define QS_END_()                       } else ((void)0)
    #define QS_BEGIN_NOLOCK_(rec_, refObj_, obj_) if (0) { ((void)0)
    #define QS_END_NOLOCK_()                QS_END_()
    #define QS_EXIT()                       ((void)0)

    #define QS_U8_(data_)                   ((void)0)
    #define QS_U16_(data_)                  ((void)0)
    #define QS_U32_(data_)                  ((void)0)
    #define QS_TIME_()                      ((void)0)
    #define QS_SIG_(sig_)                   ((void)0)
    #define QS_EVS_(size_)                  ((void)0)
    #define QS_OBJ_(obj_)                   ((void)0)
    #define QS_FUN_(fun_)                   ((void)0)
    #define QS_EQC_(ctr_)                   ((void)0)
    #define QS_MPC_(ctr_)                   ((void)0)
    #define QS_MPS_(size_)                  ((void)0)
    #define QS_TEC_(ctr_)                   ((void)0)
#endif

                                 /* QF-specific interrupt locking/unlocking */
#ifndef QF_INT_KEY_TYPE
    /** \brief This is an internal macro for defining the interrupt lock key.
    *
    * The purpose of this macro is to enable writing the same code for the
    * case when interrupt key is defined and when it is not. If the macro
    * #QF_INT_KEY_TYPE is defined, this internal macro provides the
    * definition of the lock key variable. Otherwise this macro is empty.
    * \sa #QF_INT_KEY_TYPE, #QK_INT_KEY_TYPE
    */
    #define QF_INT_LOCK_KEY_

    /** \brief This is an internal macro for locking interrupts.
    *
    * The purpose of this macro is to enable writing the same code for the
    * case when interrupt key is defined and when it is not. If the macro
    * #QF_INT_KEY_TYPE is defined, this internal macro invokes #QF_INT_LOCK
    * passing the key variable as the parameter. Otherwise #QF_INT_LOCK
    * is invoked with a dummy parameter.
    * \sa #QF_INT_LOCK, #QK_INT_LOCK
    */
    #define QF_INT_LOCK_()      QF_INT_LOCK(ignore_)

    /** \brief This is an internal macro for unlocking interrupts.
    *
    * The purpose of this macro is to enable writing the same code for the
    * case when interrupt key is defined and when it is not. If the macro
    * #QF_INT_KEY_TYPE is defined, this internal macro invokes #QF_INT_UNLOCK
    * passing the key variable as the parameter. Otherwise #QF_INT_UNLOCK
    * is invoked with a dummy parameter.
    * \sa #QF_INT_UNLOCK, #QK_INT_UNLOCK
    */
    #define QF_INT_UNLOCK_()    QF_INT_UNLOCK(ignore_)
#else
    #define QF_INT_LOCK_KEY_    QF_INT_KEY_TYPE intLockKey__;
    #define QF_INT_LOCK_()      QF_INT_LOCK(intLockKey__)
    #define QF_INT_UNLOCK_()    QF_INT_UNLOCK(intLockKey__)
#endif

/* package-scope objects ---------------------------------------------------*/
extern QTimeEvt *QF_timeEvtListHead_; /**< head of linked list of time evts */
extern QF_EPOOL_TYPE_ QF_pool_[3];              /**< allocate 3 event pools */
extern uint8_t QF_maxPool_;               /**< # of initialized event pools */
extern QSubscrList *QF_subscrList_;          /**< the subscriber list array */
extern QSignal QF_maxSignal_;             /**< the maximum published signal */

/*..........................................................................*/
/** \brief Name for the ::QFreeBlockTag structure
* \sa ::QFreeBlock
*/
typedef struct QFreeBlockTag QFreeBlock;
/** \brief Structure representing a free block in the Native QF Memory Pool
* \sa ::QFreeBlock, ::QMPool
*/
struct QFreeBlockTag {
    QFreeBlock *next;
};

#endif                                                          /* qf_pkg_h */

