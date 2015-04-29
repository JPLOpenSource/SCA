/**
 * Created Mar 22, 2010.
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
package gov.nasa.jpl.statechart.model.visitor;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.model.IDesiredEvent;
import gov.nasa.jpl.statechart.model.IDesiredEvent.EventTransitionPair;
import gov.nasa.jpl.statechart.uml.Event;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.Transition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Visitor to collect all the desired events within the orthogonal regions of
 * a State visited.  Events are collected as pairs of event and a corresponding
 * transition that it triggers.
 * <p>
 * This visitor keeps a map of event to list of regions that
 * care about that event, so keep this visitor instance around for
 * future queries of this mapping, particularly for determining to what regions
 * an event should be dispatched.
 * </p><p>
 * This visitor also tracks whether an event object originates in a
 * substate-machine, which is critical for detecting and dispatching timer
 * events correctly into submachines.
 * </p><p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public class DesiredEventVisitor extends AbstractVisitor<EventTransitionPair> implements IDesiredEvent {
    private static final long serialVersionUID = 9169390734293473956L;

    // map of event object to its top-down chain of substate-prefixes, if any
    private Map<EventTransitionPair,String[]> substatePrefixOfEvent = Util.newMap();

    // stack tracking the current submachine being visited
    private Stack<State> curSubstateStack = new Stack<State>();
    // stack tracking the current orthogonal region being visited
    private Stack<Region> curRegionStack = new Stack<Region>();

    // cache of list of regions that "desires" an event signal (no prefix!)
    private Map<String,List<Region>> regionsDesiringEvent = Util.newMap();
    // cache of Substate that "desires" an event-by-prefixed-name
    private Map<String,State> substateDesiringEvent = Util.newMap();

    // flag indicating whether to populate list with null-event transitions
    private boolean enableNullEventTransitions = false;

    /**
     * Main constructor, sets up the visitor to always descend into SubMachines.
     */
    public DesiredEventVisitor (QueryPolicy nullEvTrans) {
        super(true);

        enableNullEventTransitions = (nullEvTrans == QueryPolicy.NULL_EV);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.IDesiredEvent#sort()
     */
    public void sort () {
        Collections.sort(this, new Comparator<EventTransitionPair>() {
            public int compare (EventTransitionPair p1, EventTransitionPair p2) {
                // first, compare on event prefixes
                int prefixComp = Util.join(substatePrefixOfEvent.get(p1),"")
                    .compareTo(Util.join(substatePrefixOfEvent.get(p2), ""));
                if (prefixComp == 0) {  // then, compare on event names
                    boolean isP1Null = (p1.getEvent() == null);
                    boolean isP2Null = (p2.getEvent() == null);
                    if (isP1Null || isP2Null) {
                        if (isP1Null && !isP2Null) {
                            return 1;
                        } else if (!isP1Null && isP2Null) {
                            return -1;
                        } else {  // BOTH null, considered equal
                            return 0;
                        }
                    } else {
                        return p1.getEvent().getName().compareTo(p2.getEvent().getName());
                    }
                } else {
                    return prefixComp;
                }
            }
        });
    }

    /**
     * If descending from State into one of its orthogonal regions, puts
     * that region on stack to track current orthogonal region.
     * If descending from State into a Substate-Machine, put the name of that
     * substate (NOT the SM) into the stack.
     * 
     * @see gov.nasa.jpl.statechart.model.visitor.AbstractVisitor#moveDown(gov.nasa.jpl.statechart.uml.NamedElement, gov.nasa.jpl.statechart.uml.NamedElement)
     */
    @Override
    public void moveDown (NamedElement from, NamedElement to) {
        super.moveDown(from, to);

        if (from instanceof State && to instanceof Region) {
            State s = (State) from;
            Region r = (Region) to;
            if (s.isOrthogonal()) {
                Util.debug("Descending into orthogonal region: " + r.getQualifiedName() + "...");
                // push orthogonal region into current stack
                curRegionStack.push(r);
            }
        } else if (from instanceof State
                && to instanceof StateMachine) {  // entering submachine
            State s = (State) from;
            StateMachine subm = (StateMachine) to;
            Util.debug("Descending into substate: " + s.getName() + ":"
                    + subm.getQualifiedName() + "...");
            // push submachine into current stack
            curSubstateStack.push(s);
        }
    }

    /**
     * When ascending from an orthogonal region, remove that region from the
     * stack of current orthogonal regions being visited.
     * Likewise if ascending from a Substate-Machine, remove it from stack.
     * 
     * @see gov.nasa.jpl.statechart.model.visitor.AbstractVisitor#moveUp(gov.nasa.jpl.statechart.uml.NamedElement, gov.nasa.jpl.statechart.uml.NamedElement)
     */
    @Override
    public void moveUp (NamedElement from, NamedElement to) {
        super.moveUp(from, to);

        if (from instanceof Region && to instanceof State) {
            Region r = (Region) from;
            if (curRegionStack.contains(r)) {
                // pop orthogonal region from current stack
                curRegionStack.pop();
                Util.debug("Leaving orthogonal region: " + r.getQualifiedName() + "...");
            }
        } else if (from instanceof StateMachine
                && to instanceof State) {  // exiting submachine
            StateMachine subm = (StateMachine) from;
            State s = (State) to;
            // pop submachine from current stack
            curSubstateStack.pop();
            Util.debug("Leaving substate : " + s.getName() + ":"
                    + subm.getQualifiedName() + "...");
        }
    }

    /**
     * Visits the state and extract all events on its outgoing transitions to
     * be registered as desired events, and cache regions and submachines
     * currently under visit that are interested in the events.
     * 
     * @see gov.nasa.jpl.statechart.model.visitor.AbstractVisitor#visit(gov.nasa.jpl.statechart.uml.State)
     */
    @Override
    public void visit (State state) {
        if (! curRegionStack.isEmpty()) {
            Util.debug("  - searching substate: " + state.getQualifiedName());
        }

        for (Transition transition : state.getOutgoing()) {
            Collection<Event> events = transition.getAllEvents();

            // if applicable, add a pair for null-event transition
            if (enableNullEventTransitions && transition.isNullEvent()) {
                EventTransitionPair evTrPair = new EventTransitionPair(null, transition);
                add(evTrPair);
                substatePrefixOfEvent.put(evTrPair, new String[0]);
            }

            // add to event-region map each event paired with each region under visit
            for (Event ev : events) {
                // collect as desired event
                EventTransitionPair evTrPair = new EventTransitionPair(ev, transition);
                add(evTrPair);

                if (Util.isDebugLevel()) {
                    List<String> names = Util.newList();
                    for (EventTransitionPair pair : this) {
                        if (pair.getEvent() == null) {
                            names.add("null event");
                        } else {
                            names.add(pair.getEvent().getName() + "@" + pair.getEvent().id());
                        }
                    }
                    Util.debug(">> desired event list: " + Util.join(names, ", "));
                }

                List<String> substates = Util.newList();
                // if we're within submachines, add substate(s) as prefix(es)
                for (State substate : curSubstateStack) {
                    substates.add(substate.getName());
                }
                // store the event's substate prependix
                substatePrefixOfEvent.put(evTrPair, substates.toArray(new String[0]));
                String evName = getSubstatePrefixString(evTrPair);

                // event-sub-statemachine mapping
                if (! curSubstateStack.isEmpty()) {
                    substateDesiringEvent.put(evName, curSubstateStack.get(0));
                }
                // event-region mapping
                List<Region> interestedRegions = regionsDesiringEvent.get(ev.getName());
                if (interestedRegions == null) {
                    interestedRegions = Util.newList();
                    regionsDesiringEvent.put(ev.getName(), interestedRegions);
                }
                for (Region r : curRegionStack) {
                    // register each region in the stack as interested regions
                    interestedRegions.add(r);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.IDesiredEvent#getEvents()
     */
    public List<EventTransitionPair> getEventTransitions () {
        return this;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.IDesiredEvent#getSubstatePrefixOfEvent(gov.nasa.jpl.statechart.uml.Event)
     */
    public String[] getSubstatePrefixOfEvent (EventTransitionPair evTrPair) {
        return substatePrefixOfEvent.get(evTrPair);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.IDesiredEvent#getSubmachinePrefixOfEvent(gov.nasa.jpl.statechart.uml.Event)
     */
    public String[] getSubmachinePrefixOfEvent (EventTransitionPair evTrPair) {
        String[] prefixes = getSubstatePrefixOfEvent(evTrPair);
        if (prefixes.length > 0) {
            List<String> prefixList = Arrays.asList(prefixes);
            prefixes = prefixList.subList(1, prefixList.size()).toArray(new String[0]);
        }
        return prefixes;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.IDesiredEvent#getSubcribingRegionsOfState(gov.nasa.jpl.statechart.uml.State, gov.nasa.jpl.statechart.uml.Event)
     */
    public Collection<Region> getDesiringRegionsOfState (State state, EventTransitionPair evTrPair) {
        Set<Region> subsRegions = Util.newSortedSet();  // start with empty set

        if (state.isOrthogonal()) {  // only care if state is orthogonal
            List<Region> regions = regionsDesiringEvent.get(evTrPair.getEvent().getName());
            if (regions != null) {
                // return only the immediate orthogonal regions of given state
                for (Region r : regions) {
                    if (r.getState() == state) {
                        subsRegions.add(r);
                    }
                }
            }
        }

        return subsRegions;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.IDesiredEvent#getSubtateSubscribingToEvent(gov.nasa.jpl.statechart.uml.Event)
     */
    public State getSubtateDesiringEvent (EventTransitionPair evTrPair) {
        return substateDesiringEvent.get(getSubstatePrefixString(evTrPair));
    }


    private String getSubstatePrefixString (EventTransitionPair evTrPair) {
        List<String> paths = Util.newList(Arrays.asList(substatePrefixOfEvent.get(evTrPair)));
        paths.add(evTrPair.getEvent().getName());
        return Util.join(paths, Util.PACKAGE_SEP);
    }

}
