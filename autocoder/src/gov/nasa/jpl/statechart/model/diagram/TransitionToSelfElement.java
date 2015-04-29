/**
 * Created Dec 7, 2009.
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
import java.util.Map;

/**
 * Represents the diagram information for a UML Transition-to-Self, which has
 * an additional piece of "edge" information.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class TransitionToSelfElement extends TransitionElement {

    /**
     * For some reason, doubling of distance is necessary for the midpoint
     * of the curved line to prevent the curve dome from being half the size.
     */
    public static final int SPLINE_CURVE_FUDGEFACTOR = 2;

    /**
     * Elements indicate, by label, one of four sides of a box:
     * Top, Right, Bottom, Left.  There's a default Unknown.
     */
    public static enum Edge {
        Unknown, Top, Right, Bottom, Left
    }

    private static Map<Integer,Edge> numToEdgeMap = null;
    static {
        numToEdgeMap = Util.newMap();
        numToEdgeMap.put(0, Edge.Top);
        numToEdgeMap.put(1, Edge.Right);
        numToEdgeMap.put(2, Edge.Bottom);
        numToEdgeMap.put(3, Edge.Left);
    }

    // edge of state to which transition-to-self arrow is glued
    private Edge edge = Edge.Unknown;  // default to unknown edge


    /**
     * Constructor for a self-transition, takes in the transition refid;
     * internally sets boolean indicating transition-to-self.
     * 
     * @param tid  refid of the UML Transition
     */
    public TransitionToSelfElement (String tid) {
        super(tid);

        transToSelf = true;
    }

    /**
     * Adds additional points necessary for a transition-to-self arrow.
     * 
     * @see gov.nasa.jpl.statechart.model.diagram.DiagramElement#addPoint(java.awt.Point)
     */
    @Override
    public void addPoint (Point point) {
        if (pointList().size() < 1) {  // just add the first point for now
            super.addPoint(point);
        } else {
            // adjust first point based on glue-edge
            Point size = point;
            Point beginPt = pointList().get(0);
            if (edge == Edge.Left) {  // begin-point is first point + width
                beginPt.x += size.x;
            } else if (edge == Edge.Top) {  // begin-point is first point + height
                beginPt.y += size.y;
            }

            // interpolate midpoint and end-point
            Point midPt = new Point(beginPt);
            Point endPt = new Point(beginPt);
            if (edge == Edge.Left || edge == Edge.Right) {
                // arrow points downward, so y is adjusted the same way
                midPt.y += size.y/2;
                endPt.y += size.y;
                if (edge == Edge.Left) {  // arrow bulges to the left
                    midPt.x -= size.x * SPLINE_CURVE_FUDGEFACTOR;
                } else {  // arrow bulges to the right
                    midPt.x += size.x * SPLINE_CURVE_FUDGEFACTOR;
                }
            }
            if (edge == Edge.Top || edge == Edge.Bottom) {
                // arrow points rightward, so x is adjusted the same way
                midPt.x += size.x/2;
                endPt.x += size.x;
                if (edge == Edge.Top) {  // arrow bulges to the top
                    midPt.y -= size.y * SPLINE_CURVE_FUDGEFACTOR;
                } else {  // arrow bulges to the bottom
                    midPt.y += size.y * SPLINE_CURVE_FUDGEFACTOR;
                }
            }
            super.addPoint(midPt);
            super.addPoint(endPt);
        }
    }

    /**
     * Sets the edge to which this transition-to-self arrow is glued.
     * 
     * @param edgeIdx  the number as retrieved from the XMI.0
     */
    public void setEdge (int edgeIdx) {
        edge = numToEdgeMap.get(edgeIdx);
        if (edge == null) {
            edge = Edge.Unknown;
        }
    }

    /**
     * Returns the edge as an enum element.
     * 
     * @return
     * @see Edge
     */
    public Edge getEdge () {
        return edge;
    }

}
