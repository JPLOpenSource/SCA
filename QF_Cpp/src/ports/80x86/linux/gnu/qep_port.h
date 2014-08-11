//////////////////////////////////////////////////////////////////////////////
// Product: QEP/C++  port to 80x86, GNU compiler
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
#ifndef qep_port_h
#define qep_port_h
                                          // 2-byte signal space (64K signals)
#define Q_SIGNAL_SIZE  2

#include <stdint.h>  // exact-width integers, WG14/N843 C99 Standard, 7.18.1.1

#include "qep.h"                  // QEP platform-independent public interface

#endif                                                           // qep_port_h
