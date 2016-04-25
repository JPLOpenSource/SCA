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
//       File: KeyboardImpl.h
// Created on: 02-Oct-2011 21:28:57
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C variant of Miro Samek's Quantum Framework.
//===========================================================================
#ifndef _KEYBOARD_IMPL_H
#define _KEYBOARD_IMPL_H

#include <qf_port.h>
#include <qassert.h>


typedef struct KeyboardImpl {
    char machineName[256];
    /** Cache of pointer to the container QActive object, for ease of access */
    QActive* active;
} KeyboardImpl;

KeyboardImpl* KeyboardImpl_Constructor (KeyboardImpl* mepl);  // Default constructor
void KeyboardImpl_destructor (KeyboardImpl* mepl);  // Best-practice destructor
void KeyboardImpl_set_qactive (KeyboardImpl* mepl, QActive* active);
////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////
void KeyboardImpl_clearAll (KeyboardImpl* mepl);
void KeyboardImpl_clearEntry (KeyboardImpl* mepl);
void KeyboardImpl_powerOff (KeyboardImpl* mepl);
void KeyboardImpl_sendEquals (KeyboardImpl* mepl);
void KeyboardImpl_sendKey (KeyboardImpl* mepl, char arg1);
void KeyboardImpl_sendOperator (KeyboardImpl* mepl, char arg1);
void KeyboardImpl_sendPoint (KeyboardImpl* mepl);

#endif  /* _KEYBOARD_IMPL_H */
