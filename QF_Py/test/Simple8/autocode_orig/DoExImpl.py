#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: DoExImpl.py

Date Created:  22-Dec-2009 20:27:33
Created By:    reder

Python custom-implementation class for functions referenced in
the DoEx Statechart model.
"""
# Python imports here
import sys
import logging
import time

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
    def __init__(self, machine_name="DoEx"):
        """
        Constructor
        """
        self.__machine_name = machine_name
        #
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
        Gets the boolean value of an attribute
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
        print "%s.s1Entry() default action implementation invoked" % self.__machine_name
        
    def s1Do(self):
        """
        Implementation Action method for s1Do()
        """
        print "%s.s1Do() default action implementation invoked" % self.__machine_name
        #
        # Owen I of course added this user code to test the thread execution.
        for i in range(10):
            time.sleep(1.0)
            printf("Running thread iteration %d\n", i)

    def s1Exit(self):
        """
        Implementation Action method for s1Exit()
        """
        print "%s.s1Exit() default action implementation invoked" % self.__machine_name
