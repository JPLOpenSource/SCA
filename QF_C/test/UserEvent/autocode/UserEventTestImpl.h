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
//       File: UserEventTestImpl.h
// Created on: 02-Oct-2011 22:47:03
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C variant of Miro Samek's Quantum Framework.
//===========================================================================
#ifndef _USEREVENTTEST_IMPL_H
#define _USEREVENTTEST_IMPL_H

#include <qf_port.h>
#include <qassert.h>

typedef struct UserEventTestImpl {
    char machineName[256];
    /** Cache of pointer to the container QActive object, for ease of access */
    QActive* active;

    char var[32];
} UserEventTestImpl;

UserEventTestImpl* UserEventTestImpl_Constructor (UserEventTestImpl* mepl);  // Default constructor
void UserEventTestImpl_destructor (UserEventTestImpl* mepl);  // Best-practice destructor
void UserEventTestImpl_set_qactive (UserEventTestImpl* mepl, QActive* active);
////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////
bool UserEventTestImpl_isValue (UserEventTestImpl* mepl, const char* arg1, double arg2);

void UserEventTestImpl_noUserEventOnInit (UserEventTestImpl* mepl, const char* arg1, const char* arg2);
void UserEventTestImpl_noUserEvent (UserEventTestImpl* mepl, const char* arg1, QEvent const* e);
void UserEventTestImpl_userEventOnTrans (UserEventTestImpl* mepl, QEvent const* e, const char* arg2, double arg3);
void UserEventTestImpl_userEventAvailable (UserEventTestImpl* mepl, const char* arg1, QEvent const* e, const char* arg3);

#endif  /* _USEREVENTTEST_IMPL_H */
