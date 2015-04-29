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
//       File: Test/test_testcalculations_impl.c
// Created on: 17-Aug-2011 13:30:38
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C variant of Miro Samek's Quantum Framework.
//===========================================================================
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <log_event.h>
#include <qf_port.h>
#include <qassert.h>
#include <My/Calc/my_calc_calculator.h>
#include <My/user_event_impl.h>
#include <My/UI/my_ui_statechart_signals.h>
#include <Test/test_testcalculations_impl.h>
#include <Test/test_statechart_signals.h>

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
const int PRIO_CALC = 3;  // hardwired prioity level for Calc
const int IDX_EXPR = 0;
const int IDX_RESULT = 1;

test_testcalculations_impl* test_testcalculations_impl_constructor (test_testcalculations_impl* mepl) {
    strcpy(mepl->machineName, "TestCalculations");

    AttributeMapper_init(mepl);

    return mepl;
}

void test_testcalculations_impl_destructor (test_testcalculations_impl* mepl) {
    AttributeMapper_clean(mepl);
}

void test_testcalculations_impl_set_qactive (test_testcalculations_impl* mepl, QActive* active) {
}


////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////

bool test_testcalculations_impl_isInOperand2 (test_testcalculations_impl* mepl) {
    printf("in Operand2? '%d'\n", QHsm_isIn((QHsm*)(mepl->calcActive), (QStateHandler)&my_calc_calculator_operand2));
    return QHsm_isIn((QHsm*)(mepl->calcActive), (QStateHandler)&my_calc_calculator_operand2);
}

bool test_testcalculations_impl_isCalcDone (test_testcalculations_impl* mepl) {
    return mepl->charIdx >= strlen(mepl->expr);
}

void test_testcalculations_impl_initTest (test_testcalculations_impl* mepl) {
    mepl->calcActive = QF_active_[PRIO_CALC];
    mepl->idx = -1;  // reset index to right before the 1st element
    mepl->errCnt = 0;
}

void test_testcalculations_impl_stageNextCalculation (test_testcalculations_impl* mepl) {
    // grab next calculation
    mepl->idx++;
    if (mepl->idx < CALC_TEST_SIZE) {
        strcpy(mepl->expr, CALC_TESTS[mepl->idx][IDX_EXPR]);
        strcat(mepl->expr, "=");  // append an equal operator
        // reset the index count into the expression
        mepl->charIdx = 0;
    } else {
        QF_publish(Q_NEW(QEvent, TEST_TEST_LIST_DONE_SIG));
    }
}

void test_testcalculations_impl_sendNextChar (test_testcalculations_impl* mepl) {
    if (mepl->charIdx >= strlen(mepl->expr)) {
        return;  // no more char to send
    }
    // send the next character and increment counter
    char c = mepl->expr[mepl->charIdx++];
    switch (c) {
    case '+':
        QF_publish((QEvent*)Q_NEW(KeyEvent, MY_UI_B_OP_PLUS_SIG));
        break;
    case '-':
        QF_publish((QEvent*)Q_NEW(KeyEvent, MY_UI_B_OP_MINUS_SIG));
        break;
    case '*':
        QF_publish((QEvent*)Q_NEW(KeyEvent, MY_UI_B_OP_TIMES_SIG));
        break;
    case '/':
        QF_publish((QEvent*)Q_NEW(KeyEvent, MY_UI_B_OP_DIVIDE_SIG));
        break;
    case '=':
        QF_publish((QEvent*)Q_NEW(KeyEvent, MY_UI_B_EQUAL_SIG));
        break;
    case '.':
        QF_publish((QEvent*)Q_NEW(KeyEvent, MY_UI_B_DOT_SIG));
        break;
    case '0':
        QF_publish((QEvent*)Q_NEW(KeyEvent, MY_UI_B_0_SIG));
        break;
    case '1':
        QF_publish((QEvent*)Q_NEW(KeyEvent, MY_UI_B_1_SIG));
        break;
    case '2':
        QF_publish((QEvent*)Q_NEW(KeyEvent, MY_UI_B_2_SIG));
        break;
    case '3':
        QF_publish((QEvent*)Q_NEW(KeyEvent, MY_UI_B_3_SIG));
        break;
    case '4':
        QF_publish((QEvent*)Q_NEW(KeyEvent, MY_UI_B_4_SIG));
        break;
    case '5':
        QF_publish((QEvent*)Q_NEW(KeyEvent, MY_UI_B_5_SIG));
        break;
    case '6':
        QF_publish((QEvent*)Q_NEW(KeyEvent, MY_UI_B_6_SIG));
        break;
    case '7':
        QF_publish((QEvent*)Q_NEW(KeyEvent, MY_UI_B_7_SIG));
        break;
    case '8':
        QF_publish((QEvent*)Q_NEW(KeyEvent, MY_UI_B_8_SIG));
        break;
    case '9':
        QF_publish((QEvent*)Q_NEW(KeyEvent, MY_UI_B_9_SIG));
        break;
    default:
        printf("ERR! Unrecognized character: '%c'\n", c);
        break;
    }
}

void test_testcalculations_impl_checkOperand (test_testcalculations_impl* mepl) {
    QF_publish((QEvent*)Q_NEW(QEvent, TEST_TEST_OPERAND_CHK_SIG));
}

void test_testcalculations_impl_checkResult (test_testcalculations_impl* mepl, QEvent const* e) {
    ResultEvent const* re = (ResultEvent const*)e;
    char expStr[256];
    strcpy(expStr, CALC_TESTS[mepl->idx][IDX_RESULT]);
    if (strncmp(re->result, "ERR", 3) == 0) {  // an error output
        if (strlen(expStr) > 0 && strcmp(expStr, "ERR") == 0) {
            printf("+++ Computation error as expected: %s\n", re->result);
        } else {
            printf("xxx Computation result NOT err! %s\n", re->result);
            mepl->errCnt++;
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
            mepl->errCnt++;
        }
    }
}

void test_testcalculations_impl_cleanup (test_testcalculations_impl* mepl) {
    printf("Calculation error count == %d\n", mepl->errCnt);
}
