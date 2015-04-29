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
//       File: My/Calc/CalculatorImpl.cpp
// Created on: 30-Jul-2011 10:20:42
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C++ variant of Miro Samek's Quantum Framework.
//===========================================================================
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <log_event.h>
#include <qf_port.h>
#include <qassert.h>
#include <My/UserEventImpl.h>
#include <My/Calc/CalculatorImpl.h>
#include <My/Calc/StatechartSignals.h>

namespace My {
    namespace Calc {

/**
 * Default constructor.
 */
CalculatorImpl::CalculatorImpl () {
    strcpy(this->machineName, "Calculator");

    // for testing, not used for calculation
    AttributeMapper::init(this);
    this->set_isKeyId(0);
    this->set_onError(0);

    this->clearAll();
}

/**
 * Required virtual destructor.
 */
CalculatorImpl::~CalculatorImpl () {
    AttributeMapper::clean(this);
}

void CalculatorImpl::setQActive (QActive* active) {
    m_active = active;
}


////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////

void CalculatorImpl::set_isKeyId (bool flag) {
    AttributeMapper::set(this, "isKeyId", flag);
}
void CalculatorImpl::set_onError (bool flag) {
    AttributeMapper::set(this, "onError", flag);
}

bool CalculatorImpl::isKeyId (QEvent const* e, char arg1) {
	My::KeyEvent const* ke = static_cast<My::KeyEvent const*>(e);
	printf("*** event keyId == '%c' and query is '%c'\n", ke->keyId, arg1);
	bool rv = (ke->keyId == arg1);
	printf("%s.isKeyId() == %s\n", this->machineName, AttributeMapper::booltostr(rv));
	return rv;
}

bool CalculatorImpl::onError () {
	if (AttributeMapper::get(this, "TESTING")) {  // for unit-testing
		bool rv = AttributeMapper::get(this, "onError");
		printf("%s.onError() == %s\n", this->machineName, AttributeMapper::booltostr(rv));
		return rv;
	} else {
		bool rv = (strlen(this->errStat) > 0);
		if (rv) {
			printf("%s.onError() == %s\n", this->machineName, this->errStat);
			My::ResultEvent* re = Q_NEW(My::ResultEvent, ReportResult);
			strcpy(re->result, this->errStat);
			QF::publish(re);
		}
		return rv;
	}
}

/**
 * Unsets the last operand.
 */
void CalculatorImpl::ce () {
	strcpy(this->operand, "");
}

/**
 * Resets the computation data.
 */
void CalculatorImpl::clearAll () {
	strcpy(this->operand, "");
	strcpy(this->resultStr, "");
	this->oper = ' ';
	this->lOperand = 0.0;
	this->rOperand = 0.0;
	this->result = 0.0;
	strcpy(this->errStat, "");
}

/**
 * Saves the negative sign.
 */
void CalculatorImpl::negate () {
	strcpy(this->operand, "-");
}

/**
 * Updates the operand string.
 */
void CalculatorImpl::updateOperand (QEvent const* e) {
	if (!AttributeMapper::get(this, "TESTING")) {
		My::KeyEvent const* ke = static_cast<My::KeyEvent const*>(e);
		// debug output...
/*
		printf("KeyEvent object od content:\n ");
		printf(" %04X", *((QSignal* )ke));
		printf(" %02X", *(((uint8_t* )ke)+2));
		for (int i=3; i < 6; i++) {
			printf(" %02X", ((uint8_t* )ke)[i]);
		}
		printf("\nOperand od content:\n ");
		for (int i=0; i < 32; i+=4) {
			printf(" %08X", ((uint32_t* )this->operand)[i]);
		}
		printf("\n  Key ID: '%X'\n", ke->keyId);
*/
		sprintf(this->operand, "%s%c", this->operand, ke->keyId);
		printf(">>> operand: \"%s\"\n", this->operand);
	}
}

/**
 * By this point, the operand should be ready for use.
 * If it's empty, use the result, else it's an error!
 * If an operand is available, save as left-operand and
 * clear the operand for the next entry.
 */
void CalculatorImpl::op (QEvent const* e) {
	if (!AttributeMapper::get(this, "TESTING")) {
		My::KeyEvent const* ke = static_cast<My::KeyEvent const*>(e);
		this->oper = ke->keyId;
		if (strlen(this->operand) == 0) {
			if (strlen(this->resultStr) == 0) {
				sprintf(this->errStat, "ERR: no operand to compute with for %c!", this->oper);
			} else {  // use result as left-operand
				this->lOperand = this->result;
			}
		} else {
			sscanf(this->operand, "%lf", &this->lOperand);
			strcpy(this->operand, "");  // clear the operand input buffer
			printf("%s.op() resulted in expression '%lf%c'\n", this->machineName, this->lOperand, this->oper);
		}
	}
//	printf("*** Operator stored is: %c\n", this->oper);
}

/**
 * Computes the expression and store result, also store as operand.
 * If compute was called due to an operand, then we need to update
 * the expression as if op() was invoked.
 */
void CalculatorImpl::compute (QEvent const* e) {
	if (AttributeMapper::get(this, "TESTING")) {
		return;
	}

	// save second operand, then compute
	sscanf(this->operand, "%lf", &this->rOperand);
	bool ok = 1;
	switch (this->oper) {
	case '/':  // special handling of division
		if (this->rOperand == 0) {
			sprintf(this->errStat, "ERR! ZeroDivisionError: %lf / %lf", this->lOperand, this->rOperand);
			printf("ERROR computing result! %s\n", this->errStat);
			ok = 0;
		} else {
			this->result = this->lOperand / this->rOperand;
		}
		break;
	case '*':
		this->result = this->lOperand * this->rOperand;
		break;
	case '+':
		this->result = this->lOperand + this->rOperand;
		break;
	case '-':
		this->result = this->lOperand - this->rOperand;
		break;
	default:  // don't know what to compute, complain
		sprintf(this->errStat, "ERR! Unknown operand: %c", this->oper);
		printf("ERROR computing result! %s\n", this->errStat);
		ok = 0;
		break;
	}

	if (ok) {
		// print result
		sprintf(this->resultStr, "%lf", this->result);
		printf("%s.compute() result: %s\n", this->machineName, this->resultStr);
		strcpy(this->errStat, "");
	}

	// clear operands and expr
	strcpy(this->operand, "");
	this->oper = ' ';
	this->lOperand = 0.0;
	this->rOperand = 0.0;
	if ((static_cast<My::KeyEvent const*>(e))->keyId != '=') {  // use result in next operation
		this->op(e);
	}
}

void CalculatorImpl::reportResult () {
	My::ResultEvent* re = Q_NEW(My::ResultEvent, ReportResult);
	sprintf(re->result, "%lf", this->result);
	QF::publish(re);
}


/////////////////////////////////////////////////////////////////////////
// Override submachine-instance action and guard implementation methods
/////////////////////////////////////////////////////////////////////////

/**
 * Default constructor.
 */
Operand1_OperandXImpl::Operand1_OperandXImpl () {
    strcpy(this->machineName, "Operand1:OperandX");

    AttributeMapper::init(this);
}

/**
 * Required virtual destructor.
 */
Operand1_OperandXImpl::~Operand1_OperandXImpl () {
    AttributeMapper::clean(this);
}

/**
 * Default constructor.
 */
Operand2_OperandXImpl::Operand2_OperandXImpl () {
    strcpy(this->machineName, "Operand2:OperandX");

    AttributeMapper::init(this);
}

/**
 * Required virtual destructor.
 */
Operand2_OperandXImpl::~Operand2_OperandXImpl () {
    AttributeMapper::clean(this);
}

    }
}

