#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: CalculatorImpl.py

Date Created:  16-Apr-2010 09:54:38
Created By:    scheng

Python custom-implementation class for functions referenced in
the Calculator Statechart model.
"""
# Python imports here
import sys
import logging

# QF imports here
from qf import framework
from qf import event

# Submachine imports here
from OperandXImpl import *

from UserEventImpl import ResultEvent


# Module globals initialized here
LOGGER = logging.getLogger('CalculatorLogger')


def printf(format, *args):
    # sys.stdout.write(format % args)
    LOGGER.info(format % args)


class CalculatorImpl(object):
    """
    Calculator state machine implementation object.
    """
    def __init__(self, machine_name="Calculator"):
        """
        Constructor
        """
        self.__machine_name = machine_name
        self.fr = framework.QF.getInstance()
        self.activeObjects = self.fr.getActiveDict()
        #
        print "*Accessible: IMPL['Calculator']"
        self.set("isKeyId", False)  # for testing, not used for calculation
        self.set("onError", False)  # for testing, not used for calculation
        #
        # initialize the computation data
        self.clearAll(None)

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

    def isKeyId(self, e, arg1):
        """
        Implementation Guard method for isKeyId().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        if 'keyId' in e.keys():
            rv = (e['keyId'] == arg1)
            print "%s.isKeyId() == %s" % (self.__machine_name, str(rv))
            return rv
        else:  # unit-testing code
            print "%s.isKeyId() == %s" % (self.__machine_name, self.get("isKeyId"))
            return self.get("isKeyId")

    def onError(self, e):
        """
        Implementation Guard method for onError().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        if 'keyId' in e.keys():
            print "%s.onError() == %s" % (self.__machine_name, str(self.__errStat))
            if self.__errStat is not None:
                self.fr.publish(ResultEvent(self.__errStat))
            return self.__errStat is not None
        else:  # unit-testing code
            print "%s.onError() == %s" % (self.__machine_name, self.get("onError"))
            return self.get("onError")

    def ce(self, e):
        """
        Implementation Action method for ce().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        
        Unsets the last operand.
        """
        self.__operand = ""

    def clearAll(self, e):
        """
        Implementation Action method for clearAll().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        
        Reset the computation data.
        """
        self.__operand = ""  # last operand entered
        self.__oper = ""     # last operation pressed
        self.__expr = ""     # expression accumulated so far
        self.__result = 0    # evaluation result of expression
        self.__errStat = None

    def negate(self, e):
        """
        Implementation Action method for negate().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        
        Save the negative sign.
        """
        self.__operand = "-"

    def updateOperand(self, e):
        """
        Implementation Action method for updateOperand().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        
        Update the operand string.
        """
        if 'keyId' in e.keys():
            self.__operand += e['keyId']
            print "%s.updateOperand() resulted in operand '%s'" % (self.__machine_name, self.__operand)

    def op(self, e):
        """
        Implementation Action method for op().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        
        By this point, the operand should be ready for use.
        If it's empty, use the result, else it's an error!
        If an operand is available, move to expression and
        clear the operand for the next entry.
        """
        if 'keyId' in e.keys():
            self.__oper = e['keyId']
            if self.__operand == "":
                if self.__result == "":
                    self.__errStat = "ERR: no operand to compute with for %s!" % self.__oper
                else:
                    self.__operand = str(self.__result)
            self.__expr = self.__operand + self.__oper
            self.__operand = ""
            print "%s.op() resulted in expression '%s'" % (self.__machine_name, self.__expr)

    def compute(self, e):
        """
        Implementation Action method for compute().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        
        Computes the expression and store result, also store as operand.
        If compute was called due to an operand, then we need to update
        the expression as if op() was invoked.
        """
        # complete the expression with the last operand
        if 'keyId' not in e.keys():
            return;
        #
        try:
            if self.__oper == '/':  # special handling of division
                # make second operand a float to force type conversion
                self.__expr += str(float(self.__operand))
                self.__result = eval(self.__expr)
                # see if result is an integer, if so, store just integer
                if self.__result == int(self.__result):
                    self.__result = int(self.__result)
            else:  # straightforward evaluation
                self.__expr += self.__operand
                self.__result = eval(self.__expr)
            # print result
            print "%s.compute() result: %s" % (self.__machine_name, str(self.__result))
            self.__errStat = None
        except Exception, info:
            print "ERROR computing result! %s" % repr(info)
            self.__errStat = repr(info)
        #
        # clear operand and expr
        self.__operand = ""
        self.__oper = ""
        self.__expr = ""
        if e['keyId'] != '=':  # use result in next operation
            self.op(e)

    def reportResult(self, e):
        """
        Implementation Action method for compute().
        The triggering event is supplied as a dictionary object 'e', so member
        data "data1" of the original Event object is accessible as e['data1'] .
        """
        self.fr.publish(ResultEvent(self.__result))


class operand1_OperandXImpl(OperandXImpl):
    """
    operand1_OperandXImpl SubMachine implementation override object.

    Override the action methods of the SubMachine individually if custom
    behavior is desired.
    """
    def __init__(self, machine_name="operand1:OperandX"):
        """
        Override constructor
        """
        OperandXImpl.__init__(self, machine_name)
        self.__machine_name = machine_name


class operand2_OperandXImpl(OperandXImpl):
    """
    operand2_OperandXImpl SubMachine implementation override object.

    Override the action methods of the SubMachine individually if custom
    behavior is desired.
    """
    def __init__(self, machine_name="operand2:OperandX"):
        """
        Override constructor
        """
        OperandXImpl.__init__(self, machine_name)
        self.__machine_name = machine_name
