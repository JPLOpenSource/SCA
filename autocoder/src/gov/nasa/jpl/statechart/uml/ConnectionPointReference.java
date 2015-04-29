/**
 * A connection point reference represents a usage (as part of a submachine
 * state) of an entry/exit point defined in the statemachine reference by the
 * submachine state.
 * 
 * Connection point references of a submachine state can be used as
 * sources/targets of transitions. They represent entries into or exits out of
 * the submachine state machine referenced by the submachine state.
 */
package gov.nasa.jpl.statechart.uml;

import java.util.List;

public interface ConnectionPointReference extends Vertex {

    /**
     * Returns the list of entryPoint Pseudostates referenced.
     * 
     * @return {@link List} of referenced entryPoint {@link Pseudostate}s.
     */
    public List<Pseudostate> getEntry ();

    /**
     * Returns the list of exitPoint Pseudostates referenced.
     * 
     * @return {@link List} of referenced exitPoint {@link Pseudostate}s.
     */
    public List<Pseudostate> getExit ();

    /**
     * Returns the (supposed) Submachine State on which this
     * ConnectionPointReference is defined.
     * 
     * @return UML {@link State} on which this {@link ConnectionPointReference}
     * is defined.
     */
    public State getState ();

    /**
     * NON-UML Standard:  using {@link PseudostateKind}, returns whether this
     * ConnectionPointReference is an entry or exit type.
     * 
     * @return {@link PseudostateKind} of the referenced connectionPoint
     */
    public PseudostateKind getKind();
}
