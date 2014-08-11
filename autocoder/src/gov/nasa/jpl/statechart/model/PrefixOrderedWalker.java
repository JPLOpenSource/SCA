/**
 * Created Sep 29, 2009, for housing traverse methods from VelocityModel.
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
package gov.nasa.jpl.statechart.model;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.model.visitor.Visitor;
import gov.nasa.jpl.statechart.uml.FinalState;
import gov.nasa.jpl.statechart.uml.Model;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.PseudostateKind;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.Vertex;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class of methods for performing a prefix traversal of UML Model.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>, moved existing methods by Eddie from VelocityModel
 *
 */
public abstract class PrefixOrderedWalker {

    /**
     * These are a series of methods and classes to actually perform the 
     * traversals in a generic manner.
     */
    public static <T extends Visitor> T traverse (NamedElement element, T v) {
        if (element instanceof Model) {
            return traverse((Model) element, v);
        }
        if (element instanceof StateMachine) {
            return traverse((StateMachine) element, v);
        }
        if (element instanceof Region) {
            return traverse((Region) element, v);
        }
        if (element instanceof FinalState) {
            return traverse((FinalState) element, v);
        }
        if (element instanceof State) {
            return traverse((State) element, v);
        }
        if (element instanceof Pseudostate) {
            return traverse((Pseudostate) element, v);
        }
        if (element instanceof Vertex && v.followOutTransitions()) {
            /* Special handling to follow outgoing Transitions to targets, used
             * for Visitors that walk the model from Vertices to peers, rather
             * than walking up or down state levels of the machine, e.g.,
             * TransitionPathVisitor.
             * 
             * We following outgoing transitions last, traversing each target.
             * Since transition hop is a peer-level walk, no moveDown or moveUp
             * calls are warranted.  But when a target Vertex is traversed,
             * it is considered visited.
             */
            for (Transition t : ((Vertex) element).getOutgoing()) {
                traverse(t.getTarget(), v);
            }
        }
        return v;
    }

    public static <T extends Visitor> T traverse (Model model, T v) {
        for (StateMachine sm : model.getStateMachines()) {
            v.moveDown(model, sm);
            traverse(sm, v);
            v.moveUp(sm, model);
        }
    
        return v;
    }

    private static <T extends Visitor> T traverse (StateMachine stateMachine, T v) {
        if (v.isVisiting(stateMachine)) return v;

        v.visit(stateMachine);

        for (Region region : stateMachine.getRegion()) {
            v.moveDown(stateMachine, region);
            traverse(region, v);
            v.moveUp(region, stateMachine);
        }

        // visit StateMachine connection points
        for (Pseudostate pseudo : stateMachine.getConnectionPoint()) {
            v.moveDown(stateMachine, pseudo);
            traverse(pseudo, v);
            v.moveUp(pseudo, stateMachine);
        }

        return v;
    }

    public static <T extends Visitor> T traverse (Region region, T v) {
        if (v.isVisiting(region)
                || !v.expandOrthogonalRegions()) return v;

        // Visit this region
        v.visit(region);

        // Descend into all child subvertices that are States or Pseudostates
        Collection<Vertex> subvertices = region.getSubvertex();
        List<State> substates = new LinkedList<State>(Util.filter(subvertices, State.class));

        for (Pseudostate pseudo : Util.filter(subvertices, Pseudostate.class)) {
            v.moveDown(region, pseudo);
            traverse(pseudo, v);
            v.moveUp(pseudo, region);

            if (pseudo.getKind() == PseudostateKind.initial) {
                // Make sure target of initial Pseudostate gets traversed before other states
                for (Transition t : pseudo.getOutgoing()) {
                    if (t.getTarget() != null && t.getTarget() instanceof State) {
                        State target = (State) t.getTarget();
                        substates.remove(target);
                        substates.add(0, target);
                    }
                }
            }
        }

        for (State state : substates) {
            v.moveDown(region, state);
            if (state instanceof FinalState) {
                traverse((FinalState) state, v);
            } else {
                traverse(state, v);
            }
            v.moveUp(state, region);
        }

        return v;
    }

    private static <T extends Visitor> T traverse (State state, T v) {
        if (v.isVisiting(state)
                || !v.expandOrthogonalRegions()) return v;

        // Visit this state
        v.visit(state);

        // visit the connection points, but do NOT visit ConnectionPointRefs,
        // as that causes submachine's entry/exitPoints to show up on main SM
        for (Pseudostate pseudo : state.getConnectionPoint()) {
            v.moveDown(state, pseudo);
            traverse(pseudo, v);
            v.moveUp(pseudo, state);
        }

        // Recursively search all Regions of the Composite and Orthogonal states
        for (Region subregion : state.getRegion()) {
            v.moveDown(state, subregion);
            traverse(subregion, v);
            v.moveUp(subregion, state);
        }
    
        // If the state is a submachine state and the visitor wants to look
        // at its states, go ahead.
        if (state.isSubmachineState() && v.expandSubmachines()) {
            StateMachine stateMachine = state.getSubmachine();
            if (stateMachine != null) {  // UMLValidator will catch null SubMs
                v.moveDown(state, stateMachine);
                traverse(stateMachine, v);
                v.moveUp(stateMachine, state);
            }
        }

        return v;
    }

    private static <T extends Visitor> T traverse (FinalState state, T v) {
        if (v.isVisiting(state)
                || !v.expandOrthogonalRegions()) return v;

        // Visit final state
        v.visit(state);

        // FinalStates are leaves on the tree, so we're done
        return v;
    }

    private static <T extends Visitor> T traverse (Pseudostate state, T v) {
        if (v.isVisiting(state)
                || !v.expandOrthogonalRegions()) return v;

        // Visit pseudostate
        v.visit(state);
    
        // Pseudostates are effectively leaves on the tree, so we're done
        return v;
    }

}
