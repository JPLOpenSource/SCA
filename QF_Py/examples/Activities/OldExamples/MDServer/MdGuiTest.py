#!/usr/bin/env python
'''
Created on Jan 5, 2010
Adapted from gui_server June 28, 2010

This module implements an XML-RPC client to run against the MagicDraw
GUI updater XML-RPC Server.

Make sure that the MagicDraw GUI updater server is up and running.
Then run this test with "python -i MdGuiTest.py", and then you can type updates
with s.sendUpdate("<Diagram> <Action> <On/Off>"; end with s.endSession().


@author: Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
'''
import sys
import time
import traceback


# Port number for the XMLRPC server
XMLRPC_PORT = 8154

if __name__ == "__main__":
    # Main test code
    try:
        print "Starting XML-RPC client..."
        ##
        ## This also serves as an example of client's interaction with the
        ## MagicDraw GUI updater XMLRPC server.
        #
        import xmlrpclib
        s = xmlrpclib.ServerProxy('http://localhost:%d' % XMLRPC_PORT)
        print "Proxy to access MD XMLRPC server: s == %s" % repr(s)
        #
        # Debug:  print list of available methods
        print "Available XML-RPC server methods:", s.system.listMethods()
        print "hello"
        #
        # Now, start a session
        print s.beginSession()
        print s.sendUpdate("Activite A1 On")
        #print s.sendUpdate("ExampleDiagram Act1 Off")
        #print s.sendUpdate("ExampleDiagram Act2 On")
        #print s.sendUpdate("ExampleDiagram Act2 Off")
        #print s.sendUpdate("ExampleDiagram Act3 On")
        #print s.sendUpdate("ExampleDiagram Act3 Off")
        print s.endSession()
        #print
        print "Proxy to access MD XMLRPC server: s == %s" % repr(s)
    except:
        try:
            print s.endSession()
        except:
            pass
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            print message
        sys.exit(0)
