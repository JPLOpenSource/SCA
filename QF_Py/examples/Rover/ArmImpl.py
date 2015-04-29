#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: ArmImpl.py

Date Created:  25-Mar-2010 10:56:24
Created By:    watney

Python custom-implementation class for functions referenced in
the Arm Statechart model.
"""
# Python imports here
import sys
import logging

# QF imports here
from qf import event
from qf import framework


# Module globals initialized here
LOGGER = logging.getLogger('ArmLogger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class ArmImpl(object):
    """
    Arm state machine implementation object.
    """
    def __init__(self, machine_name="Arm"):
        """
        Constructor
        """
        self.__machine_name = machine_name
        #
        print "*Accessible: IMPL['Arm']"
        self.set('roverStationary', False)
        self.fr = framework.QF.getInstance()
        self.activeObjects = self.fr.getActiveDict()


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

    def roverStationary(self, e):
        """
        Implementation Guard method for roverStationary()
        """
        return (self.activeObjects['Rover'][0].getCurrentState() != 'Driving')

