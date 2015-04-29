/**
 * Relocated Sep 29, 2009.
 * <p>
 * Copyright 2009, by the California Institute of Technology. ALL RIGHTS
 * RESERVED. United States Government Sponsorship acknowledged. Any commercial
 * use must be negotiated with the Office of Technology Transfer at the
 * California Institute of Technology.
 * </p>
 * <p>
 * This software is subject to U.S. export control laws and regulations and has
 * been classified as 4D993. By accepting this software, the user agrees to
 * comply with all applicable U.S. export laws and regulations. User has the
 * responsibility to obtain export licenses, or other export authority as may be
 * required before exporting such information to foreign countries or providing
 * access to foreign persons.
 * </p>
 */
package gov.nasa.jpl.statechart.model.visitor;

import gov.nasa.jpl.statechart.uml.FinalState;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;

/**
 * The visitor interface is used for traversals of the states in a State
 * Machine.  Typically, we will use this to select a subset of the
 * states.
 */
public interface Visitor {
    // Checks/sets node active visiting status to prevent infinite recursion.
    public boolean isVisiting (NamedElement ne);
    public void setVisiting (NamedElement ne, boolean flag);

    // We can encounter all sorts of elements during the
    // traversal and all may be interesting
    public void visit (StateMachine stateMachine);

    public void visit (Region region);

    public void visit (State state);

    public void visit (FinalState finalState);

    public void visit (Pseudostate pseudostate);

    /**
     * The visitor may be interested in the transitions up and down through
     * the class hierarchy.
     * The Namespace objects may be States, Regions, or StateMachines.
     * <br/><br/>
     * Implementing subclasses should always call this super method to properly
     * register a node as being actively visited.
     */
    public void moveDown (NamedElement from, NamedElement to);

    /**
     * Implementing subclasses should always call this super method to properly
     * register a node as no longer being actively visited.
     */
    public void moveUp (NamedElement from, NamedElement to);

    public int maxDepth ();

    /**
     * Returns flag indicating whether to descend into Submachine nodes.
     * @return <code>true</code> if children elements of submachines should be
     *      visited, <code>false</code> otherwise.
     */
    public boolean expandSubmachines ();

    /**
     * Returns flag indicating whether to go into orthogonal regions to visit
     * children elements.
     * @return <code>true</code> if children elements of orthogonal regions
     *      should be included, <code>false</code> otherwise.
     */
    public boolean expandOrthogonalRegions ();

    /**
     * Returns flag indicating whether to following outgoing Transitions when
     * traversing the model.  This is used for walking a cut of the model from
     * a Vertex, in particular, TransitionPathVisitor.
     * 
     * @return <code>true</code> if transitions from a Vertex should be followed;
     *      <code>false</code> otherwise. The default is <code>false</code>.
     */
    public boolean followOutTransitions ();

}
