/*****************************************************************************
* Product:  QF/C
* Last Updated for Version: 4.0.00
* Date of the Last Update:  Apr 05, 2008
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
#ifndef qvanilla_h
#define qvanilla_h

/**
* \file
* \ingroup qf
* \brief platform-independent interface to the cooperative "vanilla" kernel.
*/

#include "qequeue.h"    /* "Vanilla" kernel uses the native QF event queue  */
#include "qmpool.h"     /* "Vanilla" kernel uses the native QF memory pool  */
#include "qpset.h"      /* "Vanilla" kernel uses the native QF priority set */

                           /* the event queue type for the "Vanilla" kernel */
#define QF_EQUEUE_TYPE              QEQueue

                                        /* native QF event queue operations */
#define QACTIVE_EQUEUE_WAIT_(me_) \
    Q_ASSERT((me_)->eQueue.frontEvt != (QEvent *)0)

#if (QF_MAX_ACTIVE <= 8)
    #define QACTIVE_EQUEUE_SIGNAL_(me_) \
        QPSet8_insert(&QF_readySet_, (me_)->prio)

    #define QACTIVE_EQUEUE_ONEMPTY_(me_) \
        QPSet8_remove(&QF_readySet_, (me_)->prio)
#else
    #define QACTIVE_EQUEUE_SIGNAL_(me_) \
        QPSet64_insert(&QF_readySet_, (me_)->prio)

    #define QACTIVE_EQUEUE_ONEMPTY_(me_) \
        QPSet64_remove(&QF_readySet_, (me_)->prio)
#endif
                                         /* native QF event pool operations */
#define QF_EPOOL_TYPE_              QMPool
#define QF_EPOOL_INIT_(p_, poolSto_, poolSize_, evtSize_) \
    QMPool_init(&(p_), poolSto_, poolSize_, evtSize_)
#define QF_EPOOL_EVENT_SIZE_(p_)    ((p_).blockSize)
#define QF_EPOOL_GET_(p_, e_)       ((e_) = (QEvent *)QMPool_get(&(p_)))
#define QF_EPOOL_PUT_(p_, e_)       (QMPool_put(&(p_), e_))

#if (QF_MAX_ACTIVE <= 8)
    extern QPSet8  volatile QF_readySet_;/** QF-ready set of active objects */
#else
    extern QPSet64 volatile QF_readySet_;/** QF-ready set of active objects */
#endif

#endif                                                        /* qvanilla_h */
