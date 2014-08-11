#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: UserEventTestImpl.py

Date Created:  02-Jun-2010 11:31:49
Created By:    scheng

Python custom-implementation class for functions referenced in
the UserEventTest Statechart model.
"""
# Python imports here
import sys
import logging

# QF imports here
from qf import framework
from qf import event

import UserEventImpl

# Module globals initialized here
LOGGER = logging.getLogger('UserEventTestLogger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class UserEventTestImpl(object):
    """
    UserEventTest state machine implementation object.
    """
    def __init__(self, machine_name="UserEventTest"):
        """
        Constructor
        """
        self.__machine_name = machine_name
        self.fr = framework.QF.getInstance()
        self.activeObjects = self.fr.getActiveDict()
        #
        print "*Accessible: IMPL['UserEventTest']"
        self.set("FUNCTIONALITY", False)
        #
        self.var = None

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

    def isValue(self, arg1, arg2):
        """
        Implementation Guard method for isValue().
        """
        print "%s.isValue() == %s" % (self.__machine_name, arg1 == arg2)
        return arg1 == arg2

    def noUserEventOnInit(self, arg1, arg2):
        """
        Implementation Action method for noUserEventOnInit().
        """
        self.var = arg2
        print "%s.noUserEventOnInit() 'var' originally '%s', setting to '%s'"\
                % (self.__machine_name, arg1, arg2)

    def noUserEvent(self, arg1, e):
        """
        Implementation Action method for noUserEvent().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        print "%s.noUserEvent() received var value '%s' and event signal '%s'"\
                % (self.__machine_name, arg1, e['sType'])

        if self.get("FUNCTIONALITY"):
            print "%s.noUserEvent() no access to user event on entry/exit: '0'"\
                    % self.__machine_name

    def userEventOnTrans(self, e, arg2, arg3):
        """
        Implementation Action method for userEventOnTrans().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        print "%s.userEventOnTrans() received event signal '%s', var value '%s', and literal '%s'"\
                % (self.__machine_name, e['sType'], arg2, arg3)

        if self.get("FUNCTIONALITY"):
            self.var = e['data']
            self.fr.publish(UserEventImpl.DataEvent("TEST_ACK_EV1", arg3))
        else:
            self.var = arg3


    def userEventAvailable(self, arg1, e, arg3):
        """
        Implementation Action method for userEventAvailable().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        print "%s.userEventAvailable() received string '%s', event signal '%s', and var value '%s'"\
                % (self.__machine_name, arg1, e['sType'], arg3)

        if self.get("FUNCTIONALITY"):
            self.var = e['data']
