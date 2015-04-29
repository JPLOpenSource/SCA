/*****************************************************************************
* Product:  QEP/C platform-independent public interface
* Last Updated for Version: 4.0.02
* Date of the Last Update:  Nov 10, 2008
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
*
* Copyright (C) 2002-2008 Quantum Leaps, LLC. All rights reserved.
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
* e-mail:                  info@quantum-leaps.com
*****************************************************************************/
#ifndef qep_h
#define qep_h

/**
* \file
* \ingroup qep qf qk
* \brief Public QEP/C interface.
*
* This header file must be included, perhaps indirectly, in all modules
* (*.c files) that use QEP/C
*/

#include "qevent.h"        /* QEP event processor needs the QEvent facility */

/****************************************************************************/
/** \brief obtain the current QEP version number string
*
* \return version of the QEP as a constant 6-character string of the form
* x.y.zz, where x is a 1-digit major version number, y is a 1-digit minor
* version number, and zz is a 2-digit release number.
*/
char const Q_ROM * Q_ROM_VAR QEP_getVersion(void);

/****************************************************************************/

                    /** \brief Type returned from  a state-handler function */
typedef uint8_t QState;

                               /** \brief pointer to state-handler function */
typedef QState (*QStateHandler)(void *me, QEvent const *e);


/****************************************************************************/
/** \brief Finite State Machine
*
* QFsm represents a traditional non-hierarchical Finite State Machine (FSM)
* without state hierarchy, but with entry/exit actions.
*
* \note QFsm is not intended to be instantiated directly, but rather serves
* as the base structure for derivation of state machines in the application
* code.
*
* The following example illustrates how to derive a state machine structure
* from QFsm. Please note that the QFsm member super is defined as the FIRST
* member of the derived struct.
* \include qep_qfsm.c
*
* \sa \ref derivation
*/
typedef struct QFsmTag {
    QStateHandler state;         /**< current active state (state-variable) */
} QFsm;

/** \brief Protected "constructor" of a FSM.
*
* Performs the first step of FSM initialization by assigning the
* initial pseudostate to the currently active state of the state machine.
* \note Must be called only by the "constructors" of the derived state
* machines.
* \note Must be called only ONCE before QFsm_init().
*
* The following example illustrates how to invoke QFsm_ctor() in the
* "constructor" of a derived state machine:
* \include qep_qfsm_ctor.c
*/
#define QFsm_ctor(me_, initial_) ((me_)->state = (initial_))

/****************************************************************************/
/** \brief Performs the second step of FSM initialization by triggering the
* top-most initial transition.
*
* \param me pointer the state machine structure derived from QFsm
* \param e constant pointer the QEvent or a structure derived from QEvent
* \note Must be called only ONCE after the "constructor" QFsm_ctor().
*
* The following example illustrates how to initialize a FSM, and dispatch
* events to it:
* \include qep_qfsm_use.c
*/
void QFsm_init(QFsm *me, QEvent const *e);

/** \brief Dispatches an event to a FSM
*
* Processes one event at a time in Run-to-Completion fashion. The argument
* \a me is the pointer the state machine structure derived from QFsm.
* The argument \a e is a constant pointer the QEvent or a structure
* derived from QEvent.
*
* \note Must be called after the "constructor" QFsm_ctor() and QFsm_init().
*
* \sa example for QFsm_init() \n \ref derivation
*/
void QFsm_dispatch(QFsm *me, QEvent const *e);


/****************************************************************************/
/** \brief Hierarchical State Machine
*
* QHsm represents a Hierarchical Finite State Machine (HSM) with full
* support for hierarchical nesting of states, entry/exit actions,
* and initial transitions in any composite state.
*
* \note QHsm is not intended to be instantiated directly, but rather serves
* as the base structure for derivation of state machines in the application
* code.
*
* The following example illustrates how to derive a state machine structure
* from QHsm. Please note that the QHsm member super is defined as the FIRST
* member of the derived struct.
* \include qep_qhsm.c
*
* \sa \ref derivation
*/
typedef struct QHsmTag {
    QStateHandler state;         /**< current active state (state-variable) */

    // [SWC 2013.02.13] Separately defined QHsmTag to add two flags
    //- Output Boolean "handled" for whether QHsm dispatched the event
    //- Input Boolean "ignore_dropped" to ignore or ASSERT on dropped event
    bool handled;           /**< did QHsm handle the last dispatched event? */
    bool ignore_dropped;              /**< ignore dropped event, or assert? */
} QHsm;

/* public methods */

/** \brief protected "constructor" of a HSM.
* Performs the first step of HSM initialization by assigning the
* initial pseudostate to the currently active state of the state machine.
*
* \note Must be called only by the "constructors" of the derived state
* machines.
* \note Must be called before QHsm_init().
*
* The following example illustrates how to invoke QHsm_ctor() in the
* "constructor" of a derived state machine:
* \include qep_qhsm_ctor.c
*
* \sa #QFsm_ctor
*/
#define QHsm_ctor(me_, initial_) ((me_)->state  = (initial_))

/** \brief Performs the second step of HSM initialization by triggering the
* top-most initial transition.
*
* \param me pointer the state machine structure derived from QHsm
* \param e constant pointer the QEvent or a structure derived from QEvent
* \note Must be called only ONCE after the "constructor" QHsm_ctor().
*
* The following example illustrates how to initialize a HSM, and dispatch
* events to it:
* \include qep_qhsm_use.c
*/
void QHsm_init(QHsm *me, QEvent const *e);

/** \brief Dispatches an event to a HSM
*
* Processes one event at a time in Run-to-Completion fashion.
* \param me is the pointer the state machine structure derived from ::QHsm.
* \param e is a constant pointer the ::QEvent or a structure derived
* from ::QEvent.
*
* \note Must be called after the "constructor" QHsm_ctor() and QHsm_init().
*
* \sa example for QHsm_init() \n \ref derivation
*/
void QHsm_dispatch(QHsm *me, QEvent const *e);

/** \brief Tests if a given state is part of the current active state
* configuratioin
*
* \param me is the pointer the state machine structure derived from ::QHsm.
* \param state is a pointer to the state handler function, e.g., &QCalc_on.
*/
uint8_t QHsm_isIn(QHsm *me, QStateHandler state);

/* protected methods */

/** \brief the top-state.
*
* QHsm_top() is the ultimate root of state hierarchy in all HSMs derived
* from ::QHsm. This state handler always returns (QSTATE)0, which means
* that it "handles" all events.
*
* \sa Example of the QCalc_on() state handler for Q_INIT().
*/
QState QHsm_top(QHsm *me, QEvent const *e);

/** \brief Value returned by a non-hierarchical state-handler function when
* it ignores (does not handle) the event.
*/
#define Q_RET_IGNORED       ((QState)1)

/** \brief The macro returned from a non-hierarchical state-handler function
* when it ignores (does not handle) the event.
*
* You call that macro after the return statement (return Q_IGNORED();)
*
* \include qepn_qfsm.c
*/
#define Q_IGNORED()         (Q_RET_IGNORED)

/** \brief Value returned by a state-handler function when it handles
* the event.
*/
#define Q_RET_HANDLED       ((QState)0)

/** \brief Value returned by a state-handler function when it handles
* the event.
*
* You call that macro after the return statement (return Q_HANDLED();)
* Q_HANDLED() can be used both in the FSMs and HSMs.
*
* \include qepn_qfsm.c
*/
#define Q_HANDLED()         (Q_RET_HANDLED)

/** \brief Value returned by a state-handler function when it takes a
* regular state transition.
*/
#define Q_RET_TRAN          ((QState)2)

/** \brief Designates a target for an initial or regular transition.
* Q_TRAN() can be used both in the FSMs and HSMs.
*
* \include qepn_qtran.c
*/
/*lint -e960 */     /* ignore MISRA Rule 42 (comma operator) for this macro */
#define Q_TRAN(target_)  \
    (((QFsm *)me)->state = (QStateHandler)(target_), Q_RET_TRAN)

/** \brief Value returned by a state-handler function when it cannot
* handle the event.
*/
#define Q_RET_SUPER         ((QState)3)

/** \brief Designates the superstate of a given state in an HSM.
*
* \include qepn_qhsm.c
*/
/*lint -e960 */     /* ignore MISRA Rule 42 (comma operator) for this macro */
#define Q_SUPER(super_)  \
    (((QHsm *)me)->state = (QStateHandler)(super_),  Q_RET_SUPER)


/****************************************************************************/
/** \brief QEP reserved signals */
enum QReservedSignals {
    Q_ENTRY_SIG = 1,                   /**< signal for coding entry actions */
    Q_EXIT_SIG,                         /**< signal for coding exit actions */
    Q_INIT_SIG,                  /**< signal for coding initial transitions */
    Q_USER_SIG      /**< first signal that can be used in user applications */
};

/****************************************************************************/
/* QS software tracing integration, only if enabled                         */
#ifdef Q_SPY                                /* QS software tracing enabled? */
    #ifndef qs_h
    #include "qs_port.h"                                 /* include QS port */
    #endif                                                          /* qs_h */

    #if (Q_SIGNAL_SIZE == 1)

        /** \brief Internal QS macro to output an unformatted event signal
        * data element
        * \note the size of the pointer depends on the macro #Q_SIGNAL_SIZE.
        */
        #define QS_SIG_(sig_)       QS_u8_(sig_)
    #elif (Q_SIGNAL_SIZE == 2)
        #define QS_SIG_(sig_)       QS_u16_(sig_)
    #elif (Q_SIGNAL_SIZE == 4)
        #define QS_SIG_(sig_)       QS_u32_(sig_)
    #endif

#else
    #ifndef qs_dummy_h
    #include "qs_dummy.h"                /* disable the QS software tracing */
    #endif
#endif                                                             /* Q_SPY */

#endif                                                             /* qep_h */
