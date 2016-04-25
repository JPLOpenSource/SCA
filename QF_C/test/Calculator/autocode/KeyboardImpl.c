//===========================================================================
// This software contains Caltech/JPL confidential information.
//
// Copyright 2009-2011, by the California Institute of Technology.
// ALL RIGHTS RESERVED. United States Government Sponsorship Acknowledged.
// Any commercial use must be negotiated with the Office of Technology
// Transfer at the California Institute of Technology.
//
// This software may be subject to US export control laws and
// regulations. By accepting this document, the user agrees to comply
// with all applicable U.S. export laws and regulations, including the
// International Traffic and Arms Regulations, 22 C.F.R. 120-130 and the
// Export Administration Regulations, 15 C.F.R. 730-744. User has the
// responsibility to obtain export licenses, or other export authority as
// may be required before exporting such information to foreign countries
// or providing access to foreign persons.
//===========================================================================
//
//       File: KeyboardImpl.c
// Created on: 02-Oct-2011 21:28:57
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C variant of Miro Samek's Quantum Framework.
//===========================================================================
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <log_event.h>
#include <qf_port.h>
#include <qassert.h>
#include <KeyboardImpl.h>
#include <user_event_impl.h>
#include <StatechartSignals.h>

KeyboardImpl* KeyboardImpl_Constructor (KeyboardImpl* mepl) {
    strcpy(mepl->machineName, "keyboard");

    AttributeMapper_init(mepl);

    return mepl;
}

void KeyboardImpl_destructor (KeyboardImpl* mepl) {
    AttributeMapper_clean(mepl);
}

void KeyboardImpl_set_qactive (KeyboardImpl* mepl, QActive* active) {
    mepl->active = active;
}


////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////

void KeyboardImpl_clearAll (KeyboardImpl* mepl) {
    QF_publish((QEvent*)Q_NEW(KeyEvent, CLEAR_SIG));
}

void KeyboardImpl_clearEntry (KeyboardImpl* mepl) {
    QF_publish((QEvent*)Q_NEW(KeyEvent, CLEARENTRY_SIG));
}

void KeyboardImpl_powerOff (KeyboardImpl* mepl) {
    QF_publish((QEvent*)Q_NEW(KeyEvent, POWEROFF_SIG));
}

void KeyboardImpl_sendEquals (KeyboardImpl* mepl) {
    KeyEvent* ke = Q_NEW(KeyEvent, EQUALS_SIG);
    ke->keyId = '=';
    QF_publish((QEvent*)ke);
}

void KeyboardImpl_sendKey (KeyboardImpl* mepl, char arg1) {
    KeyEvent* ke;
    if (arg1 == '0') {
        ke = Q_NEW(KeyEvent, DIGIT_0_SIG);
    } else {
        ke = Q_NEW(KeyEvent, DIGIT_1_9_SIG);
    }
    ke->keyId = arg1;
    QF_publish((QEvent*)ke);
}

void KeyboardImpl_sendPoint (KeyboardImpl* mepl) {
    KeyEvent* ke = Q_NEW(KeyEvent, POINT_SIG);
    ke->keyId = '.';
    QF_publish((QEvent*)ke);
}

void KeyboardImpl_sendOperator (KeyboardImpl* mepl, char arg1) {
    KeyEvent* ke = Q_NEW(KeyEvent, OPER_SIG);
    ke->keyId = arg1;
    QF_publish((QEvent*)ke);
}
