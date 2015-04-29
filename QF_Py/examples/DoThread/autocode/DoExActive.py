#!/usr/bin/env python -i
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
WARNING: This file was automatically generated - DO NOT HAND EDIT

File: DoExActive.py

Automatically generated DoEx state machine.
Date Created:  22-Dec-2009 08:53:19
Created By:    watney

Python implementation of the DoEx Statechart model
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
from DoThread import *

# Module globals initialized here
LOGGER = logging.getLogger('DoExLogger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class DoExActive(active.Active):
    """
    DoEx state machine active object.
    """
    def __init__(self, impl_object=None, window_name="doex"):
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
        self.addState ( "s1",        self.s1,           self.top)
        self.addState ( "s2",        self.s2,           self.top)

        # For GUI messages
        self.__machine_name = "DoEx"
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
            self.stateStart(self.s1)
            return 0
        else:
            return 0


    def __s1Entry(self):
        """
        Implementation method for s1Entry()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "s1Entry" in dir(self.__impl_obj):
                # Execute self.__impl_obj.s1Entry() here.
                e = "impl_obj." + "s1Entry()"
                eval(e, {}, locals() )
            else:
                printf("Warning: s1Entry() is not implemented!")
        else:
            printf("Warning: no implementation object for s1Entry()")


    def __s1Do(self):
        """
        Implementation method for s1Do()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "s1Do" in dir(self.__impl_obj):
                # Execute self.__impl_obj.s1Do() here.
                e = "impl_obj." + "s1Do()"
                eval(e, {}, locals() )
            else:
                printf("Warning: s1Do() is not implemented!")
        else:
            printf("Warning: no implementation object for s1Do()")


    def __s1Exit(self):
        """
        Implementation method for s1Exit()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "s1Exit" in dir(self.__impl_obj):
                # Execute self.__impl_obj.s1Exit() here.
                e = "impl_obj." + "s1Exit()"
                eval(e, {}, locals() )
            else:
                printf("Warning: s1Exit() is not implemented!")
        else:
            printf("Warning: no implementation object for s1Exit()")


    def s1(self):
        """
        State s1
        """
        machine = self.__machine_name
        state_name = "s1"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            
            # GW - The following 2 lines have been added
            self.s1Thread = DoThread(self.__s1Do)
            self.s1Thread.start()
            
            self.__s1Entry()
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            
            # GW - The following 1 line has been added
            self.s1Thread.terminate()

            self.__s1Exit()
            return 0
        elif self.tEvt['sType'] == "Ev1":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.s2)
            return 0
        return self.tEvt['sType']


    def s2(self):
        """
        State s2
        """
        machine = self.__machine_name
        state_name = "s2"
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
            self.stateTran(self.s1)
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
    doex = DoExActive()
    # start active object register into qf
    doex.startActive()
    # start/initialize HSM
    doex.onStart(doex.top)
    # start the active object thread
    doex.start()
    #
    # Run event dispatch loop
    qf.run()


if __name__ == "__main__":
    main()
