/**
 * A trigger specifies an event that may cause the execution of an associated
 * behavior. An event is often ultimately caused by the execution of an action,
 * but need not be.
 */
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.model.ModelScape;

import org.w3c.dom.Node;

public class UMLTrigger extends UMLNamedElement implements Trigger {

    public UMLTrigger (Node element, ModelScape scape) {
        super(element, scape);
    }

    public Event getEvent () {
        return (Event) xmi2uml(getAttribute(domElement, UMLLabel.KEY_EVENT));
    }
}
