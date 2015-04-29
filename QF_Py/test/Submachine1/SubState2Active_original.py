#!/usr/bin/env python -i
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
WARNING: This file was automatically generated - DO NOT HAND EDIT

File: SubState2Active.py

Automatically generated SubState2 state machine.
Date Created:  08-Dec-2009 11:50:10
Created By:    reder

Python implementation of the SubState2 Statechart model
as a Python Active object.
"""
# Python imports here
import sys
import logging

# QF imports here
from qf import miros
from qf import active
from qf import event
from qf import time_event
from qf import framework

# Module globals initialized here
LOGGER = logging.getLogger('SubState2Logger')
#- bail-region event
BAIL_REGION_EVENT = event.Event("#BAIL#")

def printf(format, *args):
    #sys.stdout.write(format % args)
    LOGGER.info(format % args)


class SubState2Active(active.Active):
    """
    SubState2 state machine active object.
    """
    def __init__(self, impl_object=None, window_name="substate2"):
        """
        Constructor
        """
        active.Active.__init__(self)
        # --------------------------------------------------------------------
        #               name                                      parent's
        #                of               event                   event
        #               state             handler                 handler
        # --------------------------------------------------------------------
        self.addState ( "top",            self.top,               None)
        self.addState ( "SubState2_S1",   self.SubState2_S1,      self.top)
        self.addState ( "SubState2_S2",   self.SubState2_S2,      self.top)
        self.addState ( "SubState2_S3",   self.SubState2_S3,      self.top)
        self.addState("final",                   self.final,                     self.top)

        # For GUI messages
        self.__machine_name = "SubState2"
        self.__window_name = window_name
        # For manually coded implementation
        # of all actions, guards, etc.
        self.__impl_obj = impl_object


    def initialize(self):
        """
        Override active object initialize with
        custom initialize routine.  Mostly used
        to subscribe signals for state machine.
        """
        # Subscribe to signals here.
        self._subscribe("Ev1")
        self._subscribe("Ev2")
        self._subscribe("Ev3")

        # Timer event objects created here.
        pass


    def sendUpdate(self, state_name, entry_exit):
        """
        Send gui entry or exit messages.
        """
        str = "%s %s%s %s" % (self.__window_name, self.__machine_name, state_name, entry_exit)
        #print str
        self._sendUpdate(str)


    def top(self):
        """
        The routine handles initial events and events that get bubbled 
        to the top of the state-machine.  Events that get bubbled to the top can
        either be ignored (return 0) or an error message can be generated.
        """
        if self.tEvt['sType'] == "init":
            self.stateStart(self.SubState2_S1)
            return 0
        else:
            return 0


    def SubState2_S1(self):
        """
        State SubState2_S1
        """
        machine = self.__machine_name
        state_name = "SubState2_S1"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        elif self.tEvt['sType'] == "Ev1":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.SubState2_S2)
            return 0
        elif self.tEvt['sType'] == BAIL_REGION_EVENT.signal:
            self.stateTran(self.final)
            return 0
        return self.tEvt['sType']


    def SubState2_S2(self):
        """
        State SubState2_S2
        """
        machine = self.__machine_name
        state_name = "SubState2_S2"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        elif self.tEvt['sType'] == "Ev2":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.SubState2_S3)
            return 0
        elif self.tEvt['sType'] == BAIL_REGION_EVENT.signal:
            self.stateTran(self.final)
            return 0
        return self.tEvt['sType']


    def SubState2_S3(self):
        """
        State SubState2_S3
        """
        machine = self.__machine_name
        state_name = "SubState2_S3"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        elif self.tEvt['sType'] == "Ev3":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.SubState2_S1)
            return 0
        elif self.tEvt['sType'] == BAIL_REGION_EVENT.signal:
            self.stateTran(self.final)
            return 0
        return self.tEvt['sType']


    def final(self):
        if self.tEvt['sType'] == "init":
            return 0
        else:
            return 0


def main():
    """
    Basic standalone test harness that all Python
    active object modules use for unit testing.
    """
    # Enable logger as info messages only.
    LOGGER.setLevel(logging.INFO)
    # Log to stdout only.
    logger_output_handler = logging.StreamHandler(sys.stdout)
    logger_output_handler.setLevel(logging.INFO)
    # Include only message in output.
    logger_formatter = logging.Formatter('%(message)s')
    logger_output_handler.setFormatter(logger_formatter)
    #
    LOGGER.addHandler(logger_output_handler)
    #
    # Instance the QF code for running main thread
    # and create active object
    qf = framework.QF.getInstance()
    #
    # Framework initiallization (GUI turned off)
    #
    qf.init(qf_gui=False)

    # Active object start up and register with QF
    substate2 = SubState2Active()
    # start active object register into qf
    substate2.startActive()
    # start/initialize HSM
    substate2.onStart(substate2.top)
    # start the active object thread
    substate2.start()
    #
    # Run event dispatch loop
    qf.run()


if __name__ == "__main__":
    main()
