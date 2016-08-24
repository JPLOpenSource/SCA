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
//       File: Simple1Impl.h
// Created on: 26-Apr-2016 16:57:32
//     Author: watney@jpl.nasa.gov
// SCACmdLine: -c -sm Simple1 ../Simple.mdxml
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C variant of Miro Samek's Quantum Framework.
//===========================================================================
#ifndef SIMPLE1IMPL_H
#define SIMPLE1IMPL_H

#include <qf_port.h>
#include <qassert.h>


typedef struct Simple1Impl {
    char machineName[128];
    /** Cache of pointer to the container QActive object, for ease of access */
    QActive *active;
} Simple1Impl;

Simple1Impl *Simple1Impl_Constructor (Simple1Impl *mepl);  // Default constructor
void Simple1Impl_set_qactive (Simple1Impl *mepl, QActive *active);
int32_t Simple1Impl_get_verbosity ();
////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////

#endif  /* SIMPLE1IMPL_H */
