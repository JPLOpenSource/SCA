#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: ZTestUserEventsImpl.py

Date Created:  02-Jun-2010 15:55:19
Created By:    scheng

Python custom-implementation class for functions referenced in
the ZTestUserEvents Statechart model.
"""
# Python imports here
import sys
import logging

# QF imports here
from qf import framework
from qf import event

import UserEventImpl

# Module globals initialized here
LOGGER = logging.getLogger('ZTestUserEventsLogger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class ZTestUserEventsImpl(object):
    """
    ZTestUserEvents state machine implementation object.
    """
    def __init__(self, machine_name="ZTestUserEvents"):
        """
        Constructor
        """
        self.__machine_name = machine_name
        self.fr = framework.QF.getInstance()
        self.activeObjects = self.fr.getActiveDict()
        #
        print "*Accessible: IMPL['ZTestUserEvents']"
        #
        self.__userData = None

    def isInS2(self):
        """
        Implementation Guard method for isInS2().
        """
        rv = ("S2" == self.fr.getCurrentState(self.activeObjects['UserEventTest'][0]))
        print "%s.isInS2() == %s" % (self.__machine_name, rv)
        return rv


    def init(self):
        """
        Implementation Action method for init().
        """
        pass

    def sendEv1(self, arg1):
        """
        Implementation Action method for sendEv1().
        """
        self.fr.publish(UserEventImpl.DataEvent("Ev1", arg1))

    def sendEv2(self):
        """
        Implementation Action method for sendEv2().
        """
        self.fr.publish(UserEventImpl.DataEvent("Ev2", self.__userData))

    def storeEventData(self, e):
        """
        Implementation Action method for storeEventData().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        self.__userData = e['data']

    def cleanup(self):
        """
        Implementation Action method for cleanup().
        """
        print "%s completed!" % self.__machine_name
