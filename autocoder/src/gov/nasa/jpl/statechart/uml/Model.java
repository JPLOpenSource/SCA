package gov.nasa.jpl.statechart.uml;

import java.util.Collection;

public interface Model extends Namespace {

    /**
     * Returns the top-level state machines from this model.
     * The top-level state machine is the controlling state machine.  Any
     * other state machines in the model are assumed to be referenced as
     * submachines of the top-level state machine.
     */
    public Collection<StateMachine> getStateMachines ();

    /**
     * Return whether Model manifests an exception.
     * @return  <code>true</code> if Model has an exception, <code>false</code> otherwise.
     */
    public boolean hasException ();

}
