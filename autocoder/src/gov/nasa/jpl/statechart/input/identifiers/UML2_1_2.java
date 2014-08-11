/**
 * Created Aug 26, 2009.
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
package gov.nasa.jpl.statechart.input.identifiers;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.VersionSupport;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.PseudostateKind;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.UMLElement;
import gov.nasa.jpl.statechart.uml.UMLVertex;

import java.util.Collection;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Support for reading UML schema version 2.1.2.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
@VersionSupport(name="http://schema.omg.org/spec/UML/2.1.2", version="2.1.2")
public class UML2_1_2 extends UML2_0 {

    public UML2_1_2 () {
        super();
        // override member element
        literalMap.put(UMLLabel.TAG_MEMBER_ELEMENT, "packagedElement");
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.UML2_0#transition_getGuard(org.w3c.dom.Node)
     */
    @Override
    public Node transition_getGuard (Node n) throws XPathExpressionException {
        // UML 2.1.x, guard is a property that points to a child ownedRule
        String guardId = Util.getNodeAttribute(n, lit(UMLLabel.KEY_GUARD));
        if (guardId == null) {  // then assume no guard, regardless of child element?
            return null;
        }
        return (Node) UMLElement.xpath.evaluate(
                path2NodeOfTypeAndId(UMLLabel.TAG_MEMBER_RULE, UMLLabel.TYPE_CONSTRAINT, guardId),
                n,
                XPathConstants.NODE);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.UML2_0#timeEvent_getTimingExpression(org.w3c.dom.Node)
     */
    @Override
    public Node timeEvent_getTimingExpression (Node n) throws XPathExpressionException {
        // UML 2.1.x, try expr node under when tag
        // also, get LiteralString
        return (Node) UMLElement.xpath.evaluate(
                lit(UMLLabel.TAG_WHEN) + "/" +
                    path2NodeOfAnyOfTypes(UMLLabel.TAG_EXPR, UMLLabel.TYPE_LITERAL_STRING, UMLLabel.TYPE_EXPR_OPAQUE),
                n,
                XPathConstants.NODE);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.UML2_0#vertex_getIncomingTransitions(gov.nasa.jpl.statechart.uml.UMLVertex)
     */
    @Override
    public Collection<Transition> vertex_getIncomingTransitions (UMLVertex v) {
        Collection<Transition> incoming = Util.newList();

        /* since transition can come from any leve, need to search all
         * transitions from this point down into inner states, and then
         * up through ancestor regions, filtering on targets;
         * so the XPath syntax is:
         *   descendant::* /transition[@target='<id>'] | ancestor-or-self::* /transition[@target='<id>']
         *
         * If vertex is a connectionPoint (i.e., entry-/exitPoint), then search
         * needs to begin from the peer-level as vertex, so prepend "..".
         */
        try {
            NodeList transitions = (NodeList) UMLElement.xpath.evaluate(
                    (isConnectionPoint(v) ? "../" : "")
                    + "descendant::*/"
                        + lit(UMLLabel.TAG_TRANSITION)
                        + "[@" + lit(UMLLabel.KEY_TARGET) + "='" + v.id() + "']"
                        + "/@" + XMIIdentifiers.id()
                    + " | ancestor-or-self::*//"
                        + lit(UMLLabel.TAG_TRANSITION)
                        + "[@" + lit(UMLLabel.KEY_TARGET) + "='" + v.id() + "']"
                        + "/@" + XMIIdentifiers.id(),
                    v.getNode(),
                    XPathConstants.NODESET);

            for (int i = 0; i < transitions.getLength(); i++) {
                String tid = transitions.item(i).getNodeValue();
                incoming.add((Transition) v.xmi2uml(tid));
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UML2_1_2.vertex_getIncomingTransitions(): ");
        }

        return incoming;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.UML2_0#vertex_getOutgoingTransitions(gov.nasa.jpl.statechart.uml.UMLVertex)
     */
    @Override
    public Collection<Transition> vertex_getOutgoingTransitions (UMLVertex v) {
        Collection<Transition> outgoing = Util.newList();

        /* search all transitions from one level down into the region(s),
         * up through ancestor regions, filtering on source IDs;
         * so the XPath syntax is:
         *   region/transition[@source='<id>'] | ancestor-or-self::transition[@source='<id>']
         *
         * If vertex is a connectionPoint (i.e., entry-/exitPoint), then search
         * needs to begin from the peer-level as vertex, so prepend "..".
         */
        try {
            NodeList transitions = (NodeList) UMLElement.xpath.evaluate(
                    (isConnectionPoint(v) ? "../" : "")
                    + lit(UMLLabel.TAG_REGION) + "/"
                        + lit(UMLLabel.TAG_TRANSITION)
                        + "[@" + lit(UMLLabel.KEY_SOURCE) + "='" + v.id() + "']"
                        + "/@" + XMIIdentifiers.id()
                    + " | ancestor-or-self::*/"
                        + lit(UMLLabel.TAG_TRANSITION)
                        + "[@" + lit(UMLLabel.KEY_SOURCE) + "='" + v.id() + "']"
                        + "/@" + XMIIdentifiers.id(),
                    v.getNode(),
                    XPathConstants.NODESET);

            if (Util.isDebugLevel()) {
                Util.debug("UML 2.1.2 getOutgoingTrans: [");
            }
            for (int i = 0; i < transitions.getLength(); i++) {
                String tid = transitions.item(i).getNodeValue();
                outgoing.add((Transition) v.xmi2uml(tid));
                if (Util.isDebugLevel()) {
                    Util.debug("  " + tid);
                }
            }
            if (Util.isDebugLevel()) {
                Util.debug("]");
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UML2_1_2.vertex_getOutgoingTransitions(): ");
        }

        return outgoing;
    }

    private boolean isConnectionPoint (UMLVertex v) {
        boolean isConnPt = false;
        if (v instanceof Pseudostate) {
            PseudostateKind kind = ((Pseudostate) v).getKind();
            if (kind == PseudostateKind.entryPoint
                    || kind == PseudostateKind.exitPoint) {
                isConnPt = true;
            }
        }
        return isConnPt;
    }

}
