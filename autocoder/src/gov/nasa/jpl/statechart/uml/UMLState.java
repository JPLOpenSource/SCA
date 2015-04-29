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

public class UMLState extends UMLNamespace implements State {

    // Copied from the UMLVertex implementation
    private Vertex vertex = null;

    private Collection<ConnectionPointReference> connection = Util.newList();
    private Collection<Pseudostate> connectionPoint = Util.newList();
    // TODO: UMLState deferrableTrigger unused!
    //private Collection<Trigger> deferrableTrigger = Util.newList();

    private Behavior entry = null;
    private Behavior exit = null;
    private Behavior doActivity = null;
    // TODO: UMLState redefinedState unused!
    //private State redefinedState = null;
    private Collection<Region> region = Util.newList();
    private StateMachine submachine = null;

    public UMLState (Node element, ModelScape scape) {
        super(element, scape);
        vertex = new UMLVertex(element, scape);

        // Use XPath to get all the child nodes we're interested in
        try {
            entry = getBehaviorSubtype(UMLLabel.TAG_ENTRY, element);
            exit = getBehaviorSubtype(UMLLabel.TAG_EXIT, element);
            doActivity = getBehaviorSubtype(UMLLabel.TAG_DOACTIVITY, element);

            NodeList regions = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_REGION, UMLLabel.TYPE_REGION),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < regions.getLength(); ++i) {
                region.add(new UMLRegion(regions.item(i), scape));
            }

            if (isSubmachineState()) {
                NodeList connections = (NodeList) xpath.evaluate(
                        UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_CONNECTION, UMLLabel.TYPE_CONNPT_REF),
                        element, XPathConstants.NODESET);
                for (int i = 0; i < connections.getLength(); ++i) {
                    connection.add(new UMLConnectionPointReference(connections.item(i), scape));
                }
            }

            if (isComposite()) {
                // TODO support for Composite state connection point??
                NodeList connPoints = (NodeList) xpath.evaluate(
                        UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_CONNECTION_POINT, UMLLabel.TYPE_PSEUDOSTATE),
                        element, XPathConstants.NODESET);
                for (int i = 0; i < connPoints.getLength(); ++i) {
                    connectionPoint.add(new UMLPseudostate(connPoints.item(i), scape));
                }
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLState constructor: ");
        }
    }

    public boolean isSimple () {
        return (getSubmachine() == null) && region.isEmpty();
    }

    public boolean isComposite () {
        return !region.isEmpty();
    }

    public boolean isOrthogonal () {
        return (region.size() > 1);
    }

    public boolean isSubmachineState () {
        return !"".equals(getAttribute(domElement, UMLLabel.KEY_SUBMACHINE));
    }

    public StateMachine getSubmachine () {
        if (submachine == null) {  // populate it
            submachine = (StateMachine) xmi2uml(getAttribute(domElement, UMLLabel.KEY_SUBMACHINE));
        }
        return submachine;
    }

    public Collection<Region> getRegion () {
        return Collections.unmodifiableCollection(region);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.State#getEntry()
     */
    public Behavior getEntry () {
        return entry;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.State#getExit()
     */
    public Behavior getExit () {
        return exit;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.State#getDo()
     */
    public Behavior getDo () {
        return doActivity;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.State#getConnection()
     */
    public Collection<ConnectionPointReference> getConnection () {
        return Collections.unmodifiableCollection(connection);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.State#getConnectionPoint()
     */
    public Collection<Pseudostate> getConnectionPoint () {
        return Collections.unmodifiableCollection(connectionPoint);
    }

    /**
     * Vertex delegator
     */
    public StateMachine getContainingStatemachine () {
        return vertex.getContainingStatemachine();
    }

    public Collection<Transition> getIncoming () {
        List<Transition> incoming = Util.newList(vertex.getIncoming());
        // also include the connection point refs, if any
        for (ConnectionPointReference cpr : connection) {
            incoming.addAll(cpr.getIncoming());
        }
        return incoming;
    }

    public Collection<Transition> getOutgoing () {
        List<Transition> outgoing = Util.newList(vertex.getOutgoing());
        // also include the connection point refs, if any
        for (ConnectionPointReference cpr : connection) {
            outgoing.addAll(cpr.getOutgoing());
        }
        return outgoing;
    }

    public Region getContainer () {
        return vertex.getContainer();
    }

    public State getParentState () {
        return vertex.getParentState();
    }

}
