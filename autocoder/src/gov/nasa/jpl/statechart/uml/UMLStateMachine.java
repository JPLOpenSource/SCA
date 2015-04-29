/**
 * A state machine owns one or more regions, which in turn own vertices and
 * transitions. The behaviored classifier context owning a state machine defines
 * which signal and call triggers are defined for the state machine, and which
 * attributes and operations are available in activities of the state machine.
 * Signal triggers and call triggers for the state machine are defined according
 * to the receptions and operations of this classifier.
 * 
 * As a kind of behavior, a state machine may have an associated behavioral
 * feature (specification) and be the method of this behavioral feature. In this
 * case the state machine specifies the behavior of this behavioral feature. The
 * parameters of the state machine in this case match the parameters of the
 * behavioral feature and provide the means for accessing (within the state
 * machine) the behavioral feature parameters.
 * 
 * A state machine without a context classifier may use triggers that are
 * independent of receptions or operations of a classifier, i.e., either just
 * signal triggers or call triggers based upon operation template parameters of
 * the (parameterized) statemachine.
 */
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.Timer;
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

public class UMLStateMachine extends UMLBehavior implements StateMachine {

    // There must be at least one region
    private Collection<Region> region = Util.newList();
    private Collection<Pseudostate> connectionPoint = Util.newList();
    private List<Behavior> ownedBehavior = Util.newList();

    private Timer timer = new Timer();

    public UMLStateMachine (Node element, ModelScape scape) {
        super(element, scape);

        try {
            // Look for all the <region> tags that are a child of this node
            NodeList regions = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_REGION, UMLLabel.TYPE_REGION),
                    element, XPathConstants.NODESET);

            for (int i = 0; i < regions.getLength(); i++) {
                region.add(new UMLRegion(regions.item(i), scape));
            }

            // Look for all the children <connectionPoint> tags to this node
            NodeList connPts = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_CONNECTION_POINT, UMLLabel.TYPE_PSEUDOSTATE),
                    element, XPathConstants.NODESET);

            for (int i = 0; i < connPts.getLength(); i++) {
                connectionPoint.add(new UMLPseudostate(connPts.item(i), scape));
            }

            // Find all owned Function Behaviors
            NodeList functs = (NodeList) UMLElement.xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_MEMBER_BEHAVIOR, UMLLabel.TYPE_BEH_FUNCTION),
                    element, XPathConstants.NODESET );
            for (int i = 0 ; i < functs.getLength() ; ++i) {
                ownedBehavior.add(new UMLFunctionBehavior(functs.item(i), scape));
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLStateMachine constructor: ");
        }

        if (Util.isInfoLevel()) timer.markTime();  // time stamp
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.StateMachine#getRegion()
     */
    public Collection<Region> getRegion () {
        return Collections.unmodifiableCollection(region);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.StateMachine#getConnectionPoint()
     */
    public Collection<Pseudostate> getConnectionPoint () {
        return Collections.unmodifiableCollection(connectionPoint);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.StateMachine#ancestor(gov.nasa.jpl.statechart.uml.State, gov.nasa.jpl.statechart.uml.State)
     */
    public boolean ancestor (State s1, State s2) {
        if (s1 == null || s2 == null) {
            return false;
        }
        if (s1.equals(s2)) {
            return true;
        }
        if (s1.getContainer() == null) {
            return true;
        }
        if (s2.getContainer() == null) {
            return false;
        }
        return ancestor(s1, s2.getContainer().getState());
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.StateMachine#getOwnedBehavior()
     */
    public Collection<Behavior> getOwnedBehavior () {
        return Collections.unmodifiableCollection(ownedBehavior);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.StateMachine#getInstanceIds()
     */
    public String[] getInstanceIds () {
        return modelScape.instancesById.keySet().toArray(new String[0]);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.StateMachine#getFriends()
     */
    public String[] getFriends () {
        String [] list = modelScape.getCachedFriendsList().get(id);
        if (list == null) {
            return new String[0];
        } else {
            return list;
        }
    }

}
