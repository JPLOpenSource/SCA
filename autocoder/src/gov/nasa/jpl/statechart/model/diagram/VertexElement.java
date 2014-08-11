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

/**
 * Represents the diagram information for a UML Region, State, or Pseudostate.
 * A couple Pseudostates have specialized coordinate computation methods, which
 * we have not yet refactored into specialized classes: <ul>
 * <li> The 'terminate' Pseudostate requires {@link #upperLeftOffset()} and
 * {@link #lowerRightOffset()} for coordinates of the inner filled circle.
 * <li> The 'choice' Pseudostate requires four coordinates for the vertices of
 * the diamond: .
 * </ul>
 * Specializing this class is doable, but we will do only as necessary.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class VertexElement extends DiagramElement {

    public static final int defaultPseudostateFillColor = 0x0000ff;
    public static final int defaultStateFillColor = 0xffffcc;
    public static final int circleOffset = 4;
    public static final double COS_ANGLE45 = Math.sqrt(2)/2;

    private boolean hasOutlineColor = false;
    private int outlineColor = 0;
    private int width = 0;
    private int height = 0;
    private double avgRadius = 0.0;
    private int rectDim = 0;

    /**
     * Main Constructor, takes in the vertex refid and the corner coordinates.
     * @param vid  refid of the UML Region or Vertex
     * @param p1   upper-left coordinate point of the box
     * @param p2   (width,height) to lower-right coordinate point of the box
     */
    public VertexElement (String vid, Point p1, Point p2) {
        super(vid);

        addPoint(p1);
        // p2 is actually w & h dimensions, so add p1 to get lower-right corner
        width = p2.x;
        height = p2.y;
        // for computing X coordinates on circle
        avgRadius = (double)(width + height)/(2.0*2);
        rectDim = (int)(avgRadius * COS_ANGLE45);
        addPoint(new Point(p1.x + width, p1.y + height));
    }

    /**
     * Constructor that takes just the vertex refid; coordinates are added
     * as separate operations.
     * @param vid  refid of UML Region or Vertex
     */
    public VertexElement (String vid) {
        super(vid);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.diagram.DiagramElement#toString()
     */
    @Override
    public String toString () {
        return super.toString() + ", outlineColor " + outlineColor;
    }

    /**
     * Returns a string representing the default fill color for a UML State in
     * hext form #rrggbb
     * @return  default fill color of the form '#rrggbb' for a State.
     */
    public String defaultStateFillColorText () {
        return Util.toHexString(defaultStateFillColor, 6);
    }

    /**
     * Method to specify an outline color for the vertex diagram element.
     * 
     * @param value  Integer representation of the fill color in the format
     *      "rrggbb", where "rr" are two hex digits (one byte) specifying
     *      red intensity, "gg" are two hex digits specifying green
     *      intensity, and "bb" are two hex digits specifying blue
     *      intensity. The six hex digits "rrggbb" together are specified as
     *      one 24 bit unsigned integer value.
     */
    public void setOutlineColor (int value) {
        outlineColor = value;
        hasOutlineColor = true;
    }

    /**
     * Returns a string representing the outline color in hex form #rrggbb.
     * @return  outline color string of the form '#rrggbb'
     */
    public String outlineColorText () {
        return Util.toHexString(outlineColor, 6);
    }

    /**
     * Returns whether an outline color is defined for this diagram element.
     * @return  <code>true</code> if outline color is defined, <code>false</code> otherwise.
     */
    public boolean hasOutlineColor () {
        return hasOutlineColor;
    }

    /**
     * Returns the upper-left coordinate point for drawing the vertex shape.
     * @return  a {@link Point} representing the upper-left coordinate.
     */
    public Point upperLeftCoord () {
        return pointList().get(0);
    }

    /**
     * Returns the lower-right coordinate point for drawing the vertex shape.
     * @return  a {@link Point} representing the lower-right coordinate.
     */
    public Point lowerRightCoord () {
        return pointList().get(1);
    }

    /**
     * Returns the upper-left coordinate point, offset slightly to the inside
     * of the usual upper-left coordinate, for drawing an inner circle for the
     * 'terminate' Pseudostate.
     * 
     * @return  a {@link Point} representing a slightly offset upper-left coordinate.
     */
    public Point upperLeftOffset () {
        return new Point(upperLeftCoord().x + circleOffset, upperLeftCoord().y + circleOffset);
    }

    /**
     * Returns the lower-right coordinate point, offset slightly to the inside
     * of the usual lower-right coordinate, for drawing an inner circle for the
     * 'terminate' Pseudostate.
     * 
     * @return  a {@link Point} representing a slightly offset lower-right coordinate.
     */
    public Point lowerRightOffset () {
        return new Point(lowerRightCoord().x - circleOffset, lowerRightCoord().y - circleOffset);
    }

    /**
     * Creates (if not yet exist) and returns the coordinate for the text label.
     * @return  a 2D {@link Point} for the upper-left coordinate of the text label.
     */
    public Point textCoord () {
        if (pointList().size() < 3) {  // create and add new point
            addPoint(new Point(
                    upperLeftCoord().x + ((lowerRightCoord().x - upperLeftCoord().x) / 2),
                    upperLeftCoord().y + 5));
        }
        return pointList().get(2);
    }

    /**
     * Returns the top Vertex of a diamond that would fit snugly in the
     * rectangular space defined by the two coordinates of this diagram element.
     * @return  a {@link Point} representing the top Vertex of a diamond.
     */
    public Point middleUpperVertex () {
        return new Point((upperLeftCoord().x + lowerRightCoord().x)/2, upperLeftCoord().y);
    }

    /**
     * Returns the bottom Vertex of a diamond that would fit snugly in the
     * rectangular space defined by the two coordinates of this diagram element.
     * @return  a {@link Point} representing the bottom Vertex of a diamond.
     */
    public Point middleLowerVertex () {
        return new Point((upperLeftCoord().x + lowerRightCoord().x)/2, lowerRightCoord().y);
    }

    /**
     * Returns the left Vertex of a diamond that would fit snugly in the
     * rectangular space defined by the two coordinates of this diagram element.
     * @return  a {@link Point} representing the left Vertex of a diamond.
     */
    public Point leftMiddleVertex () {
        return new Point(upperLeftCoord().x, (upperLeftCoord().y + lowerRightCoord().y)/2);
    }

    /**
     * Returns the right Vertex of a diamond that would fit snugly in the
     * rectangular space defined by the two coordinates of this diagram element.
     * @return  a {@link Point} representing the right Vertex of a diamond.
     */
    public Point rightMiddleVertex () {
        return new Point(lowerRightCoord().x, (upperLeftCoord().y + lowerRightCoord().y)/2);
    }

    public Point circleUpperLeft () {
        return new Point((upperLeftCoord().x + lowerRightCoord().x)/2 - rectDim,
                         (upperLeftCoord().y + lowerRightCoord().y)/2 - rectDim);
    }

    public Point circleUpperRight () {
        return new Point((upperLeftCoord().x + lowerRightCoord().x)/2 + rectDim,
                         (upperLeftCoord().y + lowerRightCoord().y)/2 - rectDim);
    }

    public Point circleLowerLeft () {
        return new Point((upperLeftCoord().x + lowerRightCoord().x)/2 - rectDim,
                         (upperLeftCoord().y + lowerRightCoord().y)/2 + rectDim);
    }

    public Point circleLowerRight () {
        return new Point((upperLeftCoord().x + lowerRightCoord().x)/2 + rectDim,
                         (upperLeftCoord().y + lowerRightCoord().y)/2 + rectDim);
    }

}
