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
//       File: calculator_impl.h
// Created on: 02-Oct-2011 21:28:57
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C variant of Miro Samek's Quantum Framework.
//===========================================================================
#ifndef _CALCULATOR_IMPL_H
#define _CALCULATOR_IMPL_H

#include <qf_port.h>
#include <qassert.h>
/* Submachine impls */
#include <operandx_impl.h>


typedef struct calculator_impl {
    char machineName[256];
    /** Cache of pointer to the container QActive object, for ease of access */
    QActive* active;
    // calculator data
    char operand[32];
    char resultStr[32];
    char oper;
    double lOperand;
    double rOperand;
    double result;
    char errStat[256];
} calculator_impl;

calculator_impl* calculator_impl_constructor (calculator_impl* mepl);  // Default constructor
void calculator_impl_destructor (calculator_impl* mepl);  // Best-practice destructor
void calculator_impl_set_qactive (calculator_impl* mepl, QActive* active);
////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////
void calculator_impl_set_isKeyId (calculator_impl* mepl, bool flag);
void calculator_impl_set_onError (calculator_impl* mepl, bool flag);
bool calculator_impl_isKeyId (calculator_impl* mepl, QEvent const* e, const char arg1);
bool calculator_impl_onError (calculator_impl* mepl);
void calculator_impl_ce (calculator_impl* mepl);
void calculator_impl_clearAll (calculator_impl* mepl);
void calculator_impl_negate (calculator_impl* mepl);
void calculator_impl_updateOperand (calculator_impl* mepl, QEvent const* e);
void calculator_impl_op (calculator_impl* mepl, QEvent const* e);
void calculator_impl_compute (calculator_impl* mepl, QEvent const* e);
void calculator_impl_reportResult (calculator_impl* mepl);



/**
 * operand1_operandx_impl SubMachine implementation overriding subclass.
 *
 * Override the action methods of the SubMachine individually if custom
 * behavior is desired.
 */
typedef struct operand1_operandx_impl {
    operandx_impl super;
} operand1_operandx_impl;

operand1_operandx_impl* operand1_operandx_impl_constructor (operand1_operandx_impl* mepl);  // Default constructor
void operand1_operandx_impl_destructor (operand1_operandx_impl* mepl);  // Best-practice destructor
/////////////////////////////////////////////////////////////////////////
// Override submachine-instance action and guard implementation methods
/////////////////////////////////////////////////////////////////////////

/**
 * operand2_operandx_impl SubMachine implementation overriding subclass.
 *
 * Override the action methods of the SubMachine individually if custom
 * behavior is desired.
 */
typedef struct operand2_operandx_impl {
    operandx_impl super;
} operand2_operandx_impl;

operand2_operandx_impl* operand2_operandx_impl_constructor (operand2_operandx_impl* mepl);  // Default constructor
void operand2_operandx_impl_destructor (operand2_operandx_impl* mepl);  // Best-practice destructor
/////////////////////////////////////////////////////////////////////////
// Override submachine-instance action and guard implementation methods
/////////////////////////////////////////////////////////////////////////
#endif  /* _CALCULATOR_IMPL_H */
