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
//       File: ztestuserevents_impl.c
// Created on: 02-Oct-2011 22:54:03
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
#include <usereventtest.h>
#include <ztestuserevents_impl.h>
#include <statechart_signals.h>

const int PRIO_UET = 2;  // hardwired prioity level for UserEventTest

ztestuserevents_impl* ztestuserevents_impl_constructor (ztestuserevents_impl* mepl) {
    strcpy(mepl->machineName, "ztestuserevents");
    return mepl;
}

void ztestuserevents_impl_destructor (ztestuserevents_impl* mepl) {
    AttributeMapper_clean(mepl);
}

void ztestuserevents_impl_set_qactive (ztestuserevents_impl* mepl, QActive* active) {
    mepl->active = active;
}


////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////

bool ztestuserevents_impl_isInS2 (ztestuserevents_impl* mepl) {
    bool rv = QHsm_isIn((QHsm*)(mepl->active), (QStateHandler)&usereventtest_s2);
    printf("%s.isInS2() == %s\n", mepl->machineName, AttributeMapper_booltostr(rv));
    return rv;
}

void ztestuserevents_impl_init (ztestuserevents_impl* mepl) {
    mepl->active = QF_active_[PRIO_UET];
}

void ztestuserevents_impl_sendEv1 (ztestuserevents_impl* mepl, const char* arg1) {
    DataEvent* de = Q_NEW(DataEvent, EV1_SIG);
    strncpy(de->data, arg1, MAX_DATA_SIZE);
    QF_publish((QEvent*)de);
}

void ztestuserevents_impl_sendEv2 (ztestuserevents_impl* mepl) {
    DataEvent* de = Q_NEW(DataEvent, EV2_SIG);
    // transfer stored user data to event to publish
    strncpy(de->data, mepl->userData, MAX_DATA_SIZE);
    QF_publish((QEvent*)de);
}

void ztestuserevents_impl_storeEventData (ztestuserevents_impl* mepl, QEvent const* e) {
    DataEvent const* de = (DataEvent const*)e;
    strncpy(mepl->userData, de->data, MAX_DATA_SIZE);
}

void ztestuserevents_impl_cleanup (ztestuserevents_impl* mepl) {
    printf("%s completed!\n", mepl->machineName);
}
