/**
 * A transition trigger associated with AnyReceiveEvent specifies that the
 * transition is to be triggered by the receipt of any message that is not
 * explicitly referenced in another transition from the same vertex.
 */
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.model.ModelScape;

import org.w3c.dom.Node;

public class UMLAnyReceiveEvent extends UMLMessageEvent implements
        AnyReceiveEvent {

    public UMLAnyReceiveEvent (Node element, ModelScape scape) {
        super(element, scape);
    }
}
