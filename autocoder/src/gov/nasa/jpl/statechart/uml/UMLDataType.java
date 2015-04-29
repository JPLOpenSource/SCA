package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.model.ModelScape;

import java.util.Collection;

import org.w3c.dom.Node;

public class UMLDataType extends UMLClassifier implements DataType {

    protected Collection<Property> ownedAttribute = Util.newList();
    protected Collection<Operation> ownedOperation = Util.newList();  // currently unused

    public UMLDataType (Node element, ModelScape scape) {
        super(element, scape);

        ownedAttribute.addAll(attribute);
    }

}
