/**
 * Created Dec 15, 2009.
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

import gov.nasa.jpl.statechart.uml.State;

/**
 * Extracts and returns a collection of States that refer to a Sub-StateMachine.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public class SubmachineStateVisitor extends AbstractVisitor<State> {
    private static final long serialVersionUID = -5431194728196591179L;

    public SubmachineStateVisitor (boolean descend) {
        super(descend);
    }

    @Override
    public void visit (State state) {
        if (state.isSubmachineState()) {
            add(state);
        }
    }
    
}
