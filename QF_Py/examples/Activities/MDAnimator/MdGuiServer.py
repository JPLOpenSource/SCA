#!/usr/bin/env python
'''
Created on July 28, 2010

This module implements an Activity Runner as an XML-RPC server to
interact with MagicDraw as the client.  The MD client interacts with this
server to, in effect, step through the execution of an activity.

The module is intended to run directly from command-prompt, in the directory
housing the activity python modules.

@author: Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
'''
import os
import sys
import threading

import sim_state_start
from qf import framework
# A kludge:  Start the related state machine
def smSpawner():
    qf = framework.QF.getInstance()
    qf.init(qf_gui=False)
    sim_state_start.main([], nogui_flag=False,
                         path=os.sep.join([sys.path[0], "DemoSM", "autocode"]))

# XML-RPC server
import SimpleXMLRPCServer

# Port number for the XMLRPC server
XMLRPC_PORT = 8150

class MdActivityRunner(object):
    '''
    XML-RPC Server class to await messages from MagicDraw,
    and to start an activity diagram, or to step through it,
    or to quit the activity run.
    '''

    def __init__(self, port=XMLRPC_PORT, debug=False, bogoMode=False):
        '''
        Constructor
        '''
        self.__serverName = "localhost"
        self.__serverPort = port
        self.__debug = debug  # debug flag
        self.__bogoMode = bogoMode
        #
        # Activity instances currently used, Python modules, protected by a lock
        self.__actInstLock = threading.Condition(threading.Lock())
        self.__actInst = None
        self.__actModule = None
        self.__implInst = None
        self.__implModule = None
        self.__modList = []
#        self.__consoleQueue = Queue.Queue()
        self.__qfThread = None
        #
        # Private members to manage the GUI events and the XML-RPC Server
        self.__keepRunning = True
        self.__rpcServer = None
        #
        # Add cwd to sys path
        self.__cwd = os.getcwd()
        sys.path.append(self.__cwd)

    def __destroy(self):
        """
        Method to stop this XML-RPC server.
        It stops and deletes the thread, and clears all queues.
        """
        # Stop and delete the threads, should have already been set
        self.__keepRunning = False
        #- stop the XML-RPC server
        self.__rpcServer.server_close()
        del self.__rpcServer
        self.__rpcServer = None
        #
        # Destroy the activity instances and modules
        if self.__implInst is not None:
            del self.__implInst
            self.__implInst = None
            del self.__implModule
            self.__implModule = None
        if self.__actInst is not None:
            del self.__actInst
            self.__actInst = None
            del self.__actModule
            self.__actModule = None
        del self.__actInstLock
        self.__actInstLock = None
        for m in self.__modList:
            if m in sys.modules:
                del sys.modules[m]
        self.__modList = []
        #
        # Empty out the console queue
#        try:
#            while not self.__consoleQueue.empty():
#                self.__consoleQueue.get(False)
#        except Queue.Empty:  # good, emptied update queue
#            pass
        if self.__qfThread is not None:
            del self.__qfThread
            self.__qfThread = None
        #
        # clean up sys.path
        sys.path.remove(self.__cwd)
        #
        if self.__debug:
            print "Destroy() complete!"


    def serve(self):
        """
        Method to set-up the XML-RPC server and begin handling GUI activities.
        """
        self.__keepRunning = True
        #
        # OK to instantiate XML-RPC server thread
        self.__rpcServer = SimpleXMLRPCServer.SimpleXMLRPCServer((self.__serverName, self.__serverPort), logRequests=False)
        print "XML-RPC server started on http://%s:%d/RPC2..." % (self.__serverName, self.__serverPort)
        self.__rpcServer.register_introspection_functions()
        self.__rpcServer.register_instance(self)
        self.__rpcServer.allow_none = True
        #
        # Handle XML-RPC requests forever
        try:
            while self.__keepRunning:
                self.__rpcServer.handle_request()
            # Done
            self.__destroy()
        except KeyboardInterrupt:
            print
            print "XML-RPC Server terminated!"
            sys.exit(0)


    def start(self, name, param=None):
        """
        Starts activity executable identified by name.  This consists of:
        1. Finding and importing the named activity + impl modules.
        2. Starting the named activity.  If the Activity requires input, then
            input needs to be supplied in the form of comma-separated
            key=value units, with strings quoted ('' or ""), e.g.,
            "input1='x', input1='y', input2='z'"
        3. Returns a True status if started, or a string for any warnings and
            error messages.
        """
        # If bogoMode, return a test output
        if self.__bogoMode:
            return True
        #
        retMsgs = ""
        #
        # make sure that no current activity has started
        if self.__actInst is not None:
            retMsgs += "ERROR! Another activity instance already running, please stop it before starting another!"
            return retMsgs
        #
        # Import modules
        actName = name
        implName = "%sImpl" % actName
        try:
            self.__implModule = None
            exec("import %s" % implName)
            self.__implModule = locals()[implName]
            self.__modList.append(implName)
        except Exception, info:
            retMsgs += "Warning! Activity Impl class '%s' not loaded: %s\n" % (implName, info)
        #
        try:
            self.__actModule = None
            exec("import %s" % actName)
            self.__actModule = locals()[actName]
            self.__modList.append(actName)
        except Exception, info:
            retMsgs += "FAILED to load Activity class '%s': %s\n" % (actName, info)
        #
        # Ready to start the impl and activities
        self.__actInstLock.acquire()
        try:
            # instantiate QF and sim_state_start if SignalExample
            self.__qfThread = threading.Thread(target=smSpawner)
            self.__qfThread.start()
            self.__implInst = None
            if self.__implModule is not None:
                self.__implInst = self.__implModule.__dict__[implName]()
            self.__actInst = self.__actModule.__dict__[actName](self.__implInst)
        except Exception, info:
            retMsgs += "FAILED instantiating Activity module '%s': %s\n" % (actName, info)
        #
        self.__actInstLock.release()
        #
        # Now start the activity executable
        if self.__actInst is not None:
            # first, parse the params and create a token dictionary
            paramDict = {}
            if param is not None:
                for pair in param.split(","):
                    list = pair.strip().split("=")
                    key = list[0].strip()  # assume first '=' delimits key,value
                    value = "".join(list[1:]).strip()  # in case of other '='s
                    if key in paramDict:  # append to existing token value list
                        paramDict[key].append(value)
                    else:  # create new token value list
                        paramDict[key] = [ value ]
            if self.__debug:
                print "Running activity '%s' with params:\n  %s" % (actName, paramDict)
            self.__currentName=None
            self.__currentName=actName
            self.__actInst.run(paramDict)
        #
        # output message
        if self.__debug:
            print "Message to return to MD: '%s'" % retMsgs
        if len(retMsgs) == 0:
            return True
        else:
            return retMsgs

    def step(self):
        """
        Invokes one step on the activity instance.
        Returns a list of five items:
        1. activity name being stepped
        2. action name executed
        3. on/off for status of action
        4. all output as one string
        5. True/False indicating whether the activity is at its end, and
            thus expecting a prompt
        The list may be empty if something is wrong, e.g., no activity started!
        """
        # If bogoMode, return a test output
        if self.__bogoMode:
            return [ "Increment Example", "Initialize Count", "on" ]
        #
        retList = []
        self.__actInstLock.acquire()
        if self.__actInst is not None:
            # Returned dictionary contains 'status' and 'message' elements
            retDict = self.__actInst.step()
            # store the highlight status as 3 separate elements
            for statElem in retDict["status"]:
                retList.append(statElem)
            # then store the step outputs
            retList.append(retDict["message"])
            # finally, store boolean indicating whether activity is done
            retList.append(self.__actInst.isDone())
        self.__actInstLock.release()
        #
        return retList

    def stop(self, showOutcome=True):
        """
        Stops currently executing activity.
        Input parameter is a boolean indicating whether to show activity output.
        Default is to show output.  The output is returned by this method.
        """
        # If bogoMode, return a test output
        if self.__bogoMode:
            return "Activity done, outcome is 42!"
        #
        output = ""
        #
        self.__actInstLock.acquire()
        if self.__actInst is not None:
            output = repr(self.__actInst.stop(showOutcome))
        self.__actInstLock.release()
        #
        cname=self.__currentName
        self.__actInst=None
        #
        # kill QF thread, if any
        if self.__qfThread is not None:
            sim_state_start.StartStateSim.cleanUp()
            del self.__qfThread
            self.__qfThread = None

        return output

    def terminate(self):
        """
        Method invoked by the XML-RPC client to notify MagicDraw to end the
        session started for diagram modification.
        """
        self.__keepRunning = False
        return self.__currentName


if __name__ == "__main__":
    # Starts a standalone server on port 8150
    print "Starting XML-RPC server..."
    MdActivityRunner(debug=True, bogoMode=False).serve()
