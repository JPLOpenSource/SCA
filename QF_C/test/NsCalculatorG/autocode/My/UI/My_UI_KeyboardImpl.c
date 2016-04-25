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
//       File: My/UI/My_UI_KeyboardImpl.c
// Created on: 17-Aug-2011 09:02:25
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
#include <My/UI/My_UI_KeyboardImpl.h>
#include <My/user_event_impl.h>
#include <StatechartSignals.h>

My_UI_KeyboardImpl* My_UI_KeyboardImpl_Constructor (My_UI_KeyboardImpl* mepl) {
    strcpy(mepl->machineName, "Keyboard");

    AttributeMapper_init(mepl);

    return mepl;
}

void My_UI_KeyboardImpl_destructor (My_UI_KeyboardImpl* mepl) {
    AttributeMapper_clean(mepl);
}

void My_UI_KeyboardImpl_set_qactive (My_UI_KeyboardImpl* mepl, QActive* active) {
    mepl->active = active;
}


////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////

void My_UI_KeyboardImpl_clearAll (My_UI_KeyboardImpl* mepl) {
    QF_publish((QEvent*)Q_NEW(KeyEvent, MY_CALC_CLEAR_SIG));
}

void My_UI_KeyboardImpl_clearEntry (My_UI_KeyboardImpl* mepl) {
    QF_publish((QEvent*)Q_NEW(KeyEvent, MY_CALC_CLEARENTRY_SIG));
}

void My_UI_KeyboardImpl_powerOff (My_UI_KeyboardImpl* mepl) {
    QF_publish((QEvent*)Q_NEW(KeyEvent, MY_CALC_POWEROFF_SIG));
}

void My_UI_KeyboardImpl_sendEquals (My_UI_KeyboardImpl* mepl) {
    KeyEvent* ke = Q_NEW(KeyEvent, MY_CALC_EQUALS_SIG);
    ke->keyId = '=';
    QF_publish((QEvent*)ke);
}

void My_UI_KeyboardImpl_sendKey (My_UI_KeyboardImpl* mepl, char arg1) {
    KeyEvent* ke;
    if (arg1 == '0') {
        ke = Q_NEW(KeyEvent, MY_CALC_DIGIT_0_SIG);
    } else {
        ke = Q_NEW(KeyEvent, MY_CALC_DIGIT_1_9_SIG);
    }
    ke->keyId = arg1;
    QF_publish((QEvent*)ke);

}
void My_UI_KeyboardImpl_sendPoint (My_UI_KeyboardImpl* mepl) {
    KeyEvent* ke = Q_NEW(KeyEvent, MY_CALC_POINT_SIG);
    ke->keyId = '.';
    QF_publish((QEvent*)ke);
}

void My_UI_KeyboardImpl_sendOperator (My_UI_KeyboardImpl* mepl, char arg1) {
    KeyEvent* ke = Q_NEW(KeyEvent, MY_CALC_OPER_SIG);
    ke->keyId = arg1;
    QF_publish((QEvent*)ke);
}
