#!/usr/bin/env python -i
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
WARNING: This file was automatically generated - DO NOT HAND EDIT

File: Composite5Active.py

Automatically generated Composite5 state machine.
Date Created:  10 Sept. 2009
Created By:    reder

Python implementation of the Composite5 Statechart model
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
LOGGER = logging.getLogger('Composite5Logger')


def printf(format, *args):
    #sys.stdout.write(format % args)
    LOGGER.info(format % args)


class Composite5Active(active.Active):
    """
    Composite5 state machine active object.
    """
    def __init__(self, impl_object=None, window_name="composite5"):
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
        self.addState ( "S1",        self.S1,           self.top)
        self.addState ( "S2",        self.S2,           self.top)
        self.addState ( "S21",       self.S21,          self.S2)
        self.addState ( "S22",       self.S22,          self.S2)

        # For GUI messages
        self.__machine_name = "Composite5"
        self.__window_name = window_name
        # For manually coded implementation
        # of all actions, guards, etc.
        self.__impl_obj = impl_object
        # Timer event object instances
        self.s1_timeout = None
        self.s2_timeout = None
        self.s21_timeout = None
        self.s22_timeout = None


    def initialize(self):
        """
        Override active object initialize with
        custom initialize routine.  Mostly used
        to subscribe signals for state machine.
        """
        # Subscribe to signals here.
        self._subscribe("Ev1")

        # Timer event objects created here.
        self.s1_timeout = time_event.TimeEvt(self, "S1TimerEv")
        self.s2_timeout = time_event.TimeEvt(self, "S2TimerEv")
        self.s21_timeout = time_event.TimeEvt(self, "S21TimerEv")
        self.s22_timeout = time_event.TimeEvt(self, "S22TimerEv")


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
            self.stateStart(self.S1)
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
            self.s1_timeout.postIn(1)
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            self.s1_timeout.disarm()
            return 0
        elif self.tEvt['sType'] == "S1TimerEv":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.S2)
            return 0
        elif self.tEvt['sType'] == "Ev1":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
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
            self.stateStart(self.S21)
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            self.s2_timeout.postIn(5)
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            self.s2_timeout.disarm()
            return 0
        elif self.tEvt['sType'] == "S2TimerEv":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.S1)
            return 0
        elif self.tEvt['sType'] == "Ev1":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.S1)
            return 0
        return self.tEvt['sType']


    def S21(self):
        """
        State S21
        """
        machine = self.__machine_name
        state_name = "S21"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            self.s21_timeout.postIn(3)
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            self.s21_timeout.disarm()
            return 0
        elif self.tEvt['sType'] == "S21TimerEv":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.S22)
            return 0
        return self.tEvt['sType']


    def S22(self):
        """
        State S22
        """
        machine = self.__machine_name
        state_name = "S22"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            self.s22_timeout.postIn(1)
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            self.s22_timeout.disarm()
            return 0
        elif self.tEvt['sType'] == "S22TimerEv":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.S21)
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
    # Instance the QF code for running main thread
    # and create active object
    qf = framework.QF.getInstance()
    #
    # Framework initiallization (GUI turned off)
    #
    qf.init(qf_gui=False)

    # Active object start up and register with QF
    composite5 = Composite5Active()
    # start active object register into qf
    composite5.startActive()
    # start/initialize HSM
    composite5.onStart(composite5.top)
    # start the active object thread
    composite5.start()
    #
    # Run event dispatch loop
    qf.run()


if __name__ == "__main__":
    main()
