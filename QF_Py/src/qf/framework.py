
# Since part of the miros, It is licensed under the same terms as Python itself.
#
"""
NAME: framework.py
DESCRIPTION: Emulates the QF Framework class that provides infrastructure
             publish/subscribe functionality for all events.
AUTHOR: Leonard J. Reder
EMAIL:  reder@jpl.nasa.gov
DATE CREATED:
"""
from __future__ import with_statement # 2.5 only
# Python Modules
import threading
import time
import sys
import re

import active
import event
import framework_gui

class QF(object):
    """
    QF services.

    This class groups together QF services.
    Publish/subscribe messaging system.
    Implementation of tick method to dispatch Timer events.
    A Singleton used by all Active objects.
    """
    __instance = None
    #
    # Flag indicating whether we're in interactive mode
    __interactive = False
    #
    # List of registered active objects.
    # Not to be used by Clients directly.
    # 
    __active = []
    #
    # Dict of registered active objects.
    # Each state machine type is a list of instances.
    # @todo:  add instance name interface to autocode state machine so we can use this as key
    #
    __active_dict = dict()
    #
    # List of active timer event objects.
    #
    __timers = []
    #
    # Maximum priority of active objects
    __qf_max_active = 0
    #
    # This data type represents a set of active objects that
    # subscribe to a given signal.  Rather than using arrays of bits this
    # data type is a Python dictionary.  Each key is a signal with each
    # value being a list of subscribing active objects.
    __subscribe_list = dict()
    
    # Timer lock so any active object can arm a timer event.
    __timer_lock = None
    # Gui singleton handle
    __qf_gui = None
    
    def __init__(self):
        """
        Constructor.
        """
        self.__active = []
        self.__active_dict = dict()
        self.__qf_max_active = 0
        self.__timer_lock = threading.RLock()


    def getInstance():
        """
        Return instance of singleton.
        """
        if(QF.__instance is None):
            QF.__instance = QF()
        return QF.__instance

    def destroy():
        """
        Destroys singleton instance to allow reloading the QF framework.
        
        We attempt thorough cleansing to ensure no stale data sticks around.
        """
        if QF.__instance is not None:
            qf = QF.__instance
            if qf.__qf_gui != None:
                qf.__qf_gui.destroy()
                qf.__qf_gui = None
            # iterate through all actives and 'quit' and remove them
            for active in qf.__active:
                active.enqueueEvent(event.Quit())
            qf.__active_dict = None
            for sig in qf.__subscribe_list:
                del qf.__subscribe_list[sig][0:]
            qf.__subscribe_list = None
            qf.__timers = None
            qf.__timer_lock = None
            # unset singleton instance
            QF.__instance = None
            #print "*** Destroyed QF singleton %s! ***" % qf

    # define static methods
    getInstance = staticmethod(getInstance)
    destroy = staticmethod(destroy)
    
    def init(self, qf_gui=False, qf_max_active=20,
             state_charts_h_file="./StatechartSignals.h", gui_py_file="./gui.py",
             notify_dropped_events=False):
        """
        QF initialization.
        
        This function initializes QF and must be called exactly once before
        any other QF function.
        """
        self.__qf_max_active = qf_max_active
        # set flag for whether to notify of dropped events
        self.notify_dropped_events = notify_dropped_events
        #
        # Launch the GUI here.
        if qf_gui == True:
            self.__qf_gui = framework_gui.QFGUI.getInstance(state_charts_h_file, gui_py_file)
            # no need to delay, as GUI instantiation will wait until ready


    def updateGui(self, msg):
        if self.__qf_gui is not None:
            self.__qf_gui.updateGui(msg)


    def run(self, interactive=False):
        """
        Transfers control to QF to run the application.
        If interactive==True use sendEvent to control state machines.
        """
        self.__interactive = interactive

        self.onStartup()
        
        if interactive == False:
            self.consoleRun()
            self.onCleanup()


    def sendEvent(self, e):
        """
        Send an Event, a tick, a signal or quit.
        """
        if type(e) == type('str'):
            sType = e
            if sType == "quit":
                e = event.Quit()
                self.publish(e)
                self.onCleanup()
                return
            elif sType == "tick":
                self.tick()
            else:
                # publish to active objects that have subscribed.
                e = event.Event(sType)
                # @todo: add command line parsing of optional data args
                self.publish(e)
        elif isinstance(e, event.Event) == True:
            self.publish(e)
        else:
            print "ERROR: invalid arg, must be event or signal or quit or tick"
            raise


    def sendAndWait(self, signal, delay):
        """
        Send an event and wait for 'delay' seconds.
        """
        self.sendEvent(signal)
        time.sleep(delay)


    def consoleRun(self):
        """
        Run a console loop to input command events.
        """
        print("\nInteractive Active Object Hierarchical State Machine event processing")
        print("Enter 'quit' to end.\n")

        while True:
            # get letter of event
            sType = raw_input("\nEvent<-")
            if sType == "quit":
                e = event.Quit()
                self.publish(e)
                return
            elif sType == "tick":
                self.tick()
            else:
                # publish to active objects that have subscribed.
                e = event.Event(sType)
                # @todo: add command line parsing of optional data args
                self.publish(e)


    def onStartup(self):
        """
        Startup QF callback.
        """
        # If there is a GUI start it's handler thread here.
        if self.__qf_gui != None:
            self.__qf_gui.start()
        

    def onCleanup(self):
        """
        Cleanup QF callback.
        """
        # if there is a GUI Quit!
        if self.__qf_gui != None:
            self.__qf_gui.destroy()
        #
        print "Exit: %s\n" % self
        
        # Force exit since some threads not picking this up!
        sys.exit(0)


    def addSignalQueue(self, sig, active):
        """
        Subscribe an active object to a signal.
        """
        if sig in self.__subscribe_list:
            self.__subscribe_list[sig].append(active)
        else:
            self.__subscribe_list[sig] = [active]
        #print "addSignalQueue: %s for signal %s" % (self.__subscribe_list[sig], sig)


    def publish(self, e):
        """
        Publish event to the framework.
        static void publish(QEvent const *e);
        
        This function posts (using the FIFO policy) the event e to ALL
        active object that have subscribed to the signal e->sig.
        This function is designed to be callable from any part of the system,
        including ISRs, device drivers, and active objects.
        """
        e = self.__ensureEventObject(e)
        #print e.signal
        #print self.__subscribe_list
        if e.signal in self.__subscribe_list:
            for a in self.__subscribe_list[e.signal]:
                a.enqueueEvent(e)


    def tick(self):
        """
        Processes all armed time events at every clock tick.
        
        This function must be called periodically from a time-tick ISR or from
        the highest-priority task so that QF can manage the timeout events.
        
        Note: The QF.tick() function is not reentrant meaning that it must
        run to completion before it is called again. Also, QF.tick() assumes
        that it never will get preempted by a task, which is always the case
        when it is called from the highest-priority task.
        """

        with self.__timer_lock:
            for timer_event in self.__timers:
                ctn = timer_event.getCounter()
                ctn = ctn - 1
                timer_event.setCounter(ctn)
                if ctn == 0:    # is the time evt expired?
                    interval = timer_event.getInterval()
                    if interval != 0:   # is it a periodic time evt?
                        ctn = interval
                        #print "Tick event periodic reset (%s): %d" % (timer_event.signal,ctn)
                    #
                    timer_event.getActive().enqueueEvent(timer_event)
                    print "Tick event fired (%s): %d" % (timer_event.signal,ctn)
                else:
                    pass
                    #print "Tick down (%s): %d" % (timer_event.signal,ctn)
            # one-shot time event, disarm by removing it from the list
            # but only after all have fired.
            for t in self.__timers:
                if t.getCounter() == 0 and t.getInterval() == 0:
                    self.__timers.remove(t)


    def getVersion(self):
        """
        Returns the QF version.
        
        This function returns constant version string in the format x.y.zz,
        where x (one digit) is the major version, y (one digit) is the minor
        version, and zz (two digits) is the maintenance release version.
        An example of the version string is "3.1.03".
        """
        return "0.0.0"
    
    
    def Q_NEW(self, e, sig):
        """
        Allocate an event.

        This returns an event.  The event is initialized with the signal
        sig. 
        """
        return event.Event(sig)


    def add(self, a):
        """
        NOTE WAS PRIVATE IN C++ AND MAY NOT BE NEEDED.
        static void add_(QActive *a);
        
        Register an active object to be managed by the framework

        This function should not be called by the application directly, only
        through the function Active.start(). The priority of the active
        object "a" should be set before calling this function.
        
        Note: This function raises an exception if the priority of the active
        object exceeds the maximum value __qf_max_active.  Also, this function
        raises an exception if the priority of the active object is already in
        use. (QF requires each active object to have a UNIQUE priority.)
        """
        p = a.getPriority()
        
        if p == 0:
            self.__active.append(a)
        else:
            print "Active object priorities not yet implemented."
            raise Exception
        
        # For each active object add a default quit signal
        e = event.Quit()
        self.addSignalQueue(e.signal, a)
        #
        # For access to active objects set up a dict
        # This builds a list of active object for each type
        # to handle multiple instances.
        #
        name = "%s" % a
        name = name.split('Active')[0][1:]
        if name in self.__active_dict:
            self.__active_dict[name].append(a)
        else:  
            self.__active_dict[name] = [a]


    def remove(self, a):
        """
        Remove the active object from the framework.
        static void remove_(QActive const *a);
            
        This function should not be called by the application directly, only
        inside the QF port. The priority level occupied by the active object
        is freed-up and can be reused for another active object.
        
        The active object that is removed from the framework can no longer
        participate in the publish-subscribe event exchange.
        
        Note: This function raises an exception if the priority of the active
        object exceeds the maximum value __qf_max_active or is not used.
        """
        p = a.getPriority()
        
        if p == 0:
            if a in self.__active:
                self.__active.remove(a)
            else:
                print "Active object not found in list: %s" % self.__active
                raise Exception
        else:
            print "Active object priorities not yet implemented."
            raise Exception
        
        # @todo: must remove active object from subscriber list of quit signal.


    def getQFMaxActive(self):
        """
        Return maximum number of active objects permitted.
        """
        return self.__qf_max_active


    def addTimer(self, timer_event):
        """
        Add timer event object to list.
        """
        with self.__timer_lock:
            self.__timers.append(timer_event)

    
    def removeTimer(self, timer_event):
        """
        Remove timer event object from list.
        """
        removed = False
        with self.__timer_lock:
            if timer_event in self.__timers:
                self.__timers.remove(timer_event)
                #print "** %s removed." % timer_event.signal
                removed = True
        # otherwise, timer was not removed
        return removed


    def timerArmed(self, timer_event):
        """
        Return True if timer_event armed (e.g. in list).
        """
        armed = False
        with self.__timer_lock:
            armed = (timer_event in self.__timers)
        return armed


    def getActive(self, smIdx=None):
        """
        Return the Active object given by the index.
        """
        if type(smIdx) == int:
            return self.__active[smIdx]
        else:
            return self.__active

    def getIndexOfName(self, name):
        """
        Return the index of the Active object by the given name
        """
        idx = -1
        for i in range(0, len(self.__active)):
            m = re.match(r"<(" + name + r")Active\(", repr(self.__active[i]))
            if m is not None and m.group(1) == name:  # found it!
                idx = i
                break
        return idx

    def getActiveDict(self):
        """
        Return the Active object dictionary known to this QF framework.
        """
        return self.__active_dict

    def getCurrentState(self, state_machine):
        """
        Return the current active state of the supplied Hsm.
        """
        return state_machine.stateCurrent()['name']


    def __ensureEventObject(self, e):
        """
        Makes sure that the supplied event is an event object.
        If event is a dictionary, converts it to a generic event object.
        """
        if isinstance(e, event.Event):
            # just return the event object
            return e
        #
        # first, create the generic event object
        ev = event.Event(e['sType'])
        # then, populate the event object attributes from dictionary
        for k in e.keys():
            if k != 'sType' and k != 'signal':  # signal already initialized
                setattr(ev, k, e[k])
        return ev

