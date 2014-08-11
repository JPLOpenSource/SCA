/*****************************************************************************
* Product:  QF/C, port to 80x86, Linux/P-threads, gcc
* Last Updated for Version: 4.2.00
* Date of the Last Update:  Jul 23, 2011
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
*
* Copyright (C) 2002-2011 Quantum Leaps, LLC. All rights reserved.
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
#ifndef qf_port_h
#define qf_port_h

                                      /* Linux event queue and thread types */
#define QF_EQUEUE_TYPE              QEQueue
#define QF_OS_OBJECT_TYPE           pthread_cond_t
#define QF_THREAD_TYPE              pthread_t

                 /* The maximum number of active objects in the application */
#define QF_MAX_ACTIVE               63
                    /* The maximum number of event pools in the application */
#define QF_MAX_EPOOL                8
                     /* various QF object sizes configuration for this port */
#define QF_EVENT_SIZ_SIZE           4
#define QF_EQUEUE_CTR_SIZE          4
#define QF_MPOOL_SIZ_SIZE           4
#define QF_MPOOL_CTR_SIZE           4
#define QF_TIMEEVT_CTR_SIZE         4

                    /* QF critical section entry/exit for Linux, see NOTE01 */
/* QF_INT_KEY_TYPE not defined */
#define QF_INT_LOCK(dummy)          pthread_mutex_lock(&QF_pThreadMutex_)
#define QF_INT_UNLOCK(dummy)        pthread_mutex_unlock(&QF_pThreadMutex_)

#include <pthread.h>                                    /* POSIX-thread API */
#include "qep_port.h"                                           /* QEP port */
#include "qequeue.h"                             /* Linux needs event-queue */
#include "qmpool.h"                              /* Linux needs memory-pool */
#include "qf.h"                 /* QF platform-independent public interface */


/*****************************************************************************
* interface used only inside QF, but not in applications
*/
                                      /* OS-object implementation for Linux */
#define QACTIVE_EQUEUE_WAIT_(me_) \
    while ((me_)->eQueue.frontEvt == (QEvent *)0) \
        pthread_cond_wait(&(me_)->osObject, &QF_pThreadMutex_)

#define QACTIVE_EQUEUE_SIGNAL_(me_) \
    pthread_cond_signal(&(me_)->osObject)

#define QACTIVE_EQUEUE_ONEMPTY_(me_) ((void)0)

                                         /* native QF event pool operations */
#define QF_EPOOL_TYPE_              QMPool
#define QF_EPOOL_INIT_(p_, poolSto_, poolSize_, evtSize_) \
    QMPool_init(&(p_), poolSto_, poolSize_, evtSize_)
#define QF_EPOOL_EVENT_SIZE_(p_)    ((p_).blockSize)
#define QF_EPOOL_GET_(p_, e_)       ((e_) = (QEvent *)QMPool_get(&(p_)))
#define QF_EPOOL_PUT_(p_, e_)       (QMPool_put(&(p_), e_))


extern pthread_mutex_t QF_pThreadMutex_;   /* mutex for QF critical section */

/*****************************************************************************
*
* NOTE01:
* QF, like all real-time frameworks, needs to execute certain sections of
* code indivisibly to avoid data corruption. The most straightforward way of
* protecting such critical sections of code is disabling and enabling
* interrupts, which Linux does not allow.
*
* This QF port uses therefore a single package-scope p-thread mutex
* QF_pThreadMutex_ to protect all critical sections. The mutex is locked upon
* the entry to each critical sectioni and unlocked upon exit.
*
* Using the single mutex for all crtical section guarantees that only one
* thread at a time can execute inside a critical section. This prevents race
* conditions and data corruption.
*
* Please note, however, that the mutex implementation of a critical section
* behaves differently than the standard interrupt locking. A common mutex
* ensures that only one thread at a time can execute a critical section, but
* it does not guarantee that a context switch cannot occur within the
* critical section. In fact, such context switches probably will happen, but
* they should not cause concurrency hazards because the mutex eliminates all
* race conditionis.
*
* Unlinke simply disabling and enabling interrupts, the mutex approach is
* also subject to priority inversions. However, the p-thread mutex
* implementation, such as Linux p-threads, should support the priority-
* inheritance protocol.
*/

#endif                                                         /* qf_port_h */
