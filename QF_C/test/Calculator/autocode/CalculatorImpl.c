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
//       File: CalculatorImpl.c
// Created on: 02-Oct-2011 21:28:57
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C variant of Miro Samek's Quantum Framework.
//===========================================================================
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <log_event.h>
#include <qf_port.h>
#include <qassert.h>
#include <CalculatorImpl.h>
#include <user_event_impl.h>
#include <StatechartSignals.h>

CalculatorImpl* CalculatorImpl_Constructor (CalculatorImpl* mepl) {
    strcpy(mepl->machineName, "calculator");

    // for testing, not used for calculation
    AttributeMapper_init(mepl);
    CalculatorImpl_set_isKeyId(mepl, 0);
    CalculatorImpl_set_onError(mepl, 0);

    CalculatorImpl_clearAll(mepl);

    return mepl;
}

void CalculatorImpl_destructor (CalculatorImpl* mepl) {
    AttributeMapper_clean(mepl);
}

void CalculatorImpl_set_qactive (CalculatorImpl* mepl, QActive* active) {
    mepl->active = active;
}


////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////

void CalculatorImpl_set_isKeyId (CalculatorImpl* mepl, bool flag) {
    AttributeMapper_set(mepl, "isKeyId", flag);
}

void CalculatorImpl_set_onError (CalculatorImpl* mepl, bool flag) {
    AttributeMapper_set(mepl, "onError", flag);
}

bool CalculatorImpl_isKeyId (CalculatorImpl* mepl, QEvent const* e, const char arg1) {
    KeyEvent const* ke = (KeyEvent const*)e;
    printf("*** event keyId == '%c' and query is '%c'\n", ke->keyId, arg1);
    bool rv = (ke->keyId == arg1);
    printf("%s.isKeyId() == %s\n", mepl->machineName, AttributeMapper_booltostr(rv));
    return rv;
}

bool CalculatorImpl_onError (CalculatorImpl* mepl) {
    if (AttributeMapper_get(mepl, "TESTING")) {  // for unit-testing
        bool rv = AttributeMapper_get(mepl, "onError");
        printf("%s.onError() == %s\n", mepl->machineName, AttributeMapper_booltostr(rv));
        return rv;
    } else {
        bool rv = (strlen(mepl->errStat) > 0);
        if (rv) {
            printf("%s.onError() == %s\n", mepl->machineName, mepl->errStat);
            ResultEvent* re = Q_NEW(ResultEvent, REPORTRESULT_SIG);
            strcpy(re->result, mepl->errStat);
            QF_publish((QEvent*)re);
        }
        return rv;
    }
}

/**
 * Unsets the last operand.
 */
void CalculatorImpl_ce (CalculatorImpl* mepl) {
    strcpy(mepl->operand, "");
}

/**
 * Resets the computation data.
 */
void CalculatorImpl_clearAll (CalculatorImpl* mepl) {
    strcpy(mepl->operand, "");
    strcpy(mepl->resultStr, "");
    mepl->oper = ' ';
    mepl->lOperand = 0.0;
    mepl->rOperand = 0.0;
    mepl->result = 0.0;
    strcpy(mepl->errStat, "");
}

/**
 * Saves the negative sign.
 */
void CalculatorImpl_negate (CalculatorImpl* mepl) {
    strcpy(mepl->operand, "-");
}

/**
 * Updates the operand string.
 */
void CalculatorImpl_updateOperand (CalculatorImpl* mepl, QEvent const* e) {
    if (!AttributeMapper_get(mepl, "TESTING")) {
        KeyEvent const* ke = (KeyEvent const*)e;
        sprintf(mepl->operand, "%s%c", mepl->operand, ke->keyId);
        printf(">>> operand: \"%s\"\n", mepl->operand);
    }
}

/**
 * By this point, the operand should be ready for use.
 * If it's empty, use the result, else it's an error!
 * If an operand is available, save as left-operand and
 * clear the operand for the next entry.
 */
void CalculatorImpl_op (CalculatorImpl* mepl, QEvent const* e) {
    if (!AttributeMapper_get(mepl, "TESTING")) {
        KeyEvent const* ke = (KeyEvent const*)e;
        mepl->oper = ke->keyId;
        if (strlen(mepl->operand) == 0) {
            if (strlen(mepl->resultStr) == 0) {
                sprintf(mepl->errStat, "ERR: no operand to compute with for %c!", mepl->oper);
            } else {  // use result as left-operand
                mepl->lOperand = mepl->result;
            }
        } else {
            sscanf(mepl->operand, "%lf", &mepl->lOperand);
            strcpy(mepl->operand, "");  // clear the operand input buffer
            printf("%s.op() resulted in expression '%lf%c'\n", mepl->machineName, mepl->lOperand, mepl->oper);
        }
    }
}

/**
 * Computes the expression and store result, also store as operand.
 * If compute was called due to an operand, then we need to update
 * the expression as if op() was invoked.
 */
void CalculatorImpl_compute (CalculatorImpl* mepl, QEvent const* e) {
    if (AttributeMapper_get(mepl, "TESTING")) {
        return;
    }

    // save second operand, then compute
    sscanf(mepl->operand, "%lf", &mepl->rOperand);
    bool ok = 1;
    switch (mepl->oper) {
    case '/':  // special handling of division
        if (mepl->rOperand == 0) {
            sprintf(mepl->errStat, "ERR! ZeroDivisionError: %lf / %lf", mepl->lOperand, mepl->rOperand);
            printf("ERROR computing result! %s\n", mepl->errStat);
            ok = 0;
        } else {
            mepl->result = mepl->lOperand / mepl->rOperand;
        }
        break;
    case '*':
        mepl->result = mepl->lOperand * mepl->rOperand;
        break;
    case '+':
        mepl->result = mepl->lOperand + mepl->rOperand;
        break;
    case '-':
        mepl->result = mepl->lOperand - mepl->rOperand;
        break;
    default:  // don't know what to compute, complain
        sprintf(mepl->errStat, "ERR! Unknown operand: %c", mepl->oper);
        printf("ERROR computing result! %s\n", mepl->errStat);
        ok = 0;
        break;
    }

    if (ok) {
        // print result
        sprintf(mepl->resultStr, "%lf", mepl->result);
        printf("%s.compute() result: %s\n", mepl->machineName, mepl->resultStr);
        strcpy(mepl->errStat, "");
    }

    // clear operands and expr
    strcpy(mepl->operand, "");
    mepl->oper = ' ';
    mepl->lOperand = 0.0;
    mepl->rOperand = 0.0;
    if (((KeyEvent const*)e)->keyId != '=') {  // use result in next operation
        CalculatorImpl_op(mepl, e);
    }
}

void CalculatorImpl_reportResult (CalculatorImpl* mepl) {
    ResultEvent* re = Q_NEW(ResultEvent, REPORTRESULT_SIG);
    sprintf(re->result, "%lf", mepl->result);
    QF_publish((QEvent*)re);
}
