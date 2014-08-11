/**
 * A classifier can have behavior specifications defined in its namespace. One
 * of these may specify the behavior of the classifier itself.
 */
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.model.ModelScape;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UMLBehavioredClassifier extends UMLClassifier implements BehavioredClassifier {

    private List<Behavior> ownedBehavior = Util.newList();

    public UMLBehavioredClassifier (Node element, ModelScape scape) {
        super(element, scape);

        // Get all the owned elements
        try {
            NodeList stateMachines = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_MEMBER_BEHAVIOR, UMLLabel.TYPE_STATEMACHINE),
                    element, XPathConstants.NODESET);

            // Instantiate all the classes (inner State Machines)
            for (int i = 0; i < stateMachines.getLength(); i++) {
                Node smNode = stateMachines.item(i);
                if (modelScape.shouldLoadMachine(smNode)) {  // process this SM
                    ownedBehavior.add(new UMLStateMachine(smNode, scape));
                }
            }

            // Add this as the context
            for (Behavior behavior : ownedBehavior) {
                behavior.setContext(this);
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLBehavioredClassifer constructor: ");
        }

        // Append the ownedBehaviors to the Namespace::ownedMember list
        ownedMember.addAll(ownedBehavior);
    }

    public Collection<Behavior> getOwnedBehavior () {
        return Collections.unmodifiableCollection(ownedBehavior);
    }

}
