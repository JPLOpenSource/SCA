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
package gov.nasa.jpl.statechart.autocode.c;

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
 * generates all corresponding .h files for C enumerated type declarations.
 * This writer behaves differently depending on the supplied command-line option
 * {@link Autocoder#OPT_SIG_NAMESPACE}:<ul>
 * <li> {@link SignalNamespaceType#NONE}: a single signal file at the root of
 * the target output directory.
 * <li> {@link SignalNamespaceType#GLOBAL}: a single signal file at the
 * root of the target output directory is created, bearing the module name
 * equivalent to the supplied command-line option {@link Autocoder#OPT_QF_NAMESPACE}.
 * <li> {@link SignalNamespaceType#LOCAL}: a separate signal file per module
 * directory, as designated for the StateMachines that were processed.
 * </ul><p>
 * Copyright &copy; 2009-2011 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 */
public class CLocalStateChartSignalWriter extends UMLStateChartTemplateWriter<UMLToCMapper> {

    public static final String SIGNAL_FILENAME = "statechart_signals";

    protected static final String QP_CODE = "qp-statechartsignals.vm";

    /** Keeps track of state machines to be processed per their module-namespace */
    private Map<String,List<StateMachine>> smByModule = Util.newMap();
    /** Keeps track of events to be processed per module-namespace */
    private Map<String,SortedSet<Signal>> signalByModule = Util.newMap();
    private Map<String,SortedSet<TimeEvent>> timeEvByModule = Util.newMap();

    /**
     * Main constructor, calls super with a Model Group.
     */
    public CLocalStateChartSignalWriter (ModelGroup modelGrp) {
        super(modelGrp, new UMLToCMapper());
        // set any "autocode" designation
        tMapper.setAutocodeDesignation(evalExpr("#autocodeDesignation", "Constructor", null));
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.IWriter#write()
     */
    public void write () {
        String signalFileName = tMapper.sanitize(SIGNAL_FILENAME);
        String signalFileExt = ".h";

        // Create the proxy objects that expose the type information
        GlobalVelocityModel model = new GlobalVelocityModel(tModelGrp);
        tContext.put("model", model);
        tContext.put("sigFileName", signalFileName);

        String baseNs = null;
        SignalNamespaceType nsType = Autocoder.inst().getSignalNamespaceType();
        if (nsType != SignalNamespaceType.NONE) {
            // determine the file path of the base signal file
            String[] sigBaseModules = Util.splitPreservingQuotes(Autocoder.inst().getSigBaseNamespace(), Util.PACKAGE_SEP);
            baseNs = tMapper.mapToNamespacePathPrefix(sigBaseModules);
        }

        if (nsType == SignalNamespaceType.LOCAL) {
            tContext.put("LOCAL_SIGNAL_FILE", true);  // creating local signal files
            /* Based on designated statemachines, determine modules to scan.
             * Algorithm:
             * 1. Add the base module.
             * 2. Start with list of state machines, and get their module.
             * 3. For each machine, find its desired events, retrieve the event
             *    signal, and fetch the signals' modules.
             * 4. Go through all the signals and TimerEvents in the model, and
             *    filter only the ones that belong to one of the modules we
             *    have seen so far.
             * 5. For each module we've scanned, generate a signal file.
             */
            // List of additional modules to scan...
            Set<String> modules = Util.newSet();
            //- include base signal namespace path
            modules.add(baseNs);

            // Bin state machines by modules for listing them in signal files
            for (StateMachine sm : tModelGrp.getStateMachines()) {
                String module = tMapper.mapToNamespacePathPrefix(sm.getPackageNames());
                List<StateMachine> smList = smByModule.get(module);
                if (smList == null) {  // create list and add as value of map
                    smList = Util.newList();
                    smByModule.put(module, smList);

                    // also create lists for signal and TimeEvent maps
                    signalByModule.put(module, Util.<Signal>newSortedSet());
                    timeEvByModule.put(module, Util.<TimeEvent>newSortedSet());
                }
                smList.add(sm);  // add SM to list for this module

                // fetch all desired events and include the signal's modules
                for (Event ev : model.getDesiredEvents(sm)) {
                    if (ev instanceof SignalEvent) {
                        String eModule = tMapper.mapToNamespacePathPrefix(((SignalEvent) ev).getSignal().getPackageNames());
                        modules.add(eModule);
                    } else if (ev instanceof TimeEvent) {
                        String eModule = tMapper.mapToNamespacePathPrefix(ev.getPackageNames());
                        modules.add(eModule);
                    }
                }
            }
            for (String module : modules) {
                List<StateMachine> smList = smByModule.get(module);
                if (smList == null) {
                    smList = Util.newList();
                    smByModule.put(module, smList);
                    signalByModule.put(module, Util.<Signal>newSortedSet());
                    timeEvByModule.put(module, Util.<TimeEvent>newSortedSet());
                }
            }

            // Bin Signal and Time Events by modules, for relevant modules
            for (UMLModel m : tModelGrp.models()) {
                for (Signal sig : m.getSignals()) {
                    String module = tMapper.mapToNamespacePathPrefix(sig.getPackageNames());
                    SortedSet<Signal> sigList = signalByModule.get(module);
                    if (sigList != null) {  // otherwise, toss it out
                        sigList.add(sig);  // add signal to list for this module
                    }
                }
                for (TimeEvent tEv : m.getTimeEvents()) {
                    String module = tMapper.mapToNamespacePathPrefix(tEv.getPackageNames());
                    SortedSet<TimeEvent> tList = timeEvByModule.get(module);
                    if (tList != null) {  // otherwise, toss it out
                        // add time event to list for this module
                        tList.add(tEv);
                    }
                }
            }

            // For each module namespace, generate a single signal file
            int modI = 0;
            for (String module : smByModule.keySet()) {
                Collection<StateMachine> smList = model.sort(smByModule.get(module));
                tContext.put("smList", smList);
                if (!module.equals(baseNs)) {
                    tContext.put("moduleIdxDelta", modI++);
                }
                String[] modulePkgs = module.split("/");
                String modulePrefix = Util.joinWithPrefixes(modulePkgs, "", "_");
                tContext.put("modulePrefix", modulePrefix.toLowerCase());
                tContext.put("moduleDefPrefix", modulePrefix.toUpperCase());
                tContext.put("modulePathPrefix", module);
                SortedSet<Signal> sortedSignals = signalByModule.get(module);
                SortedSet<TimeEvent> sortedTimeEvents = timeEvByModule.get(module);
                if (module.equals(baseNs)) {
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

                // munge the signals filename
                tContext.put("pkgs", modulePkgs);
                String fullSigFilename = evalExpr(
                		"#mapToFileName($sigFileName,$pkgs,'signals')",
                		"write", modulePrefix + signalFileName);
                tContext.remove("pkgs");
                tContext.put("fullSigFileName", fullSigFilename);
                String writeFilename = module + fullSigFilename + signalFileExt;
                if (module.equals(baseNs)
                		&& fileAsPath(writeFilename).exists()) {  // do not override
                	// a bit inefficient to bail here, but not many signal files get written
                    System.out.println(getTargetLabel() + " target " + writeFilename + " exists, will not override!");
                } else {
                    writeCode(writeFilename, QP_CODE);
                }

                tContext.remove("fullSigFileName");
                tContext.remove("BASE_SIGNAL_FILE");
            }
            tContext.remove("LOCAL_SIGNAL_FILE");
        } else {
            tContext.remove("LOCAL_SIGNAL_FILE");  // NOT local signal file
            tContext.put("BASE_SIGNAL_FILE", true);  // is the only file, thus BASE
            // Write a single state chart signal file
            tContext.put("smList", model.sort(tModelGrp.getStateMachines()));

            String[] modulePkgs = new String[0];
            String module = "";
            String modulePrefix = "";
            if (nsType == SignalNamespaceType.GLOBAL && baseNs.length() > 0) {
                // append the namespace path of the base signal file
                module = baseNs;
                modulePrefix = Util.joinWithPrefixes(module.split("/"), "", "_");
                tContext.put("modulePrefix", modulePrefix.toLowerCase());
                tContext.put("moduleDefPrefix", modulePrefix.toUpperCase());
                tContext.put("modulePathPrefix", module);
            } else {
            	// let's see if state machines are in only a single package
            	Collection<String[]> sigPkgs = model.getRequiredSignalPackagePaths(tModelGrp, tMapper);
            	if (sigPkgs.size() == 1) {  // yes, use this as prefix!
            		modulePkgs = sigPkgs.iterator().next();
            		modulePrefix = Util.joinWithPrefixes(modulePkgs, "", "_");
                    tContext.put("modulePrefix", modulePrefix.toLowerCase());
                    tContext.put("moduleDefPrefix", modulePrefix.toUpperCase());
                    tContext.put("modulePathPrefix", "");
            	} else {  // can't determine, we use NO prefixes
                    tContext.put("modulePrefix", "");
                    tContext.put("moduleDefPrefix", "");
                    tContext.put("modulePathPrefix", "");
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
            tContext.put("pkgs", modulePkgs);
            String fullSigFilename = evalExpr(
            		"#mapToFileName($sigFileName,$pkgs,'signals')",
            		"write", modulePrefix + signalFileName);
            tContext.remove("pkgs");
            tContext.put("fullSigFileName", fullSigFilename);
            String writeFilename = module + fullSigFilename + signalFileExt;
            if (module.equals(baseNs)
            		&& fileAsPath(writeFilename).exists()) {  // do not override
            	// a bit inefficient to bail here, but not many signal files get written
                System.out.println(getTargetLabel() + " target " + writeFilename + " exists, will not override!");
            } else {
                writeCode(writeFilename, QP_CODE);
            }

            tContext.remove("fullSigFileName");
            tContext.remove("BASE_SIGNAL_FILE");
        }

        tContext.remove("modulePathPrefix");
        tContext.remove("moduleDefPrefix");
        tContext.remove("modulePrefix");
        tContext.remove("smList");
        tContext.remove("sigFileName");
        tContext.remove("model");
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getVelociMacroFile()
     */
    @Override
    protected String getVelociMacroFile () {
        return CQuantumStateMachineWriter.QP_MACROS;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getTargetLabel()
     */
    @Override
    protected String getTargetLabel () {
        return "C Signals";
    }

}
