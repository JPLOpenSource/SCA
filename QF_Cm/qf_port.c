/*****************************************************************************
* Product:  QF/C, port to 80x86, uC/OS-II, Turbo C++ 1.01, Large model
* Last Updated for Version: 3.2.05
* Date of the Last Update:  Dec 08, 2006
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
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
* terms of Quantum Leaps commercial licenses, which expressly supersede
* the GPL and are specifically designed for licensees interested in
* retaining the proprietary status of their code.
*
* Contact information:
* Quantum Leaps Web site:  http://www.quantum-leaps.com
* e-mail:                  sales@quantum-leaps.com
*****************************************************************************/
#include "qf_pkg.h"
#include "qassert.h"
#include <string.h>

#define bzero(b,len) (memset((b), '\0', (len)), (void) 0)

Q_DEFINE_THIS_MODULE(qf_port)

/* local objects -----------------------------------------------------------*/


/*..........................................................................*/
void QF_start(void) {

}

void QF_init(int maxSignals, int bigEventSize, int numberEvents) {
  QSubscrList* subscrSto = (QSubscrList*)malloc(sizeof(QSubscrList)*maxSignals);  
  bzero(subscrSto, sizeof(QSubscrList)*maxSignals); /* set the entire stucture to zero   */

  QF_psInit(subscrSto, maxSignals);
  
  QEvent * regPoolSto = (QEvent*)malloc(bigEventSize*numberEvents);  
  bzero(regPoolSto, bigEventSize*numberEvents);    /* set the entire stucture to zero   */

  QF_poolInit(regPoolSto, bigEventSize*numberEvents, bigEventSize);

}


