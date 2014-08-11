/**
 * A message event specifies the receipt by an object of either a call or a
 * signal. MessageEvent is an abstract metaclass.
 */
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.model.ModelScape;

import org.w3c.dom.Node;

// Actually extends PackageableElement, but we don't support that.
public class UMLMessageEvent extends UMLEvent implements MessageEvent {

    public UMLMessageEvent (Node element, ModelScape scape) {
        super(element, scape);
    }
}
