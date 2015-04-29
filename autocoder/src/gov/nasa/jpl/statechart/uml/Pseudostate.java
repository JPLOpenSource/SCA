package gov.nasa.jpl.statechart.uml;


public interface Pseudostate extends Vertex {

    /**
     * Returns the StateMachine on which this connectionPoint Pseudostate
     * is defined, if any.
     * 
     * @return if applicable, the {@link StateMachine} on which is defined
     * this connectionPoint Pseudostate; otherwise, <code>null</code>.
     */
    public StateMachine getStatemachine ();

    /**
     * Returns the Composite State on which this connectionPoint Pseudostate
     * is defined, if any.
     * 
     * @return if applicable, the Composite {@link State} on which is defined
     * this connectionPoint Pseudostate; otherwise, <code>null</code>.
     */
    public State getState ();

    /**
     * Returns the kind of Pseudostate.
     * 
     * @return the {@link PseudostateKind} identifying the kind of Pseudostate.
     */
    public PseudostateKind getKind ();
}
