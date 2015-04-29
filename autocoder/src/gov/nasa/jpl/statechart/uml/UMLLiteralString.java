/**
 * Created Sep 30, 2009.
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
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.model.ModelScape;

import org.w3c.dom.Node;

/**
 * Class to represent a literal string value in a UML model.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class UMLLiteralString extends UMLValueSpecification implements LiteralString {

    /**
     * @param element
     * @param scape
     */
    public UMLLiteralString (Node element, ModelScape scape) {
        super(element, scape);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.UMLValueSpecification#isComputable()
     */
    @Override
    public boolean isComputable () {
        return true;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.UMLValueSpecification#isNull()
     */
    @Override
    public boolean isNull () {
        return (stringValue == null);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.UMLValueSpecification#isSupported()
     */
    @Override
    public boolean isSupported () {
        return true;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.UMLValueSpecification#isLiteral()
     */
    @Override
    public boolean isLiteral () {
        return true;
    }

}
