#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: Simple9Impl.py

Date Created:  05-Apr-2010 17:24:57
Created By:    scheng

Python custom-implementation class for functions referenced in
the Simple9 Statechart model.
"""
# Python imports here
import sys
import logging

# QF imports here
from qf import framework
from qf import event


# Module globals initialized here
LOGGER = logging.getLogger('Simple9Logger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class Simple9Impl(object):
    """
    Simple9 state machine implementation object.
    """
    def __init__(self, machine_name="Simple9"):
        """
        Constructor
        """
        self.__machine_name = machine_name
        self.fr = framework.QF.getInstance()
        self.activeObjects = self.fr.getActiveDict()
        #
        # literals accessed by Active class
        self.OPCODE = "OPC1"
        self.OPCODE2 = "OPC2"
        #
        print "*Accessible: IMPL['Simple9']"

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

    def action1(self, e, arg1, arg2, arg3):
        """
        Implementation Action method for action1().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        print "%s.action1() invoked with args (%s:%s, %s:%s, %s:%s)" %\
                (self.__machine_name, arg1, type(arg1), arg2, type(arg2), arg3, type(arg3))

    def action2(self, e, arg1, arg2):
        """
        Implementation Action method for action2().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        print "%s.action2() invoked with args (%s:%s, %s:%s)" %\
                (self.__machine_name, arg1, type(arg1), arg2, type(arg2))
