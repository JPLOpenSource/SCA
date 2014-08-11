#!/usr/bin/env python
'''
Created on Jan 5, 2010

This module implements Autocoder trace GUI support as an XML-RPC server.
The module is intended to be invoked from the "Main thread" so that the
Application GUI can be in the main thread.

It can be tested by running the module from the command line with the
--test option.

@author: Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
'''
import sys
import time
import threading
import Queue
#
# XML-RPC server
import SimpleXMLRPCServer

# Port number for the XMLRPC server
XMLRPC_PORT = 8091

class GuiMessageHandler(object):
    '''
    XML-RPC Server class to handle GUI messages for
    the Autocoder Python trace GUI.
    '''

    def __init__(self, port=XMLRPC_PORT, embedded=False, debug=False):
        '''
        Constructor
        '''
        self.__serverName = "localhost"
        self.__serverPort = port
        self.__embeddedExec = embedded
        self.__debug = debug  # debug flag
        #
        # Establishes whether GUI is up and ready
        self.__guiReady = False
        self.__guiReadyLock = threading.Condition(threading.Lock())
        #
        # Instantiate a thread-safe queue for event updates
        self.__updateQueue = Queue.Queue()
        # Instantiate a thread-safe queue for GUI button events
        self.__buttonQueue = Queue.Queue()
        #
        # Private members to manage the GUI events and the XML-RPC Server
        self.__keepRunning = True
        self.__xmlrpcServerThread = None
        self.__rpcServer = None

    def __destroy(self):
        """
        Method to stop this XML-RPC server.
        It stops and deletes the thread, and clears all queues.
        """
        self.__guiReady = False
        # Stop and delete the threads, should have already been set
        self.__keepRunning = False
        #- stop the XML-RPC server
        self.__rpcServer.server_close()
        del self.__rpcServer
        self.__rpcServer = None
        #- kill the XML-RPC server thread
        del self.__xmlrpcServerThread
        self.__xmlrpcServerThread = None
        #
        # Empty out the queues
        #- the update queue
        try:
            while not self.__updateQueue.empty():
                self.__updateQueue.get(False)
        except Queue.Empty:  # good, emptied update queue
            pass
        if self.__debug:
            print "Update queue empty!"
        #
        #- the button queue
        try:
            while not self.__buttonQueue.empty():
                self.__buttonQueue.get(False)
        except Queue.Empty:  # good, emptied button queue
            pass
        if self.__debug:
            print "Button queue empty!"
        #
        if self.__debug:
            print "Destroy() complete!"


    def serve(self):
        """
        Method to set-up the XML-RPC server and begin handling GUI activities.
        """
        self.__keepRunning = True
        # Do nothing if rpc thread already instantiated AND alive
        if self.__xmlrpcServerThread is not None:
            if self.__xmlrpcServerThread.isAlive():
                return
            else:
                del self.__xmlrpcServerThread
        #
        # OK to instantiate XML-RPC server thread
        self.__rpcServer = SimpleXMLRPCServer.SimpleXMLRPCServer((self.__serverName, self.__serverPort), logRequests=self.__debug)
        print "XMLRPC GUI server started on http://%s:%d/RPC2..." % (self.__serverName, self.__serverPort)
        self.__rpcServer.register_introspection_functions()
        self.__rpcServer.register_instance(self)
        self.__xmlrpcServerThread = threading.Thread(target=self.__runRpcServer)
        self.__xmlrpcServerThread.start()
        #
        if self.__debug:
            print "GUI Server started!"
        #
        try:
            # Now run the GUI update loop
            self.__runGuiUpdater()
        except KeyboardInterrupt:
            sys.exit(0)

    def __markGuiReady(self):
        """
        Marks flag that GUI is ready for prime time.
        """
        self.__guiReadyLock.acquire()
        self.__guiReady = True
        self.__guiReadyLock.notify()
        self.__guiReadyLock.release()

    def __runRpcServer(self):
        """
        Main loop for the XML-RPC service thread.
        """
        while self.__keepRunning:
            self.__rpcServer.handle_request()
        #
        if self.__debug:
            print "XML-RPC server thread stopped!"

    def __runGuiUpdater(self):
        """
        Main loop for the GUI thread.
        It is responsible for periodically updating the TkInter objects and
        sending Entry and Exit events to the statecharts from the update queue.
        """
        class PseudoSocket:
            """
            Pseudo-Socket class for the StateChartSignal GUI
            to send button events.
            """
            def __init__(self, server):
                self.__server = server
            def send(self, inString):
                # strip the newline
                cmds = inString.split('\n')
                self.__server.queueButtonEvent(cmds[0])
        #
        # Autocoder Python trace GUI imports
        try:
            import StatechartSignals
            StatechartSignals.StatechartSignals(PseudoSocket(self))
            #
            # invoke sim_state_start to generate Application.py if
            #  gui_server was invoked standalone (meaning, no sim_state_start!)
            if not self.__embeddedExec:  # import sim_state_start and set target
                import sim_state_start
                sim_state_start.setTargetDir(".")  # assume current dir
                sim_state_start.StartStateSim.getInstance().generateApplicationPy([])
            #
            # Now we can try importing Application.py
            import Application
            Application.update()  # bring up the GUI window(s)
            while not Application.windowsReady():
                time.sleep(0.5)
        except ImportError:
            raise
        #
        # Let's client know (via client's polling awaitGuiReady)
        # that GUI is ready 
        self.__markGuiReady()
        #
        while self.__keepRunning:
            # Call all the objects' update()
            Application.update()
            params = None
            try:
                # block, but only briefly to prevent locking GUI
                event = self.__updateQueue.get(block=True, timeout=0.0005)
                params = event.split()  # Don't process the \n at the end of a command
                if self.__debug:
                    print "==> Got params:", params
            except Queue.Empty:
                pass  # ignore, as exception is expected
            #
            # Send the Entry or Exit event to the object.
            # mapCharts return the object
            # EnterState and ExitState are member functions of the object
            if params is not None:
                # if "obj:Type", use "obj" as GUI window name, concat "Type" with state 
                instType = params[0].split(":")
                winName = instType[0]
                stateName = params[1]
                if len(instType) > 1:  # pattern "obj:Type", get "obj"
                    stateName = instType[1] + stateName
                if Application.mapCharts.has_key(winName):
                    if params[2] == 'ENTRY':
                        Application.mapCharts[winName].EnterState(stateName)
                    elif params[2] == 'EXIT':
                        Application.mapCharts[winName].ExitState(stateName)
        #
        if self.__debug:
            print "GUI update thread stopped!"
        #
        self.__destroy()

    def queueButtonEvent(self, ev):
        """
        Method used by the PseudoSocket to add button event to queue.
        """
        self.__buttonQueue.put(ev)


    def awaitGuiReady(self):
        """
        Method that blocks until GUI is initialized and ready.
        This method can be called shortly after xmlrpc client is setup.
        """
        self.__guiReadyLock.acquire()
        while not self.__guiReady:
            self.__guiReadyLock.wait()
        self.__guiReadyLock.release()
        #
        # return GUI-ready flag
        return self.__guiReady

    def sendUpdate(self, msg):
        """
        Method to send ENTRY/EXIT event updates to the GUI.
        """
        # By default, Queue.put blocks if full
        self.__updateQueue.put(msg)
        #
        if self.__debug:
            print "Update queue:", self.__updateQueue
        #
        return "%s queued for GUI update" % msg

    def pollButtonEvent(self, block=False):
        """
        Method to retrieve the next button event.
        """
#        if self.__debug:
#            print "Button queue:", self.__buttonQueue
        #
        ev = ''
        if block:
            # block, keeping in mind this locks up the XML-RPC server
            ev = self.__buttonQueue.get()
        else:
            try:
                # block only briefly to prevent locking the XML-RPC server
                # - the brief timeout prevents CPU-hogging.
                ev = self.__buttonQueue.get(block=True, timeout=0.01)
            except Queue.Empty:
                pass
        return ev

    def terminate(self):
        """
        Method invoked by the XML-RPC client to terminate the server.
        """
        self.__keepRunning = False
        return "GUI Server terminating..."


if __name__ == "__main__":
    # Test code
    if len(sys.argv) > 1 and sys.argv[1] == '--test':
        try:
            print "Starting XML-RPC server..."
            gui = GuiMessageHandler(debug=True)
            threading.Thread(target=gui.serve).start()
    
            print "Starting XML-RPC client..."
            import xmlrpclib
            s = xmlrpclib.ServerProxy('http://localhost:%d' % XMLRPC_PORT)
            # Print list of available methods
            print "Available XML-RPC server methods:", s.system.listMethods()
            print s.sendUpdate("Ev1")
            while 1:
                print s.pollButtonEvent()
        except KeyboardInterrupt:
            gui.terminate()
            time.sleep(3)
            sys.exit(0)
        except Exception:
            gui.destroy()
            raise
    #
    # Main code
    from optparse import OptionParser

    usage = "usage: %prog [options]"
    vers  = "%prog "
    parser = OptionParser(usage, version=vers)
    parser.add_option("-e", "--embedded", dest="embedded_flag",
                      help="Sets flag that GUI was embedded within another app.",
                      action="store_true", default=False)
    parser.add_option("-p", "--port", dest="server_port",
                      help="Server port to use for XMLRPC GUI server.",
                      action="store", default=XMLRPC_PORT)
    (opt, args) = parser.parse_args()

    GuiMessageHandler(port=int(opt.server_port), embedded=opt.embedded_flag).serve()
