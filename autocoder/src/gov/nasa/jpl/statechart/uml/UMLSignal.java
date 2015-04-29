/**
 * A signal is a specification of send request instances communicated between
 * objects. The receiving object handles the received request instances as
 * specified by its receptions. The data carried by a send request (which was
 * passed to it by the send invocation occurrence that caused that request) are
 * represented as attributes of the signal. A signal is defined independently of
 * the classifiers handling the signal occurrence.
 */
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.model.ModelScape;

import org.w3c.dom.Node;

public class UMLSignal extends UMLClassifier implements Signal {

    public UMLSignal (Node element, ModelScape scape) {
        super(element, scape);
    }
}
