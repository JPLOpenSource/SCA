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


/**
 * Represents the diagram information for a UML Transition.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class TransitionElement extends DiagramElement {

    protected boolean transToSelf = false;

    /**
     * Main Constructor, takes in the transition refid of the UML Transition.
     * 
     * @param tid  refid of the UML Transition
     */
    public TransitionElement (String tid) {
        super(tid);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.diagram.DiagramElement#toString()
     */
    @Override
    public String toString () {
        return super.toString() + ", to-self? " + transToSelf;
    }

    public boolean toSelf () {
        return transToSelf;
    }

}
