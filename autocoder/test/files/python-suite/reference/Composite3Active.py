#!/usr/bin/env python -i
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
WARNING: This file was automatically generated - DO NOT HAND EDIT

File: Composite3Active.py

Automatically generated Composite3 state machine.
Date Created:  8 Sept. 2009
Created By:    reder

Python implementation of the Composite3 Statechart model
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
LOGGER = logging.getLogger('Composite3Logger')
#- bail-region event
BAIL_REGION_EVENT = event.Event("#BAIL#")


def printf(format, *args):
    #sys.stdout.write(format % args)
    LOGGER.info(format % args)


class Composite3Active(active.Active):
    """
    Composite3 state machine active object.
    """
    def __init__(self, impl_object=None, window_name="composite3"):
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

        # For GUI messages
        self.__machine_name = "Composite3"
        self.__window_name = window_name
        # For manually coded implementation
        # of all actions, guards, etc.
        self.__impl_obj = impl_object

        # Orthogonal regions instanced here
        self.__region1 = Composite3S2Region1(self.__impl_obj, \
                                             self.__machine_name, \
                                             self.__window_name, \
                                             self)
        self.__region2 = Composite3S2Region2(self.__impl_obj, \
                                             self.__machine_name, \
                                             self.__window_name, \
                                             self)


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
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
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
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            self.__region1.onStart(self.__region1.top)
            self.__region2.onStart(self.__region2.top)
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            self.__region1.dispatch(BAIL_REGION_EVENT)
            self.__region2.dispatch(BAIL_REGION_EVENT)
            return 0
        elif self.tEvt['sType'] == "Ev1":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.S1)
            return 0
        elif self.tEvt['sType'] == "Ev2":
            printf("%s %s Region1 %s", machine, state_name, self.tEvt['sType'])
            self.__region1.dispatch(self.tEvt)
            return 0
        elif self.tEvt['sType'] == "Ev3":
            printf("%s %s Region2 %s", machine, state_name, self.tEvt['sType'])
            self.__region2.dispatch(self.tEvt)
            return 0
        return self.tEvt['sType']


class Composite3S2Region1(miros.Hsm):
    """
    Composite3 state machine orthogonal Composite3S2Region1 object.
    """
    def __init__(self, impl_object=None, machine_name=None, window_name=None, active_obj=None):
        """
        Constructor
        """
        miros.Hsm.__init__(self)
        # --------------------------------------------------------------------
        #               name                            parent's
        #                of          event              event
        #               state        handler            handler
        # --------------------------------------------------------------------
        self.addState ( "top",       self.top,          None)
        self.addState ( "S21",       self.S21,          self.top)
        self.addState ( "S22",       self.S22,          self.top)

        # For GUI messages
        self.__machine_name = machine_name
        self.__window_name = window_name
        # For manually coded implementation
        # of all actions, guards, etc.
        self.__impl_obj = impl_object
        # For Active object access
        self.__active_obj = active_obj


    def sendUpdate(self, state_name, entry_exit):
        """
        Send gui entry or exit messages.
        """
        if self.__active_obj != None:
            self.__active_obj.sendUpdate(state_name, entry_exit)


    def top(self):
        """
        The routine handles initial events and events that get bubbled 
        to the top of the state-machine.  Events that get bubbled to the top can
        either be ignored (return 0) or an error message can be generated.
        """
        if self.tEvt['sType'] == "init":
            self.stateStart(self.S21)
            return 0
        else:
            return 0


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
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        elif self.tEvt['sType'] == "Ev2":
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
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        elif self.tEvt['sType'] == "Ev2":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.S21)
            return 0
        return self.tEvt['sType']


class Composite3S2Region2(miros.Hsm):
    """
    Composite3 state machine orthogonal Composite3S2Region2 object.
    """
    def __init__(self, impl_object=None, machine_name=None, window_name=None, active_obj=None):
        """
        Constructor
        """
        miros.Hsm.__init__(self)
        # --------------------------------------------------------------------
        #               name                            parent's
        #                of          event              event
        #               state        handler            handler
        # --------------------------------------------------------------------
        self.addState ( "top",       self.top,          None)
        self.addState ( "S23",       self.S23,          self.top)
        self.addState ( "S24",       self.S24,          self.top)

        # For GUI messages
        self.__machine_name = machine_name
        self.__window_name = window_name
        # For manually coded implementation
        # of all actions, guards, etc.
        self.__impl_obj = impl_object
        # For Active object access
        self.__active_obj = active_obj


    def sendUpdate(self, state_name, entry_exit):
        """
        Send gui entry or exit messages.
        """
        if self.__active_obj != None:
            self.__active_obj.sendUpdate(state_name, entry_exit)


    def top(self):
        """
        The routine handles initial events and events that get bubbled 
        to the top of the state-machine.  Events that get bubbled to the top can
        either be ignored (return 0) or an error message can be generated.
        """
        if self.tEvt['sType'] == "init":
            self.stateStart(self.S23)
            return 0
        else:
            return 0


    def S23(self):
        """
        State S23
        """
        machine = self.__machine_name
        state_name = "S23"
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
            self.stateTran(self.S24)
            return 0
        return self.tEvt['sType']


    def S24(self):
        """
        State S24
        """
        machine = self.__machine_name
        state_name = "S24"
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
            self.stateTran(self.S23)
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
    composite3 = Composite3Active()
    # start active object register into qf
    composite3.startActive()
    # start/initialize HSM
    composite3.onStart(composite3.top)
    # start the active object thread
    composite3.start()
    #
    # Run event dispatch loop
    qf.run()


if __name__ == "__main__":
    main()
