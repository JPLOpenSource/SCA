#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: UserEventTestAutocoder21Impl.py

Date Created:  02-Jun-2010 10:23:26
Created By:    scheng

Python custom-implementation class for functions referenced in
the UserEventTestAutocoder21 Statechart model.
"""
# Python imports here
import sys
import logging

# QF imports here
from qf import framework
from qf import event


# Module globals initialized here
LOGGER = logging.getLogger('UserEventTestAutocoder21Logger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class UserEventTestAutocoder21Impl(object):
    """
    UserEventTestAutocoder21 state machine implementation object.
    """
    def __init__(self, machine_name="UserEventTestAutocoder21"):
        """
        Constructor
        """
        self.__machine_name = machine_name
        self.fr = framework.QF.getInstance()
        self.activeObjects = self.fr.getActiveDict()
        #
        self.var = None

    def isValue(self, e, arg1, arg2):
        """
        Implementation Guard method for isValue().
        """
        print "%s.isValue() == %s" % (self.__machine_name, arg1 == arg2)
        return arg1 == arg2

    def noUserEventOnInit(self, e, arg1, arg2):
        """
        Implementation Action method for noUserEventOnInit().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        self.var = arg2
        print "%s.noUserEventOnInit() 'var' originally '%s', setting to '%s'"\
                % (self.__machine_name, arg1, arg2)

    def noUserEvent(self, e, arg1):
        """
        Implementation Action method for noUserEvent().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        print "%s.noUserEvent() received var value '%s' and event signal '%s'"\
                % (self.__machine_name, arg1, e['sType'])

    def userEventOnTrans(self, e, arg2, arg3):
        """
        Implementation Action method for userEventOnTrans().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        self.var = arg3
        print "%s.userEventOnTrans() received event signal '%s', var value '%s', and literal '%s'"\
                % (self.__machine_name, e['sType'], arg2, arg3)

    def userEventAvailable(self, e, arg1, arg3):
        """
        Implementation Action method for userEventAvailable().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        print "%s.userEventAvailable() received string '%s', event signal '%s', and var value '%s'"\
                % (self.__machine_name, arg1, e['sType'], arg3)
