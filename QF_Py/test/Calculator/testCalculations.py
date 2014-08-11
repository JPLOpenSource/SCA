'''
Created on Apr 16, 2010

Runs the Calculator to check a series of calculations.

NOTE!  This script should be run from unittest; it can also be run using
execfile from Python prompt, but it will NOT run standalone.

@author: scheng
'''
import re
import sys
import time

try:
    from qf import framework
    from qf import event
except:
    print "\nUSAGE: Run sim_state_start.py;\n" +\
        "       then invoke execfile('%s') in Python prompt.\n" % sys.argv[0]
sys.path.append("autocode")
from UserEventImpl import KeyEvent
sys.path.remove("autocode")


qf = framework.QF.getInstance()

# The series of calculations to test
CALC_TESTS = [
{ "1+1" : 2 },
{ "0-0" : 0 },
{ "5*5" : 25 },
{ "4/2" : 2 },
{ "7+1*5-2" : 38 },
{ "1/0" : "ERR" },
{ "1+4*5/4+-.25" : 6 },
{ "-1.125*-8" : 9 },
{ "1+1=*9=+1/3-.333333333333" : 6.0 },
{ "1+2+3+4+5+6+7+8+9+10" : 55 },
{ "0-1-2-3-4-5-6-7-8-9-10" : -55 },
{ "1*2*3*4*5*6*7*8*9*10" : 3628800 },
{ "+*-/-52+64=0--0+1.2--888+.2*-.0987/-4" : 21.945945 }
]


class ResultEventDummyActive:
    def __init__(self):
        self.clearEvent()

    def enqueueEvent(self, e):
        self.__event = e
        self.__hasEvent = True

    def getResultEvent(self):
        return self.__event

    def hasEvent(self):
        return self.__hasEvent

    def clearEvent(self):
        self.__event = None
        self.__hasEvent = False


def waitState(sm, states, timeout=3):
    """
    Waits until StateMachine is in one of the given list of states,
    or until timeout expires (if > 0).  Returns whether one of the
    states waited upon is true.
    """
    gotState = False
    startTime = time.time()
    while not gotState:
        curState = qf.getCurrentState(sm)
        for s in states:
            if re.match(s, curState):
                gotState = True
        if not gotState:
            time.sleep(0.100)  # 100 ms
        if timeout > 0 and (time.time() - startTime) > timeout:
            break
    return gotState

def sendToCalc(sm, expr):
    for char in expr+"=":
        if char == '+':
            waitState(sm, ["operand[12]"])
            qf.publish(event.Event("B_OP_PLUS"))
            waitState(sm, ["OpEntered", "Error"])
        elif char == '-':
            waitState(sm, ["operand[12]"])
            qf.publish(event.Event("B_OP_MINUS"))
            waitState(sm, ["OpEntered", "Error"])
        elif char == '*':
            waitState(sm, ["operand[12]"])
            qf.publish(event.Event("B_OP_TIMES"))
            waitState(sm, ["OpEntered", "Error"])
        elif char == '/':
            waitState(sm, ["operand[12]"])
            qf.publish(event.Event("B_OP_DIVIDE"))
            waitState(sm, ["OpEntered", "Error"])
        elif char == '.':
            qf.publish(event.Event("B_DOT"))
        elif char == '=':
            waitState(sm, ["operand2"])
            qf.publish(event.Event("B_EQUAL"))
            waitState(sm, ["Result", "Error"])
        elif char >= '0' and char <= '9':
            qf.publish(event.Event("B_%s" % char))
        else:
            print "ERR! Unrecognized character: '%s'" % char


def run():
    """
    Test: a series of Calculations using the complex Calculator StateMachines
    """
    # Register with QF to get error event
    eventListener = ResultEventDummyActive()
    qf.addSignalQueue("ReportResult", eventListener)
    #
    # Get state machine
    sm = qf.getActiveDict()['Calculator'][0]
    #
    wrongResults = 0
    print "Starting calculations..."
    for t in CALC_TESTS:
        for k in t.keys():
            expr = k
            expResult = t[expr]
            result = None
            #
            # Clear calculator and reset the event listener
            qf.publish(KeyEvent("B_C"))
            eventListener.clearEvent()
            # Submit calculations
            sendToCalc(sm, expr)
            # Wait until Result or Error state to read a ResultEvent
            startTime = time.time()
            while not (waitState(sm, ["Result", "Error"]) and\
                       eventListener.hasEvent()):
                time.sleep(0.1)
                if time.time() - startTime > 10:  # time out after 10 secs
                    break
            #
            print
            if eventListener.hasEvent():
                result = eventListener.getResultEvent().result
                if type(result) == type(""):  # an error!
                    if len(expResult) > 0 and expResult != "ERR":
                        print "xxx Computation result NOT err! %s" % result
                        wrongResults += 1
                    else:
                        print "+++ Computation error as expected: %s" % result
                else:
                    if abs(result - expResult) < 0.0001:  # allow 0.01% error
                        print "+++ Correct computation result: %s" % result
                    else:
                        print "xxx Wrong computation result! %s" % result
                        wrongResults += 1
            else:
                print "*** NO RESULT!"
                wrongResults += 1
            print
    #
    return wrongResults


if __name__ == '__main__':
    print '=' * 79
    print "===== Calculator: Calculator functionality using entry-/exitPoints ====="
    print '=' * 79
    if run():
        print "PASSED!\n"
