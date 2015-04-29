#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: KeyboardImpl.py

Date Created:  15-Apr-2010 18:29:03
Created By:    scheng

Python custom-implementation class for functions referenced in
the Keyboard Statechart model.
"""
# Python imports here
import sys
import logging

# QF imports here
from qf import framework
from qf import event
from UserEventImpl import KeyEvent

# Module globals initialized here
LOGGER = logging.getLogger('KeyboardLogger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class KeyboardImpl(object):
    """
    Keyboard state machine implementation object.
    """
    def __init__(self, machine_name="Keyboard"):
        """
        Constructor
        """
        self.__machine_name = machine_name
        self.fr = framework.QF.getInstance()
        self.activeObjects = self.fr.getActiveDict()
        #
        print "*Accessible: IMPL['Keyboard']"

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

    def clearAll(self, e):
        """
        Implementation Action method for clearAll().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        self.fr.publish(KeyEvent("Clear"))

    def clearEntry(self, e):
        """
        Implementation Action method for clearError().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        self.fr.publish(KeyEvent("ClearEntry"))

    def powerOff(self, e):
        """
        Implementation Action method for powerOff().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        self.fr.publish(KeyEvent("PowerOff"))

    def sendEquals(self, e):
        """
        Implementation Action method for sendEquals().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        self.fr.publish(KeyEvent("Equals", '='))

    def sendKey(self, e, arg1):
        """
        Implementation Action method for sendKey().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        if arg1 == '0':
            self.fr.publish(KeyEvent("Digit_0", arg1))
        else:
            self.fr.publish(KeyEvent("Digit_1_9", arg1))

    def sendPoint(self, e):
        """
        Implementation Action method for sendPoint().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        self.fr.publish(KeyEvent("Point", '.'))

    def sendOperator(self, e, arg1):
        """
        Implementation Action method for sendOperator().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        self.fr.publish(KeyEvent("Oper", arg1))
