#!/usr/bin/env python -i
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
WARNING: This file was automatically generated - DO NOT HAND EDIT

File: SequencerActive.py

Automatically generated Sequencer state machine.
Date Created:  25-Feb-2010 13:22:43
Created By:    reder

Python implementation of the Sequencer Statechart model
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
LOGGER = logging.getLogger('SequencerLogger')
# Bail event for orthogonal region or submachine
BAIL_EVENT = event.Event("#BAIL#")


def printf(format, *args):
    #sys.stdout.write(format % args)
    LOGGER.info(format % args)


class SequencerActive(active.Active):
    """
    Sequencer state machine active object.
    """
    def __init__(self, impl_object=None, window_name="sequencer", active_obj=None, instance_name=None, is_substate=False):
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
        self.addState ( "ERROR",     self.ERROR,        self.top )
        self.addState ( "IDLE",      self.IDLE,         self.NOMINAL )
        self.addState ( "NOMINAL",   self.NOMINAL,      self.top )
        self.addState ( "STEP1",     self.STEP1,        self.NOMINAL )
        self.addState ( "STEP2",     self.STEP2,        self.NOMINAL )
        self.addState ( "STEPn",     self.STEPn,        self.NOMINAL )
        self.addState ( "final",     self.final,        self.top )

        # Current state
        self.__current_state = None

        # For GUI messages
        self.__machine_name = "Sequencer"
        self.__is_substate = is_substate
        if instance_name is not None:
            self.__machine_name = instance_name + ":" + self.__machine_name
        self.__window_name = window_name
        # For manually coded implementation
        # of all actions, guards, etc.
        self.__impl_obj = impl_object
        # For Active object access, 'None' if no super Active object 
        self.__active_obj = active_obj

        # History states memory here.


    def initialize(self):
        """
        Override active object initialize with
        custom initialize routine.  Mostly used
        to subscribe signals for state machine.
        """
        # Subscribe to signals here.
        self._subscribe("Error")
        self._subscribe("Event0")
        self._subscribe("Event1")
        self._subscribe("Event2")
        self._subscribe("EventN")
        self._subscribe("Recover")
        self._subscribe("RepeatAction")
        self._subscribe("Reset")

        # Timer event objects created here.


    def getCurrentState(self):
        """
        Returns the current leaf state.
        """
        return self.__current_state


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
            self.stateStart(self.NOMINAL)
            return 0
        return 0


    def final(self):
        return 0


    def __Action1(self):
        """
        Implementation method for Action1()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "Action1" in dir(self.__impl_obj):
                # Execute self.__impl_obj.Action1() here.
                e = "impl_obj." + "Action1()"
                eval(e, {}, locals() )
            else:
                printf("Warning: Action1() is not implemented!")
        else:
            printf("Warning: no implementation object for Action1()")


    def __Action2(self):
        """
        Implementation method for Action2()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "Action2" in dir(self.__impl_obj):
                # Execute self.__impl_obj.Action2() here.
                e = "impl_obj." + "Action2()"
                eval(e, {}, locals() )
            else:
                printf("Warning: Action2() is not implemented!")
        else:
            printf("Warning: no implementation object for Action2()")


    def __ActionN(self):
        """
        Implementation method for ActionN()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "ActionN" in dir(self.__impl_obj):
                # Execute self.__impl_obj.ActionN() here.
                e = "impl_obj." + "ActionN()"
                eval(e, {}, locals() )
            else:
                printf("Warning: ActionN() is not implemented!")
        else:
            printf("Warning: no implementation object for ActionN()")


    def __errorHandler(self):
        """
        Implementation method for errorHandler()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "errorHandler" in dir(self.__impl_obj):
                # Execute self.__impl_obj.errorHandler() here.
                e = "impl_obj." + "errorHandler()"
                eval(e, {}, locals() )
            else:
                printf("Warning: errorHandler() is not implemented!")
        else:
            printf("Warning: no implementation object for errorHandler()")


    def __showStatus(self):
        """
        Implementation method for showStatus()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "showStatus" in dir(self.__impl_obj):
                # Execute self.__impl_obj.showStatus() here.
                e = "impl_obj." + "showStatus()"
                eval(e, {}, locals() )
            else:
                printf("Warning: showStatus() is not implemented!")
        else:
            printf("Warning: no implementation object for showStatus()")


    def ERROR(self):
        """
        State ERROR
        """
        machine = self.__machine_name
        state_name = "ERROR"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            self.__current_state = state_name
            self.__errorHandler()
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        elif self.tEvt['sType'] == BAIL_EVENT.signal:
            self.stateTran(self.final)
            return 0
        elif self.tEvt['sType'] == "Recover":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.__NOMINAL_history)
            return 0
        return self.tEvt['sType']


    def IDLE(self):
        """
        State IDLE
        """
        machine = self.__machine_name
        state_name = "IDLE"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            self.__current_state = state_name
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            self.__NOMINAL_history = self.IDLE
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        elif self.tEvt['sType'] == BAIL_EVENT.signal:
            self.stateTran(self.final)
            return 0
        elif self.tEvt['sType'] == "Event0":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.STEP1)
            return 0
        return self.tEvt['sType']


    def NOMINAL(self):
        """
        State NOMINAL
        """
        machine = self.__machine_name
        state_name = "NOMINAL"
        if self.tEvt['sType'] == "init":
            self.stateStart(self.IDLE)
            return 0
        elif self.tEvt['sType'] == "entry":
            self.__current_state = state_name
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
        elif self.tEvt['sType'] == "Error":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.ERROR)
            return 0
        elif self.tEvt['sType'] == "RepeatAction":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.__showStatus()
            self.stateTran(self.__NOMINAL_history)
            return 0
        elif self.tEvt['sType'] == "Reset":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.NOMINAL)
            return 0
        return self.tEvt['sType']


    def STEP1(self):
        """
        State STEP1
        """
        machine = self.__machine_name
        state_name = "STEP1"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            self.__current_state = state_name
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            self.__NOMINAL_history = self.STEP1
            self.__Action1()
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        elif self.tEvt['sType'] == BAIL_EVENT.signal:
            self.stateTran(self.final)
            return 0
        elif self.tEvt['sType'] == "Event1":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.STEP2)
            return 0
        return self.tEvt['sType']


    def STEP2(self):
        """
        State STEP2
        """
        machine = self.__machine_name
        state_name = "STEP2"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            self.__current_state = state_name
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            self.__NOMINAL_history = self.STEP2
            self.__Action2()
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        elif self.tEvt['sType'] == BAIL_EVENT.signal:
            self.stateTran(self.final)
            return 0
        elif self.tEvt['sType'] == "Event2":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.STEPn)
            return 0
        return self.tEvt['sType']


    def STEPn(self):
        """
        State STEPn
        """
        machine = self.__machine_name
        state_name = "STEPn"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            self.__current_state = state_name
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            self.__NOMINAL_history = self.STEPn
            self.__ActionN()
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        elif self.tEvt['sType'] == BAIL_EVENT.signal:
            self.stateTran(self.final)
            return 0
        elif self.tEvt['sType'] == "EventN":
            printf("%s %s %s", machine, state_name, self.tEvt['sType'])
            self.stateTran(self.IDLE)
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
    sequencer = SequencerActive()
    # start active object register into qf
    sequencer.startActive()
    # start/initialize HSM
    sequencer.onStart(sequencer.top)
    # start the active object thread
    sequencer.start()
    #
    # Run event dispatch loop
    qf.run()


if __name__ == "__main__":
    main()
