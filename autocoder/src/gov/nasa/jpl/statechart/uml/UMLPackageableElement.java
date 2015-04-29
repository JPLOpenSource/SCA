/**
 * Created Apr 6, 2011.
 * <p>
 * Copyright 2009-2011, by the California Institute of Technology. ALL RIGHTS
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
 * A packageable element indicates a named element that may be owned directly
 * by a package.
 * <p>
 * Copyright &copy; 2009-2011 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public class UMLPackageableElement extends UMLNamedElement implements PackageableElement {

    /**
     * Main constructor to construct a packageable element using a supplied
     * XMI Node element representing the packageable element.
     * @param element
     */
    public UMLPackageableElement (Node element, ModelScape scape) {
        super(element, scape);

        // Visibility is required, so default to PRIVATE visibility.
        if (visibility == null) {
            visibility = VisibilityKind.PRIVATE;
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.PackageableElement#visibility()
     */
    public VisibilityKind visibility () {
        return visibility;
    }

}
