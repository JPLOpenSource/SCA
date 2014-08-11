/**
 * Created Oct 2, 2009.
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

import gov.nasa.jpl.statechart.uml.Region;

/**
 * Picks up the orthogonal regions below the starting vertex.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class OrthogonalRegionVisitor extends AbstractVisitor<Region> {
    private static final long serialVersionUID = -6424253998160999902L;

    public OrthogonalRegionVisitor (boolean descend) {
        super(descend);
    }

    @Override
    public void visit (Region region) {
        if (region.getState() != null && region.getState().isOrthogonal() && !contains(region)) {
            add(region);
        }
    }

}
