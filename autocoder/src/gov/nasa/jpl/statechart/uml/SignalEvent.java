/**
 * A signal event represents the receipt of an asynchronous signal instance. A
 * signal event may, for example, cause a state machine to trigger a transition.
 */
package gov.nasa.jpl.statechart.uml;

public interface SignalEvent extends MessageEvent {

    public Signal getSignal ();
}
