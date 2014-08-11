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
//       File: Test/TestCalculationsImpl.cpp
// Created on: 30-Jul-2011 10:20:32
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C++ variant of Miro Samek's Quantum Framework.
//===========================================================================
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <log_event.h>
#include <qf_port.h>
#include <qassert.h>
#include <My/UserEventImpl.h>
#include <Test/TestCalculationsImpl.h>
#include <StatechartSignals.h>
#include <StatechartSignals.h>

namespace Test {

/**
 * Default constructor.
 */
TestCalculationsImpl::TestCalculationsImpl () {
    strcpy(this->machineName, "TestCalculations");

    AttributeMapper::init(this);
}

/**
 * Required virtual destructor.
 */
TestCalculationsImpl::~TestCalculationsImpl () {
    AttributeMapper::clean(this);
}

void TestCalculationsImpl::setQActive (QActive* active) {
	// ignore, since initTest() will set calcActive
}


////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////

bool TestCalculationsImpl::isInOperand2 () {
	bool rv = (this->calcActive->getCurrentState() == My::Calc::CALCULATOR_ON_OPERAND2);
	printf("in Operand2? '%d'\n", rv);
	return rv;
}

bool TestCalculationsImpl::isCalcDone () {
	return this->charIdx >= strlen(this->expr);
}


void TestCalculationsImpl::initTest () {
	this->calcActive = static_cast<My::Calc::Calculator*>(QF::active_[TestCalculationsImpl::PRIO_CALC]);
	this->idx = -1;  // reset index to right before the 1st element
	this->errCnt = 0;
}

void TestCalculationsImpl::stageNextCalculation () {
	// grab next calculation
	this->idx++;
	if (this->idx < CALC_TEST_SIZE) {
		strcpy(this->expr, CALC_TESTS[this->idx][TestCalculationsImpl::IDX_EXPR]);
		strcat(this->expr, "=");  // append an equal operator
		// reset the index count into the expression
		this->charIdx = 0;
	} else {
		QF::publish(Q_NEW(QEvent, TEST_LIST_DONE));
	}
}

void TestCalculationsImpl::sendNextChar () {
	if (this->charIdx >= strlen(this->expr)) {
		return;  // no more char to send
	}
	// send the next character and increment counter
	char c = this->expr[this->charIdx++];
	switch (c) {
	case '+':
		QF::publish(Q_NEW(My::KeyEvent, B_OP_PLUS));
		break;
	case '-':
		QF::publish(Q_NEW(My::KeyEvent, B_OP_MINUS));
		break;
	case '*':
		QF::publish(Q_NEW(My::KeyEvent, B_OP_TIMES));
		break;
	case '/':
		QF::publish(Q_NEW(My::KeyEvent, B_OP_DIVIDE));
		break;
	case '=':
		QF::publish(Q_NEW(My::KeyEvent, B_EQUAL));
		break;
	case '.':
		QF::publish(Q_NEW(My::KeyEvent, B_DOT));
		break;
	case '0':
		QF::publish(Q_NEW(My::KeyEvent, B_0));
		break;
	case '1':
		QF::publish(Q_NEW(My::KeyEvent, B_1));
		break;
	case '2':
		QF::publish(Q_NEW(My::KeyEvent, B_2));
		break;
	case '3':
		QF::publish(Q_NEW(My::KeyEvent, B_3));
		break;
	case '4':
		QF::publish(Q_NEW(My::KeyEvent, B_4));
		break;
	case '5':
		QF::publish(Q_NEW(My::KeyEvent, B_5));
		break;
	case '6':
		QF::publish(Q_NEW(My::KeyEvent, B_6));
		break;
	case '7':
		QF::publish(Q_NEW(My::KeyEvent, B_7));
		break;
	case '8':
		QF::publish(Q_NEW(My::KeyEvent, B_8));
		break;
	case '9':
		QF::publish(Q_NEW(My::KeyEvent, B_9));
		break;
	default:
		printf("ERR! Unrecognized character: '%c'\n", c);
		break;
	}
}

void TestCalculationsImpl::checkOperand () {
	QF::publish(Q_NEW(QEvent, TEST_OPERAND_CHK));
}

void TestCalculationsImpl::checkResult (QEvent const* e) {
	My::ResultEvent const* re = static_cast<My::ResultEvent const*>(e);
	char expStr[256];
	strcpy(expStr, CALC_TESTS[this->idx][TestCalculationsImpl::IDX_RESULT]);
	if (strncmp(re->result, "ERR", 3) == 0) {  // an error output
		if (strlen(expStr) > 0 && strcmp(expStr, "ERR") == 0) {
			printf("+++ Computation error as expected: %s\n", re->result);
		} else {
			printf("xxx Computation result NOT err! %s\n", re->result);
			this->errCnt++;
		}
	} else {  // check numeric result
		double result, expResult;
		sscanf(re->result, "%lf", &result);
		sscanf(expStr, "%lf", &expResult);
		printf("Comparing expected %lf vs computed %lf...\n", expResult, result);
		if (fabs(result - expResult) < 0.0001) {
			printf("+++ Correct computation result: %s\n", re->result);
		} else {
			printf("xxx Wrong computation result, %s! Expecting %s\n", re->result, expStr);
			this->errCnt++;
		}
	}
}

void TestCalculationsImpl::cleanup () {
	printf("Calculation error count == %d\n", this->errCnt);
}

}
