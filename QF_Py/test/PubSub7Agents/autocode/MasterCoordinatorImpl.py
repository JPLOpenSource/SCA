#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: MasterCoordinatorImpl.py

Date Created:  09-Nov-2009 17:23:47
Created By:    scheng

Python custom-implementation class for functions referenced in
the MasterCoordinator Statechart model.
"""
# Python imports here
import sys
import logging

# QF imports here
from qf import event
from qf import framework


# Module globals initialized here
LOGGER = logging.getLogger('MasterCoordinatorLogger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class MasterCoordinatorImpl(object):
    """
    MasterCoordinator state machine implementation object.
    """
    def __init__(self):
        """
        Constructor
        """
        print "*Accessible: IMPL['MasterCoordinator']"
        self.MAX_AGENTS = 7
        self._agentsActive = 0

    def set(self, attr, value=True):
        """
        Sets the boolean value of an attribute, default to True
        """
        if type(value) == bool:
            self.__dict__['_'+attr] = value
            print attr, "set to", value
        else:
            print 'Value must be a bool, either "True" or "False" (no quotes)!'

    def get(self, attrname):
        """
        Gets the value of an attribute
        """
        return self.__dict__['_'+attrname]

    def clear(self, attrname):
        """
        Clears the value of an attribute to False
        """
        self.__dict__['_'+attrname] = False

    def doClearTally(self, e):
        """
        Implementation Action method for doClearTally()
        """
        self._agentsActive = 0

    def doTallyAgent(self, e):
        """
        Implementation Action method for doTallyAgent()
        """
        self._agentsActive += 1
        print "Active agents:", self._agentsActive
        # announce when agents active
        if self._agentsActive == self.MAX_AGENTS:
            framework.QF.getInstance().publish(event.Event("AllAgentsTallied"))
