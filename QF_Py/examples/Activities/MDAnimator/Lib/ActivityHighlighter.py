from com.nomagic.magicdraw.core import Application
from com.nomagic.magicdraw.openapi.uml import *
from com.nomagic.magicdraw.ui import DiagramSurfacePainter

from java.awt import *
from java.awt import Dimension
from java.lang import Object

import traceback
import sys
import re
import time

gl = Application.getInstance().getGUILog()
SM = SessionManager

class ActHi(Object):
    def __init__(self, project,debug):
        self.debug=debug
        if self.debug is True:
            gl.log("Starting the Activity Highlighter for " + project.getName())
        self.diagrams=project.getDiagrams()
        self.denMap={}
        self.elementMap={}
        self.painters={}
        
    def getDiagramByName(self,dname):
        d=None
        for diagram in self.diagrams:
            if diagram.getName()==dname:
                d=diagram
                self.mapDiagram(d)
        return d
    
    def getElementByName(self,eName,diagram):
        element=None
        if eName in self.denMap[diagram]:
            element=self.elementMap[diagram][eName]
        return element    
    
    def mapDiagram(self,diagram):
        if diagram not in self.denMap.keys():
            self.denMap[diagram]=[]
            for p in diagram.getPresentationElements():
                if p.getElement() is not None:
                    if p.getElement().getName() is not None:
                        self.denMap[diagram].append(p.getElement().getName())
        if diagram not in self.elementMap.keys():
            dict={}
            for p in diagram.getPresentationElements():
                if p.getElement() is not None:
                    if p.getElement().getName() is not None:
                        dict[p.getElement().getName()]=p
            self.elementMap[diagram]=dict
                    
    
    def highlight(self,diagramName,elementName,state):
        d=self.getDiagramByName(diagramName)
        e=self.getElementByName(elementName,d)
        self.clearAll()
        
        if d is not None and e is not None:
            if state=="on":
                if self.debug is True:
                    gl.log("turning " + e.name + " to " + state)
                painter=autocoderDiagramPainter(e)
                d.getDiagramSurface().addPainter(painter)
                painter.paint(newGraphics2D(),d)
                self.painters[e]=painter
                d.getDiagramSurface().repaint()
            elif state=="off":
                if self.debug is True:
                    gl.log("turning " + e.name + "to " + state)
                d.getDiagramSurface().removePainter(self.painters[e])
                d.getDiagramSurface().repaint()
            else:
                gl.log("HIGHLIGHTER ERROR: invalid state. State must be on or off.")
        else:
            if d is None:
                gl.log("Diagram name is incorrect or missing! Cannot Highlight.")
            elif e is None:
                gl.log("Element name [" + elementName + "] does not appear on diagram!")
            else:
                gl.log("cannot highlight. How did you even get here!?")
            
    def clearAll(self):
        #gl.log("clearing")
        for d in self.denMap.keys():
            if self.debug is True:
                gl.log("clearing " + d.getName())
            for p in d.getDiagramSurface().getPainters():
                d.getDiagramSurface().removePainter(p)
            d.getDiagramSurface().repaint()
    
    def repaintDiagram(self,dname):
        d=self.getDiagramByName(dname)
        if d is not None:
            for p in d.getDiagramSurface().getPainters():
                d.getDiagramSurface().removePainter(p)
            d.getDiagramSurface().repaint()
            if self.debug is True:
                gl.log("repainting...")
        else:
            gl.log("ERROR: No diagram to repaint!")

class autocoderDiagramPainter(DiagramSurfacePainter):
    def __init__(self,element):
        self.element=element
    def paint(self,g,diagram):
        g.setColor(Color.RED)
        bounds=self.element.getBounds()
        bounds.grow(5,5)
        g.draw(bounds)
        
class newGraphics2D(Graphics2D):
    def blah(self):
        gl.log("Hi")
        
        