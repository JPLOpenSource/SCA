/*****************************************************************************
* Product: QEP/C
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
#ifndef qep_pkg_h
#define qep_pkg_h

/** \ingroup qep
* \file qep_pkg.h
* \brief Internal (package scope) QEP/C interface.
*/

#include "qep_port.h"                                           /* QEP port */

#ifdef Q_SPY
    #include "qs_port.h"                                         /* QS port */
#else
    #define QS_BEGIN_(rec_, refObj_, obj_)  if (0) { ((void)0)
    #define QS_END_()                       } else ((void)0)
    #define QS_U8_(data_)                   ((void)0)
    #define QS_U16_(data_)                  ((void)0)
    #define QS_U32_(data_)                  ((void)0)
    #define QS_TIME_()                      ((void)0)
    #define QS_SIG_(sig_)                   ((void)0)
    #define QS_OBJ_(obj_)                   ((void)0)
    #define QS_FUN_(fun_)                   ((void)0)
#endif

extern QEvent const QEP_reservedEvt_[];   /**< preallocated reserved events */

/** internal QEP constants */
enum QEPConst {
    QEP_EMPTY_SIG_ = 0,             /**< empty signal for internal use only */

    /** maximum depth of state nesting (including the top level),
     * must be >= 3
     */
    QEP_MAX_NEST_DEPTH_ = 6
};

/** helper macro to trigger reserved event in an HSM */
#define QEP_TRIG_(state_, sig_) \
    ((QHsmState)(*(state_))(me, &QEP_reservedEvt_[sig_]))

/** helper function to execute HSM transition */
void QHsm_execTran(QHsm *me, QHsmState path[], struct QTran_ *tran);

#endif                                                         /* qep_pkg_h */
