/**
 * An event is the specification of some occurrence that may potentially trigger
 * effects by an object.
 */
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.model.ModelScape;

import org.w3c.dom.Node;

// Actually extends PackageableElement, but we don't support that.
public class UMLEvent extends UMLNamedElement implements Event {

    public UMLEvent (Node element, ModelScape scape) {
        super(element, scape);
    }
}
