package gov.nasa.jpl.statechart.uml;

import java.util.*;

public interface Vertex extends NamedElement {

    public StateMachine getContainingStatemachine ();

    public Collection<Transition> getIncoming ();

    public Collection<Transition> getOutgoing ();

    public Region getContainer ();

    /**
     * NON-UML Standard!
     * <p>
     * Convenient query method to get the parent UML State, if any.
     * </p>
     * @return Parent UML State
     */
    public State getParentState ();

}
