from com.nomagic.magicdraw.actions import MDActionsCategory
from com.nomagic.magicdraw.actions import DiagramContextAMConfigurator
from com.nomagic.magicdraw.ui.actions import DefaultDiagramAction
from com.nomagic.magicdraw.uml.symbols import DiagramPresentationElement
from com.nomagic.magicdraw.uml.symbols import PresentationElement
from com.nomagic.magicdraw.actions import *
from javax.swing import JOptionPane
from com.nomagic.actions import *
from com.nomagic.magicdraw.core import Application

from com.nomagic.magicdraw.uml import *
from java.lang import String
import os
import sys
import traceback

class ExampleAction( MDAction ):
	def __init__(self, name):
		MDAction.__init__( self,"", name, None, None )

	def actionPerformed(self, event):
		filename = pluginDescriptor.getPluginDirectory().getAbsolutePath()+os.sep+self.getName()+".py"
		try:
			execfile( filename )
		except :
			gl = Application.getInstance().getGUILog()
			exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
			gl.log("*** EXCEPTION:")
			messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
			for message in messages:
				gl.log(message)
		
	
# Configurator adds new menu item to First menu in main menu
class MainMenuConfigurator( AMConfigurator):
	
	def configure( self,manager ):
		try:
			category=MDActionsCategory( "", "" )
			category.addAction( ExampleAction("Start_Animator") )
			category.addAction( ExampleAction("Step") )
			category.addAction( ExampleAction("Stop_Animator") )
			category.addAction( ExampleAction("Terminate_Server") )
			manager.getCategories().get(0).addAction( category )
		except:
			print "exception"
	def getPriority(self):
		return AMConfigurator.LOW_PRIORITY
	
class DiagramConfigurator(DiagramContextAMConfigurator):

    def configure(self,manager,diagram,selected,requestor):
        try:
            category=MDActionsCategory("", "")
            manager.addCategory(category)
        except:
            print "exception"

    def getPriority(self):
        return AMConfigurator.LOW_PRIORITY

class ActDiagramConfigurator(DiagramContextAMConfigurator):
    
    def configure(self,manager,diagram,selected,requestor):
        try:
            category=MDActionsCategory("", "")
            manager.addCategory(category)
        except:
            print "exception"
        
    def getPriority(self):
        return AMConfigurator.LOW_PRIORITY

class BrowserContextAMConfigurator(BrowserContextAMConfigurator):
    def configure (self,manager,tree):
        try:
            category=MDActionsCategory("","")
            manager.addCategory(category)
        except:
            print "exception"

    def getPriority(self):
        return AMConfigurator.LOW_PRIORITY
	

			
	
# Script starts here
print "Starting script, descriptor", pluginDescriptor
#JOptionPane.showMessageDialog(None,"HIIII")
ActionsConfiguratorsManager.getInstance().addMainMenuConfigurator( MainMenuConfigurator() )
#ActionsConfiguratorsManager.getInstance().addContainmentBrowserContextConfigurator(BrowserContextAMConfigurator())
#ActionsConfiguratorsManager.getInstance().addBaseDiagramContextConfigurator("Class Diagram", DiagramConfigurator())
#ActionsConfiguratorsManager.getInstance().addBaseDiagramContextConfigurator("Activity Diagram", ActDiagramConfigurator())