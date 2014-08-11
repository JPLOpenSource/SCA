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
//       File: My/UI/my_ui_keyboard_impl.h
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


typedef struct my_ui_keyboard_impl {
    char machineName[256];
    /** Cache of pointer to the container QActive object, for ease of access */
    QActive* active;
} my_ui_keyboard_impl;

my_ui_keyboard_impl* my_ui_keyboard_impl_constructor (my_ui_keyboard_impl* mepl);  // Default constructor
void my_ui_keyboard_impl_destructor (my_ui_keyboard_impl* mepl);  // Best-practice destructor
void my_ui_keyboard_impl_set_qactive (my_ui_keyboard_impl* mepl, QActive* active);
////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////
void my_ui_keyboard_impl_clearAll (my_ui_keyboard_impl* mepl);
void my_ui_keyboard_impl_clearEntry (my_ui_keyboard_impl* mepl);
void my_ui_keyboard_impl_powerOff (my_ui_keyboard_impl* mepl);
void my_ui_keyboard_impl_sendEquals (my_ui_keyboard_impl* mepl);
void my_ui_keyboard_impl_sendKey (my_ui_keyboard_impl* mepl, char arg1);
void my_ui_keyboard_impl_sendOperator (my_ui_keyboard_impl* mepl, char arg1);
void my_ui_keyboard_impl_sendPoint (my_ui_keyboard_impl* mepl);

#endif  /* MY_UI_KEYBOARDIMPL_H */
