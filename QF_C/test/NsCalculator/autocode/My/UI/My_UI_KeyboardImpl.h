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
//       File: My/UI/My_UI_KeyboardImpl.h
// Created on: 17-Aug-2011 13:30:36
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C variant of Miro Samek's Quantum Framework.
//===========================================================================
#ifndef MY_UI_KEYBOARDIMPL_H
#define MY_UI_KEYBOARDIMPL_H

#include <qf_port.h>
#include <qassert.h>


typedef struct My_UI_KeyboardImpl {
    char machineName[256];
    /** Cache of pointer to the container QActive object, for ease of access */
    QActive* active;
} My_UI_KeyboardImpl;

My_UI_KeyboardImpl* My_UI_KeyboardImpl_Constructor (My_UI_KeyboardImpl* mepl);  // Default constructor
void My_UI_KeyboardImpl_destructor (My_UI_KeyboardImpl* mepl);  // Best-practice destructor
void My_UI_KeyboardImpl_set_qactive (My_UI_KeyboardImpl* mepl, QActive* active);
////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////
void My_UI_KeyboardImpl_clearAll (My_UI_KeyboardImpl* mepl);
void My_UI_KeyboardImpl_clearEntry (My_UI_KeyboardImpl* mepl);
void My_UI_KeyboardImpl_powerOff (My_UI_KeyboardImpl* mepl);
void My_UI_KeyboardImpl_sendEquals (My_UI_KeyboardImpl* mepl);
void My_UI_KeyboardImpl_sendKey (My_UI_KeyboardImpl* mepl, char arg1);
void My_UI_KeyboardImpl_sendOperator (My_UI_KeyboardImpl* mepl, char arg1);
void My_UI_KeyboardImpl_sendPoint (My_UI_KeyboardImpl* mepl);

#endif  /* MY_UI_KEYBOARDIMPL_H */
