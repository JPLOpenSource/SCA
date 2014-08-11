//////////////////////////////////////////////////////////////////////////////
// Product: QEP/C++ platform-independent public interface
// Last Updated for Version: 4.0.04
// Date of the Last Update:  Apr 08, 2009
//
//                    Q u a n t u m     L e a P s
//                    ---------------------------
//                    innovating embedded systems
//
// Copyright (C) 2002-2009 Quantum Leaps, LLC. All rights reserved.
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
#ifndef qep_h
#define qep_h

/// \file
/// \ingroup qep qf qk
/// \brief QEP/C++ platform-independent public interface.
///
/// This header file must be included directly or indirectly
/// in all modules (*.cpp files) that use QEP/C++.

#include "qevent.h"           // QEP event processor needs the QEvent facility

//////////////////////////////////////////////////////////////////////////////
/// \brief Provides miscellaneous QEP services.
class QEP {
public:
    /// \brief get the current QEP version number string
    ///
    /// \return version of the QEP as a constant 6-character string of the
    /// form x.y.zz, where x is a 1-digit major version number, y is a
    /// 1-digit minor version number, and zz is a 2-digit release number.
    static char const Q_ROM * Q_ROM_VAR getVersion(void);
};

//////////////////////////////////////////////////////////////////////////////

                       /// \brief Type returned from  a state-handler function
typedef uint8_t QState;

                                  /// \brief pointer to state-handler function
typedef QState (*QStateHandler)(void *me, QEvent const *e);


//////////////////////////////////////////////////////////////////////////////
/// \brief Finite State Machine base class
///
/// QFsm represents a traditional non-hierarchical Finite State Machine (FSM)
/// without state hierarchy, but with entry/exit actions.
///
/// QFsm is also a base structure for the ::QHsm class.
///
/// \note QFsm is not intended to be instantiated directly, but rather serves
/// as the base class for derivation of state machines in the application
/// code.
///
/// The following example illustrates how to derive a state machine class
/// from QFsm.
/// \include qep_qfsm.cpp
class QFsm {
protected:
    QStateHandler m_state;          ///< current active state (state-variable)

public:
    /// \brief virtual destructor
    virtual ~QFsm();

    /// \brief Performs the second step of FSM initialization by triggering
    /// the top-most initial transition.
    ///
    /// The argument \a e is constant pointer to ::QEvent or a class
    /// derived from ::QEvent.
    ///
    /// \note Must be called only ONCE before QFsm::dispatch()
    ///
    /// The following example illustrates how to initialize a FSM, and
    /// dispatch events to it:
    /// \include qep_qfsm_use.cpp
    void init(QEvent const *e = (QEvent *)0);

    /// \brief Dispatches an event to a FSM
    ///
    /// Processes one event at a time in Run-to-Completion (RTC) fashion.
    /// The argument \a e is a constant pointer the ::QEvent or a
    /// class derived from ::QEvent.
    ///
    /// \note Must be called after QFsm::init().
    ///
    /// \sa example for QFsm::init()
    void dispatch(QEvent const *e);

protected:

    /// \brief Protected constructor of a FSM.
    ///
    /// Performs the first step of FSM initialization by assigning the
    /// initial pseudostate to the currently active state of the state
    /// machine.
    ///
    /// \note The constructor is protected to prevent direct instantiating
    /// of QFsm objects. This class is intended for subclassing only.
    ///
    /// \sa The ::QFsm example illustrates how to use the QHsm constructor
    /// in the constructor initializer list of the derived state machines.
    QFsm(QStateHandler initial) : m_state(initial) {}
};

//////////////////////////////////////////////////////////////////////////////
/// \brief Hierarchical State Machine base class
///
/// QHsm represents a Hierarchical Finite State Machine (HSM). QHsm derives
/// from the ::QFsm class and extends the capabilities of a basic FSM
/// with state hierarchy.
///
/// \note QHsm is not intended to be instantiated directly, but rather serves
/// as the base structure for derivation of state machines in the application
/// code.
///
/// [SWC 2013.02.13] Add two new protected fields:
///- Output Boolean "handled" for whether QHsm dispatched the event
///- Input Boolean "ignore_dropped" to ignore or ASSERT on dropped event
///
/// The following example illustrates how to derive a state machine class
/// from QHsm.
/// \include qep_qhsm.cpp
class QHsm {
protected:
    QStateHandler m_state;          ///< current active state (state-variable)
    bool handled;              ///< did QHsm handle the last dispatched event?
    bool ignore_dropped;                 ///< ignore dropped event, or assert?

public:
    /// \brief virtual destructor
    virtual ~QHsm();

    /// \brief Performs the second step of HSM initialization by triggering
    /// the top-most initial transition.
    ///
    /// \param e constant pointer ::QEvent or a class derived from ::QEvent
    /// \note Must be called only ONCE before QHsm::dispatch()
    ///
    /// The following example illustrates how to initialize a HSM, and
    /// dispatch events to it:
    /// \include qep_qhsm_use.cpp
    void init(QEvent const *e = (QEvent *)0);

    /// \brief Dispatches an event to a HSM
    ///
    /// Processes one event at a time in Run-to-Completion (RTC) fashion.
    /// The argument \a e is a constant pointer the ::QEvent or a
    /// class derived from ::QEvent.
    ///
    /// \note Must be called after QHsm::init().
    ///
    /// \sa example for QHsm::init()
    void dispatch(QEvent const *e);

    /// \brief Tests if a given state is part of the current active state
    /// configuratioin
    ///
    /// \param state is a pointer to the state handler function, e.g.,
    /// &QCalc::on.
    uint8_t isIn(QStateHandler state);

    /// \brief Tests if last event dispatch was handled
    bool isHandled();

    /// \brief Sets state to ignore dropped event on the next dispatch call
    ///
    /// \param flag  boolean state to set ignore_dropped to
    void setIgnoreDropped(bool flag);

protected:

    /// \brief Protected constructor of a HSM.
    ///
    /// Performs the first step of HSM initialization by assigning the
    /// initial pseudostate to the currently active state of the state
    /// machine.
    ///
    /// \note The constructor is protected to prevent direct instantiating
    /// of QHsm objects. This class is intended for subclassing only.
    ///
    /// \sa The ::QHsm example illustrates how to use the QHsm constructor
    /// in the constructor initializer list of the derived state machines.
    /// \sa QFsm::QFsm()
    QHsm(QStateHandler initial) : m_state(initial) {}

    /// \brief the top-state.
    ///
    /// QHsm::top() is the ultimate root of state hierarchy in all HSMs
    /// derived from ::QHsm. This state handler always returns (QSTATE)0,
    /// which means that it "handles" all events.
    ///
    /// \sa Example of the QCalc::on() state handler.
    static QState top(QHsm *me, QEvent const *e);
};

/// \brief Value returned by a non-hierarchical state-handler function when
/// it ignores (does not handle) the event.
#define Q_RET_IGNORED       ((QState)1)

/// \brief The macro returned from a non-hierarchical state-handler function
/// when it ignores (does not handle) the event.
///
/// You call that macro after the return statement (return Q_IGNORED();)
///
/// \include qepn_qfsm.cpp
#define Q_IGNORED()         (Q_RET_IGNORED)

/// \brief Value returned by a state-handler function when it handles
/// the event.
#define Q_RET_HANDLED       ((QState)0)

/// \brief Value returned by a state-handler function when it handles
/// the event.
///
/// You call that macro after the return statement (return Q_HANDLED();)
/// Q_HANDLED() can be used both in the FSMs and HSMs.
///
/// \include qepn_qfsm.cpp
#define Q_HANDLED()         (Q_RET_HANDLED)

/// \brief Value returned by a state-handler function when it takes a
/// regular state transition.
#define Q_RET_TRAN          ((QState)2)

/// \brief Designates a target for an initial or regular transition.
/// Q_TRAN() can be used both in the FSMs and HSMs.
///
/// \include qepn_qtran.cpp
//lint -e960 -e1924 ignore MISRA Rule 42 (comma operator) and C-style cast
#define Q_TRAN(target_)  \
    (me->m_state = (QStateHandler)(target_), Q_RET_TRAN)

/// \brief Value returned by a state-handler function when it cannot
/// handle the event.
#define Q_RET_SUPER         ((QState)3)

/// \brief Designates the superstate of a given state in an HSM.
///
/// \include qepn_qhsm.cpp
//lint -e960 -e1924 ignore MISRA Rule 42 (comma operator) and C-style cast
#define Q_SUPER(super_)  \
    (me->m_state = (QStateHandler)(super_),  Q_RET_SUPER)


//////////////////////////////////////////////////////////////////////////////
/// \brief QEP reserved signals.
enum QReservedSignals {
    Q_ENTRY_SIG = 1,                             ///< signal for entry actions
    Q_EXIT_SIG,                                   ///< signal for exit actions
    Q_INIT_SIG,                     ///< signal for nested initial transitions
    Q_USER_SIG                              ///< signal to offset user signals
};

//////////////////////////////////////////////////////////////////////////////
// QS software tracing integration, only if enabled
#ifdef Q_SPY                                   // QS software tracing enabled?
    #ifndef qs_h
    #include "qs_port.h"                                    // include QS port
    #endif                                                             // qs_h

    #if (Q_SIGNAL_SIZE == 1)

        /// \brief Internal QS macro to output an unformatted event signal
        /// data element
        /// \note the size of the pointer depends on the macro #Q_SIGNAL_SIZE.
        #define QS_SIG_(sig_)       QS::u8_(sig_)
    #elif (Q_SIGNAL_SIZE == 2)
        #define QS_SIG_(sig_)       QS::u16_(sig_)
    #elif (Q_SIGNAL_SIZE == 4)
        #define QS_SIG_(sig_)       QS::u32_(sig_)
    #endif

#else
    #ifndef qs_dummy_h
    #include "qs_dummy.h"                   // disable the QS software tracing
    #endif
#endif                                                                // Q_SPY

#endif                                                                // qep_h
