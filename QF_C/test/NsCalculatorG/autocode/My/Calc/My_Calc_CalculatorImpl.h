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
//       File: My/Calc/My_Calc_CalculatorImpl.h
// Created on: 16-Aug-2011 00:25:02
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
#include <My/Calc/My_Calc_OperandXImpl.h>


typedef struct My_Calc_CalculatorImpl {
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
} My_Calc_CalculatorImpl;

My_Calc_CalculatorImpl* My_Calc_CalculatorImpl_Constructor (My_Calc_CalculatorImpl* mepl);  // Default constructor
void My_Calc_CalculatorImpl_destructor (My_Calc_CalculatorImpl* mepl);  // Best-practice destructor
void My_Calc_CalculatorImpl_set_qactive (My_Calc_CalculatorImpl* mepl, QActive* active);
////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////
void My_Calc_CalculatorImpl_set_isKeyId (My_Calc_CalculatorImpl* mepl, bool flag);
void My_Calc_CalculatorImpl_set_onError (My_Calc_CalculatorImpl* mepl, bool flag);
bool My_Calc_CalculatorImpl_isKeyId (My_Calc_CalculatorImpl* mepl, QEvent const* e, char arg2);
bool My_Calc_CalculatorImpl_onError (My_Calc_CalculatorImpl* mepl);
void My_Calc_CalculatorImpl_ce (My_Calc_CalculatorImpl* mepl);
void My_Calc_CalculatorImpl_clearAll (My_Calc_CalculatorImpl* mepl);
void My_Calc_CalculatorImpl_negate (My_Calc_CalculatorImpl* mepl);
void My_Calc_CalculatorImpl_updateOperand (My_Calc_CalculatorImpl* mepl, QEvent const* e);
void My_Calc_CalculatorImpl_op (My_Calc_CalculatorImpl* mepl, QEvent const* e);
void My_Calc_CalculatorImpl_compute (My_Calc_CalculatorImpl* mepl, QEvent const* e);
void My_Calc_CalculatorImpl_reportResult (My_Calc_CalculatorImpl* mepl);



/**
 * My_Calc_operand1_operandx_impl SubMachine implementation overriding subclass.
 *
 * Override the action methods of the SubMachine individually if custom
 * behavior is desired.
 */
typedef struct My_Calc_operand1_operandx_impl {
    My_Calc_OperandXImpl super;
} My_Calc_operand1_operandx_impl;

My_Calc_operand1_operandx_impl* My_Calc_operand1_operandx_impl_constructor (My_Calc_operand1_operandx_impl* mepl);  // Default constructor
void My_Calc_operand1_operandx_impl_destructor (My_Calc_operand1_operandx_impl* mepl);  // Best-practice destructor

/**
 * My_Calc_operand2_operandx_impl SubMachine implementation overriding subclass.
 *
 * Override the action methods of the SubMachine individually if custom
 * behavior is desired.
 */
typedef struct My_Calc_operand2_operandx_impl {
    My_Calc_OperandXImpl super;
} My_Calc_operand2_operandx_impl;

My_Calc_operand2_operandx_impl* My_Calc_operand2_operandx_impl_constructor (My_Calc_operand2_operandx_impl* mepl);  // Default constructor
void My_Calc_operand2_operandx_impl_destructor (My_Calc_operand2_operandx_impl* mepl);  // Best-practice destructor

#endif  /* MY_CALC_CALCULATORIMPL_H */
