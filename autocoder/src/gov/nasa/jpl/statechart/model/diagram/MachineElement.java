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
 * StateMachine diagram element.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class MachineElement extends DiagramElement {

    private double zoomFactor = 1.0;  // default to a reasonable 100% zoom.
    private Point windowBoundP1 = new Point(0, 0);
    private Point windowBoundP2 = new Point(600, 500);

    /**
     * Main constructor, sets the refid of the UML element.
     * @param smid  refid of the UML StateMachine
     */
    public MachineElement (String smid) {
        super(smid);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.diagram.DiagramElement#toString()
     */
    @Override
    public String toString () {
        return super.toString() + ", zoom " + zoomFactor + ", window dims " + windowDimensions();
    }

    public void setZoomFactor (double factor) {
        zoomFactor = factor;
    }

    public double zoomFactor () {
        return zoomFactor;
    }

    public Point frameDimensions () {
        if (pointList().size() < 2) {  // use window bounds instead
            return windowDimensions();
        }

        Point p1 = pointList().get(0);
        Point p2 = pointList().get(1);
        return new Point(p2.x - p1.x, p2.y - p1.y);
    }

    public void setWindowBounds (Point p1, Point p2) {
        windowBoundP1 = p1;
        windowBoundP2 = p2;

        // modify window bound if it's greater than frame dims.
        if (pointList().size() >= 2) {
            // ok to call frameDimensions to get frame size
            Point frameSize = frameDimensions();
            // ok to call windowDimensions now that P1 and P2 are set
            Point winBound = windowDimensions();
            if (winBound.x > frameSize.x || winBound.y > frameSize.y) {
                // adjust window P2 to be, at max, P1 + frameSize
                windowBoundP2.x = windowBoundP1.x + frameSize.x;
                windowBoundP2.y = windowBoundP1.y + frameSize.y;
            }
        }
    }

    public Point windowDimensions () {
        return new Point(windowBoundP2.x - windowBoundP1.x,
                windowBoundP2.y - windowBoundP1.y);
    }

}
