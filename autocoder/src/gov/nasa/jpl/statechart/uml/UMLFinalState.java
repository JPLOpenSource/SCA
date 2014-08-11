package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.model.ModelScape;

import java.util.Collections;
import java.util.List;

import org.w3c.dom.Node;

public class UMLFinalState extends UMLState implements FinalState {

    public UMLFinalState (Node element, ModelScape scape) {
        super(element, scape);
    }

    public List<Transition> getOutgoing () {
        return Collections.emptyList();
    }

    public List<Region> getRegions () {
        return Collections.emptyList();
    }

    public StateMachine getSubmachine () {
        return null;
    }
}
