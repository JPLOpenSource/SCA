/**
 * Created Oct 8, 2009.
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
package gov.nasa.jpl.statechart.model.diagram;

import java.awt.Point;


/**
 * A line which separates concurrent regions within a state machine.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class SeparatorElement extends DiagramElement {

    /**
     * Default constructor, no param.
     */
    public SeparatorElement (Point p1, Point p2) {
        super(null);

        addPoint(p1);
        // p2 is actually w & h dimensions, so add only p1.x to get horizontal line
        addPoint(new Point(p1.x + p2.x, p1.y));
    }

}
