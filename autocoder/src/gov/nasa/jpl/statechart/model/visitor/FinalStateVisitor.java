/**
 * Created Jan 13, 2010.
 * <p>
 * Copyright 2010, by the California Institute of Technology. ALL RIGHTS
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

import gov.nasa.jpl.statechart.uml.FinalState;

/**
 * Visits all states below a starting point and collects only the final states.
 * <p>
 * Copyright &copy; 2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public class FinalStateVisitor extends AbstractVisitor<FinalState> {
    private static final long serialVersionUID = 496973903669446768L;

    public FinalStateVisitor (boolean descend) {
        super(descend);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.AbstractVisitor#visit(gov.nasa.jpl.statechart.uml.FinalState)
     */
    @Override
    public void visit (FinalState finalState) {
        add(finalState);
    }

}
