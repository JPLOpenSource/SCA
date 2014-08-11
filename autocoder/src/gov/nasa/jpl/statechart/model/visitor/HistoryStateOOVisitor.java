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

import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.Region;

/**
 * Extracts all the history states from a starting point down, but without
 * descending below orthogonal regions nor Submachines.
 */
public class HistoryStateOOVisitor extends AbstractVisitor<Pseudostate> {
    private static final long serialVersionUID = -5233441471734578051L;

    public HistoryStateOOVisitor () {
        super(false, OrthoRegion.STOP_AT_ORTHO);
    }

    @Override
    public void visit (Region region) {
        // check for region with history!
        if (region.containsHistoryState()) {  // add history states
            addAll(region.getHistoryState());
        }
    }
}
