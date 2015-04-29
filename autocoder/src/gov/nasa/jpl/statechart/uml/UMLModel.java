/**
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
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.input.identifiers.XMIIdentifiers;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.model.ModelScape;
import gov.nasa.jpl.statechart.model.PrefixOrderedWalker;
import gov.nasa.jpl.statechart.model.visitor.SignalVisitor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class sets up the structure of the internal representation using the
 * supplied Document Node as the model root.  In this implementation, XPath is
 * used to traverse the DOM structure and fetch the expected UML elements.
 * In this leaf class, StateMachines and Signals that are not processed by
 * construction activities in the superclasses are processed and stored as
 * owned members here.  Signal Events and Time Events are only processed and
 * stored here in this leaf Model class (which is the top vertex if viewed
 * compositionally) so that one unique set of events is tracked at the
 * "model-global" level.
 * <p>
 * The UMLModel statically keeps track of mappings between UML elements and the
 * model to which they belong.  This enables multiple models to be processed
 * in a single Autocoder run.
 * </p>
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class UMLModel extends UMLPackage implements Model {
    private ModelGroup modelGroup = null;

    // cache of 4 key Model elements, in order of insertion, for fast access
    private List<Signal> allSignals = null;
    private List<SignalEvent> allSignalEvents = null;
    private List<TimeEvent> allTimeEvents = null;
    private List<StateMachine> allStateMachines = null;
    // flag to track exception in processing that prevents autocoding
    private boolean modelException = false;

    /**
     * Main constructor, accepts a {@link ModelGroup} and {@link ModelScape}
     * object that provides a "model landscape".
     * 
     * @param mGrp   {@link ModelGroup} object that contains this model.
     * @param scape  {@link ModelScape} object containing collected sets of XMI Nodes.
     */
    public UMLModel (ModelGroup mGrp, ModelScape scape) {
        super(scape.element, scape);

        modelGroup = mGrp;
        // initial cache collections
        allSignals = Util.newList();
        allSignalEvents = Util.newList();
        allTimeEvents = Util.newList();
        allStateMachines = Util.newList();

        try {
            // instantiate State Machines that are not yet created
            for (int i = 0; i < scape.machineNodes.getLength(); i++) {
                Node smNode = scape.machineNodes.item(i);
                String id = getAttribute(smNode, XMIIdentifiers.id());
                if (xmi2uml(id) == null) { // State Machine not yet instantiated
                    if (modelScape.shouldLoadMachine(smNode)) { // process this SM
                        StateMachine sm = new UMLStateMachine(smNode, scape);
                        ownedMember.add(sm);
                        allStateMachines.add(sm);
                    }
                } else { // keep track of the instantiated sub-statemachines
                    StateMachine sm = (StateMachine) xmi2uml(id);
                    allStateMachines.add(sm);
                }
            }
            if (Util.isInfoLevel())
                scape.timer.markTime("Loaded State Machines."); // time stamp

            /* At this point, we have found all the StateMachines, and we can
             * figure out what signals and events are needed.
             * Notice we cannot distinguish between SignalEvents and TimeEvents
             * yet because we're going by the trigger object.
             */
            Collection<String> neededEvents = Util.newSet();
            Collection<String> neededSignals = Util.newSet();
            if (Autocoder.filterTransitionEvents()) {
                // specific SMs requested, so what's not in the set can be
                // ignored
                for (StateMachine machine : getStateMachines()) {
                    if (!Autocoder.specificSMRequested(machine.getName()))
                        continue;
                    // Careful! can't use visitors yet because events not
                    // created

                    Node smElem = ((UMLStateMachine) machine).getDOMElement();
                    NodeList eventNodes = (NodeList) xpath.evaluate(
                            ".//"
                                    + UMLIdentifiers.path2NodeOfType(
                                            UMLLabel.TAG_TRIGGER,
                                            UMLLabel.TYPE_TRIGGER), smElem,
                            XPathConstants.NODESET);
                    for (int i = 0; i < eventNodes.getLength(); ++i) {
                        neededEvents.add(getAttribute(eventNodes.item(i),
                                UMLIdentifiers.inst().lit(UMLLabel.KEY_EVENT)));
                    }

                    // pick up any already processed signals
                    for (Signal signal : PrefixOrderedWalker.traverse(machine,
                            new SignalVisitor(true))) {
                        neededSignals.add(signal.id());
                    }
                }
                if (Util.isInfoLevel())
                    scape.timer.markTime("Determined needed subset of events and signals...");
                if (Util.isInfoLevel()
                        || scape.sumCount() > ModelScape.COUNT_THRESHOLD) {
                    StringBuilder sb = new StringBuilder("Processing partial (").append(
                            neededEvents.size()).append(
                            ") SignalEvents and TimeEvents, and needed Signals...");
                    if (scape.sumCount() > ModelScape.COUNT_THRESHOLD) {
                        System.out.println(sb.append(
                                "(this may take a minute or two)").toString());
                    } else {
                        Util.info(sb.toString());
                    }
                }
            } else {
                if (scape.sumCount() > ModelScape.COUNT_THRESHOLD) {
                    System.out.println("Processing all SignalEvents, TimeEvents, and Signals...(this may take a minute or two)");
                } else {
                    Util.info("Processing all SignalEvents, TimeEvents, and Signals...");
                }
            }

            if (neededEvents.isEmpty()) { // processing ALL events!
                for (int i = 0; i < scape.signalEventNodes.getLength(); ++i) {
                    SignalEvent sigEv = new UMLSignalEvent(scape.signalEventNodes.item(i), scape);
                    ownedMember.add(sigEv);
                    allSignalEvents.add(sigEv);
                }
                if (Util.isInfoLevel())
                    scape.timer.markTime("Processed " + allSignalEvents.size()
                            + " SignalEvents...");

                for (int i = 0; i < scape.timeEventNodes.getLength(); ++i) {
                    TimeEvent timeEv = new UMLTimeEvent(scape.timeEventNodes.item(i), scape);
                    ownedMember.add(timeEv);
                    allTimeEvents.add(timeEv);
                }
                if (Util.isInfoLevel())
                    scape.timer.markTime("Processed " + allTimeEvents.size()
                            + " TimeEvents...");

                // for Signals, fill in only the missed ones
                for (int i = 0; i < scape.signalNodes.getLength(); ++i) {
                    Node signalNode = scape.signalNodes.item(i);
                    String id = getAttribute(signalNode, XMIIdentifiers.id());
                    if (xmi2uml(id) == null) {
                        // instantiate all, since can't know when one is fired!
                        Signal signal = new UMLSignal(signalNode, scape);
                        ownedMember.add(signal);
                        allSignals.add(signal);
                    } else { // track the already created signals
                        Signal signal = (Signal) xmi2uml(id);
                        allSignals.add(signal);
                    }
                }
                if (Util.isInfoLevel())
                    scape.timer.markTime("Processed " + allSignals.size()
                            + " Signals...");

            } else {
                // Signal events first...
                for (int i = 0; i < scape.signalEventNodes.getLength(); ++i) {
                    Node signalEvNode = scape.signalEventNodes.item(i);
                    String id = getAttribute(signalEvNode, XMIIdentifiers.id());
                    if (neededEvents.contains(id)) {
                        SignalEvent sigEv = new UMLSignalEvent(signalEvNode, scape);
                        ownedMember.add(sigEv);
                        allSignalEvents.add(sigEv);
                        // store the signal of event in needed set
                        neededSignals.add(getAttribute(signalEvNode,
                                UMLIdentifiers.inst().lit(UMLLabel.KEY_SIGNAL)));
                    }
                }
                if (Util.isInfoLevel())
                    scape.timer.markTime("Processed " + allSignalEvents.size()
                            + " SignalEvents...");

                // Time events next...
                for (int i = 0; i < scape.timeEventNodes.getLength(); ++i) {
                    Node timeEvNode = scape.timeEventNodes.item(i);
                    String id = getAttribute(timeEvNode, XMIIdentifiers.id());
                    if (neededEvents.contains(id)) {
                        TimeEvent timeEv = new UMLTimeEvent(timeEvNode, scape);
                        ownedMember.add(timeEv);
                        allTimeEvents.add(timeEv);
                        // store the signal of event in needed set
                        neededSignals.add(getAttribute(timeEvNode,
                                UMLIdentifiers.inst().lit(UMLLabel.KEY_SIGNAL)));
                    }
                }
                if (Util.isInfoLevel())
                    scape.timer.markTime("Processed " + allTimeEvents.size()
                            + " TimeEvents...");

                // Process signals only if non-empty...
                // - since only partial set of events are process, empty really
                // means no signal needed!
                if (!neededSignals.isEmpty()) {
                    for (int i = 0; i < scape.signalNodes.getLength(); ++i) {
                        Node signalNode = scape.signalNodes.item(i);
                        String id = getAttribute(signalNode,
                                XMIIdentifiers.id());
                        if (xmi2uml(id) == null) { // Signal not yet created
                            if (neededSignals.contains(id)) { // and needs to be
                                Signal signal = new UMLSignal(signalNode, scape);
                                ownedMember.add(signal);
                                allSignals.add(signal);
                            }
                        } else { // already created, tally it for info
                            Signal signal = (Signal) xmi2uml(id);
                            allSignals.add(signal);
                        }
                    }
                    if (Util.isInfoLevel())
                        scape.timer.markTime("Processed " + allSignals.size()
                                + " Signals...");
                }
            }

        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLModel constructor: ");
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Model#getStateMachines()
     */
    public Collection<StateMachine> getStateMachines () {
        // Look through the ownedMembers of the model and return all
        // UMLStateMachines
        // (now cached...)
        return Collections.unmodifiableCollection(allStateMachines);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Model#hasException()
     */
    public boolean hasException () {
        return modelException;
    }

    public ModelGroup getModelGroup () {
        return modelGroup;
    }

    public ModelScape getModelScape () {
        return modelScape;
    }

    public Collection<Signal> getSignals () {
        return Collections.unmodifiableCollection(allSignals);
    }

    public Collection<SignalEvent> getSignalEvents () {
        return Collections.unmodifiableCollection(allSignalEvents);
    }

    public Collection<TimeEvent> getTimeEvents () {
        return Collections.unmodifiableCollection(allTimeEvents);
    }

    /**
     * Sets the model exception state; set to <code>true</code> to indicate that
     * there is an exception in processing the Model which prevents the Autocoder
     * from generating code correctly.
     * @param b  new boolean state
     */
    public void setModelException (boolean b) {
        modelException = b;
    }
}
