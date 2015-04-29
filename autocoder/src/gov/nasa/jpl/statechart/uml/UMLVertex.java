/**
 * A vertex is an abstraction of a node in a state machine graph. In general, it
 * can be the source or destination of any number of transitions.
 */
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.model.ModelScape;

import java.util.Collection;
import java.util.Collections;

import org.w3c.dom.Node;

public class UMLVertex extends UMLNamedElement implements Vertex {

    // We will cache these values
    private Collection<Transition> incoming = null;
    private Collection<Transition> outgoing = null;

    public UMLVertex (Node element, ModelScape scape) {
        super(element, scape);
    }

    public Region getContainer () {
        return getParentAs(Region.class);
    }

    public Collection<Transition> getIncoming () {
        if (incoming == null) {
            incoming = UMLIdentifiers.inst().vertex_getIncomingTransitions(this);
        }

        return Collections.unmodifiableCollection(incoming);
    }

    public Collection<Transition> getOutgoing () {
        if (outgoing == null) {
            outgoing = UMLIdentifiers.inst().vertex_getOutgoingTransitions(this);
        }

        return Collections.unmodifiableCollection(outgoing);
    }

    public StateMachine getContainingStatemachine () {
        StateMachine stateMachine = null;

        Region container = getContainer();
        if (container != null) {
            // The container is a region
            stateMachine = container.getContainingStatemachine();
        } else if (this instanceof Pseudostate) {
            Pseudostate pseudostate = (Pseudostate) this;
            PseudostateKind kind = pseudostate.getKind();

            if (kind.equals(PseudostateKind.entryPoint)
                    || kind.equals(PseudostateKind.exitPoint)) {
                // TODO is this the right implementation?
                stateMachine = pseudostate.getStatemachine();
            } else if (this instanceof ConnectionPointReference) {
                ConnectionPointReference cpr = (ConnectionPointReference) this;
                stateMachine = cpr.getState().getContainingStatemachine();
            } else {
                throw new RuntimeException("Vertex::containingStatemachine");
            }
        }

        return stateMachine;
    }

    /**
     * Returns the parent UML State of this state, if any, or <code>null</code> otherwise.
     * @return  Parent UML State, or <code>null</code>.
     * @see gov.nasa.jpl.statechart.uml.State#getParentState()
     */
    public State getParentState () {
        State parentState = null;

        NamedElement cur = this;
        NamedElement parent = null;
        // loop invariant:  terminate when cur == null
        while (cur != null) {
            parent = cur.getParent();
            if (parent instanceof State) {  // we're golden
                parentState = (State) parent;
                break;
            } else if (parent instanceof Package) {
                // we've gotten past highest enclosing StateMachine to a package
                cur = null;  // consider this to be _done_
            } else {
                cur = parent;
            }
        }

        return parentState;
    }

}
