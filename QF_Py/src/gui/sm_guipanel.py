#!/usr/bin/env python
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
NAME: sm_guipanel.py
DESCRIPTION: State machine GUI panel display
AUTHOR: Will Sun
EMAIL: wsun@jpl.nasa.gov
DATE CREATED: 10/15/2009
"""
# Python Modules
from Tkinter import *
import Tkinter
import Pmw
import sys
import os
import shutil
import logging
import glob
import atexit
from tkFileDialog import *

class StateTraceGUI():
    """
    Define state machines GUI display class
    """	

    def __init__(self, root, title='Statechart Sim/Test Handler'):
        """
        """
        self.root = root
        self.root.title(title)	
        self._modelsetDir = "none"
        
        self.MenuBar()
        self.ModelsetsScrolledlist()
        self.StatemachinesScrolledlist()
	self.EventDropdown()
        self.MessageBox()
#        self.Eventlist()

    def MenuBar(self, parent = None):
        """
        Main menu interface for the GUI
        """
        # Create the Balloon.
        self.balloon = Pmw.Balloon(parent)

        # Create and pack the MenuBar.
        menuBar = Pmw.MenuBar(parent,
                hull_relief = 'raised',
                hull_borderwidth = 2,
                balloon = self.balloon)
        menuBar.pack(fill = 'x')
        self.menuBar = menuBar

        # Add some buttons to the MainMenuBar.
        menuBar.addmenu('File', 'Open this window or exit')
#        menuBar.addcascademenu('File', 'Open (ModelSet)',
#                'Set some other preferences', traverseSpec = 'z', tearoff = 1)

#        sim_state_start.globAutocode()
#        autocode_path = sim_state_start.active_modules
#        autocode_path = sim_state_start.globAutocode(object)
#        autocode_path = '/proj/alab/wsun/Autocoders_wsun/QF-Py2.0/examples/Ares1/autocode'
#        autocode_path = askdirectory()

#        for select in glob.glob(autocode_path + os.sep + "*.py"):
#            model_name = os.path.split(select)[1]
#            menuBar.addmenuitem('Open (ModelSet)', 'command', 'Set select to ' + model_name,
#                    command = PrintOne('Action: Open (ModelSet) ' + model_name),
#                    label = model_name)
#            modelset_data = model_name
	
        menuBar.addmenuitem('File', 'command', 'Open this window',
                command = self.getSMPath,
                label = 'Open (Model_Set)')

        menuBar.addmenuitem('File', 'command', 'Save loggers',
                command = PrintOne('Action: Save Logger'),
                label = 'Save Logger')
        menuBar.addmenuitem('File', 'command', 'New ver. of same panel',
                command = PrintOne('Action: New'),
                label = 'New')
        menuBar.addmenuitem('File', 'separator')
        menuBar.addmenuitem('File', 'command', 'Exit the application',
                command = root.destroy,
                label = 'Exit')

        menuBar.addmenu('View', 'View content')
        menuBar.addmenuitem('View', 'command', 'Tile View',
                command = PrintOne('Action: Tile'),
                label = 'Tile')
        menuBar.addmenuitem('View', 'command', 'Cascade View',
                command = PrintOne('Action: Cascade'),
                label = 'Cascade')
        menuBar.addmenuitem('View', 'command', 'Hide All',
                command = PrintOne('Action: Hide All'),
                label = 'Hide All')
        menuBar.addmenuitem('View', 'command', 'Show All',
                command = PrintOne('Action: Show All'),
                label = 'Show All')

        menuBar.addmenu('Statecharts', 'Turn on/off state machine logger')
#        menuBar.addmenuitem('Options', 'command', 'Set general preferences',
#                command = PrintOne('Action: general options'),
#                label = 'General...')

        # Create a checkbutton menu item.
        self.toggleVar = Tkinter.IntVar()
        # Initialise the checkbutton to 0:
        self.toggleVar.set(0)
        menuBar.addmenuitem('Statecharts', 'checkbutton', 'Toggle me on/off',
                label = 'SM1 Logger',
                command = self._toggleMe,
                variable = self.toggleVar)
        self._toggleMe()
        menuBar.addmenuitem('Statecharts', 'checkbutton', 'Toggle me on/off',
                label = 'SM2 Logger',
                command = self._toggleMe,
                variable = self.toggleVar)
        self._toggleMe()
        menuBar.addmenuitem('Statecharts', 'checkbutton', 'Toggle me on/off',
                label = 'SM3 Logger',
                command = self._toggleMe,
                variable = self.toggleVar)
        self._toggleMe()
        menuBar.addmenuitem('Statecharts', 'checkbutton', 'Toggle me on/off',
                label = 'SM4 Logger',
                command = self._toggleMe,
                variable = self.toggleVar)
        self._toggleMe()

#        menuBar.addcascademenu('Statecharts', 'Size',
#                'Set some other preferences', traverseSpec = 'z', tearoff = 1)
#        for size in ('tiny', 'small', 'average', 'big', 'huge'):
#            menuBar.addmenuitem('Size', 'command', 'Set size to ' + size,
#                    command = PrintOne('Action: size ' + size),
#                    label = size)

        menuBar.addmenu('Help', 'User manuals', name = 'help')
        menuBar.addmenuitem('Help', 'command', 'About this application',
                command = PrintOne('Action: about'),
                label = 'About...')

    def getSMPath(self):
        SM_Path = askdirectory()
        print SM_Path

        self.box.insert(END, SM_Path)
        self.statechartsbox.clear()

        ac = os.path.split(SM_Path)[1]
        print ac

        if os.path.split(SM_Path)[1] != "autocode":
            for select in glob.glob(SM_Path + os.sep + "autocode" + os.sep + "*Active.py"):
                model_name = os.path.split(select)[1]
                SM_name = model_name.rstrip('Active.py')
                self.statechartsbox.insert(END, SM_name)
        else:
            for select in glob.glob(SM_Path + os.sep + "*Active.py"):
                model_name = os.path.split(select)[1]
                SM_name = model_name.rstrip('Active.py')
                self.statechartsbox.insert(END, SM_name)

    def splitpath(origpath):
        basename = os.path.split(origpath)[1]
        if basename == origpath:
            if '\\' in origpath:
                basename = origpath.split('\\')[-1]
            elif '/' in origpath:
                basename = origpath.split('/')[-1]
        return basename

    def ModelsetsScrolledlist(self, parent = None):
        """
        Create and pack the 'Model Sets' scrolledList of the window.
        """
#        data = ('Model Set 1', 'Model Set 2', 'Model Set 3', 'Model Set 4', \
#                'Model Set 5', 'Model Set 6', 'Model Set 7', 'Model Set 8', \
#                'Model Set 9', 'Model Set 10', 'Model Set 11', 'Model Set 12', \
#                '...........', '...........', '...........', '...........')
        self.box = Pmw.ScrolledListBox(self.root,
#                                       items = data,
                                       labelpos='nw',
                                       label_text='Model Sets (single selection):',
                                       listbox_selectmode=SINGLE,
                                       selectioncommand=self.selectionCommand,
                                       dblclickcommand=self.defCmd,
                                       usehullsize = 1,
                                       hull_width = 600,
                                       hull_height = 140,
                                       )

        self.box.pack(side = 'top', fill = 'both', expand = True)

        # Create a Frame to align ButtonBox to right side of panel (east)
        button_box_frame1 = Frame(self.root)
        button_box_frame1.pack(side = 'top', anchor='e')
        # Create and pack the 'Reset', 'Generate' and 'Execute' button boxes under the 'Model Sets'
        self.modelsetsbuttonBox = Pmw.ButtonBox(button_box_frame1,
                                                labelpos="w",
                                                frame_borderwidth = 2,
                                                frame_relief = 'groove')
        self.modelsetsbuttonBox.pack(side = 'right')
        self.modelsetsbuttonBox.add('Delete', text = 'Delete', command = self.DeleteModelset)
#        self.modelsetsbuttonBox.add('Generate', text = 'Generate', command = PrintOne('Action: Generate'))
        self.modelsetsbuttonBox.add('Generate', text = 'Generate', command = self.GenerateStatechart)


    def GenerateStatechart(self, target="-python", opts="", javaOpts=""):
        """
        Invoking shell, executes the autocoder to autocode the test file to Python
        """
        path_selected = self.box.getcurselection()

        if len(path_selected) == 0:
            print 'No selection'
        else:
            self._modelsetDir = path_selected[0]
            print self._modelsetDir

        project = "Ares_I"

        aclink = self._modelsetDir + os.sep + "Autocoder"
#        print aclink
        smfile = self._modelsetDir + os.sep + project + ".xml"
#        print smfile

        if os.path.exists(aclink) and os.path.exists(smfile):
            acDir = self._modelsetDir + os.sep + "autocode"
            if not os.path.exists(acDir):
                os.makedirs(acDir)
            autocoder_jar = self._modelsetDir + os.sep + "Autocoder"\
                          + os.sep + "autocoder" + os.sep + "autocoder.jar"
#            cmd = "cd " + acDir + "; "\
#                + "java " + " -jar " + autocoder_jar + " "\
#                + target + " " + " ../" + project + ".xml; "\
#                + "cd -"

            cmd = "cd " + self._modelsetDir + "; " + "make"
            print cmd
            os.system(cmd)
            self.selectionCommand()
        else:
            print 'NO state machine file and/or Autocoder sym-link in the selected modelset path'

    def SelectedModelset(self):
        """
        Identify the selected modelset path in the 'Model Sets' scrolled list box 
        """
        selection = self.box.curselection()
        print selection

        ModelSetList = self.box.get()
        CurrentList = self.box.getvalue()
        print CurrentList

        for i in selection:
            num = int(i)
            print ModelSetList[num]
            print i

    def DeleteModelset(self):
        """
        Delete the selected modelset path in the 'Model Sets' scrolled list box after 'Delete' button was clicked
        """
        selection = self.box.curselection()

        ModelSetList = self.box.get()

        for i in selection:
            num = int(i)
            print ModelSetList[num]
            print i
            self.box.delete(num)
            self.statechartsbox.clear()

    def StatemachinesScrolledlist(self, parent = None):
        """
        Create and pack the 'Statecharts' scrolledList of the window.
        """
        data = ('State Machine 1', 'State Machine 2', 'State Machine 3', 'State Machine 4', \
                'State Machine 5', 'State Machine 6', 'State Machine 7', 'State Machine 8', \
                'State Machine 9', 'State Machine 10', 'State Machine 11', 'State Machine 12', \
                'State Machine 13', 'State Machine 14', 'State Machine 15', 'State Machine 16', \
                'State Machine 17', 'State Machine 18', 'State Machine 19', 'State Machine 20', \
                '...........', '...........', '...........', '...........')   
        self.statechartsbox = Pmw.ScrolledListBox(self.root,
#                                                  items = data, 
                                                  labelpos='nw',
                                                  label_text='Statecharts (single or multiple selections, no shift key):',
                                                  listbox_selectmode=MULTIPLE,
                                                  selectioncommand=self.selectionCommand,
                                                  dblclickcommand=self.defCmd,
                                                  usehullsize = 1,
                                                  hull_width = 600,
                                                  hull_height = 140,
        )
	
        self.statechartsbox.pack(side='top', fill = 'both', expand = True)

        # Create a Frame to align ButtonBox to right side of panel (east)
        button_box_frame2 = Frame(self.root)
        button_box_frame2.pack(side = 'top', anchor='e')

        # Create and pack the 'Show' and 'Hide' button boxes under the 'Statecharts'
        self.statechartsbuttonBox = Pmw.ButtonBox(button_box_frame2,
                                                  labelpos = 'w',
                                                  frame_borderwidth = 2,
                                                  frame_relief = 'groove')
        self.statechartsbuttonBox.pack(side = 'right')
        self.statechartsbuttonBox.add('Show', text = 'Show', command = self.SelectedSM)
        self.statechartsbuttonBox.add('Hide', text = 'Hide', command = PrintOne('Action: Hide'))

    def SelectedSM(self):
        """
        Identify the selected state machine name in the 'Statecharts' scrolled list box 
        """
        selection_SM = self.statechartsbox.curselection()

        SMList = self.statechartsbox.get()

        for j in selection_SM:
            index_num = int(j)
            print SMList[index_num]
            print j 

    def _toggleMe(self):
    	"""
	"""
        print 'Toggle value:', self.toggleVar.get()

    def add(self):
        if len(self.testMenuList) == 0:
            num = 0
        else:
            num = self.testMenuList[-1]
        num = num + 1
        name = 'Menu%d' % num
        self.testMenuList.append(num)

        self.menuBar.addmenu(name, 'This is ' + name)

    def delete(self):
        if len(self.testMenuList) == 0:
            self.menuBar.bell()
        else:
            num = self.testMenuList[0]
            name = 'Menu%d' % num
            del self.testMenuList[0]
            self.menuBar.deletemenu(name)

    def additem(self):
        if len(self.testMenuList) == 0:
            self.menuBar.bell()
        else:
            num = self.testMenuList[-1]
            menuName = 'Menu%d' % num
            menu = self.menuBar.component(menuName)
            if menu.index('end') is None:
                label = 'item X'
            else:
                label = menu.entrycget('end', 'label') + 'X'
            self.menuBar.addmenuitem(menuName, 'command', 'Help for ' + label,
                    command = PrintOne('Action: ' + menuName + ': ' + label),
                    label = label)
            
    def deleteitem(self):
        if len(self.testMenuList) == 0:
            self.menuBar.bell()
        else:
            num = self.testMenuList[-1]
            menuName = 'Menu%d' % num
            menu = self.menuBar.component(menuName)
            if menu.index('end') is None:
                self.menuBar.bell()
            else:
                self.menuBar.deletemenuitems(menuName, 0)
	
    def EventDropdown(self, parent = None):
        """
        Create and pack the dropdown ComboBox.
	"""

        # Create a Frame to align event dropdown and 'Send' button
        event_frame = Frame(self.root)
        event_frame.pack(side = 'top', fill = 'both')

        events = ('             ', 'Event 1, 1, 2', 'Event 2, 1, 2', 'Event 3, 1, 2', 'Event 4, 1, 2',
                  'Event 5, 1, 2', 'Event 6, 1, 2', 'Event 7, 1, 2', 'Event 8, 1, 2',
                  'Event 9, 1, 2', 'Event 10, 1, 2', 'Event 11, 1, 2', 'Event 12, 1, 2')
        dropdown = Pmw.ComboBox(event_frame,
                   label_text = 'Event, Args: ',
                   labelpos = 'w',
                   selectioncommand = self.changeEvent,
                   scrolledlist_items = events,
                   )
        dropdown.pack(side = 'left', anchor = 'w',
                      fill = 'both', expand = True, padx = 8, pady = 8)

        # Display the first colour.
        first = events[0]
        dropdown.selectitem(first)
        self.changeEvent(first)

        # Create and pack the 'Send' button boxes on the right of the event drop box:
        self.sendBox = Pmw.ButtonBox(event_frame,
                                     labelpos = 'e',
                                     frame_borderwidth = 0,
                                     frame_relief = 'groove')
        self.sendBox.pack(side = 'right', anchor = 'e', fill = 'x', expand = True)
        self.sendBox.add('Send', text = 'Send', command = PrintOne('Action: Send'))
    

    def changeEvent(self, event):
        print 'Event, Args: ' + event

    def changeText(self, text):
        print 'Text: ' + text
	

    def MessageBox(self, parent = None):
    	"""
	Scrolled text widget for messages/loggers
	"""
        # Create the Scrolledtext widget
        fixedFont = Pmw.logicalfont('Fixed')
        self.st = Pmw.ScrolledText(parent,
                borderframe = 1,
                labelpos = 'n',
                label_text='Messages/Loggers',
                usehullsize = 1,
                hull_width = 200,
                hull_height = 200,
                text_wrap='none',
                text_padx = 4,
                text_pady = 4,
	)

	self.st.appendtext('This is a test message:\nFile name: sm_guipanel.py\n\n')
	
	self.st.importfile('sm_guipanel.py')
	
        self.st.pack(side = 'right', padx = 5, pady = 5, fill = 'x', expand = 1)


#    def Eventlist(self, parent = None):
#    	"""
#	Scrolled list widget for events
#	"""
        # Create the ScrolledListBox.
#        self.box = Pmw.ScrolledListBox(parent,
#        self.box = Pmw.ScrolledListBox(self.root,
#                items=('Sydney', 'Melbourne', 'Brisbane'),
#                labelpos='nw',
#                label_text='Events:',
#                listbox_height = 6,
#                selectioncommand=self.selectionCommand,
#                dblclickcommand=self.defCmd,
#                usehullsize = 2,
#                hull_width = 200,
#                hull_height = 200,
#        )

#        self.setvscrollmode()

#        buttonBox = Pmw.ButtonBox(self.root)
#        buttonBox.pack(side = 'bottom')
#        buttonBox.add('Show', text = 'Show', command = PrintOne('Action: Show'))
#        buttonBox.add('Hide', text = 'Hide', command = PrintOne('Action: Hide'))

        # Pack this last so that the buttons do not get shrunk when
        # the window is resized.
#        self.box.pack(fill = 'both', side = 'right', expand = 1, padx = 5, pady = 5)

        # Do this after packing the scrolled list box, so that the
        # window does not resize as soon as it appears (because
        # alignlabels has to do an update_idletasks).
#        Pmw.alignlabels((hmode, vmode))

        # Add some more entries to the listbox.
#        items = ('Event1', 'Event2', 'Event3', 'Event4', \
#	         'Event5', 'Event6', 'Event7', 'Event8', \
#		 'Event9', 'Event10', 'Event11', 'Event12', \
#		 'Event13', 'Event14', 'Event15', 'Event16', \
#		 '...........', '...........', '...........', '...........')
#        self.box.setlist(items)

#    def sethscrollmode(self, tag):
#        self.box.configure(hscrollmode = tag)

#    def setvscrollmode(self, tag = None):
#        self.box.configure(vscrollmode = 'static')

    def selectionCommand(self):
        sels = self.box.getcurselection()
        if len(sels) == 0:
            print 'No selection'
        else:
            print 'Selection:', sels[0]
            self.statechartsbox.clear()

            ac = os.path.split(sels[0])[1]
            print ac

            if os.path.split(sels[0])[1] != "autocode":
                for select in glob.glob(sels[0] + os.sep + "autocode" + os.sep + "*Active.py"):
                    model_name = os.path.split(select)[1]
                    SM_name = model_name.rstrip('Active.py')
                    self.statechartsbox.insert(END, SM_name)
            else:
                for select in glob.glob(sels[0] + os.sep + "*Active.py"):
                    model_name = os.path.split(select)[1]
                    SM_name = model_name.rstrip('Active.py')
                    self.statechartsbox.insert(END, SM_name)

    def defCmd(self):
        sels = self.box.getcurselection()
        if len(sels) == 0:
            print 'No selection for double click'
        else:
            print 'Double click:', sels[0]

    def showYView(self):
        print self.box.yview()

    def pageDown(self):
        self.box.yview('scroll', 1, 'page')

    def centerPage(self):
        top, bottom = self.box.yview()
        size = bottom - top
        middle = 0.5 - size / 2
        self.box.yview('moveto', middle)

class PrintOne:
    """
    Callback utility
    """
    def __init__(self, text):
        self.text = text

    def __call__(self):
        print self.text

if __name__ == "__main__":
    root = Tk()
    Pmw.initialise(root)

    statetracegui = StateTraceGUI(root)
    root.mainloop()
