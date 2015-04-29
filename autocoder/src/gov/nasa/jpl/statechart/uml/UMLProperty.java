/**
 * Created Jul 10, 2013.
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

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.model.ModelScape;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A property is a structural feature.  Related to a classifier, it represents
 * an attribute; related to an Association, it represents an end of association.
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
public class UMLProperty extends UMLNamedElement implements Property {

    protected Classifier classifier = null;
    protected ValueSpecification defaultValue = null;

    /** NON-UML standard!!
     * The string representation of the Property's value.
     */
    protected String valueString = null;


    /**
     * @param element
     * @param scape
     */
    public UMLProperty (Node element, ModelScape scape) {
        super(element, scape);

        try {
            // Fetch default value, if any
            NodeList defaultValueNodes = (NodeList) xpath.evaluate(
                    getValueSpecificationXpath(UMLLabel.TAG_DEFAULT_VALUE),
                    element, XPathConstants.NODESET);

            for (int i = 0; i < defaultValueNodes.getLength(); i++) {
                defaultValue = getValueSpecificationSubtype(defaultValueNodes.item(i));

                // cache string representation of value
                setValueString(defaultValue.stringValue());
            }

        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLProperty constructor: ");
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Property#classifier()
     */
    public Classifier classifier () {
        return classifier;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Property#getDefaultValue()
     */
    public ValueSpecification getDefaultValue () {
        return defaultValue;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Property#setValueString(java.lang.String)
     */
    public void setValueString (String valStr) {
        valueString = valStr;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Property#getValueString()
     */
    public String getValueString () {
        return valueString;
    }

    /**
     * Sets the Classifier that owns this Property.
     * 
     * @param owningClassifier
     */
    public void setClassifier (Classifier owningClassifier) {
        classifier = owningClassifier;
    }

}
