//////////////////////////////////////////////////////////////////////////////
// Product:  QF/C++, Cortex-M3, QK port, IAR compiler
// Last Updated for Version: 4.0.01
// Date of the Last Update:  Apr 24, 2008
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
                    // The maximum number of active objects in the application
#define QF_MAX_ACTIVE               63
                                             // QF critical section entry/exit
// QF_INT_KEY_TYPE not defined
#define QF_INT_LOCK(dummy)      __disable_interrupt()
#define QF_INT_UNLOCK(dummy)    __enable_interrupt()

#include <intrinsics.h>                             // IAR intrinsic functions

#include "qep_port.h"                                              // QEP port
#include "qk_port.h"                   // QK port provides integration with QF
#include "qf.h"                    // QF platform-independent public interface

#endif                                                            // qf_port_h
