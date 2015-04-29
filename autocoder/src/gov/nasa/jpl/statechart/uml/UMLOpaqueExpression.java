package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.model.ModelScape;

import org.w3c.dom.Node;

public class UMLOpaqueExpression extends UMLValueSpecification implements OpaqueExpression {

    public UMLOpaqueExpression (Node element, ModelScape scape) {
        super(element, scape);

        stringValue = UMLIdentifiers.inst().value_getExprBody(this);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.UMLValueSpecification#isSupported()
     */
    @Override
    public boolean isSupported () {
        return true;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.UMLValueSpecification#isNull()
     */
    @Override
    public boolean isNull () {
        return (stringValue == null);
    }

    public String getBody () {
        return stringValue;
    }

    public String getLanguage () {
        return getAttribute(domElement, UMLLabel.KEY_LANGUAGE);
    }

}
