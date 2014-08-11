#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: DoExImpl.py

Date Created:  22-Dec-2009 08:51:33
Created By:    watney

Python custom-implementation class for functions referenced in
the DoEx Statechart model.
"""
# Python imports here
import sys
import logging

# QF imports here
from qf import event


# Module globals initialized here
LOGGER = logging.getLogger('DoExLogger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class DoExImpl(object):
    """
    DoEx state machine implementation object.
    """
    def __init__(self):
        """
        Constructor
        """
        print "*Accessible: IMPL['DoEx']"

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

    def s1Entry(self):
        """
        Implementation Action method for s1Entry()
        """
        pass

    def s1Exit(self):
        """
        Implementation Action method for s1Exit()
        """
        pass


    def s1Do(self):
        """
        Implementation Action method for s1Do()
        """
        print "In s1 Do"
