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
//       File: ZTestUserEventsImpl.h
// Created on: 30-Jul-2011 10:20:51
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C++ variant of Miro Samek's Quantum Framework.
//===========================================================================
#ifndef ZTestUserEventsImpl_h
#define ZTestUserEventsImpl_h

#include <qf_port.h>
#include <qassert.h>
#include <UserEventImpl.h>

class ZTestUserEventsImpl {

    friend class ZTestUserEventsImplSpy;

public:
    ZTestUserEventsImpl ();  // Default constructor
    virtual ~ZTestUserEventsImpl ();  // Required virtual destructor

    virtual void setQActive (QActive* active);

    ////////////////////////////////////////////
    // Action and guard implementation methods
    ////////////////////////////////////////////
    virtual bool isInS2 ();

    virtual void init ();
    virtual void sendEv1 (const char* arg1);
    virtual void sendEv2 ();
    virtual void storeEventData (QEvent const* e);
    virtual void cleanup ();

    static const int PRIO_UET = 2;  // hardwired prioity level for UserEventTest

private:
    char machineName[256];
    /** Cache of pointer to the container QActive object, for ease of access */
    QActive* m_active;
    // private storage of user data
    char userData[MAX_DATA_SIZE];

};

#endif  /* ZTestUserEventsImpl_h */
