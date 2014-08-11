package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.model.ModelScape;

import org.w3c.dom.Node;

public class UMLActivity extends UMLBehavior implements Activity {

    public UMLActivity (Node element, ModelScape scape) {
        super(element, scape);
    }

    public boolean isReadOnly () {
        return false;
    }

}
