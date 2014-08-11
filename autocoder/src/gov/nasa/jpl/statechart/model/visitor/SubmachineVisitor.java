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

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;

import java.util.Map;

/**
 * Extract all the submachines from the submachine states.
 */
public class SubmachineVisitor extends AbstractVisitor<StateMachine> {
    private static final long serialVersionUID = -8943828277479999765L;

    // records whether StateMachine has already been added, for quick search
    private Map<String,Boolean> smAdded = null;

    public SubmachineVisitor (boolean descend) {
        super(descend);

        smAdded = Util.newMap();
    }

    @Override
    public void visit (State state) {
        if (state.isSubmachineState()) {
            StateMachine smToAdd = state.getSubmachine();
            if (smToAdd != null && ! smAdded.containsKey(smToAdd.getName())){
                // submachine doesn't already exist in array
                add(smToAdd);
                smAdded.put(smToAdd.getName(), true);
            }
        }
    }

}