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
//       File: TestCalculationsImpl.h
// Created on: 02-Oct-2011 22:31:32
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C variant of Miro Samek's Quantum Framework.
//===========================================================================
#ifndef _TESTCALCULATIONS_IMPL_H
#define _TESTCALCULATIONS_IMPL_H

#include <qf_port.h>
#include <qassert.h>

/**
 * Set of test calculations to carry out.
 */
#define CALC_TEST_SIZE 13

typedef struct TestCalculationsImpl {
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
} TestCalculationsImpl;

TestCalculationsImpl* TestCalculationsImpl_Constructor (TestCalculationsImpl* mepl);  // Default constructor
void TestCalculationsImpl_destructor (TestCalculationsImpl* mepl);  // Best-practice destructor
void TestCalculationsImpl_set_qactive (TestCalculationsImpl* mepl, QActive* active);
////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////
bool TestCalculationsImpl_isInOperand2 (TestCalculationsImpl* mepl);
bool TestCalculationsImpl_isCalcDone (TestCalculationsImpl* mepl);

void TestCalculationsImpl_initTest (TestCalculationsImpl* mepl);
void TestCalculationsImpl_stageNextCalculation (TestCalculationsImpl* mepl);
/**
 * Sends next character of expression string to the calc.
 */
void TestCalculationsImpl_sendNextChar (TestCalculationsImpl* mepl);
void TestCalculationsImpl_checkOperand (TestCalculationsImpl* mepl);
void TestCalculationsImpl_checkResult (TestCalculationsImpl* mepl, QEvent const* e);
void TestCalculationsImpl_cleanup (TestCalculationsImpl* mepl);

#endif  /* _TESTCALCULATIONS_IMPL_H */
