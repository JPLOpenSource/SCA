/*****************************************************************************
* Product:  QF/C platform-independent public interface
* Last Updated for Version: 3.3.00
* Date of the Last Update:  Jan 22, 2007
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
*
* Copyright (C) 2002-2007 Quantum Leaps, LLC. All rights reserved.
*
* This software may be distributed and modified under the terms of the GNU
* General Public License version 2 (GPL) as published by the Free Software
* Foundation and appearing in the file GPL.TXT included in the packaging of
* this file. Please note that GPL Section 2[b] requires that all works based
* on this software must also be made publicly available under the terms of
* the GPL ("Copyleft").
*
* Alternatively, this software may be distributed and modified under the
* terms of Quantum Leaps commercial licenses, which expressly supersede
* the GPL and are specifically designed for licensees interested in
* retaining the proprietary status of their code.
*
* Contact information:
* Quantum Leaps Web site:  http://www.quantum-leaps.com
* e-mail:                  sales@quantum-leaps.com
*****************************************************************************/
#ifndef qf_h
#define qf_h

/** \ingroup qf
* \file qf.h
* \brief QF/C platform-independent public interface.
*
* This header file must be included directly or indirectly
* in all modules (*.c files) that use QF/C.
*/

/****************************************************************************/
#ifndef QF_MAX_ACTIVE
    /** \brief Default value of the macro configurable value in qf_port.h
    */
    #define QF_MAX_ACTIVE  63
#elif (QF_MAX_ACTIVE < 1) || (63 < QF_MAX_ACTIVE)
    #error "QF_MAX_ACTIVE out of range. The valid range is 1..63"
#endif

/****************************************************************************/
#ifndef QF_EVENT_SIZ_SIZE
    /** \brief Default value of the macro configurable value in qf_port.h
    */
    #define QF_EVENT_SIZ_SIZE 2
#endif
#if (QF_EVENT_SIZ_SIZE == 1)
    /** \brief The data type to store the block-size defined based on
    * the macro #QF_EVENT_SIZ_SIZE.
    *
    * The dynamic range of this data type determines the maximum block
    * size that can be managed by the pool.
    */
    typedef uint8_t QEventSize;
#elif (QF_EVENT_SIZ_SIZE == 2)
    typedef uint16_t QEventSize;
#elif (QF_EVENT_SIZ_SIZE == 4)
    typedef uint32_t QEventSize;
#else
    #error "QF_EVENT_SIZ_SIZE defined incorrectly, expected 1, 2, or 4"
#endif

/****************************************************************************/
#ifndef QF_FSM_ACTIVE
    /** \brief the base structure for derivation of the ::QActive structure.
    *
    * By default, the ::QActive structure is derived from ::QHsm. However,
    * if the macro #QF_FSM_ACTIVE is defined, QActive is derived from ::QFsm.
    * Defining QF_FSM_ACTIVE can be advantageous in resource-constraint
    * applications because avoiding QHsm functions saves about 1KB of code
    * space (typically ROM), and QFsm functions are slightly faster than
    * the more general QHsm functions.
    *
    * \sa \ref derivation
    */
    #define QF_ACTIVE_SUPER_               QHsm

    /** \brief The "constructor" of the base structure for ::QActive.
    * \note this macro depends on the setting of the QF_FSM_ACTIVE switch.
    * The valid values are QHsm_ctor_() and QFsm_ctor_().
    * \sa macro ::QF_ACTIVE_SUPER_
    */
    #define QF_ACTIVE_CTOR_(me_, initial_) QHsm_ctor_((me_), (initial_))

    /** \brief The initialization of the ::QActive state machine.
    * \note this macro depends on the setting of the QF_FSM_ACTIVE switch.
    * The valid values are QHsm_init() and QFsm_init().
    * \sa macro ::QF_ACTIVE_SUPER_
    */
    #define QF_ACTIVE_INIT_(a_, e_)        QHsm_init((QHsm *)(a_), (e_))

    /** \brief The dispatching events to the ::QActive state machine.
    * \note this macro depends on the setting of the QF_FSM_ACTIVE switch.
    * The valid values are QHsm_dispatch() and QFsm_dispatch().
    * \sa macro ::QF_ACTIVE_SUPER_
    */
    #define QF_ACTIVE_DISPATCH_(a_, e_)    QHsm_dispatch((QHsm *)(a_), (e_))
#else
    #define QF_ACTIVE_SUPER_               QFsm
    #define QF_ACTIVE_CTOR_(me_, initial_) QFsm_ctor_((me_), (initial_))
    #define QF_ACTIVE_INIT_(a_, e_)        QFsm_init((QFsm *)(a_), (e_))
    #define QF_ACTIVE_DISPATCH_(a_, e_)    QFsm_dispatch((QFsm *)(a_), (e_))
#endif

/****************************************************************************/
/** \brief a name for QActiveTag struct
*
* QActive is the base structure for derivation of active objects. Active
* objects in QF are encapsulated tasks (each embedding a state machine and
* an event queue) that communicate with one another asynchronously by
* sending and receiving events. Within an active object, events are
* processed sequentially in a run-to-completion (RTC) fashion, while QF
* encapsulates all the details of thread-safe event exchange and queuing.
*
* \note QActive is not intended to be instantiated directly, but rather
* serves as the base structure for derivation of active objects in the
* application code.
*
* The following example illustrates how to derive an active object from
* QActive. Please note that the QActive member super_ is defined as the
* FIRST member of the derived struct.
* \include qf_qactive.c
*
* \sa ::QActiveTag for the description of the data members \n \ref derivation
*/
typedef struct QActiveTag QActive;
/** \brief Active Object structure
* \sa ::QActive
*/
struct QActiveTag {
    /** base structure for derivation of QActive.
    * \sa ::QF_ACTIVE_SUPER_ \n \ref derivation
    */
    QF_ACTIVE_SUPER_ super_;

    /** \brief OS-dependent event-queue type.
    *
    * The type of the queue depends on the underlying operating system or
    * a kernel. Many kernels support "message queues" that can be adapted
    * to deliver QF events to the active object. Alternatively, QF provides
    * a native event queue implementation that can be used as well.
    *
    * The native QF event queue is configured by defining the macro
    * #QF_EQUEUE_TYPE as ::QEQueue.
    */
    QF_EQUEUE_TYPE eQueue__;

    /** \brief OS-dependent per-thread object.
    *
    * This data might be used in various ways, depending on the QF port.
    * In some ports osObject_ is used to block the calling thread when
    * the native QF queue is empty. In other QF ports the OS-dependent
    * object might be used differently.
    */
    QF_OS_OBJECT_TYPE osObject__;

    /** \brief OS-dependent representation of the thread of the active object.
    *
    * This data might be used in various ways, depending on the QF port.
    * In some ports thread__ is used to store the thread handle. In other
    * ports thread_ can be the pointer to the Thread-Local-Storage (TLS).
    */
    QF_THREAD_TYPE thread__;

    /** \brief QF priority associated with the active object.
    * \sa QActive_start()
    */
    uint8_t prio__;

    /** \brief The Boolean loop variable determining if the thread routine
    * of the active object is running.
    *
    * This flag is only used with the traditional loop-structured thread
    * routines. Clearing this flag breaks out of the thread loop, which is
    * often the cleanest way to terminate the thread. The following example
    * illustrates the thread routine for Win32:
    * \include qf_run.c
    */
    uint8_t running__;
};

/* public methods */
/** \brief Starts execution of an active object and registers the object
* with the framework.
*
* The function takes seven arguments.
* \a me is a pointer to the active object structure.
* \a prio is the priority of the active object. QF allows you to start up
* to 63 active objects, each one having a unique priority number between
* 1 and 63 inclusive, where higher numerical values correspond to higher
* priority (urgency) of the active object relative to the others.
* \a qSto[] and \a qLen arguments are the storage and size of the event
* queue used by this active object.
* \a stkSto and \a stkSize are the stack storage and size in bytes. Please
* note that a per-active object stack is used only when the underlying OS
* requies it. If the stack is not required, or the underlying OS allocates
* the stack internally, the \a stkSto should be NULL and/or \a stkSize
* should be 0.
* \a ie is an optional initialization event that can be used to pass
* additional startup data to the active object. (Pass NULL if your active
* object does not expect the initialization event).
*
* \note This function is strongly OS-dependent and must be defined in the
* QF port to a particular platform.
*
* The following example shows starting of the Philosopher object when a
* per-task stack is required:
* \include qf_start.c
*/
void QActive_start(QActive *me, uint8_t prio,
                   QEvent const *qSto[], uint32_t qLen,
                   void *stkSto, uint32_t stkSize,
                   QEvent const *ie);

/** \brief Posts an event \a e directly to the event queue of the acitve
* object \a me using the First-In-First-Out (FIFO) policy.
*
* Direct event posting is the simplest asynchronous communication method
* available in QF. The following example illustrates how the Philosopher
* active object posts directly the HUNGRY event to the Table active object.
* \include qf_post.c
*
* \note The producer of the event (Philosopher in this case) must only "know"
* the recipient (Table) by a generic (QActive *QDPP_table) pointer, but the
* specific definition of the Table structure is not required.
*
* \note Direct event posting should not be confused with direct event
* dispatching. In contrast to asynchronous event posting through event
* queues, direct event dispatching is synchronous. Direct event dispatching
* occurs when you call QHsm_dispatch(), or QFsm_dispatch() function.
*/
void QActive_postFIFO(QActive *me, QEvent const *e);

/** \brief Posts an event directly to the event queue of the active object
* \a me using the Last-In-First-Out (LIFO) policy.
*
* \note The LIFO policy should be used only with great caution because it
* alters order of events in the queue.
* \sa QActive_postFIFO()
*/
void QActive_postLIFO(QActive *me, QEvent const *e);

/* protected methods ...*/

/** \brief protected "constructor" of an active object.
* Performs the first step of active object initialization by assigning the
* initial pseudostate to the currently active state of the state machine.
*
* \note Must be called only by the "constructors" of the derived active
* objects, as shown in the following example:
* \include qf_ctor.c
*
* \note Must be called before QActive_start().
*
* The following example illustrates how to invoke QFsm_ctor_() in the
* "constructor" of a derived state machine:
* \include qep_qhsm_ctor.c
*
* \sa #QHsm_ctor_ and #QFsm_ctor_
*/
void QActive_ctor_(QActive *me, QState initial);

/** \brief Stops execution of an active object and removes it from the
* framework's supervision.
*
* The preferred way of calling this function is from within the active
* object that needs to stop. In other words, an active object should stop
* itself rather than being stopped by some other entity. This policy works
* best, because only the active object itself "knows" when it has reached
* the appropriate state for the shutdown.
*
* \note This function is strongly OS-dependent and should be defined in the
* QF port to a particular platform. This function is optional in embedded
* systems where active objects never need to be stopped.
*/
void QActive_stop_(QActive *me);

/** \brief Subscribes for delivery of signal \a sig to the active object
* \a me.
*
* This function is part of the Publish-Subscribe event delivery mechanism
* available in QF. Subscribing to an event means that the framework will
* start posting all published events with a given signal \a sig to the
* event queue of the active object \a me.
*
* The following example shows how the Table active object subscribes
* to three signals in the initial transition:
* \include qf_subscribe.c
*
* \sa QF_publish(), QActive_unsubscribe_(), and QActive_unsubscribeAll_()
*/
void QActive_subscribe_(QActive const *me, QSignal sig);

/** \brief Un-subscribes from the delivery of signal \a sig to the
* active object \a me.
*
* This function is part of the Publish-Subscribe event delivery mechanism
* available in QF. Un-subscribing from an event means that the framework
* will stop posting published events with a given signal \a sig to the
* event queue of the active object \a me.
*
* \note Due to the latency of event queues, an active object should NOT
* assume that a given signal \a sig will never be dispatched to the
* state machine of the active object after un-subscribing from that signal.
* The event might be already in the queue, or just about to be posted
* and the un-subscribe operation will not flush such events.
*
* \note Un-subscribing from a signal that has never been subscribed in the
* first place is considered an error and QF will rise an assertion.
*
* \sa QF_publish(), QActive_subscribe_(), and QActive_unsubscribeAll_()
*/
void QActive_unsubscribe_(QActive const *me, QSignal sig);

/** \brief Un-subscribes from the delivery of all signals to the active
* object \a me.
*
* This function is part of the Publish-Subscribe event delivery mechanism
* available in QF. Un-subscribing from all events means that the framework
* will stop posting any published events to the event queue of the active
* object \a me.
*
* \note Due to the latency of event queues, an active object should NOT
* assume that no events will ever be dispatched to the state machine of
* the active object after un-subscribing from all events.
* The events might be already in the queue, or just about to be posted
* and the un-subscribe operation will not flush such events. Also, the
* alternative event-delivery mechanisms, such as direct event posting or
* time events, can be still delivered to the event queue of the active
* object.
*
* \sa QF_publish(), QActive_subscribe_(), and QActive_unsubscribe_()
*/
void QActive_unsubscribeAll_(QActive const *me);

/** \brief Defer an event to a given separate event queue.
*
* This function is part of the event deferral support. An active object
* uses this method to defer an event \a e to the QF-supported native
* event queue \a eq. QF correctly accounts for another outstanding
* reference to the event and will not recycle the event at the end of
* the RTC step. Later, the active object might recall one event at a
* time from the event queue.
*
* An active object can use multiple event queues to defer events of
* different kinds.
*
* \sa QActive_recall_(), QEQueue
*/
void QActive_defer_(QActive *me, QEQueue *eq, QEvent const *e);

/** \brief Recall a deferred event from a given event queue.
*
* This function is part of the event deferral support. An active object
* uses this method to recall a deferred event from a given QF
* event queue. Recalling an event means that it is removed from the
* deferred event queue \a eq and posted (LIFO) to the event queue of
* the active object.
*
* QActive_recall_() returns the pointer to the recalled event to the
* caller. The function returns NULL if no event has been recalled.
*
* An active object can use multiple event queues to defer events of
* different kinds.
*
* \sa QActive_recall_(), QEQueue, QActive_postLIFO()
*/
QEvent const *QActive_recall_(QActive *me, QEQueue *eq);

/** \brief Get an event from the event queue of an active object.
*
* This method is used internally by a QF port to extract events from
* the event queue of an active object. This method depends on the event
* queue implementation and is sometimes implemented in the QF port
* (qf_port.cpp file). Depending on the underlying OS or kernel, the
* function might block the calling thread when no events are available.
*
* \note QActive_get_() is public because it often needs to be called
* from thread-run routines with difficult to foresee signature (so
* declaring friendship with such function(s) is not possible.)
*
* \sa QActive_postFIFO(), QActive_postLIFO()
*/
QEvent const *QActive_get_(QActive *me);
/* friend class QF; */
/* friend class QTimeEvt; */

/****************************************************************************/
#ifndef QF_TIMEEVT_CTR_SIZE
    /** \brief macro to override the default QTimeEvtCtr size.
    * Valid values 1, 2, or 4; default 2
    */
    #define QF_TIMEEVT_CTR_SIZE 2
#endif
#if (QF_TIMEEVT_CTR_SIZE == 1)

    /** \brief type of the Time Event counter, which determines the dynamic
    * range of the time delays measured in clock ticks.
    *
    * This typedef is configurable via the preprocessor switch
    * #QF_TIMEEVT_CTR_SIZE. The other possible values of this type are
    * as follows: \n
    * uint8_t when (QF_TIMEEVT_CTR_SIZE == 1), and \n
    * uint32_t when (QF_TIMEEVT_CTR_SIZE == 4).
    */
    typedef uint8_t QTimeEvtCtr;
#elif (QF_TIMEEVT_CTR_SIZE == 2)
    typedef uint16_t QTimeEvtCtr;
#elif (QF_TIMEEVT_CTR_SIZE == 4)
    typedef uint32_t QTimeEvtCtr;
#else
    #error "QF_TIMEEVT_CTR_SIZE defined incorrectly, expected 1, 2, or 4"
#endif

/** \brief a name for QTimeEvtTag struct
*
* Time events are special QF events equipped with the notion of time passage.
* The basic usage model of the time events is as follows. An active object
* allocates one or more QTimeEvt objects (provides the storage for them).
* When the active object needs to arrange for a timeout, it arms one of its
* time events to fire either just once (one-shot) or periodically. Each time
* event times out independently from the others, so a QF application can make
* multiple parallel timeout requests (from the same or different active
* objects). When QF detects that the appropriate moment has arrived, it
* inserts the time event directly into the recipient's event queue. The
* recipient then processes the time event just like any other event.
*
* Time events, as any other QF events derive from the ::QEvent base structure.
* Typically, you will use a time event as-is, but you can also further
* derive more specialized time events from it by adding some more data
* members and/or specialized functions that operate on the specialized
* time events.
*
* Internally, the armed time events are organized into a bi-directional
* linked list. This linked list is scanned in every invocation of the
* QF_tick() function. Only armed (timing out) time events are in the list,
* so only armed time events consume CPU cycles.
*
* \sa ::QTimeEvtTag for the description of the data members \n \ref derivation
*
* \note QF manages the time events in the function QF_tick(), which
* must be called periodically, preferably from the clock tick ISR.
*
* \note In this version of QF QTimeEvt objects should be allocated statically
* rather than dynamically from event pools. Currently, QF will not correctly
* recycle the dynamically allocated Time Events.
*/
typedef struct QTimeEvtTag QTimeEvt;
/** \brief Time Event structure
* \sa ::QTimeEvt
*/
struct QTimeEvtTag {
    /** base structure from which QTimeEvt derives
    * \sa ::QEvent \n \ref derivation
    */
    QEvent super_;

    /** link to the previous time event in the list
    */
    QTimeEvt *prev__;

    /** link to the next time event in the list
    */
    QTimeEvt *next__;

    /** the active object that receives the time events. If this pointer is
    * NULL, the event is published globally rather than posted directly.
    */
    QActive *act__;

    /** the internal down-counter of the time event. The down-counter
    * is decremented by 1 in every QF_tick() invocation. The time event
    * fires (gets posted or published) when the down-counter reaches zero.
    */
    QTimeEvtCtr ctr__;

    /** the interval for the periodic time event (zero for the one-shot
    * time event). The value of the interval is re-loaded to the internal
    * down-counter when the time event expires, so that the time event
    * keeps timing out periodically.
    */
    QTimeEvtCtr interval__;
};

/* public methods */

/** \brief The "constructor" to initialize a Time Event.
*
* You should call this function exactly once for every Time Event object
* BEFORE arming the Time Event. The most important action performed in
* this function is assigning a signal to the Time Event. You can reuse
* the Time Event any number of times, but you should not change the signal.
* This is because pointers to Time Events might still be held in event
* queues and changing signal could to hard-to-detect errors.
*
* The following example shows the invocation of QTimeEvt_ctor() from the
* "constructor" of the Philosopher active object that owns the time event
* \include qf_ctor.c
*/
void QTimeEvt_ctor(QTimeEvt *me, QSignal sig);

/** \brief Arm a one-shot time event for direct event posting.
*
* Arms a time event \a me_ to fire in \a nTicks_ clock ticks
* (one-shot time event). The time event gets directly posted (using the
* FIFO policy) into the event queue of the active object \a act_.
*
* After posting, the time event gets automatically disarmed and can be
* reused for a one-shot or periodic timeout requests.
*
* A one-shot time event can be disarmed at any time by calling the
* QTimeEvt_disarm() function. Also, a one-shot time event can be re-armed
* to fire in a different number of clock ticks by calling the
* QTimeEvt_rearm() function.
*
* The following example shows how to arm a one-shot time event from a state
* machine of an active object:
* \include qf_state.c
*/
#define QTimeEvt_postIn(me_, act_, nTicks_) do { \
    (me_)->interval__ = (QTimeEvtCtr)0; \
    QTimeEvt_arm__((me_), (act_), (nTicks_)); \
} while (0)

/** \brief Arm a periodic time event for direct event posting.
*
* Arms a time event \a me_ to fire every \a nTicks_ clock ticks
* (periodic time event). The time event gets directly posted (using the
* FIFO policy) into the event queue of the active object \a act_.
*
* After posting, the time event gets automatically re-armed to fire again
* in the specified \a nTicks_ clock ticks.
*
* A periodic time event can be disarmed only by calling the QTimeEvt_disarm()
* function. After disarming, the time event can be reused for a one-shot or
* periodic timeout requests.
*
* \note An attempt to reuse (arm again) a running periodic time event
* raises an assertion.
*
* Also, a periodic time event can be re-armed to shorten or extend the
* current period by calling the QTimeEvt_rearm() function. After adjusting
* the current period, the periodic time event goes back timing out at the
* original rate.
*/
#define QTimeEvt_postEvery(me_, act_, nTicks_) do { \
    (me_)->interval__ = (nTicks_); \
    QTimeEvt_arm__((me_), (act_), (nTicks_)); \
} while (0)

/** \brief Arm a one-shot time event for global event publishing.
*
* Arms a time event \a me_ to fire in \a nTicks_ clock ticks
* (one-shot time event). The time event gets globally published to the
* framework to be delivered to all subscriber active objects.
*
* After posting, the time event gets automatically disarmed and can be
* reused for a one-shot or periodic timeout requests.
*
* A one-shot time event can be disarmed at any time by calling the
* QTimeEvt_disarm() function. Also, a one-shot time event can be re-armed
* to fire in a different number of clock ticks by calling the
* QTimeEvt_rearm() function.
*/
#define QTimeEvt_publishIn(me_, nTicks_) do { \
    (me_)->interval__ = (QTimeEvtCtr)0; \
    QTimeEvt_arm__((me_), (QActive *)0, (nTicks_)); \
} while (0)

/** \brief Arm a periodic time event for global event publishing.
*
* Arms a time event \a me_ to fire every \a nTicks_ clock ticks
* (periodic time event). The time event gets globally published to the
* framework to be delivered to all subscriber active objects.
*
* After posting, the time event gets automatically re-armed to fire again
* in the specified \a nTicks_ clock ticks.
*
* A periodic time event can be disarmed only by calling the QTimeEvt_disarm()
* function. After disarming, the time event can be reused for a one-shot or
* periodic timeout requests.
*
* \note An attempt to reuse (arm again) a running periodic time event
* raises an assertion.
*
* Also, a periodic time event can be re-armed to shorten or extend the
* current period by calling the QTimeEvt_rearm() function. After adjusting
* the current period, the periodic time event goes back timing out at the
* original rate.
*/
#define QTimeEvt_publishEvery(me_, nTicks_) do { \
    (me_)->interval__ = (nTicks_); \
    QTimeEvt_arm__((me_), (QActive *)0, (nTicks_)); \
} while (0)

/** \brief Disarm a time event.
*
* The time event \a me gets disarmed and can be reused. The function
* returns 1 (TRUE) if the time event was truly disarmed, that is, it
* was running. The return of 0 (FALSE) means that the time event was
* not truly disarmed because it was not running. The FALSE return is only
* possible for one-shot time events that have been automatically disarmed
* upon expiration. In this case the FALSE return means that the time event
* has already been posted or published and should be expected in the
* active object's state machine.
*/
uint8_t QTimeEvt_disarm(QTimeEvt *me);

/** \brief Rearm a time event.
*
* The time event \a me gets rearmed with a new number of clock ticks
* \a nTicks. This facility can be used to prevent a one-shot time event
* from expiring (e.g., a watchdog time event), or to adjusts the
* current period of a periodic time event. Rearming a periodic timer
* leaves the interval unchanged and is a convenient method to adjust the
* phasing of the periodic time event.
*
* The function returns 1 (TRUE) if the time event was running as it
* was re-armed. The return of 0 (FALSE) means that the time event was
* not truly rearmed because it was not running. The FALSE return is only
* possible for one-shot time events that have been automatically disarmed
* upon expiration. In this case the FALSE return means that the time event
* has already been posted or published and should be expected in the
* active object's state machine.
*/
uint8_t QTimeEvt_rearm(QTimeEvt *me, QTimeEvtCtr nTicks);

/* for backwards compatibility */

/** \brief Arm a one-shot time event for direct event posting (obsolete).
*
* This facility is now obsolete, please use \sa #QTimeEvt_postIn.
*/
#define QTimeEvt_fireIn(me_, act_, nTicks_) \
    QTimeEvt_postIn(me_, act_, nTicks_)

/** \brief Arm a periodic time event for direct event posting (obsolete).
*
* This facility is now obsolete, please use \sa #QTimeEvt_postEvery.
*/
#define QTimeEvt_fireEvery(me_, act_, nTicks_) \
    QTimeEvt_postEvery(me_, act_, nTicks_)

/* private methods */

/** \brief Arm a time event (internal function to be used through macros
* only).
*
* \sa #QTimeEvt_postIn, #QTimeEvt_postEvery,
* \sa #QTimeEvt_publishIn, #QTimeEvt_publishEvery
*/
void QTimeEvt_arm__(QTimeEvt *me, QActive *act, QTimeEvtCtr nTicks);

/* friend class QF; */

/*****************************************************************************
* QF facilities
*/

/** \brief a name for QSubscrListTag struct
*
* This data type represents a set of active objects that subscribe to
* a given signal. The set is represented as an array of bits, where each
* bit corresponds to the unique priority of an active object.
*
* \sa ::QSubscrListTag for the description of the data members
*/
typedef struct QSubscrListTag QSubscrList;
/** \brief Subscriber-List structure
* \sa ::QSubscrList
*/
struct QSubscrListTag {

    /** An array of bits representing subscriber active objects. Each bit
    * in the array corresponds to the unique priority of the active object.
    * The size of the array is determined of the maximum number of active
    * objects in the application configured by the #QF_MAX_ACTIVE macro.
    * For example, an active object of priority p is a subscriber if the
    * following is true: ((bits[QF_div8Lkup[p]] & QF_pwr2Lkup[p]) != 0)
    *
    * \sa QF_psInit(), ::QF_div8Lkup, ::QF_pwr2Lkup, #QF_MAX_ACTIVE
    */
    uint8_t bits[((QF_MAX_ACTIVE - 1) / 8) + 1];
};

/* public methods */

/** \brief QF initialization.
*
* This function initializes QF and must be called exactly once before any
* other QF function.
*/

// GW Create a public interface to QF for the creation of the event pool.
void QF_init(int maxSignals, int bigEventSize, int numberEvents);

/** \brief Publish-subscribe initialization.
*
* This function initializes the publish-subscribe facilities of QF and must
* be called exactly once before any subscriptions/publications occur in
* the application. The arguments are as follows: \a subscrSto is a pointer
* to the array of subscriber-lists. \a maxSignal is the dimension of this
* array and at the same time the maximum signal that can be published or
* subscribed.
*
* The array of subscriber-lists is indexed by signals and provides mapping
* between the signals and subscriber-lists. The subscriber-lists are bitmasks
* of type ::QSubscrList, each bit in the bitmask corresponding to the unique
* priority of an active object. The size of the ::QSubscrList bitmask depends
* on the value of the #QF_MAX_ACTIVE macro.
*
* \note The publish-subscribe facilities are optional, meaning that you
* might choose not to use publish-subscribe. In that case calling QF_psInit()
* and using up memory for the subscriber-lists is unnecessary.
*
* \sa ::QSubscrList
*
* The following example shows the typical initialization sequence of QF:
* \include qf_main.c
*/
void QF_psInit(QSubscrList *subscrSto, QSignal maxSignal);

/** \brief Event pool initialization for dynamic allocation of events.
*
* This function initializes one event pool at a time and must be called
* exactly once for each event pool before the pool can be used.
* The arguments are as follows: \a poolSto is a pointer to the memory
* block for the events. \a poolSize is the size of the memory block in
* bytes. \a evtSize is the block-size of the pool in bytes, which determines
* the maximum size of events that can be allocated from the pool.
*
* You might initialize one, two, and up to three event pools by making
* one, two, or three calls to the QF_poolInit() function. However,
* for the simplicity of the internal implementation, you must initialize
* event pools in the ascending order of the event size.
*
* Many RTOSes provide fixed block-size heaps, a.k.a. memory pools that can
* be adapted for QF event pools. In case such support is missing, QF provides
* a native QF event pool implementation. The macro #QF_EPOOL_TYPE_ determines
* the type of event pool used by a particular QF port. See structure ::QMPool
* for more information.
*
* \note The actual number of events available in the pool might be actually
* less than (\a poolSize / \a evtSize) due to the internal alignment
* of the blocks that the pool might perform. You can always check the
* capacity of the pool by calling QF_getPoolMargin().
*
* \note The dynamic allocation of events is optional, meaning that you
* might choose not to use dynamic events. In that case calling QF_poolInit()
* and using up memory for the memory blocks is unnecessary.
*
* \sa QF initialization example for QF_init()
*/
void QF_poolInit(void *poolSto, uint32_t poolSize, QEventSize evtSize);

/** \brief Starts the interrupts and initializes other critical resources
* that might interact with the QF application.
*
* The timeline for calling QF_start() depends on the particular port of QF.
* In general, this function must be called after QF_init(), but before
* QF_run(). Often it is actually called from QF_run(), right before starting
* any multitasking kernel or the background loop.
*
* \note This function is strongly platform-dependent and is not implemented
* in the QF, but either in the QF port or in the Board Support Package (BSP)
* for the given application. Some QF ports might not require implementing
* QF_start() at all.
*
* \sa QF initialization example for QF_init()
*/
void QF_start(void);

/** \brief Transfers control to QF to run the application.
*
* QF_run() is typically called from your startup code after you initialize
* the QF and start at least one active object with QActive_start(). Also,
* QF_start() call must precede the transfer of control to QF_run(), but
* some QF ports might call QF_start() from QF_run(). QF_run() typically
* never returns to the caller.
*
* \note This function is strongly platform-dependent and is not implemented
* in the QF, but either in the QF port or in the Board Support Package (BSP)
* for the given application. All QF ports must implement QF_run().
*
* \note When the Quantum Kernel (QK) is used as the underlying real-time
* kernel for the QF, all platfrom dependencies are handled in the QK, so
* no porting of QF is necessary. In other words, you only need to recompile
* the QF platform-independent code with the compiler for your platform, but
* you don't need to provide any platform-specific implementation (so, no
* qf_port.c file is necessary). Moreover, QK implements the function QF_run()
* in a platform-independent way, in the modile qk.c.
*/
void QF_run(void);

// GW Add declaration
void QF_runSM(QActive *me);

#ifndef QF_INT_KEY_TYPE
    void QF_onIdle(void);                 /* interrupt lock key NOT defined */

#else

    /** \brief QF idle callback (customized in BSPs for QF)
    *
    * QF_onIdle() is called by the non-preemptive scheduler built into QF
    * when the framework detects that no events are available for active
    * objects (the idle condition). This callback gives the application an
    * opportunity to enter a power-saving CPU mode, or perform some other
    * idle processing (such as Q-Spy output).
    *
    * \note QF_onIdle() is invoked with interrupts LOCKED because the idle
    * condition can be asynchronously changed at any time by an interrupt.
    * QF_onIdle() MUST unlock the interrupts internally, but not before
    * putting the CPU into the low-power mode. (Ideally, unlocking interrupts
    * and low-power mode should happen atomically). At the very least, the
    * function MUST unlock interrupts, otherwise interrups will be locked
    * permanently.
    *
    * \note QF_onIdle() is only used by the non-preemptive scheduler built
    * into QF in the "bare metal" port, and is NOT used in any other ports.
    * When QF is combined with QK, the QK idle loop calls a different
    * function QK_onIdle(), with different semantics than QF_onIdle().
    * When QF is combined with a 3rd-party RTOS or kernel, the idle
    * processing mechanism of the RTOS or kernal is used instead of
    * QF_onIdle().
    */
    void QF_onIdle(QF_INT_KEY_TYPE intLockKey);    /* int. lock key defined */

#endif                                                   /* QF_INT_KEY_TYPE */


/** \brief Function invoked by the application layer to stop the QF
* application and return control to the OS/Kernel.
*
* This function stops the QF application. After calling this function,
* QF attempts to gracefully stop the  application. This graceful shutdown
* might take some time to complete. The typical use of this funcition is
* for terminating the QF application to return back to the operating
* system or for handling fatal errors that require shutting down
* (and possibly re-setting) the system.
*
* This function is strongly platform-specific and is not implemented in
* the QF, but either in the QF port or in the Board Support Package (BSP)
* for the given application. Some QF ports might not require implementing
* QF_stop() at all, because many embedded application don't have anything
* to exit to.
*
* \sa QF_stop() and QF_cleanup()
*/
void QF_stop(void);

/** \brief QF cleanup callback (customized in QF ports or BSPs for QF)
*
* QF_cleanup() is called in some QF ports before QF returns to the
* unerlying operating system or RTOS.
*
* This function is strongly platform-specific and is not implemented in
* the QF, but either in the QF port or in the Board Support Package (BSP)
* for the given application. Some QF ports might not require implementing
* QF_cleanup() at all, because many embedded application don't have anything
* to exit to.
*
* \sa QF_init() and QF_stop()
*/
void QF_cleanup(void);

/** \brief Exits the QF application and returns control to the OS/Kernel
* (Deprecated: use QF_stop() in new designs)
*
* This function exits the framework. After calling this function, QF is no
* longer in control of the application. The typical use of this method is
* for exiting the QF application to return back to the operating system
* or for handling fatal errors that require resetting the system.
*
* This function is strongly platform-dependent and is not implemented in
* the QF, but either in the QF port or in the Board Support Package (BSP)
* for the given application. Some QF ports might not require implementing
* QF_cleanup() at all, because many embedded application don't have anything
* to clean up.
*
* \sa QF_stop() and QF_cleanup()
*/
void QF_exit(void);

/** \brief Publish event to the framework.
*
* This function posts (using the FIFO policy) the event \a e it to ALL
* active object that have subscribed to the signal \a e->sig.
* This function is designed to be callable from any part of the system,
* including ISRs, device drivers, and active objects.
*
* In the general case, event publishing requires multi-casting the
* event to multiple subscribers. This happens in the caller's thread with
* the scheduler locked to prevent preemptions during the multi-casting
* process. (Please note that the interrupts are not locked.)
*
*/
void QF_publish(QEvent const *e);

/** \brief Processes all armed time events at every clock tick.
*
* This function must be called periodically from a time-tick ISR or from
* the highest-priority task so that QF can manage the timeout events.
*
* \note The QF_tick() function is not reentrant meaning that it must run to
* completion before it is called again. Also, QF_tick() assumes that it
* never will get preempted by a task, which is always the case when it is
* called from an ISR or the highest-priority task.
*
* \sa ::QTimeEvt.
*
* The following example illustrates the call to QF_tick():
* \include qf_tick.c
*/
void QF_tick(void);

/* methods used in the QF ports only ---------------------------------------*/

/** \brief Register an active object to be managed by the framework
*
* This function should not be called by the application directly, only
* through the function QActive_start(). The priority of the active object
* \a a should be set before calling this function.
*
* \note This function raises an assertion if the priority of the active
* object exceeds the maximum value #QF_MAX_ACTIVE. Also, this function
* raises an assertion if the priority of the active object is already in
* use. (QF requires each active object to have a UNIQUE priority.)
*/
void QF_add_(QActive *a);

/** \brief Remove the active object from the framework.
*
* This function should not be called by the application directly, only
* inside the QF port. The priority level occupied by the active object
* is freed-up and can be reused for another active object.
*
* The active object that is removed from the framework can no longer
* participate in the publish-subscribe event exchange.
*
* \note This function raises an assertion if the priority of the active
* object exceeds the maximum value #QF_MAX_ACTIVE or is not used.
*/
void QF_remove_(QActive const *a);

/** \brief Returns the QF version.
*
* This function returns constant version string in the format x.y.zz,
* where x (one digit) is the major version, y (one digit) is the minor
* version, and zz (two digits) is the maintenance release version.
* An example of the version string is "3.1.03".
*
* The following example illustrates the usage of this function:
* \include qf_version.c
*/
char const Q_ROM * Q_ROM_VAR QF_getVersion(void);

/** \brief Returns the QF-port version.
*
* This function returns constant version string in the format x.y.zz,
* where x (one digit) is the major version, y (one digit) is the minor
* version, and zz (two digits) is the maintenance release version.
* An example of the QF-port version string is "1.1.03".
*
* \sa QF_getVersion()
*/
char const Q_ROM * Q_ROM_VAR QF_getPortVersion(void);

/** \brief This function returns the margin of the given event pool.
*
* This function returns the margin of the given event pool \a poolId, where
* poolId is the ID of the pool initialized by the call to QF_poolInit().
* The poolId of the first initialized pool is 1, the second 2, and so on.
*
* The returned pool margin is the minimal number of free blocks encountered
* in the given pool since system startup.
*
* \note Requesting the margin of an un-initialized pool raises an assertion
* in the QF.
*/
uint32_t QF_getPoolMargin(uint8_t poolId);

/** \brief This function returns the margin of the given event queue.
*
* This function returns the margin of the given event queue of an active
* object with priority \a prio. (QF priorities start with 1 and go up to
* #QF_MAX_ACTIVE.) The margin is the minimal number of free events
* encountered in the given queue since system startup.
*
* \note QF_getQueueMargin() is available only when the native QF event
* queue implementation is used. Requesting the queue margin of an unused
* priority level raises an assertion in the QF. (A priority level becomes
* used in QF after the call to the QF_add_() function.)
*/
uint32_t QF_getQueueMargin(uint8_t prio);

/** \brief Internal QF implementation of the dynamic event allocator.
*
* \note The application code should not call this function directly.
* Please use the macro #Q_NEW.
*/
QEvent *QF_new_(QEventSize evtSize, QSignal sig);

/** \brief Allocate a dynamic event.
*
* This macro returns an event pointer cast to the type \a evtT_. The event
* is initialized with the signal \a sig. Internally, the macro calls the
* internal QF function QF_new_(), which always returns a valid event pointer.
*
* \note The internal QF function QF_new_() raises an assertion when
* the allocation of the event turns out to be impossible due to event pool
* depletion, or incorrect (too big) size of the requested event.
*
* The following example illustrates dynamic allocation of an event:
* \include qf_post.c
*/
#define Q_NEW(evtT_, sig_) ((evtT_ *)QF_new_(sizeof(evtT_), (sig_)))

/** \brief Recycle a dynamic event.
*
* This function implements a simple garbage collector for the dynamic events.
* Only dynamic events are candidates for recycling. (A dynamic event is one
* that is allocated from an event pool, which is determined as non-zero
* e->attrQF__ attribute.) Next, the function decrements the reference counter
* of the event, and recycles the event only if the counter drops to zero
* (meaning that no more references are outstanding for this event).
* The dynamic event is recycled by returning it to the pool from which
* it was originally allocated. The pool-of-origin information is stored in
* the upper 2-MSBs of the e->attrQF__ attribute.)
*
* \note QF invokes the garbage collector at all appropriate contexts, when
* an event can become garbage (automatic garbage collection), so the
* application code should have no need to call QF_gc() directly. The QF_gc()
* function is exposed only for special cases when your application sends
* dynamic events to the "raw" thread-safe queues (see ::QEQueue). Such
* queues are processed outside of QF and the automatic garbage collection
* is CANNOT be performed for these events. In this case you need to call
* QF_gc() explicitly.
*/
void QF_gc(QEvent const *e);

/** \brief Lookup table for (log2(n) + 1), where n is the index
* into the table.
*
* This lookup delivers the 1-based number of the most significant 1-bit
* of a byte.
*
* \note Index range n = 0..255. The first index (n == 0) should never be used.
*/
extern uint8_t const Q_ROM Q_ROM_VAR QF_log2Lkup[256];

/** \brief Lookup table for (1 << ((n-1) % 8)), where n is the index
* into the table.
*
* \note Index range n = 0..64. The first index (n == 0) should never be used.
*/
extern uint8_t const Q_ROM Q_ROM_VAR QF_pwr2Lkup[65];

/** \brief Lookup table for ~(1 << ((n-1) % 8)), where n is the index
* into the table.
*
* \note Index range n = 0..64. The first index (n == 0) should never be used.
*/
extern uint8_t const Q_ROM Q_ROM_VAR QF_invPwr2Lkup[65];

/** \brief Lookup table for (n-1)/8
*
* \note Index range n = 0..64. The first index (n == 0) should never be used.
*/
extern uint8_t const Q_ROM Q_ROM_VAR QF_div8Lkup[65];


/** \brief array of registered active objects
*
* \note Not to be used by Clients directly, only in ports of QF
*/
extern QActive *QF_active_[];

#ifndef Q_ROM_BYTE
    /** \brief Macro to access a byte allocated in ROM
    *
    * Some compilers for Harvard-architecture MCUs, such as gcc for AVR, do
    * not generate correct code for accessing data allocated in the program
    * space (ROM). The workaround for such compilers is to explictly add
    * assembly code to access each data element allocated in the program
    * space. The macro Q_ROM_BYTE() retrieves a byte from the given ROM
    * address.
    *
    * The Q_ROM_BYTE() macro should be defined in the qpn_port.h header file
    * for each compiler that cannot handle correctly data allocated in ROM
    * (such as the gcc). If the macro is left undefined, the default
    * definition simply returns the argument and lets the compiler generate
    * the correct code.
    */
    #define Q_ROM_BYTE(rom_var_)   (rom_var_)
#endif

/****************************************************************************/
/* Macros for QS instrumentation of interrupts locking/unlocking and
* ISR entry/exit
*/
#ifdef Q_SPY

/** \brief interrupt-lock nesting level
*
* \note Not to be used by Clients directly, only in ports of QF
*/
extern uint8_t QF_intLockNest_;

/** \brief ISR-call nesting level
*
* \note Not to be used by Clients directly, only in ports of QF
*/
extern uint8_t QF_isrNest_;

#define QF_QS_INT_LOCK() \
    QS_BEGIN_NOLOCK_(QS_QF_INT_LOCK, 0, 0); \
        QS_TIME_(); \
        QS_U8_((uint8_t)(++QF_intLockNest_)); \
    QS_END_NOLOCK_()

#define QF_QS_INT_UNLOCK() \
    QS_BEGIN_NOLOCK_(QS_QF_INT_UNLOCK, 0, 0); \
        QS_TIME_(); \
        QS_U8_((uint8_t)(QF_intLockNest_--)); \
    QS_END_NOLOCK_()

#define QF_QS_ISR_ENTRY(prio_) \
    QS_BEGIN_NOLOCK_(QS_QF_ISR_ENTRY, 0, 0); \
        QS_TIME_(); \
        QS_U8_((uint8_t)(++QF_isrNest_)); \
        QS_U8_(prio_); \
    QS_END_NOLOCK_()

#define QF_QS_ISR_EXIT(prio_) \
    QS_BEGIN_NOLOCK_(QS_QF_ISR_EXIT, 0, 0); \
        QS_TIME_(); \
        QS_U8_((uint8_t)(QF_isrNest_--)); \
        QS_U8_(prio_); \
    QS_END_NOLOCK_()

#define QF_QS_ACTION(act_)      (act_)

#else

#define QF_QS_INT_LOCK()        ((void)0)
#define QF_QS_INT_UNLOCK()      ((void)0)
#define QF_QS_ISR_ENTRY(prio_)  ((void)0)
#define QF_QS_ISR_EXIT(prio_)   ((void)0)
#define QF_QS_ACTION(act_)      ((void)0)

#endif                                                             /* Q_SPY */

/****************************************************************************/
/**
\page qf_rev QF/C Revision History

\section qf_3_3_00 Version 3.3.00 (Product)
Release date: Jan 22, 2007\n

The main change in this release is adding the macro #Q_ROM_VAR in QEP
for all constant objects allocated in ROM. The #Q_ROM_VAR macro has been
added for the compilers like Freescale HC(S)08, which require far pointers
to access the objects in ROM. Please note that specifying the pointer size
for accessing a ROM objects is syntactically different than specifying
that the object is allocated in ROM (see macro #Q_ROM).\n
\n
The other significant change in release 3.3.00 is simplification of the
build strategy for QP ports. Instead of separate Makefile for every QP
component, such as QEP, QF, QK, and QS, not the "ports" directory contains
a batch file "make.bat" that builds all the libraries at once.
\n

-# in file qf.h added macro #Q_ROM_VAR for objects allocated in ROM
and to signatures of functions accessing these objects.
-# in file qf.h removed method QF_getTime() and deleted external variable
QF_tickCtr_.
-# deleted obsolete file qa_fifo_.c
-# deleted obsolete file qa_lifo_.c
-# deleted obsolete file qf_time.c
-# in file qf_act.c added Q_ROM_VAR to the signature of QEP_getVersion().
-# in file qf_log2.c added Q_ROM_VAR to the definition of the lookup table.
-# in file qf_pwr2.c added Q_ROM_VAR to the definition of the lookup tables.
-# in file qf_tick.c removed incrementing QF_tickCtr_.
-# In file qf_act.c updated version number to 3.3.00
-# Updated the "QP Programmer's Manaul" to Revision E


\section qf_3_2_05 Version 3.2.05
Release date: Dec 08, 2006\n

This QF release rolls back the changes made to the reference-counting
policy. The reference count of a dynamic event is incremented
when the event is posted, but is NOT decremented when the event is
retreived from the queue. The reference count is decremented only later,
in the garbage collector (QF_gc()).

This release adds direct support for event deferral (the "Deferred Event"
state pattern) through methods QActive_defer_() and QActive_recall_().\n

-# In file qf.h added methods QActive_defer_() and QActive_recall_().
-# In file qa_get_.c removed decrementing the reference count of a
dynamic event.
-# In file qeq_get.c removed decrementing the reference count of a
dynamic event.
-# In file qf_gc.cpp restored decrementing of the reference count of a
dynamic event.
-# Added new file qa_defer.c that implements QActive_defer_() and
QActive_recall_().
-# In file qf_act.c updated version number to 3.2.05
-# Updated the "QP Programmer's Manaul" to Revision D


\section qf_3_2_04 Version 3.2.04
Release date: Dec 01, 2006\n

This QF release changes the internal policy of reference-counting for
dynamic events. The reference count of a dynamic event is now incremented
when the event is posted to a queue and decremented when the event is
later retreived from the queue. This policy pertains to both active
object queues and native QF thread-safe queues (QEQueue).\n

Previously, the reference count of a dynamic event was not decremented
upon retreival of the event from the event queue, but rather in the
garbage collector (QF_gc()).\n

-# In file qf.h eliminated methods QActive_postFIFO_() and
QActive_postLIFO_().
-# In file qf.h changed method QActive_get__() to QActive_get_() (protected
scope) to make it available to various thread-run routines.
-# In file qa_fifo.c changed the implementation of QActive_postFIFO()
to represent the native QF event queue of an active object.
-# In file qa_fifo_.c removed the implementation of QActive_postFIFO__()
and declared the file obsolete (will be removed in future releases).
-# In file qa_lifo.c changed the implementation of QActive_postLIFO()
to represent the native QF event queue of an active object.
-# In file qa_lifo_.c removed the implementation of QActive_postLIFO__()
and declared the file obsolete (will be removed in future releases).
-# In file qa_get_.cpp added decrementing the reference count of a
dynamic event.
-# In file qf_gc.c removed decrementing of the reference count of a
dynamic event. Also changed the test for recycling an event (reference
count of zero).
-# In file qf_pspub.c removed incrementing the reference count of a
dynamic event.
-# Removed all uses of the macros #QACTIVE_POST_FIFO_, #QACTIVE_POST_LIFO_,
and QACTIVE_GET_. These macros are made now obsolete.
-# In file qsched.h removed definitions of the obsolete macros
#QACTIVE_POST_FIFO_, #QACTIVE_POST_LIFO_, and QACTIVE_GET_.
-# In file qf_pspub.c replaced the macro QACTIVE_POST_FIFO_() with the direct
call to the function QActive_postFIFO(QF_active_[p], e).
-# In file qf_tick.c replaced the macro QACTIVE_POST_FIFO_() with the direct
call to the function QActive_postFIFO(QF_active_[p], e).
-# Changed the uC/OS-II port to reflect new policy of handling reference
counters inside the dynamic events. Also removed files qa_fifo.c and
qa_lifo.c from the uC/OS-II build.
-# In file qf_act.c updated version number to 3.2.04
-# Updated the "QP Programmer's Manaul" to Revision C


\section qf_3_2_03 Version 3.2.03 (Product)
Release date: Nov 15, 2006\n

The main purpose of this release is to adapt the code to the shortcomings of
the gcc compiler for handling data in program ROM for Harvard architecture
CPUs, such as the Atmel's AVR or the 8051. In such machines, the data space
(RAM) and program space (ROM) are accessed with different instructions.
The gcc compiler does not automatically synthesize the correct code for
accessing data placed in the program ROM, even though
__attribute__((__progmem__)) is used. The workaround for the gcc is to add
special assembly instructions to transfer the data from the program space to
the data space. This version of QP-nano adds macros for each data element
allocated to the program space (delcared with the Q_ROM attribute). Please
note that commercial compilers, such as IAR, handle data allocated in the
program space (ROM) correctly and do not need any workarounds.\n

This release also fixes a few minor inconsistencies in the code (see the list
below):\

-# In file qf.h added default definition of macro #Q_ROM_BYTE
-# In file qf_set.h added macro #Q_ROM_BYTE to access the lookup tables
allocated in ROM (several places).
-# In file qf_sched.h changed types QF_OS_OBJECT_TYPE and QF_THREAD_TYPE to
uint8_t from int8_t
-# In file qs_dummy.h added dummy definitions of some missing QS macros
-# In file qa_sub.c added macro #Q_ROM_BYTE to access the lookup tables
allocated in ROM (several places).
-# In file qa_usub.c added macro #Q_ROM_BYTE to access the lookup tables
allocated in ROM (several places).
-# In file qf_usuba.c added macro #Q_ROM_BYTE to access the lookup tables
allocated in ROM (several places).
-# In file qf_pspub.c added macro #Q_ROM_BYTE to access the lookup tables
allocated in ROM (several places).
-# In file qf_act.c updated version number to 3.2.03


\section qf_3_2_01 Version 3.2.01 (Product)
Release date: Sep 01, 2006\n

-# In file qf_act.c updated version number to 3.2.01
-# Added makefiles for building ports of all QP/C libraries at once.
-# Created the consolidated manual "QP/C Programmer's Manual".


\section qf_3_2_00 Version 3.2.00 (Product Release)
Release date: Aug 07, 2006\n

-# In file qf.h changed the semantics of the QF_onIdle() callback.
This callback is now invoked with interrupts LOCKED from the non-preemptive
scheduler used in the "vanilla" QF ports to "bare metal" target boards.<br>
<br>
The modification changes the responsibilities of QF_onIdle(), which now MUST
at least unlock interrupts. A failure to unlock interrupts in QF_onIdle()
will leave the interrupts locked all the time and would prevent the
application from running.<br>
<br>
Also, the signature of QF_onIdle() now depends on the interrupt locking
policy. In case of the "save and restore interrupt status" policy, the
QF_onIdle() callback takes the interrupt lock key as parameter (to be able
to unlock the interrups correctly).
-# In file qf.h used the macro Q_ROM to allocate constant objects
to ROM (\sa qep.h). Objects allocated to ROM are: the version strings, and
the lookup tables (QF_log2Lkup[], QF_pwr2Lkup[], QF_invPwr2Lkup, and
QF_div8Lkup[].
-# Added new platform-independent header file qsched.h to provide the
interface to the simple non-preemptive scheduler used in the "vanilla" ports
of QF to "bare metal" targets. This header file is only applicable to the
"vanilla" ports.
-# Added new platform-independent implementation file qf_run.c to
implement the simple non-preemptive scheduler used in the "vanilla" ports
of QF to "bare metal" targets. This implementation file eliminates the need
for qf_port.c file in the "vanilla" ports of QF. Also, the qf_run.c module
should only be placed in the QF library in the vanilla QF ports.
-# Simplified all "vanilla" ports of QF to use the common platform-
independent implementation provided in qf_run.c.
-# Updated QF_onIdle() callback in all examples of "vanilla" ports of QF to
unlock interrupts.
-# Modified file qf_pspub.c to allow allocating a temporary stack variable
inside the macro QF_SCHED_LOCK(). This change is related to modification in
QK v 3.2.00.
-# Updated the "QF/C Programmer's Manual".


\section qf_3_1_06 Version 3.1.06 (Product Release)
Release date: Jul 14, 2006\n

-# In file qf.h added function QF_stop() to be called from the application
code to stop the framework.
-# In file qf.h added callback function QF_cleanup() to be called from the
QF port to cleanup before exiting to the OS.
-# In file qf.h deprecated the function QF_exit().


\section qf_3_1_05 Version 3.1.05 (Product Release)
Release date: Feb 08, 2006\n

-# In file qf_act.c added the Revision History Doxygen comment, which was
previously in doxygen/qp.h
-# In file qf.h augmented comment for QF_run() to cover the case when QF is
used with QK.
-# In file qf.h added the extern declarations of ::QF_tickCtr_,
::QF_intLockNest, and ::QF_isrNest_, which were previously declared
in qf_pkg.h.
-# In file qf.h added macros #QF_QS_INT_LOCK, #QF_QS_INT_UNLOCK(),
#QF_QS_ISR_ENTRY, and #QF_QS_ISR_EXIT, which were previously declared
in qs_port.h.
-# In file ports/linux/gcc/qf_port.h added extern uint8_t QF_running_.
-# In file qf/80x86/dos/tcpp101/l/qf_port.c replaced deprecated
QPSet_hasElements() to QPSet_isEmpty().
-# In file qf/80x86/linux/gcc/qf_port.c added QF_run()
-# In file qeq_init.c:186 changed QS_OBJ(me) to QS_OBJ(qSto) to consistently
refer to a queue by the ring buffer object
-# In file qf_pkg.h removed extern ::QF_tickCtr_.


\section qf_3_1_04 Version 3.1.04 (Product Release)
Release date: Dec 08, 2005\n

-# In file qmpool.h changed the definition of the #QF_MPOOL_SIZ_SIZE macro
to remove the dependency on the #QF_EVENT_SIZ_SIZE. Macro #QF_EVENT_SIZ_SIZE
might not be defined by the time qmpool.h is included.
-# Added explicit definition of the configuration macro QF_EVENT_SIZ_SIZE to
all qf_port.h files.
-# Fixed a bug in function QMPool_init() (file qmp_init.c) by changing
the type of variable n from uint8_t to QMPoolCtr. The uint8_t data type
was failing for bigger block sizes.
-# Added the QF_onIdle() callback to qf.h
-# Improved comments in qpset.h
-# Corrected dependencies in the Makefile for QDPP example
(directory 80x86/dos/tcpp101/l and 80x86/dos/tcpp101/s)
-# Added Linux QF port to the standard QF/C distribution.
-# Released the "QF/C Programmer's Manual"


\section qf_3_1_03 Version 3.1.03 (Beta1 Release)
Release date: Nov 18, 2005\n

-# Added Doxygen documentation to the source code
-# Added running__ member to the QActive structure
-# Added QF_EVENT_SIZ_SIZE configuration macro and related data type
QEventSize. Made the following changes to the signatures:\n
void QF_poolInit(void *poolSto, uint32_t poolSize, QEventSize evtSize);\n
QEvent *QF_new_(QEventSize evtSize, QSignal sig);
-# Changed the name of protected function from QF_new() to QF_new_().


\section qf_3_1_00 Version 3.1.00 (Snapshot Release)
Release date: Oct 10, 2005\n

-# Applied new directory structure desribed in
<A HREF="http://www.quantum-leaps.com/doc/AN_QP_Directory_Structure.pdf">
Application Note: QP Directory Structure</A>
-# Added <A HREF="http://www.quantum-leaps.com/products/qs.htm">
Quantum Spy</A> instrumentation.
*/

/** \defgroup qf Quantum Framework in C (QF/C)
* \image html logo_qf_TM.jpg
*
* Quantum Frameowrk (QF) is a reusable event-driven application framework
* for executing concurrent state machines specifically designed for real-time
* embedded (RTE) systems. The use of QF generally simplifies the design of
* event-driven software by allowing the application to be divided into
* multiple active objects  that the framework manages. Active objects in QF
* are encapsulated tasks (each embedding a state machine and an event queue)
* that communicate with one another asynchronously by sending and receiving
* events. Within an active object, events are processed sequentially in a
* run-to-completion (RTC) fashion, while QF encapsulates all the details of
* thread-safe event exchange and queuing..
*
* Most of QF/C is written in portable ANSI-C, with microprocessor-specific,
* compiler-specific, or op-erating system-specific code kept to a minimum for
* ease of portability. QF is designed to work with Quantum Event Processor
* (QEP) and a Real Time Operating System (RTOS) of your choice, or even with
* just "main+ISRs" configuration. The framework is very compact, typically
* taking up about 4KB of code and data (including the QEP).
* QF has been used in hundreds of event-driven applications worldwide
* and has been originally described in Part 2 of the book
* <A HREF="http://www.quantum-leaps.com/writings/book.htm">Practical
* Statecharts in C/C++</A> by Miro Samek, CMP Books 2002.
*
* \sa <A HREF="http://www.quantum-leaps.com/doc/QP_Manual.pdf">
*      QP Programmer's Manual</A> \n
*      \ref qf_rev
*/

#endif                                                              /* qf_h */
