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

/**
 * A property is a structural feature.  Related to a classifier, it represents
 * an attribute; related to an Association, it represents an end of association.
 * <br/>
 * <p>
 * UML specifies Property as a StructuralFeature, which both a TypedElement
 * and a MultiplicityElement.  Since we do not model double inheritance in Java,
 * and for simplicity, we've skipped the StructuralFeature hierachy and made
 * Property a subtype of NamedElement (which TypedElement is a subtype of).
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
public interface Property extends NamedElement {

    /**
     * Returns the Classifier owning this attribute.
     * 
     * @return {@link Classifier} owning this attribute
     */
    Classifier classifier ();

    /**
     * @return The default value of this property, if any
     */
    ValueSpecification getDefaultValue ();

    /** NON-UML standard!!
     * Sets the string representation of the property's value
     * @param valStr  new string to set value
     */
    void setValueString (String valStr);

    /** NON-UML standard!!
     * @return The string representation of the Property's value
     */
    String getValueString ();
    
}
