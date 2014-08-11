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
// Created on: 09-Aug-2011 17:59:20
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

SubMachine2Impl* SubMachine2Impl_Constructor (SubMachine2Impl* mepl) {
    strcpy(mepl->machineName, "SubMachine2");

    AttributeMapper_init(mepl);

    return mepl;
}

void SubMachine2Impl_Destructor (SubMachine2Impl* mepl) {
    AttributeMapper_clean(mepl);
}

void SubMachine2Impl_setQActive (SubMachine2Impl* mepl, QActive* active) {
    mepl->active = active;
}


////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////


/////////////////////////////////////////////////////////////////////////
// Override submachine-instance action and guard implementation methods
/////////////////////////////////////////////////////////////////////////

S1_SubMImpl* S1_SubMImpl_Constructor (S1_SubMImpl* mepl) {
    strcpy(((SubMachine2Impl*)mepl)->machineName, "S1:SubM");

    AttributeMapper_init(mepl);

    return mepl;
}

/**
 * Required virtual destructor.
 */
void S1_SubMImpl_Destructor (S1_SubMImpl* mepl) {
    AttributeMapper_clean(mepl);
}

S2_SubMImpl* S2_SubMImpl_Constructor (S2_SubMImpl* mepl) {
    strcpy(((SubMachine2Impl*)mepl)->machineName, "S2:SubM");

    AttributeMapper_init(mepl);

    return mepl;
}

/**
 * Required virtual destructor.
 */
void S2_SubMImpl_Destructor (S2_SubMImpl* mepl) {
    AttributeMapper_clean(mepl);
}
