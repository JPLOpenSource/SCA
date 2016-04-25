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
//       File: Test/Test_TestCalculationsImpl.h
// Created on: 17-Aug-2011 13:30:38
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C variant of Miro Samek's Quantum Framework.
//===========================================================================
#ifndef TEST_TESTCALCULATIONSIMPL_H
#define TEST_TESTCALCULATIONSIMPL_H

#include <qf_port.h>
#include <qassert.h>

/**
 * Set of test calculations to carry out.
 */
#define CALC_TEST_SIZE 13

typedef struct Test_TestCalculationsImpl {
    char machineName[256];
    // QActive object of Calculator
    QActive* calcActive;
    // index of calculation to send
    uint16_t idx;
    // cache of expression to send
    char expr[256];
    // index of character within calculation to send
    uint16_t charIdx;
    // count of erroneous calculations
    uint32_t errCnt;
} Test_TestCalculationsImpl;

Test_TestCalculationsImpl* Test_TestCalculationsImpl_Constructor (Test_TestCalculationsImpl* mepl);  // Default constructor
void Test_TestCalculationsImpl_destructor (Test_TestCalculationsImpl* mepl);  // Best-practice destructor
void Test_TestCalculationsImpl_set_qactive (Test_TestCalculationsImpl* mepl, QActive* active);
////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////
bool Test_TestCalculationsImpl_isInOperand2 (Test_TestCalculationsImpl* mepl);
bool Test_TestCalculationsImpl_isCalcDone (Test_TestCalculationsImpl* mepl);

void Test_TestCalculationsImpl_initTest (Test_TestCalculationsImpl* mepl);
void Test_TestCalculationsImpl_stageNextCalculation (Test_TestCalculationsImpl* mepl);
/**
 * Sends next character of expression string to the calc.
 */
void Test_TestCalculationsImpl_sendNextChar (Test_TestCalculationsImpl* mepl);
void Test_TestCalculationsImpl_checkOperand (Test_TestCalculationsImpl* mepl);
void Test_TestCalculationsImpl_checkResult (Test_TestCalculationsImpl* mepl, QEvent const* e);
void Test_TestCalculationsImpl_cleanup (Test_TestCalculationsImpl* mepl);

#endif  /* TEST_TESTCALCULATIONSIMPL_H */
