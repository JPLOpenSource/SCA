/**
 * A transition is a directed relationship between a source vertex and a target
 * vertex. It may be part of a compound transition, which takes the state
 * machine from one state configuration to another, representing the complete
 * response of the state machine to an occurrence of an event of a particular
 * type.
 */
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.model.ModelScape;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UMLTransition extends UMLNamespace implements Transition {

    private TransitionKind kind;
    private Collection<Trigger> triggers = Util.newList();
    private Constraint guard = null;
    private Behavior effect = null;

    // NON-UML standard: cache of frequently access elements
    private Collection<SignalEvent> cachedSignalEvents = null;
    private Collection<TimeEvent> cachedTimeEvents = null;
    private Boolean hasNoTriggerEvent = null;


    public UMLTransition (Node element, ModelScape scape) {
        super(element, scape);

        try {
            try {
                // Get the "kind" attribute
                kind = Enum.valueOf(TransitionKind.class, getAttribute(element, UMLLabel.KEY_KIND));
            } catch (IllegalArgumentException e) {  // default to external
                // In MagicDraw, unspecified "kind" is external, as can be
                //  confirmed by saving project as rich XMI.
                kind = TransitionKind.external;
            }

            // Search for all the triggers
            NodeList triggerNodes = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_TRIGGER, UMLLabel.TYPE_TRIGGER),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < triggerNodes.getLength(); i++) {
                triggers.add(new UMLTrigger(triggerNodes.item(i), scape));
            }

            // If no triggers defined, then set hasNoTriggerEvent
            if (triggers.size() == 0) {
                Util.info("Info: Transition found with No Event trigger: "
                        + getQualifiedName() + " (ID " + id() + ")");

                hasNoTriggerEvent = Boolean.TRUE;
            }  // otherwise, leave hasNoTriggerEvent NULL for later evaluation

            // Get the effect
            effect = getBehaviorSubtype(UMLLabel.TAG_EFFECT, element);

            // Get the guard
            Node node = UMLIdentifiers.inst().transition_getGuard(element);
            if (node != null) {
                guard = new UMLConstraint(node, scape);
            }
            

            if (Util.isDebugLevel()) {
                Util.debug("UMLTransition: processed new trans " + getQualifiedName()
                        + ((kind == TransitionKind.internal) ?
                                "; internal" : "; external")
                        + "; id: " + id());
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLTransition constructor: ");
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Transition#getTrigger()
     */
    public Collection<Trigger> getTrigger () {
        return Collections.unmodifiableCollection(triggers);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Transition#getGuard()
     */
    public Constraint getGuard () {
        return guard;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Transition#getEffect()
     */
    public Behavior getEffect () {
        return effect;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Transition#getSource()
     */
    public Vertex getSource () {
        return (Vertex) xmi2uml(getAttribute(domElement, UMLLabel.KEY_SOURCE));
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Transition#getTarget()
     */
    public Vertex getTarget () {
        return (Vertex) xmi2uml(getAttribute(domElement, UMLLabel.KEY_TARGET));
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Transition#getKind()
     */
    public TransitionKind getKind () {
        return kind;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Transition#isInternal()
     */
    public boolean isInternal () {
        return kind == TransitionKind.internal;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Transition#getSignalEvents()
     */
    public Collection<SignalEvent> getSignalEvents () {
        if (cachedSignalEvents == null) {
            cachedSignalEvents = Util.newList();
            for (Trigger trigger : triggers) {
                if (!checkTriggerHasNoEvent(trigger)
                        && trigger.getEvent() instanceof SignalEvent) {

                    cachedSignalEvents.add((SignalEvent) trigger.getEvent());
                }
            }
        }

        return Collections.unmodifiableCollection(cachedSignalEvents);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Transition#getTimeEvents()
     */
    public Collection<TimeEvent> getTimeEvents () {
        if (cachedTimeEvents == null) {
            cachedTimeEvents = Util.newList();
            for (Trigger trigger : triggers) {
                if (!checkTriggerHasNoEvent(trigger)
                        && trigger.getEvent() instanceof TimeEvent) {

                    cachedTimeEvents.add((TimeEvent) trigger.getEvent());
                }
            }
        }

        return Collections.unmodifiableCollection(cachedTimeEvents);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Transition#getAllEvents()
     */
    public Collection<Event> getAllEvents () {
        List<Event> allEvents = Util.newList();
        allEvents.addAll(getSignalEvents());
        allEvents.addAll(getTimeEvents());
        return allEvents;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Transition#isNullEvent()
     */
    public boolean isNullEvent () {
        if (hasNoTriggerEvent == null) {
            for (Trigger trigger : triggers) {
                if (checkTriggerHasNoEvent(trigger)) {
                    break;
                }
            }
            if (hasNoTriggerEvent == null) {  // at the end, set FALSE value
                hasNoTriggerEvent = Boolean.FALSE;
            }
        }

        return hasNoTriggerEvent;
    }

    private boolean checkTriggerHasNoEvent (Trigger trigger) {
        boolean hasNoEvent = false;
        if (hasNoTriggerEvent == null && trigger.getEvent() == null) {
            Util.info("Info: Transition found with Null Event trigger: "
                    + getQualifiedName() + " (ID " + id() + ")");

            hasNoEvent = true;

            // also set member state to avoid unnecessary loop
            hasNoTriggerEvent = Boolean.TRUE;
        }

        return hasNoEvent;
    }

}
