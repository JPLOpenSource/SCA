#!/usr/bin/env python -i
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
WARNING: This file was automatically generated - DO NOT HAND EDIT

File: Composite8_1Active.py

Automatically generated Composite8_1 state machine.
Date Created:  06-Oct-2009 16:07:24
Created By:    reder

Python implementation of the Composite8_1 Statechart model
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
LOGGER = logging.getLogger('Composite8_1Logger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class Composite8_1Active(active.Active):
    """
    Composite8_1 state machine active object.
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

        # For GUI messages
        self.__machine_name = "Composite8_1"
        self.__window_name = "composite8_1"
        # For manually coded implementation
        # of all actions, guards, etc.
        self.__impl_obj = impl_object

        # Orthogonal regions instanced here
        self.__region1 = Composite8_1S1Region1(self.__impl_obj, \
                                               self.__machine_name, \
                                               self.__window_name,
                                               self)
        self.__region2 = Composite8_1S1Region2(self.__impl_obj, \
                                               self.__machine_name, \
                                               self.__window_name,
                                               self)
        self.__region3 = Composite8_1S1Region3(self.__impl_obj, \
                                               self.__machine_name, \
                                               self.__window_name,
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
        self._subscribe("Ev4")
        self._subscribe("Ev5")
        self._subscribe("Ev6")

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
            self.__region1.onStart(self.__region1.top)
            self.__region2.onStart(self.__region2.top)
            self.__region3.onStart(self.__region3.top)
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        elif self.tEvt['sType'] == "Ev1":
            printf("%s Region1 %s", state_name, self.tEvt['sType'])
            self.__region1.dispatch(self.tEvt)
            printf("%s Region2 %s", state_name, self.tEvt['sType'])
            self.__region2.dispatch(self.tEvt)
            printf("%s Region3 %s", state_name, self.tEvt['sType'])
            self.__region3.dispatch(self.tEvt)
            return 0
        elif self.tEvt['sType'] == "Ev2":
            printf("%s Region1 %s", state_name, self.tEvt['sType'])
            self.__region1.dispatch(self.tEvt)
            printf("%s Region2 %s", state_name, self.tEvt['sType'])
            self.__region2.dispatch(self.tEvt)
            printf("%s Region3 %s", state_name, self.tEvt['sType'])
            self.__region3.dispatch(self.tEvt)
            return 0
        elif self.tEvt['sType'] == "Ev3":
            printf("%s Region1 %s", state_name, self.tEvt['sType'])
            self.__region1.dispatch(self.tEvt)
            printf("%s Region2 %s", state_name, self.tEvt['sType'])
            self.__region2.dispatch(self.tEvt)
            printf("%s Region3 %s", state_name, self.tEvt['sType'])
            self.__region3.dispatch(self.tEvt)
            return 0
        elif self.tEvt['sType'] == "Ev4":
            printf("%s Region1 %s", state_name, self.tEvt['sType'])
            self.__region1.dispatch(self.tEvt)
            printf("%s Region2 %s", state_name, self.tEvt['sType'])
            self.__region2.dispatch(self.tEvt)
            printf("%s Region3 %s", state_name, self.tEvt['sType'])
            self.__region3.dispatch(self.tEvt)
            return 0
        elif self.tEvt['sType'] == "Ev5":
            printf("%s Region1 %s", state_name, self.tEvt['sType'])
            self.__region1.dispatch(self.tEvt)
            printf("%s Region2 %s", state_name, self.tEvt['sType'])
            self.__region2.dispatch(self.tEvt)
            printf("%s Region3 %s", state_name, self.tEvt['sType'])
            self.__region3.dispatch(self.tEvt)
            return 0
        elif self.tEvt['sType'] == "Ev6":
            printf("%s Region1 %s", state_name, self.tEvt['sType'])
            self.__region1.dispatch(self.tEvt)
            printf("%s Region2 %s", state_name, self.tEvt['sType'])
            self.__region2.dispatch(self.tEvt)
            printf("%s Region3 %s", state_name, self.tEvt['sType'])
            self.__region3.dispatch(self.tEvt)
            return 0
        return self.tEvt['sType']


class Composite8_1S1Region1(miros.Hsm):
    """
    Composite8_1 state machine orthogonal Composite8_1S1Region1 object.
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
        self.addState ( "top",       self.top,          None )
        self.addState ( "S11",       self.S11,          self.top)
        self.addState ( "S12",       self.S12,          self.top)
        self.addState ( "S121",      self.S121,         self.S12)
        self.addState ( "S122",      self.S122,         self.S12)
        self.addState ( "S13",       self.S13,          self.top)

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
            self.stateStart(self.S11)
            return 0
        else:
            return 0


    def S11(self):
        """
        State S11
        """
        machine = self.__machine_name
        state_name = "S11"
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
            self.stateTran(self.S12)
            return 0
        return self.tEvt['sType']


    def S12(self):
        """
        State S12
        """
        machine = self.__machine_name
        state_name = "S12"
        if self.tEvt['sType'] == "init":
            self.stateStart(self.S121)
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        return self.tEvt['sType']


    def S121(self):
        """
        State S121
        """
        machine = self.__machine_name
        state_name = "S121"
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
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S122)
            return 0
        return self.tEvt['sType']


    def S122(self):
        """
        State S122
        """
        machine = self.__machine_name
        state_name = "S122"
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
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S121)
            return 0
        elif self.tEvt['sType'] == "Ev1":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S13)
            return 0
        return self.tEvt['sType']


    def S13(self):
        """
        State S13
        """
        machine = self.__machine_name
        state_name = "S13"
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
        return self.tEvt['sType']


class Composite8_1S1Region2(miros.Hsm):
    """
    Composite8_1 state machine orthogonal Composite8_1S1Region2 object.
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
        self.addState ( "top",       self.top,          None )
        self.addState ( "S14",       self.S14,          self.top)
        self.addState ( "S15",       self.S15,          self.top)

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
            self.stateStart(self.S14)
            return 0
        else:
            return 0


    def __Action1Ev2Action3Action4(self):
        """
        Implementation method for Action1Ev2Action3Action4()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "Action1Ev2Action3Action4" in dir(self.__impl_obj):
                # Execute self.__impl_obj.Action1Ev2Action3Action4() here.
                e = "impl_obj." + "Action1Ev2Action3Action4()"
                eval(e, {}, locals() )
            else:
                printf("Warning Action1Ev2Action3Action4() is not implemented!")
        else:
            printf("Warning no implementation object for Action1Ev2Action3Action4()")


    def S14(self):
        """
        State S14
        """
        machine = self.__machine_name
        state_name = "S14"
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
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S15)
            return 0
        return self.tEvt['sType']


    def S15(self):
        """
        State S15
        """
        machine = self.__machine_name
        state_name = "S15"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            self.__Action1()
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        elif self.tEvt['sType'] == "Ev3":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S14)
            return 0
        return self.tEvt['sType']


class Composite8_1S1Region3(miros.Hsm):
    """
    Composite8_1 state machine orthogonal Composite8_1S1Region3 object.
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
        self.addState ( "top",       self.top,          None )
        self.addState ( "S16",       self.S16,          self.top)
        self.addState ( "S161",      self.S161,         self.S16)
        self.addState ( "S1611",     self.S1611,        self.S161)
        self.addState ( "S17",       self.S17,          self.top)

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
            self.stateStart(self.S16)
            return 0
        else:
            return 0


    def S16(self):
        """
        State S16
        """
        machine = self.__machine_name
        state_name = "S16"
        if self.tEvt['sType'] == "init":
            self.stateStart(self.S161)
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        elif self.tEvt['sType'] == "Ev4":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S17)
            return 0
        return self.tEvt['sType']


    def S161(self):
        """
        State S161
        """
        machine = self.__machine_name
        state_name = "S161"
        if self.tEvt['sType'] == "init":
            self.stateStart(self.S1611)
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s", machine, state_name, "ENTRY")
            self.sendUpdate(state_name,"ENTRY")
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        return self.tEvt['sType']


    def S1611(self):
        """
        State S1611
        """
        machine = self.__machine_name
        state_name = "S1611"
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
        return self.tEvt['sType']


    def S17(self):
        """
        State S17
        """
        machine = self.__machine_name
        state_name = "S17"
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
        elif self.tEvt['sType'] == "Ev4":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S16)
            return 0
        elif self.tEvt['sType'] == "Ev5":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S161)
            return 0
        elif self.tEvt['sType'] == "Ev6":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S1611)
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
    composite8_1 = Composite8_1Active()
    # start active object register into qf
    composite8_1.startActive()
    # start/initialize HSM
    composite8_1.onStart(composite8_1.top)
    # start the active object thread
    composite8_1.start()
    #
    # Run event dispatch loop
    qf.run()


if __name__ == "__main__":
    main()
