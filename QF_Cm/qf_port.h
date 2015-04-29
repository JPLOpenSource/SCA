/*****************************************************************************
* Product:  QF/C, vanilla port to x86, DOS, Turbo C++ 1.01
* Last Updated for Version: 3.2.00
* Date of the Last Update:  Aug 07, 2006
*
* Copyright (C) 2002-2006 Quantum Leaps, LLC. All rights reserved.
*
* This software may be distributed and modified under the terms of the GNU
* General Public License version 2 (GPL) as published by the Free Software
* Foundation and appearing in the file GPL.TXT included in the packaging of
* this file. Please note that GPL Section 2[b] requires that all works based
* on this software must also be made publicly available under the terms of
* the GPL ("Copyleft").
*
* Alternatively, this software may be distributed and modified under the
* terms of Quantum Leaps commercial licenses, which expressly allow the
* licensees to retain the proprietary status of their code. The licensees
* who use this software under one of Quantum Leaps commercial licenses do
* not use this software under the GPL and therefore are not subject to any
* of its terms.
*
* Contact information:
* Quantum Leaps Web site:  http://www.quantum-leaps.com
* Quantum Leaps licensing: http://www.quantum-leaps.com/licensing
* Quantum Leaps products:  http://www.quantum-leaps.com/products
* e-mail:                  sales@quantum-leaps.com
*****************************************************************************/
#ifndef qf_port_h
#define qf_port_h

#include <stdlib.h>

                     /* various QF object sizes configuration for this port */
#define QF_MAX_ACTIVE               63
#define QF_EVENT_SIZ_SIZE           2
#define QF_EQUEUE_CTR_SIZE          1
#define QF_MPOOL_SIZ_SIZE           2
#define QF_MPOOL_CTR_SIZE           2
#define QF_TIMEEVT_CTR_SIZE         2

                                         /* DOS critical section entry/exit */
/* QF_INT_KEY_TYPE not defined */
// GW keep this undefined
#define QF_INT_LOCK(key_)          
#define QF_INT_UNLOCK(key_)        

// GW Remove the dos.h
//#include <dos.h>                                                 /* DOS API */
#undef outportb /*don't use the macro because it has a bug in Turbo C++ 1.01*/

#include "qep_port.h"                                           /* QEP port */
#include "qequeue.h"                 /* Vanilla QF/C port needs event-queue */
#include "qmpool.h"                  /* Vanilla QF/C port needs memory-pool */
#include "qsched.h"  /* Vanilla QF/C port uses the non-preemptive scheduler */
#include "qf.h"                 /* QF platform-independent public interface */
#include "qpset.h"                  /* Vanilla QF/C port needs priority-set */

#endif                                                         /* qf_port_h */
