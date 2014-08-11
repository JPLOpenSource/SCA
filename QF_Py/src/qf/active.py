
# Since part of the miros, It is licensed under the same terms as Python itself.
#
"""
NAME: active.py
DESCRIPTION: Emulates the QF Active class that provides infrastructure
             thread functionality for each state machine.
AUTHOR: Leonard J. Reder
EMAIL:  reder@jpl.nasa.gov
DATE CREATED:
"""
from __future__ import with_statement # 2.5 only
# Python Modules
import threading
import Queue

# QF Modules
import miros
import framework
import event

class Active(miros.Hsm, threading.Thread):
    """
    Base class for derivation of application-level active object
    classes.

    Active is the base class for derivation of active objects. Active objects
    in QF are encapsulated tasks (each embedding a state machine and an event
    queue) that communicate with one another asynchronously by sending and
    receiving events. Within an active object, events are processed
    sequentially in a run-to-completion (RTC) fashion, while QF encapsulates
    all the details of thread-safe event exchange and queuing.

    Active is not intended to be instantiated directly, but rather
    serves as the base class for derivation of active objects in the
    application code.    
    """
    
    # event-queue type.
    __event_queue = None
    __msg_queue   = None
    
    # priority associated with the active object.
    # 
    __prio        = None

    # The Boolean loop variable determining if the thread routine
    # of the active object is running.

    # This flag is only used with the traditional loop-structured thread
    # routines. Clearing this flag breaks out of the thread loop, which is
    # often the cleanest way to terminate the thread.
    #
    __running    = True
    #
    # QF singlton pointer (do not access when in run state without locks)
    #
    __qf = None


    def __init__(self):
        """
        Constructor
        
        Performs the first step of active object initialization by assigning
        the initial pseudostate to the currently active state of the state
        machine.
    
        The constructor is protected to prevent direct instantiation
        of QActive objects. This class is intended only for derivation
        (abstract class).
        """
        self.__running = True
        self.__qf = framework.QF.getInstance()
        # Create event Queue
        self.__event_queue = Queue.Queue()
        # Add test for class type here to make sure it is inherited.
        miros.Hsm.__init__(self, notify_dropped_events=self.__qf.notify_dropped_events)
        threading.Thread.__init__(self)


    def startActive(self, priority=0, ie=None):
        """
        Starts execution of an active object and registers the object
        with the framework.

        The function takes six arguments.
        @param priority: is the priority of the active object. QF allows you to start
        up to 63 active objects, each one having a unique priority number
        between 1 and 63 inclusive, where higher numerical values correspond
        to higher priority (urgency) of the active object relative to the
        others.  We could do more in the python port but will restrict it so
        the user cannot do inconsistent things.
        @param ie: is an optional initialization event that can be used to pass
        additional startup data to the active object. (Pass NULL if your
        active object does not expect the initialization event).
        """
        #print "Active.start()"
        self.__prio = priority
        qf_max_active = self.__qf.getQFMaxActive()
        
        # Check prio if used.  Priority of zero is not using priority
        if self.__prio == 0:
            pass
        else:
            if self.__prio < 0 and self.__prio > qf_max_active:
                # @todo: add specific Exception
                raise
        #
        # Make QF aware of this active object
        self.__qf.add(self)
        #
        # Initiallize 
        self.initialize()
        # Execute initial transition
        # @todo: add the onStart here once running
        #
        # Flush the queue
        #
        # Set flag for thread run loop
        self.__running = True


    def initialize(self):
        """
        Initialize must be implemented.
        """
        print "Active.Initiallized must be implemented in concrete state class."
        raise Exception


    def run(self):
        """
        Traditional loop-structured thread routine for an active object
        
        This function is a QF ported to a traditional
        RTOS/Kernel functionality. Active.run() is structured as a typical
        endless loop, which blocks on the event queue get() operation of an
        active object. When an event becomes available, it's dispatched to
        the active object's state machine and after this the event is recycled.
        The loop uses the __running flag to terminate and cause Active.run()
        to return which is often the cleanest way to terminate the thread.
        """
        # Endless loop of thread
        while self.__running:
            event_obj = self._receiveOneEvent()
            if event_obj is None:
                break
            #print "%s.run() dispatching: %s" % (threading.currentThread(), event_obj.signal)
            self.dispatch(event_obj)
        print "Exit: %s\n" % self

    def _receiveOneEvent(self):
        """
        Receives one event from the event queue.
        """
        #[SWC 2009.12.01] NOTE: Queue is thread-safe, get() blocks if empty
        event_obj = self.__event_queue.get()
        if isinstance(event_obj, event.Event):
            if isinstance(event_obj, event.Quit):
                self._stop()
                return None
        else:
            print "Dispatch event type error: %s\n" % repr(event_obj)
            raise Exception
        return event_obj

    def _stop(self):
        """
        Stops execution of an active object and removes it from the
        framework's supervision.
        
        The preferred way of calling this function is from within the active
        object that needs to stop (that's why this function is protected).
        In other words, an active object should stop itself rather than being
        stopped by some other entity. This policy works best, because only
        the active object itself "knows" when it has reached the appropriate
        state for the shutdown.
        
        Note: This function is optional in embedded systems where active
        objects never need to be stopped.
        """
        self.__qf.remove(self)
        self.__qf = None
        self.__running = False


    def _sendUpdate(self, str):
        """
        If there is a GUI send an update string.
        """
        self.__qf.updateGui(str)


    def _publish(self, e):
        """
        Publish event to the framework.
        """
        self.__qf.publish(e)


    def _subscribe(self, sig):
        """
        Subscribes for delivery of signal sig to the active object
        
        This function is part of the Publish-Subscribe event delivery
        mechanism available in QF. Subscribing to an event means that the
        framework will start posting all published events with a given signal
        sig to the event queue of the active object.
        """
        self.__qf.addSignalQueue(sig, self)


    def _unsubscribe(self, sig):
        """
        Un-subscribes from the delivery of signal sig to the active object.
        
        This function is part of the Publish-Subscribe event delivery
        mechanism available in QF. Un-subscribing from an event means that
        the framework will stop posting published events with a given signal
        a sig to the event queue of the active object.
        
        Note: Due to the latency of event queues, an active object should NOT
        assume that a given signal sig will never be dispatched to the
        state machine of the active object after un-subscribing from that
        signal. The event might be already in the queue, or just about to be
        posted and the un-subscribe operation will not flush such events.

        Un-subscribing from a signal that has never been subscribed in
        the first place is considered an error and QF will rise an exception.
        """
        pass


    def _defer(self, eq, e):
        """
        Defer an event to a given separate event queue.
        void defer(QEQueue *eq, QEvent const *e);
        
        This function is part of the event deferral support. An active object
        uses this function to defer an event e to the QF-supported native
        event queue eq. QF correctly accounts for another outstanding
        reference to the event and will not recycle the event at the end of
        the RTC step. Later, the active object might recall one event at a
        time from the event queue.
        
        An active object can use multiple event queues to defer events of
        different kinds.
        """
        pass


    def _recall(self,eq):
        """
        Recall a deferred event from a given event queue.
        QEvent const *recall(QEQueue *eq);
        
        This function is part of the event deferral support. An active object
        uses this function to recall a deferred event from a given QF
        event queue. Recalling an event means that it is removed from the
        deferred event queue eq and posted (LIFO) to the event queue of
        the active object.
        
        Active.recall(eq) returns the recalled event to the caller.
        The function returns NULL if no event has been recalled.

        An active object can use multiple event queues to defer events of
        different kinds.
        """
        pass


    def unsubscribeAll(self):
        """
        Un-subscribes from the delivery of all signals to the active object.
        
        This function is part of the Publish-Subscribe event delivery
        mechanism available in QF. Un-subscribing from all events means that
        the framework will stop posting any published events to the event
        queue of the active object.

        Note: Due to the latency of event queues, an active object should NOT
        assume that no events will ever be dispatched to the state machine of
        the active object after un-subscribing from all events.
        The events might be already in the queue, or just about to be posted
        and the un-subscribe operation will not flush such events. Also, the
        alternative event-delivery mechanisms, such as direct event posting or
        time events, can be still delivered to the event queue of the active
        object.
        """
        pass

    def getPriority(self):
        return self.__prio

    def enqueueEvent(self, e):
        """
        Put an event e into the queue for this Active object.
        The Deque object ensure thread-safety with an underlying mutex lock.
        """
        # By default, Queue.put blocks if full
        self.__event_queue.put(e)
