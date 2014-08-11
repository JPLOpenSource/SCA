/**
 * A transition is a directed relationship between a source vertex and a target
 * vertex. It may be part of a compound transition, which takes the state
 * machine from one state configuration to another, representing the complete
 * response of the state machine to an occurrence of an event of a particular
 * type.
 */
package gov.nasa.jpl.statechart.uml;

import java.util.Collection;

public interface Transition extends Namespace {

    public Collection<Trigger> getTrigger ();

    public Constraint getGuard ();

    public Behavior getEffect ();

    public Vertex getSource ();

    public Vertex getTarget ();

    public TransitionKind getKind ();

    /**
     * Convenient function which indicates whether this transition is internal
     * @return <code>true</code> if transition is internal, <code>false</code> otherwise.
     */
    public boolean isInternal ();

    /**
     * Convenience function, returns cached list of signal events from this
     * transition's triggers.
     * @return  collection of SignalEvents
     */
    public Collection<SignalEvent> getSignalEvents ();

    /**
     * Convenience function, returns cached list of time events from this
     * transition's triggers.
     * @return  collection of TimeEvents
     */
    public Collection<TimeEvent> getTimeEvents ();

    /**
     * Convenience function, returns list of signal and time events, in that order.
     * @return
     */
    public Collection<Event> getAllEvents ();

    /**
     * NON-UML Standard:
     * 
     * Convenience function, returns whether this transition has neither a
     * {@link SignalEvent} nor a {@link TimeEvent} trigger.
     * @return  boolean <code>true</code> if this transition has NO trigger event; <code>false</code> otherwise.
     */
    public boolean isNullEvent ();

}
