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

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.Autocoder.SignalNamespaceType;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.model.BreadthFirstWalker;
import gov.nasa.jpl.statechart.model.IDesiredEvent;
import gov.nasa.jpl.statechart.model.IDesiredEvent.EventTransitionPair;
import gov.nasa.jpl.statechart.model.IDesiredEvent.QueryPolicy;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.model.ModelScape;
import gov.nasa.jpl.statechart.model.PrefixOrderedWalker;
import gov.nasa.jpl.statechart.model.UMLModelGroup;
import gov.nasa.jpl.statechart.model.visitor.AbstractVisitor;
import gov.nasa.jpl.statechart.model.visitor.AbstractVisitor.OrthoRegion;
import gov.nasa.jpl.statechart.model.visitor.CallActionVisitor;
import gov.nasa.jpl.statechart.model.visitor.DesiredEventVisitor;
import gov.nasa.jpl.statechart.model.visitor.FinalStateVisitor;
import gov.nasa.jpl.statechart.model.visitor.OrthogonalRegionVisitor;
import gov.nasa.jpl.statechart.model.visitor.StateVisitor;
import gov.nasa.jpl.statechart.model.visitor.SubmachineStateVisitor;
import gov.nasa.jpl.statechart.model.visitor.SubmachineVisitor;
import gov.nasa.jpl.statechart.model.visitor.TransitionSignalEventVisitor;
import gov.nasa.jpl.statechart.model.visitor.TransitionTimeEventVisitor;
import gov.nasa.jpl.statechart.model.visitor.VertexVisitor;
import gov.nasa.jpl.statechart.uml.Behavior;
import gov.nasa.jpl.statechart.uml.ConnectionPointReference;
import gov.nasa.jpl.statechart.uml.Element;
import gov.nasa.jpl.statechart.uml.Event;
import gov.nasa.jpl.statechart.uml.FinalState;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Namespace;
import gov.nasa.jpl.statechart.uml.OpaqueExpression;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.PseudostateKind;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.Signal;
import gov.nasa.jpl.statechart.uml.SignalEvent;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.TimeEvent;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.UMLElement;
import gov.nasa.jpl.statechart.uml.Vertex;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;

/**
 * Abstract base class for Velocity Models.
 * <p>
 * Most query methods do not require setting a StateMachine, but subclass
 * models that require specifying a specific machine can set that on this
 * base class, via {@link #setStateMachine(StateMachine)}.
 * </p><p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov> refactored from old VelocityModel
 */
public abstract class AbstractVelocityModel {

    private class NameComparator<T extends NamedElement> implements Comparator<T> {
        public int compare (T o1, T o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    // State machine that is being processed
    private StateMachine stateMachine = null;
    // cache of orthogonal regions
    private Collection<Region> orthoRegions = null;


    /**
     * Main constructor.
     */
    public AbstractVelocityModel () {
        // does nothing
    }

    /**
     * Returns the state machine context object used to make model queries,
     * if any.
     * @return UML {@link StateMachine} on which model queries is being performed.
     */
    public StateMachine getStateMachine () {
        return stateMachine;
    }

    /**
     * Sets the currently active state machine being autocoded.
     * 
     * @param sm  active UML {@link StateMachine} being autocoded.
     */
    public void setStateMachine (StateMachine sm) {
        stateMachine = sm;
    }

    /**
     * Returns a string identifying the type of object by taking the lowercase
     * of the type name.  For UML Pseudostates, this is equal to string
     * "pseudostate:kind" where "kind" is the Kind of the pseudostate.
     * Other examples are "state" for States, "region" for regions, 
     * "statemachine" for state machine nodes, etc.
     *
     * @param ne  UML NamedElement
     * @return a string representing the type of this element
     */
    public String getType (NamedElement ne) {
        if (ne instanceof Pseudostate) {
            return "pseudostate:" + ((Pseudostate) ne).getKind().name();
        }
        if (ne instanceof FinalState) {
            return "finalState";
        }
        if (ne instanceof State) {
            return "state";
        }
        if (ne instanceof Region) {
            return "region";
        }
        if (ne instanceof StateMachine) {
            return "statemachine";
        }
        if (ne instanceof ConnectionPointReference) {
            return "connectionPointReference";
        }
        if (ne instanceof Behavior) {
            return "behavior";
        }
        if (ne instanceof OpaqueExpression) {
            return "opaqueexpression";
        }
        // careful! this returns the most specific subtype
//        return ne.getClass().getSimpleName().toLowerCase();
        return "unknown";
    }

    /**
     * Returns all the NamedElements that would turn into Classes,
     * i.e., top-level StateMachine and all Orthogonal Regions,
     * all the way down the tree!
     * 
     * The Collection contains first the {@link StateMachine} associated with
     * this query model, and a list of orthogonal regions ordered top-down.
     * 
     * @return  Collection of UML NamedElement that are either {@link StateMachine} or {@link Region}s
     */
    public Collection<Namespace> getAllClassLevelElements () {
        // preserve insertion order
        List<Namespace> elems = Util.newList();

        // add StateMachine
        elems.add(stateMachine);

        // add all orthogonal subregions
        for (Region r : getAllOrthogonalRegions()) {
            elems.add(r);
        }

        return elems;
    }

    /**
     * Returns all orthogonal regions in this StateMachine, in breadth-first
     * order, so that the collection of {@link Region}s returned are ordered
     * top-down.
     * 
     * @return Collection of {@link Region}s ordered top-down.
     */
    public Collection<Region> getAllOrthogonalRegions () {
        if (orthoRegions == null) {
            orthoRegions = BreadthFirstWalker.traverse(stateMachine, new OrthogonalRegionVisitor(false));
        }

        return orthoRegions; 
    }
    
    /**
     * Gets the children regions of the supplied state if state is orthogonal. 
     * @param state  UML State whose child regions to obtain
     * @return  list of orthogonal regions.
     */
    public Collection<Region> getChildOrthogonalRegions (State state) {
        if (state.isOrthogonal()) {
            return sort(state.getRegion());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Retrieve all the Orthogonal regions that are nested within a given
     * named element, but do not include orthogonal regions within it.
     */
    public Collection<Region> getLocalOrthogonalRegions (Namespace element) {
        Set<Region> regions = Util.newSet();

        /* Consider only the states that are nested below this element;
         * and will include this element if it is a state.
         * 
         * For each state, check to see if it is an Orthogonal state. If so,
         * ensure that it is contained within the current state machine and
         * only then get its Orthogonal Regions.
         */
        for (State state : getStates(element, false, OrthoRegion.STOP_AT_ORTHO)) {
            // If the state is not an Orthogonal state, skip it
            if (!state.isOrthogonal())
                continue;

            // If the state is not owned by this state machine, ignore
            if (!state.getContainingStatemachine().equals(getStateMachine()))
                continue;

            // Retrieve each orthogonal region
            for (Region region : state.getRegion()) {
                regions.add(region);
            }
        }

        return regions;
    }

    /**
     * Returns a collection of States, which refer to sub-StateMachines,
     * within the supplied Namespace.
     *
     * @param ns  UML Namespace within which to look for submachine states.
     * @return  Collection of UML States that refer to submachines.
     */
    public Collection<State> getSubmachineStates (Namespace ns) {
        return PrefixOrderedWalker.traverse(ns, new SubmachineStateVisitor(true /*TODO Re-examine: should SubmachineStateVisitor descend into submachines?!*/));
    }

    /**
     * Returns Submachine states that are nested within a given named element
     * without including orthogonal regions within.
     * @param ns  UML {@link Namespace} within which to look for submachine states
     * @return  Collection of UML {@link State}s that are submachine states
     *      immediately within the given namespace, but not below orthogonal regions.
     */
    public Collection<State> getLocalSubmachineStates (Namespace ns) {
        Set<State> states = Util.newSet();

        /* Consider only states nested below this element, and include only
         * if it is a submachine state
         */
        for (State state : getStates(ns, false, OrthoRegion.STOP_AT_ORTHO)) {
            if (state.isSubmachineState()) {
                states.add(state);
            }
        }

        return states;
    }

    /**
     * Returns a collection of unique Submachines from submachine States
     * within the supplied Namespace.
     *
     * @param ns  UML Namespace within which to look for submachine states.
     * @return  Collection of UML StateMachines.
     */
    public Collection<StateMachine> getSubmachines (Namespace ns) {
        return PrefixOrderedWalker.traverse(ns, new SubmachineVisitor(true));
    }

    /**
     * Determines if the supplied state machine object is terminate-able, that
     * is, if its state handlers should have a BAIL Signal case, whether to
     * code the reinit function, and also whether "final" state handler should
     * be coded (any final state will have its own state handler).
     * 
     * The determining factors include whether the supplied {@link Namespace} is
     * a Region, or if a StateMachine, whether it is referenced as a submachine
     * in the current ModelGroup set.
     * 
     * @param ns  A {@link StateMachine} or {@link Region} object.
     * @return <code>true</code> if sm is considered terminate-able;
     *   <code>false</code> otherwise.
     */
    public boolean isMachineTerminable (Namespace ns) {
        if (ns instanceof Region) {
            return true;
        }

        if (!(ns instanceof StateMachine)) {
            return false;
        }

        // see if any other state machine in the ModelGroup contains a substate
        // that refers to me as StateMachine.
        for (StateMachine searchSM : UMLModelGroup.element2Model(ns).getModelGroup().getStateMachines()) {
            for (State s : getSubmachineStates(searchSM)) {
                if (ns.equals(s.getSubmachine())) {
                    // found!
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Determines if the supplied state machine object needs to terminate any
     * substate or orthogonal region within.  The determining factors include
     * whether this machine has any orthogonal regions, or any submachine state
     * even within orthogonal regions.
     * 
     * @param ns  A {@link StateMachine} or {@link Region} object.
     * @return <code>true</code> if sm is considered terminate-able;
     *   <code>false</code> otherwise.
     */
    public boolean isMachineTerminator (Namespace ns) {
        if (!(ns instanceof StateMachine)) {
            return false;
        }

        return (getLocalOrthogonalRegions(ns).size() > 0) || (getSubmachineStates(ns).size() > 0);
    }

    /**
     * Checks whether the supplied Region is an ancestor region of the
     * supplied State.
     * @param state   UML State to check
     * @param region  UML Region to check
     * @return <code>true</code> if Region is in the ancestor axis of the State;
     *   <code>false</code> otherwise.
     */
    public boolean isAncestorRegion (State state, Region region) {
        Region ancestorRegion = state.getContainer();
        while (ancestorRegion != null && ancestorRegion != region) {
            State parentState = ancestorRegion.getState();
            if (parentState != null) {
                ancestorRegion = parentState.getContainer();
            } else {
                ancestorRegion = null;
            }
        }
        // ancestor region IS the supplied region if not NULL
        return ancestorRegion != null;
    }

    /**
     * Returns the enclosing orthogonal region of a given state, or null if
     * state NOT within an orthogonal region.
     * 
     * @param state  the state whose enclosing orthogonal region is sought
     * @return the enclosing Region
     */
    public Region getEnclosingOrthogonalRegion (Vertex state) {
        if (state == null) return null;

        State parent = null;
        Region region = state.getContainer();
        // Loop Invariant:  stop when found parent OR exhausted parent region
        while (parent == null && region != null) {
            parent = region.getState();  // parent state of region
            if (parent != null) {
                if (parent.isOrthogonal()) {
                    // that's it, found desired enclosing region!
                } else {  // try further up
                    region = parent.getContainer();
                    parent = null;
                }
            } else {  // can't go up anymore
                region = null;
            }
        }

        return region;
    }

    /**
     * Collects and returns descendant states, self included if a State. 
     * Descent into submachines is determined by argument to the
     * <code>descend</code> parameter.  Orthogonal regions are included.
     * 
     * @param ns  UML {@link Namespace} whose descendant states to get.
     * @param descend  flag indicating whether to descend into submachines.
     * @return  Collection of contained {@link State}.
     */
    public Collection<State> getStates (Namespace ns, boolean descend) {
        return PrefixOrderedWalker.traverse(ns, new StateVisitor(descend));
    }

    /**
     * Collects and returns descendant states, self included if a State. 
     * Descent into submachines and orthogonal regions are separate options.
     * 
     * @param ns  UML {@link Namespace} whose descendant states to get.
     * @param descend  flag indicating whether to descend into submachines.
     * @param include  flag indicating whether to include orthogonal regions.
     * @return  Collection of contained {@link State}.
     */
    protected Collection<State> getStates (Namespace ns, boolean descend, OrthoRegion include) {
        return PrefixOrderedWalker.traverse(ns, new StateVisitor(descend, include));
    }

    /**
     * Collects and returns descendant vertices, including Pseudostates and
     * States.
     * 
     * @param ns  UML Namespace whose descendant vertices to get.
     * @param descend  flag indicating whether to descend into submachines.
     * @return  Collection of contained {@link Vertex}.
     */
    public Collection<Vertex> getVertices (Namespace ns, boolean descend) {
        return PrefixOrderedWalker.traverse(ns, new VertexVisitor(descend));
    }

    /**
     * Returns a list of all the children states of this parent state.
     * 
     * @param state  UML State whose immediate child state to gather.
     * @return
     */
    public Collection<State> getImmediateChildStates (State state) {
        List<State> states = Util.newList();

        // Retrieve states from each orthogonal region
        for (Region region : state.getRegion()) {
            states.addAll(Util.filter(region.getSubvertex(), State.class));
        }

        return states;
    }

    /**
     * Returns the parent state, or StateMachine, of this NamedElement,
     * or null if top of model reached.
     * 
     * @return the parent State or StateMachine NamedElement.
     */
    public NamedElement getParentState (NamedElement ne) {
        NamedElement parent = null;
        // loop invariant:  terminate when ne == null
        while (ne != null) {
            parent = ne.getParent();
            if (parent instanceof State
                    || parent instanceof StateMachine) {
                // we're golden
                break;
            } else {
                ne = parent;
                // null out parent to prevent returning a wrong element
                parent = null;
            }
        }
        return parent;
    }

    /**
     * Returns the nearest enclosing orthogonal state, or null if none.
     * 
     * @param v  the State for which to find nearest enclosing orthogonal state
     * @return the parent State of an enclosing orthogonal region, if any.
     */
    public State getParentOrthogonalState (Vertex state) {
        Region parent = getEnclosingOrthogonalRegion(state);
        return (parent == null) ? null : parent.getState();
    }

    /**
     * Returns a collection of {@link FinalState}s within supplied namespace.
     * No descent into submachine is necessary as the submachines should take
     * care of their own final state reseting.
     * 
     * @param ns  Namespace within which to search for FinalStates.
     * @return  Collection of UML {@link FinalState}.
     */
    public Collection<FinalState> getFinalStates (Namespace ns) {
        // don't need to descend into submachines
        return PrefixOrderedWalker.traverse(ns, new FinalStateVisitor(false));
    }

    /**
     * Returns the set of TimeEvents that trigger transition from a particular state.
     */
    public Collection<TimeEvent> getTimeEvents (Vertex state) {
        List<TimeEvent> events = new ArrayList<TimeEvent>();
        // For each transition out of the state, examine its triggers
        // and keep the time event triggers
        for (Transition transition : state.getOutgoing()) {
            events.addAll(transition.getTimeEvents());
        }
        return events;
    }

    private Map<Namespace,Set<State>> statesExpectingCompletion = Util.newMap();
    /**
     * Returns a sorted set of States that expect an internal completion event
     * to be generated.  This would only apply to composite and submachine
     * states, and should not consider empty outgoing transitions at all.
     * This is useful for generating completion-event signals and subscriptions.
     * <p>
     * Note: this method is potentially time-consuming, so we cache the result
     * set from the first invocation of this get method, and return that in
     * future invocations.
     * </p>
     * @param ns  Namespace within which to search for states that expect
     *      internal completion event(s).
     * @return  {@link Collection} of UML {@link State}s.
     * 
     * TODO Known issue: submachine of submachine wouldn't be considered!
     */
    public Collection<State> getStatesExpectingCompletionEvent (Namespace ns) {
        Set<State> states = statesExpectingCompletion.get(ns);
        if (states == null) {
            states = Util.newSortedSet();
            statesExpectingCompletion.put(ns, states);
            for (State state : getStates(ns, false)) {
                if (!state.isSimple()) {
                    if (state.isSubmachineState()) {  // expect completion event
                        states.add(state);
                    } else {  // look for a final state one-level down ONLY
                        for (Region r : state.getRegion()) {
                            if (Util.filter(r.getSubvertex(), FinalState.class).size() > 0) {
                                states.add(state);
                            }
                        }
                    }
                }
            }
        }
        return states;
    }

    public Collection<String> getCompletionSignalSet (Namespace ns, TargetLanguageMapper mapper) {
        Set<String> sigSet = new LinkedHashSet<String>();
        for (State state : getStatesExpectingCompletionEvent(ns)) {
            String sig = null;
            if (state.isSubmachineState()) {
                // make sure not already in set
                sig = mapper.mapToSignalEnum(state.getSubmachine());
            } else {
                sig = mapper.mapToSignalEnum(state);
            }
            if (!sigSet.contains(sig)) {
                sigSet.add(sig);
            }
        }
        if (ns instanceof StateMachine) {  // look one-level down for FinalState
            for (Region r : ((StateMachine) ns).getRegion()) {
                if (Util.filter(r.getSubvertex(), FinalState.class).size() > 0) {
                    String sig = mapper.mapToSignalEnum(ns);
                    if (!sigSet.contains(sig)) {
                        sigSet.add(sig);
                    }
                }
            }
        }
        return sigSet;
    }

    /**
     * Returns empty transitions originating from a {@link State} (not needed
     * for Pseudostates), meaning transitions that do not define a SignalEvent
     * nor TransitionEvent, INCLUDING from exitPoints of that State.
     * 
     * @param state UML {@link State} whose outgoing transitions to scan for.
     * @return {@link Collection} of outgoing {@link Transition} without event.
     */
    public Collection<Transition> getEmptyTransitions (State state) {
        List<Transition> trans = Util.newList();

        // find transitions without trigger event AND that are sourced at state
        for (Transition transition : state.getOutgoing()) {
            if (transition.getAllEvents().size() == 0) {
                trans.add(transition);
            }
        }
        // validation already done, so no additional error-checking here

        return trans;
    }

    /**
     * Returns a list of transitions with either SignalEvents or TimeEvents
     * trigger out of the given UML State.
     * @param state  the UML state for which to compute transition events.
     */
    public Collection<Transition> getTransitionsWithEvent (Vertex state) {
        List<Transition> trans = Util.newList();

        for (Transition transition : state.getOutgoing()) {
            if (transition.getAllEvents().size() > 0) {
                trans.add(transition);
            }
        }

        return trans;
    }

    /**
     * Returns the set of Transitions from a particular namespace whose trigger is
     * a time event.
     */
    public Collection<Transition> getTransitionsWithTimeEvent (Namespace ns, boolean descend) {
        return PrefixOrderedWalker.traverse(ns, new TransitionTimeEventVisitor(descend));
    }

    /**
     * Returns the set of Transitions from a particular namespace whose trigger is
     * a signal event.
     */
    public Collection<Transition> getTransitionsWithSignalEvent (Namespace ns, boolean descend) {
        return PrefixOrderedWalker.traverse(ns, new TransitionSignalEventVisitor(descend));
    }

    /**
     * Returns a sorted list of unique signal events for all the transitions in
     * the supplied Namespace.
     * 
     * @param ns  Namespace whose transitions to examine.
     * @return  {@link Collection} of {@link SignalEvent}s sorted by name.
     */
    public Collection<SignalEvent> getTransitionSignalEvents (Namespace ns) {
        Set<SignalEvent> signalEvents = Util.newSet();

        for (Transition transition : getTransitionsWithSignalEvent(ns, true)) {
            signalEvents.addAll(transition.getSignalEvents());
        }

        return sortBySignal(signalEvents);
    }

    /**
     * Returns a unique list of time events for all the transitions in the
     * supplied Namespace, but without descending into submachines.
     * 
     * @param ns  Namespace whose transitions to examine.
     * @return  {@link Collection} of {@link TimeEvent}s.
     */
    public Collection<TimeEvent> getTransitionTimeEvents (Namespace ns) {
        Set<TimeEvent> timeEvents = Util.newSet();

        for (Transition transition : getTransitionsWithTimeEvent(ns, false)) {
            timeEvents.addAll(transition.getTimeEvents());
        }

        return sort(timeEvents);
    }

    /**
     * Returns the set of transitions from a Junction pseudostate, ordered so
     * that the last one is the "else", the branch without a guard (or has
     * a guard with the sole words "else" or "no", case-insensitive).
     * @param entry
     * @return
     */
    public List<Transition> getJunctionTransitions (Pseudostate pseudo) {
        SortedMap<String,Transition> specTransMap = new TreeMap<String,Transition>();
        Transition elseBranch = null;
        for (Transition trans : pseudo.getOutgoing()) {
            if (hasGuard(trans)) {
                // get the spec text to sort the transitions by spec
                String spec = trans.getGuard().getSpecification().stringValue();
                specTransMap.put(spec, trans);  // add guarded transition
            } else {
                // save elseGuard for last in list
                if (elseBranch != null) {  // uh oh! already one else branch!
                    Util.error("ERROR in AbstractVelocityModel.getJunctionTransitions()! More than one default branch in junction/choice '"
                            + pseudo.getQualifiedName() + "'!");
                } else {
                    elseBranch = trans;
                }
            }
        }

        // now obtain sorted transition list from the map
        List<Transition> transList = Util.newList();
        transList.addAll(specTransMap.values());

        if (specTransMap.size() > 0) {
            // add the noGuard transition; null as placeholder if none
            transList.add(elseBranch);
        } else if (elseBranch == null) {
            Util.info("AbstractVelocityModel.getJunctionTransitions(): No outgoing transition from Pseudostate " + pseudo.getQualifiedName() + ", so default 'else' branch added.");
        }

        return transList;
    }

    /**
     * Returns whether the supplied namespace (usually StateMachine or Region)
     * has any guard functions, i.e. function calls serving as a guard, at and
     * below the namespace, descending any submachines.
     * 
     * @param ns  UML {@link Namespace}
     * @return  <code>true</code> if namespace has any guard function, <code>false</code> otherwise.
     */
    public boolean hasGuardFunction (Namespace ns) {
        boolean rv = false;
    
        for (FunctionCall fc : getCallActions(ns, true)) {
            if (fc.isGuard()) {
                rv = true;
                break;
            }
        }
    
        return rv;
    }

    /**
     * Returns whether the supplied transition has a valid guard, which means
     * a non-null guard object that has non-empty specification body AND
     * the spec is not "else" or "no" (case-insensitive).
     * 
     * @param trans  UML {@link Transition}
     * @return  <code>true</code> if transition has a valid guard, <code>false</code> otherwise.
     */
    public boolean hasGuard (Transition trans) {
        boolean rv = false;
        if (trans.getGuard() != null) {  // non-null guard object
            String specBody = trans.getGuard().getSpecification().stringValue();
            if (specBody != null && specBody.length() > 0
                    && !"else".equalsIgnoreCase(specBody)
                    && !"no".equalsIgnoreCase(specBody)) {
                // valid, non-zero-length specification body AND not 'else'
                rv = true;
            }
        }
        return rv;
    }

    /**
     * Retrieves a collection of all signals that are fired as signal events
     * within the supplied {@link Namespace} (descending within orthogonal
     * regions, if applicable, e.g., StateMachine).
     * Matching is by signal name only, considering package
     * namespaces if {@link SignalNamespaceType#LOCAL} is enabled.
     * Note that Signals must exist within model, or else this query will
     * simply issue a warning and then ignore it.
     * 
     * @param ns  UML {@link Namespace} within which to search for action signal events.
     * @return  {@link Collection} of {@link Signal}s fired within the Namespace.
     */
    public Collection<Signal> getActionEventSignals (Namespace ns) {
        Set<Signal> signals = Util.newSet();

        // first, retrieve all signals we fire as action event
        ModelScape modelScape = UMLModelGroup.element2Model(ns).getModelScape();
        Map<String,Behavior> firedSignals = modelScape.getCachedFiredSignals(ns);
        if (firedSignals.size() > 0) {  // now we do the heavier lifting
            // search by name in all signals within model group
            Map<String,Signal> signalsByName = modelScape.getCachedSignalsByName();
            for (String fSig : firedSignals.keySet()) {
            	Signal sig = findSignalByName(fSig, signalsByName);
            	if (sig != null) {
            		signals.add(sig);
            	}
            }
        }

        return signals;
    }

    public Signal findSignalByName (String fSig, Map<String,Signal> signalsByName) {
    	Signal sig = null;
        if (signalsByName.containsKey(fSig)) {
            sig = signalsByName.get(fSig);
        } else {
            int sepIdx = fSig.lastIndexOf(TargetLanguageMapper.UML_SEPARATOR);
            if (sepIdx > -1) {
                // try removing namespace
                String strippedSig = fSig.substring(sepIdx+TargetLanguageMapper.UML_SEPARATOR.length());
                if (signalsByName.containsKey(strippedSig)) {
                    sig = signalsByName.get(strippedSig);
                }
            } else {
                // try tacking on SM's namespace
                String nsSig = Util.joinWithPrefixes(getStateMachine().getPackageNames(), fSig, TargetLanguageMapper.UML_SEPARATOR);
                if (signalsByName.containsKey(nsSig)) {
                    sig = signalsByName.get(nsSig);
                }
            }
        }

        return sig;
    }

    /**
     * Returns the collection of Signal or Time Events that a StateMachine
     * is interested in.
     * @param sm  UML StateMachine whose events of interest to collect
     * @return  Collection of UML Events the StateMachine has interest in.
     */
    public Collection<Event> getDesiredEvents (StateMachine sm) {
        List<Event> events = Util.newList();

        for (State state : PrefixOrderedWalker.traverse(sm, new StateVisitor(true))) {
            for (Transition transition : getTransitionsWithEvent(state)) {
                events.addAll(transition.getAllEvents());
            }
        }

        return events;
    }

    /**
     * Returns whether the given {@link StateMachine} subscribes to the given
     * {@link Event} (either {@link SignalEvent} or {@link TimeEvent}) by name.
     * This query involves traversing the entire StateMachine to find out its
     * desired events, and then iterating the set to see if the supplied event
     * matches any in the set by name, using the supplied name mapper.
     * 
     * TODO subscribesToEvent query: may need to cache transition events to optimize run time
     * 
     * @param sm  {@link StateMachine} whose subscribed events to query for a match.
     * @param ev  {@link Event} to match in StateMachine's set of subscribed events.
     * @param mapper  {@link TargetLanguageMapper} used to get the event's name
     * @return  <code>true</code> if <code>e</code> is in <code>sm</code>'s set of subscribed events,
     *      <code>false</code> otherwise.
     */
    public boolean subscribesToEvent (StateMachine sm, Event ev, TargetLanguageMapper mapper) {
        String evName = mapper.mapEventToLiteral(ev);
        for (Event checkEv : getDesiredEvents(sm)) {
            String checkEvName = mapper.mapEventToLiteral(checkEv);
            if (evName.equals(checkEvName)) {
                // found a matching event by name!
                return true;
            }
        }

        // did not match event in list of desired events
        return false;
    }

    // cache of visitor for desired events
    private DesiredEventVisitor desiredEventVisitor = null;
    /**
     * Returns a query object that allows retrieving the set of either
     * SignalEvents or TimeEvents that trigger transition out of the supplied
     * state.  Information can be retrieved about descendant orthogonal regions
     * and submachines of the supplied state subscribing to particular events.
     * 
     * @param state  the UML state for which to compute desired events.
     * @param nullEv enable or disable null-event transitions, depending on back-end
     * @return {@link IDesiredEvent} query interface to obtain desired event information.
     */
    public IDesiredEvent queryDesiredEvents (State state, QueryPolicy nullEv) {
        if (desiredEventVisitor == null) {
            desiredEventVisitor = new DesiredEventVisitor(nullEv);
        }
        PrefixOrderedWalker.traverse(state, desiredEventVisitor);
    
        return desiredEventVisitor;
    }
    public void clearDesiredEvents (QueryPolicy nullEv) {
        if (desiredEventVisitor == null) {  // lazy init needed for empty list
            desiredEventVisitor = new DesiredEventVisitor(nullEv);
        }
        desiredEventVisitor.clear();
    }

    /**
     * Returns a query object that allows retrieving time events for all the
     * transitions in sub-statemachines of the supplied Namespace.
     * 
     * @param ns  Namespace whose sub-statemachine transitions to examine.
     * @return {@link IDesiredEvent} query interface to obtain desired TimeEvent information.
     */
    public IDesiredEvent querySubmachineTransitionTimeEvents (Namespace ns) {
        clearDesiredEvents(QueryPolicy.NO_NULL_EV);
        for (State state : getSubmachineStates(ns)) {
            IDesiredEvent query = queryDesiredEvents(state, QueryPolicy.NO_NULL_EV);
            List<EventTransitionPair> evTrPairs = query.getEventTransitions();
            for (EventTransitionPair pair : new ArrayList<EventTransitionPair>(evTrPairs)) {
                if (!(pair.getEvent() instanceof TimeEvent)) {
                    // get rid of non-TimeEvents
                    evTrPairs.remove(pair);
                }
            }
        }
        return desiredEventVisitor;
    }

    /**
     * Returns the initial state for a region, state or statemachine.  The
     * states are always contained in region.  If we are given a
     * statemachine or state, we look for a region.  If there is more than
     * one region, throw an exception.
     */
    public Pseudostate getInitialState (Namespace ns) {
        // There must be a region, but we'll let StateMachine and State pass, too.
        if (ns instanceof StateMachine) {
            Collection<Region> regions = ((StateMachine) ns).getRegion();
            if (regions.size() != 1) return null;

            ns = regions.iterator().next();
        }

        if (ns instanceof State) {
            Collection<Region> regions = ((State) ns).getRegion();
            if (regions.size() != 1) return null;

            ns = regions.iterator().next();
        }

        // Now, we should have a region, if not return null
        if (ns instanceof Region) {
            for (Vertex subvertex : ((Region) ns).getSubvertex()) {
                if (!(subvertex instanceof Pseudostate)) continue;

                Pseudostate ps = (Pseudostate) subvertex;

                if (!ps.getKind().equals(PseudostateKind.initial)) continue;

                return ps;
            }
        }

        return null;
    }

    /**
     * Returns the initial transition from a Pseudostate, or null if none found
     * (and print error message!)
     * @param pseudo  UML Pseudostate to find initial transition from
     * @return  UML Transition that represents the initial transition
     */
    public Transition getInitialTransition (Pseudostate pseudo) {
        if (pseudo == null) {
            Util.error("Error! Supplied initial state was NULL!");
            // TODO should we throw exception, print stack trace, or something??
            return null;
        }

        Collection<Transition> transList = pseudo.getOutgoing();
        if (transList.size() == 0) {  // error should already be reported
            return null;
        }
        if (transList.size() > 1) {
            Util.error("Initial state " + pseudo.getQualifiedName()
                    + " has more than one outgoing transition! I'm picking the first transition, but you might not get the right behavior.");
        }

        return transList.iterator().next();
    }

    /**
     * Given a source state and a transition, returns the target state.
     * 
     * @param source  source UML {@link State}
     * @param transition  transition to take from <code>source</code>
     * @return  target UML {@link Vertex} transition from <code>source</code> via <code>transition</code>.
     */
    public Vertex getTarget (Vertex source, Transition transition) {
        Vertex target = transition.getTarget();
        if (!source.equals(transition.getSource())) {
            // if transition source is a Connection Point Ref or exitPoint,
            // make sure that the state it resides on is the source state.
            boolean erroneous = false;
            if (transition.getSource() instanceof ConnectionPointReference) {
                ConnectionPointReference cpr = (ConnectionPointReference) transition.getSource();
                if (!source.equals(cpr.getState())) {
                    throw new RuntimeException("Transition source ConnectionPointReference '"
                            + cpr.getQualifiedName()
                            + "' NOT defined on given source State '"
                            + source.getQualifiedName() + "'!");
                } // otherwise, OK
            } else if (transition.getSource() instanceof Pseudostate) {
                Pseudostate pseudo = (Pseudostate) transition.getSource();
                if (pseudo.getKind() == PseudostateKind.exitPoint) {
                    if (!source.equals(pseudo.getState())) {
                        throw new RuntimeException("Transition source Pseudostate:exitPoint '"
                                + pseudo.getQualifiedName()
                                + "' NOT defined on given source State '"
                                + source.getQualifiedName() + "'!");
                    } // otherwise, OK
                } else {  // not a special case, considered error
                    erroneous = true;
                }
            } else {
                erroneous = true;
            }
            if (erroneous) {
                // generic error
                throw new RuntimeException("Given source '"
                        + source.getQualifiedName()
                        + "' does NOT match transition source '"
                        + transition.getSource().getQualifiedName() + "'!");
            }
        }
        return target;
    }


    // cache of call actions by a starting UML Namespace tree-vertex
    private Map<Namespace,Collection<FunctionCall>> funcsInNamespace = Util.newMap();
    private Map<Namespace,Collection<FunctionCall>> funcsInDeepNamespace = Util.newMap();

    /**
     * Returns a Collection of call-actions in the model tree, descending from
     * the given Namespace, down into children regardless of orthogonal regions.
     * The call-actions are returned as {@link FunctionCall} objects for query
     * convenience.
     * 
     * @param ns  UML Namespace object to descend from.
     * @param descend  whether to descend into Submachines.
     * @return  Collection of FunctionCall objects.
     */
    public Collection<FunctionCall> getCallActions (Namespace ns, boolean descend) {
        Collection<FunctionCall> rv = null;
        if (descend) {
            rv = funcsInDeepNamespace.get(ns);
        } else {
            rv = funcsInNamespace.get(ns);
        }

        if (rv == null) {  // walk tree and store outcome in cache map
            CallActionVisitor v = new CallActionVisitor(descend, OrthoRegion.INCLUDE_BELOW_ORTHO);
            PrefixOrderedWalker.traverse(ns, v);
            Collections.sort(v);
            if (descend) {
                funcsInDeepNamespace.put(ns, v);
            } else {
                funcsInNamespace.put(ns, v);
            }
            rv = v;
        }
        return rv;
    }

    /**
     * Returns the call actions for just one state or pseudostate; the
     * underlying visitor will look at the state itself and its outgoing
     * transitions only; inner states are not visited.
     * 
     * @param vertex  UML Pseudostate or State in which to search for call actions.
     * @return  Collection of FunctionCall objects.
     */
    public Collection<FunctionCall> getCallActionsOfVertex (Vertex vertex) {
        CallActionVisitor v = new CallActionVisitor(false, OrthoRegion.STOP_AT_ORTHO);
        if (vertex instanceof State) {
            v.visit((State) vertex);
        } else if (vertex instanceof Pseudostate) {
            v.visit((Pseudostate) vertex);
        }
        return v;
    }

    /**
     * Returns a Collection of all call-actions in the model tree, descending
     * from the StateMachine down into orthogonal regions, but NOT submachines.
     * This method is used solely for autocoding impl-classes.
     * 
     * @return  Collection of {@link FunctionCall} objects.
     */
    public Collection<FunctionCall> getAllCallActions () {
        return getCallActions(stateMachine, false);
    }

    /**
     * Returns whether the StateMachine contains any state with Do activity.
     * The Visitor stops searching (but will still visit the entire model tree)
     * as soon as one is found.
     * 
     * @param sm  UML StateMachine to search for Do activity.
     * @return  <code>true</code> if any state has Do activity, <code>false</code> otherwise.
     */
    public boolean hasDoActivity (StateMachine sm) {
        return PrefixOrderedWalker.traverse(sm, new AbstractVisitor<Boolean>(false) {
            private static final long serialVersionUID = 1612572786346429584L;
            private boolean foundDoActivity = false;
            @Override
            public void visit (State state) {
                if (foundDoActivity) return;
                if (hasDoActivity(state)) {
                    // found one, done!
                    add(true);
                    foundDoActivity = true;
                    return;
                }
            }
        }).size() > 0;
    }
    public boolean hasDoActivity (State state) {
        return state.getDo() != null && state.getDo().actionList().size() > 0; 
    }


    /**
     * Queries for all the signal packages that the supplied StateMachine
     * depends upon, based on its desired events.  It bins the desired events
     * by their namespace path prefixes.  It excludes the signals package
     * in which this StateMachine belongs (that's handled by the header file).
     * 
     * @param sm  {@link StateMachine} for which to look for signal packages
     * @return  Sorted {@link Collection} of Strings identifying the package paths.
     */
    public Collection<String> getRequiredSignalPackagePaths (StateMachine sm, TargetLanguageMapper mapper) {
        Set<String> pkgs = Util.newSortedSet();
        // only care if we're using local signal namespaces
        if (Autocoder.inst().getSignalNamespaceType() == SignalNamespaceType.LOCAL) {
            // fetch all desired events and include the signal's packages
            for (Event ev : getDesiredEvents(sm)) {
                if (ev instanceof SignalEvent) {
                    pkgs.add(mapper.mapToNamespacePathPrefix(((SignalEvent) ev).getSignal().getPackageNames()));
                } else if (ev instanceof TimeEvent) {
                    pkgs.add(mapper.mapToNamespacePathPrefix(ev.getPackageNames()));
                }
            }
            // also fetch the fired signals
            for (Signal sig : getActionEventSignals(sm)) {
                pkgs.add(mapper.mapToNamespacePathPrefix(sig.getPackageNames()));
            }
            // remove from set this state machine's path
            pkgs.remove(mapper.mapToNamespacePathPrefix(sm.getPackageNames()));
        }
        return pkgs;
    }

    public Collection<String[]> getRequiredSignalPackagePaths (ModelGroup modelGrp, TargetLanguageMapper mapper) {
    	Collection<String> pkgs = Util.newSortedSet();
    	for (StateMachine sm : modelGrp.getStateMachines()) {
    		String[] pkgArray = sm.getPackageNames();
    		if (pkgArray.length > 0) {
        		pkgs.add(mapper.mapToNamespacePathPrefix(pkgArray));
    		}
    	}
    	// split each path up into String arrays
    	Collection<String[]> sigPkgs = Util.newList();
    	for (String pkg : pkgs) {
    		sigPkgs.add(pkg.split("/"));
    	}
    	return sigPkgs;
    }


    /////////////////////////////////////////////////
    // Utility methods for manipulating model parts
    /////////////////////////////////////////////////

    public boolean isCallAction (String exprStr) {
        return FunctionCall.isFunctionCall(exprStr);
    }

    /**
     * Returns the call action function object containing name and args.
     * @param beh  Behavior object (entry, exit, do, etc.)
     * @return
     */
    public FunctionCall getCallParts (Behavior beh) {
        return new FunctionCall(beh.getName());
    }

    /**
     * Returns the call function object containing name and args.
     * @param spec  Expression spec object (mostly for transition guards)
     * @return
     */
    public FunctionCall getCallParts (OpaqueExpression spec) {
        return new FunctionCall(spec.getBody());
    }

    /**
     * Common method that takes a string, presumably a method call, and returns
     * a {@link FunctionCall} object containing its name and optional args.
     * @param exprStr  expression String to parse
     * @return  {@link FunctionCall} object
     */
    public FunctionCall getCallParts (String exprStr) {
        return new FunctionCall(exprStr);
    }

    /**
     * Sort a collection of named elements by qualified name using a {@link SortedMap}.
     */
    public <T extends NamedElement> SortedMap<String, T> sort (Map<String, T> map) {
        return new TreeMap<String, T>(map);
    }

    /**
     * Sort a collection of named elements by elements alone.
     */
    public <T extends NamedElement> Collection<T> sort (Collection<T> elements) {
        List<T> toSort = new LinkedList<T>(elements);
        Collections.sort(toSort, new NameComparator<T>());
        return toSort;
    }

    /**
     * Sort a collection of signal events by their signal names
     */
    public List<SignalEvent> sortBySignal (Collection<SignalEvent> events) {
        SortedMap<String,SignalEvent> nameEventMap = Util.newSortedMap();
        for (SignalEvent ev : events) {
            Signal signal = ev.getSignal();
            if (signal == null) {  // error!
                // Validator catches this, or NOT if back-end skips, so no error
                nameEventMap.put(ev.getName(), ev);
            } else {
                nameEventMap.put(signal.getName(), ev);
            }
        }
        return Util.newList(nameEventMap.values());
    }

    public <E> List<E> newList (E item) {
        return new ArrayList<E>();
    }

    public Set<NamedElement> makeSet () {
        return new LinkedHashSet<NamedElement>();
    }

    public Set<NamedElement> addToSet (Set<NamedElement> set, Collection<NamedElement> toAdd) {
        Set<NamedElement> newSet = new LinkedHashSet<NamedElement>();
        if (set != null) {
            newSet.addAll(set);
        }
        if (toAdd != null) {
            newSet.addAll(toAdd);
        }
        return newSet;
    }

    public Set<NamedElement> removeFromSet (Set<NamedElement> set, Collection<NamedElement> toRemove) {
        Set<NamedElement> newSet = new LinkedHashSet<NamedElement>();
        if (set != null && !set.isEmpty()) {  // first, add source set to new set
            newSet.addAll(set);
            if (toRemove != null) {  // then, remove removal set from new set
                newSet.removeAll(toRemove);
            }
        }
        return newSet;
    }

    public Set<NamedElement> removeFromSet (Set<NamedElement> set, NamedElement itemToRemove) {
        List<NamedElement> toRemove = new ArrayList<NamedElement>();
        toRemove.add(itemToRemove);
        return removeFromSet(set, toRemove);
    }

    /**
     * This is a MagicDraw 12.5/16.0/16.5 specific method.
     *
     * For MagicDraw 12.5/16.0/16.5 files, the event that are owned
     * by a particular state machine are placed inside an
     * <xmi:extension> tag.  The reason for this is that *all* the
     * event (TimeEvent, SignalEvent, etc.) tags may be declared
     * at the top level
     */
    protected List<Event> getOwnedEvents (StateMachine stateMachine) {
        List<Event> events = Util.newList();

        // Use XPath to get all the idrefs of any
        // xmi:extention/modelExtension/event tags
        try {
            String query = "xmi:Extension/modelExtension/event/@xmi:idref";
            NodeList idrefs = (NodeList) UMLElement.xpath.evaluate(query,
                    ((UMLElement) stateMachine).getNode(),
                    XPathConstants.NODESET);

            // Look up the events based on the idrefs
            for (int i = 0; i < idrefs.getLength(); i++) {
                String idref = idrefs.item(i).getNodeValue();
                Element elem = ((UMLElement) stateMachine).xmi2uml(idref);

                if (elem instanceof Event) {
                    events.add((Event) elem);
                }
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "AbstractVelocityModel.getOwnedEvents(): ");
        }

        return events;
    }


    //////////////////////////////////
    // Miscellaneous utility methods
    //////////////////////////////////

    private SimpleDateFormat dateFormatter = null;
    public String timestamp () {
        if (dateFormatter == null) {
            dateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        }
        return dateFormatter.format(Calendar.getInstance().getTime());
    }

    public String username () {
        return System.getProperty("user.name");
    }

    public int fromHexString (String hexStr) {
        return Integer.parseInt(hexStr, 16);
    }

    public String toHexString (int value, int numDigits) {
        return Util.toHexString(value, numDigits);
    }

    public String[] splitByPkgs (String ns) {
        if (Util.isVarArgsEmpty(ns)) {
            return new String[0];
        } else {
            return ns.split(Util.PACKAGE_SEP);
        }
    }

    public String joinWithPrefixes (String[] prefixes, String name, String sep) {
    	return Util.joinWithPrefixes(prefixes, name, sep);
    }

}
