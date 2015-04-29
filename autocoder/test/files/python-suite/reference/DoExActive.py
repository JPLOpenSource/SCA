#!/usr/bin/env python -i
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
WARNING: This file was automatically generated - DO NOT HAND EDIT

File: DoExActive.py

Automatically generated DoEx state machine.
Date Created:  22-Dec-2009 20:27:33
Created By:    reder

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
# Added for any do activity thread actions defined.
from qf import do_activity_thread

# Module globals initialized here
LOGGER = logging.getLogger('DoExLogger')
# Bail event for orthogonal region or submachine
BAIL_EVENT = event.Event("#BAIL#")


def printf(format, *args):
    #sys.stdout.write(format % args)
    LOGGER.info(format % args)


class DoExActive(active.Active):
    """
    DoEx state machine active object.
    """
    def __init__(self, impl_object=None, window_name="doex", active_obj=None, instance_name=None, is_substate=False):
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
        self.addState ( "s1",        self.s1,           self.top)
        self.addState ( "s2",        self.s2,           self.top)
        self.addState ( "final",     self.final,        self.top)

        # For GUI messages
        self.__machine_name = "DoEx"
        self.__is_substate = is_substate
        if instance_name is not None:
            self.__machine_name = instance_name + ":" + self.__machine_name
        self.__window_name = window_name
        # For manually coded implementation
        # of all actions, guards, etc.
        self.__impl_obj = impl_object
        # For Active object access, 'None' if no super Active object 
        self.__active_obj = active_obj


    def initialize(self):
        """
        Override active object initialize with
        custom initialize routine.  Mostly used
        to subscribe signals for state machine.
        """
        # Subscribe to signals here.
        self._subscribe("Ev1")
        self._subscribe(BAIL_EVENT.signal)

        # Timer event objects created here.
        pass


    def isSubstate(self):
        """
        Return flag indicating whether this StateMachine instance
        is instantiated as a substate of another StateMachine.
        """
        return self.__is_substate


    def sendUpdate(self, state_name, entry_exit):
        """
        Send gui entry or exit messages.
        """
        windowName = self.__window_name
        machineName = self.__machine_name
        stateName = state_name.split(':')[0]
        #
        # determine if machine name has ':'
        submParts = machineName.split(':')
        if len(submParts) > 1:  # yes, reform window name
            windowName = submParts[0] + '_' + submParts[1]
            windowName = windowName.lower()
            machineName = submParts[1]  # machine name should be a simple name
        #
        # now build the update string
        str = "%s %s%s %s" % (windowName, machineName, stateName, entry_exit)
        #print str
        if self.__active_obj != None:
            self.__active_obj._sendUpdate(str)
        else:
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


    def final(self):
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
            # Instance and invoke doActivity thread here...
            self.__do_thread = do_activity_thread.DoThread(target=self.__s1Do)
            self.__do_thread.start()
            #
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            self.__s1Entry()
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            self.__s1Exit()
            # Destroy instance and kill doActivity thread if alive...
            if self.__do_thread.isAlive() == True:
                self.__do_thread.kill()
            del self.__do_thread
            #
            return 0
        elif self.tEvt['sType'] == BAIL_EVENT.signal:
            self.stateTran(self.final)
            return 0
        elif self.tEvt['sType'] == "Ev1":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
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
        elif self.tEvt['sType'] == BAIL_EVENT.signal:
            self.stateTran(self.final)
            return 0
        elif self.tEvt['sType'] == "Ev1":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
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
