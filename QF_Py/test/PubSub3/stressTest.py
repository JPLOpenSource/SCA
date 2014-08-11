'''
Created on Nov 12, 2009

Bombards the 3-slave pub-sub State Machines with a slew of tick events and
checks for the expected active states at each step.

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
NUM_TRIES = 10
NUM_RUNS = 2000
# Integer "constants" for the StateMachines
I_MASTER = qf.getIndexOfName("Master")
I_SLAVE1 = qf.getIndexOfName("Slave1")
I_SLAVE2 = qf.getIndexOfName("Slave2")
I_SLAVE3 = qf.getIndexOfName("Slave3")


def expectState(irun, ai, expectedState):
    """
    Checks to see Active state of StateMachine ai is in the desired state.
    """
    sm = qf.getActive(ai)
    for n in range(NUM_TRIES):  # give up to X chances
        if qf.getCurrentState(sm) == expectedState:  # great!
            return True
        # otherwise, sleep a bit
        time.sleep(0.5)

    # Ahh, at this point, failed to see desired state!
    print "FAILED! Tick Stress Test; %s NOT in '%s' on run #%s!" % (sm, expectedState, irun)
    return False


def run():
    """
    Test: arm; then repeat sending tick and await desired states; disarm
    """
    # Arm the master
    qf.sendEvent("Arm")

    # Stress test with Ticks!
    for i in range(1,NUM_RUNS+1):  # run N times
        print "\n    =-=-=-=-= Run %s =-=-=-=-=\n" % i
        # Cycle of 3 slave ticks begins
        if not expectState(i, I_MASTER, 's21'): return False
        if not expectState(i, I_SLAVE1, 'Set'): return False
        # 1st tick!
        qf.sendEvent("tick")
        if not expectState(i, I_MASTER, 's22'): return False
        if not expectState(i, I_SLAVE3, 'Reset'): return False
        if not expectState(i, I_SLAVE2, 'Set'): return False
        # 2nd tick!
        qf.sendEvent("tick")
        if not expectState(i, I_MASTER, 's23'): return False
        if not expectState(i, I_SLAVE1, 'Reset'): return False
        if not expectState(i, I_SLAVE3, 'Set'): return False
        # 3rd tick!
        qf.sendEvent("tick")
        if not expectState(i, I_SLAVE2, 'Reset'): return False

    # Reset the master
    qf.sendEvent("Reset")
    if not expectState(0, I_MASTER, "s1"): return False

    # At this point, all good, test passed!
    return True


if __name__ == '__main__':
    print '=' * 79
    print "========================== Test: slew of tick events =========================="
    print '=' * 79
    if run():
        print "PASSED!\n"
