#!/usr/bin/env python -i
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
WARNING: This file was automatically generated - DO NOT HAND EDIT

File: Composite7_1Active.py

Automatically generated Composite7_1 state machine.
Date Created:  03-Oct-2009 23:31:06
Created By:    reder

Python implementation of the Composite7_1 Statechart model
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
LOGGER = logging.getLogger('Composite7_1Logger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class Composite7_1Active(active.Active):
    """
    Composite7_1 state machine active object.
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
        self.addState ( "S11",       self.S11,          self.S1)
        self.addState ( "S12",       self.S12,          self.S1)

        # For GUI messages
        self.__machine_name = "Composite7_1"
        self.__window_name = "composite7_1"
        # For manually coded implementation
        # of all actions, guards, etc.
        self.__impl_obj = impl_object

        # Orthogonal regions instanced here
        self.__region1 = Composite7_1S1S12Region1(self.__impl_obj, \
                                                  self.__machine_name, \
                                                  self.__window_name,
                                                  self)
        self.__region2 = Composite7_1S1S12Region2(self.__impl_obj, \
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


    def __InternalHandler2(self):
        """
        Implementation method for InternalHandler2()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "InternalHandler2" in dir(self.__impl_obj):
                # Execute self.__impl_obj.InternalHandler2() here.
                e = "impl_obj." + "InternalHandler2()"
                eval(e, {}, locals() )
            else:
                printf("Warning InternalHandler2() is not implemented!")
        else:
            printf("Warning no implementation object for InternalHandler2()")


    def __InternalHandler3(self):
        """
        Implementation method for InternalHandler3()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "InternalHandler3" in dir(self.__impl_obj):
                # Execute self.__impl_obj.InternalHandler3() here.
                e = "impl_obj." + "InternalHandler3()"
                eval(e, {}, locals() )
            else:
                printf("Warning InternalHandler3() is not implemented!")
        else:
            printf("Warning no implementation object for InternalHandler3()")


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
                printf("Warning Action1() is not implemented!")
        else:
            printf("Warning no implementation object for Action1()")


    def S1(self):
        """
        State S1
        """
        machine = self.__machine_name
        state_name = "S1"
        if self.tEvt['sType'] == "init":
            self.stateStart(self.S11)
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
            self.__InternalHandler2()
            return 0
        return self.tEvt['sType']


    def S11(self):
        """
        State S11
        """
        machine = self.__machine_name
        state_name = "S11"
        if self.tEvt['sType'] == "init":
            self.__Action1()
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
            return 0
        elif self.tEvt['sType'] == "Ev1":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S11)
            printf("%s Region1 %s", state_name, self.tEvt['sType'])
            self.__region1.dispatch(self.tEvt)
            printf("%s Region2 %s", state_name, self.tEvt['sType'])
            self.__region2.dispatch(self.tEvt)
            return 0
        elif self.tEvt['sType'] == "Ev3":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.__InternalHandler3()
            return 0
        elif self.tEvt['sType'] == "Ev4":
            printf("%s Region1 %s", state_name, self.tEvt['sType'])
            self.__region1.dispatch(self.tEvt)
            printf("%s Region2 %s", state_name, self.tEvt['sType'])
            self.__region2.dispatch(self.tEvt)
            return 0
        elif self.tEvt['sType'] == "Ev5":
            printf("%s Region1 %s", state_name, self.tEvt['sType'])
            self.__region1.dispatch(self.tEvt)
            printf("%s Region2 %s", state_name, self.tEvt['sType'])
            self.__region2.dispatch(self.tEvt)
            return 0
        elif self.tEvt['sType'] == "Ev6":
            printf("%s Region1 %s", state_name, self.tEvt['sType'])
            self.__region1.dispatch(self.tEvt)
            printf("%s Region2 %s", state_name, self.tEvt['sType'])
            self.__region2.dispatch(self.tEvt)
            return 0
        return self.tEvt['sType']


class Composite7_1S1S12Region1(miros.Hsm):
    """
    Composite7_1 state machine orthogonal Composite7_1S1S12Region1 object.
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
        self.addState ( "S111",      self.S111,         self.top)
        self.addState ( "S112",      self.S112,         self.top)

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
            self.__Action2()
            self.stateStart(self.S111)
            return 0
        else:
            return 0


    def __InternalHandler4(self):
        """
        Implementation method for InternalHandler4()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "InternalHandler4" in dir(self.__impl_obj):
                # Execute self.__impl_obj.InternalHandler4() here.
                e = "impl_obj." + "InternalHandler4()"
                eval(e, {}, locals() )
            else:
                printf("Warning InternalHandler4() is not implemented!")
        else:
            printf("Warning no implementation object for InternalHandler4()")


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
                printf("Warning Action2() is not implemented!")
        else:
            printf("Warning no implementation object for Action2()")


    def S111(self):
        """
        State S111
        """
        machine = self.__machine_name
        state_name = "S111"
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
        elif self.tEvt['sType'] == "Ev5":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S112)
            return 0
        elif self.tEvt['sType'] == "Ev4":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.__InternalHandler4()
            return 0
        return self.tEvt['sType']


    def S112(self):
        """
        State S112
        """
        machine = self.__machine_name
        state_name = "S112"
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
        elif self.tEvt['sType'] == "Ev5":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S111)
            return 0
        return self.tEvt['sType']


class Composite7_1S1S12Region2(miros.Hsm):
    """
    Composite7_1 state machine orthogonal Composite7_1S1S12Region2 object.
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
        self.addState ( "S211",      self.S211,         self.top)
        self.addState ( "S212",      self.S212,         self.top)

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
            self.__Action3()
            self.stateStart(self.S211)
            return 0
        else:
            return 0


    def __InternalHandler4(self):
        """
        Implementation method for InternalHandler4()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "InternalHandler4" in dir(self.__impl_obj):
                # Execute self.__impl_obj.InternalHandler4() here.
                e = "impl_obj." + "InternalHandler4()"
                eval(e, {}, locals() )
            else:
                printf("Warning InternalHandler4() is not implemented!")
        else:
            printf("Warning no implementation object for InternalHandler4()")


    def __Action3(self):
        """
        Implementation method for Action3()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "Action3" in dir(self.__impl_obj):
                # Execute self.__impl_obj.Action3() here.
                e = "impl_obj." + "Action3()"
                eval(e, {}, locals() )
            else:
                printf("Warning Action3() is not implemented!")
        else:
            printf("Warning no implementation object for Action3()")


    def S211(self):
        """
        State S211
        """
        machine = self.__machine_name
        state_name = "S211"
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
        elif self.tEvt['sType'] == "Ev6":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S212)
            return 0
        return self.tEvt['sType']


    def S212(self):
        """
        State S212
        """
        machine = self.__machine_name
        state_name = "S212"
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
        elif self.tEvt['sType'] == "Ev6":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S211)
            return 0
        elif self.tEvt['sType'] == "Ev4":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.__InternalHandler4()
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
    composite7_1 = Composite7_1Active()
    # start active object register into qf
    composite7_1.startActive()
    # start/initialize HSM
    composite7_1.onStart(composite7_1.top)
    # start the active object thread
    composite7_1.start()
    #
    # Run event dispatch loop
    qf.run()


if __name__ == "__main__":
    main()
