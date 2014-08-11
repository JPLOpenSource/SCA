#!/usr/bin/env python
###################################################################################
# Filename:  gui.py
#
#   Displays the statecharts defined in Application.py and starts up a command
# button list defined in StatechartSignals.  This program can work either with
# sockets or with std io.  Currently it works with std io.
#
#
###################################################################################
import Application
import StatechartSignals
import string
import sys

from threading import Thread, Condition, Lock, currentThread


class suedoSocket:
    def send(self, inString):
        print inString,
        sys.stdout.flush()

guiOutput = suedoSocket()


# The parent thread is responsible for periodically updating the TkInter objects and
# sending Entry and Exit events to the statecharts from the global Events list
def parent():
    keepRunning = True  # flag to indicate whether to keep running
    #sys.stderr.write("\ngui Parent thread started: %s\n" % currentThread())
    Thread(target=child, args=(1,)).start()
    signals = StatechartSignals.StatechartSignals(guiOutput)
    while keepRunning:
        # Call all the objects' update()
        Application.update()
        mutex.acquire()
        if len(Events) > 0:  # check length of event queue before popping
            event = Events.pop(0)
            # LJR - Added special exit event.
            if event == "Quit":
                # quit parent
                keepRunning = False
                break;
            if len(event) > 1:   # Don't process the \n at the end of a command
                params = event.split()
                # Send the Entry or Exit event to the object
                # mapCharts return the object
                # EnterState and ExitState are member functions of the object
                #sys.stderr.write("## gui.%s processing %s ##\n" % (currentThread(), params))
                if Application.mapCharts.has_key(params[0]):
                    if params[2] == 'ENTRY':
                        Application.mapCharts[params[0]].EnterState(params[1])
                    elif params[2] == 'EXIT':
                        Application.mapCharts[params[0]].ExitState(params[1])
        else:  # release lock until event queue has something; prevents CPU hog
            mutex.wait(0.05) # timeout after 50ms, sufficient for GUI response
        #
        mutex.release()

# The child thread is responsible for reading data from the dms socket and loading
# the events into the global Events list for use by the parent thread.    
def child(tid):                                                                                                                                                                                                      
    keepRunning = True  # flag to indicate whether to keep running
    #sys.stderr.write("\ngui Child thread started: %s\n" % currentThread())
    while keepRunning:
        msgString = sys.stdin.readline()
        if not msgString: 
            break
        newcmds = msgString.split('\n')
        #sys.stderr.write("## gui.%s received %s ##\n" % (currentThread(), newcmds))
        for cmd in newcmds:
            if cmd == "Quit":
                # cause child to stop running, will be forwarded to parent
                keepRunning = False
            mutex.acquire()
            Events.append(cmd)
            mutex.notify()
            mutex.release()

# Shared memory used by parent and child threads.
Events =  []
#
# Mutex lock for shared event array
mutex = Condition(Lock())

try:
    parent()
except KeyboardInterrupt:
    print "Closing GUI"

#sys.stderr.write("Successfully quit GUI!\n")
