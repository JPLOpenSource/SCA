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

Q_DEFINE_THIS_MODULE(qf_pspub)

/// \file
/// \ingroup qf
/// \brief QF::publish() implementation.

//............................................................................
void QF::publish(QEvent const *e) {
         // make sure that the published signal is within the configured range
    Q_REQUIRE(e->sig < QF_maxSignal_);

    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();

    QS_BEGIN_NOLOCK_(QS_QF_PUBLISH, (void *)0, (void *)0)
        QS_TIME_();                                           // the timestamp
        QS_SIG_(e->sig);                            // the signal of the event
        QS_U8_(e->dynamic_);            // the dynamic attributes of the event
    QS_END_NOLOCK_()

    if (e->dynamic_ != (uint8_t)0) {                 // is it a dynamic event?
        //lint -e1773                               Attempt to cast away const
        ++((QEvent *)e)->dynamic_;      // increment reference counter, NOTE01
                   // NOTE: cast the 'const' away, which is legitimate because
                   // it's a dynamic event */
    }
    QF_INT_UNLOCK_();

#if (QF_MAX_ACTIVE <= 8)
    uint8_t tmp = QF_subscrList_[e->sig].m_bits[0];
    while (tmp != (uint8_t)0) {
        uint8_t p = Q_ROM_BYTE(QF_log2Lkup[tmp]);
        tmp &= Q_ROM_BYTE(QF_invPwr2Lkup[p]);      // clear the subscriber bit
        Q_ASSERT(active_[p] != (QActive *)0);            // must be registered

        active_[p]->postFIFO(e);  // internally asserts if the queue overflows
    }
#else
    uint8_t i = Q_DIM(QF_subscrList_[0].m_bits);// number of bytes in the list
    do {                       // go through all bytes in the subsciption list
        --i;
        uint8_t tmp = QF_subscrList_[e->sig].m_bits[i];
        while (tmp != (uint8_t)0) {
            uint8_t p = Q_ROM_BYTE(QF_log2Lkup[tmp]);
            tmp &= Q_ROM_BYTE(QF_invPwr2Lkup[p]);  // clear the subscriber bit
            p = (uint8_t)(p + (i << 3));                // adjust the priority
            Q_ASSERT(active_[p] != (QActive *)0);        // must be registered

                       // postFIFO() internally asserts if the queue overflows
            active_[p]->postFIFO(e);
        }
    } while (i != (uint8_t)0);
#endif

    gc(e);                            // run the garbage collector, see NOTE01
}

//////////////////////////////////////////////////////////////////////////////
// NOTE01:
// QF::publish() increments the reference counter to prevent premature
// recycling of the event while the multicasting is still in progress.
// At the end of the function, the garbage collector step decrements the
// reference counter and recycles the event if the counter drops to zero.
// This covers the case when the event was published without any subscribers.
//
