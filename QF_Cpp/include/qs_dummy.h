//////////////////////////////////////////////////////////////////////////////
// Product: QP/C++
// Last Updated for Version: 4.0.00
// Date of the Last Update:  Apr 06, 2008
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
#ifndef qs_dummy_h
#define qs_dummy_h

/// \file
/// \ingroup qep qf qk
/// \brief Dummy definitions of the QS macros that avoid code generation from
/// the QS instrumentation.

#ifdef Q_SPY
    #error "Q_SPY must NOT be defined to include qs_dummy.h"
#endif

#define QS_INIT(arg_)                   ((uint8_t)1)
#define QS_EXIT()                       ((void)0)
#define QS_DUMP()                       ((void)0)
#define QS_FILTER_ON(rec_)              ((void)0)
#define QS_FILTER_OFF(rec_)             ((void)0)
#define QS_FILTER_SM_OBJ(obj_)          ((void)0)
#define QS_FILTER_AO_OBJ(obj_)          ((void)0)
#define QS_FILTER_MP_OBJ(obj_)          ((void)0)
#define QS_FILTER_EQ_OBJ(obj_)          ((void)0)
#define QS_FILTER_TE_OBJ(obj_)          ((void)0)
#define QS_FILTER_AP_OBJ(obj_)          ((void)0)

#define QS_GET_BYTE(pByte_)             ((uint16_t)0xFFFF)
#define QS_GET_BLOCK(pSize_)            ((uint8_t *)0)

#define QS_BEGIN(rec_, obj_)            if (0) {
#define QS_END()                        }
#define QS_BEGIN_NOLOCK(rec_, obj_)     if (0) {
#define QS_END_NOLOCK()                 }

#define QS_I8(width_, data_)            ((void)0)
#define QS_U8(width_, data_)            ((void)0)
#define QS_I16(width_, data_)           ((void)0)
#define QS_U16(width_, data_)           ((void)0)
#define QS_I32(width_, data_)           ((void)0)
#define QS_U32(width_, data_)           ((void)0)
#define QS_F32(width_, data_)           ((void)0)
#define QS_F64(width_, data_)           ((void)0)
#define QS_STR(str_)                    ((void)0)
#define QS_STR_ROM(str_)                ((void)0)
#define QS_MEM(mem_, size_)             ((void)0)
#define QS_SIG(sig_, obj_)              ((void)0)
#define QS_OBJ(obj_)                    ((void)0)
#define QS_FUN(fun_)                    ((void)0)

#define QS_SIG_DICTIONARY(sig_, obj_)   ((void)0)
#define QS_OBJ_DICTIONARY(obj_)         ((void)0)
#define QS_FUN_DICTIONARY(fun_)         ((void)0)
#define QS_FLUSH()                      ((void)0)

// internal QS macros used only in the QP components .........................
#define QS_INT_LOCK_KEY_
#define QS_BEGIN_(rec_, refObj_, obj_)  if (0) {
#define QS_END_()                       }
#define QS_BEGIN_NOLOCK_(rec_, refObj_, obj_) if (0) {
#define QS_END_NOLOCK_()                }
#define QS_U8_(data_)                   ((void)0)
#define QS_U16_(data_)                  ((void)0)
#define QS_U32_(data_)                  ((void)0)
#define QS_TIME_()                      ((void)0)
#define QS_SIG_(sig_)                   ((void)0)
#define QS_EVS_(size_)                  ((void)0)
#define QS_OBJ_(obj_)                   ((void)0)
#define QS_FUN_(fun_)                   ((void)0)
#define QS_EQC_(ctr_)                   ((void)0)
#define QS_MPC_(ctr_)                   ((void)0)
#define QS_MPS_(size_)                  ((void)0)
#define QS_TEC_(ctr_)                   ((void)0)

#define QF_QS_INT_LOCK()                ((void)0)
#define QF_QS_INT_UNLOCK()              ((void)0)
#define QF_QS_ISR_ENTRY(isrnest_, prio_) ((void)0)
#define QF_QS_ISR_EXIT(isrnest_, prio_) ((void)0)
#define QF_QS_ACTION(act_)              ((void)0)

#endif                                                           // qs_dummy_h
