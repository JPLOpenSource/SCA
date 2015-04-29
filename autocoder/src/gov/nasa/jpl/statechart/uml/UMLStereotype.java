/**
 * Created Jul 13, 2013.
 * <p>
 * Copyright 2009-2013, by the California Institute of Technology. ALL RIGHTS
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
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.model.ModelScape;

import org.w3c.dom.Node;

/**
 * Implementation class for a UML Stereotype, which is a subtype of Class, so
 * all additional attributes need to be defined.
 * <br/>
 * <p>
 * Copyright 2009-2013, by the California Institute of Technology. ALL RIGHTS
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
 *
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public class UMLStereotype extends UMLClass implements Stereotype {

    protected Profile containingProfile = null;

    /**
     * @param element
     * @param scape
     */
    public UMLStereotype (Node element, ModelScape scape) {
        super(element, scape);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Stereotype#containingProfile()
     */
    public Profile containingProfile () {
        return containingProfile;
    }

    public void setContainingProfile (Profile myContainingProfile) {
        containingProfile = myContainingProfile;
    }

}
