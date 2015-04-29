
"""
EWTrafficLightActive.py

Python implementation of the EWTrafficLight Statechart model
within a Python Active object.

@todo: Need to fix the initialization of the GUI.
@todo: entry, exit, transition method stubs for user code.
@todo: Log event method for user logging.
"""

import miros
import active
import event
import time_event
import framework
import sys


def printf(format, *args):
    sys.stdout.write(format % args)


class EWTrafficLightActive(active.Active):
    """
    """  
    def __init__(self):
        """
        Constructor
        """
        active.Active.__init__(self)
        # --------------------------------------------------------------------
        #               name                                   parent's
        #                of              event                 event
        #               state            handler               handler
        # --------------------------------------------------------------------
        self.addState ( "top",           self.top,                None)
        self.addState ( "Red",           self.Red,               self.top)
        self.addState ( "NoCars",        self.NoCars,            self.Red)
        self.addState ( "CarWaiting",    self.CarWaiting,        self.Red)
        self.addState ( "Green",         self.Green,             self.top)  
        self.addState ( "Stable",        self.Stable,            self.Green)      
        self.addState ( "Transient",     self.Transient,         self.Green)          
        self.addState ( "Yellow",        self.Yellow,            self.top)
        
        # For GUI messages
        self.__machine_name = "EWTrafficLight"
        self.__window_name = "EW"


    def initialize(self):
        """
        Override active object initialize with
        custom initialize routine.  Mostly used
        to subscribe signals for state machine.
        """
        # Subscribe to signals here.
        self._subscribe("NSRequest")
        self._subscribe("EWCar")
        self._subscribe("EWOK")

        # Timer event objects created here.
        self.__transient_timeout = time_event.TimeEvt(self, "TransientTimerEv")
        self.__yellow_timeout = time_event.TimeEvt(self, "YellowTimerEv")

    
    def sendUpdate(self, state_name, entry_exit):
        """
        Send gui entry or exit messages.
        """
        str = "%s %s%s %s" % (self.__window_name, self.__machine_name, state_name, entry_exit)
        print str
        self._sendUpdate(str)


    def top(self):
        """
        The routine handles initial events and events that get bubbled 
        to the top of the state-machine.  Events that get bubbled to the top can
        either be ignored (return 0) or an error message can be generated.
        """
        if self.tEvt['sType'] == "init":
            self.stateStart(self.Green)
            return 0
        else:
            return 0

       
    def Green(self):
        """
        State Green
        """
        machine = self.__machine_name
        stateName = "Green"
        if self.tEvt['sType'] == "init":
            self.stateStart(self.Stable)
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s\n", machine, stateName, "ENTRY")
            self.sendUpdate(stateName,"ENTRY")
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s\n", machine, stateName, "EXIT")
            self.sendUpdate(stateName,"EXIT")
            return 0
        return self.tEvt['sType']


    def Stable(self):
        """
        State Stable
        """
        machine = self.__machine_name
        stateName = "Stable"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s\n", machine, stateName, "ENTRY")
            self.sendUpdate(stateName,"ENTRY")
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s\n", machine, stateName, "EXIT")
            self.sendUpdate(stateName,"EXIT")
            return 0
        elif self.tEvt['sType'] == "NSRequest":
            printf("%s %s\n", stateName, self.tEvt['sType'])
            self.stateTran(self.Transient)
            return 0
        return self.tEvt['sType']
      

    def Transient(self):
        """
        State Transient
        """
        machine = self.__machine_name
        stateName = "Transient"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s\n", machine, stateName, "ENTRY")
            self.sendUpdate(stateName,"ENTRY")
            self.__transient_timeout.postIn(3)
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s\n", machine, stateName, "EXIT")
            self.sendUpdate(stateName,"EXIT")
            self.__transient_timeout.disarm()
            return 0
        elif self.tEvt['sType'] == "TransientTimerEv":
            printf("%s %s\n", stateName, self.tEvt['sType'])
            self.stateTran(self.Yellow)
            return 0
        return self.tEvt['sType']


    def Red(self):
        """
        State Red
        """
        machine = self.__machine_name
        stateName = "Red"
        if self.tEvt['sType'] == "init":
            self.stateStart(self.NoCars)
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s\n", machine, stateName, "ENTRY")
            self.sendUpdate(stateName,"ENTRY")
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s\n", machine, stateName, "EXIT")
            self.sendUpdate(stateName,"EXIT")
            return 0
        return self.tEvt['sType']


    def NoCars(self):
        """
        State NoCars
        """
        machine = self.__machine_name
        stateName = "NoCars"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s\n", machine, stateName, "ENTRY")
            self.sendUpdate(stateName,"ENTRY")
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s\n", machine, stateName, "EXIT")
            self.sendUpdate(stateName,"EXIT")
            return 0
        elif self.tEvt['sType'] == "EWCar":
            printf("%s %s\n", stateName, self.tEvt['sType'])
            self.stateTran(self.CarWaiting)
            return 0
        return self.tEvt['sType']


    def CarWaiting(self):
        """
        State CarWaiting
        """
        machine = self.__machine_name
        stateName = "CarWaiting"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            publish("EWRequest")
            self._publish(event.Event("EWRequest"))
            printf("%s %s %s\n", machine, stateName, "ENTRY")
            self.sendUpdate(stateName,"ENTRY")
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s\n", machine, stateName, "EXIT")
            self.sendUpdate(stateName,"EXIT")
            return 0
        elif self.tEvt['sType'] == "EWOK":
            printf("%s %s\n", stateName, self.tEvt['sType'])
            self.stateTran(self.Green)
            return 0
        return self.tEvt['sType']
      

    def Yellow(self):
        """
        State Yellow
        """
        machine = self.__machine_name
        stateName = "Yellow"
        if self.tEvt['sType'] == "init":
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("%s %s %s\n", machine, stateName, "ENTRY")
            self.sendUpdate(stateName,"ENTRY")
            self.__yellow_timeout.postIn(1)
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("%s %s %s\n", machine, stateName, "EXIT")
            self.sendUpdate(stateName,"EXIT")
            self.__yellow_timeout.disarm()
            return 0
        elif self.tEvt['sType'] == "YellowTimerEv":
            printf("%s %s\n", stateName, self.tEvt['sType'])
            publish("NSOK")
            self._publish(event.Event("NSOK"))
            self.stateTran(self.Red)
            return 0
        return self.tEvt['sType']


def publish(event):
    print ("*** Publish: " + event);


if __name__ == "__main__":
    import NSTrafficLightActive
    # Instance the QF code for running main thread
    # and create active object.  
    qf = framework.QF.getInstance()
    
    # Framework init
    qf.init(True)
    
    # Active object start up and register with QF
    EW = EWTrafficLightActive()
    NS = NSTrafficLightActive.NSTrafficLightActive()
    # start/initialize HSM
    EW.onStart(EW.top)
    NS.onStart(NS.top)
    #
    EW.startActive()
    NS.startActive()
    #
    EW.start()
    NS.start()
    
    # Run event dispatch loop
    qf.run()


#raw_input("Press return to continue")

