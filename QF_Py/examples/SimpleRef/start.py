#!/usr/bin/env python -i
"""
Basic example of using sim_state_start module.
"""
import sim_state_start
import sys
import time

def checkState(machine, state):
    """
    Check for a state and timeout if not found.
    """
    timeout = 0
    current_state = ""
    while (state != current_state):
        current_state = sim_state_start.currentState('Simple1')
        timeout = timeout + 1
        if timeout > 100000:
            print "Timeout: looking for expected current state: %s" % current_state
            sys.exit(-1)
    return current_state


if __name__ == "__main__":
    #
    # Start all Statecharts in ./autocode without trace gui widget
    #
    sim_state_start.main()
    time.sleep(5)
    #
    # send event PwrOn to Simple1
    #
    print "Send PwrOn event."
    sim_state_start.sendEvent('PwrOn')
    #
    # wait for current state to enter Idle
    #
    current_state = checkState("Simple1","Idle")
    print "Current state is: %s\n" % current_state
    #
    # send event PwrOff to Simple1
    #
    print "Send PwrOff event."
    #
    # wait for current state to enter Off
    #
    sim_state_start.sendEvent('PwrOff')
    current_state = checkState("Simple1","Off")
    print "Current state is: %s\n" % current_state
    #
    print "Interactive Python prompt next"

