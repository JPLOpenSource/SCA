#!/tps/bin/python

import StatechartSignals
import thread
import string
import sys
import socket

print 'Starting application GUI windows...'

# The following import statement loads the application specific GUI windows:
import Application

######################################################################################
# The update thread is responsible for periodically updating the TkInter objects and
# sending Entry and Exit events to the statecharts from the global Events list.
#
def updateThread(tid):
  while True:
    Application.update()
    mutex.acquire()
    if len(Events) > 0:
      event = Events.pop(0)
      if len(event) > 1:   # Don't process the \n at the end of a command
        params = event.split()
        if params[2] == 'ENTRY':
          Application.mapCharts[params[0]].EnterState(params[1])
        elif params[2] == 'EXIT':
          Application.mapCharts[params[0]].ExitState(params[1])
    mutex.release()
######################################################################################
                                                                                               
# Shared objects used by main and update threads:
Events =  []
mutex = thread.allocate_lock()

# Spawn the update thread:
thread.start_new(updateThread, (1,))

IPort = 2236
listenSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
listenSocket.bind ( ( '', IPort) )
listenSocket.listen ( 1 )

print "Waiting for connection from client application..."
clientSocket, details = listenSocket.accept()
print "Connected to client application."

# Create a StatechartSignals object which will send signals (events) to the C++
# application over this same socket:
print "Starting StatechartSignals window..."
signals = StatechartSignals.StatechartSignals(clientSocket)
print "Running.  Click on button to send corresponding statechart signal..."

#Infinite loop to receive state change messages from client application:
while True:
  msgString = clientSocket.recv(1024)
  #print "Received: " + msgString
  if not msgString: 
    break
  newcmds = msgString.split('\n')
  for cmd in newcmds:
    mutex.acquire()
    Events.append(cmd)
    mutex.release()

print "Closing down client socket."
clientSocket.close()
