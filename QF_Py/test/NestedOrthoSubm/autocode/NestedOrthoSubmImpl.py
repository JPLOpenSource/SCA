#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: NestedOrthoSubmImpl.py

Date Created:  24-Jun-2010 16:22:02
Created By:    scheng

Python custom-implementation class for functions referenced in
the NestedOrthoSubm Statechart model.
"""
# Python imports here
import sys
import logging
import time

# QF imports here
from qf import framework
from qf import event

# Submachine imports here


# Module globals initialized here
LOGGER = logging.getLogger('NestedOrthoSubmLogger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class NestedOrthoSubmImpl(object):
    """
    NestedOrthoSubm state machine implementation object.
    """
    def __init__(self, machine_name="NestedOrthoSubm"):
        """
        Constructor
        """
        self.__machine_name = machine_name
        self.fr = framework.QF.getInstance()
        self.activeObjects = self.fr.getActiveDict()
        #
        # guards default to False

    def doWorkNested2a1(self):
        """
        Implementation Action method for doWorkNested2a1().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        LOGGER.info("%s.doWorkNested2a1() user impl-action invoked" % self.__machine_name)
        #
        # user code to test the thread execution.
        for i in range(5):
            time.sleep(0.25)
            printf("Running doWorkNested2a1 thread iteration %d\n", i)

    def doWorkNested2b1(self):
        """
        Implementation Action method for doWorkNested2b1().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        LOGGER.info("%s.doWorkNested2b1() user impl-action invoked" % self.__machine_name)
        #
        # user code to test the thread execution.
        for i in range(5):
            time.sleep(0.25)
            printf("Running doWorkNested2b1 thread iteration %d\n", i)

    def onEntryNested2a1(self):
        """
        Implementation Action method for onEntryNested2a1().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        LOGGER.info("%s.onEntryNested2a1() default action implementation invoked" % self.__machine_name)

    def onEntryNested2b1(self):
        """
        Implementation Action method for onEntryNested2b1().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        LOGGER.info("%s.onEntryNested2b1() default action implementation invoked" % self.__machine_name)

