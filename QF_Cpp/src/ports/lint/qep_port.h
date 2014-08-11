//////////////////////////////////////////////////////////////////////////////
// Product: QEP/C++  port to lint, generic C++ compiler
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

/// \file
/// \ingroup qf qk
/// \brief QEP/C port to QK for a "generic" C compiler.
///
/// \note This is just an example of a QEP port used for "linting" the QEP.
/// Ports of QEP are located in the directory &lt;qpc_3&gt;/ports.

/// \brief The size (in bytes) of the signal of an event. Valid values:
/// 1, 2, or 4; default 1
///
/// This macro can be defined in the QEP ports to configure the ::QSignal
/// type. If the macro is not defined, the default of 1 byte will be chosen
/// in qep.h. The valid #QEP_SIGNAL_SIZE values of 1, 2, or 4, correspond to
/// ::QSignal of uint8_t, uint16_t, and uint32_t, respectively. The ::QSingal
/// data type determines the dynamic range of numberical values of signals
/// you your application.
/// \sa ::QEvent
///
/// \note Once you choose a certain value of #QEP_SIGNAL_SIZE, you must
/// consistently use the same value in building all the QP component libraries
/// and your own application code. The consistency is guaranteed if you define
/// this macro only once in the qep_port.h header file and henceforth include
/// this header file in all builds.
#define QEP_SIGNAL_SIZE 1

// Exact-width types. WG14/N843 C99 Standard, Section 7.18.1.1
typedef signed   char  int8_t;          ///< C99 exact-width  8-bit signed int
typedef signed   short int16_t;         ///< C99 exact-width 16-bit signed int
typedef signed   long  int32_t;         ///< C99 exact-width 32-bit signed int
typedef unsigned char  uint8_t;       ///< C99 exact-width  8-bit unsigned int
typedef unsigned short uint16_t;      ///< C99 exact-width 16-bit unsigned int
typedef unsigned long  uint32_t;      ///< C99 exact-width 32-bit unsigned int

#include "qep.h"                  // QEP platform-independent public interface

#endif                                                           // qep_port_h
