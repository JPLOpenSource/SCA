//===========================================================================
// This software contains Caltech/JPL confidential information.
//
// Copyright 2009-2016, by the California Institute of Technology.
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
//       File: Simple1Impl.c
// Created on: 26-Apr-2016 16:57:32
//     Author: watney@jpl.nasa.gov
// SCACmdLine: -c -sm Simple1 ../Simple.mdxml
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
#include <assert.h>
#include <Simple1Impl.h>
#include <StatechartSignals.h>


int32_t Simple1Impl_verbosity_level = 0;


Simple1Impl *Simple1Impl_Constructor (Simple1Impl *mepl) {
    strncpy(mepl->machineName, "Simple1", 128);
    mepl->machineName[128-1] = '\0';  // null-terminate to be sure

    AttributeMapper_init(mepl);

    return mepl;
}

void Simple1Impl_set_qactive (Simple1Impl *mepl, QActive *active) {
    mepl->active = active;
}

int32_t Simple1Impl_get_verbosity () {
    return Simple1Impl_verbosity_level;
}


////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////
