#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: OperandXImpl.py

Date Created:  16-Apr-2010 09:54:38
Created By:    scheng

Python custom-implementation class for functions referenced in
the OperandX Statechart model.
"""
# Python imports here
import sys
import logging

# QF imports here
from qf import framework
from qf import event
from UserEventImpl import KeyEvent

# Module globals initialized here
LOGGER = logging.getLogger('OperandXLogger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class OperandXImpl(object):
    """
    OperandX state machine implementation object.
    """
    def __init__(self, machine_name="OperandX"):
        """
        Constructor
        """
        self.__machine_name = machine_name
        self.fr = framework.QF.getInstance()
        self.activeObjects = self.fr.getActiveDict()
        #
        print "*Accessible: IMPL['OperandX']"

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

    def fraction(self, e):
        """
        Implementation Action method for fraction().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        if 'keyId' in e.keys():
            self.fr.publish(KeyEvent("OperandChanged", e['keyId']))

    def insert(self, e):
        """
        Implementation Action method for insert().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        if 'keyId' in e.keys():
            self.fr.publish(KeyEvent("OperandChanged", e['keyId']))
