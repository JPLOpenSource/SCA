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

import gov.nasa.jpl.statechart.model.visitor.AbstractVisitor.OrthoRegion;
import gov.nasa.jpl.statechart.uml.Region;

/**
 * Visits and extracts all the regions from a starting point down.
 * The second, orthogonal-region parameter in constructor
 * {@link #RegionVisitor(boolean, AbstractVisitor.OrthoRegion)}
 * can be set to {@link OrthoRegion#STOP_AT_ORTHO} to stop the walker from
 * descending below orthogonal regions, if these sub-regions are
 * taken care of separately by respective classes.
 * (True of Python and C++, NOT used by C.)
 * <p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 */
public class RegionVisitor extends AbstractVisitor<Region> {
    private static final long serialVersionUID = -5128391315087789247L;

    /**
     * Main constructor to use for visiting states, descending into SubMachines
     * based on <code>descend</code>, and including elements below
     * orthogonal regions.
     * 
     * @param descend  flag to indicate whether to descend into SubMachines.
     */
    public RegionVisitor (boolean descend) {
        super(descend);
    }

    /**
     * Full constructor, used to configure both whether to <code>descend</code>
     * into SubMachines as well as whether to <code>include</code> elements
     * below orthogonal regions.
     * 
     * @param descend  flag to indicate whether to descend into SubMachines.
     * @param include  enum to indicate whether to include orthogonal regions.
     */
    public RegionVisitor (boolean descend, OrthoRegion include) {
        super(descend, include);
    }

    @Override
    public void visit (Region region) {
        add(region);
    }
}
