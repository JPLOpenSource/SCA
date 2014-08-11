/**
 * Created Oct 27, 2009.
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

package gov.nasa.jpl.statechart.autocode.promela;

import gov.nasa.jpl.statechart.Pair;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.autocode.IGenerator;
import gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter;
import gov.nasa.jpl.statechart.model.IDesiredEvent;
import gov.nasa.jpl.statechart.model.IDesiredEvent.EventTransitionPair;
import gov.nasa.jpl.statechart.model.IDesiredEvent.QueryPolicy;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.template.FlattenedVelocityModel;
import gov.nasa.jpl.statechart.template.FunctionCall;
import gov.nasa.jpl.statechart.uml.Behavior;
import gov.nasa.jpl.statechart.uml.ConnectionPointReference;
import gov.nasa.jpl.statechart.uml.Event;
import gov.nasa.jpl.statechart.uml.Namespace;
import gov.nasa.jpl.statechart.uml.OpaqueBehavior;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.Vertex;

import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * <p>
 * This class writes out a state machine in the form of Promela code based on the
 * second generation model pattern developed by Garth Watney and Ed Gable at JPL.
 * The code is generated from an internal representation of UML meta-object
 * classes, written out via Velocity templates.
 * </p>
 * <p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Leonard J. Reder <reder@jpl.nasa.gov>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 */
public class PromelaStateMachineWriter extends UMLStateChartTemplateWriter<UMLToPromelaMapper> {
    // Promela Main.pml filename and template
    protected static final String PROMELA_MAIN_CODE = "pml-main.vm";
    protected static final String MAIN_FILENAME = "Main";
    // Promela Init.pml filename and template
    // Only generated if it does not exist
    protected static final String PROMELA_INIT_CODE = "pml-init.vm";
    protected static final String INIT_FILENAME = "Init";
    // Promela ManStubs.pml filename and template
    // Only generated if it does not exist
    protected static final String PROMELA_MAN_STUBS_CODE = "pml-man-stubs.vm";
    protected static final String MAN_STUBS_FILENAME = "ManStubs";

    /** Filename for velocity macro */
    protected static final String PROMELA_MACROS = "pml-velocimacros.vm";
    public static final String[] EMPTY_PARAMS = new String[0];

    protected static final String VMACRO_NEWLINE          = "blankLine";
    protected static final String VMACRO_SEP_LINES        = "separatorLines";
    protected static final String VMACRO_RDZV_CHANNEL     = "rendezvousChannel";
    protected static final String VMACRO_RUN_ORTHO        = "runRegion";
    protected static final String VMACRO_ACTIVATE_ORTHO   = "activateRegion";
    protected static final String VMACRO_DEACTIVATE_ORTHO = "deactivateRegion";
    protected static final String VMACRO_DISPATCH_ORTHO   = "dispatchRegion";
    protected static final String VMACRO_NULL_STATE_BEGIN = "beginNullState";
    protected static final String VMACRO_NULL_STATE_END   = "endNullState";
    protected static final String VMACRO_BRANCH_EXIT_BEGIN = "beginExitBranch";
    protected static final String VMACRO_BRANCH_EXIT_END  = "endExitBranch";
    protected static final String VMACRO_LABEL_STATE      = "labelState";
    protected static final String VMACRO_BRANCH_SIGNAL    = "signalBranch";
    protected static final String VMACRO_BRANCH_NULL_EVT  = "nullEventBranch";
    protected static final String VMACRO_ELSE_EXECUTE     = "elseExec";
    protected static final String VMACRO_ELSE_NOOP        = "elseNoop";
    protected static final String VMACRO_TARGET_UNKNOWN   = "gotoTargetUnknown";
    protected static final String VMACRO_FOOTER_GUARD     = "footerGuard";
    protected static final String VMACRO_FOOTER_ORTHO     = "footerOrtho";
    protected static final String VMACRO_FOOTER_BLOCK     = "footerBlock";

    /** Filename for main template */
    protected static final String PML_CODE                = "pml-code.vm";
    protected static final String PML_PROC_MAIN           = "pml-proc-main.vm";
    protected static final String PML_PROC_ORTHO          = "pml-proc-ortho.vm";

    protected static final String PML_ACTION_EVENT        = "pml-action-event.vm";
    protected static final String PML_ACTION_FUNCTION     = "pml-action-function.vm";

    protected static final String PML_STATE_BEGIN         = "pml-statebegin.vm";
    protected static final String PML_STATE_END           = "pml-stateend.vm";

    protected static final String PML_TRANS_GUARD_BEGIN   = "pml-transition-beginguard.vm";
//    protected static final String PML_TRANS_DEEPHISTORY   = "pml-transition-deephistory.vm";
    protected static final String PML_TRANS_STATE         = "pml-transition-state.vm";

    // Flattened-hierarchy query model used to write out state machines.
    private FlattenedVelocityModel tModel = null;
    // Stores a map of state name to how many orthogonal regions it contains.
    private Map<String,Integer> regionCountMap = null;
    // Stores a map of a region to its channel name and index.
    private Map<Region,Pair<String,Integer>> channelNameIdMap = null;

    /**
     * Main constructor, creates a writer given a UML Model Group.
     * 
     * @param modelGrp the group of UML model(s) used for writing target output.
     */
    public PromelaStateMachineWriter (ModelGroup modelGrp) {
        super(modelGrp, new UMLToPromelaMapper());

        tModel = new FlattenedVelocityModel(tModelGrp, null);
        regionCountMap = new HashMap<String,Integer>();
        channelNameIdMap = new HashMap<Region, Pair<String,Integer>>();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.IWriter#write()
     */
    public void write () {
        // create writer-specific files for each of the top-level state machines
        for (StateMachine stateMachine : tModelGrp.getStateMachines()) {
            regionCountMap.clear();
            channelNameIdMap.clear();
            writeStateMachine(stateMachine);
        }
        writeMainFiles();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getVelociMacroFile()
     */
    @Override
    protected String getVelociMacroFile () {
        return PROMELA_MACROS;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getTargetLabel()
     */
    @Override
    protected String getTargetLabel () {
        return IGenerator.Kind.Promela.label();
    }

    private void writeMainFiles () {
        String mainFilename = tMapper.sanitize(MAIN_FILENAME) + ".pml";
        String initFilename = tMapper.sanitize(INIT_FILENAME) + ".pml";
        String manFilename  = tMapper.sanitize(MAN_STUBS_FILENAME) + ".pml";

        // Pass in the query model to the velocity templates
        tContext.put("model", tModel);

        // Write the code to a file
        writeCode(mainFilename, PROMELA_MAIN_CODE);
        if (fileAsPath(initFilename).exists()) {  // do not override Init
            System.out.println(getTargetLabel() + " target " + initFilename + " exists, will not override!");
        } else {
            writeCode(initFilename, PROMELA_INIT_CODE);
        }
//        if (fileAsPath(manFilename).exists()) {  // do not override ManStub
//            System.out.println(getTargetLabel() + " target " + manFilename + " exists, will not override!");
//        } else {
            writeCode(manFilename,  PROMELA_MAN_STUBS_CODE);
//        }

        tContext.remove("model");
    }

    private void writeStateMachine (StateMachine stateMachine) {
        String classname = tMapper.mapToFileName(stateMachine);
        String promelaFilename = classname + ".pml";

        // Sets the active StateMachine on the query model
        tModel.setStateMachine(stateMachine);
        tContext.put("model", tModel);

        Map<Vertex,Double> sourceTimeoutMap = tModel.getEffectiveTimeoutMap();
        if (Util.isDebugLevel()) {
            Util.debug("Mapping of vertex to effective timeouts:");
            for (Vertex v : sourceTimeoutMap.keySet()) {
                Util.debug("  * " + v.getQualifiedName() + " : " + sourceTimeoutMap.get(v));
            }
        }
 
        // Compose and write the code file
        Writer w = beginWriteCode(promelaFilename);
        composeCode(w);
        endWriteCode(promelaFilename);

        // unset "model" variable
        tContext.remove("model");
    }

    private void composeCode (Writer w) {
        // Set up some "global" context variables for later templates
        StateMachine sm = tModel.getStateMachine();
        String smName = tMapper.mapToTypeName(sm);
        tContext.put("sm", sm);
        tContext.put("smName", smName);
        tContext.put("smGuiName", smName.toLowerCase());

        System.out.println("** State-Machine: " + smName);
        writeCodePart(w, PML_CODE);

        // Configure orthogonal regions
        //- first, tally orthogonal regions by states, and map regions to channel ID
        for (Region region : tModel.getLocalOrthogonalRegions(sm)) {
            String parentState = tMapper.mapToFunctionName(region.getState());
            Integer cnt = regionCountMap.get(parentState);
            if (cnt == null) {  // new state, start new counter
                cnt = 0;
            }
            // establish mapping of region to parent state name and channel ID
            channelNameIdMap.put(region, new Pair<String,Integer>(parentState, cnt));
            // update orthogonal region count for parent state
            ++cnt;
            regionCountMap.put(parentState, cnt);
        }

        // First write the proc for the state-machine
        boolean isOrtho = false;  // flag indicating whether inside an ortho
        tContext.put("isOrtho", isOrtho);  // no null node 
        tContext.put("typeName", smName);
        makeProc(w, sm, isOrtho);
        
        // Then write the proc for each orthogonal region within this state-machine
        for(Region region:tModel.getLocalOrthogonalRegions(sm)) {
            isOrtho = true;
            tContext.put("isOrtho", isOrtho);  // no null node 
            tContext.put("typeName", tMapper.mapToQualifiedName(region));
            makeProc(w, region, isOrtho);
        }
        
        tContext.remove("typeName");
        tContext.remove("isOrtho");

        // remove context vars
        tContext.remove("sm");
        tContext.remove("smName");
        tContext.remove("smGuiName");
    }

    private void makeProc (Writer w, Namespace machineOrRegion, boolean isOrtho) {
        tContext.put("typeObj", machineOrRegion);
        if (isOrtho) {
            writeCodePart(w, PML_PROC_ORTHO);
            tMapper.incIndent();
        } else {
            writeCodePart(w, PML_PROC_MAIN);
        }

        Collection<State> states = tModel.sort(tModel.getLeafStatesAboveOrtho(machineOrRegion));
        // Initialize rendezvous point channels
        for (State state : states) {
            String stateName = tMapper.mapToFunctionName(state);
            if (regionCountMap.containsKey(stateName)) {
                tContext.put("parentName", stateName);
                tContext.put("count", regionCountMap.get(stateName));
                String[] params = { "parentName", "count" };
                invokeVmacro(w, VMACRO_RDZV_CHANNEL, "makeProc", params);
            }
        }
        tContext.remove("parentName");
        tContext.remove("count");

        // Start up any orthogonal region procs
        invokeVmacro(w, VMACRO_NEWLINE, "makeProc", EMPTY_PARAMS);
        clearCodeWritten();
        for (Region region : tModel.sort(tModel.getLocalOrthogonalRegions(machineOrRegion))) {
            Pair<String,Integer> stChPair = channelNameIdMap.get(region);
            tContext.put("regionName", tMapper.mapToQualifiedName(region));
            tContext.put("parentName", stChPair.getFirst());
            tContext.put("channelID", stChPair.getSecond());
            String[] params = { "parentName", "regionName", "channelID" };
            invokeVmacro(w, VMACRO_RUN_ORTHO, "makeProc", params);
        }
        if (wasCodeWritten()) {
            invokeVmacro(w, VMACRO_NEWLINE, "makeProc", EMPTY_PARAMS);
        }
        tContext.remove("regionName");
        tContext.remove("parentName");
        tContext.remove("channelID");

        // Transition to initial state
        if (isOrtho) {
            invokeVmacro(w, VMACRO_NULL_STATE_BEGIN, "makeProc", EMPTY_PARAMS);
        }
        Pseudostate initialState = tModel.getInitialState(machineOrRegion);
        if (initialState == null) {
            Util.error("Error! Initial state expected but NOT found within " + machineOrRegion.getQualifiedName());
        } else {
            Transition initialTran = tModel.getInitialTransition(initialState);
            if (initialTran != null) {
                if (isOrtho) {  // special indenting for ortho init action
                    tMapper.incIndent();
                    tMapper.incIndent();
                    tMapper.incIndent();
                }
                tContext.put("isInitTran", true);
                makeStateTransition(w, initialState, initialState, initialTran);
                tContext.remove("isInitTran");
                if (isOrtho) {
                    tMapper.decIndent();
                    tMapper.decIndent();
                    tMapper.decIndent();
                }
            }
        }
        if (isOrtho) {
            invokeVmacro(w, VMACRO_NULL_STATE_END, "makeProc", EMPTY_PARAMS);
        }

        // Iterate through states in the scope of the class
        for (State state : states) {
            if (Util.isDebugLevel()) {
                Util.debug("=> makeProc: making state: " + state.getQualifiedName());
            }
            System.out.println("**** state: " + state.getName());
            makeState(w, state, machineOrRegion, isOrtho);
        }

        // End the proc block
        if (isOrtho) {
            tMapper.decIndent();
            invokeVmacro(w, VMACRO_FOOTER_ORTHO, "makeProc", EMPTY_PARAMS);
        }
        invokeVmacro(w, VMACRO_FOOTER_BLOCK, "makeProc", EMPTY_PARAMS);

        tContext.remove("typeObj");
    }

    private void makeState (Writer w, State state, Namespace container, boolean isOrtho) {
        // Current working State
        State curState = state;
        // Remember parent composite state of any junction init
        State compositeStateJunctionInit = null;

        // Generate the labels for the chain of states into target state
        // Complication:
        // If an initial state goes into a junction, then we can't simply
        //  tack on labels.  The parent state containing the junction needs to
        //  have a separate label definition.
        // We also cannot re-delcare the same parent label twice, so need
        //  a tracker list...
        while (curState != null) {
            // Remember the outermost of Init Based Ancester Chain
            State outermost = null;

            for (State ancestor : tModel.getInitBasedAncestorChain(curState, container)) {
                if (outermost == null) {  // remember the outermost state of chain
                    outermost = ancestor;

                    // code out separator lines in autocode
                    invokeVmacro(w, VMACRO_SEP_LINES, "makeState", EMPTY_PARAMS);
                }
                if (Util.isDebugLevel()) {
                    Util.debug("=> makeState: Generating labels for chain of states into target state, label of state: "
                            + ancestor.getQualifiedName());
                }
                String ancestorName = tMapper.mapToFunctionName(ancestor);
                // stamp out the state label
                tContext.put("ancestorName", ancestorName);
                String[] params = { "ancestorName" };
                invokeVmacro(w, VMACRO_LABEL_STATE, "makeState", params);
                tContext.remove("ancestorName");

                // stamp out any do actions, single-space-separated
                clearCodeWritten();
                makeAction(w, ancestor.getEntry(), " ");
                // stamp out activation of orthogonal regions, if any
                for (Region region : tModel.getChildOrthogonalRegions(curState)) {
                    Pair<String,Integer> stChPair = channelNameIdMap.get(region);
                    tContext.put("parentName", stChPair.getFirst());
                    tContext.put("channelID", stChPair.getSecond());
                    String[] params2 = { "parentName", "channelID" };
                    invokeVmacro(w, VMACRO_ACTIVATE_ORTHO, "makeState", params2);
                }
                if (!wasCodeWritten()) {  // no trans-action or activate; newline
                    invokeVmacro(w, VMACRO_NEWLINE, "makeState", EMPTY_PARAMS);
                }
                tContext.remove("parentName");
                tContext.remove("channelID");
            }

            if (outermost == null) {  // nothing new to code out
                break;
            }
            // label and begin the target state
            tContext.put("stateName", tMapper.mapToFunctionName(curState));
            writeCodePart(w, PML_STATE_BEGIN);

            // Write the code for every transition out of this state-chain;
            //  for this we need to get the entire ancestor chain of the state,
            //  and process transition-events inside out.
            Set<String> processedSignals = Util.newSet();
            for (State ancestor : tModel.reverse(tModel.getAncestorChain(curState, container))) {
                if (Util.isDebugLevel()) {
                    Util.debug("=> makeState: Getting transition out of state chain, on state: "
                            + ancestor.getQualifiedName());
                }
                tModel.clearDesiredEvents(QueryPolicy.NULL_EV);
                IDesiredEvent desiredEventQuery = tModel.queryDesiredEvents(ancestor, QueryPolicy.NULL_EV);
                for (EventTransitionPair pair : desiredEventQuery.getEventTransitions()) {
                    Event ev = pair.getEvent();
                    Transition transition = pair.getTransition();
                    String[] evPrefix = desiredEventQuery.getSubstatePrefixOfEvent(pair);
                    String evName = tMapper.mapEventToLiteral(ev, evPrefix);
                    if (Util.isDebugLevel()) {
                        Util.debug("  * transition event: " + evName);
                    }

                    // Promela allows transitions from a state triggered by
                    // the same Signal or by a Timer, but SKIPPING still
                    // necessary to address inner/outer event overlap issues.
                    if (processedSignals.contains(evName)) continue;

                    if (ancestor.isOrthogonal() ||
                            ancestor.equals(transition.getSource())) {

                        // check for TimeEvent transition
                        if (tModel.skipTimeEventTransition(ancestor, transition)) {
                            continue;  // NULLify time transition!
                        }
                    
                        // Now we're sure we want to stamp out a signal branch...
                        // This event is handled at this state level.
                        if (ev == null) {
                            // Handle null event
                            invokeVmacro(w, VMACRO_BRANCH_NULL_EVT, "makeState", EMPTY_PARAMS);
                        } else {
                            // Consider the event signal "processed"
                            processedSignals.add(evName);
                            tContext.put("evName", evName);
                            String[] params = { "evName", "smName"};
                            invokeVmacro(w, VMACRO_BRANCH_SIGNAL, "makeState", params);
                            tContext.remove("evName");
                        }

                        tMapper.incIndent();
                        if (ancestor.isOrthogonal() && ev != null) {
                            for (Region region : desiredEventQuery.getDesiringRegionsOfState(ancestor, pair)) {
                                Pair<String,Integer> stChPair = channelNameIdMap.get(region);
                                tContext.put("parentName", stChPair.getFirst());
                                tContext.put("regionName", tMapper.mapToFunctionName(region));
                                tContext.put("channelID", stChPair.getSecond());
                                String[] params2 = { "parentName", "regionName", "channelID" };
                                invokeVmacro(w, VMACRO_DISPATCH_ORTHO, "makeState", params2);
                            }
                            tContext.remove("parentName");
                            tContext.remove("regionName");
                            tContext.remove("channelID");
                        }
                        if (ancestor.equals(transition.getSource())) {
                            if (transition.isInternal()) {
                                if (Util.isDebugLevel()) {
                                    Util.debug("    -> stamping out internal event action");
                                }
                                makeAction(w, transition.getEffect(), tMapper.indentation());
                            } else {
                                if (Util.isDebugLevel()) {
                                    Util.debug("    -> stamping out transition from state " + ancestor.getQualifiedName());
                                }
                                makeStateTransition(w, curState, ancestor, transition);
                            }
                        }
                        tMapper.decIndent();
                    }
                }
            }

            // Stamp out initial junction transition, if any
            if (compositeStateJunctionInit != null) {
                // process any initial transition from state region
                Pseudostate initialState = tModel.getInitialState(compositeStateJunctionInit);
                if (initialState != null) {
                    Transition initialTran = tModel.getInitialTransition(initialState);
                    if (initialTran != null) {
                        invokeVmacro(w, VMACRO_ELSE_EXECUTE, "makeStateTransition", EMPTY_PARAMS);
                        tMapper.incIndent();
                        tContext.put("isInitTran", true);
                        // produce initial transition into junction
                        makeStateTransition(w, initialState, initialState, initialTran);
                        tContext.remove("isInitTran");
                        tMapper.decIndent();
                    }
                }

            }

            // Stamp out the orthogonal-region exit branch, if in orthogonal region
            // Use computeTransitionPathActivity from present state to container.
            // Then ignore the entry into container.
            if (isOrtho && container instanceof Region) {
                invokeVmacro(w, VMACRO_BRANCH_EXIT_BEGIN, "makeState", EMPTY_PARAMS);

                // Get exit action chain to parent state, and make actions
                tMapper.incIndent();
                State parentState = ((Region) container).getState();
                for (Behavior exitBeh : tModel.computeTransitionPathActivity(curState, parentState).getFirst()) {
                    makeAction(w, exitBeh, tMapper.indentation());
                }
                tMapper.decIndent();

                invokeVmacro(w, VMACRO_BRANCH_EXIT_END, "makeState", EMPTY_PARAMS);
            }

            if (compositeStateJunctionInit != null) {
                tContext.put("skipElseBranch", true);
            }
            writeCodePart(w, PML_STATE_END);
            tContext.remove("skipElseBranch");

            compositeStateJunctionInit = null;
            if (outermost != null && tModel.isOnInitPath(outermost)) {
                // move up to parent state, exit yet?
                curState = outermost.getParentState();
                if (container.equals(outermost)) {
                    curState = null;
                } else {
                    // loop continues ONLY if not at container
                    compositeStateJunctionInit = curState;
                }
            } else {
                curState = null;
            }

            tContext.remove("stateName");
        }
    }

    // stack of flags indicating whether recursive entry is for junction/choice or not.
    private Stack<Boolean> junctionGuardStack = new Stack<Boolean>();

    private void makeStateTransition (Writer w, Vertex source, Vertex ancestor, Transition transition) {
        if (junctionGuardStack.empty()) {
            junctionGuardStack.push(false);
        }
        if (tModel.hasGuard(transition)) {
            String guardName = transition.getGuard().getName();
            tContext.put("guardName", guardName);
            writeCodePart(w, PML_TRANS_GUARD_BEGIN);
            tContext.remove("guardName");
            tMapper.incIndent();
        }

        // Get the chains of exit & entry activities from source to target
        Vertex tgtState = tModel.getTarget(ancestor, transition);
        Pair<List<Behavior>,List<Behavior>> actionPair = tModel.computeTransitionPathActivity(source, tgtState);
        List<Behavior> exitActions = actionPair.getFirst();
        List<Behavior> entryActions = actionPair.getSecond();

        // If NOT initial transition, stamp out the exit chain of actions
        if (tContext.get("isInitTran") != Boolean.TRUE) {
            for (Behavior exitBeh : exitActions) {
                if (source instanceof State) {
                    State sourceState = (State) source;
                    if (exitBeh == sourceState.getExit()) {
                        // we're doing an exit action out of source state
                        // stamp out orthogonal regions exits, if any
                        for (Region region : tModel.getChildOrthogonalRegions(sourceState)) {
                            Pair<String,Integer> stChPair = channelNameIdMap.get(region);
                            tContext.put("parentName", stChPair.getFirst());
                            tContext.put("channelID", stChPair.getSecond());
                            String[] params = { "parentName", "channelID" };
                            invokeVmacro(w, VMACRO_DEACTIVATE_ORTHO, "makeState", params);
                        }
                        tContext.remove("parentName");
                        tContext.remove("channelID");
                    }
                }
                makeAction(w, exitBeh, tMapper.indentation());
            }
        }
        // Do action, perform all the activities along transition path
        makeAction(w, transition.getEffect(), tMapper.indentation());
        // Stamp out the entry chain of actions
        for (Behavior entryBeh : entryActions) {
            makeAction(w, entryBeh, tMapper.indentation());
        }

        // Handle target state(s) by type, assumes we're external transition!
        //  (Pseudostate handling not supported:  deepHistory, join, fork
        //   entryPoint, exitPoint, terminate)
        tContext.put("target", tgtState);
        if (tgtState instanceof Pseudostate) {
            Pseudostate tgtPseudo = (Pseudostate) tgtState;
            switch (tgtPseudo.getKind()) {
            case junction:
            case choice:
                junctionGuardStack.push(true);  // junction/choice guarding!
                List<Transition> junctionList = tModel.getJunctionTransitions(tgtPseudo);
                // For a complex junction, every guard except for the one before
                // the 'else' needs to be stamped out with its own else.
                // The last one before the 'else' will need its own else if no
                // 'else' transition has been specified.
                for (int i=0; i < junctionList.size(); ++i) {
                    Transition juncTrans = junctionList.get(i);
                    if (i < junctionList.size()-1) {  // not yet last one; last one is no-guard/"else"
                        makeStateTransition(w, tgtState, tgtState, juncTrans);
                        if (i < junctionList.size()-2) {
                            // not yet the last non-else, so wrap-up 'if'
                            invokeVmacro(w, VMACRO_ELSE_NOOP, "makeStateTransition", EMPTY_PARAMS);
                            invokeVmacro(w, VMACRO_FOOTER_GUARD, "makeStateTransition", EMPTY_PARAMS);
                        }
                    } else {  // i == size()-1, the final, 'else' transition
                        if (null != juncTrans) {  // valid no-guard transition
                            invokeVmacro(w, VMACRO_ELSE_EXECUTE, "makeStateTransition", EMPTY_PARAMS);
                            tMapper.incIndent();
                            // take the else junction transition!
                            makeStateTransition(w, tgtState, tgtState, juncTrans);
                            tMapper.decIndent();
                        } else {  // uh-oh, unknown!
                            invokeVmacro(w, VMACRO_ELSE_NOOP, "makeStateTransition", EMPTY_PARAMS);
                        }
                        invokeVmacro(w, VMACRO_FOOTER_GUARD, "makeStateTransition", EMPTY_PARAMS);
                    }
                }
                junctionGuardStack.pop();  // restore last guard state in stack
                break;
            }
        } else if (tgtState instanceof ConnectionPointReference) {
            // TODO process connection point reference to a submachine?!
            // refer to lines 1341--1380 in StateChartCWriter.writeToConnectionPointReference()
        } else if (tgtState instanceof State)  {
            //- just a regular state (probably also handles submachine state automagically?!)
            writeCodePart(w, PML_TRANS_STATE);
        } else {
            String[] params = { "target" };
            invokeVmacro(w, VMACRO_TARGET_UNKNOWN, "makeStateTransition", params);
        }

        if (tModel.hasGuard(transition)) {
            tMapper.decIndent();
            if (!junctionGuardStack.peek()) {
                // we're not in a junction/choice transition, so wrap-up if
                invokeVmacro(w, VMACRO_ELSE_NOOP, "makeStateTransition", EMPTY_PARAMS);
                invokeVmacro(w, VMACRO_FOOTER_GUARD, "makeStateTransition", EMPTY_PARAMS);
            }
        }
        tContext.remove("target");
        tContext.remove("if");
    }

    private void makeAction (Writer w, Behavior activity, String spacing) {
        if (activity != null) {
            tContext.put("s", spacing);
            System.out.println("   activity: " + activity.getName());
            if (activity instanceof OpaqueBehavior) {
                OpaqueBehavior opActivity = (OpaqueBehavior) activity;
                String body = opActivity.getName();
                if (opActivity.body().length > 0) {
                    // fetch first body text as name
                    body = opActivity.body()[0];
                }
// requires newest UMLToPromelaMapper
//                String actionCandidate = tMapper.mapOpaqueBodyToEvents(body);
                String actionCandidate = "";
                if (actionCandidate != null && actionCandidate.length() > 0) {
                    tContext.put("action", actionCandidate);
                    writeCodePart(w, PML_ACTION_EVENT);
                    tContext.remove("action");
                } else if (FunctionCall.isFunctionCall(body)) {
                    // treat action as function call
                    tContext.put("funcCall", tModel.getCallParts(body));
                    writeCodePart(w, PML_ACTION_FUNCTION);
                    tContext.remove("funcCall");
                } else {
                    // treat as event
                    tContext.put("action", body);
                    writeCodePart(w, PML_ACTION_EVENT);
                    tContext.remove("action");
                }
            } else {
                for (String actionStr : activity.actionList()) {
                    if (actionStr != null && actionStr.length() > 0) {
                        if (FunctionCall.isFunctionCall(actionStr)) {
                            // treat action as function call
                            tContext.put("funcCall", tModel.getCallParts(actionStr));
                            writeCodePart(w, PML_ACTION_FUNCTION);
                            tContext.remove("funcCall");
                        } else { // Event action, so publish as signal event
                            tContext.put("action", actionStr);
                            writeCodePart(w, PML_ACTION_EVENT);
                            tContext.remove("action");
                        }
                    }
                }
            }
            tContext.remove("s");
        }
    }

}
