#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: DemoSMImpl.py

Date Created:  23-Aug-2010 16:01:48
Created By:    scheng

Python custom-implementation class for functions referenced in
the DemoSM Statechart model.
"""
# Python imports here
import sys
import logging

# QF imports here
from qf import framework
from qf import event


# Module globals initialized here
LOGGER = logging.getLogger('DemoSMLogger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class SystemOnEvent(event.Event):
    def __init__(self, sig, data):
        event.Event.__init__(self, sig)
        self.data = data


class DemoSMImpl(object):
    """
    DemoSM state machine implementation object.
    """
    def __init__(self, machine_name="DemoSM"):
        """
        Constructor
        """
        self.__machine_name = machine_name
        self.fr = framework.QF.getInstance()
        self.activeObjects = self.fr.getActiveDict()

    def handleOnMode(self, e):
        """
        Handles the SystemOn event, outputting the received event data.

        Implementation Action method for handleOnMode().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        LOGGER.info("%s.handleOnMode() handling SystemOn, signal and data sent: %s, %s" % (self.__machine_name, e['signal'], e['data']))

    def processOnSignal(self, e):
        """
        processes the OnSignal, sending out a SystemOn info event.

        Implementation Action method for processOnSignal().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        
        If there is no data, then will proceed normally.
        """
        found_data = False
        for data in e.keys():
            if data == 'data':
                found_data = True
                break
        if found_data == False: # If data entry not found, then set data as None.
            e['data'] = None
            
        LOGGER.info("%s.processOnSignal() got signal '%s' and data '%s'; responding with info" % (self.__machine_name, e['signal'], e['data']))
        self.fr.publish(SystemOnEvent("SystemOn", "%s info" % self.__machine_name))

