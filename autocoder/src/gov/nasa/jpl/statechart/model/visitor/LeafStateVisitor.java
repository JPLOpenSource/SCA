/**
 * Created Dec 8, 2009.
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

import gov.nasa.jpl.statechart.model.visitor.AbstractVisitor.OrthoRegion;
import gov.nasa.jpl.statechart.uml.State;

/**
 * Visits all the states and collects only the leaf states.
 * Note that the first constructor parameter determines whether to descend
 * below submachines.  The single-parameter constructor should always be used
 * unless the client intends to NOT descend below orthogonal regions.
 * <p>
 * The second parameter of the two-parameter constructor can be used to force
 * NOT descending below orthogonal regions.  In this case, supply the enum
 * {@link OrthoRegion#STOP_AT_ORTHO} for the second parameter.
 * Be aware that when not descending below orthogonal regions, the
 * containing orthogonal states are included in the list of leaf states.
 * </p><p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class LeafStateVisitor extends AbstractVisitor<State> {
    private static final long serialVersionUID = 2356125334850801954L;

    public LeafStateVisitor (boolean descend) {
        super(descend, OrthoRegion.INCLUDE_BELOW_ORTHO);
    }

    public LeafStateVisitor (boolean descend, OrthoRegion ortho) {
        super(descend, ortho);
    }

    @Override
    public void visit (State state) {
        if (state.isSimple()) {  // found leaf state
            addUnique(state);
        } else {  // consider orthogonal states if NOT descending into them
            if (state.isOrthogonal() && !includeOrtho()) {
                addUnique(state);
            }
        }
    }

}
