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
//       File: My/Calc/my_calc_operandx_impl.c
// Created on: 17-Aug-2011 13:30:35
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
#include <My/Calc/my_calc_operandx_impl.h>
#include <My/user_event_impl.h>
#include <My/Calc/my_calc_statechart_signals.h>

my_calc_operandx_impl* my_calc_operandx_impl_constructor (my_calc_operandx_impl* mepl) {
    strcpy(mepl->machineName, "OperandX");

    AttributeMapper_init(mepl);

    return mepl;
}

void my_calc_operandx_impl_destructor (my_calc_operandx_impl* mepl) {
    AttributeMapper_clean(mepl);
}

void my_calc_operandx_impl_set_qactive (my_calc_operandx_impl* mepl, QActive* active) {
    mepl->active = active;
}


////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////

void my_calc_operandx_impl_fraction (my_calc_operandx_impl* mepl) {
    KeyEvent* ke = Q_NEW(KeyEvent, MY_CALC_OPERANDCHANGED_SIG);
    ke->keyId = '.';
    QF_publish((QEvent*)ke);
}

void my_calc_operandx_impl_insert (my_calc_operandx_impl* mepl, QEvent const* e) {
    KeyEvent* ke = Q_NEW(KeyEvent, MY_CALC_OPERANDCHANGED_SIG);
    ke->keyId = ((KeyEvent const*)e)->keyId;
    QF_publish((QEvent*)ke);
}
