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
import gov.nasa.jpl.statechart.input.ReaderNamespaceContext;
import gov.nasa.jpl.statechart.input.VersionSupport;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.UMLElement;
import gov.nasa.jpl.statechart.uml.UMLOpaqueBehavior;
import gov.nasa.jpl.statechart.uml.UMLValueSpecification;
import gov.nasa.jpl.statechart.uml.UMLVertex;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Support for reading UML schema version 2.0.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
@VersionSupport(name="http://schema.omg.org/spec/UML/2.0", version="2.0")
public class UML2_0 extends UMLIdentifiers {

    public UML2_0 () {
        super();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers#transition_getGuard(org.w3c.dom.Node)
     */
    @Override
    public Node transition_getGuard (Node n) throws XPathExpressionException {
        return (Node) UMLElement.xpath.evaluate(
                path2NodeOfType(UMLLabel.TAG_GUARD, UMLLabel.TYPE_CONSTRAINT),
                n,
                XPathConstants.NODE);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers#timeEvent_getTimingExpression(org.w3c.dom.Node)
     */
    @Override
    public Node timeEvent_getTimingExpression (Node n) throws XPathExpressionException {
        return (Node) UMLElement.xpath.evaluate(
                path2NodeOfType(UMLLabel.TAG_WHEN, UMLLabel.TYPE_EXPR_OPAQUE),
                n,
                XPathConstants.NODE);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers#signalEvent_getReferencedSignalId(org.w3c.dom.Node)
     */
    @Override
    public String signalEvent_getReferencedSignalId (Node n) {
        String id = null;
        try {
            id = (String) UMLElement.xpath.evaluate(
                    path2NodeOfType(UMLLabel.TAG_SIGNAL, UMLLabel.TYPE_SIGNAL)
                    + "/@" + ReaderNamespaceContext.hrefAttr(),
                    n,
                    XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UML2_0.signalEvent_getReferencedSignalId(): ");
        }
        return id;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers#behavior_getSpec(gov.nasa.jpl.statechart.uml.UMLOpaqueBehavior)
     */
    @Override
    public Map<String,String> behavior_getSpec (UMLOpaqueBehavior beh) {
        // in UML 2.0, only 1 pair of body/lang is returned
        Map<String,String> specBodyLangMap = new LinkedHashMap<String,String>();

        String body = Util.getNodeAttribute(beh.getNode(), lit(UMLLabel.KEY_BODY));
        String lang = Util.getNodeAttribute(beh.getNode(), lit(UMLLabel.KEY_LANGUAGE));
        specBodyLangMap.put(body, lang);

        return specBodyLangMap;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers#expr_getBody(gov.nasa.jpl.statechart.uml.UMLValueSpecification)
     */
    @Override
    public String value_getExprBody (UMLValueSpecification spec) {
        String attribute = Util.getNodeAttribute(spec.getNode(), lit(UMLLabel.KEY_BODY));
        return (attribute == null) ? "" : attribute;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers#vertex_getIncomingTransitions(gov.nasa.jpl.statechart.uml.UMLVertex)
     */
    @Override
    public Collection<Transition> vertex_getIncomingTransitions (UMLVertex v) {
        Collection<Transition> incoming = Util.newList();
        try {
            NodeList transitions = (NodeList) UMLElement.xpath.evaluate(
                    lit(UMLLabel.TAG_INCOMING) + "/@" + XMIIdentifiers.idref(),
                    v.getNode(),
                    XPathConstants.NODESET);

            for (int i = 0; i < transitions.getLength(); i++) {
                incoming.add((Transition) v.xmi2uml(transitions.item(i).getNodeValue()));
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UML2_0.vertex_getIncomingTransitions(): ");
        }

        return incoming;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers#vertex_getOutgoingTransitions(gov.nasa.jpl.statechart.uml.UMLVertex)
     */
    @Override
    public Collection<Transition> vertex_getOutgoingTransitions (UMLVertex v) {
        Collection<Transition> outgoing = Util.newList();
        try {
            NodeList transitions = (NodeList) UMLElement.xpath.evaluate(
                    lit(UMLLabel.TAG_OUTGOING) + "/@" + XMIIdentifiers.idref(),
                    v.getNode(),
                    XPathConstants.NODESET);

            for (int i = 0; i < transitions.getLength(); i++) {
                outgoing.add((Transition) v.xmi2uml(transitions.item(i).getNodeValue()));
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UML2_0.vertex_getOutgoingTransitions(): ");
        }

        return outgoing;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers#property_getTypeFromHref(org.w3c.dom.Node)
     */
    @Override
    public String property_getTypeFromHref (Node n) {
        String href = Util.getNodeAttribute(n, ReaderNamespaceContext.hrefAttr());

        Pattern regex = Pattern.compile("http://schema.omg.org/spec/UML/2.0/PrimitiveTypes.xmi#(.+)$");
        Matcher matcher = regex.matcher(href);
        if (matcher.matches()) {
            return matcher.group(1);
        }

        return null;
    }

}
