#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: Simple1Impl.py

Date Created:  30-Nov-2009 17:13:19
Created By:    reder

Python custom-implementation class for functions referenced in
the Simple1 Statechart model.
"""
# Python imports here
import sys
import logging

# QF imports here
from qf import event
from qf import framework

# Module globals initialized here
LOGGER = logging.getLogger('Simple1Logger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class Simple1Impl(object):
    """
    Simple1 state machine implementation object.
    """
    def __init__(self):
        """
        Constructor
        """
        print "*Accessible: IMPL['Simple1']"

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

    def dumpActiveObj(self):
        """
        Implementation Action method for dumpActiveObj()
        """
        adict = framework.QF.getInstance().getActiveDict()
        print "Dump active objects:"
        for sm in adict:
            print "%s: %s" % (sm, adict[sm][0])



