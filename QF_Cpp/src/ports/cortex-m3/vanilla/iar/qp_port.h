//////////////////////////////////////////////////////////////////////////////
// Product: QP/C++ port
// Last Updated for Version: 4.0.09
// Date of the Last Update:  Mar 16, 2009
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
#ifndef qp_port_h
#define qp_port_h

#include "qf_port.h"             // includes qep_port.h and qk_port.h, if used
#include "qassert.h"                                // customizable assertions

#if (Q_SIGNAL_SIZE == 1)
    #define DEV_DRIVER_SIG  (0xFF - 8)
#elif (Q_SIGNAL_SIZE == 2)
    #define DEV_DRIVER_SIG  (0xFFFF - 8)
#elif (Q_SIGNAL_SIZE == 4)
    #define DEV_DRIVER_SIG  (0xFFFFFFFF - 8)
#else
    #error "Q_SIGNAL_SIZE not defined or incorrect"
#endif

#endif                                                            // qp_port_h
