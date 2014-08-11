/**
 * Created Oct 12, 2009.
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
import gov.nasa.jpl.statechart.uml.Vertex;

/**
 * Visitor to collect all the Pseudostate and State vertices visited.
 * <p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 */
public class VertexVisitor extends AbstractVisitor<Vertex> {
    private static final long serialVersionUID = -3107366839981424685L;

    public VertexVisitor (boolean descend) {
        super(descend);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.AbstractVisitor#visit(gov.nasa.jpl.statechart.uml.Pseudostate)
     */
    @Override
    public void visit (Pseudostate pseudostate) {
        addUnique(pseudostate);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.AbstractVisitor#visit(gov.nasa.jpl.statechart.uml.State)
     */
    @Override
    public void visit (State state) {
        if (addUnique(state)) {
            // add the ConnectionPointReferences
            addAll(state.getConnection());
        }
    }

}
