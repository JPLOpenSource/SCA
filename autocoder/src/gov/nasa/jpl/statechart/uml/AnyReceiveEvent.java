/**
 * A transition trigger associated with AnyReceiveEvent specifies that the
 * transition is to be triggered by the receipt of any message that is not
 * explicitly referenced in another transition from the same vertex.
 */
package gov.nasa.jpl.statechart.uml;

public interface AnyReceiveEvent extends MessageEvent {
}
