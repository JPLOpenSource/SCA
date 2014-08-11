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
import os
import xml.parsers.expat
xml.parsers.expat._xerces_parser = "com.sun.org.apache.xerces.internal.parsers.SAXParser"
from javax.swing import *
from java.awt import GridLayout
from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper

# Port number for the XMLRPC server
XMLRPC_PORT = 8150



gl = Application.getInstance().getGUILog()
import ActivityHighlighter
reload(ActivityHighlighter)
from ActivityHighlighter import ActHi
debug=1

if __name__ == "__main__":
    # Main test code
    try:
        gl.log( "Starting XML-RPC client...")
        ##
        ## This also serves as an example of client's interaction with the
        ## ActivityRunner server
        #
        import xmlrpclib
        s = xmlrpclib.ServerProxy('http://localhost:%d' % XMLRPC_PORT)
        #
        # Debug:  print list of available methods
        gl.log("Proxy to access MD XMLRPC server: s == %s" % repr(s))
        try: 
            blah=s.system.listMethods()
            if debug==1:
                gl.log("DEBUG: " + str(blah))
        except:
            try:
                #gl.log("server not started (or some other error...)")
                #os.chdir("/Users/mjackson/Desktop")
                #os.system("source start_server.sh")
                blah=s.system.listMethods()        
            except:
                #s.terminate()
                gl.log("*** EXCEPTION:")
                exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
                messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
                for message in messages:
                    gl.log(message)
                pass
        # Now, start an activity
        project = Application.getInstance().getProjectsManager().getActiveProject()
        ExAct=StereotypesHelper.getStereotype(project,"ActivityExecutable")
        activityOptions=StereotypesHelper.getExtendedElements(ExAct)
        ao=[]
        for a in activityOptions:
            ao.append(a.getName())
        
        options=["Submit","Cancel"]
        textPanel=JPanel(GridLayout(4,0))
        actlabel=JLabel("Activity Name")
        actname=JComboBox(ao)
        inputlabel=JLabel("Input Parameters")
        inputs=JTextField()
        textPanel.add(actlabel)
        textPanel.add(actname)
        textPanel.add(inputlabel)
        textPanel.add(inputs)
        frame=JFrame("Start Activity")
        a=JOptionPane.showOptionDialog(frame,
                               textPanel,
                               "Start Activity: ",
                               JOptionPane.YES_NO_OPTION,
                               JOptionPane.QUESTION_MESSAGE,
                               None,
                               options,
                               options[1])

        #Edit both package name and block name       
        if a==JOptionPane.YES_OPTION:
            activity=actname.getSelectedItem()
            params=inputs.getText()
            #validate format!
            #if invalid, call window again?
            if debug==1:
                gl.log("Starting activity " + activity + " with parameters " + params)
            project = Application.getInstance().getProjectsManager().getActiveProject()
            ah=ActHi(project,False)
            ah.repaintDiagram(activity)
            start=s.start(activity, params)
            #gl.log(str(start))
            if start is True:
                gl.log(activity + " has successfully started!")
            else:
                gl.log(str(start))                
  
  
        elif a==JOptionPane.NO_OPTION:
            pass
        else:
            pass
        
        

    except KeyboardInterrupt:
        print s.terminate()
        sys.exit(0)
    except Exception:
        #print s.terminate()
        gl.log( "Blurrbbhhh exception")
        raise
