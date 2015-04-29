/**
 * Created Sep 29, 2009.
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
package gov.nasa.jpl.statechart.template;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.model.BreadthFirstWalker;
import gov.nasa.jpl.statechart.model.PrefixOrderedWalker;
import gov.nasa.jpl.statechart.model.visitor.ConnectionPointOOVisitor;
import gov.nasa.jpl.statechart.model.visitor.HistoryStateOOVisitor;
import gov.nasa.jpl.statechart.model.visitor.StateVisitor;
import gov.nasa.jpl.statechart.model.visitor.AbstractVisitor.OrthoRegion;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Namespace;
import gov.nasa.jpl.statechart.uml.Package;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.PseudostateKind;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;

import java.util.Collection;
import java.util.Collections;

/**
 * This class extends the base velocity model class with OO-specific methods
 * for querying the UML Model.  The main difference has to do with provision
 * for inheritance, which does not require qualified names.  Visitors
 * used in this class generally should have the orthogonal region option set
 * to {@link OrthoRegion#STOP_AT_ORTHO},
 * and descent into submachines should generally be <code>false</code>.
 * <p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class OOVelocityModel extends AbstractVelocityModel {

    // cache of StateMachine connection points found
    private Collection<Pseudostate> sortedConnPts = null;


    /**
     * Main constructor to initialize the Velocity Model.
     * 
     * @param myStateMachine  the state machine with which to construct this model
     */
    public OOVelocityModel (StateMachine myStateMachine) {
        super();

        // Initialize the variables
        setStateMachine(myStateMachine);
    }

    /**
     * Gets descendant states <i>without</i> including orthogonal regions,
     * self included if a State. 
     * 
     * @see gov.nasa.jpl.statechart.template.AbstractVelocityModel#getStates(gov.nasa.jpl.statechart.uml.Namespace, boolean)
     */
    @Override
    public Collection<State> getStates (Namespace ns, boolean descend) {
        return super.getStates(ns, descend, OrthoRegion.STOP_AT_ORTHO);
    }

    /**
     * Gets all the states in a breadth-first ordering.
     */
    public Collection<State> getStatesBreadthFirst (Namespace ns, boolean descend) {
        return BreadthFirstWalker.traverse(ns, new StateVisitor(descend));
    }

    /**
     * Gets sorted list of descendant ConnectionPoint Pseudostates, i.e.
     * entry-/exitPoints, <i>without</i> descending into orthogonal regions.
     * If a StateMachine, then only look at the immediate child region.
     * 
     * @param ns  Namespace within which to search for ConnectionPoints.
     * @param descend  flag indicating whether to descend into submachines.
     * @return  {@link Collection} of entry-/exitPoint {@link Pseudostate}s.
     */
    public Collection<Pseudostate> getConnectionPoints (Namespace ns, boolean descend) {
        Collection<Pseudostate> sortedVs = null;
        if (ns == getStateMachine() && sortedConnPts != null) {  // returned cache
            sortedVs = sortedConnPts;
        }

        if (sortedVs == null) {
            if (ns instanceof StateMachine) {
                sortedVs = Util.newSortedSet();
                for (Pseudostate pseudostate : getStateMachine().getConnectionPoint()) {
                    if (pseudostate.getKind() == PseudostateKind.entryPoint
                            || pseudostate.getKind() == PseudostateKind.exitPoint) {
                        sortedVs.add(pseudostate);
                    }
                }
            } else {
                // visit model and retrieve descendant ConnectionPoints
                // TODO only one-level down?!
                sortedVs = sort(PrefixOrderedWalker.traverse(ns, new ConnectionPointOOVisitor(descend)));
            }
        }
        if (ns == getStateMachine() && sortedConnPts == null) {  // cache the set
            sortedConnPts = sortedVs;
        }

        return Collections.unmodifiableCollection(sortedVs);
    }

    /**
     * Returns whether this StateMachine has any entryPoint ConnectionPoint
     * defined, whether it be on its "border" or on descendant Composite States.
     * 
     * @return  <code>true</code> if StateMachine has at least one entryPoint,
     * <code>false</code> otherwise.
     */
    public boolean hasMachineEntryPoint () {
        boolean rv = false;
        for (Pseudostate pseudo : getConnectionPoints(getStateMachine(), false)) {
            if (pseudo.getKind() == PseudostateKind.entryPoint) {
                rv = true;
                break;
            }
        }
        return rv;
    }

    /**
     * Returns whether this StateMachine has any exitPoint ConnectionPoint
     * defined, whether it be on its "border" or on descendant Composite States.
     * 
     * @return  <code>true</code> if StateMachine has at least one exitPoint,
     * <code>false</code> otherwise.
     */
    public boolean hasMachineExitPoint () {
        boolean rv = false;
        for (Pseudostate pseudo : getConnectionPoints(getStateMachine(), false)) {
            if (pseudo.getKind() == PseudostateKind.exitPoint) {
                rv = true;
                break;
            }
        }
        return rv;
    }

    /**
     * Returns whether SubMachine state has entryPoint ConnectionPoint defined,
     * which is useful to determine if we're making a default transition into
     * it, to unset the entry point.
     * 
     * @return  <code>true</code> if SubMachine state has at least one entryPoint,
     * <code>false</code> otherwise.
     */
    public boolean hasSubMachineEntryPoint (State state) {
        boolean rv = false;
        if (state.getSubmachine() != null) {
            for (Pseudostate pseudo : state.getSubmachine().getConnectionPoint()) {
                if (pseudo.getKind() == PseudostateKind.entryPoint) {
                    rv = true;
                    break;
                }
            }
        }
        return rv;
    }

    /**
     * Gets history states from this starting Namespace element.
     */
    public Collection<Pseudostate> getOOHistoryStates (Namespace ns) {
        return PrefixOrderedWalker.traverse(ns, new HistoryStateOOVisitor());
    }

    /**
     * Returns the parent state, or the machine if <code>ne</code> is an orthogonal region.
     * @return the parent State element, stopping at an orthogonal region.
     */
    public NamedElement getParentStateWithinOrthogonal (NamedElement ne) {
        NamedElement parent = null;
        // loop invariant:  terminate when ne == null
        while (ne != null) {
            parent = ne.getParent();
            if (parent instanceof State) {
                if (((State) parent).isOrthogonal()) {  // no good!
                    parent = null;
                }
                break;
            } else if (parent instanceof StateMachine) {
                break;
            } else if (parent instanceof Package) {
                // somehow we've skipped StateMachine and gotten to a package!
                Util.error("ERROR! Unexpectedly encountered package '"
                        + ((Package )parent).getName() + "' traversing ancestor chain of '"
                        + ne.getName() + "'without reaching the containing StateMachine!");
                parent = null;
                break;
            }
            ne = parent;
            // null out parent to prevent returning a wrong element
            parent = null;
        }
        return (parent == null) ? getStateMachine() : parent;
    }

}
