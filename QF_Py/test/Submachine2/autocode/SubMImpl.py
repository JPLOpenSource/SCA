#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: SubMImpl.py

Date Created:  05-Apr-2010 13:44:07
Created By:    scheng

Python custom-implementation class for functions referenced in
the SubM Statechart model.
"""
# Python imports here
import sys
import logging

# QF imports here
from qf import framework
from qf import event


# Module globals initialized here
LOGGER = logging.getLogger('SubMLogger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class SubMImpl(object):
    """
    SubM state machine implementation object.
    """
    def __init__(self, machine_name="SubM"):
        """
        Constructor
        """
        self.__machine_name = machine_name
        self.fr = framework.QF.getInstance()
        self.activeObjects = self.fr.getActiveDict()
        #
        print "*Accessible: IMPL['SubM']"

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

    def takeAction(self, e):
        """
        Implementation Action method for takeAction().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        print "%s.takeAction() default action implementation invoked" % self.__machine_name
