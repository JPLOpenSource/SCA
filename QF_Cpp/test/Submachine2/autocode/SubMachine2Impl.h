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
// Created on: 30-Jul-2011 10:19:33
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C++ variant of Miro Samek's Quantum Framework.
//===========================================================================
#ifndef SubMachine2Impl_h
#define SubMachine2Impl_h

#include <qf_port.h>
#include <qassert.h>
/* Submachine impls */
#include <SubMImpl.h>

class SubMachine2Impl {

    friend class SubMachine2ImplSpy;

public:
    SubMachine2Impl ();  // Default constructor
    virtual ~SubMachine2Impl ();  // Required virtual destructor

    virtual void setQActive (QActive* active);

    ////////////////////////////////////////////
    // Action and guard implementation methods
    ////////////////////////////////////////////

private:
    char machineName[256];
    /** Cache of pointer to the container QActive object, for ease of access */
    QActive* m_active;

};


/**
 * S1_SubMImpl SubMachine implementation overriding subclass.
 *
 * Override the action methods of the SubMachine individually if custom
 * behavior is desired.
 */
class S1_SubMImpl : public SubMImpl {

    friend class S1_SubMImplSpy;

public:
    S1_SubMImpl ();  // Default constructor
    virtual ~S1_SubMImpl ();  // Default virtual destructor

    /////////////////////////////////////////////////////////////////////////
    // Override submachine-instance action and guard implementation methods
    /////////////////////////////////////////////////////////////////////////
    virtual void takeAction ();

private:
    char machineName[256];

};

/**
 * S2_SubMImpl SubMachine implementation overriding subclass.
 *
 * Override the action methods of the SubMachine individually if custom
 * behavior is desired.
 */
class S2_SubMImpl : public SubMImpl {

    friend class S2_SubMImplSpy;

public:
    S2_SubMImpl ();  // Default constructor
    virtual ~S2_SubMImpl ();  // Default virtual destructor

    /////////////////////////////////////////////////////////////////////////
    // Override submachine-instance action and guard implementation methods
    /////////////////////////////////////////////////////////////////////////
    virtual void takeAction ();

private:
    char machineName[256];

};
#endif  /* SubMachine2Impl_h */
