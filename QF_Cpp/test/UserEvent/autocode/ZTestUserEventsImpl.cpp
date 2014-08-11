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
//       File: ZTestUserEventsImpl.cpp
// Created on: 30-Jul-2011 10:20:51
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
#include <UserEventTest.h>
#include <ZTestUserEventsImpl.h>
#include <StatechartSignals.h>

/**
 * Default constructor.
 */
ZTestUserEventsImpl::ZTestUserEventsImpl () {
    strcpy(this->machineName, "ZTestUserEvents");
}

/**
 * Required virtual destructor.
 */
ZTestUserEventsImpl::~ZTestUserEventsImpl () {
}

void ZTestUserEventsImpl::setQActive (QActive* active) {
    m_active = active;
}


////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////

bool ZTestUserEventsImpl::isInS2 () {
	bool rv = this->m_active->isIn((QStateHandler) &UserEventTest::S2);
    printf("%s.isInS2() == %s\n", this->machineName, AttributeMapper::booltostr(rv));
    return rv;
}

void ZTestUserEventsImpl::init () {
	this->m_active = QF::active_[ZTestUserEventsImpl::PRIO_UET];
}

void ZTestUserEventsImpl::sendEv1 (const char* arg1) {
	DataEvent* de = Q_NEW(DataEvent, Ev1);
	strncpy(de->data, arg1, MAX_DATA_SIZE);
	QF::publish(de);
}

void ZTestUserEventsImpl::sendEv2 () {
	DataEvent* de = Q_NEW(DataEvent, Ev2);
	// transfer stored user data to event to publish
	strncpy(de->data, this->userData, MAX_DATA_SIZE);
	QF::publish(de);
}

void ZTestUserEventsImpl::storeEventData (QEvent const* e) {
    DataEvent const* de = static_cast<DataEvent const*>(e);
	strncpy(this->userData, de->data, MAX_DATA_SIZE);
}

void ZTestUserEventsImpl::cleanup () {
	printf("%s completed!\n", this->machineName);
}
