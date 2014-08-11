/**
 * Relocated Sep 29, 2009.
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
package gov.nasa.jpl.statechart.model.visitor;

import gov.nasa.jpl.statechart.template.FunctionCall;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.Vertex;

/**
 * Extracts the call actions from all behaviors, but without descending into
 * orthogonal regions, since those are taken care of in their respective
 * subclasses.  (True of Python and C++, NOT used by C.)
 */
public class CallActionVisitor extends AbstractVisitor<FunctionCall> {
    private static final long serialVersionUID = -9211585654329214833L;

    // saves the index location of guards so that we can keep guards
    // grouped before the behaviors
    private int lastGuardIndex = 0;

    public CallActionVisitor (final boolean descend, final OrthoRegion forceOrtho) {
        super(descend, forceOrtho);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.VelocityModel.AbstractVisitor#visit(gov.nasa.jpl.statechart.uml.Pseudostate)
     */
    @Override
    public void visit (Pseudostate pseudostate) {
        checkTransition(pseudostate);
    }

    @Override
    public void visit (State state) {
        // ok to check state's actions and out-transition effects
        if (state.getEntry() != null) {
            for (String action : state.getEntry().actionList()) {
                if (FunctionCall.isFunctionCall(action)) {
                    addUnique(new FunctionCall(action));
                }
            }
        }
        if (state.getExit() != null) {
            for (String action : state.getExit().actionList()) {
                if (FunctionCall.isFunctionCall(action)) {
                    addUnique(new FunctionCall(action));
                }
            }
        }
        if (state.getDo() != null) {
            if (state.getDo().actionList().size() > 0) {
                // don't check for function call; it should always be a function
                addUnique(new FunctionCall(state.getDo().actionList().get(0)));
            }
        }
        checkTransition(state);
    }

    private void checkTransition (Vertex v) {
        for (Transition transition : v.getOutgoing()) {
            if (transition.getGuard() != null) {
                String exprStr = transition.getGuard().getSpecification().stringValue();
                if (FunctionCall.isFunctionCall(exprStr)) {
                    FunctionCall fc = new FunctionCall(exprStr, transition.getGuard());
                    // add to last known guard location, increment afterward!
                    if (! this.contains(fc)) {
                        add(lastGuardIndex++, fc);
                    }
                }
            }
            if (transition.getEffect() != null) {
                for (String action : transition.getEffect().actionList()) {
                    if (FunctionCall.isFunctionCall(action)) {
                        addUnique(new FunctionCall(action));
                    }
                }
            }
        }
    }

}
