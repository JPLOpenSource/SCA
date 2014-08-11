package gov.nasa.jpl.statechart.uml;

import java.util.Collection;

public interface State extends Namespace, Vertex {

    public boolean isSimple ();

    public boolean isComposite ();

    public boolean isOrthogonal ();

    public boolean isSubmachineState ();

    public StateMachine getSubmachine ();

    public StateMachine getContainingStatemachine ();

    public Collection<Region> getRegion ();

    public Behavior getEntry ();

    public Behavior getExit ();

    public Behavior getDo ();

    /**
     * Returns a collection of {@link ConnectionPointReference}s defined on
     * this (presumably) Submachine state.
     * 
     * @return {@link Collection} of {@link ConnectionPointReference}s.
     */
    public Collection<ConnectionPointReference> getConnection ();

    /**
     * Returns a collection of connection point {@link Pseudostate}s defined
     * on this (presumably) Composite state.
     *  
     * @return {@link Collection} of {@link Pseudostate}s.
     */
    public Collection<Pseudostate> getConnectionPoint ();

}
