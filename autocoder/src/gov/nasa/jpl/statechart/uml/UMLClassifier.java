/**
 * A classifier is a classification of instances, it describes a set of
 * instances that have features in common.
 */
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.model.ModelScape;

import java.util.Collection;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UMLClassifier extends UMLNamespace implements Classifier {

    protected Collection<Property> attribute = Util.newList();

    /* NON-UML Standard!
     * Cache of attributes by ID
     */
    private Map<String,Property> attributeById = null;

    public UMLClassifier (Node element, ModelScape scape) {
        super(element, scape);

        try {
            // Fetch owned attributes
            NodeList attrNodes = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_MEMBER_ATTRIBUTE, UMLLabel.TYPE_PROPERTY),
                    element, XPathConstants.NODESET);

            for (int i = 0; i < attrNodes.getLength(); i++) {
                UMLProperty prop = new UMLProperty(attrNodes.item(i), scape);
                attribute.add(prop);
                prop.setClassifier(this);
            }

        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLClassifier constructor: ");
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Classifier#getAttributesMap()
     */
    public Map<String,Property> getAttributesMap () {
        if (attributeById == null) {  // lazy init
            attributeById = Util.newSortedMap();

            for (Property p : attribute) {
                attributeById.put(p.id(), p);
            }
        }

        return attributeById;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Classifier#getAttribute(java.lang.String)
     */
    public Property getAttribute (String name) {
        Property retProp = null;  // default: not found

        for (Property p : attribute) {
            if (p.getName().equalsIgnoreCase(name)) {
                retProp = p;
                break;
            }
        }

        return retProp;
    }

    /**
     * Convenience function to query an attribute's name by ID.
     * 
     * @param id  XMI ID of Attribute to lookup
     * @return  String name of the Attribute
     */
    protected String getAttributeName (String id) {
        return getAttributesMap().get(id).getName();
    }

}
