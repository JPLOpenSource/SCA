/**
 * Created Jun 02, 2013.
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
package gov.nasa.jpl.statechart.input.identifiers;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.ReaderNamespaceContext;
import gov.nasa.jpl.statechart.input.VersionSupport;
import gov.nasa.jpl.statechart.uml.UMLElement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

/**
 * Support for reading UML schema version 2.4.1 minor revision dated 2011.07.01.
 * <p>
 * Copyright &copy; 2010--2013 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng@jpl.nasa.gov
 *
 */
@VersionSupport(name="http://www.omg.org/spec/UML/20110701", version="2.4.1")
public class UML2_4_1 extends UML2_3_2009_0901 {

    public UML2_4_1 () {
        super();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.UML2_1_2#timeEvent_getTimingExpression(org.w3c.dom.Node)
     */
    @Override
    public Node timeEvent_getTimingExpression (Node n) throws XPathExpressionException {
        // UML 2.4.x, grab expr node regardless of intermediate 'when' tag
        // and account for LiteralString, as well as ElementValue
        return (Node) UMLElement.xpath.evaluate(
                lit(UMLLabel.TAG_WHEN) + "/"
                    + path2NodeOfAnyOfTypes(UMLLabel.TAG_EXPR, UMLLabel.TYPE_LITERAL_STRING, UMLLabel.TYPE_EXPR_OPAQUE)
                + " | .//"
                    + path2NodeOfAnyOfTypes(UMLLabel.TAG_EXPR, UMLLabel.TYPE_ELEMENT_VALUE),
                n,
                XPathConstants.NODE);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers#property_getTypeFromHref(org.w3c.dom.Node)
     */
    @Override
    public String property_getTypeFromHref (Node n) {
        String href = Util.getNodeAttribute(n, ReaderNamespaceContext.hrefAttr());

        Pattern regex = Pattern.compile("http://www.omg.org/spec/UML/20110701/PrimitiveTypes.xmi#(.+)$");
        Matcher matcher = regex.matcher(href);
        if (matcher.matches()) {
            return matcher.group(1);
        }

        return null;
    }

}
