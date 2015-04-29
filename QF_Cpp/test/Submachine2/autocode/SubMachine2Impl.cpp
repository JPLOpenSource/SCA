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
//       File: SubMachine2Impl.cpp
// Created on: 30-Jul-2011 10:19:33
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
#include <SubMachine2Impl.h>
#include <StatechartSignals.h>

/**
 * Default constructor.
 */
SubMachine2Impl::SubMachine2Impl () {
    strcpy(this->machineName, "SubMachine2");

    AttributeMapper::init(this);
}

/**
 * Required virtual destructor.
 */
SubMachine2Impl::~SubMachine2Impl () {
    AttributeMapper::clean(this);
}

void SubMachine2Impl::setQActive (QActive* active) {
    m_active = active;
}


////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////


/////////////////////////////////////////////////////////////////////////
// Override submachine-instance action and guard implementation methods
/////////////////////////////////////////////////////////////////////////

/**
 * Default constructor.
 */
S1_SubMImpl::S1_SubMImpl () {
    strcpy(this->machineName, "S1:SubM");

    AttributeMapper::init(this);
}

/**
 * Required virtual destructor.
 */
S1_SubMImpl::~S1_SubMImpl () {
    AttributeMapper::clean(this);
}

void S1_SubMImpl::takeAction () {
    printf("%s.takeAction() overridden action implementation invoked\n", this->machineName);
}

/**
 * Default constructor.
 */
S2_SubMImpl::S2_SubMImpl () {
    strcpy(this->machineName, "S2:SubM");

    AttributeMapper::init(this);
}

/**
 * Required virtual destructor.
 */
S2_SubMImpl::~S2_SubMImpl () {
    AttributeMapper::clean(this);
}

void S2_SubMImpl::takeAction () {
    printf("%s.takeAction() overridden action implementation invoked\n", this->machineName);
}
