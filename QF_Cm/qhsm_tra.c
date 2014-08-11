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
#include "qep_pkg.h"
#include "qassert.h"

Q_DEFINE_THIS_MODULE(qhsm_tra)

/** \ingroup qep
* \file qhsm_tra.c
* \brief QHsm_execTran() implementation.
*/

/* helper macros ...........................................................*/
#define QEP_REC_(state_, sig_) \
    actions = (uint16_t)((actions >> 2) | ((uint16_t)(sig_) << 14)); \
    tran->chain[ic++] = (state_)

#define QEP_EXIT_AND_REC_(state_) \
    if (QEP_TRIG_(state_, Q_EXIT_SIG) == (QHsmState)0) { \
        QS_BEGIN_(QS_QEP_STATE_EXIT, QS_smObj_, me); \
            QS_OBJ_(me); \
            QS_FUN_(state_); \
        QS_END_(); \
        if (tran != (struct QTran_ *)0) { \
            QEP_REC_(state_, Q_EXIT_SIG); \
        } \
    } else ((void)0)

#define QEP_ENTER_AND_REC_(state_) \
    if (QEP_TRIG_(state_, Q_ENTRY_SIG) == (QHsmState)0) { \
        QS_BEGIN_(QS_QEP_STATE_ENTRY, QS_smObj_, me); \
            QS_OBJ_(me); \
            QS_FUN_(state_); \
        QS_END_(); \
        if (tran != (struct QTran_ *)0) { \
            QEP_REC_(state_, Q_ENTRY_SIG); \
        } \
    } else ((void)0)

/*..........................................................................*/
void QHsm_execTran(QHsm *me, QHsmState path[], struct QTran_ *tran) {
    QHsmState s;
    QHsmState t = path[0];                      /* target of the transition */
    QHsmState const src = path[1];              /* source of the transition */
    uint16_t actions = (uint16_t)0;     /* actions bitmask for static tran. */
    uint8_t ic = (uint8_t)0;               /* static-transition chain index */
    int8_t ip = (int8_t)(-1);                /* transition entry path index */
    int8_t iq;                        /* helper transition entry path index */

    if (src == t) {      /* (a) check source == target (transition to self) */
        QEP_EXIT_AND_REC_(src);                          /* exit the source */
        ++ip;                                           /* enter the target */
    }
    else {
        t = QEP_TRIG_(t, QEP_EMPTY_SIG_);/* put superstate of target into t */
        if (src == t) {                /* (b) check source == target->super */
            ++ip;                                       /* enter the target */
        }
        else {
            s = QEP_TRIG_(src, QEP_EMPTY_SIG_);/*superstate of source into s*/
            if (s == t) {       /* (c) check source->super == target->super */
                QEP_EXIT_AND_REC_(src);                  /* exit the source */
                ++ip;                                   /* enter the target */
            }
            else {
                if (s == path[0]) {    /* (d) check source->super == target */
                    QEP_EXIT_AND_REC_(src);              /* exit the source */
                }
                else { /* (e) check rest of source == target->super->super...
                        * and store the entry path along the way
                        */
                    iq = (int8_t)0;          /* indicate that LCA not found */

                    ++ip;                               /* enter the target */
                    path[++ip] = t;       /* enter the superstate of target */
                    t = QEP_TRIG_(t, QEP_EMPTY_SIG_);
                    while (t != (QHsmState)0) {
                        path[++ip] = t;             /* store the entry path */
                        if (t == src) {                /* is it the source? */
                            iq = (int8_t)1;  /* indicate that the LCA found */
                                            /* entry path must not overflow */
                            Q_ASSERT(ip < (int8_t)QEP_MAX_NEST_DEPTH_);
                            --ip;                /* do not enter the source */
                            t = (QHsmState)0;         /* terminate the loop */
                        }
                        else {       /* it is not the source, keep going up */
                            t = QEP_TRIG_(t, QEP_EMPTY_SIG_);
                        }
                    }
                    if (iq == (int8_t)0) {        /* the LCA not found yet? */

                                            /* entry path must not overflow */
                        Q_ASSERT(ip < (int8_t)QEP_MAX_NEST_DEPTH_);

                        QEP_EXIT_AND_REC_(src);              /* exit source */

                            /* (f) check the rest of source->super
                             *                      == target->super->super...
                             */
                        iq = ip;
                        do {
                            if (s == path[iq]) {        /* is this the LCA? */
                                t = s;    /* indicate that the LCA is found */
                                ip = (int8_t)(iq - 1);  /* do not enter LCA */
                                iq = (int8_t)(-1);    /* terminate the loop */
                            }
                            else {
                                --iq;     /* try lower superstate of target */
                            }
                        } while (iq >= (int8_t)0);

                        if (t == (QHsmState)0) {  /* the LCA not found yet? */
                                /* (g) check each source->super->...
                                 * for each target->super...
                                 */
                            do {
                                t = QEP_TRIG_(s, Q_EXIT_SIG);     /* exit s */
                                if (t != (QHsmState)0) {  /* exit unhandled */
                                    s = t;   /* t points to superstate of s */
                                }
                                else {               /* exit action handled */

                                    QS_BEGIN_(QS_QEP_STATE_EXIT,
                                              QS_smObj_, me);
                                        QS_OBJ_(me);
                                        QS_FUN_(s);
                                    QS_END_();

                                    if (tran != (struct QTran_ *)0) {
                                        QEP_REC_(s, Q_EXIT_SIG);
                                    }
                                    s = QEP_TRIG_(s, QEP_EMPTY_SIG_);
                                }
                                iq = ip;
                                do {
                                    if (s == path[iq]) {/* is this the LCA? */
                                                    /* do not enter the LCA */
                                        ip = (int8_t)(iq - 1);
                                        iq = (int8_t)(-1);/*break inner loop*/
                                        s = (QHsmState)0; /* and outer loop */
                                    }
                                    else {
                                        --iq;
                                    }
                                } while (iq >= (int8_t)0);
                            } while (s != (QHsmState)0);
                        }
                    }
                }
            }
        }
    }
                    /* retrace the entry path in reverse (desired) order... */
    for (; ip >= (int8_t)0; --ip) {
        QEP_ENTER_AND_REC_(path[ip]);                     /* enter path[ip] */
    }
    s = path[0];                          /* stick the target into register */
    ((QFsm *)me)->state__.hsm = s;              /* update the current state */

    while (QEP_TRIG_(s, Q_INIT_SIG) == (QHsmState)0) { /* drill into target */

        t = ((QFsm *)me)->state__.hsm;

        QS_BEGIN_(QS_QEP_STATE_INIT, QS_smObj_, me);
            QS_OBJ_(me);                       /* this state machine object */
            QS_FUN_(s);                         /* the source (pseudo)state */
            QS_FUN_(t);                     /* the target of the transition */
        QS_END_();

        if (tran != (struct QTran_ *)0) {             /* static tranistion? */
            QEP_REC_(s, Q_INIT_SIG);
        }
                                               /* store the entry path to s */
        path[0] = t;
        ip = (int8_t)0;
        for (t = QEP_TRIG_(t, QEP_EMPTY_SIG_); t != s;
             t = QEP_TRIG_(t, QEP_EMPTY_SIG_))
        {
            path[++ip] = t;
        }
                                            /* entry path must not overflow */
        Q_ASSERT(ip < (int8_t)QEP_MAX_NEST_DEPTH_);

        do {        /* retrace the entry path in reverse (correct) order... */
            QEP_ENTER_AND_REC_(path[ip]);                 /* enter path[ip] */
            --ip;
        } while (ip >= (int8_t)0);
        s = ((QFsm *)me)->state__.hsm;
    }

    if (tran != (struct QTran_ *)0) {                 /* static transition? */
                                      /* transition chain must not overflow */
        Q_ENSURE(ic < (uint8_t)Q_DIM(tran->chain));

        tran->chain[ic] = s; /* store the ultimate target of the transition */
        actions = (uint16_t)(actions >> (15 - (ic << 1)));
        tran->actions[1] = (uint8_t)(actions >> 8);
        tran->actions[0] = (uint8_t)(actions | 0x1);
    }
}
