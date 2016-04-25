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
//       File: My/Calc/My_Calc_OperandXImpl.c
// Created on: 17-Aug-2011 09:02:23
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
#include <My/Calc/My_Calc_OperandXImpl.h>
#include <My/user_event_impl.h>
#include <StatechartSignals.h>

My_Calc_OperandXImpl* My_Calc_OperandXImpl_Constructor (My_Calc_OperandXImpl* mepl) {
    strcpy(mepl->machineName, "OperandX");

    AttributeMapper_init(mepl);

    return mepl;
}

void My_Calc_OperandXImpl_destructor (My_Calc_OperandXImpl* mepl) {
    AttributeMapper_clean(mepl);
}

void My_Calc_OperandXImpl_set_qactive (My_Calc_OperandXImpl* mepl, QActive* active) {
    mepl->active = active;
}


////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////

void My_Calc_OperandXImpl_fraction (My_Calc_OperandXImpl* mepl) {
    KeyEvent* ke = Q_NEW(KeyEvent, MY_CALC_OPERANDCHANGED_SIG);
    ke->keyId = '.';
    QF_publish((QEvent*)ke);
}

void My_Calc_OperandXImpl_insert (My_Calc_OperandXImpl* mepl, QEvent const* e) {
    KeyEvent* ke = Q_NEW(KeyEvent, MY_CALC_OPERANDCHANGED_SIG);
    ke->keyId = ((KeyEvent const*)e)->keyId;
    QF_publish((QEvent*)ke);
}
