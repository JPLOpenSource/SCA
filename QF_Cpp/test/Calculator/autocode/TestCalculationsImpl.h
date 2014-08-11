//-*- Mode: C++; -*-
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
// Created on: 30-Jul-2011 10:20:08
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C++ variant of Miro Samek's Quantum Framework.
//===========================================================================
#ifndef TestCalculationsImpl_h
#define TestCalculationsImpl_h

#include <qf_port.h>
#include <qassert.h>

/**
 * Set of test calculations to carry out.
 */
const int CALC_TEST_SIZE = 13;
const char CALC_TESTS[CALC_TEST_SIZE][2][256] = {
		{ "1+1", "2" },
		{ "0-0", "0" },
		{ "5*5", "25" },
		{ "4/2", "2" },
		{ "7+1*5-2", "38" },
		{ "1/0", "ERR" },
		{ "1+4*5/4+-.25", "6" },
		{ "-1.125*-8", "9" },
		{ "1+1=*9=+1/3-.333333333333", "6.0" },
		{ "1+2+3+4+5+6+7+8+9+10", "55" },
		{ "0-1-2-3-4-5-6-7-8-9-10", "-55" },
		{ "1*2*3*4*5*6*7*8*9*10", "3628800" },
		{ "+*-/-52+64=0--0+1.2--888+.2*-.0987/-4", "21.945945" }
};

class TestCalculationsImpl {

    friend class TestCalculationsImplSpy;

public:
    TestCalculationsImpl ();  // Default constructor
    virtual ~TestCalculationsImpl ();  // Required virtual destructor

    virtual void setQActive (QActive* active);

    ////////////////////////////////////////////
    // Action and guard implementation methods
    ////////////////////////////////////////////
    virtual bool isInOperand2 ();
    virtual bool isCalcDone ();

    virtual void initTest ();
    virtual void stageNextCalculation ();
    /**
     * Sends next character of expression string to the calc.
     */
    virtual void sendNextChar ();
    virtual void checkOperand ();
    virtual void checkResult (QEvent const* e);
    virtual void cleanup ();

    static const int PRIO_CALC = 3;  // hardwired prioity level for Calc
    static const int IDX_EXPR = 0;
    static const int IDX_RESULT = 1;

private:
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

};

#endif  /* TestCalculationsImpl_h */
