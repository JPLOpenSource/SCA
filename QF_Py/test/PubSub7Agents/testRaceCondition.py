'''
Created on Nov 10, 2009

Bombards the 7-agents pub-sub State Machines with events and checks for
the expected active states at each step.

NOTE!  This script should be run from unittest; it can also be run using
execfile from Python prompt, but it will NOT run standalone.

@author: scheng
'''
import sys
import time

try:
    from qf import framework
except:
    print "\nUSAGE: Run sim_state_start.py;\n" +\
        "       then invoke execfile('%s') in Python prompt.\n" % sys.argv[0]

qf = framework.QF.getInstance()

# testing constants
NUM_TRIES = 20
NUM_RUNS = 2000


def count(items, valToCheck):
    tally = 0
    for x in items:
        if x == valToCheck:
            tally += 1
    return tally


def checkAllAgentsAndCoordinator(expectedState):
    """
    Checks to see if all 7 Agents AND coordinator are in the desired state
    """
    activated = dict()
    for n in range(NUM_TRIES):  # give up to X chances
        for ai in range(8):
            sm = qf.getActive(ai)
            activated[sm] = qf.getCurrentState(sm)
        if count(activated.values(), expectedState) == 8:  # great!
            return True
        # otherwise, sleep a bit
        time.sleep(0.25)

    # At this point, we have failed to find all machines in desired states!
    print "ERR! Race condition occurred again! Expected: %s, bad machines:" % expectedState
    for ai in range(8):
        sm = qf.getActive(ai)
        if activated[sm] != expectedState:
            print "    * '%s' in state '%s'" % (sm, activated[sm])
    return False


def run():
    """
    Test: send activate event, await desired state; terminate & await; repeat
    """
    for i in range(1,NUM_RUNS+1): # run N times
        print "\n    =-=-=-=-= Run %s =-=-=-=-=\n" % i
        # publish the activate event
        qf.sendEvent("Activate")
        if not checkAllAgentsAndCoordinator('Active'):
            print "FAILED! Event Pair Test: 'Agents_Activate' on run #%s!" % i
            return False
        # publish the terminate event
        qf.sendEvent("Terminate")
        if not checkAllAgentsAndCoordinator('Inactive'):
            print "FAILED! Event Pair Test: 'Terminate' on run #%s!" % i
            return False
    return True

if __name__ == '__main__':
    print '=' * 79
    print "======================== Test: Alternating event pair ========================="
    print '=' * 79
    if run():
        print "PASSED!\n"
