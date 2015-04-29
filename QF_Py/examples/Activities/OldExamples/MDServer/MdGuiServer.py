#!/usr/bin/env python
'''
Created on Jan 5, 2010
Adapted from gui_server June 28, 2010

This module implements Autocoder trace GUI support as an XML-RPC server.
The module is intended to be invoked from the "Main thread" so that the
Application GUI can be in the main thread.

It can be tested by running the module from the command line with the
--test option.

@author: Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
'''
from com.nomagic.magicdraw.core import Application
from com.nomagic.magicdraw.openapi.uml import *
import sys
#
# Workaround for SAXParser ClassNotFoundException
#import xml.parsers.expat
#xml.parsers.expat._xerces_parser = "com.sun.org.apache.xerces.internal.parsers.SAXParser"
#from org.xml.sax import *
#from org.xml.sax.ext import *
#from org.xml.sax.helpers import *
#from org.python.apache.xerces.parsers.SAXParser import *
#reader = XMLReaderFactory.createXMLReader()
#from java.lang import System
#print System.getProperty("java.version")
#print System.getProperty("java.vendor")
#print System.getProperty("java.class.path")
#print reader.getClass().getName()
#enabled = reader.getFeature("http://xml.org/sax/features/use-entity-resolver2")
#print "Entity Resolver 2 enabled:", enabled 
#
# XML-RPC server
import SimpleXMLRPCServer
# Port number for the XMLRPC server
XMLRPC_PORT = 8154
gl = Application.getInstance().getGUILog()
#
import ActivityHighlighter
reload(ActivityHighlighter)
from ActivityHighlighter import ActHi

class MdAnimationUpdater(object):
    '''
    XML-RPC Server class to handle GUI messages for
    the Autocoder Python trace GUI.
    '''

    def __init__(self, MDProject, port=XMLRPC_PORT, debug=False):
        '''
        Constructor
        '''
        gl.log("init method called")
        self.__serverName = "localhost"
        self.__serverPort = port
        self.__debug = debug  # debug flag
        # whether to keep serving or not
        self.__keepServing = True
        #
        self.__mdSession = None  # Maddalena: cache of MagicDraw session?!
        self.__highlighter = None
        self.__project = MDProject

    def __destroy(self):
        """
        Method to stop this XML-RPC server.
        It stops and deletes the thread, and clears all queues.
        """
        self.__keepServing = False  # stop the serving
        # Clean-up session
        self.__teardownMDSession()
        #- stop the XML-RPC server
        self.__rpcServer.server_close()
        del self.__rpcServer
        self.__rpcServer = None
        #
        if self.__debug:
            gl.log( "destroy: Destroy() complete!")
            print "destroy complete!"
            print "------"


    def serve(self):
        """
        Method to set-up the XML-RPC server and begin handling GUI activities.
        """
        # Instantiate XML-RPC server thread
        self.__rpcServer = SimpleXMLRPCServer.SimpleXMLRPCServer((self.__serverName, self.__serverPort), logRequests=True)
        print "XMLRPC GUI server started on http://%s:%d/RPC2..." % (self.__serverName, self.__serverPort)
        gl.log("serve: XMLRPC GUI server started...")
        print "XMLRPC GUI server started... "
        self.__rpcServer.register_introspection_functions()
        self.__rpcServer.register_instance(self)
        print self.__rpcServer.system_listMethods()
        #
        if self.__debug:
            print "debug serve: GUI Server started!"
            
        #
        try:
            # Now run the GUI update loop
            gl.log("serve called")
            print "runRPCSERVER called!"
            while self.__keepServing:
                self.__rpcServer.handle_request()
            #
            if self.__debug:
                gl.log( "XML-RPC server thread stopped!")
                print "XML-RPC server thread stopped!"
            self.__destroy()
        except Exception:
            print
            print "GUI Server terminated!"
            sys.exit(0)

    def __setupMDSession(self):
        print "startmdsess called"
        """
        Sets up a MagicDraw session, as appropriate
        """
        ##
        ## Maddalena: set up MagicDraw session here
        self.__highlighter=ActHi(self.__project)
        self.__mdSession = object()
        pass

    def __teardownMDSession(self):
        """
        Tears down the MagicDraw session, if necessary
        """
        ##
        ## Maddalena: tear down MagicDraw session here if needed
        ## Should clear all painters on exit. Need to put this elsewhere also?
        if self.__highlighter is not None:
            self.__highlighter.clearAll()
        self.__mdSession = None

    def beginSession(self):
        gl.log("beginSession called")
        """
        Method invoked by the XML-RPC client to notify MagicDraw to begin a
        session for diagram modifications if one isn't already active.
        """
        self.__setupMDSession()
        return "MagicDraw session starting..."

    def sendUpdate(self, params):
        """
        Method to send ON/OFF GUI updates to MagicDraw.
        This method uses the highlighter object to highlight a diagram element.
        
        Each message must consist of 3 whitespace-separated elements:
            <Diagram Name> <Action/Flow Name> <On/Off>
        """
        # Figure out which diagram, and which action within that diagram
        # to change to what state
        if params is not None:
            ##
            ## Maddalena, this is all yours!  :-)
            ##
            if len(params) < 3:
                # raise exception?
                gl.log( "GUI update param must have 3 elements!")
            else:
                if self.__mdSession is not None and self.__highlighter is not None:
                    print "==> Turn diagram:node/flow '%s:%s' highlight '%s'"\
                            % (params[0], params[1], params[2]) 
                    self.__highlighter.highlight(params[0],params[1],params[2])
                else:
                    gl.log( "ERROR! No MagicDraw session to allow updating diagram(s)!" )
        #
        if self.__debug:
            print "Update queue:", self.__updateQueue.queue
        #
        return "'%s' GUI update" % params

    def endSession(self):
        """
        Method invoked by the XML-RPC client to notify MagicDraw to end the
        session started for diagram modification.
        """
        self.__teardownMDSession()
        self.__destroy()
        return "MagicDraw session ended!"


#if __name__ == "__main__":
#    # Test code, starts a standalone server on port 8150
#    gl.log( "Starting XML-RPC server..."
#    MdAnimationUpdater(debug=True).serve()
