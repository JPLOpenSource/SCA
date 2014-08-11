"""
Miros - A Python module that implements a Hierarchical State Machine (HSM)
class (i.e. one that implements behavioral inheritance).
     
It is based on the excellent work of Miro Samek (hence the module name
"miros"). This implementation closely follows an older C/C++
implementation published in the 8/00 edition of "Embedded Systems"
magazine by Miro Samek and Paul Montgomery under the title "State
Oriented Programming". The article and code can be found here:
http://www.embedded.com/2000/0008.
     
A wealth of more current information can be found at Miro's well kept
site: http://www.state-machine.com/.
      
As far as I know this is the first implementation of Samek's work in
Python. It was tested with Python 2.5
     
It is licensed under the same terms as Python itself.
     
-----------------------------------------------------------
Change history.
Date        Comments
-----------------------------------------------------------
  
2/15/09    Tom Schmit, Began porting from Lua version.
2/22/09    TS, test_non_interactive() and test_interactive() run as expected.

2/13/13    SWC added private 'ignore_dropped' and 'handled' flags to coordinate
           SubMachine dispatch of event, so there's a way to know whether the
           SubMachine dropped an event.

"""
class Hsm():
    """
    """
    revision = "1165"
    #
    # ====================================
    # Instantiates a new HSM.
    def __init__(self, notify_dropped_events=False):
        self.hsm = {}
        # import pprint
        # self.pp = pprint.PrettyPrinter(indent = 4)
        self.__notify_dropped_events = notify_dropped_events
        self.__handled = False
        self.__ignore_dropped = False
    #
    # ====================================
    # Adds a new state to the HSM. Specifies name, handler function, parent
    # state, and initializes an empty cache for toLca. These are added to Hsm
    # as a sub-table that is indexed by a reference to the associated handler
    # function. Note that toLca is computed when state transitions. It is the
    # number of levels between the source state and the least/lowest common
    # ancestor is shares with the target state.
    def addState(self, sName, fHandler, fSuper):
        self.hsm[fHandler] = dict( name = sName,
                                   handler = fHandler,
                                   super = fSuper,
                                   toLcaCache = {}
                                   # toLca = 0
                                 )
        # print("addState:", sType, self.hsm[fHandler], fHandler, fSuper)
    #
    # ====================================
    # Displays parent-child relationship of states.
    def dump(self):
        # self.pp.pprint(self.hsm)
        print
        for state in self.hsm:
            print "State: " + self.hsm[state]['name'] + "\t", self.hsm[state]
    #
    # ====================================
    # Starts HSM. Enters and starts top state.
    def onStart(self, top):
        # expandable table containing event parameters sent to HSM
        if "tEvt" in dir(self):
            # [SWC 2010.04.16] for submachine entry, don't clobber event
            # make sure to set sType to "entry" first
            self.tEvt['sType'] = "entry"
        else:
            self.tEvt = {'sType': "entry"}
        self.rCurr = self.hsm[top]
        # self.pp.pprint(self.rCurr)
        self.rNext = 0
        # handler for top sets initial state
        #self.rCurr['handler'](self)
        self.rCurr['handler']()
        # get path from initial state to top/root state
        while True:
            self.tEvt['sType'] = "init"
            #self.rCurr['handler'](self)
            self.rCurr['handler']()
            if self.rNext == 0:
                break
            entryPath = []
            s = self.rNext
            while s != self.rCurr:
            #while s != self.rCurr and s['super'] != None:
                # trace path to target
                entryPath.append(s['handler'])
                s = self.hsm[s['super']]
                # s = self.hsm.get(s['super'], None)
            # follow path in reverse calling each handler
            self.tEvt['sType'] = "entry"
            entryPath.reverse()
            for h in entryPath:
                # retrace entry from source
                # self.pp.pprint(h)
                #h(self)
                h()
            self.rCurr = self.rNext
            #    print "\n",self.rCurr
            self.rNext = 0
    #
    # ====================================
    # Dispatches events.
    def dispatch(self, event):
        """
        Accept event object and create a tEvt
        dict for internal use by states.
        
        If it is not an event object, then treat
        as a HSM dict and convert to event.
        """
        #print "+> Type of event is %s" % type(event)
        if isinstance(event, type(dict())):  # make a copy of the dict
            self.tEvt = event.copy()
            self.onEvent(event['sType'])
        else:
            self.tEvt = event.__dict__.copy()  # copy of the event object
            self.onEvent(event.signal)

    def onEvent(self, sType):
        self.tEvt['sType'] = sType
        s = self.rCurr
        self.rSource = 0
        i = 0
        while True:
            i += 1
            # level of outermost event handler
            self.rSource = s
            self.tEvt['sType'] = s['handler']()
            # processed?
            if self.tEvt['sType'] == 0:
                # state transition taken?
                if self.rNext != 0:
                    entryPath = []
                    s = self.rNext
                    while s != self.rCurr:
                        # trace path to target
                        entryPath.append(s['handler'])
                        s = self.hsm[s['super']]
                    # follow path in reverse from LCA calling each handler
                    self.tEvt['sType'] = "entry"
                    entryPath.reverse()
                    for h in entryPath:  # retrace entry from source
                        h()
                    self.rCurr = self.rNext
                    self.rNext = 0
                    while True:
                        self.tEvt['sType'] = "init"
                        self.rCurr['handler']()
                        if self.rNext == 0:
                            break
                        entryPath = []
                        s = self.rNext
                        while s != self.rCurr:
                            # record path to target
                            entryPath.append(s['handler'])
                            s = self.hsm[s['super']]
                        # follow path in reverse calling each handler
                        self.tEvt['sType'] = "entry"
                        entryPath.reverse()
                        for h in entryPath:  # retrace entry
                            h()
                        self.rCurr = self.rNext
                        self.rNext = 0
                # event processed
                break
            #
            s = self.hsm[s['super']]
            if s['name'] == "top" and self.__notify_dropped_events:
                # we've reached the top, report event NOT processed
                print "Dropped event: %s in '%s' by \"%s\"" %\
                    (sType, self.rCurr['name'], repr(self).split("Active(")[0][1:])
        return 0
    #
    # ====================================
    # Exits current states and all super states up to LCA.
    def exit(self, toLca):
        s = self.rCurr
        self.tEvt['sType'] = "exit"
        while s != self.rSource:
        # while s != self.rSource and s['super'] != None:
            #s['handler'](self)
            s['handler']()
            s = self.hsm[s['super']]
            # s = self.hsm.get(s['super'], None)
        while toLca != 0:
            toLca = toLca - 1
            #s['handler'](self)
            s['handler']()
            s = self.hsm[s['super']]
            # s = self.hsm.get(s['super'], None)
        self.rCurr = s
    #
    # ====================================
    # Finds number of levels to LCA (least common ancestor).
    def toLCA(self, Target):
        toLca = 0
        if self.rSource == Target:
            return 1
        s = self.rSource
        while s != None:
            t = Target
            while t != None:
                if s == t:
                    return toLca
                t = self.hsm.get(t['super'], None)
            toLca = toLca + 1
            s = self.hsm.get(s['super'], None)
        return 0
    #
    # ====================================
    # Transitions to new state.
    # ==========================
    # Cached version.
    def stateTran(self, rTarget):
        # self.pp.pprint(self.rCurr)
        # print "\nCurrent state: ", self.rCurr; print "Source state: ", self.rSource
        if self.rCurr['toLcaCache'].get(self.tEvt['sType'], None) == None:
            self.rCurr['toLcaCache'][self.tEvt['sType']] = self.toLCA(self.hsm[rTarget])
        # self.pp.pprint(self.rCurr); print
        self.exit(self.rCurr['toLcaCache'][self.tEvt['sType']])
        self.rNext = self.hsm[rTarget]
    #
#       # ==========================
#       # non-cached version
#       def stateTran(self, rTarget):
#               toLca = self.toLCA(self.hsm[rTarget])
#               self.exit(toLca)
#               self.rNext = self.hsm[rTarget]
    #     
    # ====================================
    # Sets initial state.
    def stateStart(self, Target):
        self.rNext = self.hsm[Target]
    #
    # ====================================
    # Gets current state.
    def stateCurrent(self):
        return self.rCurr
    #
    # ====================================
    # Sets 'ignore_dropped' flag
    def setIgnoreDropped(self, newFlag):
        """
        Sets a flag that, if True, a dropped event on the subsequent dispatch()
        will NOT result in a warning about dropped event 
        """
        self.__ignore_dropped = newFlag
    #
    # ====================================
    # Gets 'handled' flag
    def isHandled(self):
        """
        Returns whether the last dispatched event was handled or not
        """
        return self.__handled


# ===================================================================
#
if __name__ == "__main__":
    pass
