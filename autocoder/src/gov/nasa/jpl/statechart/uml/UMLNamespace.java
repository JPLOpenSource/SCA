/**
 * Created prior to July 13, 2009.
 * <p>
 * Copyright 2009-2010, by the California Institute of Technology. ALL RIGHTS
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
import java.util.Collections;
import java.util.Set;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A namespace is an element in a model that contains a set of named elements
 * that can be identified by name.
 * <p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public class UMLNamespace extends UMLNamedElement implements Namespace {

    protected Collection<NamedElement> ownedMember = Util.newList();
    protected Collection<Constraint> ownedRule = Util.newList();

    // protected Collection<ElementImport> elementImport = Util.newList();
    // protected Collection<PackageImport> packageImport = Util.newList();

    public UMLNamespace (Node element, ModelScape scape) {
        super(element, scape);

        // Get all the owned elements
        try {
            // get only the machines, both element and behavior in the current node
            NodeList stateMachines = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_MEMBER_ELEMENT, UMLLabel.TYPE_STATEMACHINE)
                    + " | " + UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_MEMBER_BEHAVIOR, UMLLabel.TYPE_STATEMACHINE),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < stateMachines.getLength(); i++) {
                Node smNode = stateMachines.item(i);
                if (modelScape.shouldLoadMachine(smNode)) {  // process the SM
                    ownedMember.add(new UMLStateMachine(smNode, scape));
                }
            }

            NodeList signals = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_MEMBER_ELEMENT, UMLLabel.TYPE_SIGNAL),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < signals.getLength(); i++) {
                // instantiate all, since can't know when one is fired!
                ownedMember.add(new UMLSignal(signals.item(i), scape));
            }

            NodeList anyRecvEvents = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_MEMBER_ELEMENT, UMLLabel.TYPE_ANY_RECEIVE_EVENT),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < anyRecvEvents.getLength(); i++) {
                ownedMember.add(new UMLAnyReceiveEvent(anyRecvEvents.item(i), scape));
            }

            // find all owned constraints
            NodeList constraints = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_MEMBER_RULE, UMLLabel.TYPE_CONSTRAINT),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < constraints.getLength(); i++) {
                ownedRule.add(new UMLConstraint(constraints.item(i), scape));
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLNamespace constructor: ");
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Namespace#getOwnedMember()
     */
    public Collection<NamedElement> getOwnedMember () {
        return Collections.unmodifiableCollection(ownedMember);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Namespace#getOwnedRule()
     */
    public Collection<Constraint> getOwnedRule () {
        return Collections.unmodifiableCollection(ownedRule);
    }

    /**
     * TODO right implementation for {@link UMLNamespace#getNamesOfMember(NamedElement)}?
     * 
     * Gets all the name spaces accessible from this namespace, searches within
     * those namespaces for the supplied element, and return all the qualified names.
     */
    public Set<String> getNamesOfMember (NamedElement element) {
        Set<String> set = Util.newSet();
        // at least add its qualified name from the default namespace
        set.add(element.getQualifiedName());
        for (Namespace ns : allNamespaces()) {
            for (Element e : ns.allOwnedElements()) {
                if (e instanceof NamedElement && e.equals(element)) {
                    set.add(((NamedElement) e).getQualifiedName());
                }
            }
        }
        return set;
    }

}
