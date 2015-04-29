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
//       File: SubMachine2Impl.h
// Created on: 09-Aug-2011 17:59:20
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C variant of Miro Samek's Quantum Framework.
//===========================================================================
#ifndef _SUBMACHINE2IMPL_H
#define _SUBMACHINE2IMPL_H

#include <qf_port.h>
#include <qassert.h>
/* Submachine impls */
#include <SubMImpl.h>


typedef struct SubMachine2Impl {
    char machineName[256];
    /** Cache of pointer to the container QActive object, for ease of access */
    QActive* active;
} SubMachine2Impl;

SubMachine2Impl* SubMachine2Impl_Constructor (SubMachine2Impl* mepl);  // Default constructor
void SubMachine2Impl_Destructor (SubMachine2Impl* mepl);  // Best-practice destructor
void SubMachine2Impl_setQActive (SubMachine2Impl* mepl, QActive* active);
////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////



/**
 * S1_SubMImpl SubMachine implementation overriding subclass.
 *
 * Override the action methods of the SubMachine individually if custom
 * behavior is desired.
 */
typedef struct S1_SubMImpl {
    SubMImpl super;
} S1_SubMImpl;

S1_SubMImpl* S1_SubMImpl_Constructor (S1_SubMImpl* mepl);  // Default constructor
void S1_SubMImpl_Destructor (S1_SubMImpl* mepl);  // Best-practice destructor
/////////////////////////////////////////////////////////////////////////
// Override submachine-instance action and guard implementation methods
/////////////////////////////////////////////////////////////////////////

/**
 * S2_SubMImpl SubMachine implementation overriding subclass.
 *
 * Override the action methods of the SubMachine individually if custom
 * behavior is desired.
 */
typedef struct S2_SubMImpl {
    SubMImpl super;
} S2_SubMImpl;

S2_SubMImpl* S2_SubMImpl_Constructor (S2_SubMImpl* mepl);  // Default constructor
void S2_SubMImpl_Destructor (S2_SubMImpl* mepl);  // Best-practice destructor
/////////////////////////////////////////////////////////////////////////
// Override submachine-instance action and guard implementation methods
/////////////////////////////////////////////////////////////////////////
#endif  /* _SUBMACHINE2IMPL_H */
