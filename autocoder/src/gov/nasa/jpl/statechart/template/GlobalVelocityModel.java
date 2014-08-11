/**
 * Created Sep 28, 2009.
 * <p>
 * Copyright 2009, by the California Institute of Technology. ALL RIGHTS
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
package gov.nasa.jpl.statechart.template;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.model.IDesiredEvent;
import gov.nasa.jpl.statechart.model.IDesiredEvent.EventTransitionPair;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.model.PrefixOrderedWalker;
import gov.nasa.jpl.statechart.model.visitor.OrthogonalRegionVisitor;
import gov.nasa.jpl.statechart.model.visitor.TransitionVisitor;
import gov.nasa.jpl.statechart.uml.Signal;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.TimeEvent;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.UMLModel;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

/**
 * Velocity Model that provides query methods to facilitate autocode tasks
 * requiring the entire group of UML Models.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class GlobalVelocityModel extends AbstractVelocityModel {

    /** Group of UML Models that is being processed */
    protected final ModelGroup modelGrp;

    /**
     * Main constructor.
     * 
     * @param myModelGrp  the UML Model group from which to autocode
     */
    public GlobalVelocityModel (ModelGroup myModelGrp) {
        super();

        modelGrp = myModelGrp;
    }

    /**
     * Returns a collection of {@link StateMachine}s in this {@link ModelGroup}.
     * @return  {@link Collection} of {@link StateMachine}s in this {@link ModelGroup}.
     */
    public Collection<StateMachine> getStateMachines () {
        return modelGrp.getStateMachines();
    }


    /**
     * Returns a collection of Submachines in this {@link ModelGroup}, by
     * querying for the submachines of each {@link StateMachine} in this group.
     * 
     * @return  {@link Collection} of {@link StateMachine}s that are submachines in this {@link ModelGroup}.
     */
    public Collection<StateMachine> getSubmachines () {
        Set<StateMachine> subms = Util.newSet();

        for (StateMachine sm : getStateMachines()) {
            subms.addAll(getSubmachines(sm));
        }

        return subms;
    }

    /**
     * Returns all the unique {@link Signal}s in this {@link ModelGroup}.
     * @return  {@link Collection} of {@link Signal}s.
     */
    public Collection<Signal> getSignals () {
        Set<Signal> signals = Util.newSortedSet();
        
        for (UMLModel model : modelGrp.models()) {
            // Signal objects are not all available at model's top-level.
            signals.addAll(model.getSignals());
        }

        return signals;
    }

    /**
     * Returns the mapped names of all the {@link TimeEvent}s in this
     * {@link ModelGroup}.
     * 
     * @param mapper  TargetLanguageMapper used to map time event to name.
     * @return  {@link Collection} of String names of {@link TimeEvent}s.
     */
    public Collection<String> getTimeEventNames (TargetLanguageMapper mapper) {
        // put events by name into a set to get rid of duplicates
        SortedSet<String> timeEventMap = Util.newSortedSet();

        for (UMLModel model : modelGrp.models()) {
            // TimeEvent objects are all available at model's top-level.
            for (TimeEvent ev : model.getTimeEvents()) {
                timeEventMap.add(mapper.mapTimeEventToName(ev));
            }
            // collect submachine time-events as well
            IDesiredEvent query = querySubmachineTransitionTimeEvents(model);
            for (EventTransitionPair pair : query.getEventTransitions()) {
                // all events are known to be TimeEvents
                TimeEvent tEv = (TimeEvent) pair.getEvent();
                String name = mapper.mapTimeEventToName(tEv, query.getSubstatePrefixOfEvent(pair));
                timeEventMap.add(name);
            }
        }

        return timeEventMap;
    }

    /**
     * Returns whether any of the machine defines orthogonal region(s).
     * 
     * @return <code>true</code> if at least one state is an orthogonal state
     *      in any of the machine; <code>false</code> otherwise.
     */
    public boolean hasOrthogonalRegions () {
        boolean rv = false;

        OrthogonalRegionVisitor v = new OrthogonalRegionVisitor(false);
        for (StateMachine sm : getStateMachines()) {
            PrefixOrderedWalker.traverse(sm, v);
            if (v.size() > 0) {
                rv = true;
                break;
            }
        }

        return rv;
    }

    /**
     * Collects all the UML Transitions in this State-Machine, prefix ordered.
     * <p>
     * Note: Do NOT descend into submachines, as those would be handled
     * separately when processing those respective machines.
     * </p>
     * @return
     */
    public Collection<Transition> getAllTransitions () {
        return PrefixOrderedWalker.traverse(getStateMachine(), new TransitionVisitor(false));
    }

}
