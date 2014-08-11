#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: SequencerImpl.py

Date Created:  24-Feb-2010 23:15:47
Created By:    reder

Python custom-implementation class for functions referenced in
the Sequencer Statechart model.
"""
# Python imports here
import sys
import logging
import time

# QF imports here
from qf import event
from qf import framework

import sim_state_start

# Module globals initialized here
LOGGER = logging.getLogger('SequencerLogger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class SequencerImpl(object):
    """
    Sequencer state machine implementation object.
    """
    def __init__(self, machine_name="Sequencer"):
        """
        Constructor
        """
        self.__machine_name = machine_name
        self.__state = None          # The current action state
        self.__error_flag = False   # Set True to simulate and Error
        #
        print "*Accessible: IMPL['Sequencer']"


    def getCurrentState(self):
        """
        Return current state.
        """
        active = framework.QF.getInstance().getActive(0)
        return active.getCurrentState()


    def setError(self):
        """
        Sets the error flag.
        """
        self.__error_flag = True


    def clearError(self):
        """
        Clear the error flag.
        """
        self.__error_flag = False


    def Action(self, act):
        """
        Generic state action code.
        """
        self.__state = self.getCurrentState()
        if self.__error_flag == True:
            print "%s.%s() ERROR in state %s" % (self.__machine_name, act, self.__state)
            sim_state_start.sendEvent("Error")
            return

        print "%s.%s() default action implementation invoked in state %s" % (self.__machine_name, act, self.__state)

        
    def Action1(self):
        """
        Implementation Action method for Action1()
        """
        self.Action("Action1")

 
    def Action2(self):
        """
        Implementation Action method for Action2()
        """
        self.Action("Action2")


    def ActionN(self):
        """
        Implementation Action method for ActionN()
        """
        self.Action("ActionN")

        
    def errorHandler(self):
        """
        Implementation errorHandler method for errorHandler()
        """
        printf("Logging error from state: %s and recover in 3 seconds.", self.__state)
        self.__error_flag = False
        time.sleep(3.0)
        # Recover from error and re-execute
        sim_state_start.sendEvent("Recover")


    def showStatus(self):
        """
        Implementation Action method for showStatus()
        """
        print "Status:"
        print "======================"
        print "Current State: %s" % self.__state
        print "Machine Name: %s" % self.__machine_name
        print "Error flag: %s" % self.__error_flag
        print "======================"
