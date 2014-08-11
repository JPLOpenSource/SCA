/**
 * Created Apr 11, 2011, adapted from CppStateChartSignalWriter.
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
package gov.nasa.jpl.statechart.autocode.cpp;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.Autocoder.SignalNamespaceType;
import gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter;
import gov.nasa.jpl.statechart.model.IDesiredEvent;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.model.IDesiredEvent.EventTransitionPair;
import gov.nasa.jpl.statechart.template.GlobalVelocityModel;
import gov.nasa.jpl.statechart.uml.Event;
import gov.nasa.jpl.statechart.uml.Signal;
import gov.nasa.jpl.statechart.uml.SignalEvent;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.TimeEvent;
import gov.nasa.jpl.statechart.uml.UMLModel;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * Finds all specified signals and time events in the global UML model and
 * generates all corresponding .h files for C++ enumerated type declarations.
 * This writer behaves differently depending on the supplied command-line option
 * {@link Autocoder#OPT_SIG_NAMESPACE}:<ul>
 * <li> {@link SignalNamespaceType#NONE}: a single signal file at the root of
 * the target output directory without any namespace defined.
 * <li> {@link SignalNamespaceType#GLOBAL}: a single signal file at the
 * root of the target output directory is created, bearing the namespace that
 * was supplied with the command-line option {@link Autocoder#OPT_QF_NAMESPACE}.
 * <li> {@link SignalNamespaceType#LOCAL}: a separate signal file per namespace
 * as detected for the StateMachines that were processed, each defined within
 * that namespace.
 * </ul><p>
 * Copyright &copy; 2009-2011 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 */
public class CppStateChartSignalWriter extends UMLStateChartTemplateWriter<UMLToCppMapper> {

    protected static final String QP_CODE = "qp-statechartsignals.vm";
    protected static final String SIGNAL_FILENAME = "StatechartSignals";

    /** Keeps track of state machines to be processed per their package-namespace */
    private Map<String,List<StateMachine>> smByPackage = Util.newMap();
    /** Keeps track of events to be processed per package-namespace */
    private Map<String,SortedSet<Signal>> signalByPackage = Util.newMap();
    private Map<String,SortedSet<TimeEvent>> timeEvByPackage = Util.newMap();

    /**
     * Main constructor, calls super with a Model Group.
     */
    public CppStateChartSignalWriter (ModelGroup modelGrp) {
        super(modelGrp, new UMLToCppMapper());
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.IWriter#write()
     */
    public void write () {
        String signalFilename = tMapper.sanitize(SIGNAL_FILENAME);
        String signalFileExt = ".h";

        // Create the proxy objects that expose the type information
        GlobalVelocityModel model = new GlobalVelocityModel(tModelGrp);
        tContext.put("model", model);
        if (Autocoder.isNamespaceEnabled()) {
            tContext.put("qfNs", tMapper.mapToNamespacePrefix(Autocoder.inst().getQfNamespace()));
        } else {  // no namespace
            tContext.put("qfNs", "");
        }

        String baseNs = null;
        SignalNamespaceType nsType = Autocoder.inst().getSignalNamespaceType();
        if (nsType != SignalNamespaceType.NONE) {
            // determine the file path of the base signal file
            String[] sigBasePkgs = Util.splitPreservingQuotes(Autocoder.inst().getSigBaseNamespace(), Util.PACKAGE_SEP);
            baseNs = tMapper.mapToNamespacePathPrefix(sigBasePkgs);
        }

        if (nsType == SignalNamespaceType.LOCAL) {
            tContext.put("LOCAL_SIGNAL_FILE", true);  // creating local signal files
            /* Based on designated statemachines, determine packages to scan.
             * Algorithm:
             * 1. Add the base package.
             * 2. Start with list of state machines, and get their package.
             * 3. For each machine, find its desired events, retrieve the event
             *    signal, and fetch the signals' packages.
             * 4. Go through all the signals and TimerEvents in the model, and
             *    filter only the ones that belong to one of the packages we
             *    have seen so far.
             * 5. For each package we've scanned, generate a signal file.
             */
            // List of additional packages to scan...
            Set<String> pkgs = Util.newSet();
            //- include base signal namespace path
            pkgs.add(baseNs);

            // Bin state machines by packages for listing them in signal files
            for (StateMachine sm : tModelGrp.getStateMachines()) {
                String pkg = tMapper.mapToNamespacePathPrefix(sm.getPackageNames());
                List<StateMachine> smList = smByPackage.get(pkg);
                if (smList == null) {  // create list and add as value of map
                    smList = Util.newList();
                    smByPackage.put(pkg, smList);

                    // also create lists for signal and TimeEvent maps
                    signalByPackage.put(pkg, Util.<Signal>newSortedSet());
                    timeEvByPackage.put(pkg, Util.<TimeEvent>newSortedSet());
                }
                smList.add(sm);  // add SM to list for this package

                // fetch all desired events and include the signal's packages
                for (Event ev : model.getDesiredEvents(sm)) {
                    if (ev instanceof SignalEvent) {
                        String ePkg = tMapper.mapToNamespacePathPrefix(((SignalEvent) ev).getSignal().getPackageNames());
                        pkgs.add(ePkg);
                    } else if (ev instanceof TimeEvent) {
                        String ePkg = tMapper.mapToNamespacePathPrefix(ev.getPackageNames());
                        pkgs.add(ePkg);
                    }
                }
            }
            for (String pkg : pkgs) {
                List<StateMachine> smList = smByPackage.get(pkg);
                if (smList == null) {
                    smList = Util.newList();
                    smByPackage.put(pkg, smList);
                    signalByPackage.put(pkg, Util.<Signal>newSortedSet());
                    timeEvByPackage.put(pkg, Util.<TimeEvent>newSortedSet());
                }
            }

            // Bin Signal and Time Events by packages, for relevant packages
            for (UMLModel m : tModelGrp.models()) {
                for (Signal sig : m.getSignals()) {
                    String pkg = tMapper.mapToNamespacePathPrefix(sig.getPackageNames());
                    SortedSet<Signal> sigList = signalByPackage.get(pkg);
                    if (sigList != null) {  // otherwise, toss it out
                        sigList.add(sig);  // add signal to list for this package
                    }
                }
                for (TimeEvent tEv : m.getTimeEvents()) {
                    String pkg = tMapper.mapToNamespacePathPrefix(tEv.getPackageNames());
                    SortedSet<TimeEvent> tList = timeEvByPackage.get(pkg);
                    if (tList != null) {  // otherwise, toss it out
                        // add time event to list for this package
                        tList.add(tEv);
                    }
                }
            }

            // For each package namespace, generate a single signal file
            for (String pkg : smByPackage.keySet()) {
                Collection<StateMachine> smList = model.sort(smByPackage.get(pkg));
                tContext.put("smList", smList);
                if (pkg.length() > 0) {  // otherwise, don't set pkg list
                    tContext.put("nsPkgs", pkg.split("/"));
                }
                tContext.put("nsDefPrefix", Util.join(pkg.split("/"), ""));
                tContext.put("nsPathPrefix", pkg);
                SortedSet<Signal> sortedSignals = signalByPackage.get(pkg);
                SortedSet<TimeEvent> sortedTimeEvents = timeEvByPackage.get(pkg);
                if (pkg.equals(baseNs)) {
                    tContext.put("BASE_SIGNAL_FILE", true);
                }
                tContext.put("sortedSignals", sortedSignals);

                // populate list of all timer event names for list of SMs
                SortedSet<String> sortedTimeEventNames = Util.newSortedSet();
                for (TimeEvent tEv : sortedTimeEvents) {
                    sortedTimeEventNames.add(tMapper.mapTimeEventToLiteral(tEv));
                }
                // collect submachine time-events as well
                for (StateMachine sm : smList) {
                    IDesiredEvent query = model.querySubmachineTransitionTimeEvents(sm);
                    for (EventTransitionPair pair : query.getEventTransitions()) {
                        // all events are known to be TimeEvents
                        TimeEvent tEv = (TimeEvent) pair.getEvent();
                        sortedTimeEventNames.add(tMapper.mapTimeEventToLiteral(tEv, query.getSubstatePrefixOfEvent(pair)));
                    }
                }
                tContext.put("sortedTimeEventNames", sortedTimeEventNames);

                // populate list of all internal state machine signals, if any
                Collection<String> completionSet = new LinkedHashSet<String>();
                for (StateMachine sm : smList) {
                    completionSet.addAll(model.getCompletionSignalSet(sm, tMapper));
                }
                tContext.put("completionSet", completionSet);

                writeCode(pkg + signalFilename + signalFileExt, QP_CODE);
                tContext.remove("BASE_SIGNAL_FILE");
                tContext.remove("nsPkgs");
            }
            tContext.remove("LOCAL_SIGNAL_FILE");
        } else {
            tContext.remove("LOCAL_SIGNAL_FILE");  // NOT local signal file
            tContext.put("BASE_SIGNAL_FILE", true);  // is the only file, thus BASE
            // Write a single state chart signal file
            tContext.put("smList", model.sort(tModelGrp.getStateMachines()));

            String[] pkgs = new String[0];
            String pkg = "";
            String pkgPrefix = "";
            if (nsType == SignalNamespaceType.GLOBAL && baseNs.length() > 0) {
                // append the namespace path of the base signal file
                pkg = baseNs;
                pkgs = pkg.split("/");
                if (pkg.length() > 0) {  // otherwise, don't set pkg list
                    tContext.put("nsPkgs", pkgs);
                }
                pkgPrefix = Util.joinWithPrefixes(pkgs, "", "_");
                tContext.put("nsDefPrefix", pkgPrefix.toUpperCase());
                tContext.put("nsPathPrefix", pkg);
            } else {
                // let's see if state machines are in only a single package
                Collection<String[]> sigPkgs = model.getRequiredSignalPackagePaths(tModelGrp, tMapper);
                if (sigPkgs.size() == 1) {  // yes, use this as prefix!
                    pkgs = sigPkgs.iterator().next();
                    pkgPrefix = Util.joinWithPrefixes(pkgs, "", "_");
                    tContext.put("nsPkgs", pkgs);
                    tContext.put("nsDefPrefix", pkgPrefix.toUpperCase());
                    tContext.put("nsPathPrefix", "");
                } else {  // can't determine, we use NO prefixes
                    tContext.put("nsDefPrefix", "");
                    tContext.put("nsPathPrefix", "");
                }
            }

            tContext.put("sortedSignals", model.getSignals());
            tContext.put("sortedTimeEventNames", model.getTimeEventNames(tMapper));
            Collection<String> completionSet = new LinkedHashSet<String>();
            for (StateMachine sm : model.sort(model.getStateMachines())) {
                completionSet.addAll(model.getCompletionSignalSet(sm, tMapper));
            }
            tContext.put("completionSet", completionSet);

            // munge the signals filename
            tContext.put("pkgs", pkgs);
            String fullSigFilename = evalExpr(
                    "#mapToFileName($sigFileName,$pkgs,'signals')",
                    "write", pkgPrefix + signalFilename);
            tContext.remove("pkgs");
            tContext.put("fullSigFileName", fullSigFilename);
            String writeFilename = pkg + fullSigFilename + signalFileExt;
            if (pkg.equals(baseNs)
                    && fileAsPath(writeFilename).exists()) {  // do not override
                // a bit inefficient to bail here, but not many signal files get written
                System.out.println(getTargetLabel() + " target " + writeFilename + " exists, will not override!");
            } else {
                writeCode(writeFilename, QP_CODE);
            }

            tContext.remove("fullSigFileName");
            tContext.remove("nsPkgs");
            tContext.remove("BASE_SIGNAL_FILE");
        }

        tContext.remove("nsPathPrefix");
        tContext.remove("nsDefPrefix");
        tContext.remove("qfNs");
        tContext.remove("smList");
        tContext.remove("model");
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getVelociMacroFile()
     */
    @Override
    protected String getVelociMacroFile () {
        return CppQuantumStateMachineWriter.QP_MACROS;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getTargetLabel()
     */
    @Override
    protected String getTargetLabel () {
        return "C++ Signals";
    }

}
