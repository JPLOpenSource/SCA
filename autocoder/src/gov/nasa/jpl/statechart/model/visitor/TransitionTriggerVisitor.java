/**
 * Created Oct 2, 2009.
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

import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.Trigger;
import gov.nasa.jpl.statechart.uml.Vertex;

/**
 * Abstract visitor to facilitate working with transition triggers.
 * Basically, for each transition out of a state or Pseudostate,
 * delegate examination of its triggers to the subclass.
 * <p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public abstract class TransitionTriggerVisitor<T> extends AbstractVisitor<T> {
    private static final long serialVersionUID = 9075573179782043880L;

    public TransitionTriggerVisitor (boolean descend) {
        super(descend);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.AbstractVisitor#visit(gov.nasa.jpl.statechart.uml.State)
     */
    @Override
    public void visit (State state) {
        for (Transition transition : state.getOutgoing()) {
            for (Trigger trigger : transition.getTrigger()) {
                doTransitionTrigger(state, transition, trigger);
            }
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.AbstractVisitor#visit(gov.nasa.jpl.statechart.uml.Pseudostate)
     */
    @Override
    public void visit (Pseudostate pseudostate) {
        for (Transition transition : pseudostate.getOutgoing()) {
            for (Trigger trigger : transition.getTrigger()) {
                doTransitionTrigger(pseudostate, transition, trigger);
            }
        }
    }

    protected abstract void doTransitionTrigger (Vertex v, Transition transition, Trigger trigger);

}
