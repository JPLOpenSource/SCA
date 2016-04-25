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
//       File: UserEventTestImpl.c
// Created on: 02-Oct-2011 22:47:03
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
#include <user_event_impl.h>
#include <UserEventTestImpl.h>
#include <StatechartSignals.h>

UserEventTestImpl* UserEventTestImpl_Constructor (UserEventTestImpl* mepl) {
    strcpy(mepl->machineName, "usereventtest");
    strcpy(mepl->var, "None");

    AttributeMapper_init(mepl);

    return mepl;
}

void UserEventTestImpl_destructor (UserEventTestImpl* mepl) {
    AttributeMapper_clean(mepl);
}

void UserEventTestImpl_set_qactive (UserEventTestImpl* mepl, QActive* active) {
    mepl->active = active;
}


////////////////////////////////////////////
// Action and guard implementation methods
////////////////////////////////////////////

bool UserEventTestImpl_isValue (UserEventTestImpl* mepl, const char* arg1, double arg2) {
    // parse string val as double
    double varVal;
    sscanf(arg1, "%lf", &varVal);
    bool rv = (varVal == arg2);
    printf("%s.isValue() == %s\n", mepl->machineName, AttributeMapper_booltostr(rv));
    return rv;
}

void UserEventTestImpl_noUserEventOnInit (UserEventTestImpl* mepl, const char* arg1, const char* arg2) {
    printf("%s.noUserEventOnInit() 'var' originally '%s', setting to '%s'\n",
            mepl->machineName, arg1, arg2);
    strcpy(mepl->var, arg2);
}

void UserEventTestImpl_noUserEvent (UserEventTestImpl* mepl, const char* arg1, QEvent const* e) {
    printf("%s.noUserEvent() received var value '%s' and event signal '%hd'\n",
            mepl->machineName, arg1, e->sig);

    if (AttributeMapper_get(mepl, "FUNCTIONALITY")) {
        // show that there is no data!
        DataEvent const* de = (DataEvent const*)e;
        printf("%s.noUserEvent() no access to user event on entry/exit: '%hX'\n",
                mepl->machineName, ((uint8_t*) de)[3]);
    }
}

void UserEventTestImpl_userEventOnTrans (UserEventTestImpl* mepl, QEvent const* e, const char* arg2, double arg3) {
    printf("%s.userEventOnTrans() received event signal '%hd', var value '%s', and literal '%lf'\n",
            mepl->machineName, e->sig, arg2, arg3);

    if (AttributeMapper_get(mepl, "FUNCTIONALITY")) {
        // transfer e data to var, and save literal as new e data
        DataEvent const* de = (DataEvent const*)e;
        strncpy(mepl->var, de->data, MAX_DATA_SIZE);
        // publish new data in e as test Ack event
        DataEvent* newDE = Q_NEW(DataEvent, TEST_ACK_EV1_SIG);
        sprintf(newDE->data, "%lf", arg3);
        QF_publish((QEvent*)newDE);
    } else {
        // set var to literal
        sprintf(mepl->var, "%lf", arg3);
    }
}

void UserEventTestImpl_userEventAvailable (UserEventTestImpl* mepl, const char* arg1, QEvent const* e, const char* arg3) {
    printf("%s.userEventAvailable() received string '%s', event signal '%d', and var value '%s'\n",
            mepl->machineName, arg1, e->sig, arg3);

    if (AttributeMapper_get(mepl, "FUNCTIONALITY")) {
        // transfer e data back to var
        DataEvent const* de = (DataEvent const*)e;
        strncpy(mepl->var, de->data, MAX_DATA_SIZE);
    }
}
