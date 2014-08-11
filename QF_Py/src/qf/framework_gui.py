
# Since part of the miros, It is licensed under the same terms as Python itself.
#
"""
NAME: framework_gui.py
DESCRIPTION: Implements a separate thread for communicating with
             legacy autocoded gui.py application from any of the
             auto-coded state machines.
             
             WARNING: this introduces a dependency on the autocoded
             QF-C code in that the file StatechartSignals.h must be
             read to translate the enum values to signal string names
             for the python.
AUTHOR: Leonard J. Reder
EMAIL:  reder@jpl.nasa.gov
DATE CREATED:
"""
from __future__ import with_statement # 2.5 only
# Python Modules
import os
import sys
import errno
import socket
import time
import threading
import xmlrpclib

import framework
import event
from gui_server import XMLRPC_PORT

class QFGUI(threading.Thread):
    """
    QFGUI services.

    This class is a thread sigleton for connecting
    the legacy python test harness GUI to python QF
    framework.
    """
    #
    __instance = None
    #
    # flag to indicate whether run() should keep running
    __running = True
    #
    # XML-RPC proxy object to the GUI message handler
    __gui_app  = None
    # Thread to run the XML-RPC server
    __gui_server_thread = None
    #
    #
    __enum_event_map = dict()
    #
    __xmlrpc_port = None
    
    def __init__(self, state_charts_h_file, gui_py_file):
        """
        Constructor.
        """
        self.__instance = None
        #
        self.__qf = framework.QF.getInstance()
        #
        # Generate map of C enum values to signal string names here.
        #self.__enum_event_map = self.__parseStatechartSignalsH("../../test_harness-C/autocode/StatechartSignals.h")
        self.__enum_event_map = self.__parseStatechartSignalsH(state_charts_h_file)
        #
        # Hardwire the special number 4 to the tick method call...
        self.__enum_event_map[4] = "tick"  # this is the old DURING button.
        #
        # Determine next available port
        p = XMLRPC_PORT
        while True:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            try:
                s.bind(('', p))
                # found available port!
                #print "Port found: %d" % p
                s.close()  # make sure to clean up socket!
                break
            except socket.error, (err, msg):
                if err == errno.EADDRINUSE:
                    print "Port %d in use!" % p
                    p += 1
                else:
                    print "Unexpected socket error '%s'!" % msg
                    sys.exit(-1)
        self.__xmlrpc_port = p
        #
        # Start the GUI Application
        def __runGuiServer(gui_py_file, port):
            cmd = "python %s -e -p %d" % (gui_py_file, port)
            os.system(cmd)
        self.__gui_server_thread = threading.Thread(target=__runGuiServer,
                                                    kwargs={'gui_py_file':gui_py_file,
                                                            'port':p})
        self.__gui_server_thread.start()
        time.sleep(1)  # wait a tad for GUI xmlrpc server to start
        #
        # Setup XML-RPC client to connect to GUI server
        TRIES = 10
        for i in range(1, TRIES+1):
            try:  # attempt up to 10 times until no more socket error
                self.__gui_app = xmlrpclib.ServerProxy('http://localhost:%d' % p)
                self.__gui_app.awaitGuiReady()
                break
            except socket.error, info:  # most likely 'connection refused'
                if i >= TRIES:
                    print "ERROR: framework_gui could not connect to XML-RPC server after %d tries! %s" % (i, info)
                    sys.exit(-1)
                time.sleep(0.5)
        #
        threading.Thread.__init__(self)


    def getInstance(state_charts_h_file=None, gui_py_file=None):
        """
        Return instance of singleton.
        """
        if(QFGUI.__instance is None):
            if state_charts_h_file != None and gui_py_file != None:
                QFGUI.__instance = QFGUI(state_charts_h_file,gui_py_file)
            else:
                print "ERROR: Must set StateChartSignals.h path and gui_server.py path."
                raise
        return QFGUI.__instance


    def destroy():
        """
        Destroys singleton instance to allow reloading the QF framework.
        """
        if QFGUI.__instance is not None:
            QFGUI.__instance.__running = False
            try:
                QFGUI.__instance.__gui_app.terminate()
            except socket.error:  # ignore
                pass
            QFGUI.__instance.__gui_app = None
            del QFGUI.__instance.__gui_server_thread  # clean up resource
            QFGUI.__instance.__gui_server_thread = None
            QFGUI.__instance.__qf = None
            QFGUI.__instance = None
            #
            # DO NOT COMMENT: important to tell unittest that GUI is destroyed
            print "*** Destroyed QF GUI ***"

    # define static methods
    getInstance = staticmethod(getInstance)
    destroy = staticmethod(destroy)


    def __parseStatechartSignalsH(self, file):
        """
        Parse a StatechartSignals.h file and generate
        an indexed dictionary of signal names.
        """
        # Read file
        f = open(file,"r")
        lines = f.readlines()
        #
        event_lines = []
        event_capture = False
        event_dict = dict()
        # Capture lines with user events
        for line in lines:
            if line.find("User defined signals") > -1:
                event_capture = True
            if event_capture == True:
                event_lines.append(line)
            if line.find("Timer Events") > -1\
                    or line.find("Maximum signal id") > -1\
                    or line.find("State-Machine internal signals") > -1:
                break
        # Make list of string event names and enum event values
        event_lines = map(lambda x: x.strip(), event_lines[1:-1])[:-1]
        event_names = map(lambda x: x.split(",")[0].strip(), event_lines)
        x = map(lambda x: x.split(",")[1].split("/*")[1].split("*/")[0].strip(), event_lines)
        event_nums = map(lambda x: int(x,16), x)
        # Create the dictionary
        for ev in enumerate(event_names):
            event_dict[event_nums[ev[0]]] = ev[1]
            #print ev
        return event_dict

    
    def updateGui(self, msg):
        """
        Tries up to 10 times to update GUI
        """
        TRIES = 10
        for i in range(1, TRIES+1):
            try:  # attempt up to 10 times if socket error
                #self.__gui_app.sendUpdate(msg)
                # LJR - Fixed Python 2.7 bug by reconnecting to server locally on each request
                g= xmlrpclib.ServerProxy('http://localhost:%d' % self.__xmlrpc_port)
                g.sendUpdate(msg)
                break
            except socket.error, info:  # most likely 'connection refused'
                if i == TRIES:
                    print "ERROR: XML-RPC call failed all %d tries! %s" % (i,info)
                    sys.exit(-1)
                time.sleep(0.1)

    def _publish(self,e):
        """
        Convert numeric event signal to string name.
        Publish to QF.
        """
        # translate here....
        ev = self.__enum_event_map[int(e)]
        print ev
        # publish here....
        self.__qf.publish(event.Event(ev))
        print "complete"


    def run(self):
        """
        Main thread loop.
        Accepts button events and publishes them.
        """
        while self.__running:
            enum = self.__gui_app.pollButtonEvent()
            if enum != '':
                print enum
                if int(enum, 16) == 4:
                    self.__qf.tick()
                else:
                    self._publish(enum)

        print "Exit: %s\n" % self

