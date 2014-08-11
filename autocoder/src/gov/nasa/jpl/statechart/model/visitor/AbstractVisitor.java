/**
 * Relocated Sep 29, 2009.
 * Incorporated NotBelowOrthogonalVisitor functionality Sep 15, 2010.
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

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.model.BreadthFirstWalker;
import gov.nasa.jpl.statechart.model.PrefixOrderedWalker;
import gov.nasa.jpl.statechart.uml.FinalState;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;

import java.util.ArrayList;
import java.util.Map;

/**
 * Default visitor implementation that does not do any visiting. but inherits
 * from List<T>, so that it can be returned directly.
 * <p>
 * To facilitate choosing whether to descend below Sub-machines as well as
 * orthogonal regions, this abstract base class tracks two internal state
 * variables configurable at construction time.  Those two states are
 * accessible to walkers via {@link #expandOrthogonalRegions()} and
 * {@link #expandSubmachines()}.  Walkers algorithms must make use of these
 * methods to determine whether to descend into an orthogonal region or a
 * submachine, respectively.
 * </p><p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 * @param <T>  the Class of objects to be stored in the list structure.
 * @see PrefixOrderedWalker
 * @see BreadthFirstWalker
 */
public abstract class AbstractVisitor<T> extends ArrayList<T> implements Visitor {
    private static final long serialVersionUID = -246717107479030610L;

    public enum OrthoRegion {
        /** Option to include orthogonal regions and below. */
        INCLUDE_BELOW_ORTHO,
        /** Option to NOT go below orthogonal regions. */
        STOP_AT_ORTHO
    }

    /** HashMap for tracking whether a node is currently being visited.
     *  A node that is being descended into should be set true, and then
     *  set back to false when ascending from that node.  This prevents
     *  (and could be used to help identify) infinite recursion. */
    private Map<NamedElement,Boolean> visitingMap = null;
    /** Flag indicating whether to descend into submachines. */
    private final boolean descend;
    /** Flag indicating whether to visit below orthogonal region. */
    private final boolean includeOrtho;

    // internal store used to determine whether to exclude orthogonal region
    private NamedElement withinOrthogonalRegion = null;

    /**
     * Default constructor, sets up visitor to visit model without descending
     * into SubMachines.
     */
    public AbstractVisitor () {
        this(false, OrthoRegion.INCLUDE_BELOW_ORTHO);
    }

    /**
     * Main constructor, sets up visitor to visit model and descend into
     * SubMachines depending on the <code>descend</code> flag.
     * Elements below orthogonal regions will be included.
     *
     * @param descend  <code>true</code> to descend into SubMachines, <code>false</code> if not.
     */
    public AbstractVisitor (boolean descend) {
        this(descend, OrthoRegion.INCLUDE_BELOW_ORTHO);
    }

    /**
     * Full constructor, sets up visitor to visit model, to descend into
     * SubMachines depending on the <code>descend</code> flag, and to include
     * orthogonal regions depending on the <code>include</code> flag.
     * 
     * @param descend  <code>true</code> to descend into SubMachines, <code>false</code> if not.
     * @param include  <code>true</code> to include orthogonal regions, <code>false</code> if not.
     */
    public AbstractVisitor (boolean descend, OrthoRegion include) {
        this.descend = descend;
        this.includeOrtho = (include == OrthoRegion.INCLUDE_BELOW_ORTHO);
        this.visitingMap = Util.newMap();
        if (Util.isDebugLevel()) {
            Util.debug("Visitor: " + this.getClass().getSimpleName());
        }
    }

    /**
     * Returns whether the supplied Model NamedElement node is currently being
     * visited by this visitor.
     * 
     * @param ne  UML {@link NamedElement} node to check
     * @return  <code>true</code> if node is being visited; <code>false</code> otherwise.
     */
    public boolean isVisiting (NamedElement ne) {
        boolean rv = false;
        if (visitingMap.containsKey(ne)) {
            rv = visitingMap.get(ne);
        }  // otherwise, rv stays false
        return rv;
    }

    /**
     * Sets the active visiting status of the supplied NamedElement.
     * 
     * @param ne    UML {@link NamedElement} node to set visiting status.
     * @param flag  boolean flag to set as new active-visiting status.
     */
    public void setVisiting (NamedElement ne, boolean flag) {
        visitingMap.put(ne, flag);
    }


    public void visit (StateMachine stateMachine) {
    }

    public void visit (State state) {
    }

    public void visit (FinalState finalState) {
        // by default, treat finalState as regular state.
        visit((State) finalState);
    }

    public void visit (Region region) {
    }

    public void visit (Pseudostate pseudostate) {
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.Visitor#moveDown(gov.nasa.jpl.statechart.uml.NamedElement, gov.nasa.jpl.statechart.uml.NamedElement)
     */
    public void moveDown (NamedElement from, NamedElement to) {
        // Descending into "to" element, considered to be actively visiting
        // the "from" node.
        if (Util.isDebugLevel()) {
            Util.debug("Visitor.moveDown: from " + from.getQualifiedName() +
                    " to " + to.getQualifiedName());
        }
        setVisiting(from, true);

        if (from instanceof State && to instanceof Region) {
            if (withinOrthogonalRegion == null && ((State) from).isOrthogonal()) {
                // we're moving from a state down into an orthogonal region!
                withinOrthogonalRegion = from;
            }
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.Visitor#moveUp(gov.nasa.jpl.statechart.uml.NamedElement, gov.nasa.jpl.statechart.uml.NamedElement)
     */
    public void moveUp (NamedElement from, NamedElement to) {
        // Ascending from "from" element, no longer actively visiting "to" node.
        if (Util.isDebugLevel()) {
            Util.debug("Visitor.moveUp: back to " + to.getQualifiedName()
                    + " from " + from.getQualifiedName());
        }
        setVisiting(to, false);

        if (from instanceof Region && to instanceof State) {
            if (to == withinOrthogonalRegion) {
                // we're moving back up from an orthogonal region!
                withinOrthogonalRegion = null;
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.Visitor#maxDepth()
     */
    public int maxDepth () {
        return Integer.MAX_VALUE;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.Visitor#expandSubmachines()
     */
    public boolean expandSubmachines () {
        return descend;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.Visitor#expandOrthogonalRegions()
     */
    public boolean expandOrthogonalRegions () {
        // rv := withinOrthogonalRegion != null -> includeOrtho
        // This means that, if includeOrtho == true, then include them.
        // Otherwise, include if we're not already within an orthogonal region.
        return includeOrtho || withinOrthogonalRegion == null;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.Visitor#followOutTransitions()
     */
    public boolean followOutTransitions () {
        return false;
    }

    /**
     * Returns constructor argument flag for including orthogonal regions. 
     * @return
     */
    protected boolean includeOrtho () {
        return includeOrtho;
    }

    protected boolean addUnique (T o) {
        if (this.contains(o)) {
            return false;
        } else {
            return this.add(o);
        }
    }
}
