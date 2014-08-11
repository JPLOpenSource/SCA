#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: SubMachine2Impl.py

Date Created:  05-Apr-2010 13:44:07
Created By:    scheng

Python custom-implementation class for functions referenced in
the SubMachine2 Statechart model.
"""
# Python imports here
import sys
import logging

# QF imports here
from qf import framework
from qf import event

# Submachine imports here
from SubMImpl import *


# Module globals initialized here
LOGGER = logging.getLogger('SubMachine2Logger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class SubMachine2Impl(object):
    """
    SubMachine2 state machine implementation object.
    """
    def __init__(self, machine_name="SubMachine2"):
        """
        Constructor
        """
        self.__machine_name = machine_name
        self.fr = framework.QF.getInstance()
        self.activeObjects = self.fr.getActiveDict()
        #
        print "*Accessible: IMPL['SubMachine2']"

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
        Gets the boolean value of an attribute
        """
        return self.__dict__['_'+attrname]

    def clear(self, attrname):
        """
        Clears the value of an attribute to False
        """
        self.__dict__['_'+attrname] = False



class S1_SubMImpl(SubMImpl):
    """
    S1_SubMImpl SubMachine implementation override object.

    Override the action methods of the SubMachine individually if custom
    behavior is desired.
    """
    def __init__(self, machine_name="S1:SubM"):
        """
        Override constructor
        """
        SubMImpl.__init__(self, machine_name)
        self.__machine_name = machine_name

    def takeAction(self, e):
        """
        Override implementation Action method for takeAction().
        """
        print "%s.takeAction() overridden action implementation invoked" % self.__machine_name


class S2_SubMImpl(SubMImpl):
    """
    S2_SubMImpl SubMachine implementation override object.

    Override the action methods of the SubMachine individually if custom
    behavior is desired.
    """
    def __init__(self, machine_name="S2:SubM"):
        """
        Override constructor
        """
        SubMImpl.__init__(self, machine_name)
        self.__machine_name = machine_name

    def takeAction(self, e):
        """
        Override implementation Action method for takeAction().
        """
        print "%s.takeAction() overridden action implementation invoked" % self.__machine_name
