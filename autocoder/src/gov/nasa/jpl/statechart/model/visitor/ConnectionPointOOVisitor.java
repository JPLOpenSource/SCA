/**
 * Created Apr 13, 2010.
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
import gov.nasa.jpl.statechart.uml.PseudostateKind;

/**
 * Visitor to collect entry/exitPoint ConnectionPoint Pseudostates only,
 * without descending into Orthogonal regions.
 * <p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 */
public class ConnectionPointOOVisitor extends AbstractVisitor<Pseudostate> {
    private static final long serialVersionUID = -3107366839981424685L;

    public ConnectionPointOOVisitor (boolean descend) {
        super(descend);
    }

    @Override
    public void visit (Pseudostate pseudostate) {
        if (pseudostate.getKind() == PseudostateKind.entryPoint
                || pseudostate.getKind() == PseudostateKind.exitPoint) {
            add(pseudostate);
        }
    }
}
