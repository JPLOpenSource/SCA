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

Q_DEFINE_THIS_MODULE(qa_defer)

/// \file
/// \ingroup qf
/// \brief QActive::defer() and QActive::recall() implementation.
///

//............................................................................
void QActive::defer(QEQueue *eq, QEvent const *e) {
    eq->postFIFO(e);
}
//............................................................................
QEvent const *QActive::recall(QEQueue *eq) {
    QEvent const *e = eq->get();    // try to get an event from deferred queue
    if (e != (QEvent *)0) {                                // event available?

        postLIFO(e);      // post it to the front of the Active Object's queue

        QF_INT_LOCK_KEY_
        QF_INT_LOCK_();

        if (e->dynamic_ != (uint8_t)0) {             // is it a dynamic event?

            // after posting to the AO's queue the event must be referenced
            // at least twice: once in the deferred event queue (eq->get()
            // did NOT decrement the reference counter) and once in the
            // AO's event queue.
            Q_ASSERT((e->dynamic_ & 0x3F) > 1);

            // we need to decrement the reference counter once, to account
            // for removing the event from the deferred event queue.
            //
            //lint -e1773                           Attempt to cast away const
            --((QEvent *)e)->dynamic_;      // decrement the reference counter
                   // NOTE: cast the 'const' away, which is legitimate because
                   // it's a dynamic event
        }

        QF_INT_UNLOCK_();

    }
    return e;  // pass the recalled event to the caller (NULL if not recalled)
}
