#!/usr/bin/env python -i
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
WARNING: This file was automatically generated - DO NOT HAND EDIT

File: SubMachine1Active.py

Automatically generated SubMachine1 state machine.
Date Created:  05-Dec-2009 23:06:41
Created By:    reder

Python implementation of the SubMachine1 Statechart model
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

# Submachine imports here
import SubState1Active_original as SubState1Active
import SubState2Active_original as SubState2Active

# Module globals initialized here
LOGGER = logging.getLogger('SubMachine1Logger')
#- bail-region event
BAIL_REGION_EVENT = event.Event("#BAIL#")


def printf(format, *args):
    #sys.stdout.write(format % args)
    LOGGER.info(format % args)


class SubMachine1Active(active.Active):
    """
    SubMachine1 state machine active object.
    """
    def __init__(self, impl_object=None, window_name="submachine1"):
        """
        Constructor
        """
        active.Active.__init__(self)
        # --------------------------------------------------------------------
        #               name                            parent's
        #                of          event              event
        #               state        handler            handler
        # --------------------------------------------------------------------
        self.addState ( "top",       self.top,          None)
        self.addState ( "S1",        self.S1,           self.Sub2)
        self.addState ( "S2",        self.S2,           self.Sub2)
        self.addState(  "S3",        self.S3,           self.Sub2)
        self.addState ( "Sub1",    self.Sub1,       self.top)
        self.addState ( "Sub2",    self.Sub2,       self.top)

        # For GUI messages
        self.__machine_name = "SubMachine1"
        self.__window_name = window_name
        # For manually coded implementation
        # of all actions, guards, etc.
        self.__impl_obj = impl_object

        # Orthogonal regions instanced here
        self.__Sub1 = SubState1Active.SubState1Active(self.__impl_obj, self.__window_name)
        self.__S2 = SubState1Active.SubState1Active(self.__impl_obj, self.__window_name)
        self.__S3 = SubState2Active.SubState2Active(self.__impl_obj, self.__window_name)


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
        self._subscribe("SubEv1")
        self._subscribe("SubEv2")
        self._subscribe("SubEv3")
        self._subscribe("SubEv4")
        self._subscribe("SubEv5")
        self._subscribe(BAIL_REGION_EVENT.signal)

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
            self.stateStart(self.Sub1)
            return 0
        else:
            return 0


    def S1(self):
        """
        State S1
        """
        machine = self.__machine_name
        state_name = "S1"
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
        elif self.tEvt['sType'] == "SubEv3":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S2)
            return 0
        return self.tEvt['sType']


    def S2(self):
        """
        State S2
        """
        machine = self.__machine_name
        state_name = "S2"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            self.__S2.onStart(self.__S2.top)
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            self.__S2.dispatch(BAIL_REGION_EVENT)
            return 0
        elif self.tEvt['sType'] == "SubEv4":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S3)
            return 0
        elif self.tEvt['sType'] == "Ev1":
            printf("%s %s SubState1 submachine %s", machine, state_name, self.tEvt['sType'])
            self.__S2.dispatch(self.tEvt)
            return 0
        elif self.tEvt['sType'] == "Ev2":
            printf("%s %s SubState1 submachine %s", machine, state_name, self.tEvt['sType'])
            self.__S2.dispatch(self.tEvt)
            return 0
        return self.tEvt['sType']


    def S3(self):
        """
        State S3
        """
        machine = self.__machine_name
        state_name = "S3"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            self.__S3.onStart(self.__S3.top)
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            self.__S3.dispatch(BAIL_REGION_EVENT)
            return 0
        elif self.tEvt['sType'] == "SubEv5":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S1)
            return 0
        elif self.tEvt['sType'] == "Ev1":
            printf("%s %s SubState2 submachine %s", machine, state_name, self.tEvt['sType'])
            self.__S3.dispatch(self.tEvt)
            return 0
        elif self.tEvt['sType'] == "Ev2":
            printf("%s %s SubState2 submachine %s", machine, state_name, self.tEvt['sType'])
            self.__S3.dispatch(self.tEvt)
            return 0
        elif self.tEvt['sType'] == "Ev3":
            printf("%s %s SubState2 submachine %s", machine, state_name, self.tEvt['sType'])
            self.__S3.dispatch(self.tEvt)
            return 0
        return self.tEvt['sType']


    def Sub1(self):
        """
        State Sub1
        """
        machine = self.__machine_name
        state_name = "Sub1"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            self.__Sub1.onStart(self.__Sub1.top)
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            self.__Sub1.dispatch(BAIL_REGION_EVENT)
            return 0
        elif self.tEvt['sType'] == "SubEv1":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.Sub2)
            return 0
        elif self.tEvt['sType'] == "Ev1":
            printf("%s %s SubState1 submachine %s", machine, state_name, self.tEvt['sType'])
            self.__Sub1.dispatch(self.tEvt)
            return 0
        elif self.tEvt['sType'] == "Ev2":
            printf("%s %s SubState1 submachine %s", machine, state_name, self.tEvt['sType'])
            self.__Sub1.dispatch(self.tEvt)
            return 0
        return self.tEvt['sType']


    def Sub2(self):
        """
        State Sub2
        """
        machine = self.__machine_name
        state_name = "Sub2"
        if self.tEvt['sType'] == "init":
            self.stateStart(self.S1)
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        elif self.tEvt['sType'] == "SubEv2":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.Sub1)
            return 0
        return self.tEvt['sType']


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
    # Add logger output for submachines.
    SubState1Active.LOGGER.setLevel(logging.INFO)
    SubState2Active.LOGGER.setLevel(logging.INFO)
    SubState1Active.LOGGER.addHandler(logger_output_handler)
    SubState2Active.LOGGER.addHandler(logger_output_handler)
    #
    # Instance the QF code for running main thread
    # and create active object
    qf = framework.QF.getInstance()
    #
    # Framework initiallization (GUI turned off)
    #
    qf.init(qf_gui=False)

    # Active object start up and register with QF
    submachine1 = SubMachine1Active()
    # start active object register into qf
    submachine1.startActive()
    # start/initialize HSM
    submachine1.onStart(submachine1.top)
    # start the active object thread
    submachine1.start()
    #
    # Run event dispatch loop
    qf.run()


if __name__ == "__main__":
    main()
