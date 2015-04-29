package gov.nasa.jpl.statechart.uml;

import java.math.BigInteger;

/**
 * A ValueSpecification is technically a subtype of both TypedElement and
 * PackageableELement, but we have not chosen to implement both of these UML
 * types in our hierarchy.  Instead, the common parent type NamedElement has
 * been chosen as the super-type of ValueSpecification.
 * <br/>
 * <p>
 * As early as 2.3 (or earlier), several operators were specified on this type
 * for retrieving the value of a specific ValueSpecification sub-type.  So,
 * we've added those operators.
 * <br/></p>
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
public interface ValueSpecification extends NamedElement {

    /**
     * Returns whether the value of this {@link ValueSpecification} subtype
     * is computable.  The default is <code>false</code>, and subclass should
     * override when its value is computable.
     * 
     * @return boolean indicating whether this value is a computable value.
     */
    public boolean isComputable ();

    /**
     * Returns when computable and determined that the value is null.
     * 
     * @return boolean indicating whether the computable value is <code>null</code>. 
     */
    public boolean isNull ();

    /** NON-UML Standard!!
     *
     * Returns whether the Autocoder supports this ValueSpecification subtype.
     * 
     * @return boolean indicating if the Autocoder supports this Value
     */
    public boolean isSupported ();

    /** NON-UML Standard!!
     *
     * In place of LiteralValue parent type, indicates whether this
     * ValueSpecification subtype holds a literal value.
     *
     * @return boolean indicating if the ValueSpecification is a Literal*
     */
    public boolean isLiteral ();

    /**
     * Returns the UML Type of this value specification.
     * <br/><br/>
     * Replacement for missing TypedElement in type hiearchy.
     * 
     * @return
     */
    public String getType ();

    /**
     * Returns, if matching, the Expression subtype.  This makes it convenient
     * to obtain a reference to the Expression and then parse it.
     *
     * NOTE: Non-standard UML, but in the spirit of evaluating an expression.
     *
     * @return Expression object, or null otherwise.
     */
    public Expression expression ();

    /**
     * Returns the string value when one can be computed.
     * <br/>
     * <p>
     * The Autocoder implementation also returns the string representation of
     * any value subtype as retrieved from the XMI XML text.
     * </p>
     * 
     * @return string representation of the value
     */
    public String stringValue ();

    /**
     * Returns the boolean value when one can be computed.
     *
     * @return boolean value for this {@link ValueSpecification}
     */
    public Boolean booleanValue ();

    /**
     * Returns the integer value when one can be computed.
     *
     * @return integer value for this {@link ValueSpecification}
     */
    public Integer integerValue ();

    /**
     * Returns the real value when one can be computed.
     *
     * @return double, real value for this {@link ValueSpecification}
     */
    public Double realValue ();

    /**
     * Returns the unlimited natural value when one can be computed.
     *
     * @return BigInteger, "unlimited" value for this {@link ValueSpecification}
     */
    public BigInteger unlimitedValue ();

}
