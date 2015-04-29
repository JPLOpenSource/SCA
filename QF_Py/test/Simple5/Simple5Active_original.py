#!/usr/bin/env python -i
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
WARNING: This file was automatically generated - DO NOT HAND EDIT

File: Simple5Active..py

Automatically generated Simple1 state machine.
Date Created:  20 Sept. 2009
Created By:    reder

Python implementation of the Simple5 Statechart model
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
LOGGER = logging.getLogger('Simple4Logger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class Simple5Active(active.Active):
    """
    Simple4 state machine active object.
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
        self.addState ( "top",           self.top,                None)
        self.addState ( "S1",            self.S1,               self.top)
        self.addState ( "S2",            self.S2,               self.top)  
        self.addState ( "S3",            self.S3,               self.top) 
        self.addState ( "S4",            self.S4,               self.top) 
        self.addState ( "S5",            self.S5,               self.top)
                 
        # For GUI messages
        self.__machine_name = "Simple5"
        self.__window_name = "simple5"
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
            if self.__guard1() == True:
                self.__action1()
                self.stateTran(self.S2)
            elif self.__guard2() == True:
                self.__action2()
                self.stateTran(self.S3)
            else:
                self.__action3()
                self.stateTran(self.S4)
            return 0
        return self.tEvt['sType']


    def __action1(self):
        """
        Implementation method for action1()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "action1" in dir(self.__impl_obj):
                # Execute self.__impl_obj.action1() here.
                e = "impl_obj." + "action1()"
                eval(e, {}, locals() )
            else:
                printf("Warning action1() is not implemented!")
        else:
            printf("Warning no implementation object for action1()")
            
   
    def __action2(self):
        """
        Implementation method for action2()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "action2" in dir(self.__impl_obj):
                # Execute self.__impl_obj.action2() here.
                e = "impl_obj." + "action2()"
                eval(e, {}, locals() )
            else:
                printf("Warning action2() is not implemented!")
        else:
            printf("Warning no implementation object for action2()")
            
            
    def __action3(self):
        """
        Implementation method for action3()
        """
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "action3" in dir(self.__impl_obj):
                # Execute self.__impl_obj.action3() here.
                e = "impl_obj." + "action3()"
                eval(e, {}, locals() )
            else:
                printf("Warning action3() is not implemented!")
        else:
            printf("Warning no implementation object for action3()")


    def __guard1(self):
        """
        Implementation guard method for guard1()
        """
        res = False
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "guard1" in dir(self.__impl_obj):
                # Execute self.__impl_obj.guard1() here.
                e = "impl_obj." + "guard1()"
                res = eval(e, {}, locals() )
            else:
                printf("Warning guard1() is not implemented!")
        else:
            printf("Warning no implementation object for guard1()")                       
        return res


    def __guard2(self):
        """
        Implementation guard method for guard2()
        """
        res = False
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "guard2" in dir(self.__impl_obj):
                # Execute self.__impl_obj.guard2() here.
                e = "impl_obj." + "guard2()"
                res = eval(e, {}, locals() )
            else:
                printf("Warning guard2() is not implemented!")
        else:
            printf("Warning no implementation object for guard2()")                       
        return res
    
    
    def __guard3(self):
        """
        Implementation guard method for guard3()
        """
        res = False
        if self.__impl_obj != None:
            impl_obj = self.__impl_obj
            if "guard3" in dir(self.__impl_obj):
                # Execute self.__impl_obj.guard3() here.
                e = "impl_obj." + "guard3()"
                res = eval(e, {}, locals() )
            else:
                printf("Warning guard3() is not implemented!")
        else:
            printf("Warning no implementation object for guard3()")                       
        return res


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
        elif self.tEvt['sType'] == "Ev2":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S5)
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
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s", machine, state_name, "EXIT")
            self.sendUpdate(state_name,"EXIT")
            return 0
        elif self.tEvt['sType'] == "Ev2":
            printf("%s %s", state_name, self.tEvt['sType'])
            self.stateTran(self.S5)
            return 0
        return self.tEvt['sType']
    
    
    def S4(self):
        """
        State S4
        """
        machine = self.__machine_name
        state_name = "S4"
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
            self.stateTran(self.S5)
            return 0
        return self.tEvt['sType']


    def S5(self):
        """
        State S5
        """
        machine = self.__machine_name
        state_name = "S5"
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
    simple5 = Simple5Active()
    # start/initialize HSM
    simple5.onStart(simple5.top)
    # start active object register into qf
    simple5.startActive()
    # start the active object thread
    simple5.start()
    #
    # Run event dispatch loop
    qf.run()

if __name__ == "__main__":
    main()


