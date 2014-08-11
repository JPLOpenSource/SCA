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

/*..........................................................................*/
/** \ingroup qep
* \file qhsm_dis.c
* \brief QHsm_dispatch() implementation.
*/
/*..........................................................................*/
void QHsm_dispatch(QHsm *me, QEvent const *e) {
    QHsmState s;
    QHsmState t = ((QFsm *)me)->state__.hsm;
    QHsmState path[QEP_MAX_NEST_DEPTH_];

    path[2] = t;    /* save the current state in case a transition is taken */

    do {                             /* process the event hierarchically... */
        s = t;
        t = (QHsmState)((*s)(me, e));             /* invoke state handler s */
    } while (t != (QHsmState)0);

    if (((QFsm *)me)->tran__ != Q_TRAN_NONE_TYPE) {    /* transition taken? */
        struct QTran_ *stran;
        if ((((QFsm *)me)->tran__ & Q_TRAN_STA_TYPE) != 0) {/* static tran? */
            stran = ((QFsm *)me)->state__.tran;
        }
        else {
            path[0] = ((QFsm *)me)->state__.hsm;      /* save the new state */
            stran = (struct QTran_ *)0;
        }
        ((QFsm *)me)->state__.hsm = path[2];       /* restore current state */
        path[1] = s;                          /* save the transition source */

                  /* exit current state to the transition source path[1]... */
        for (s = path[2]; s != path[1]; ) {
            t = QEP_TRIG_(s, Q_EXIT_SIG);
            if (t != (QHsmState)0) {               /* exit action unhandled */
                s = t;                            /* t points to superstate */
            }
            else {                                   /* exit action handled */

                QS_BEGIN_(QS_QEP_STATE_EXIT, QS_smObj_, me);
                    QS_OBJ_(me);               /* this state machine object */
                    QS_FUN_(s);                         /* the exited state */
                QS_END_();

                s = QEP_TRIG_(s, QEP_EMPTY_SIG_);/* find out the superstate */
            }
        }

        if (stran != (struct QTran_ *)0) {            /* static transition? */
            uint16_t a = (uint16_t)stran->actions[0];
            if (a != (uint16_t)0) {              /* transition initialized? */
                QHsmState const *c = stran->chain;
                for (a = (uint16_t)((uint16_t)(a | (stran->actions[1] << 8))
                                    >> 1);
                     a != (uint16_t)0;
                     a >>= 2)
                {
                    uint8_t sig = (uint8_t)(a & 0x3);
                    (void)(*(*c))(me, &QEP_reservedEvt_[sig]);

                    QS_BEGIN_(sig + (uint8_t)QS_QEP_STATE_EMPTY,
                              QS_smObj_, me);
                        QS_OBJ_(me);           /* this state machine object */
                        QS_FUN_(*c);    /* entered/exited/initialized state */
                        if (sig == (QSignal)Q_INIT_SIG) {
                            QS_FUN_(((QFsm *)me)->state__.hsm);   /* target */
                        }
                    QS_END_();

                    ++c;      /* advance in the chain of the stored actions */
                }
                ((QFsm *)me)->state__.hsm = *c;        /* set the new state */
            }
            else {      /* the static transition object not initialized yet */
                path[0] = stran->chain[0];    /* save the transition target */
                s = path[2];            /* put the transition source into s */
                QHsm_execTran(me, path, stran);
            }
        }
        else {                                        /* dynamic transition */
            s = path[2];                /* put the transition source into s */
            QHsm_execTran(me, path, (struct QTran_ *)0);
        }
        ((QFsm *)me)->tran__ = Q_TRAN_NONE_TYPE; /* clear for the next tran */

        QS_BEGIN_(QS_QEP_TRAN, QS_smObj_, me);
            QS_TIME_();                                       /* time stamp */
            QS_SIG_(e->sig);                     /* the signal of the event */
            QS_OBJ_(me);                       /* this state machine object */
            QS_FUN_(s);                     /* the source of the transition */
            QS_FUN_(((QFsm *)me)->state__.hsm);     /* the new active state */
        QS_END_();
    }
    else {                                          /* transition not taken */
        #ifdef Q_SPY
        if (s != &QHsm_top) {                             /* event handled? */

            QS_BEGIN_(QS_QEP_INTERN_TRAN, QS_smObj_, me);
                QS_TIME_();                                   /* time stamp */
                QS_SIG_(e->sig);                 /* the signal of the event */
                QS_OBJ_(me);                   /* this state machine object */
                QS_FUN_(s);             /* the state that handled the event */
            QS_END_();

        }
        else {                                           /* event unhandled */

            QS_BEGIN_(QS_QEP_IGNORED, QS_smObj_, me);
                QS_TIME_();                                   /* time stamp */
                QS_SIG_(e->sig);                 /* the signal of the event */
                QS_OBJ_(me);                   /* this state machine object */
                QS_FUN_(((QFsm *)me)->state__.hsm);     /* the active state */
            QS_END_();

        }
        #endif
    }
}
