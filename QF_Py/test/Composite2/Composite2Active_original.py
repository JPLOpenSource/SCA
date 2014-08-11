#!/usr/bin/env python -i
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
WARNING: This file was automatically generated - DO NOT HAND EDIT

File: Composite2Active..py

Automatically generated Simple1 state machine.
Date Created:  10 Sept. 2009
Created By:    reder

Python implementation of the Composite2 Statechart model
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
LOGGER = logging.getLogger('Composite2Logger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class Composite2Active(active.Active):
    """
    Composite1 state machine active object.
    """

    def __init__(self, impl_object=None):
        """
        Constructor
        """
        active.Active.__init__(self)
        # --------------------------------------------------------------------
        #               name                                   parent's
        #                of              event                 event
        #               state            handler               handler
        # --------------------------------------------------------------------
        self.addState ( "top",    self.top,         None)
        self.addState ( "S1",     self.S1,          self.top)
        self.addState ( "S2",     self.S2,          self.top)
        self.addState ( "S21",    self.S21,         self.S2)
        self.addState ( "S22",    self.S22,         self.S2)
        self.addState ( "S3",     self.S3,          self.top)
        
        # For GUI messages
        self.__machine_name = "Composite2"
        self.__window_name = "composite2"
        # For manually coded implementation
        # of all actions, guards, etc.
        self.__impl_obj = impl_object
        # History states memory here.
        self.__S2_history = self.S21 
        
        # Timer event objects created here.
        pass


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

    
    def sendUpdate(self, state_name, entry_exit):
        """
        Send gui entry or exit messages.
        """
        str = "%s %s%s %s" % (self.__window_name, self.__machine_name, state_name, entry_exit)
        print str
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


    def __entry(self, state_name):
        """
        Implementation method for entry()
        """
        if self.__impl_obj != None:
            impl_obj=self.__impl_obj
            if (state_name + "Entry") in dir(self.__impl_obj):
                # Execute self.__impl_obj.<state_name>Entry() here.
                e = "impl_obj." + state_name + "Entry()"
                eval(e, {}, locals())


    def __exit(self, state_name):
        """
        Implementation method for exit()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if (state_name + "Exit") in dir(self.__impl_obj):
                # Execute self.__impl_obj.<state_name>Exit() here.
                e = "impl_obj." + state_name + "Exit()"
                eval(e, {}, locals() )
                
                
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
            self.__entry(state_name)
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            self.__exit(state_name)
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
            self.stateStart(self.S21)
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            self.__entry(state_name)
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            self.__exit(state_name)
            return 0
        elif self.tEvt['sType'] == "Ev1":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S1)
            return 0
        elif self.tEvt['sType'] == "Ev3":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S3)
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
            self.__S2_history = self.S21
            self.__entry(state_name)
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            self.__exit(state_name)
            return 0
        elif self.tEvt['sType'] == "Ev1":
            printf("%s %s", state_name, self.tEvt['sType'])
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
            self.__S2_history = self.S22
            self.__entry(state_name)
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            self.__exit(state_name)
            return 0
        elif self.tEvt['sType'] == "Ev2":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S21)
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
            self.__entry(state_name)
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            self.__exit(state_name)
            return 0
        elif self.tEvt['sType'] == "Ev3":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.__S2_history)
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
    composite2 = Composite2Active()
    # start/initialize HSM
    composite2.onStart(composite2.top)
    # start active object register into qf
    composite2.startActive()
    # start the active object thread
    composite2.start()
    #
    # Run event dispatch loop
    qf.run()


if __name__ == "__main__":
    main()

