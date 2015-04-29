/**
 * Created Apr 19, 2011.
 * <p>
 * Copyright 2009-2011, by the California Institute of Technology. ALL RIGHTS
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
import gov.nasa.jpl.statechart.template.FunctionCall;
import gov.nasa.jpl.statechart.uml.Behavior;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Namespace;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.PseudostateKind;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.Vertex;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Extracts all the Behaviors in the supplied namespace, without descending
 * into submachines, since those are taken care of by respective subclasses.
 * (True of Python, C++, and the new C.)
 * <p>
 * This visitor also caches, by namespace, maps of name-to-Behavior object for
 * all fired signals within that namespace; for states, it'll just be the
 * signals pertaining to that state and any choice and junction pseudostates
 * reachable from that state; for StateMachines, it'll be cumulative over subscopes. 
 * </p><p>
 * Copyright &copy; 2009-2011 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public class BehaviorVisitor extends AbstractVisitor<Behavior> {
    private static final long serialVersionUID = 1885605699319992550L;

    private boolean includeDo = false;
    // Map to cache set of fired Signals by the Namespace within which they fire
    private Map<Namespace,Map<String,Behavior>> cachedFiredSignalsByNamespace = null;
    // Stack of states representing hierarchy of states visited...
    private Stack<Namespace> nsStack = null;
    private Namespace curNamespace = null;


    public BehaviorVisitor (final OrthoRegion forceOrtho) {
        super(false, forceOrtho);

        cachedFiredSignalsByNamespace = Util.newMap();
        nsStack = new Stack<Namespace>();
    }

    public BehaviorVisitor (final OrthoRegion forceOrtho, boolean shouldIncludeDo) {
        this(forceOrtho);
        
        includeDo = shouldIncludeDo;
    }

    public Map<String,Behavior> getFiredSignalsByNamespace (Namespace ns) {
        if (!(ns instanceof StateMachine || ns instanceof State)) {
            Util.warn("BehaviorVisitor only caches signals based on State or StateMachine, but request is for namespace "
                    + ns.getQualifiedName() + " of type '"
                    + ns.getClass().getSimpleName() + "'");
        }
        return cachedFiredSignalsByNamespace.get(ns);
    }


    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.AbstractVisitor#moveUp(gov.nasa.jpl.statechart.uml.NamedElement, gov.nasa.jpl.statechart.uml.NamedElement)
     */
    @Override
    public void moveUp (NamedElement from, NamedElement to) {
        if (from == curNamespace) {  // leaving current namespace scope
            // propagate up all signals to owning StateMachine (by copying)
            Namespace smNs = null;
            if (to instanceof StateMachine) {
                smNs = (StateMachine)to;
            } else {  // traverse up parent until we reach a StateMachine
                NamedElement sm = to;
                while (! (sm == null || sm instanceof StateMachine)) {
                    sm = sm.getParent();
                }
                if (sm != null) {  // found parent SM
                    smNs = (StateMachine)sm;
                }
            }
            if (smNs != null) {
                // copy all signals we just got from this scope (or SM) to SM's
                Map<String,Behavior> firedSignals = getOrInitMapForNamespace(smNs);
                firedSignals.putAll(cachedFiredSignalsByNamespace.get(curNamespace));
            }
            // pop stack and set curNamespace to new top
            nsStack.pop();
            curNamespace = nsStack.peek();
        }
        super.moveUp(from, to);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.AbstractVisitor#visit(gov.nasa.jpl.statechart.uml.StateMachine)
     */
    @Override
    public void visit (StateMachine sm) {
        // we want to cache fired signals by an SM
        curNamespace = sm;
        nsStack.push(curNamespace);
        getOrInitMapForNamespace(curNamespace);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.VelocityModel.AbstractVisitor#visit(gov.nasa.jpl.statechart.uml.Pseudostate)
     */
    @Override
    public void visit (Pseudostate pseudostate) {
        if (pseudostate.getKind() == PseudostateKind.junction
                || pseudostate.getKind() == PseudostateKind.choice) {
            // special handling, need to find source state(s)
            Namespace saveNs = curNamespace;
            // for each source state, add any fired signal to the cache map
            List<State> sourceStates = findSourceState(pseudostate);
            if (Util.isDebugLevel()) {
                Util.debug("Found source state(s) for pseudostate '"
                        + pseudostate.getQualifiedName() + "': "
                        + Arrays.toString(sourceStates.toArray()));
            }
            for (State s : sourceStates) {
                curNamespace = s;
                getOrInitMapForNamespace(curNamespace);
                checkTransition(pseudostate);
            }
            // restore current namespace
            curNamespace = saveNs;
        } else {
            checkTransition(pseudostate);
        }
    }

    @Override
    public void visit (State state) {
        // we also want to cache fired signals by state
        curNamespace = state;
        nsStack.push(curNamespace);
        getOrInitMapForNamespace(curNamespace);

        // check state's actions and out-transition effects
        if (state.getEntry() != null) {
            processBehavior(state.getEntry());
        }
        if (state.getExit() != null) {
            processBehavior(state.getExit());
        }
        if (includeDo && state.getDo() != null) {
            processBehavior(state.getDo());
        }
        checkTransition(state);
    }

    private void checkTransition (Vertex v) {
        for (Transition transition : v.getOutgoing()) {
            // get effects only
            if (transition.getEffect() != null) {
                processBehavior(transition.getEffect());
            }
        }
    }

    private void processBehavior (Behavior beh) {
        // First, add to list
        addUnique(beh);

        // Next, find any cached signals and cache them
        Map<String,Behavior> firedSignals = getOrInitMapForNamespace(curNamespace);
        // get all action names that are not literals, nor functions, nor empty string
        for (String action : beh.actionList()) {
            if (! (Util.isQuotedString(action)
                    || FunctionCall.isFunctionCall(action)
                    || action.length() == 0)) {
                firedSignals.put(action, beh);
            }
        }
    }

    private List<State> findSourceState (Vertex v) {
        List<State> sourceStates = new LinkedList<State>();

        for (Transition t : v.getIncoming()) {
            if (t.getSource() != null) {
                if (t.getSource() instanceof State) {
                    // found a source state, this is the end of the recursion
                    sourceStates.add((State)t.getSource());
                } else {  // a vertex
                    // need to search source state(s) of this source vertex
                    sourceStates.addAll(findSourceState(t.getSource()));
                }
            }
        }

        return sourceStates;
    }

    private Map<String,Behavior> getOrInitMapForNamespace (Namespace ns) {
        Map<String,Behavior> firedSignals = cachedFiredSignalsByNamespace.get(ns);
        if (firedSignals == null) {
            // lazily create map for namespace
            firedSignals = Util.<String,Behavior>newSortedMap();
            cachedFiredSignalsByNamespace.put(ns, firedSignals);
        }
        return firedSignals;
    }

}
