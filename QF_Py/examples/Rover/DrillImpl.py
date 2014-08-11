#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: DrillImpl.py

Date Created:  07-Apr-2010 00:20:37
Created By:    reder

Python custom-implementation class for functions referenced in
the Drill Statechart model.
"""
# Python imports here
import sys
import logging

# QF imports here
from qf import framework
from qf import event


# Module globals initialized here
LOGGER = logging.getLogger('DrillLogger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class DrillImpl(object):
    """
    Drill state machine implementation object.
    """
    def __init__(self, machine_name="Drill"):
        """
        Constructor
        """
        self.__machine_name = machine_name
        self.fr = framework.QF.getInstance()
        self.activeObjects = self.fr.getActiveDict()
        #
        print "*Accessible: IMPL['Drill']"
        self.set('armStowed', False)

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

    def armStowed(self, e):
        """
        Implementation Guard method for armStowed().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        print "%s.armStowed() == %s" % (self.__machine_name, str(self.get('armStowed')))
        return (self.activeObjects['Arm'][0].getCurrentState() != 'Stowed')
