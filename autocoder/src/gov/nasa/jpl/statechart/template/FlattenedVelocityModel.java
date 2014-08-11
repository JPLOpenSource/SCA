/**
 * Created Dec 8, 2009.
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
package gov.nasa.jpl.statechart.template;

import gov.nasa.jpl.statechart.Pair;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.model.IDesiredEvent;
import gov.nasa.jpl.statechart.model.IDesiredEvent.EventTransitionPair;
import gov.nasa.jpl.statechart.model.IDesiredEvent.QueryPolicy;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.model.PrefixOrderedWalker;
import gov.nasa.jpl.statechart.model.visitor.AbstractVisitor.OrthoRegion;
import gov.nasa.jpl.statechart.model.visitor.LeafStateVisitor;
import gov.nasa.jpl.statechart.model.visitor.SourceTimeoutVisitor;
import gov.nasa.jpl.statechart.model.visitor.TransitionSignalEventVisitor;
import gov.nasa.jpl.statechart.uml.Behavior;
import gov.nasa.jpl.statechart.uml.Event;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Namespace;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.SignalEvent;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.TimeEvent;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.Vertex;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;


/**
 * This class extends the global-scope velocity query class with methods that
 * flatten the model hierarchy, motivated by the need to autocode Promela.
 * <p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class FlattenedVelocityModel extends GlobalVelocityModel {

    // Caches ancester-chain keyed by leaf state.
    private Map<State,List<State>> leafAncestorChain = Util.newMap();

    /**
     * Main constructor to initialize the Velocity Model.
     * 
     * @param myModelGrp  the UML Model group from which to autocode
     * @param myStateMachine  the state machine with which to construct this model.
     */
    public FlattenedVelocityModel (ModelGroup myModelGrp, StateMachine myStateMachine) {
        super(myModelGrp);

        // Initialize the variables
        setStateMachine(myStateMachine);
    }

    /**
     * Returns a collection of leaf UML States, traversing _into_ submachines,
     * to allow constructing a flattened machine.
     * 
     * @param ns  UML Namespace under which to traverse.
     * @return  Collection of states.
     */
    public Collection<State> getFlattenedStates (Namespace ns) {
        return PrefixOrderedWalker.traverse(ns, new LeafStateVisitor(true));
    }

    /**
     * Returns a collection of leaf UML States, not below orthogonal regions.
     * 
     * @param ns  UML Namespace under which to traverse.
     * @return  Collection of states.
     */
    public Collection<State> getLeafStatesAboveOrtho (Namespace ns) {
        return PrefixOrderedWalker.traverse(ns, new LeafStateVisitor(false, OrthoRegion.STOP_AT_ORTHO));
    }

    public boolean isOnInitPath (State state) {
        boolean yesInitPath = false;

        State parentState = state.getParentState();
        if (parentState != null) {
            // check if parent's initial transition comes to 'cur'
            for (Region r : parentState.getRegion()) {  // there should be only 1!
                Pseudostate init = getInitialState(r);
                if (init != null) {
                    // search incoming transitions from state
                    Collection<Transition> searchTrans = Util.newList(state.getIncoming());
                    while (!searchTrans.isEmpty()) {
                        for (Transition inTran : Util.newList(searchTrans)) {
                            searchTrans.remove(inTran);
                            Vertex source = inTran.getSource();
                            if (source instanceof Pseudostate) {
                                if (init.equals(source)) {  // YAY!
                                    yesInitPath = true;
                                    // stop the search
                                    searchTrans.clear();
                                } else {
                                    searchTrans.addAll(source.getIncoming());
                                }                                        
                                break;
                            }
                        }
                    }  // parent no longer in initial trans chain
                }
            }
        }

        return yesInitPath;
    }

    /**
     * Given a leaf state, returns the chain of its ancestor states,
     * ordered outside-in, based on whether there is an initial transition
     * from the outermost state within container.
     * <p>
     * Use of {@link State} type rather than Vertex is intentional.
     * </p>
     * @param state  Leaf UML State whose entry chain to build.
     * @param container UML Namespace within which to build chain.
     * @return  List of States in order of entry within <code>container</code>
     *      from outermost to <code>state</code>.
     */
    public List<State> getInitBasedAncestorChain (State state, Namespace container) {
        List<State> chain = leafAncestorChain.get(state);
        if (chain != null) return Collections.emptyList();  // don't return any

        // Otherwise, construct the chain.
        chain = new LinkedList<State>();
        leafAncestorChain.put(state, chain);  // cache the chain
        // check for leaf state (simple, or orthogonal)
        if (state.isSubmachineState()) return chain;

        // add this state to chain
        chain.add(state);

        // traverse up parent until no more with incoming initial transition
        State cur = state;
        State parentState;
        NamedElement parent;
        OUTER: while (cur != null) {
            // get parent element
            parent = cur.getParent();
            if (parent == container) {  // reached container!
                cur = null;
            } else {
                parentState = cur.getParentState();
                if (parentState != null) {
                    // check if parent's initial transition comes to 'cur'
                    for (Region r : parentState.getRegion()) {  // there should be only 1!
                        Pseudostate init = getInitialState(r);
                        if (init != null) {
                            Transition t = getInitialTransition(init);
                            Vertex target = getTarget(init, t);
                            if (cur.equals(target)) {  // YAY!
                                // add parent to front of chain
                                chain.add(0, parentState);
                                break;
                            } else {  // parent no longer in initial trans chain
                                // done building the chain
                                break OUTER;
                            }
                        }
                    }
                }
                cur = parentState;
            }
        }
        return chain;
    }

    /**
     * Given any state, returns the chain of its ancestor states,
     * ordered outside-in.
     * <p>
     * Use of {@link State} type rather than Vertex for List is intentional.
     * </p>
     * @param v  UML Vertex for which to build ancestor chain.
     * @return  List of States in order of containment from outermost to <code>state</code>.
     */
    public List<State> getAncestorChain (Vertex v) {
        return getAncestorChain(v, null);
    }

    /**
     * Given any state, returns the chain of its ancestor states,
     * ordered outside-in, but only within specified container.
     * <p>
     * Use of {@link State} type rather than Vertex for List is intentional.
     * </p>
     * @param v  UML Vertex for which to build ancestor chain.
     * @param container UML Namespace within which to build chain.
     * @return  List of States in order of containment within <code>container</code>
     *      from outermost to <code>state</code>.
     */
    public List<State> getAncestorChain (Vertex v, Namespace container) {
        List<State> chain = new LinkedList<State>();
        if (v instanceof State) {
            chain.add((State) v);
        }
        Vertex cur = v;
        State parentState;
        NamedElement parent;
        while (cur != null) {
            parent = cur.getParent();
            if (parent == container) {  // reached container!
                cur = null;
            } else {
                parentState = cur.getParentState();
                if (parentState != null) {
                    chain.add(0, parentState);
                }
                cur = parentState;
            }
        }
        return chain;
    }

    public <T> List<T> reverse (List<T> list) {
        Collections.reverse(list);
        return list;
    }

    /**
     * Computes the sequence of exit/entry Activity along the path of transition
     * from the <code>source</code> State to the <code>target</code> State.
     * The algorithm is simply as follows:<ol>
     * <li> Find the least-common-ancestor (LCA)
     * <li> Collect the exit actions from the source outward to the LCA
     * <li> Collect the entry actions from the LCA into the target state
     * </ol>
     * @param source  source UML Vertex of transition
     * @param target  target UML Vertex of transition
     * @return  Pair of Lists, one of exit, and one of entry Behaviors of States
     */
    public Pair<List<Behavior>,List<Behavior>> computeTransitionPathActivity (Vertex source, Vertex target) {
        List<Behavior> exitActions = Util.newList();
        List<Behavior> entryActions = Util.newList();
        Pair<List<Behavior>,List<Behavior>> actionPair =
            new Pair<List<Behavior>,List<Behavior>>(exitActions, entryActions);

        Util.debug("Computing transition path activities between source '"
                    + source.getQualifiedName() + "' and target '"
                    + target.getQualifiedName() + "':");
        /* Find the LCA:
         * a.) Determine the depth of source (sD) and target (tD), and pick the
         *     deepest index 'common' less than or equal to both.
         * b.) Starting from 'common', compare that ancestor in the ancestor
         *     chain of source and target, respectively, until one is found that
         *     is equivalent; that is the LCA.
         */
        List<State> sChain = getAncestorChain(source);
        int sD = sChain.size();
        List<State> tChain = getAncestorChain(target);
        int tD = tChain.size();
        int common = Math.min(sD, tD);  // deepest common depth
        int lca = -1;  // -1 represents tree root of State Machine
        for (int d=common-1; d >= 0; --d) {  // array index is one off from depth
            if (sChain.get(d).equals(tChain.get(d))) {  // found lca!
                lca = d;
                break;
            }
        }

        if (lca > -1 && sD-1 == lca
                && !(source instanceof Pseudostate)) {
            // need to include exit of source state; Pseudos are one-level down!
            exitActions.add(sChain.get(lca).getExit());
        }
        // from source, go up its ancestor chain to (but excl.) lca and collect exits
        for (int d=sD-1; d > lca; --d) {
            exitActions.add(sChain.get(d).getExit());
        }

        // from lca, go down target's ancestor chain to (but excl.) target and collect entries
        for (int d=lca+1; d < tD-1; ++d) {
            entryActions.add(tChain.get(d).getEntry());
        }
        // [SWC 2011.07.26] Bugfix: include target state ONLY if a Pseudostate
        if (tD-1 >= 0 && target instanceof Pseudostate) {
            Pseudostate pseudo = (Pseudostate) target;
            switch (pseudo.getKind()) {
            case junction:
            case choice:
            case deepHistory:
            case shallowHistory:
            case terminate:
                entryActions.add(tChain.get(tD-1).getEntry());
                break;
            }
        }

        if (Util.isDebugLevel()) {
            Util.debug("  * Common depth: " + common + "; LCA found: " + lca);
            Util.debug("  * Source ancestor chain ["
                        + sD + "] '" + source.getQualifiedName() + "':");
            for (State s : sChain) {
                Util.debug("    - " + s.getQualifiedName());
            }
            Util.debug("  *>>> Exit Actions, in execution order:");
            for (Behavior beh : exitActions) {
                if (beh == null) {
                    Util.debug("    - null");
                } else {
                    Util.debug("    - " + Arrays.toString(beh.actionList().toArray()));
                }
            }
            Util.debug("  * Target ancestor chain ["
                    + tD + "] '" + target.getQualifiedName() + "':");
        for (State t : tChain) {
            Util.debug("    - " + t.getQualifiedName());
        }
            Util.debug("  *<<< Entry Actions, in execution order:");
            for (Behavior beh : entryActions) {
                if (beh == null) {
                    Util.debug("    - null");
                } else {
                    Util.debug("    - " + Arrays.toString(beh.actionList().toArray()));
                }
            }
        }

        return actionPair; 
    }

    /**
     * Searches and returns a collection of all the State Machines that care
     * about the given Signal Event, by name.
     * 
     * @param evName  name of {@link SignalEvent} to query.
     * @return  Collection of {@link StateMachine}s that care about the event.
     */
    public Collection<StateMachine> getMachinesForEvent (String evName) {
        List<StateMachine> machines = Util.newList();

        // search all state machines for events they care about
        for (StateMachine machine : getStateMachines()) {
            // look through all the (outgoing) transitions
            // TODO Promela: do we worry about timer events??
            M_LOOP: for (Transition transition
                    : PrefixOrderedWalker.traverse(machine, new TransitionSignalEventVisitor(true))) {
                for (Event checkEvent : transition.getAllEvents()) {
                    if (evName.equals(checkEvent.getName())) {
                        // machine cares about event
                        machines.add(machine);
                        break M_LOOP;
                    }
                }
            }
        }

        return machines;
    }

    /**
     * From the given State, traverses its ancestor chain of states, collecting
     * a list of all Signal- and TimeEvents that are enabled from those states.
     * 
     * If the given State is an orthogonal state, invoke the desired-event
     * query to traverse within that state for all enabled Events.
     * 
     * @param s  UML {@link State} for which to return list of enabled Signal-
     *      and TimeEvents.
     * @return  Collection of {@link SignalEvent}s and {@link TimeEvent}s that
     *      are enabled when the given State is active,
     *      looking within orthogonal regions as well.
     */
    public Collection<Event> getEnabledEvents (State s, TargetLanguageMapper mapper) {
        SortedMap<String,Event> evs = Util.newSortedMap();

        for (State ancestor : getAncestorChain(s)) {
            // look at only the outgoing transitions of each state,
            // do not traverse into namespace, since we're using leaf states
            for (Transition t : ancestor.getOutgoing()) {
                if (! skipTimeEventTransition(ancestor, t)) {
                    for (Event ev : t.getAllEvents()) {
                        if (ev != null) {
                            evs.put(mapper.mapEventToLiteral(ev), ev);
                        }
                    }
                }
            }
        }

        if (s.isOrthogonal()) {   // look inside the orthogonal regions of s
            clearDesiredEvents(QueryPolicy.NULL_EV);
            IDesiredEvent desiredEventQuery = queryDesiredEvents(s, QueryPolicy.NULL_EV);
            for (EventTransitionPair pair : desiredEventQuery.getEventTransitions()) {
                if (! skipTimeEventTransition(s, pair.getTransition())) {
                    Event ev = pair.getEvent();
                    if (ev != null) {
                        // don't worry about duplicates; set will eliminate them.
                        evs.put(mapper.mapEventToLiteral(ev), ev);
                    }
                }
            }
        }

        return evs.values();
    }

    // Caches map of source vertex to effective timeout, by state machine.
    private Map<StateMachine,Map<Vertex,Double>> timeoutMaps = Util.newMap();

    public Map<Vertex,Double> getEffectiveTimeoutMap () {
        Map<Vertex,Double> timeoutMap = timeoutMaps.get(getStateMachine());

        if (timeoutMap == null) {
            SourceTimeoutVisitor v = new SourceTimeoutVisitor();
            PrefixOrderedWalker.traverse(getStateMachine(), v);
            timeoutMap = v.getMap();
            timeoutMaps.put(getStateMachine(), timeoutMap);
        }

        return timeoutMap;
    }

    public boolean skipTimeEventTransition (State state, Transition transition) {
        boolean skipTimeEvent = false;

        if (transition.getTimeEvents().size() > 0) {
            // compare effective timeout against ancestors'
            //   all the way up the chain to the TOP
            for (State ancester : getAncestorChain(state)) {
                Double inner = getEffectiveTimeoutMap().get(state);
                Double outer = getEffectiveTimeoutMap().get(ancester);
                if (inner != null && outer != null && inner > outer) {
                    // inner timeout > outer timeout, NULLify time transition!
                    skipTimeEvent = true;
                    break;
                }
            }
        }

        return skipTimeEvent;
    }

}
