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
//       File: My/Calc/My_Calc_OperandXImpl.h
// Created on: 16-Aug-2011 00:25:02
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C variant of Miro Samek's Quantum Framework.
//===========================================================================
#ifndef MY_CALC_OPERANDXIMPL_H
#define MY_CALC_OPERANDXIMPL_H

#include <qf_port.h>
#include <qassert.h>


typedef struct My_Calc_OperandXImpl {
    char machineName[256];
    /** Cache of pointer to the container QActive object, for ease of access */
    QActive* active;
} My_Calc_OperandXImpl;

My_Calc_OperandXImpl* My_Calc_OperandXImpl_Constructor (My_Calc_OperandXImpl* mepl);  // Default constructor
void My_Calc_OperandXImpl_destructor (My_Calc_OperandXImpl* mepl);  // Best-practice destructor
void My_Calc_OperandXImpl_set_qactive (My_Calc_OperandXImpl* mepl, QActive* active);
////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////
void My_Calc_OperandXImpl_fraction (My_Calc_OperandXImpl* mepl);
void My_Calc_OperandXImpl_insert (My_Calc_OperandXImpl* mepl, QEvent const* e);

#endif  /* MY_CALC_OPERANDXIMPL_H */
