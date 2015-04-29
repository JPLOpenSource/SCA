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

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.uml.FinalState;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Extract all the Namespaces that need to be generated.  This visitor
 * implements a Map interface and gathers Expanded name/UML Element
 * pairs. An expanded name is a unique path to each State in a state
 * machine, by the namespace changes when a submachine is expanded.
 *
 * An expanded names is equal to the qualified name with the qualified
 * name of the enclosing state machine removed.. e.g., 
 *
 *   state qname = Model::StateMachine::Region1::State
 *   state machine qname = Model::StateMachine
 *
 *   --> expanded name = Region1::State
 *
 * States that are found by descending into submachine are likewise
 * appended. e.g.
 *
 *   submachine state expanded name = Region1::SubmachineState
 *   state = Region2:State
 *
 * expanded name -> Region1::SubmachineState::Region2::State
 */
public class StateNameVisitor extends LinkedHashMap<String, NamedElement>
        implements Visitor {
    private static final long serialVersionUID = 8177476868531143139L;

    /** HashMap for tracking whether a node is currently being visited.
     *  A node that is being descended into should be set true, and then
     *  set back to false when ascending from that node.  This prevents
     *  (and could be used to help identify) infinite recursion. */
    private Map<NamedElement,Boolean> visitingMap = null;
    /** Keep a copy of the current state machine QName to avoid repeated
     *  XPath evaluations. */
    private String stateMachineQName;

    // Keep a list of QName prefixes that must be concatenated
    private List<String> prefix = Util.newList();

    public StateNameVisitor (StateMachine stateMachine) {
        this.stateMachineQName = stateMachine.getQualifiedName();
        this.visitingMap = Util.newMap();
    }

    /**
     * Returns whether a NamedElement node has been visited by this visitor.
     * 
     * @param ne  UML {@link NamedElement} node to check
     * @return  <code>true</code> if node has been visited; <code>false</code> otherwise.
     */
    public boolean isVisiting (NamedElement ne) {
        boolean rv = false;
        if (visitingMap.containsKey(ne)) {
            rv = visitingMap.get(ne);
        }  // otherwise, rv stays false
        return rv;
    }

    /**
     * Sets the visited status of the supplied NamedElement.
     * @param ne    UML {@link NamedElement} node to set visit status.
     * @param flag  boolean flag to set as new visited status.
     */
    public void setVisiting (NamedElement ne, boolean flag) {
        visitingMap.put(ne, flag);
    }


    public void visit (StateMachine stateMachine) {
        String name = stateMachine.getName();

        if (name != null && name.length() > 0) {
            put(getExpandedName(stateMachine), stateMachine);
        }
    }

    public void visit (Region region) {
        String name = region.getName();

        if (name != null && name.length() > 0) {
            put(getExpandedName(region), region);
        }
    }

    public void visit (State state) {
        String name = state.getName();

        if (name != null && name.length() > 0) {
            put(getExpandedName(state), state);
        }
    }

    public void visit (FinalState state) {
        visit((State) state);
    }

    public void visit (Pseudostate state) {
        String name = state.getName();

        if (name != null && name.length() > 0) {
            put(getExpandedName(state), state);
        }
    }

    public void moveDown (NamedElement from, NamedElement to) {
        // Moving into a submachine so reset the prefix QName and
        // append the expanded name of the current state into the
        // prefix list
        if (to instanceof StateMachine) {
            // This order is important. We can't change the state machine
            // QName before expandig the "from" node.
            prefix.add(getExpandedName(from));
            stateMachineQName = to.getQualifiedName();
        }

        // Descending into "to" element, considered to be actively visiting
        // the "from" node.
        setVisiting(from, true);
    }

    public void moveUp (NamedElement from, NamedElement to) {
        // Pop the prefix list and reset the QName
        if (from instanceof StateMachine) {
            prefix.remove(prefix.size() - 1);
            stateMachineQName = ((State) to).getContainingStatemachine().getQualifiedName();
        }

        // Ascending from "from" element, no longer actively visiting "to" node.
        setVisiting(to, false);
    }

    public int maxDepth () {
        return Integer.MAX_VALUE;
    }

    public boolean expandSubmachines () {
        return true;
    }

    public boolean expandOrthogonalRegions () {
        return true;
    }

    public boolean followOutTransitions () {
        return false;
    }

    private String getExpandedName (NamedElement ns) {
        String qname = ns.getQualifiedName();

        if (!qname.startsWith(stateMachineQName))
            throw new RuntimeException(
                    "Namespace does not match state machine prefix: "
                            + qname + ", " + stateMachineQName);

        return qname;

        /*
        // Get the suffix and append it with the prefixes on the list
        int    pos    = stateMachineQName.length() + ns.separator().length();
        String suffix = qname.substring( pos );

        String str = Util.join( prefix, ns.separator() );

        if ( str.length() > 0 )
           str += ns.separator();

        return str + suffix;
        */
    }
}