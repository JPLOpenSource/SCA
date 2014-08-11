/**
 * A signal event represents the receipt of an asynchronous signal instance. A
 * signal event may, for example, cause a state machine to trigger a transition.
 */
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.model.ModelScape;

import org.w3c.dom.Node;

public class UMLSignalEvent extends UMLMessageEvent implements SignalEvent {

    // NON-UML Standard: cache of Signal object
    private Signal cachedSignal = null;

    public UMLSignalEvent (Node element, ModelScape scape) {
        super(element, scape);

        // check if event declares an associated signal object!
        if (getSignal() == null && Util.isDebugLevel()) {
            Util.debug("Warning: SignalEvent does not designate a Signal! ID '" + id + "'");
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.UMLNamedElement#getName()
     */
    @Override
    public String getName () {
        Signal signal = getSignal();
        if (signal != null) {
            return signal.getName();
        } else {
            return super.getName();
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.SignalEvent#getSignal()
     */
    public Signal getSignal () {
        if (cachedSignal == null) {
            cachedSignal = (Signal) xmi2uml(getAttribute(domElement, UMLLabel.KEY_SIGNAL));

            if (cachedSignal == null) {  // attempt to get a remote ref signal
                cachedSignal = (Signal) href2uml(UMLIdentifiers.inst().signalEvent_getReferencedSignalId(domElement));
            }
        }

        return cachedSignal;
    }
}
