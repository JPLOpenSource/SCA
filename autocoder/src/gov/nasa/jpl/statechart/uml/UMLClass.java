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

public class UMLClass extends UMLClassifier implements Class {

    protected Collection<Property> ownedAttribute = Util.newList();
    protected Collection<Operation> ownedOperation = Util.newList();  // currently unused
    protected Collection<Classifier> nestedClassifier = Util.newList();

    public UMLClass (Node element, ModelScape scape) {
        super(element, scape);

        ownedAttribute.addAll(attribute);

        try {
            // Fetch any classes, if they contain statemachines (shouldLoadPath)
            NodeList classes = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_NESTED_CLASSIFIER, UMLLabel.TYPE_CLASS),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < classes.getLength(); i++) {
                Node clsNode = classes.item(i);
                if (modelScape.shouldLoadPath(clsNode)) {
                    Class c = new UMLClass(clsNode, scape);
                    ownedMember.add(c);
                    nestedClassifier.add(c);
                }
            }

            // Get all the uml:Signal nested classifiers
            NodeList signals = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_NESTED_CLASSIFIER, UMLLabel.TYPE_SIGNAL),
                    element, XPathConstants.NODESET);
            // instantiate all Signal elements (can't know when one is fired!)
            for (int i = 0; i < signals.getLength(); i++) {
                Signal signal = new UMLSignal(signals.item(i), scape);
                ownedMember.add(signal);
                nestedClassifier.add(signal);
            }

        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLClass constructor: ");
        }
    }

}
