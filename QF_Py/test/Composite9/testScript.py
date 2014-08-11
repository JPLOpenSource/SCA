'''
Created on Dec 02, 2009

Tests Composite9 for timer event used within orthogonal regions, interacting
with two other state machines.

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
# Integer "constants" indexing the StateMachines
I_MASTER = qf.getIndexOfName("Composite9")
I_AGENT1 = qf.getIndexOfName("Agent1")
I_AGENT2 = qf.getIndexOfName("Agent2")


def expectState(ai, expectedState, subStates=None):
    """
    Checks to see if StateMachine ai is in the desired state.
    Look at orthogonal region substates if Master.Active
    """
    isExpected = False
    sm = qf.getActive(ai)
    for i in range(NUM_TRIES):  # give up to X chances
        if qf.getCurrentState(sm) == expectedState:  # great!
            isExpected = True
            break
        # otherwise, sleep a bit
        time.sleep(0.4)

    if isExpected:
        # check orthogonal region substates
        regionsExpected = False
        if ai == I_MASTER and expectedState == "Active":
            for i in range(NUM_TRIES):
                numOk = 0
                for r in range(1,4):  # regions 1 to 3
                    regionStr = "qf.getActive(I_MASTER)._Composite9Active__region%d" % r
                    print "> Active state in region %d: %s" % (r, qf.getCurrentState(eval(regionStr)))
                    if qf.getCurrentState(eval(regionStr)) == subStates[r-1]:
                        numOk += 1
                if numOk == len(subStates):
                    regionsExpected = True
                    break
                # otherwise, sleep a bit
                time.sleep(0.4)
            #
            # check outcome from checking orthogonal regions
            if not regionsExpected:
                print "FAILED! Composite9; regions NOT in %s!" % subStates
                isExpected = False
    else:
        # Ahh, at this point, failed to see desired state!
        print "FAILED! Composite9; %s NOT in '%s'!" % (sm, expectedState)

    return isExpected


def run():
    """
    Test: arm; then repeat sending tick and await desired states; disarm
    """
    # Initialized states
    if not expectState(I_MASTER, "Inert"): return False
    if not expectState(I_AGENT1, "Inactive"): return False
    if not expectState(I_AGENT2, "Inactive"): return False

    # Arm the master
    qf.sendEvent("Arm")
    if not expectState(I_MASTER, "Active", ["S11", "S14", "S1611"]): return False
    if not expectState(I_AGENT1, "Inactive"): return False
    if not expectState(I_AGENT2, "Inactive"): return False

    # Series of ticks
    # 1st tick!
    qf.sendEvent("tick")
    if not expectState(I_MASTER, "Active", ["S121", "S14", "S1611"]): return False
    if not expectState(I_AGENT1, "Standby"): return False
    if not expectState(I_AGENT2, "Inactive"): return False
    # 2nd tick!
    qf.sendEvent("tick")
    if not expectState(I_MASTER, "Active", ["S13", "S15", "S1611"]): return False
    if not expectState(I_AGENT1, "Active"): return False
    if not expectState(I_AGENT2, "Standby"): return False
    # 3rd tick!
    qf.sendEvent("tick")
    #- Agent2 transitions to Active, then Inactive right away (hope to catch it)
    #if not expectState(I_AGENT2, "Active"): return False
    if not expectState(I_MASTER, "Active", ["S11", "S14", "S17"]): return False
    if not expectState(I_AGENT1, "Inactive"): return False
    if not expectState(I_AGENT2, "Inactive"): return False
    # 4th tick!
    qf.sendEvent("tick")
    if not expectState(I_MASTER, "Active", ["S121", "S14", "S1611"]): return False
    if not expectState(I_AGENT1, "Standby"): return False
    if not expectState(I_AGENT2, "Inactive"): return False
    # 5th tick!
    qf.sendEvent("tick")
    #- Transient state, so may not pass
    #if not expectState(I_MASTER, "Active", ["S122", "S15", "S1611"]): return False
    if not expectState(I_MASTER, "Active", ["S13", "S15", "S1611"]): return False
    if not expectState(I_AGENT1, "Active"): return False
    if not expectState(I_AGENT2, "Standby"): return False
    # 6th tick!
    qf.sendEvent("tick")
    #if not expectState(I_AGENT2, "Active"): return False
    if not expectState(I_MASTER, "Active", ["S11", "S14", "S1611"]): return False
    if not expectState(I_AGENT1, "Inactive"): return False
    if not expectState(I_AGENT2, "Inactive"): return False

    # Disarm the master
    qf.sendEvent("Disarm")
    if not expectState(I_MASTER, "Inert"): return False

    # Check that arming again works right
    qf.sendEvent("Arm")
    if not expectState(I_MASTER, "Active", ["S11", "S14", "S1611"]): return False

    # At this point, all good, test passed!
    return True


if __name__ == '__main__':
    print '=' * 79
    print "===== Composite9: timer events in orthogonal regions, interact with 2 SMs ====="
    print '=' * 79
    if run():
        print "PASSED!\n"
