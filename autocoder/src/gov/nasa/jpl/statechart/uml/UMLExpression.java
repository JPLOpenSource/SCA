/**
 * Created Jul 12, 2013.
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
 * Implementation class for the UML Expression  value subtype.  This class
 * allows expressions specified in the UML XMI to be parsed and represented
 * as full string expression.
 * <br/>
 * <p>
 * The expression parsing code was ported from M.Pellegrin's original draft
 * implementation in the UMLConstraint class.
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
 * @author Michael Pellegrin <Michael.G.Pellegrin@jpl.nasa.gov>
 */
public class UMLExpression extends UMLValueSpecification implements Expression {

    /** The symbol associated with the node in the expression tree. */
    protected String symbol = null;

    /** Specifies a sequence of operands. */
    protected ValueSpecification[] operand = null;


    /**
     * @param element
     * @param scape
     */
    public UMLExpression (Node element, ModelScape scape) {
        super(element, scape);

        try {
            symbol = getAttribute(element, UMLLabel.KEY_SYMBOL);
            if (symbol != null) {
                symbol = symbol.toUpperCase();
            }

            // look any level for operands of any supported UML type; parse each
            NodeList operandNodes = (NodeList) xpath.evaluate(
                    getValueSpecificationXpath(UMLLabel.TAG_OPERAND),
                    element, XPathConstants.NODESET);
            operand = new ValueSpecification[operandNodes.getLength()];
            for ( int i = 0 ; i < operandNodes.getLength() ; i++ ) {
                operand[i] = getValueSpecificationSubtype(operandNodes.item(i));
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLExpression constructor: ");
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.UMLNamedElement#toString()
     */
    @Override
    public String toString () {
        return super.toString() + ": " + stringValue();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.UMLValueSpecification#isNull()
     */
    @Override
    public boolean isNull () {
        return (symbol == null && operand == null);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.UMLValueSpecification#isSupported()
     */
    @Override
    public boolean isSupported () {
        return true;
    }

    /* Returns a prefix representation of this expression, with parentheses
     * surrounding the operands, if any.
     *
     * @see gov.nasa.jpl.statechart.uml.UMLValueSpecification#stringValue()
     */
    @Override
    public String stringValue () {
        StringBuilder sb = new StringBuilder(symbol).append("(");

        if (operand != null && operand.length > 0) {
            sb.append(operand[0].stringValue());
            for (int i=1 ; i < operand.length ; ++i) {
                sb.append(" ").append(operand[i].stringValue());
            }
        }

        sb.append(")");
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Expression#symbol()
     */
    public String symbol () {
        return symbol;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Expression#operand()
     */
    public ValueSpecification[] operand () {
        return operand;
    }

}
