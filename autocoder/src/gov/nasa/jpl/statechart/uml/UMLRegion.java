package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.model.ModelScape;

import java.util.Collection;
import java.util.Collections;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UMLRegion extends UMLNamespace implements Region {

    private Collection<Transition> transition = Util.newList();
    private Collection<Vertex> subvertex = Util.newList();
    // TODO: UMLRegion extendedRegion unused!
    //private Region extendedRegion = null;

    // elements that are not part of the UML MOF standard.
    private Collection<Pseudostate> historyStates = null;


    public UMLRegion (Node element, ModelScape scape) {
        super(element, scape);

        try {
            // Search for all the subvertices contained in this region.
            // Keep the uml:State, uml:Pseudostate, and uml:FinalState items
            // and the transitions.

            //- good o' State
            NodeList states = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_SUBVERTEX, UMLLabel.TYPE_STATE),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < states.getLength(); i++) {
                subvertex.add(new UMLState(states.item(i), scape));
            }

            //- Pseudostate
            NodeList pseudostates = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_SUBVERTEX, UMLLabel.TYPE_PSEUDOSTATE),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < pseudostates.getLength(); i++) {
                subvertex.add(new UMLPseudostate(pseudostates.item(i), scape));
            }

            //- FinalState
            states = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_SUBVERTEX, UMLLabel.TYPE_FINALSTATE),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < states.getLength(); i++) {
                subvertex.add(new UMLFinalState(states.item(i), scape));
            }

            //- Transition
            NodeList transitions = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_TRANSITION, UMLLabel.TYPE_TRANSITION),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < transitions.getLength(); i++) {
                transition.add(new UMLTransition(transitions.item(i), scape));
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLRegion constructor: ");
        }
    }

    public StateMachine getContainingStatemachine () {
        StateMachine statemachine = getStatemachine();

        if (statemachine == null)
            return getState().getContainingStatemachine();
        else
            return statemachine;
    }

    public Collection<Transition> getTransition () {
        return Collections.unmodifiableCollection(transition);
    }

    public Collection<Vertex> getSubvertex () {
        return Collections.unmodifiableCollection(subvertex);
    }

    public State getState () {
        return getParentAs(State.class);
    }

    public StateMachine getStatemachine () {
        return getParentAs(StateMachine.class);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Region#containsHistoryState()
     */
    public boolean containsHistoryState () {
        return getHistoryState().size() > 0;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Region#getHistoryState()
     */
    public Collection<Pseudostate> getHistoryState () {
        if (historyStates == null) {  // lazy init
            historyStates = Util.newSet();
            for (Pseudostate pseudo : Util.filter(subvertex, Pseudostate.class)) {
                if (pseudo.getKind() == PseudostateKind.deepHistory) {
                    historyStates.add(pseudo);
                }
            }
        }
        return Collections.unmodifiableCollection(historyStates);
    }

}
