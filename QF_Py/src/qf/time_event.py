
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
import event
import framework


class TimeEvt(event.Event):
    """
    Time Event class

    Time events are special QF events equipped with the notion of time
    passage. The basic usage model of the time events is as follows. An
    active object allocates one or more QTimeEvt objects (provides the
    storage for them). When the active object needs to arrange for a timeout,
    it arms one of its time events to fire either just once (one-shot) or
    periodically. Each time event times out independently from the others,
    so a QF application can make multiple parallel timeout requests (from the
    same or different active objects). When QF detects that the appropriate
    moment has arrived, it inserts the time event directly into the
    recipient's event queue. The recipient then processes the time event just
    like any other event.

    Time events, as any other QF events derive from the Event base
    class.  Typically, you will use a time event as-is, but you can also
    further derive more specialized time events from it by adding some more
    data members and/or specialized functions that operate on the specialized
    time events.

    Internally, the armed time events are organized into a bi-directional
    linked list.  This linked list is scanned in every invocation of the
    QF::tick() function.  Only armed (timing out) time events are in the list,
    so only armed time events consume CPU cycles.

    Note: QF manages the time events in the function QF.tick(), which
    must be called periodically.

    Note: In this version of QF QTimeEvt objects should be allocated
    statically rather than dynamically from event pools. Currently, QF will
    not correctly recycle the dynamically allocated Time Events.
    """
    # the active object that receives the time events.
    __active = None
    
    # the internal down-counter of the time event. The down-counter
    # is decremented by 1 in every QF_tick() invocation. The time event
    # fires (gets posted or published) when the down-counter reaches zero.
    __counter = int(0)

    # the interval for the periodic time event (zero for the one-shot
    # time event). The value of the interval is re-loaded to the internal
    # down-counter when the time event expires, so that the time event
    # keeps timing out periodically.
    __interval = int(0)


    def __init__(self, act, sig):
        """
        The Time Event constructor.
        QTimeEvt(QSignal s);
        
        The most important initialization performed in the constructor is
        assigning a signal to the Time Event.  You can reuse the Time Event
        any number of times, but you cannot change the signal.
        This is because pointers to Time Events might still be held in event
        queues and changing signal could lead to hard-to-detect errors.
        """
        self.__active = act
        self.__counter = 0
        self.__interval = 0
        
        self.__qf = framework.QF.getInstance()

        # Add check for unique signal here.
        event.Event.__init__(self,sig)


    def postIn(self, n_ticks):
        """
        Arm a one-shot time event for direct event posting.
        void postIn(QActive *act, QTimeEvtCtr nTicks)

        Arms a time event to fire in n_ticks clock ticks (one-shot time
        event). The time event gets directly posted (using the FIFO policy)
        into the event queue of the active object act.

        After posting, the time event gets automatically disarmed and can be
        reused for a one-shot or periodic timeout requests.

        A one-shot time event can be disarmed at any time by calling the
        TimeEvt.disarm() function.  Also, a one-shot time event can be
        re-armed to fire in a different number of clock ticks by calling the
        TimeEvt.rearm() function.

        The following example shows how to arm a one-shot time event from a
        state machine of an active object:
        """
        self.__interval = 0
        self._arm(n_ticks)


    def postEvery(self, n_ticks):
        """
        Arm a periodic time event for direct event posting.
        
        Arms a time event to fire every n_ticks clock ticks (periodic time
        event). The time event gets directly posted (using the FIFO policy)
        into the event queue of the active object act.

        After posting, the time event gets automatically re-armed to fire
        again in the specified n_ticks clock ticks.

        A periodic time event can be disarmed only by calling the
        TimeEvt.disarm() function. After disarming, the time event can be
        reused for a one-shot or periodic timeout requests.

        Note: An attempt to reuse (arm again) a running periodic time event
        raises an exception.

        Also, a periodic time event can be re-armed to shorten or extend the
        current period by calling the TimeEvt.rearm() function. After
        adjusting the current period, the periodic time event goes back
        timing out at the original rate.
        """
        self.__interval = n_ticks
        self._arm(n_ticks)


    def disarm(self):
        """
        Disarm a time event.
        uint8_t disarm(void);
        
        The time event gets disarmed and can be reused. The function
        returns True if the time event was truly disarmed, that is, it
        was running. The return of False means that the time event was
        not truly disarmed because it was not running. The False return is
        only possible for one-shot time events that have been automatically
        disarmed upon expiration. In this case the False return means that
        the time event has already been posted or published and should be
        expected in the active object's state machine.
        """
        return self.__qf.removeTimer(self)


    def rearm(self, n_ticks):
        """
        Rearm a time event.
        uint8_t rearm(QTimeEvtCtr nTicks);
        
        The time event gets rearmed with a new number of clock ticks
        n_ticks. This facility can be used to prevent a one-shot time event
        from expiring (e.g., a watchdog time event), or to adjusts the
        current period of a periodic time event. Rearming a periodic timer
        leaves the interval unchanged and is a convenient method to adjust the
        phasing of the periodic time event.
        
        The function returns True if the time event was running as it
        was re-armed. The return of False means that the time event was
        not truly rearmed because it was not running. The False return is only
        possible for one-shot time events that have been automatically
        disarmed upon expiration.  In this case the False return means that
        the time event has already been posted or published and should be
        expected in the active object's state machine.
        """
        self.__counter = n_ticks
        if self.__qf.timerArmed(self) == True:
            self.__qf.removeTimer(self)
            self._arm(n_ticks)
            is_armed = False
        else:
            is_armed = True
        return is_armed


    def _arm(self,n_ticks):
        """
        Arm a time event.
        void arm_(QActive *act, QTimeEvtCtr nTicks);
        """
        # Set down counter
        self.__counter = n_ticks

        # Arm by adding to QF_tick timer list here
        self.__qf.addTimer(self)


    def getCounter(self):
        return self.__counter
    
    def setCounter(self, n_ticks):
        self.__counter = n_ticks
    
    def getInterval(self):
        return self.__interval

    def setInterval(self, interval):
        self.__interval = interval

    def getActive(self):
        return self.__active








