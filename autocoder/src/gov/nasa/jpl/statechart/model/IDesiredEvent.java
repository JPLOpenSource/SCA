/**
 * Created Jun 23, 2010.
 * <p>
 * Copyright 2009-2010, by the California Institute of Technology. ALL RIGHTS
 * RESERVED. United States Government Sponsorship acknowledged. Any commercial
 * use must be negotiated with the Office of Technology Transfer at the
 * California Institute of Technology.
 * </p>
 * <p>
 * This software is subject to U.S. export control laws and regulations and has
 * been classified as 4D993. By accepting this software, the user agrees to
 * comply with all applicable U.S. export laws and regulations. User has the
 * responsibility to obtain export licenses, or other export authority as may be
 * required before exporting such information to foreign countries or providing
 * access to foreign persons.
 * </p>
 */
package gov.nasa.jpl.statechart.model;

import gov.nasa.jpl.statechart.Pair;
import gov.nasa.jpl.statechart.uml.Event;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.Transition;

import java.util.Collection;
import java.util.List;

/**
 * This interface specifies the query methods exposed by a desired event visitor
 * to allow information about a desired event to be retrieved, including the
 * transition in which the event is a trigger, the regions that subscribe to it,
 * and the submachine that subscribe it, as applicable.
 * <p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public interface IDesiredEvent {

    public enum QueryPolicy {
        /** Enum literal indicating NOT to populate with Null-Event Transition,
         *  used by most back-ends. */
        NO_NULL_EV,
        /** Enum literal indicating to populate with Null-Event Transition,
         *  useful for back-ends Promela and SCL. */
        NULL_EV
    }

    /**
     * Defines an event and the transition it triggers as a pair.
     */
    public class EventTransitionPair extends Pair<Event,Transition> {
        public EventTransitionPair (Event event, Transition transition) {
            super(event, transition);
        }
        public Event getEvent () {
            return super.getFirst();
        }
        public Transition getTransition () {
            return super.getSecond();
        }
    }

    public void sort ();

    /**
     * Returns the list of desired event-transition pairs collected by this
     * visitor.
     * 
     * @return {@link List} of pairs of UML Signal- or Time- {@link Event}
     *      and a corresponding transition the event triggers.
     */
    public List<EventTransitionPair> getEventTransitions ();

    /**
     * Returns the substates path-prefixes of the given Event-Transition pair
     * as a String array.
     * 
     * @param ev  {@link EventTransitionPair} whose substate prefix to get.
     * @return {@link String}[] array of the substate prefix strings.
     */
    public String[] getSubstatePrefixOfEvent (EventTransitionPair evTrPair);

    /**
     * Returns the submachine-relevant prefixes of the given Event-Transition
     * pair, which is essentially the substate prefix without the first element.
     * 
     * @param ev  {@link EventTransitionPair} whose submachine prefix to get.
     * @return {@link String}[] array of the submachine prefix strings.
     */
    public String[] getSubmachinePrefixOfEvent (EventTransitionPair evTrPair);

    /**
     * Returns the collection of immediate orthogonal regions of the supplied
     * state desiring the supplied event.
     * 
     * @param state  the UML {@link State} whose immediate orthogonal regions we want
     * @param ev     the {@link EventTransitionPair} to query for subcribing regions.
     * @return {@link Collection} of {@link Region}s interested in event <code>ev</code>.
     */
    public Collection<Region> getDesiringRegionsOfState (State state, EventTransitionPair evTrPair);

    /**
     * Returns the substate (state with Submachine) desiring the supplied
     * event-transition pair.
     * 
     * @param ev  the {@link EventTransitionPair} to query for desiring substates.
     * @return Sub{@link State} interested in event <code>ev</code>.
     */
    public State getSubtateDesiringEvent (EventTransitionPair evTrPair);

}
