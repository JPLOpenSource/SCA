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

Q_DEFINE_THIS_MODULE(qa_get_)

/// \file
/// \ingroup qf
/// \brief QActive::get_() and QF::getQueueMargin() definitions.
///
/// \note this source file is only included in the QF library when the native
/// QF active object queue is used (instead of a message queue of an RTOS).

//............................................................................
QEvent const *QActive::get_(void) {
    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();

    QACTIVE_EQUEUE_WAIT_(this);           // wait for event to arrive directly

    QEvent const *e = m_eQueue.m_frontEvt;

    if (m_eQueue.m_nFree != m_eQueue.m_end) { //any events in the ring buffer?
                                                 // remove event from the tail
        m_eQueue.m_frontEvt = m_eQueue.m_ring[m_eQueue.m_tail];
        if (m_eQueue.m_tail == (QEQueueCtr)0) {      // need to wrap the tail?
            m_eQueue.m_tail = m_eQueue.m_end;                   // wrap around
        }
        --m_eQueue.m_tail;

        ++m_eQueue.m_nFree;          // one more free event in the ring buffer

        QS_BEGIN_NOLOCK_(QS_QF_ACTIVE_GET, QS::aoObj_, this)
            QS_TIME_();                                           // timestamp
            QS_SIG_(e->sig);                       // the signal of this event
            QS_OBJ_(this);                               // this active object
            QS_U8_(e->dynamic_);        // the dynamic attributes of the event
            QS_EQC_(m_eQueue.m_nFree);               // number of free entries
        QS_END_NOLOCK_()
    }
    else {
        m_eQueue.m_frontEvt = (QEvent *)0;          // the queue becomes empty
        QACTIVE_EQUEUE_ONEMPTY_(this);

        QS_BEGIN_NOLOCK_(QS_QF_ACTIVE_GET_LAST, QS::aoObj_, this)
            QS_TIME_();                                           // timestamp
            QS_SIG_(e->sig);                       // the signal of this event
            QS_OBJ_(this);                               // this active object
            QS_U8_(e->dynamic_);        // the dynamic attributes of the event
        QS_END_NOLOCK_()
    }
    QF_INT_UNLOCK_();
    return e;
}
//............................................................................
uint32_t QF::getQueueMargin(uint8_t prio) {
    Q_REQUIRE((prio <= (uint8_t)QF_MAX_ACTIVE)
              && (active_[prio] != (QActive *)0));

    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();
    uint32_t margin = (uint32_t)(active_[prio]->m_eQueue.m_nMin);
    QF_INT_UNLOCK_();

    return margin;
}
