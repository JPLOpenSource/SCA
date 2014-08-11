/**
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

import gov.nasa.jpl.statechart.Entry;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.autocode.cm.UMLToCMapper;
import gov.nasa.jpl.statechart.model.PrefixOrderedWalker;
import gov.nasa.jpl.statechart.model.visitor.RegionVisitor;
import gov.nasa.jpl.statechart.model.visitor.SignalVisitor;
import gov.nasa.jpl.statechart.model.visitor.StateNameVisitor;
import gov.nasa.jpl.statechart.model.visitor.StateTimeEventVisitor;
import gov.nasa.jpl.statechart.model.visitor.StateVisitor;
import gov.nasa.jpl.statechart.model.visitor.SubmachineVisitor;
import gov.nasa.jpl.statechart.model.visitor.TransitionSignalEventVisitor;
import gov.nasa.jpl.statechart.model.visitor.TransitionTimeEventVisitor;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Namespace;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.PseudostateKind;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.Signal;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.TimeEvent;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This is a wrapper class that pulls information from the UML Model and
 * packages it up for use by the Velocity templates.
 * 
 * The fundamental aspect of this class is that every reference to a state or
 * region is packed into a Map.Entry class with its full path name. Since the
 * same UML object may be addressable via multiple paths, it is *critical* that
 * the path always be used.
 * 
 * Every method with a public signature should only take Map.Entry arguments and
 * return Map.Entry values.
 */
public class VelocityModel extends AbstractVelocityModel {

    // State machine that is being processed
    protected final StateMachine stateMachine;

    // Keep a cache of all the regions and states ad their expanded names
    // (which are different from the UML Qualified names). This is just
    // done for computational easy and simple referencing later.
    protected final Map<String, NamedElement> qname2ns;
    protected final Map<NamedElement, List<String>> ns2qname;

    /**
     * Main constructor to initialize the Velocity Model.
     * Mappings of qualified names to namespaces is created at the start.
     * 
     * @param myStateMachine  the state machine with which to construct this model
     */
    public VelocityModel (StateMachine myStateMachine) {
        super();

        // Initialize the variables
        stateMachine = myStateMachine;
        qname2ns = getAllStates(myStateMachine);
        ns2qname = Util.newMap();

        // Create a mapping from the elements to their expanded names
        // that will be used in the autocoder output
        //
        // The reverse mapping may be one-to-many since the same state can be
        // reached by numerous paths if it is in a submachine
        for (Map.Entry<String, NamedElement> entry : qname2ns.entrySet()) {
            if (!ns2qname.containsKey(entry.getValue())) {
                ns2qname.put(entry.getValue(), new ArrayList<String>());
            }
            ns2qname.get(entry.getValue()).add(entry.getKey());
        }
    }

    /**
     * Given a state and an enclosing namespace, return a map of all the
     * states paths that can be legally expanded from the root
     */
    public <T extends NamedElement> Map<String, T> getExpandedNamespaces (String prefix, T target) {
        Map<String, T> map = Util.newMap();

        for (String name : ns2qname.get(target)) {
            if (name.startsWith(prefix)) {
                map.put(name, target);
            }
        }

        return map;
    }

    /**
     * Given a state and an enclosing namespace, return the qualified
     * name/namespace pair that is the nearest contained match.  This
     * is just the name that matches the longest prefix
     */
    public <T extends NamedElement> Entry<String, T> getNamespace (String qname, T target) {
        // The correct target qualified name is the one with the longest matching prefix
        Iterator<String> iter = ns2qname.get(target).iterator();
        String longestMatch = iter.next();

        while (iter.hasNext()) {
            String prefix = iter.next();
            int i = 0;
            for (; i < prefix.length() && i < qname.length(); i++)
                if (prefix.charAt(i) != qname.charAt(i))
                    break;

            if (i > longestMatch.length())
                longestMatch = prefix;
        }

        if (Util.isDebugLevel()) {
            Util.debug("~ namespace '" + longestMatch + "' acquired for " + target.getQualifiedName());
        }
        return new Entry<String, T>(longestMatch, target);
    }

    /**
     * Return the current state machine context.
     */
    public Entry<String, StateMachine> getStatemachine () {
        List<String> qnames = ns2qname.get(stateMachine);

        if (qnames.size() != 1)
            throw new RuntimeException("Statemachine is multiply referenced");

        return new Entry<String, StateMachine>(qnames.get(0), stateMachine);
    }

    /**
     * @see #getType(NamedElement)
     * @param entry  the statechart qname-element pair
     * @return a string representing the type of this element
     */
    public String getType (Map.Entry<String, ? extends NamedElement> entry) {
        return getType(entry.getValue());
    }

    /**
     * Return the set of all elements that are nested below the given element,
     * excluding self.
     */
    public Map<String, NamedElement> getOwnedElements (Map.Entry<String, ? extends NamedElement> entry) {
        Map<String, NamedElement> elements = Util.newMap();

        for (Map.Entry<String, NamedElement> element : qname2ns.entrySet()) {
            String qname = element.getKey();
            NamedElement ne = element.getValue();
            if (qname.startsWith(entry.getKey()) && !ne.equals(entry.getValue())) {
                elements.put(qname, ne);
            }
        }

        return elements;
    }

    /**
     * Returns the initial state for a region, state or statemachine.  The
     * states are always contained in region.  If we are given a
     * statemachine or state, we look for a region.  If there is more than
     * one region, throw an exception.
     */
    public Entry<String, Pseudostate> getInitialState (Map.Entry<String, ? extends Namespace> entry) {
        Namespace ns = entry.getValue();
        String name = entry.getKey();

        // There must be a region, but we'll let StateMachine and State pass, too.
        if (ns instanceof StateMachine) {
            Collection<Region> regions = ((StateMachine) ns).getRegion();

            if (regions.size() != 1)
                return null;

            ns = regions.iterator().next();
            name = getNamespace(name, ns).getKey();
        }

        if (ns instanceof State) {
            Collection<Region> regions = ((State) ns).getRegion();

            if (regions.size() != 1)
                return null;

            ns = regions.iterator().next();
            name = getNamespace(name, ns).getKey();
        }

        // Now, we should have a region, if not return null
        if (ns instanceof Region) {
            for (Vertex subvertex : ((Region) ns).getSubvertex()) {
                if (!(subvertex instanceof Pseudostate))
                    continue;

                Pseudostate ps = (Pseudostate) subvertex;

                if (!ps.getKind().equals(PseudostateKind.initial))
                    continue;

                return new Entry<String, Pseudostate>(getNamespace(name, ps));
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
    public Transition getInitialTransition (Map.Entry<String,Pseudostate> pseudo) {
        if (pseudo == null || pseudo.getValue() == null) {
            Util.error("Error! Supplied initial state was NULL!");
            // TODO should we throw exception, print stack trace, or something??
            return null;
        }

        List<Transition> transList = getOutgoingTransitions(pseudo);
        if (transList.size() == 0) {  // error should already be reported
            return null;
        }
        if (transList.size() > 1) {
            Util.error("Initial state " + pseudo.getValue().getQualifiedName()
                    + " has more than one outgoing transition! I'm picking the first transition, but you might not get the right behavior.");
        }

        return getOutgoingTransitions(pseudo).get(0);
    }

    /**
     * Given a source state and a transition, returns the qname-state entry pair
     * of the target.
     */
    public Entry<String, NamedElement> getTarget (Map.Entry<String, Vertex> entry, Transition transition) {
        Vertex source = entry.getValue();
        String qname = entry.getKey();
        Vertex target = transition.getTarget();

        if (!source.equals(transition.getSource()))
            throw new RuntimeException(
                    "Passed source does not match transition source");

        return new Entry<String, NamedElement>(getNamespace(qname, target));
    }

    /**
     * Return a list of the outgoing transitions from a state.  This method is
     * provided because it is difficult to get the individual elements of a
     * raw collection from within the template.
     */
    public List<Transition> getOutgoingTransitions (Map.Entry<String, ? extends Vertex> entry) {
        return new ArrayList<Transition>(entry.getValue().getOutgoing());
    }

    /**
     * Retrieves the regions that contain deep history states.
     */
    public Map<String,Region> getHistoryContainers () {
        return getHistoryContainers(getStatemachine());
    }

    public Map<String,Region> getHistoryContainers (Map.Entry<String, ? extends Namespace> region) {
        Map<String,Region> containers = Util.newMap();

        // Consider all regions, for each, check to see if it contains a history state
        for (Map.Entry<String,Region> subregion : getRegions(region, false).entrySet()) {
            Region r = subregion.getValue();

            if (r.containsHistoryState()) {  // region with history!
                containers.put(subregion.getKey(), r);
            }
        }

        return containers;
    }

    /**
     * Returns the parent state, or StateMachine, of this namespace element,
     * or null if top of model reached.
     * @return the qname-element entry for the parent State.
     */
    public Entry<String, NamedElement> getParentState (Map.Entry<String, ? extends Namespace> entry) {
        NamedElement ns = null;
        String qname = UMLToCMapper.remove(entry.getKey());
        // loop invariant:  terminate when ns != null OR qname has zero length
        while (ns == null && qname.length() > 0) {
            ns = qname2ns.get(qname);
            if (ns instanceof State
                    || ns instanceof StateMachine) {
                // do nothing, we're golden
            } else {
                // null out ns to keep looping
                ns = null;
                // strip out last segment of qualified name
                qname = UMLToCMapper.remove(qname);
            }
        }
        return new Entry<String,NamedElement>(qname, ns);
    }

    /**
     * Returns the parent state skipping states with orthogonal regions,
     * or the StateMachine, of this namespace element, or null if top of model
     * reached.
     * @return the qname-element entry for the parent State, but skipping up if orthogonal.
     */
    public Entry<String, NamedElement> getParentStateSkipOrthogonoal (Map.Entry<String, ? extends Namespace> entry) {
        NamedElement ns = null;
        String qname = UMLToCMapper.remove(entry.getKey());
        // loop invariant:  terminate when ns != null OR qname has zero length
        while (ns == null && qname.length() > 0) {
            ns = qname2ns.get(qname);
            if (ns instanceof State) {
                if (!((State) ns).isOrthogonal()) {
                    break;
                }
            } else if (ns instanceof StateMachine) {
                break;
            }
            // null out ns to keep looping, and strip last segment of qname
            ns = null;
            qname = UMLToCMapper.remove(qname);
        }
        return new Entry<String,NamedElement>(qname, ns);
    }

    /**
     * Get all the regions
     */
    public Map<String,Region> getRegions (Map.Entry<String, ? extends Namespace> entry, boolean descend) {
        Map<String,Region> regions = Util.newMap();

        Namespace ns = entry.getValue();
        String qname = entry.getKey();

        for (Region region : PrefixOrderedWalker.traverse(ns, new RegionVisitor(descend))) {
            regions.putAll(getExpandedNamespaces(qname, region));
        }

        return regions;
    }

    /**
     * Get the enclosing state machine or region and return it with its proper name
     */
    public Entry<String, Region> getEnclosingOrthogonalRegion (Map.Entry<String, State> entry) {
        NamedElement ns = null;

        // Back up the path until we find a region in an Orthogonal state, or
        // run out of nesting
        String qname = entry.getKey();

        for (;;) {
            // Move up
            qname = UMLToCMapper.remove(qname);

            // If we're done, exit
            if (qname.length() == 0 || !qname2ns.containsKey(qname))
                break;

            // Otherwise, check the UML class. If it is a Region and
            // it's state is an orthogonal state, use it. Make sure
            // that we actually get a state because the immediate
            // region enclosed by the StateMachine will return null
            ns = qname2ns.get(qname);

            if (ns instanceof Region) {
                State state = ((Region) ns).getState();
                if (state != null && state.isOrthogonal())
                    break;
            }
        }

        if (qname.length() == 0 || !qname2ns.containsKey(qname))
            return null;

        return new Entry<String, Region>(qname, (Region) ns);
    }

    /**
     * Returns a list of all the states and regions that need to have
     * code generated for them.  We discard any state that has
     * an empty name.
     *
     * The Map will contain unique paths, but it may have duplicate UML
     * Elements since the same state may be referenced through multiple
     * paths.  
     */
    public Map<String,NamedElement> getAllStates (StateMachine stateMachine) {
        return PrefixOrderedWalker.traverse(stateMachine, new StateNameVisitor(stateMachine));
    }

    /**
     * Get all the states that are descendants of supplied Namespace entry.
     */
    public Map<String,State> getStates (Map.Entry<String, ? extends Namespace> entry, boolean descend) {
        Map<String,State> states = Util.newMap();

        Namespace ns = entry.getValue();
        String qname = entry.getKey();

        for (State state : PrefixOrderedWalker.traverse(ns, new StateVisitor(descend))) {
            states.putAll(getExpandedNamespaces(qname, state));
        }

        return states;
    }

    /**
     * Get immediate children states from all subregions
     * @param entry  the qname-state entry of the state whose children states to obtain
     * @return map of qualified names to children states
     */
    public Map<String,State> getChildrenStates (Map.Entry<String,State> entry) {
        Map<String,State> kidstates = Util.newMap();

        State state = entry.getValue();
        String qname = entry.getKey();

        for (Region r : state.getRegion()) {
            for (State kid : Util.filter(r.getSubvertex(), State.class)) {
                kidstates.putAll(getExpandedNamespaces(qname, kid));
            }
        }

        return kidstates;
    }

    /**
     * Get all the submachines of supplied Namespace
     */
    public Map<String,StateMachine> getSubmachines (Map.Entry<String, ? extends Namespace> entry, boolean descend) {
        Map<String,StateMachine> submachs = Util.newMap();

        for (StateMachine sm : PrefixOrderedWalker.traverse(entry.getValue(), new SubmachineVisitor(descend))) {
            submachs.putAll(getExpandedNamespaces(entry.getKey(), sm));
        }

        return submachs;
    }

    /**
     * Returns all the supplied states grouped into enclosing regions.
     * @return
     */
    public Map<Entry<String,Region>,SortedMap<String,State>> getStatesByEnclosingRegion (Map<String,State> states) {
        Map<Entry<String,Region>,SortedMap<String,State>> mapByRegion = Util.newMap();

        // now sort states into buckets by region, calling getEnclosingOrthogonalRegion
        for (Map.Entry<String,State> entry : states.entrySet()) {
            // region may be null, which is treated as top-level
            Entry<String,Region> regionEntry = getEnclosingOrthogonalRegion(entry);
            if (!mapByRegion.containsKey(regionEntry)) {
                mapByRegion.put(regionEntry, new TreeMap<String,State>());
            }
            Map<String,State> stateMap = mapByRegion.get(regionEntry);
            stateMap.put(entry.getKey(), entry.getValue());
        }

        return mapByRegion;
    }

    /**
     * Get the set of Time Events that trigger transition from a particular state.
     */
    public Collection<TimeEvent> getTimeEvents (Map.Entry<String, State> entry) {
        List<TimeEvent> events = new ArrayList<TimeEvent>();
        State state = entry.getValue();
        // For each transition out of the state, examine its triggers
        // and keep the time event triggers
        for (Transition transition : state.getOutgoing()) {
            events.addAll(transition.getTimeEvents());
        }
        return events;
    }

    /**
     * Get a set of states that have transitions triggered by a TimeEvent
     */
    public Map<String, ? extends Namespace> getStatesWithTimeEventTriggers (Map.Entry<String, ? extends Namespace> entry, boolean descend) {
        Map<String,State> states = Util.newMap();

        for (State state : PrefixOrderedWalker.traverse(entry.getValue(), new StateTimeEventVisitor(descend))) {
            states.putAll(getExpandedNamespaces(entry.getKey(), state));
        }

        return states;
    }

    /**
     * Get the set of Transitions from a particular state whose trigger is
     * a signal event.
     */
    public Collection<Transition> getTransitionsWithSignalEvent (Map.Entry<String,State> entry) {
        List<Transition> trans = Util.newList();
        // For each transition out of the state, examine its triggers and
        // keep the transition if the trigger is a signal event
        for (Transition transition : entry.getValue().getOutgoing()) {
            if (transition.getSignalEvents().size() > 0) {
                trans.add(transition);
            }
        }
        return trans;
    }

    /**
     * Get the set of Transitions from a particular namespace whose trigger is
     * a signal event.
     */
    public Collection<Transition> getTransitionsWithSignalEvent (Map.Entry<String, ? extends Namespace> entry, boolean descend) {
        return PrefixOrderedWalker.traverse(entry.getValue(), new TransitionSignalEventVisitor(descend));
    }

    /**
     * Get the set of Transitions from a particular state whose trigger is
     * a time event.
     */
    public Collection<Transition> getTransitionsWithTimeEvent (Map.Entry<String,State> entry) {
        List<Transition> trans = Util.newList();
        // For each transition out of the state, examine its triggers and
        // keep the transition if the trigger is a time event
        for (Transition transition : entry.getValue().getOutgoing()) {
            if (transition.getTimeEvents().size() > 0) {
                trans.add(transition);
            }
        }
        return trans;
    }

    /**
     * Get the set of Transitions from a particular namespace whose trigger is
     * a time event.
     */
    public Collection<Transition> getTransitionsWithTimeEvent (Map.Entry<String, ? extends Namespace> entry, boolean descend) {
        return PrefixOrderedWalker.traverse(entry.getValue(), new TransitionTimeEventVisitor(descend));
    }

    /**
     * Returns the set of transitions from a Junction pseudostate, ordered so
     * that the last one is the "else", the branch without a guard
     * @param entry
     * @return
     */
    public List<Transition> getJunctionTransitions (Map.Entry<String,Pseudostate> entry) {
        List<Transition> transList = Util.newList();

        Transition noGuard = null;
        for (Transition trans : entry.getValue().getOutgoing()) {
            if (trans.getGuard() != null) {
                transList.add(trans);
            } else {
                noGuard = trans;  // save it for later
            }
        }
        // now add the noGuard transition, null as placeholder if none
        transList.add(noGuard);

        return transList;
    }

    /**
     * Get all the Signals that are ever acted upon.
     */
    public Collection<Signal> getSignals (Map.Entry<String, ? extends Namespace> entry, boolean descend) {
        return PrefixOrderedWalker.traverse(entry.getValue(), new SignalVisitor(descend));
    }

    /**
     * This method retrieves all the orthogonal regions that are contained
     * within this state machine diagram.  Orthogonal regions operate
     * as independent threads
     */
    public Map<String, Region> getLocalOrthogonalRegions () {
        return getLocalOrthogonalRegions(getStatemachine());
    }

    /**
     * Retrieve all the Orthogonal regions that are nested within a given named element.
     */
    public Map<String, Region> getLocalOrthogonalRegions (Map.Entry<String, ? extends Namespace> element) {
        Map<String, Region> regions = Util.newMap();

        /* Consider only the states that are nested below this element;
         * will include this element if it is a state
         */
        Map<String, State> states = getStates(element, false);

        // For each state, check to see if it is an Orthogonal state. If so,
        // ensure that it is contained within the current state machine and
        // only then get its Orthogonal Regions.
        for (Map.Entry<String, State> entry : states.entrySet()) {
            State state = entry.getValue();

            // If the state is not an Orthogonal state, skip it
            if (!state.isOrthogonal())
                continue;

            // If the state is not owned by this state machine, ignore
            if (!state.getContainingStatemachine().equals(stateMachine))
                continue;

            // Retrieve each orthogonal region
            for (Region region : state.getRegion()) {
                Map.Entry<String, Region> ns = getNamespace(entry.getKey(), region);
                regions.put(ns.getKey(), region);
            }
        }

        return regions;
    }

    public Map<String, Region> getExternalOrthogonalRegions () {
        return Util.newMap();
    }

}
