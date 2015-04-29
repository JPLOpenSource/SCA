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
//       File: operandx_impl.h
// Created on: 02-Oct-2011 21:28:57
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C variant of Miro Samek's Quantum Framework.
//===========================================================================
#ifndef _OPERANDX_IMPL_H
#define _OPERANDX_IMPL_H

#include <qf_port.h>
#include <qassert.h>


typedef struct operandx_impl {
    char machineName[256];
    /** Cache of pointer to the container QActive object, for ease of access */
    QActive* active;
} operandx_impl;

operandx_impl* operandx_impl_constructor (operandx_impl* mepl);  // Default constructor
void operandx_impl_destructor (operandx_impl* mepl);  // Best-practice destructor
void operandx_impl_set_qactive (operandx_impl* mepl, QActive* active);
////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////
void operandx_impl_fraction (operandx_impl* mepl);
void operandx_impl_insert (operandx_impl* mepl, QEvent const* e);

#endif  /* _OPERANDX_IMPL_H */
