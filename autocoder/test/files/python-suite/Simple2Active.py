#!/usr/bin/env python -i
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
WARNING: This file was automatically generated - DO NOT HAND EDIT

File: Simple2Active.py

Automatically generated Simple2 state machine.
Date Created:  29-Sep-2009 16:54:44
Created By:    reder

Python implementation of the Simple2 Statechart model
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
LOGGER = logging.getLogger('Simple2Logger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class Simple2Active(active.Active):
    """
    Simple2 state machine active object.
    """
    def __init__(self, impl_object=None):
        """
        Constructor
        """
        active.Active.__init__(self)
        # --------------------------------------------------------------------
        #               name                            parent's
        #                of          event              event
        #               state        handler            handler
        # --------------------------------------------------------------------
        self.addState ( "top",       self.top,          None )
        self.addState ( "S1",        self.S1,           self.top)
        self.addState ( "S2",        self.S2,           self.top)

        # For GUI messages
        self.__machine_name = "Simple2"
        self.__window_name = "simple2"
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
            self.stateStart(self.S1)
            return 0
        else:
            return 0


    def __handleEv2(self):
        """
        Implementation method for handleEv2()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "handleEv2" in dir(self.__impl_obj):
                # Execute self.__impl_obj.handleEv2() here.
                e = "impl_obj." + "handleEv2()"
                eval(e, {}, locals() )
            else:
                printf("Warning handleEv2() is not implemented!")
        else:
            printf("Warning no implementation object for handleEv2()")


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
        elif self.tEvt['sType'] == "Ev1":
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
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        elif self.tEvt['sType'] == "Ev1":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S1)
            return 0
        elif self.tEvt['sType'] == "Ev2":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.__handleEv2()
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
    simple2 = Simple2Active()
    # start active object register into qf
    simple2.startActive()
    # start/initialize HSM
    simple2.onStart(simple2.top)
    # start the active object thread
    simple2.start()
    #
    # Run event dispatch loop
    qf.run()


if __name__ == "__main__":
    main()
