#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: Driving3Impl.py

Date Created:  29-Apr-2012 01:46:19
Created By:    reder

Python custom-implementation class for functions referenced in
the Driving3 Statechart model.
"""
# Python imports here
import sys
import logging

# QF imports here
from qf import framework
from qf import event


# Module globals initialized here
LOGGER = logging.getLogger('Driving3Logger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class Driving3Impl(object):
    """
    Driving3 state machine implementation object.
    """
    def __init__(self, machine_name="Driving3"):
        """
        Constructor
        """
        self.__machine_name = machine_name
        self.fr = framework.QF.getInstance()
        self.activeObjects = self.fr.getActiveDict()
        #
        # guards default to False
        self.set('InReverse', False)
        self.set('UnlockBrake', False)

    def set(self, attr, value=True):
        """
        Sets the boolean value of an attribute, default to True
        """
        if type(value) == bool:
            self.__dict__['_'+attr] = value
            LOGGER.info("%s set to %s" % (attr, repr(value)))
        else:
            LOGGER.error('Value must be a bool, either "True" or "False" (no quotes)!')

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

    def InReverse(self):
        """
        Implementation Guard method for InReverse().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        LOGGER.info("%s.InReverse() == %s" % (self.__machine_name, str(self.get('InReverse'))))
        return self.get('InReverse')

    def UnlockBrake(self):
        """
        Implementation Guard method for UnlockBrake().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        LOGGER.info("%s.UnlockBrake() == %s" % (self.__machine_name, str(self.get('UnlockBrake'))))
        return self.get('UnlockBrake')

    def BackupLightsOff(self):
        """
        Implementation Action method for BackupLightsOff().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        self.fr.sendEvent("backUpOff")
        LOGGER.info("%s.BackupLightsOff() turn lights off" % self.__machine_name)

    def BackupLightsOn(self):
        """
        Implementation Action method for BackupLightsOn().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        self.fr.sendEvent("backUpOn")
        LOGGER.info("%s.BackupLightsOn() turn lights on" % self.__machine_name)

    def Lock(self):
        """
        Implementation Action method for Lock().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        self.clear("UnlockBrake")
        LOGGER.info("%s.UnLock() default action clear UnlockBrake guard" % self.__machine_name)


    def NotReverse(self):
        """
        Implementation Action method for NotReverse().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        self.clear("InReverse")
        LOGGER.info("%s.Reverse() default action clear InReverse guard" % self.__machine_name)

    def Reverse(self):
        """
        Implementation Action method for Reverse().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        self.set("InReverse")
        LOGGER.info("%s.Reverse() default action set InReverse guard" % self.__machine_name)

    def UnLock(self):
        """
        Implementation Action method for UnLock().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        self.set("UnlockBrake")
        LOGGER.info("%s.UnLock() default action set UnlockBrake guard" % self.__machine_name)
