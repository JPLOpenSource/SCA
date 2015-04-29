/**
 * Created Aug 08, 2013.
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
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.model.ModelScape;

import java.util.Collection;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An enumeration is a data type whose values are enumerated in the model as
 * enumeration literals.  Enumeration is a kind of data type, whose instances
 * may be any of a number of user-defined enumeration literals.  It is possible
 * to extend the set of applicable enumeration literals in other packages or
 * profiles.
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
public class UMLEnumeration extends UMLDataType implements Enumeration {

    protected Collection<EnumerationLiteral> ownedLiteral = Util.newList();

    /**
     * @param element
     * @param scape
     */
    public UMLEnumeration (Node element, ModelScape scape) {
        super(element, scape);

        try {
            // Fetch owned literals
            NodeList litNodes = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_MEMBER_LITERAL, UMLLabel.TYPE_ENUMERATION_LITERAL),
                    element, XPathConstants.NODESET);

            for (int i = 0; i < litNodes.getLength(); i++) {
                UMLEnumerationLiteral lit = new UMLEnumerationLiteral(litNodes.item(i), scape);
                ownedLiteral.add(lit);
                ownedMember.add(lit);
                lit.setEnumeration(this);
            }

        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLEnumeration constructor: ");
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Enumeration#ownedLiteral()
     */
    public Collection<EnumerationLiteral> ownedLiteral () {
        return ownedLiteral;
    }

}
