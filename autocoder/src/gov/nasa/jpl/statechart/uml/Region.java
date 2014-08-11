package gov.nasa.jpl.statechart.uml;

import java.util.Collection;

public interface Region extends Namespace {

    public StateMachine getContainingStatemachine ();

    public Collection<Transition> getTransition ();

    public Collection<Vertex> getSubvertex ();

    public State getState ();

    public StateMachine getStatemachine ();

    /**
     * Returns whether this region contains any deep history state(s).
     * @return <code>true</code> if one or more deep history states exist in 
     * this region, <code>false</code> otherwise.
     */
    public boolean containsHistoryState ();

    /**
     * Returns a collection of history states within this region.
     * @return Set of Pseudostate that represent deep history states.
     */
    public Collection<Pseudostate> getHistoryState ();
}
