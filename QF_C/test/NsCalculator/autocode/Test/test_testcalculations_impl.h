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
//       File: Test/test_testcalculations_impl.h
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

typedef struct test_testcalculations_impl {
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
} test_testcalculations_impl;

test_testcalculations_impl* test_testcalculations_impl_constructor (test_testcalculations_impl* mepl);  // Default constructor
void test_testcalculations_impl_destructor (test_testcalculations_impl* mepl);  // Best-practice destructor
void test_testcalculations_impl_set_qactive (test_testcalculations_impl* mepl, QActive* active);
////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////
bool test_testcalculations_impl_isInOperand2 (test_testcalculations_impl* mepl);
bool test_testcalculations_impl_isCalcDone (test_testcalculations_impl* mepl);

void test_testcalculations_impl_initTest (test_testcalculations_impl* mepl);
void test_testcalculations_impl_stageNextCalculation (test_testcalculations_impl* mepl);
/**
 * Sends next character of expression string to the calc.
 */
void test_testcalculations_impl_sendNextChar (test_testcalculations_impl* mepl);
void test_testcalculations_impl_checkOperand (test_testcalculations_impl* mepl);
void test_testcalculations_impl_checkResult (test_testcalculations_impl* mepl, QEvent const* e);
void test_testcalculations_impl_cleanup (test_testcalculations_impl* mepl);

#endif  /* TEST_TESTCALCULATIONSIMPL_H */
