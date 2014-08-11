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
//       File: My/Calc/my_calc_calculator_impl.h
// Created on: 17-Aug-2011 13:30:36
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C variant of Miro Samek's Quantum Framework.
//===========================================================================
#ifndef MY_CALC_CALCULATORIMPL_H
#define MY_CALC_CALCULATORIMPL_H

#include <qf_port.h>
#include <qassert.h>
/* Submachine impls */
#include <My/Calc/my_calc_operandx_impl.h>


typedef struct my_calc_calculator_impl {
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
} my_calc_calculator_impl;

my_calc_calculator_impl* my_calc_calculator_impl_constructor (my_calc_calculator_impl* mepl);  // Default constructor
void my_calc_calculator_impl_destructor (my_calc_calculator_impl* mepl);  // Best-practice destructor
void my_calc_calculator_impl_set_qactive (my_calc_calculator_impl* mepl, QActive* active);
////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////
void my_calc_calculator_impl_set_isKeyId (my_calc_calculator_impl* mepl, bool flag);
void my_calc_calculator_impl_set_onError (my_calc_calculator_impl* mepl, bool flag);
bool my_calc_calculator_impl_isKeyId (my_calc_calculator_impl* mepl, QEvent const* e, char arg2);
bool my_calc_calculator_impl_onError (my_calc_calculator_impl* mepl);
void my_calc_calculator_impl_ce (my_calc_calculator_impl* mepl);
void my_calc_calculator_impl_clearAll (my_calc_calculator_impl* mepl);
void my_calc_calculator_impl_negate (my_calc_calculator_impl* mepl);
void my_calc_calculator_impl_updateOperand (my_calc_calculator_impl* mepl, QEvent const* e);
void my_calc_calculator_impl_op (my_calc_calculator_impl* mepl, QEvent const* e);
void my_calc_calculator_impl_compute (my_calc_calculator_impl* mepl, QEvent const* e);
void my_calc_calculator_impl_reportResult (my_calc_calculator_impl* mepl);



/**
 * my_calc_operand1_operandx_impl SubMachine implementation overriding subclass.
 *
 * Override the action methods of the SubMachine individually if custom
 * behavior is desired.
 */
typedef struct my_calc_operand1_operandx_impl {
    my_calc_operandx_impl super;
} my_calc_operand1_operandx_impl;

my_calc_operand1_operandx_impl* my_calc_operand1_operandx_impl_constructor (my_calc_operand1_operandx_impl* mepl);  // Default constructor
void my_calc_operand1_operandx_impl_destructor (my_calc_operand1_operandx_impl* mepl);  // Best-practice destructor

/**
 * my_calc_operand2_operandx_impl SubMachine implementation overriding subclass.
 *
 * Override the action methods of the SubMachine individually if custom
 * behavior is desired.
 */
typedef struct my_calc_operand2_operandx_impl {
    my_calc_operandx_impl super;
} my_calc_operand2_operandx_impl;

my_calc_operand2_operandx_impl* my_calc_operand2_operandx_impl_constructor (my_calc_operand2_operandx_impl* mepl);  // Default constructor
void my_calc_operand2_operandx_impl_destructor (my_calc_operand2_operandx_impl* mepl);  // Best-practice destructor

#endif  /* MY_CALC_CALCULATORIMPL_H */
