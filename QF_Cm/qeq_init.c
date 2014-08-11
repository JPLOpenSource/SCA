/*****************************************************************************
* Product: QF/C
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
#include "qf_pkg.h"

/*..........................................................................*/
/** \ingroup qf
* \file qeq_init.c
* \brief QEQueue_init() definition. This function is used both for Active
* Object queues and for the "raw" thread-safe queues.
*/

/*..........................................................................*/
void QEQueue_init(QEQueue *me, QEvent const *qSto[], QEQueueCtr qLen) {
    me->ring__     = &qSto[0];
    me->nTot__     = qLen;
    me->end__      = me->nTot__;
    me->head__     = (QEQueueCtr)0;
    me->tail__     = (QEQueueCtr)0;
    me->nUsed__    = (QEQueueCtr)0;
    me->nMax__     = (QEQueueCtr)0;
    me->frontEvt__ = (QEvent const *)0;           /* no events in the queue */

    QS_BEGIN_(QS_QF_EQUEUE_INIT, QS_eqObj_, me);
        QS_OBJ_(qSto);                               /* this QEQueue object */
        QS_EQC_(qLen);                           /* the length of the queue */
    QS_END_();
}
