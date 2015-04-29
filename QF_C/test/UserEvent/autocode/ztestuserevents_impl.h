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
//       File: ztestuserevents_impl.h
// Created on: 02-Oct-2011 22:54:03
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C variant of Miro Samek's Quantum Framework.
//===========================================================================
#ifndef _ZTESTUSEREVENTS_IMPL_H
#define _ZTESTUSEREVENTS_IMPL_H

#include <qf_port.h>
#include <qassert.h>
#include <user_event_impl.h>

typedef struct ztestuserevents_impl {
    char machineName[256];
    /** Cache of pointer to the container QActive object, for ease of access */
    QActive* active;
    // private storage of user data
    char userData[MAX_DATA_SIZE];
} ztestuserevents_impl;

ztestuserevents_impl* ztestuserevents_impl_constructor (ztestuserevents_impl* mepl);  // Default constructor
void ztestuserevents_impl_destructor (ztestuserevents_impl* mepl);  // Best-practice destructor
void ztestuserevents_impl_set_qactive (ztestuserevents_impl* mepl, QActive* active);
////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////
bool ztestuserevents_impl_isInS2 (ztestuserevents_impl* mepl);

void ztestuserevents_impl_init (ztestuserevents_impl* mepl);
void ztestuserevents_impl_sendEv1 (ztestuserevents_impl* mepl, const char* arg1);
void ztestuserevents_impl_sendEv2 (ztestuserevents_impl* mepl);
void ztestuserevents_impl_storeEventData (ztestuserevents_impl* mepl, QEvent const* e);
void ztestuserevents_impl_cleanup (ztestuserevents_impl* mepl);

#endif  /* _ZTESTUSEREVENTS_IMPL_H */
