#!/usr/bin/python -i
########################################################################################
# Filename:  setup.py
#
# This Python script runs the State-machine models in the test_harness environment.
#
# Example:
#  setup.py
#
# To Shutdown:
#   quit()
#
# For Help:
#   help()
#
# Author:  Garth Watney
#
########################################################################################
import os
import time
import thread
import string
from Tkinter import Button
import popen2
import sys

# -------------------------------------------------------
# Default instantiated state-machine
# -------------------------------------------------------
smName = "calib"

print "-------------------------------------------------------"
print " State-Machine Test Harness"
print "  "
print " To shutdown: 'quit()'"
print " For help: 'help()'"
print "-------------------------------------------------------"

# Open a log file to dump all the output from the state-machine application
appOut = open("application.out", 'w')


# -----------------------------------------------------------------------------
# terminalSetup()
#
# A procedure to setup an xterm window and fork an application
#
# ------------------------------------------------------------------------------
def terminalSetup(cmdLine):
  params = cmdLine.split(' ')
  pid = os.fork()
  if pid == 0:
    os.execvp('xterm', params)
  return pid


# -----------------------------------
# Start all the Applications
# -----------------------------------

# Start the GUI Application
guiApp = popen2.Popen3("./gui.py")

# Start the State Machine Application
smApp = popen2.Popen3("linux/active")

# Start a terminal for dumping data from the State Machine Application
application = 'xterm -bg black -fg green -geometry 64x23+461+7 -title Application_Output -e tail -f application.out'
appPid = terminalSetup(application)

# -----------------------------------------------------------------------------
# quit()
#
# A procedure to shutdown cleanly
#
# ------------------------------------------------------------------------------
def quit():
  appOut.close()
  os.kill(guiApp.pid, 9)
  os.kill(smApp.pid, 9)
  os.kill(appPid, 9)
  time.sleep(1)
  raise SystemExit


# -----------------------------------------------------------------------------
# parent()
#
# The main thread
#
# ------------------------------------------------------------------------------
def parent():
  thread.start_new(appRead,())
  thread.start_new(guiRead,())
  # Parent now interactively runs the Python interface


# ----------------------------------------------
# appRead()
#
# A thread for reading logged events from the
# statechart application and sending the events
# to the GUI.
# ----------------------------------------------
def appRead():
  try:
    while 1:
      logEvent = smApp.fromchild.readline()

      # Write to the log file
      appOut.write(logEvent)
      appOut.flush()

      guiApp.tochild.write(logEvent)
      guiApp.tochild.flush()
  except:
    print "quit appRead"

# ----------------------------------------------
# guiRead()
#
# A thread for reading signal events from the
# GUI and sending the events to the statechart
# application.
# ----------------------------------------------
def guiRead():
  try:
    while 1:
      logEvent = guiApp.fromchild.readline()
      smApp.tochild.write(logEvent)
      smApp.tochild.flush()
  except:
    print "quit guiRead"
    
    
# -----------------------------------------------------------------------------------
# Start the Python Interface
# Read the StatechartSignals.h to get the signal numbers to send across the socket
# -----------------------------------------------------------------------------------
input = open("autocode/StatechartSignals.h", 'r')

# Used to map from enumeration type to signal integer
cmdMap = {}

# Map the During signal
cmdMap["DURING"] = 4

# Loop until the user defined signals are found
# Reading StatechartSignals.h
while 1:
  data = input.readline()
  if not data:
    print "Sorry, did not find the user defined signals"
    os.exit()
    break
  if string.find(data, "User defined signals") != -1:
    print "Found user defined signals"
    break

# Map each signal to an integer
ival = 5
EventList = ""
while 1:
  data = input.readline()
  if not data:
    break
  if string.find(data, "MAX_SIG") != -1:
    break
  data = string.strip(data)
  if not data: continue
  # Skip this entire line if it starts with a #
  if data[0] == '/': continue
  data = data.split(',')
  cmdMap[data[0]] = ival
  ival = ival+1
  EventList = EventList + "\n" + str(hex(ival - 1)) + " " + data[0]

# ---------------------------------------------------
# sendCmd()
#
# A procedure for sending data to the State-machine
# std input
# ---------------------------------------------------
def sendCmd(cmd):
  scmd = str(cmd) + "\n"
  smApp.tochild.write(scmd)
  smApp.tochild.flush()


# ------------------------------------------------------
# setGuard()
#
# Set a guard in the state-machine to True or False
# The first parameter is the instantiated name of the 
# state-machine.
# The second parameter is the name of the guard function.
# The third parameter is "True" or "False"
# ------------------------------------------------------
def setGuard(smName, guardName, condition):
  sendCmd("IMPL['" + smName + "'].set('" + guardName + "'," + condition + ")")

# ------------------------------------------------------
# sendEvent()
#
# A procedure for handling user events from the Python
# interface.
# ------------------------------------------------------
def sendEvent(event, data=""):
  scmd = str(cmdMap[event]) + " " + str(data)
  sendCmd(scmd)

# ------------------------------------------------------
# help()
#
# Print out the user commands
# ------------------------------------------------------
def help():
  print "sendEvent(\"<EventName>\")"
  print "sendEvent(\"<EventName>\", \"<arguments>\")"
  print "during()"
  print "Event List: " + EventList
  print "quit()"
  print "seis[Not]Complete()"
  print "seis[Not]Valid()"
  print "seis[Not]Retry()"
  print "seisTest()"
  print "seisTransaction()"
  print "setGuard('smName', 'guardName', True|False)"




# ------------------------------------------------------
# during()
#
# Send the DURING event
# ------------------------------------------------------
def during():
  sendEvent("DURING")

# ------------------------------------------------------
# adv()
#
# Send a number of DURING events
# ------------------------------------------------------
def adv(iterations):
  for i in range(0,iterations):
    during()
    time.sleep(1)



# ------------------------------------------------------
# seisComplete()
#
# 
# ------------------------------------------------------
def seisComplete():
  sendCmd("IMPL['trans'].set('transaction_Complete',True)")

# ------------------------------------------------------
# seisNotComplete()
#
# 
# ------------------------------------------------------
def seisNotComplete():
  sendCmd("IMPL['trans'].set('transaction_Complete',False)")


# ------------------------------------------------------
# seisValid()
#
# 
# ------------------------------------------------------
def seisValid():
  sendCmd("IMPL['trans'].set('validation_Passed',True)")

# ------------------------------------------------------
# seisNotValid()
#
# 
# ------------------------------------------------------
def seisNotValid():
  sendCmd("IMPL['trans'].set('validation_Passed',False)")


# ------------------------------------------------------
# seisRetry()
#
# 
# ------------------------------------------------------
def seisRetry():
  sendCmd("IMPL['trans'].set('transaction_retry',True)")

# ------------------------------------------------------
# seisNotRetry()
#
# 
# ------------------------------------------------------
def seisNotRetry():
  sendCmd("IMPL['trans'].set('transaction_retry',False)")

# ------------------------------------------------------
# seisTest()
#
# 
# ------------------------------------------------------
def seisTest():
  
  # Kick-off the Procesing
  sendEvent("SIG_START_TRANSFER")
  time.sleep(1)

  # Make validation of science packets pass
  seisValid()
  time.sleep(1)

  # Do 3 transactions
  for i in range(0,3):
    seisTransaction()




# ------------------------------------------------------
# seisTransaction()
#
# 
# ------------------------------------------------------
def seisTransaction():
  sendEvent("SIG_LOWSPEED_COMPLETE")
  time.sleep(1)
  sendEvent("SIG_STOP_RECORDING_TIMEOUT")
  time.sleep(1)
  sendEvent("SIG_RECORDING_STOPPED")
  time.sleep(1)
  sendEvent("SIG_TRANSFER_COMPLETE")
  time.sleep(1)
  sendEvent("SIG_RAW_DATA_PROCESSING_COMPLETE")
  time.sleep(1)
  sendEvent("SIG_LOWSPEED_COMPLETE")
  time.sleep(1)




# ------------------------------------------------
# Print the help and kick-off the parent thread
# ------------------------------------------------
help()
parent();
