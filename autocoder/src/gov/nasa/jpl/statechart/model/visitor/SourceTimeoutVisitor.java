/**
 * Created Sep 22, 2010.
 * <p>
 * Copyright 2009-2010, by the California Institute of Technology. ALL RIGHTS
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
import gov.nasa.jpl.statechart.uml.LiteralString;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.PseudostateKind;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.TimeEvent;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.ValueSpecification;
import gov.nasa.jpl.statechart.uml.Vertex;

import java.util.Collection;
import java.util.Map;

/**
 * This visitor constructs a map of source vertices to its effective timeout.
 * A effective timeout for a source vertex V is the accumulated timeout,
 * along the transition path, from the initial Pseudostate to V, plus the
 * timeout value on the outgoing TimeEvent transition from V, if any.
 * This map structure facilitate the analysis of whether to exclude a time
 * out event transition when stamping out Promela code.
 * <p>
 * The algorithm relies on the model walker to visit an initial node before
 * any other vertices within any given Region.  With that given, let TE_V
 * be the effective timeout value at vertex V:
 * <ol>
 * <li> When an initial pseudostate is visited, find its target vertex V and
 * initialize the effective timeout value at V, TE_V, to zero.
 * <li> For each vertex V visited, find all target vertices VT reachable in one
 * out-transition hop.
 * <li> If the out-transition to VT is triggered by a TimeEvent of timeout X,
 * let Y = TE_V + X and set TE_V to Y.
 * If TE_VT > 0 and Y < TE_VT,
 * then set Y as the new effective timeout value at VT;
 * otherwise, leave TE_VT untouched.
 * <li> If the out-transition to VT is not a TimeEvent transition, and 
 * TE_VT is unset, then set TE_VT to zero.
 * <li> After all vertices have been visited, the resulting map will help answer
 * the question:  <em>From any vertex V, what is the minimum number of ticks
 * needed to transition out of V within V's region?</em>
 * </ol>
 * </p><p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public class SourceTimeoutVisitor extends AbstractVisitor<Void> {
    private static final long serialVersionUID = -5798534418357562916L;

    private Map<Vertex,Double> sourceTimeoutMap = null;

    /**
     * Default constructor, but invokes parent constructor
     * {@link AbstractVisitor#AbstractVisitor(boolean, OrthoRegion)}
     * with arguments <code>super(false, {@link OrthoRegion#INCLUDE_BELOW_ORTHO})</code>
     * to ensure looking inside orthogonal regions, but prevent descending
     * into SubMachines.
     */
    public SourceTimeoutVisitor () {
        super(false, OrthoRegion.INCLUDE_BELOW_ORTHO);

        sourceTimeoutMap = Util.newMap();
    }

    /**
     * Returns the map of source vertex to effective timeout value.
     * @return
     */
    public Map<Vertex,Double> getMap () {
        return sourceTimeoutMap;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.AbstractVisitor#visit(gov.nasa.jpl.statechart.uml.Pseudostate)
     */
    @Override
    public void visit (Pseudostate pseudo) {
        if (pseudo.getKind() == PseudostateKind.initial) {
            // Make sure target of initial Pseudostate gets traversed before other states
            for (Transition t : pseudo.getOutgoing()) {
                if (t.getTarget() != null) {
                    sourceTimeoutMap.put(t.getTarget(), null);
                }
            }
        } else {
            updateTargetTimeouts(pseudo);
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.AbstractVisitor#visit(gov.nasa.jpl.statechart.uml.State)
     */
    @Override
    public void visit (State state) {
        updateTargetTimeouts(state);
    }

    private void updateTargetTimeouts (Vertex v) {
        double teV = 0.0;
        Double tmObj = sourceTimeoutMap.get(v);
        if (tmObj != null) {  // initialize to zero to prevent NPE
            teV = tmObj;
        }

        // 1. Find all target vertices VT reachable in one out-transition hop
        for (Transition transition : v.getOutgoing()) {
            if (transition.getTarget() != null) {
                Vertex vt = transition.getTarget();
                Double teVTObj = sourceTimeoutMap.get(vt);
                if (teVTObj != null) {
                    teV = teVTObj.doubleValue();
                }
                Collection<TimeEvent> timeEvents = transition.getTimeEvents();
                if (timeEvents.size() > 0) {
                    // 2. TimeEvent-triggered transition... 
                    for (TimeEvent timerEv : timeEvents) {  // should be only one TimeEvent
                        //- timeout value X, try String or Real
                        double timeout = 0.0;
                        ValueSpecification valSpec = ((TimeEvent) timerEv).getWhen();
                        if (valSpec instanceof LiteralString) {
                            timeout = Double.parseDouble(valSpec.stringValue());
                        } else {  // assume real?!
                            timeout = valSpec.realValue();
                        }
                        //- let Y = TE_V + X and set TE_V to Y
                        double y = teV + timeout;
                        sourceTimeoutMap.put(v, y);
                        //- if TE_VT > 0 and Y < TE_VT
                        if (teVTObj == null || teVTObj == 0 || y < teVTObj) {
                            //- set Y as the new effective timeout value at VT
                            sourceTimeoutMap.put(vt, y);
                        }
                    }
                } else {
                    // 4. Transition not triggered by TimeEvent...
                    if (teVTObj == null) {  // TE_VT is unset
                        // null out the effective time out at target
                        sourceTimeoutMap.put(vt, null);
                    }
                }
            }
        }
    }

}
