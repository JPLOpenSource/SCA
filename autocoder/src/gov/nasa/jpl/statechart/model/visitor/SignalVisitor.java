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

import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Namespace;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.Signal;
import gov.nasa.jpl.statechart.uml.SignalEvent;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.Trigger;
import gov.nasa.jpl.statechart.uml.Vertex;

/**
 * Extract the signals
 */
public class SignalVisitor extends TransitionTriggerVisitor<Signal> {
    private static final long serialVersionUID = 8989603881676489586L;

    public SignalVisitor (boolean descend) {
        super(descend);
    }

    @Override
    public void visit (StateMachine stateMachine) {
        doVisit(stateMachine);
    }

    @Override
    public void visit (Region region) {
        doVisit(region);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.TransitionTriggerVisitor#doTransitionTrigger(gov.nasa.jpl.statechart.uml.Vertex, gov.nasa.jpl.statechart.uml.Transition, gov.nasa.jpl.statechart.uml.Trigger)
     */
    @Override
    protected void doTransitionTrigger (Vertex v, Transition transition, Trigger trigger) {
        if (trigger.getEvent() instanceof SignalEvent) {
            SignalEvent sigEv = (SignalEvent) trigger.getEvent();
            Signal sig = sigEv.getSignal();
            // only add signals if not already found
            if (sig != null && !contains(sig)) {
                add(sig);
            }
        }
    }

    private void doVisit (Namespace ns) {
        // add all owned members that are signals
        for (NamedElement elem : ns.getOwnedMember()) { 
            if (elem instanceof Signal) {
                add((Signal) elem);
            }
        }
    }

}
