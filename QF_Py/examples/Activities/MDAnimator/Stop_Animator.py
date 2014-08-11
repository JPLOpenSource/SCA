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
import traceback
import xml.parsers.expat
xml.parsers.expat._xerces_parser = "com.sun.org.apache.xerces.internal.parsers.SAXParser"

from com.nomagic.magicdraw.core import Application
# Port number for the XMLRPC server
XMLRPC_PORT = 8150
gl = Application.getInstance().getGUILog()

if __name__ == "__main__":
    # Main test code
    try:
        print "Starting XML-RPC client..."
        ##
        ## This also serves as an example of client's interaction with the
        ## ActivityRunner server
        #
        import xmlrpclib
        s = xmlrpclib.ServerProxy('http://localhost:%d' % XMLRPC_PORT)
        n = JOptionPane.showConfirmDialog(None,
                                          "Activity finished. Would you like to see \nthe final output?",
                                          "Activity Final",
                                          JOptionPane.YES_NO_OPTION);
        if n == JOptionPane.YES_OPTION:
            gl.log(s.stop(True))
        else:
            s.stop()
        #gl.log(s.terminate())
    except KeyboardInterrupt:
        print s.terminate()
        sys.exit(0)
    except Exception:
        print s.terminate()
        raise
