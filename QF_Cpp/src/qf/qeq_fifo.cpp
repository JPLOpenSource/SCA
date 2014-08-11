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

Q_DEFINE_THIS_MODULE(qeq_fifo)

/// \file
/// \ingroup qf
/// \brief QEQueue::postFIFO() implementation.

//............................................................................
void QEQueue::postFIFO(QEvent const *e) {
    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();

    QS_BEGIN_NOLOCK_(QS_QF_EQUEUE_POST_FIFO, QS::eqObj_, this)
        QS_TIME_();                                               // timestamp
        QS_SIG_(e->sig);                           // the signal of this event
        QS_OBJ_(this);                                    // this queue object
        QS_U8_(e->dynamic_);            // the dynamic attributes of the event
        QS_EQC_(m_nFree);                            // number of free entries
        QS_EQC_(m_nMin);                         // min number of free entries
    QS_END_NOLOCK_()

    if (e->dynamic_ != (uint8_t)0) {                    // is it a pool event?
        //lint -e1773                               Attempt to cast away const
        ++((QEvent *)e)->dynamic_;          // increment the reference counter
                   // NOTE: cast the 'const' away, which is legitimate because
                   // it's a dynamic event
    }

    if (m_frontEvt == (QEvent *)0) {                    // is the queue empty?
        m_frontEvt = e;                              // deliver event directly
    }
    else {               // queue is not empty, leave event in the ring-buffer
               // the queue must be able to accept the event (cannot overflow)
        Q_ASSERT(m_nFree != (QEQueueCtr)0);

        m_ring[m_head] = e;             // insert event into the buffer (FIFO)
        if (m_head == (QEQueueCtr)0) {               // need to wrap the head?
            m_head = m_end;                                     // wrap around
        }
        --m_head;

        --m_nFree;                             // update number of free events
        if (m_nMin > m_nFree) {
            m_nMin = m_nFree;                         // update minimum so far
        }
    }
    QF_INT_UNLOCK_();
}
