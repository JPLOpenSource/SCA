/**
 * Extracted from UMLModel: April 15, 2011.
 * <p>
 * Copyright 2009-2011, by the California Institute of Technology. ALL RIGHTS
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
package gov.nasa.jpl.statechart.model;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.Autocoder.SignalNamespaceType;
import gov.nasa.jpl.statechart.Timer;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.ReaderNamespaceContext;
import gov.nasa.jpl.statechart.input.identifiers.ProfileIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.ProfileLabel;
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.input.identifiers.XMIIdentifiers;
import gov.nasa.jpl.statechart.input.validator.FatalModelException;
import gov.nasa.jpl.statechart.model.visitor.AbstractVisitor.OrthoRegion;
import gov.nasa.jpl.statechart.model.visitor.BehaviorVisitor;
import gov.nasa.jpl.statechart.uml.Behavior;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Namespace;
import gov.nasa.jpl.statechart.uml.Profile;
import gov.nasa.jpl.statechart.uml.Signal;
import gov.nasa.jpl.statechart.uml.UMLElement;
import gov.nasa.jpl.statechart.uml.UMLModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class scans the model "landscape" and determine what State Machines,
 * Events, and Signals are present, as well as what partial portion of the
 * model tree to process and load.  This class helps optimize run-time and
 * improves usability.
 * <p>
 * Copyright &copy; 2009-2011 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public class ModelScape {

    public static final int COUNT_THRESHOLD = 100;

    public static Map<String,ModelScape> scapesByFilename = Util.newMap();

    /**
     * Collect some initial statistics on the Model, should be called at the
     * moment of instantiating a new UMLModel.  This pattern is used because,
     * once UMLModel is new'd, processing would have started in the parent
     * classes.  So this pattern allows the stats gathering to occur before
     * all the processing begin.
     * 
     * @param element  DOM element node
     * @return  an internal {@link ModelScape} class for passing stat info.
     */
    public static ModelScape profileModel (String filename, Node element, ModelGroup mGrp) {
        ModelScape data = new ModelScape();
        scapesByFilename.put(filename, data);
        data.element = element;
        data.modelGroup = mGrp;

        try {
            // Find all Stereotypes in this model
            data.stereotypeNodes = (NodeList) UMLElement.xpath.evaluate(".//"
                    + UMLIdentifiers.path2NodeOfType(
                            UMLLabel.TAG_MEMBER_ELEMENT,
                            UMLLabel.TYPE_STEREOTYPE), data.element,
                            XPathConstants.NODESET );
            if (data.stereotypeNodes.getLength() > 0) {
                data.loadAllPaths = true;
            }

        	// Find all descendant State Machines in this model
        	data.machineNodes = (NodeList) UMLElement.xpath.evaluate(".//"
        			+ UMLIdentifiers.path2NodeOfType(
        					UMLLabel.TAG_MEMBER_ELEMENT,
        					UMLLabel.TYPE_STATEMACHINE)
        					+ " | .//"
        					+ UMLIdentifiers.path2NodeOfType(
        							UMLLabel.TAG_MEMBER_BEHAVIOR,
        							UMLLabel.TYPE_STATEMACHINE), element,
        							XPathConstants.NODESET);
        	for (int i=0; i < data.machineNodes.getLength(); ++i) {
        		String id = Util.getNodeAttribute(data.machineNodes.item(i), XMIIdentifiers.id());
        		data.machinesById.put(id, data.machineNodes.item(i));
        	}


        	// Scan for all State Machine instantiations 
        	data.instanceNodes = (NodeList) UMLElement.xpath.evaluate(".//"
        			+ UMLIdentifiers.path2NodeOfType(
        					UMLLabel.TAG_MEMBER_ELEMENT,
        					UMLLabel.TYPE_INSTANCE_SPECIFICATION), element,
        					XPathConstants.NODESET);
        	for ( int i=0; i < data.instanceNodes.getLength(); ++i ) {
        		String id = Util.getNodeAttribute(data.instanceNodes.item(i), XMIIdentifiers.id());
        		data.instancesById.put(id, data.instanceNodes.item(i));
        	}


            // Scan top-level model for all signals
        	data.signalNodes = (NodeList) UMLElement.xpath.evaluate(".//"
        			+ UMLIdentifiers.path2NodeOfType(
        					UMLLabel.TAG_MEMBER_ELEMENT, 
        					UMLLabel.TYPE_SIGNAL)
        					+ " | .//"
        					+ UMLIdentifiers.path2NodeOfType(
        							UMLLabel.TAG_NESTED_CLASSIFIER,
        							UMLLabel.TYPE_SIGNAL), element,
        							XPathConstants.NODESET);
        	for (int i=0; i < data.signalNodes.getLength(); ++i) {
        		String id = Util.getNodeAttribute(data.signalNodes.item(i), XMIIdentifiers.id());
        		data.signalsById.put(id, data.signalNodes.item(i));
        	}

        	/* Look for all signal events and time events underneath model node;
        	 * this corresponds with how signal events are usually placed at the
        	 * "top level" immediately below the model, sibling to the machine,
        	 * and also takes into account global scope of signal events.
        	 */
        	data.signalEventNodes = (NodeList) UMLElement.xpath.evaluate(".//"
        			+ UMLIdentifiers.path2NodeOfAnyOfTypes(
        					UMLLabel.TAG_MEMBER_ELEMENT,
        					UMLLabel.TYPE_SIGNAL_EVENT,
        					UMLLabel.TYPE_SEND_SIGNAL_EVENT,
        					UMLLabel.TYPE_RECEIVE_SIGNAL_EVENT), element,
        					XPathConstants.NODESET);
        	for (int i=0; i < data.signalEventNodes.getLength(); ++i) {
        		String id = Util.getNodeAttribute(data.signalEventNodes.item(i), XMIIdentifiers.id());
        		data.signalEventsById.put(id, data.signalEventNodes.item(i));
        	}
        	data.timeEventNodes = (NodeList) UMLElement.xpath.evaluate(".//"
        			+ UMLIdentifiers.path2NodeOfType(
        					UMLLabel.TAG_MEMBER_ELEMENT,
        					UMLLabel.TYPE_TIME_EVENT), element,
        					XPathConstants.NODESET);
        	for (int i=0; i < data.timeEventNodes.getLength(); ++i) {
        		String id = Util.getNodeAttribute(data.timeEventNodes.item(i), XMIIdentifiers.id());
        		data.timeEventsById.put(id, data.timeEventNodes.item(i));
        	}

        	System.out.println("Model contains "
        			+ data.machineNodes.getLength() + " State Machines, "
        			+ data.signalEventNodes.getLength() + " SignalEvents, "
        			+ data.timeEventNodes.getLength() + " TimeEvents, "
        			+ data.signalNodes.getLength() + " Signals, "
        			+ data.instanceNodes.getLength() + " Instances, and "
                    + data.stereotypeNodes.getLength() + " Stereotypes");

        } catch (XPathExpressionException e) {  // report error and just move on
        	Util.reportException(e, "ModelScape.profileModel(): ");
        }

        return data;
    }


    public Timer timer = new Timer(); // instantiated with ModelData

    /** Starting model node used to scan model landscape */
    public Node element = null;
    public ModelGroup modelGroup = null;

    // Tracks the model nodes examined
    public NodeList machineNodes = null;
    public NodeList signalEventNodes = null;
    public NodeList timeEventNodes = null;
    public NodeList signalNodes = null;
    public NodeList instanceNodes = null;
    public NodeList stereotypeNodes = null;
    public Map<String,Node> machinesById = Util.newMap();
    public Map<String,Node> signalsById = Util.newMap();
    public Map<String,Node> signalEventsById = Util.newMap();
    public Map<String,Node> timeEventsById = Util.newMap();
    public Map<String,Node> instancesById = new LinkedHashMap<String,Node>(); 

    // Gathers info about what actually needs to be loaded; store by ID
    public Set<String> pathsToLoad = Util.newSortedSet();
    public Set<String> fullpathsToLoad = Util.newSortedSet();
    public Set<String> smsToLoad = Util.newSortedSet();
    public Set<String> eventsToLoad = Util.newSortedSet();
    public Set<String> signalsToLoad = Util.newSortedSet();

    // figure out which ModelScapes (by canonical filename) needs recomputed
    private Set<String> scapesToRecompute = Util.newSet();
    // flag set if stereotypes to load in model, which makes scoping moot
    private boolean loadAllPaths = false;


    public int sumCount () {
        return machineNodes.getLength() + signalEventNodes.getLength()
                + timeEventNodes.getLength() + signalNodes.getLength()
                + instanceNodes.getLength() + stereotypeNodes.getLength();
    }

    public boolean shouldLoadMachine (Node n) {
        return smsToLoad.contains(Util.getNodeAttribute(n, XMIIdentifiers.id()));
    }
    public boolean shouldLoadSignal (Node n) {
        return signalsToLoad.contains(Util.getNodeAttribute(n, XMIIdentifiers.id()));
    }
    public boolean shouldLoadEvent (Node n) {
        return eventsToLoad.contains(Util.getNodeAttribute(n, XMIIdentifiers.id()));
    }
    public boolean shouldLoadPath (Node n) {
        if (loadAllPaths) return true;

        String name = Util.getNodeAttribute(n, ReaderNamespaceContext.nameAttr());
        if (name == null) {
            name = "";
        }
        return pathsToLoad.contains(makeQualifiedName(computePathSegments(n), name));
    }

    /**
     * Determine and prune the model tree we will actually load.
     */
    public void pruneModelTree () {
        try {
            timer.markTime("\nPruning model tree based on relevant signals...");
            scapesToRecompute.clear();
            for (int i=0; i < machineNodes.getLength(); ++i) {
                computePathsToLoad(machineNodes.item(i));
            }
            if (Util.isInfoLevel()) {
                Util.info("Found " + signalsToLoad.size() + " signal(s) to load.");
                if (Util.isDebugLevel()) {
                    Util.debug(" >> "+ Arrays.toString(signalsToLoad.toArray()));
                }
            }
            timer.markTime("Done computing paths to load based on signals.");

            computeStereotypePkgs();
            timer.markTime("Computed stereotype packages.");

            if (Util.isInfoLevel()) {
                Util.info("Found " + pathsToLoad.size() + " path(s).");
                if (Util.isDebugLevel()) {
                    Util.debug(" >> "+ Arrays.toString(pathsToLoad.toArray()));
                }
            }

            computeDependentSMs();
            // recompute other model scapes, if identified
            for (String filepath : scapesToRecompute) {
                if (Util.isInfoLevel()) {
                    Util.info("Recomputing dependent SMs in '" + filepath + "'");
                }
                scapesByFilename.get(filepath).computeDependentSMs();
            }
            timer.markTime("Computed dependent SMs!");

        } catch (XPathExpressionException e) {  // report error and just move on
            Util.reportException(e, "ModelScape.pruneModelTree(): ");
        }
    }

    public List<String> computePathSegments (Node n) {
        List<String> segments = Util.newList();
        NodeList ancestors;
        try {
            ancestors = (NodeList) UMLElement.xpath.evaluate("ancestor::*", n, XPathConstants.NODESET);
            for (int i=0; i < ancestors.getLength(); ++i) {
                String name = Util.getNodeAttribute(ancestors.item(i), ReaderNamespaceContext.nameAttr());
                if (name != null) {
                    segments.add(name);
                }
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "ModelScape.computePathSegments(): ");
        }
        return segments;
    }

    public String makeQualifiedPath (List<String> segments) {
        return Util.join(segments, Util.PACKAGE_SEP);
    }
    public String makeQualifiedName (List<String> segments, String name) {
        List<String> segWithName = Util.newList(segments);
        segWithName.add(name);
        return Util.join(segWithName, Util.PACKAGE_SEP);
    }

    public void recordPaths (Node n) {
        List<String> segments = computePathSegments(n);
        // record all partial paths starting from root segment
        for (int i=1; i <= segments.size(); ++i) {
            String path = makeQualifiedPath(segments.subList(0, i));
            pathsToLoad.add(path);
            if (i == segments.size()) {  // full path for finding dependent SMs
                fullpathsToLoad.add(path);
            }
        }
    }

    /**
     * Given a StateMachine node, find all the signal events that trigger
     * transitions within it, and then fetch all signals related to those
     * signal events, from which we determine that a path (and all its
     * ancestor path segments) should be loaded.
     * 
     * @param smNode
     * @throws XPathExpressionException
     */
    public void computePathsToLoad (Node smNode) throws XPathExpressionException {
        // Determine if StateMachine is to be loaded
        if (Autocoder.specificSMRequested(null)) {
            // get ancestor paths
            List<String> segs = computePathSegments(smNode);
            // retrieve name of node
            String name = Util.getNodeAttribute(smNode, ReaderNamespaceContext.nameAttr());
            if (name != null) {
                String qname = makeQualifiedName(segs, name);
                if (!Autocoder.specificSMRequested(qname)) {
                    Util.info("Skipping State Machine: " + qname);
                    return;  // skip this state machine
                } else {
                    Util.info("Will load State Machine: " + qname);
                }
            } else {
                return;  // assume can't load this unnamed SM
            }
        }
        //- yes, save SM and its paths
        smsToLoad.add(Util.getNodeAttribute(smNode, XMIIdentifiers.id()));
        recordPaths(smNode);

        // find all triggers and TimeEvents
        NodeList triggers = (NodeList) UMLElement.xpath.evaluate(
                ".//" + UMLIdentifiers.inst().lit(UMLLabel.TAG_TRIGGER),
                smNode, XPathConstants.NODESET);
        NodeList timeEvts = (NodeList) UMLElement.xpath.evaluate(
                ".//" + UMLIdentifiers.path2NodeOfType(
                        UMLLabel.TAG_MEMBER_ELEMENT,
                        UMLLabel.TYPE_TIME_EVENT),
                smNode, XPathConstants.NODESET);
        // collect trigger event IDs as well as TimeEvent IDs
        Set<String> evIds = Util.newSortedSet();
        for (int i=0; i < triggers.getLength(); ++i) {
            String evId = UMLElement.xpath.evaluate(
                    "@" + UMLIdentifiers.inst().lit(UMLLabel.KEY_EVENT),
                    triggers.item(i));
            if (evId != null && evId.length() > 0) {
                evIds.add(evId);
            }
        }
        for (int i=0; i< timeEvts.getLength(); ++i) {
            evIds.add(Util.getNodeAttribute(timeEvts.item(i), XMIIdentifiers.id()));
        }
        eventsToLoad.addAll(evIds);

        // find and collect all signal IDs that this SM depends on
        Set<String> signalIds = Util.newSortedSet();
        for (String evId : evIds) {
            if (signalEventsById.containsKey(evId)) {
                String sigId = Util.getNodeAttribute(signalEventsById.get(evId),
                        UMLIdentifiers.inst().lit(UMLLabel.KEY_SIGNAL));
                if (sigId == null) {  // try looking for remote ID
                    sigId = UMLIdentifiers.inst().signalEvent_getReferencedSignalId(signalEventsById.get(evId));
                    // chop href string into file name and ID
                    int at = sigId.indexOf("#");
                    if (at > -1) {
                        String filename = sigId.substring(0, at);
                        String uid = sigId.substring(at+1);
                        // look at all known ModelScapes
                        boolean foundRemoteFile = false;
                        for (String filepath : scapesByFilename.keySet()) {
                            if (filepath.endsWith(filename)) {
                                ModelScape scape = scapesByFilename.get(filepath);
                                if (scape.signalsById.containsKey(uid)) {
                                    scape.recordPaths(scape.signalsById.get(uid));
                                    scapesToRecompute.add(filepath);
                                    foundRemoteFile = true;
                                }
                            }
                        }
                        if (!foundRemoteFile) {
                            throw new FatalModelException(
                                    "Error! Signal ID '"+ uid
                                    + "' referenced in model file '" + filename
                                    + "'; please supply it as input file before this file!");
                        }
                    }
                } else {  // signal in this file, store it
                    if (!signalIds.contains(sigId)) {
                        signalIds.add(sigId);
                    }
                }
            }
        }
        signalsToLoad.addAll(signalIds);
        if (Util.isDebugLevel()) {
            Util.debug("Found " + signalIds.size() + " relevant signals: "
                    + Arrays.toString(signalIds.toArray()));
        }

        // determine what signal paths to load
        for (String sigId : signalIds) {
            if (signalsById.containsKey(sigId)) {
                recordPaths(signalsById.get(sigId));
            }
        }
    }

    public void computeStereotypePkgs () throws XPathExpressionException {
        for (int i=0; i < stereotypeNodes.getLength(); ++i) {
            recordPaths(stereotypeNodes.item(i));
        }
    }

    public void computeDependentSMs () throws XPathExpressionException {
        // Determine what _other_ state machines ought to be loaded.
        // To do so, we check each SM node, get its path, including itself,
        //   and see what paths-to-load share the same ancestor path.
        for (String smId : machinesById.keySet()) {
            Node smNode = machinesById.get(smId);
            // form StateMachine path by appending StateMachine name as well
            String smName = Util.getNodeAttribute(smNode, ReaderNamespaceContext.nameAttr());
            String smPath = makeQualifiedName(computePathSegments(smNode), smName);
            // search through all full paths to see if state machine should be loaded 
            for (String path : fullpathsToLoad) {
                if (path.startsWith(smPath) && !smsToLoad.contains(smId)) {
                    // this SM needed to get to path
                    smsToLoad.add(smId);
                    Util.info("Will also load State Machine: " + smPath);
                    break;
                }
            }
        }
    }


    //// caches for various model queries

    // Map to cache chain of namespace/packages for NamedElement obj
    public Map<NamedElement,String[]> pkgChainOfNamedElement = Util.newMap();

    private Set<Signal> cachedSignals = null;
    // Visitor that caches fired Signals by Namespace within which they fire
    private BehaviorVisitor cachedBehVisitor = null;
    // Map to cache signals by their [namespaced] names
    private Map<String,Signal> cachedSignalsByName = null;
    // Map to cache Profiles by their names
    private Map<String,Profile> cachedProfilesByName = null;
    // Map of model elements to collection of applied model-custom stereotype
    private Map<String,Collection<Node>> cachedAppliedStereotypes = null;
    // Map of extended Package IDs to C++Namescape stereotype nodes
    private Map<String,Node> cachedCppNsStereotypes = null;
    // Map of extended SM IDs to String array of declared friends
    private Map<String,String[]> cachedFriendsList = null;

    public Collection<Signal> getCachedSignals () {
        if (cachedSignals == null) {  // lazy populate
            cachedSignals = Util.newSortedSet();
            // retrieve and cache all the signals we know of in this model
            for (UMLModel m : modelGroup.models()) {
                cachedSignals.addAll(m.getSignals());
            }
            if (Util.isDebugLevel()) {
                StringBuilder sb = new StringBuilder("Retrieved signals:\n");
                for (Signal sig : cachedSignals) {
                    sb.append("  ")
                        .append(Util.joinWithPrefixes(sig.getPackageNames(), sig.getName(), null))
                        .append("\n");
                }
                Util.debug(sb.toString());
            }
        }

        return cachedSignals;
    }

    public Map<String,Behavior> getCachedFiredSignals (Namespace ns) {
        if (cachedBehVisitor == null) {  // lazy init
            cachedBehVisitor = new BehaviorVisitor(OrthoRegion.INCLUDE_BELOW_ORTHO);
        }
        Map<String,Behavior> firedSignals = cachedBehVisitor.getFiredSignalsByNamespace(ns);
        if (firedSignals == null) {
            PrefixOrderedWalker.traverse(ns, cachedBehVisitor);
            firedSignals = cachedBehVisitor.getFiredSignalsByNamespace(ns);
        }  // TODO verify that once a BehaviorVisitor visited, no need to re-visit?
        return firedSignals;
    }

    public Map<String,Signal> getCachedSignalsByName () {
        if (cachedSignalsByName == null) {  // lazy populate
            cachedSignalsByName = Util.newSortedMap();
            // create a mapping of [namespaced] names to signal object
            if (Autocoder.inst().getSignalNamespaceType() == SignalNamespaceType.LOCAL) {
                for (Signal sig : getCachedSignals()) {
                    cachedSignalsByName.put(Util.joinWithPrefixes(sig.getPackageNames(), sig.getName(), null), sig);
                }
            } else {
                for (Signal sig : getCachedSignals()) {
                    cachedSignalsByName.put(sig.getName(), sig);
                }
            }
        }

        return cachedSignalsByName;
    }

    public Map<String,Profile> getCachedProfiles () {
        if (cachedProfilesByName == null) {
            cachedProfilesByName = Util.newMap();

            // How to load profiles??
        }

        return cachedProfilesByName;
    }

    public Map<String,Collection<Node>> getCachedAppliedStereotypes () {
        if (cachedAppliedStereotypes == null) {
            // initialize map of all applied custom stereotypes from XMI
            cachedAppliedStereotypes = Util.newMap();

            Set<String> profiles = Util.newSortedSet();

            // Determine which stereotype profile is the "Custom" by searching
            // the one that is defined as a Package in the model
            //- first, fetch all model package names once
            //- since package can nest, need to "flatten" nested packages
            List<gov.nasa.jpl.statechart.uml.Package> flattendPkgs = Util.newList();
            Set<String> pkgNames = Util.newSortedSet();
            for (UMLModel model : modelGroup.models()) {
                flattendPkgs.addAll(model.nestedPackages());
                for (int i=0 ; i < flattendPkgs.size() ; ++i) {
                    gov.nasa.jpl.statechart.uml.Package pkg = flattendPkgs.get(i);
                    // transform non-alphanumeric char to underscore for Profile names
                    pkgNames.add(pkg.getName().replaceAll("\\W", "_"));
                    // add any nested packages
                    flattendPkgs.addAll(pkg.nestedPackages());
                }
            }
            for (String prefix : ProfileIdentifiers.getSupportedPrefixes()) {
                if (pkgNames.contains(prefix)) {
                    // found one!
                    profiles.add(prefix);
                }
            }

            // Retrieve all known Custom stereotypes from model,
            //   looking at child level of the root node, could be costly!
            for (String profile : profiles) {
                try {
                    NodeList nodeExtended = ProfileIdentifiers.inst()
                            .model_getAllCustomStereotypes(element, profile);
                    if (Util.isDebugLevel()) {
                        Util.debug("Found " + nodeExtended.getLength() + " Custom " + profile +" stereotypes!");
                    }
                    // grab all stereotype-extended IDs
                    for (int i = 0; i < nodeExtended.getLength(); i++) {
                        String id = ProfileIdentifiers.inst()
                                .stereotype_getExtensionBaseId(ProfileLabel.TAGATTR_BASE_ELEMENT,
                                        nodeExtended.item(i));

                        Collection<Node> nodes = cachedAppliedStereotypes.get(id);
                        if (nodes == null) {
                            nodes = Util.newList();
                            cachedAppliedStereotypes.put(id, nodes);
                        }
                        nodes.add(nodeExtended.item(i));
                    }
                } catch (XPathExpressionException e) {
                    Util.reportException(e, "ModelScape.getCachedCustomStereotypes(): ");
                }
            }
        }

        return cachedAppliedStereotypes;
    }

    public Map<String,Node> getCachedCppNsStereotypes () {
        if (cachedCppNsStereotypes == null) {
            // initialize map of all C++Namespace stereotypes from XMI
            cachedCppNsStereotypes = Util.newMap();
            // Retrieve all package "C++Namespace" stereotypes from model,
            //   looking at child level of the root node, could be costly!
            try {
                NodeList nsExtensions = ProfileIdentifiers.inst()
                        .model_getAllStereotypes(element, ProfileLabel.C_ANSI_CPP_NAMESPACE);
                if (Util.isDebugLevel()) {
                    Util.debug("Found " + nsExtensions.getLength() + " package C++Namespace stereotypes!");
                }
                // grab all C++ Namespace IDs
                for (int i = 0; i < nsExtensions.getLength(); i++) {
                    String id = ProfileIdentifiers.inst()
                            .stereotype_getExtensionBaseId(ProfileLabel.TAGATTR_BASE_PACKAGE,
                                    nsExtensions.item(i));
                    cachedCppNsStereotypes.put(id, nsExtensions.item(i));
                }
            } catch (XPathExpressionException e) {
                Util.reportException(e, "ModelScape.getCachedCppNsStereotypes(): ");
            }
        }

        return cachedCppNsStereotypes;
    }

    public Map<String,String[]> getCachedFriendsList () {
        if (cachedFriendsList == null) {
            // initialize map of all SCAFriends stereotypes from XMI
            cachedFriendsList = Util.newMap();
            // Retrieve all state machine "SCAFriends" stereotypes from model,
            //   looking at child level of the root node, could be costly!
            try {
                NodeList nsExtensions = ProfileIdentifiers.inst()
                        .model_getAllStereotypes(element, ProfileLabel.SCA_FRIENDS);
                if (Util.isDebugLevel()) {
                    Util.debug("Found " + nsExtensions.getLength() + " state machine SCAFriends stereotypes!");
                }
                // grab all SCAFriends IDs and friends list
                for (int i = 0; i < nsExtensions.getLength(); i++) {
                    Node n = nsExtensions.item(i);
                    String id = ProfileIdentifiers.inst()
                            .stereotype_getExtensionBaseId(ProfileLabel.TAGATTR_BASE_STATEMACHINE,
                                    n);
                    String[] friends = Util
                            .splitPreservingQuotes(ProfileIdentifiers.inst()
                                    .scaProfile_getFriendsList(n), ",");
                    cachedFriendsList.put(id, friends);
                }
            } catch (XPathExpressionException e) {
                Util.reportException(e, "ModelScape.getCachedFriendsList(): ");
            }
        }

        return cachedFriendsList;
    }
    
   
}
