#!/usr/bin/env python
#
# File: timeline.py
#
# Description: Display a nominal launch timeline for the Ares I state model
#
# USAGE: timeline.py [logfile] [update_rate]
#
# If logfile is omitted then the program opens a socket connection
# and waits for data to be received on the socket.
#
# The update_rate specifies the rate at which a logfile is parsed in
# milliseconds.  For example, a value of 50 means that one line from
# the logfile is read every 50 milliseconds, or 20 lines per second.
# A value of zero activates manual stepping using the CONTINUE button.
#

import os
import sys
import thread
import time
import socket
import select
import tkFont
import cPickle
import logging
import struct

from Tkinter import *

update_rate = 20

# if no args, use the socket interface
if (len(sys.argv) == 1):

  # Set up the event socket
  sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
  sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
  sock.bind(('localhost', 7777))
  sock.listen(1)
  # lists of sockets to watch for input and output events
  ins = [sock]
  outs = []
  read_from_file = False
  file_read_done = True
  
# else read events from the specified log file
else:
  logfile = open(sys.argv[1], 'r')
  read_from_file = True
  file_read_done = False

  if (len(sys.argv) == 3):
      if (sys.argv[2].isdigit()):
          update_rate = sys.argv[2]
      
# State variable indicates termination in process
terminating = False

# Event loop counter, mostly for debugging
read_count = 0
read_next_event = True

# -----------------------------------------------------------------------------
# State class
#
# Members:
#    label       the name of the state
#    exit_event  the ONE event that triggers transition out of this state 
#                  FOR THIS TIMELINE
#    span        the number of grid cells spanned by this state (default = 1)
#    widget      the widget (label) object used to display this state
# ------------------------------------------------------------------------------
class State:
    label = ""
    state_name = ""
    span = 1
    next_col = 0
    widget = None

    def __init__(self, my_label, my_name, my_span=1):
        self.label = my_label
        if (len(my_name) > 0):
            self.state_name = my_name
        else:
            self.state_name = my_label
        self.exit_event = my_name
        self.span = my_span

    def Enter(self):
        self.widget.configure(bg="green")         

    def Exit(self):
        self.widget.configure(bg="gray")         


# -----------------------------------------------------------------------------
# Asset class
#
# Members:
#    label       the name of the asset
#    states      the sequential list of states for this asset FOR THIS TIMELINE  
#    state_idx   the index (for the 'states' list) of the current state
#    widget      the widget (label) object used to display this state
#
# ------------------------------------------------------------------------------
class Asset:
    label = ""       # Label for the timeline display
    model_name = ""  # Actual model name used in event messages
    states = list()
    state_idx = 0
    widget = None

    def __init__(self, my_label, my_name, my_states):
        self.label = my_label
        self.model_name = my_name
        self.states = my_states
        

assets = [
    Asset("FS", "FS",
          [State("Idle", "", 3),
           State("On_Pad_Operations","", 3),
           State("Autonomous_Launch_Sequence", "", 3),
           State("Autosafe", ""),
           State("Flight", "", 1),
           State("Abort", "", 1),
           State("Separation", "", 3)]),
    Asset("RGA", "Rate_Gyro_Assembly",
          [State("Idle", "", 3),
           State("Initialize", "", 2),
           State("Standby", "", 2),
           State("Operational", "", 6),
           State("Power_Off", "", 2)]),
    Asset("RoCS", "RoCS",
          [State("Idle", "", 3),
           State("Checkout", "", 2),
           State("Pad_Operations_Dry", "", 2),
           State("Pad_Operations_Fueled", "", 3),
           State("Autonomous_Operations", "", 2),
           State("Flight", "", 3)]),
    Asset("FC_AGTU", "FC_AGTU",
          [State("Idle", "", 3),
           State("Initialize", "", 2),
           State("Operational", "", 9),
           State("Failed", "")]),
    Asset("RINU", "RINU",
          [State("Idle", "", 3),
           State("Initialize", ""),
           State("Standby", ""),
           State("Gyro-Compassing", "Gyrocompassing"),
           State("Inertial Navigation", "Inertial_Navigation", 5),
           State("Commanded_BIT", "", 2),
           State("Power_Off", "", 2)]),
    Asset("FSS", "Flight_System_Safety",
          [State("Idle", "", 3),
           State("Initialize", ""),
           State("Power_Off", "", 2),
           State("On_Pad_Operations", ""),
           State("LaunchReady","",2),
           State("Terminal_Count", "", 2),
           State("FS_Flight", "", 2),
           State("US_Flight", "", 2)]),
    Asset("RF", "RF",
          [State("Idle", "", 3),
           State("Standby", ""),
           State("Off", ""),
           State("Test_Transmission", "", 2),
           State("Checkout", "", 2),
           State("Operational", "", 5),
           State("Failed", "")]),
    Asset("ReCS", "ReCS",
          [State("Idle", "", 3),
           State("Checkout", "", 2),
           State("Pad_Operations_Dry","", 2),
           State("Pad_Operations_Fueled", "", 3),
           State("Autonomous_Operations", "", 2),
           State("Flight", "", 3)]),
    Asset("US_TVC", "US_TVC",
          [State("Idle", "", 3),
           State("Checkout", ""),
           State("On Pad Operations", "On_Pad_Operations", 2),
           State("ALS", "Autonomous_Launch_Sequence"),
           State("FS Ascent", "FS_Ascent", 3),
           State("Startup", ""),
           State("Active", "", 1),
           State("Abort", ""),
           State("Power Off", "Post_Shutdown", 2)]),
    Asset("EPS", "EPS",
          [State("Idle", "", 3),
           State("Pre-Launch", "Pre_Launch", 2),
           State("Flight", "", 10)]),
    Asset("US_MPS", "US_MPS",
          [State("Idle", "", 3),                  
           State("On_Pad_Operations", "", 2),
           State("Checkout", ""),
           State("ALS", "Autonomous_Launch_Sequence"),
           State("Autosafe", ""),
           State("FS_Ignition_and_Flight", "", 2),
           State("US_Ignition_and_Flight", "", 2),
           State("MECO", "", 2),
           State("Abort", "")]),
    Asset("J2XEngine", "J2XEngine",
          [State("Idle", "", 3),
           State("Checkout", ""),
           State("OnPadOperations", "", 2),
           State("Autosafe", ""),
           State("Diagnostic", ""),
           State("StartPrep", "",  2),
           State("Mainstage", "", 2),
           State("Abort", ""),
           State("PostShutdown", "", 2 )]),
    Asset("GNC_Steering", "GNC_Steering",
          [State("Idle", "", 3),
           State("Pre_Launch","", 2),
           State("FS_Flight","", 2),
           State("FS_Separation", "", 2),
           State("US_Flight", ""),
           State("Manual_Steering", "", 2),
           State("Free_Drift", ""),
           State("US_Cutoff", "", 2)]),
    Asset("GNC_Control", "GNC_Control",
          [State("Idle", "", 1),
           State("Pre-Launch", "Pre_Launch", 2),
           State("Terminal Countdown", "Terminal_Countdown", 2),
           State("FS_Flight", ""),
           State("FS_Pre_Separation", "", 2),
           State("FS_Separation", "", 2),
           State("US_Ignition", ""),
           State("US_Flight", ""),
           State("US_Cutoff", "", 2),
           State("Shutdown", "Shutdown_and_US_Reentry")]),
    Asset("Navigation", "GNC_Navigation",
          [State("Idle", "", 3),
           State("Initialization", "", 2),
           State("FS_Flight", "",3),
           State("US_Flight", "", 3),
           State("Launch_Delay", "", 2),
           State("Gyrocompassing", "", 2)]),
    Asset("GNC_Guidance", "GNC_Guidance",
          [State("Idle", "", 3),
           State("Pre-Launch", "Pre_Launch", 2),
           State("Terminal Countdown", "Terminal_Countdown", 1),
           State("Vertical Rise", "Vertical_Rise", 1),
           State("FS Flight", "FS_Flight", 3),
           State("US Flight", "US_Flight", 2),
           State("US Cutoff", "US_Cutoff", 2),
           State("Abort", "")]),
    Asset("FC-CTC", "RS422_FC_CTC",
          [State("Initialize", ""),
           State("Operational", "", 12),
           State("Failed", "", 2)]),
    Asset("GbE US-Orion", "GbE",
          [State("Idle", "", 2),
           State("Initialize", ""),
           State("Standby", "", 2),
           State("Operational", "", 8),
           State("Failed", "", 2)]),
    Asset("1553 US-FS", "FS1553",
          [State("Initialize", ""),
           State("Standby", ""),
           State("Operational", "", 10),
           State("FailedBIT", "", 2),
           State("Failed", "")]),
    Asset("1553 US-US", "US1553",
          [State("Initialize", ""),
           State("Standby", ""),
           State("Operational", "", 12),
           State("Failed", "")]),
    Asset("FCRM", "FCRM_1",
          [State("Start", "",1),
           State("CCDLS_Synched", "", 2),
           State("FC_Minor_Cycle_Synced", "",3),
           State("CCDLS_Operational", "", 2),
           State("FCS_FCOG_Initialized", "", 2),
           State("Operational", "", 1),
           State("Simplex", "", 1),
           State("Timeout", "FCRM_INIT_TIMEOUT", 1),
           State("CCDLS Disabled", "CCDLS_DISABLED_FCS_SIMPLEX", 2)]),
    Asset("AbortManager", "AM",
          [State("Idle", "", 3),
           State("Initialization", "", 2),
           State("Operational", "", 9),
           State("Abort", "")]),
    Asset("MissionManager", "MissionManager",
          [State("Vehicle_Standby", "", 2),
           State("Vehicle_Checkout", "",2),
           State("OnPadOps", "On_Pad_Operations",1),
           State("ALS", "Autonomous_Launch_Sequence"),
           State("Auto_Safe", ""),
           State("FS_Ignition", "", 2),
           State("FS_Flight", "", 1),
           State("UsFsSep", "US_FS_Separation", 1),
           State("J2X_Ignition", ""),
           State("US_Flight", ""),
           State("UsSep", "US_Separation", 1),
           State("FlightAbort", "In_Flight_Abort", 1)])
]

# Approx conversion: the 'width' property is in average character width,
# but winfo_reqwidth() returns pixels
pixels_to_font = 435/50

col_widths = [0, 11, 8, 8, 8, 8, 19, 17, 10, 8, 14, 14, 12, 8, 12, 8] 

num_assets = len(assets)

my_font = ("Arial", 12, "normal")


# -----------------------------------------------------------------------------
# App class (i.e., the application)
#
# Members:
#    frame       the main frame
#    quitButton  user-accessible quit  
#    status      the status bar
#
# -----------------------------------------------------------------------------
class App:
    frame = None
    quitButton = None
    contButton = None
    status = None
    
    def __init__(self, master):
        
        self.frame = Frame(master)
        self.frame.grid()

        # Create the arrays
        sm_label = list()
        max_width = list()
        
        # Set up a status bar
        status_width = 0
        status_span  = 15
        for i in range (0, status_span):
            status_width += col_widths[i]
            
        self.status = Label(self.frame,
                            text="Ready",
                            width=status_width,
                            bd=2,
                            relief="ridge",
                            bg="#EEE",
                            anchor=W,
                            padx=5,
                            font=my_font)

        for i in range(0, num_assets):
            sm_label.insert(i, Label(self.frame,
                                     text=assets[i].label + ":",
                                     font=my_font))
            sm_label[i].grid(row=i, sticky=W)

            num_states = len(assets[i].states)
            for j in range (0, num_states):
                the_state = assets[i].states[j]
                the_state.widget = Label(self.frame,
                                        text=the_state.label,
                                        bg="yellow",
                                        bd=1,
                                        relief="ridge",
                                        font=my_font)
                the_widget = the_state.widget

                
                if (j == 0):
#                    the_widget.configure(bg="green")
                    col = 1
                else:
                    col = assets[i].states[j-1].next_col

                # Set the grid parameters
                the_widget.grid(row=i, column=col, columnspan=the_state.span, sticky=W)

                # Set cell width
                cell_width = 0
                for k in range (col, col + the_state.span):
                    cell_width += col_widths[k]

                # Compensate for borders
                if (the_state.span > 1):
                    cell_width += the_state.span - 1

                the_widget.configure(width=cell_width)

                # Compute the column location for the next state
                the_state.next_col = col + the_state.span                

            
        self.quitButton = Button(self.frame, text="QUIT", command=quitApp)
        self.quitButton.grid(row=i+1, sticky=W)


        self.contButton = Button(self.frame, text="CONTINUE", command=readNextEvent)
        self.contButton.grid(row=i+1, column=1, columnspan=2, sticky=W)

        self.status.grid(row=i+1, column=3, columnspan=status_span, sticky=W)        
        exit

# -----------------------------------------------------------------------------
#
# Read events from the socket connection
#
# -----------------------------------------------------------------------------
def processEvent(event):
    
    sys.stdout.write(event + "\n")
    sys.stdout.flush()
    setStatus("Received event: " + event)
    if (event == "quit"):
        quitApp()

    # Process the event
    params = event.split()
    evt_value = ""
    if (len(params) > 2):
        evt_value = params[2]

    if (evt_value == 'ENTRY' or evt_value == 'EXIT'):
        evt_asset = params[0]
        evt_state = params[1]
        
        # For each asset:
        #   If current state->event matches then increment state
        for asset in assets:
            if (asset.model_name == evt_asset):
                for state in asset.states:
                    if (state.state_name == evt_state):
                        sys.stdout.write("Change state: " + evt_asset + "." + evt_state + "." + evt_value + "\n")
                        sys.stdout.flush()
                        if (evt_value == 'ENTRY'):
                            state.Enter()

                        if (evt_value == 'EXIT'):
                            state.Exit()


# -----------------------------------------------------------------------------
#
# Process events 
#
# -----------------------------------------------------------------------------
def readEventFromSocket():
    global read_count

    # NOTE: We may (and usually do) get EBADF from select while exiting the
    #       app since we have to close the socket.  The try-except clause
    #       catches this and permits graceful termination.
    i, o, e = select.select(ins, outs, [], 0.0)
    read_count += 1
#    setStatus("read_count: " + str(read_count))

    event = ""
    for x in i:
        if x is sock:
            # input event on sock means a client is trying to connect
            newSocket, address = sock.accept()
            ins.append(newSocket)
            setStatus("Connected to client!")
            
        else:
            setStatus("Found new data: read_count = " + str(read_count))
            # other input events mean data arrived, or disconnections

            chunk = x.recv(4)
            if (len(chunk) == 4):            
                slen = struct.unpack(">L", chunk)[0]
                sys.stdout.write("slen = " + str(slen) + "\n")
                sys.stdout.flush()

                chunk = x.recv(slen)
                while (len(chunk) < slen):
                    chunk = chunk + x.recv(slen - len(chunk))

                obj = cPickle.loads(chunk)
                record = logging.makeLogRecord(obj)
                event = record.getMessage()

    return event


def readEventFromFile(file):
    global read_count
    global file_read_done

    # NOTE: We may (and usually do) get EBADF from select while exiting the
    #       app since we have to close the socket.  The try-except clause
    #       catches this and permits graceful termination.
    read_count += 1
    line = file.readline()
    event = ""
    if (len(line) == 0):
      file_read_done = True
    else:
      event = line.rstrip()

    return event


def readEvents():
    global read_next_event

    if (read_from_file):
        if (read_next_event and not file_read_done):
            event = readEventFromFile(logfile)
            processEvent(event)
            
    else:
        event = readEventFromSocket()
        if (event != ""):
            processEvent(event)

    if (update_rate == 0):
        read_next_event = False

    root.update()


    # This will call repeat the call.  This is how you repeat an event in Python Tkinter
    root.after(update_rate, readEvents)

def readNextEvent():
  global read_next_event
  read_next_event = True

# -----------------------------------------------------------------------------
#
# Set the value of the status bar
#
# ------------------------------------------------------------------------------
def setStatus(value):
    app.status.configure(text=str(value))

# -----------------------------------------------------------------------------
#
# Clean up for termination
#
# ------------------------------------------------------------------------------
def quitApp():
    
    # disconnect the socket
    if (not read_from_file):
        sock.close()
    else:
        logfile.close()
        
    root.destroy()

# -----------------------------------------------------------------------------
#
# Start the program 
#
# ------------------------------------------------------------------------------

root = Tk()
app = App(root)

# Pop the timeline window to the top
root.lift()

# Kick off the user-defined part of the event loop
root.after(100, readEvents)

# Initiate the main event loop
root.mainloop(0)
