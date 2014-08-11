//////////////////////////////////////////////////////////////////////////////
// Product: QF/C++ port to 80x86, uC/OS-II, Turbo C++ 1.01, Large model
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
#ifndef qf_port_h
#define qf_port_h
                                      // uC/OS-II event queue and thread types
#define QF_EQUEUE_TYPE              OS_EVENT *
#define QF_THREAD_TYPE              INT8U

                    // The maximum number of active objects in the application
#define QF_MAX_ACTIVE               OS_MAX_TASKS

             // uC/OS-II critical section operations (critical section type 3)
#define QF_INT_KEY_TYPE             OS_CPU_SR
#define QF_INT_LOCK(key_)           ((key_) = OSCPUSaveSR())
#define QF_INT_UNLOCK(key_)         OSCPURestoreSR(key_)

#include <dos.h>                                                    // DOS API
#undef outportb  // don't use the macro because it has a bug in Turbo C++ 1.01

#include "qep_port.h"        // QEP port, includes the master uC/OS-II include
#include "qequeue.h"             // native QF event queue for deferring events
#include "qf.h"                    // QF platform-independent public interface

//////////////////////////////////////////////////////////////////////////////
// interface used only inside QF, but not in applications
//

class UCosMemPart {                     // uC/OS-II memory pool and block-size
    OS_MEM *m_pool;                                    // uC/OS-II memory pool
    QEventSize m_block_size;                     // the block size of the pool
    friend class QF;
};
                                             // uC/OS-II event pool operations

                                             // uC/OS-II event pool operations
#define QF_EPOOL_TYPE_              UCosMemPart
#define QF_EPOOL_INIT_(p_, poolSto_, poolSize_, evtSize_) do { \
    INT8U err; \
    (p_).m_block_size = (evtSize_); \
    (p_).m_pool = OSMemCreate(poolSto_, (INT32U)((poolSize_)/(evtSize_)), \
                            (INT32U)(evtSize_), &err); \
    Q_ASSERT(err == OS_NO_ERR); \
} while (0)

#define QF_EPOOL_EVENT_SIZE_(p_)    ((p_).m_block_size)
#define QF_EPOOL_GET_(p_, e_) do { \
    INT8U err; \
    ((e_) = (QEvent *)OSMemGet((p_).m_pool, &err)); \
} while (0)

#define QF_EPOOL_PUT_(p_, e_)       OSMemPut((p_).m_pool, (void *)(e_))

#endif                                                            // qf_port_h
