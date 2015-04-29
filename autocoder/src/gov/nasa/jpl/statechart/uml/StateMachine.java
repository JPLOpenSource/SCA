/**
 * A state machine owns one or more regions, which in turn own vertices and
 * transitions. The behaviored classifier context owning a state machine defines
 * which signal and call triggers are defined for the state machine, and which
 * attributes and operations are available in activities of the state machine.
 * Signal triggers and call triggers for the state machine are defined according
 * to the receptions and operations of this classifier.
 * 
 * As a kind of behavior, a state machine may have an associated behavioral
 * feature (specification) and be the method of this behavioral feature. In this
 * case the state machine specifies the behavior of this behavioral feature. The
 * parameters of the state machine in this case match the parameters of the
 * behavioral feature and provide the means for accessing (within the state
 * machine) the behavioral feature parameters.
 * 
 * A state machine without a context classifier may use triggers that are
 * independent of receptions or operations of a classifier, i.e., either just
 * signal triggers or call triggers based upon operation template parameters of
 * the (parameterized) statemachine.
 */
package gov.nasa.jpl.statechart.uml;

import java.util.Collection;

public interface StateMachine extends Behavior {

    public Collection<Region> getRegion ();

    public Collection<Pseudostate> getConnectionPoint ();

    public boolean ancestor (State s1, State s2);

    public Collection<Behavior> getOwnedBehavior ();

    /**
     * NON-UML Standard!  Returns the list of identifiers for friends classes
     * designated in the model (via SCAProfile).
     * @return
     */
    public String[] getFriends ();

    /**
     * NON-UML Standard!  Returns the list of identifiers for machine instances.
     * @return
     */
    public String[] getInstanceIds ();

}
