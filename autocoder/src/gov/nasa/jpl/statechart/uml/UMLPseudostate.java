package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.model.ModelScape;

import org.w3c.dom.Node;

public class UMLPseudostate extends UMLVertex implements Pseudostate {

    public final PseudostateKind kind;

    UMLPseudostate (Node element, ModelScape scape) {
        super(element, scape);

        PseudostateKind testKind = PseudostateKind.initial;
        try {
            // Get the "kind" attribute
            testKind = Enum.valueOf(PseudostateKind.class, getAttribute(element, UMLLabel.KEY_KIND));
        } catch (IllegalArgumentException e) {  // default to initial
        }
        kind = testKind;
    }

    public StateMachine getStatemachine () {
        return getParentAs(StateMachine.class);
    }

    public State getState () {
        return getParentAs(State.class);
    }

    public PseudostateKind getKind () {
        return kind;
    }
}
