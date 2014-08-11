#!/usr/bin/env python
"""
Script to kill sim_state_start.py and gui_server.py since
Python 2.7.x and 2.6.x seem to prevent clean exit.  Likely
due to XMLRPC problems.
"""
import sys
import os
import signal
import subprocess
import time

if __name__ == "__main__":
    p = subprocess.Popen("uname", stdout=subprocess.PIPE)
    os_type = p.stdout.readline()
    print "OS Type: %s" % os_type
    if os_type.rstrip() == "Linux":
        os_python = "python"
    else:
        os_python = "Python"
    time.sleep(2)
    pids = []
    cmd = ["ps","-ef"]
    p = subprocess.Popen(cmd, stdout=subprocess.PIPE)
    lines = p.stdout.readlines()
    for line in lines:
        if os_python in line:
            l = line.split()
	    if os_type.rstrip() == "Linux":
                l = int(l[1])
            else:
                l = int(l[1])
            print l
            if "gui_server.py" in line:
                pids.append(l)
                print line
            if "sim_state_start.py" in line:
                pids.append(l)
                print line
    if len(pids) == 0:
        sys.exit(-1)   
    cmd = "kill " + " ".join(map(lambda x: "%s" % x, pids))
    print "Executing: %s" % cmd
    os.system(cmd) 
    print "Completed..."
           
