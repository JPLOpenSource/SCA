package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.input.ReaderNamespaceContext;
import gov.nasa.jpl.statechart.input.identifiers.XMIIdentifiers;
import gov.nasa.jpl.statechart.model.ModelScape;

import java.math.BigInteger;

import org.w3c.dom.Node;

public class UMLValueSpecification extends UMLNamedElement implements ValueSpecification {

    protected String stringValue = null;

    public UMLValueSpecification (Node element, ModelScape scape) {
        super(element, scape);

        String valAttr = getAttribute(element, ReaderNamespaceContext.valueAttr());
        stringValue = (valAttr.length() > 0) ? valAttr : null;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.ValueSpecification#isComputable()
     */
    public boolean isComputable () {
        return false;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.ValueSpecification#isNull()
     */
    public boolean isNull () {
        return false;
    }

    /**
     * By default, the ValueSpecification is NOT supported until the subtype
     * class has been implemented.
     *
     * @see gov.nasa.jpl.statechart.uml.ValueSpecification#isSupported()
     */
    public boolean isSupported () {
        return false;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.ValueSpecification#isLiteral()
     */
    public boolean isLiteral () {
        return false;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.ValueSpecification#getType()
     */
    public String getType () {
        return getAttribute(domElement, XMIIdentifiers.type());
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.ValueSpecification#expression()
     */
    public Expression expression () {
        Expression expr = null;

        if (this instanceof Expression) {
            expr = (Expression) this;
        }

        return expr;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.ValueSpecification#stringValue()
     */
    public String stringValue () {
        return stringValue;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.ValueSpecification#booleanValue()
     */
    public Boolean booleanValue () {
        return null;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.ValueSpecification#integerValue()
     */
    public Integer integerValue () {
        return null;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.ValueSpecification#realValue()
     */
    public Double realValue () {
        return null;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.ValueSpecification#unlimitedValue()
     */
    public BigInteger unlimitedValue () {
        return null;
    }

}
