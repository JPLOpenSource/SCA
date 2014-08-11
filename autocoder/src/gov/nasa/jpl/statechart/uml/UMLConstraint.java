package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.model.ModelScape;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;


public class UMLConstraint extends UMLNamedElement implements Constraint {

    // Constant to represent the ELSE constraint in State Machine modeling
    public static final UMLConstraint ELSE_CONSTRAINT = new UMLConstraint(null, null);


    private ValueSpecification specification = null;
    
    public UMLConstraint (Node element, ModelScape scape) {
        super(element, scape);

        if (element != null) {
            try {
                // load a value specification
                Node node = (Node) xpath.evaluate(
                        getValueSpecificationXpath(UMLLabel.TAG_SPECIFICATION),
                        element, XPathConstants.NODE);
                if (node != null) {
                    specification = getValueSpecificationSubtype(node);
                }
            } catch (XPathExpressionException e) {
                Util.reportException(e, "UMLConstraint constructor: ");
            }
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Constraint#getSpecification()
     */
    public ValueSpecification getSpecification () {
        return specification;
    }

}
