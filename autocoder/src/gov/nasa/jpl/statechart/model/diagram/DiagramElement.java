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

import gov.nasa.jpl.statechart.Util;

import java.awt.Point;
import java.util.List;

/**
 * Defines GUI diagram elements of a state transition diagram.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>, adapted from old DiagramElement class.
 */
public abstract class DiagramElement {

    protected String refId = null;

    private List<Point> pointList = null;

    /**
     * Main constructor, takes the reference ID to the corresponding UML element.
     * @param id  XMI refid of the UML element.
     */
    public DiagramElement (String id) {
        refId = id;
        pointList = Util.newList();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return getClass().getSimpleName() + ": ID " + refId
            + ", points " + pointList.toString();
    }

    /**
     * Adds a coordinate point to the point list for this diagram element. The
     * points are used to specify the diagram element's size and location.
     * 
     * @param point a Point object
     */
    public void addPoint (Point point) {
        pointList.add(point);
    }

    /**
     * Returns the point list for this diagram element.
     * 
     * @return list of coordinate Points defining the diagram element.
     */
    public List<Point> pointList () {
        return pointList;
    }

}
