/*****************************************************************************
* Product: QEP/C port, 80x86, Turbo C++ 1.01
* Last Updated for Version: 3.3.00
* Date of the Last Update:  Jan 22, 2007
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
*
* Copyright (C) 2002-2007 Quantum Leaps, LLC. All rights reserved.
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
* e-mail:                  sales@quantum-leaps.com
*****************************************************************************/
#ifndef qep_port_h
#define qep_port_h

// GW included stdlib
#ifdef VXWORKS
  #include <vxWorks.h>
#else
  #include <stdint.h>
#endif

// GW Define Q_ROM_VAR and Q_ROM as nothing
#define Q_ROM_VAR
#define Q_ROM
                                       /* 1-byte signal space (255 signals) */
/*#define QEP_SIGNAL_SIZE 1*/
                                       /* 2-byte signal space (65,535 signals) for Ares I */
#define QEP_SIGNAL_SIZE 2

// GW Fix the troubles with integer types.
//    The stdint.h already defines these types


#if 0
             /* Exact-width types. WG14/N843 C99 Standard, Section 7.18.1.1 */
typedef signed   char  int8_t;
typedef signed   int   int16_t;
typedef signed   long  int32_t;
typedef unsigned char  uint8_t;
typedef unsigned int   uint16_t;
typedef unsigned long  uint32_t;
#endif

#include "qep.h"

#endif                                                        /* qep_port_h */
