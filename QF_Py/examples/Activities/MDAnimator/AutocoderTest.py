from com.nomagic.magicdraw.core import Application
from com.nomagic.magicdraw.core import Project
import re
import os
import sys
import traceback
import time
import xmlrpclib

import xml.parsers.expat
xml.parsers.expat._xerces_parser = "com.sun.org.apache.xerces.internal.parsers.SAXParser"

#######################################
##set up project / script parameters
#######################################
gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProjectsManager().getActiveProject()

#ah=ActHi(project)
#ah.highlight("Activity1","A1","on")
#ah.highlight("Activity1","A2","on")
#ah.highlight("Activity1","A2","off")
#ah.clearAll()


XMLRPC_PORT = 8150
# Main test code
try:
    gl.log("Starting XML-RPC client, project %s..." % project)
    ##
    ## This also serves as an example of client's interaction with the
    ## ActivityRunner server
    #
    s = xmlrpclib.ServerProxy('http://localhost:%d' % XMLRPC_PORT)
    #
    # Debug:  gl.log(str(list of available methods))
    gl.log("Proxy to access MD XMLRPC server: s == %s" % repr(s))
    gl.log("Available XML-RPC server methods: %s" % s.system.listMethods())
    #
    # Now, start an activity
    gl.log("Starting activity IncrementExample...")
    gl.log(str(s.start("IncrementExample", "input=0, input1='x', input1='y'")))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.step()))
    gl.log(str(s.stop()))
    gl.log(str("Done running activity IncrementExample, terminating server..."))
    gl.log(str(s.terminate()))
except KeyboardInterrupt:
    gl.log(str(s.terminate()))
    raise
except Exception:
    gl.log(str(s.terminate()))
    raise
