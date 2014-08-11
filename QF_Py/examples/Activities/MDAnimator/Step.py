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
from javax.swing import *
import xml.parsers.expat
xml.parsers.expat._xerces_parser = "com.sun.org.apache.xerces.internal.parsers.SAXParser"
from com.nomagic.magicdraw.core import Application
XMLRPC_PORT = 8150
gl = Application.getInstance().getGUILog()
import ActivityHighlighter
reload(ActivityHighlighter)
from ActivityHighlighter import ActHi
debug=0

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
        #
        # Debug:  print list of available methods
        #print "Proxy to access MD XMLRPC server: s == %s" % repr(s)
        server=0
        
        try:
            blah=s.system.listMethods()
            server=1
        except:
            gl.log("ERROR: server inaccessable - not started or something. geez.")
        
        if server==1:
            step=None
            step=s.step()
            
            if debug==1:
                gl.log("DEBUG: " + str(step))
            
            if step is not None:
                project = Application.getInstance().getProjectsManager().getActiveProject()
                ah=ActHi(project,False)
                if len(step)==2:
                    msg,end=step
                    if msg=="Activity started":
                        gl.log("Starting sub activity")
                else:
                    activity,node,state,message,end=step
                    gl.log(str(message))
                    ah.highlight(activity,node,state)
                    
                    if end is True:
                        n = JOptionPane.showConfirmDialog(None,
                                                          "Activity finished. Would you like to see \nthe final output?",
                                                          "Activity Final",
                                                          JOptionPane.YES_NO_OPTION);
                        if n == JOptionPane.YES_OPTION:
                            final = s.stop(True)
                            gl.log(str(final))
                        else:
                            s.stop(False)

            else:
                gl.log("ERROR performing step [returned None]")
                

            #if return 5 is true, this is the last step. ask teh user if it wants to see the final output. if its final, call stop, and that returns the output parameterers. print if 
            #they want to see them.
    except KeyboardInterrupt:
        print s.terminate()
        sys.exit(0)
    except Exception:
        print s.terminate()
        raise