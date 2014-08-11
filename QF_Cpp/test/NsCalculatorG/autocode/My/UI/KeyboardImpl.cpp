//-*- Mode: C++; -*-
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
//       File: My/UI/KeyboardImpl.cpp
// Created on: 30-Jul-2011 10:20:31
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C++ variant of Miro Samek's Quantum Framework.
//===========================================================================
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <log_event.h>
#include <qf_port.h>
#include <qassert.h>
#include <My/UserEventImpl.h>
#include <My/UI/KeyboardImpl.h>
#include <StatechartSignals.h>

namespace My {
    namespace UI {

/**
 * Default constructor.
 */
KeyboardImpl::KeyboardImpl () {
    strcpy(this->machineName, "Keyboard");

    AttributeMapper::init(this);
}

/**
 * Required virtual destructor.
 */
KeyboardImpl::~KeyboardImpl () {
    AttributeMapper::clean(this);
}

void KeyboardImpl::setQActive (QActive* active) {
    m_active = active;
}


////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////

void KeyboardImpl::clearAll () {
	QF::publish(Q_NEW(My::KeyEvent, Clear));
}

void KeyboardImpl::clearEntry () {
	QF::publish(Q_NEW(My::KeyEvent, ClearEntry));
}

void KeyboardImpl::powerOff () {
	QF::publish(Q_NEW(My::KeyEvent, PowerOff));
}

void KeyboardImpl::sendEquals () {
	My::KeyEvent* ke = Q_NEW(My::KeyEvent, Equals);
	ke->keyId = '=';
	QF::publish(ke);
}

void KeyboardImpl::sendKey (char arg1) {
	My::KeyEvent* ke;
	if (arg1 == '0') {
		ke = Q_NEW(My::KeyEvent, Digit_0);
	} else {
		ke = Q_NEW(My::KeyEvent, Digit_1_9);
	}
	ke->keyId = arg1;
	QF::publish(ke);
}

void KeyboardImpl::sendPoint () {
	My::KeyEvent* ke = Q_NEW(My::KeyEvent, Point);
	ke->keyId = '.';
	QF::publish(ke);
}

void KeyboardImpl::sendOperator (char arg1) {
	My::KeyEvent* ke = Q_NEW(My::KeyEvent, Oper);
	ke->keyId = arg1;
	QF::publish(ke);
}

    }
}

