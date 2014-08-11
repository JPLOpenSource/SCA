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
//       File: usereventtest_impl.h
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

typedef struct usereventtest_impl {
    char machineName[256];
    /** Cache of pointer to the container QActive object, for ease of access */
    QActive* active;

    char var[32];
} usereventtest_impl;

usereventtest_impl* usereventtest_impl_constructor (usereventtest_impl* mepl);  // Default constructor
void usereventtest_impl_destructor (usereventtest_impl* mepl);  // Best-practice destructor
void usereventtest_impl_set_qactive (usereventtest_impl* mepl, QActive* active);
////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////
bool usereventtest_impl_isValue (usereventtest_impl* mepl, const char* arg1, double arg2);

void usereventtest_impl_noUserEventOnInit (usereventtest_impl* mepl, const char* arg1, const char* arg2);
void usereventtest_impl_noUserEvent (usereventtest_impl* mepl, const char* arg1, QEvent const* e);
void usereventtest_impl_userEventOnTrans (usereventtest_impl* mepl, QEvent const* e, const char* arg2, double arg3);
void usereventtest_impl_userEventAvailable (usereventtest_impl* mepl, const char* arg1, QEvent const* e, const char* arg3);

#endif  /* _USEREVENTTEST_IMPL_H */
