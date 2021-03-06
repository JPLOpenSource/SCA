/**
 * Created Jul 10, 2013.
 * <p>
 * Copyright 2009--2013, by the California Institute of Technology. ALL RIGHTS
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
 * Implementation class for the UML LiteralReal value subtype.
 * <br/>
 * <p>
 * Copyright 2009--2013, by the California Institute of Technology. ALL RIGHTS
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
public class UMLLiteralReal extends UMLValueSpecification implements LiteralReal {

    /**
     * The real value of this literal real.  As suggested by the specification
     * of LiteralUnlimitedNatural, the LiteralReal default value will be 0.0.
     */
    protected Double realValue = 0.0;

    /**
     * @param element
     * @param scape
     */
    public UMLLiteralReal (Node element, ModelScape scape) {
        super(element, scape);

        if (stringValue != null) {
            realValue = Double.parseDouble(stringValue);
        } else {  // make default value the string representation
            stringValue = realValue.toString();
        }
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
        return (stringValue == null || realValue == null);
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

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.UMLValueSpecification#realValue()
     */
    @Override
    public Double realValue () {
        return realValue;
    }

}
