from com.nomagic.magicdraw.core import Application
from com.nomagic.magicdraw.core import Project
import re
import os
import sys
import traceback
import time
import MdGuiServer
reload(MdGuiServer)
from MdGuiServer import MdAnimationUpdater


#######################################
##set up project / script parameters
#######################################
gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProjectsManager().getActiveProject()

# Test code, starts a standalone server on port 8150
gl.log("Starting XML RPC Server")
print "---------------------------------------"
testServer = MdAnimationUpdater(project, 8154, True)
testServer.serve()
#time.sleep(10)
#print("quitting...")
#gl.log("quitting...")
#testServer.endSession()



#ah=ActHi(project)
#ah.highlight("Activity1","A1","on")
#ah.highlight("Activity1","A2","on")
#ah.highlight("Activity1","A2","off")
#ah.clearAll()