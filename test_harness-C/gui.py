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
import thread
import string
import sys


class suedoSocket:
    def send(self, inString):
      print inString,
      sys.stdout.flush()

guiOutput = suedoSocket()

# The parent thread is responsible for periodically updating the TkInter objects and
# sending Entry and Exit events to the statecharts from the global Events list
def parent():
  thread.start_new(child, (1,))
  signals = StatechartSignals.StatechartSignals(guiOutput)
  while True:
    # Call all the objects update()
    Application.update()
    mutex.acquire()
    if len(Events) > 0:
      event = Events.pop(0)
      # LJR - Added special exit event.
      if event == "Quit":
          sys.exit(0)
      if len(event) > 1:   # Don't process the \n at the end of a command
        params = event.split()
        # Send the Entry or Exit event to the object
        # mapCharts return the object
        # EnterState and ExitState are member functions of the object
        if Application.mapCharts.has_key(params[0]):
          if params[2] == 'ENTRY':
            Application.mapCharts[params[0]].EnterState(params[1])
          elif params[2] == 'EXIT':
            Application.mapCharts[params[0]].ExitState(params[1])
    mutex.release()

# The child thread is responsible for reading data from the dms socket and loading
# the events into the global Events list for use by the child thread.    
def child(tid):                                                                                                                                                                                                      
  while True:
    msgString = sys.stdin.readline()
    if not msgString: 
      break
    newcmds = msgString.split('\n')
    for cmd in newcmds:
      mutex.acquire()
      Events.append(cmd)
      mutex.release()

# Shared memory used by parent and child threads.
Events =  []

mutex = thread.allocate_lock()

try:
  parent()
except KeyboardInterrupt:
  print "Closing GUI"
  

