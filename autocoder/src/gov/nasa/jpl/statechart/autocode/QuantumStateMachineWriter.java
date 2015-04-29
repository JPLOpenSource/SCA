/**
 * Created May 12, 2010, by refactoring from *QuantumStateMachineWriter.
 * <p>
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
package gov.nasa.jpl.statechart.autocode;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.model.IDesiredEvent;
import gov.nasa.jpl.statechart.model.IDesiredEvent.EventTransitionPair;
import gov.nasa.jpl.statechart.model.IDesiredEvent.QueryPolicy;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.model.UMLModelGroup;
import gov.nasa.jpl.statechart.template.FunctionCall;
import gov.nasa.jpl.statechart.template.OOVelocityModel;
import gov.nasa.jpl.statechart.template.TargetLanguageMapper;
import gov.nasa.jpl.statechart.uml.Behavior;
import gov.nasa.jpl.statechart.uml.ConnectionPointReference;
import gov.nasa.jpl.statechart.uml.Event;
import gov.nasa.jpl.statechart.uml.FinalState;
import gov.nasa.jpl.statechart.uml.Namespace;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.PseudostateKind;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.Signal;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.TimeEvent;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.Vertex;

import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * <p>
 * This class writes out a state machine for a target language, based on the
 * Quantum Framework model developed by Miro Samek.  The code is generated
 * from an internal representation of UML metaobject classes, written out via
 * Velocity templates.
 * </p><p>
 * [2010.01.20] Previously, the logic for generating code resided largely in
 * the Velocity templates, resulting in compact templates, but making
 * target-code pattern not easily discernible.  The new implementation pulled
 * the logic into Java code, dicing up target-code patterns in many more chunks
 * of Velocity files.
 * </p><p>
 * Copyright &copy; 2009--2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public abstract class QuantumStateMachineWriter<T extends TargetLanguageMapper> extends UMLStateChartTemplateWriter<T> {
    /** Filename for velocity macro */
    public static final String QP_MACROS = "qp-velocimacros.vm";
    public static final String[] EMPTY_PARAMS = new String[0];

    protected static final String VMACRO_ELSE               = "elseStmt";
    protected static final String VMACRO_LITERAL_CODE       = "literalCode";
    protected static final String VMACRO_INIT_TRANS         = "initialTransitionCase";
    protected static final String VMACRO_COMPLETION_MACHINE = "machineCompletionEvent";
    protected static final String VMACRO_COMPLETION_STATE   = "stateCompletionEvent";
    protected static final String VMACRO_EXIT_CONN_BEGIN    = "exitTransitionBegin";
    protected static final String VMACRO_EXIT_CONN_CASE     = "exitTransitionCase";
    protected static final String VMACRO_EXIT_CONN_END      = "exitTransitionEnd";
    protected static final String VMACRO_HIDDEN_FINAL       = "gotoHiddenFinal";
    protected static final String VMACRO_CHOICE_UNKNOWN     = "gotoChoiceBranchUnknown";
    protected static final String VMACRO_PSEUDO_TERMINATE   = "gotoPseudostateTerminate";
    protected static final String VMACRO_TARGET_UNKNOWN     = "gotoTargetUnknown";
    protected static final String VMACRO_FOOTER_GUARD       = "footerGuardBlock";
    protected static final String VMACRO_FOOTER_ENDCASE     = "footerCaseEnd";
    protected static final String VMACRO_FOOTER_ENDGUARDEDCASE = "footerGuardedCaseEnd";
    protected static final String VMACRO_FOOTER_HANDLED     = "footerCaseHandled";
    protected static final String VMACRO_FOOTER_ENDCASESUBM = "footerSubmachineCaseEnd";
    protected static final String VMACRO_FOOTER_INITIAL     = "footerInitialMethod";

    /** Filename for impl-class template */
    protected static final String QP_IMPL = "qp-impl.vm";

    /** Filenames for code template parts */
    protected static final String QP_INCLUDES            = "qp-includes.vm";
    protected static final String QP_CONSTRUCTOR_ACTIVE  = "qp-constructor-active.vm";
    protected static final String QP_CONSTRUCTOR_HSM     = "qp-constructor-hsm.vm";
    protected static final String QP_METHOD_INITIAL      = "qp-method-initial.vm";
    protected static final String QP_METHOD_FINAL        = "qp-method-final.vm";

    protected static final String QP_SUBMACHINE_METHODS  = "qp-submachine-methods.vm";
    protected static final String QP_SUBMACHINE_ENTRY    = "qp-submachine-entry.vm";

    protected static final String QP_ACTION_EVENT        = "qp-action-event.vm";
    protected static final String QP_ACTION_FUNCTION     = "qp-action-function.vm";

    protected static final String QP_STATE_BEGIN         = "qp-statebegin.vm";
    protected static final String QP_STATE_ENTRY_BEGIN   = "qp-state-entry-begin.vm";
    protected static final String QP_STATE_ENTRY_END     = "qp-state-entry-postaction.vm";
    protected static final String QP_STATE_EXIT_BEGIN    = "qp-state-exit-begin.vm";
    protected static final String QP_STATE_EXIT_END      = "qp-state-exit-postaction.vm";
    protected static final String QP_STATE_BAIL          = "qp-state-bail.vm";
    protected static final String QP_STATE_EVENTTRANS    = "qp-state-eventtransition.vm";
    protected static final String QP_STATE_ORTHOGONAL    = "qp-state-orthogonal.vm";
    protected static final String QP_STATE_SUBMACHINE    = "qp-state-submachine.vm";
    protected static final String QP_STATE_END           = "qp-stateend.vm";

    protected static final String QP_TRANS_GUARD_BEGIN   = "qp-transition-beginguard.vm";
    protected static final String QP_TRANS_DEEPHISTORY   = "qp-transition-deephistory.vm";
    protected static final String QP_TRANS_CHOICE        = "qp-transition-choice.vm";
    protected static final String QP_TRANS_CONNPTREF     = "qp-transition-connptref.vm";
    protected static final String QP_TRANS_STATE         = "qp-transition-state.vm";

    // Query model instance, set by the writeStateMachine() method.
    protected OOVelocityModel tModel = null;
    // Flag indicating whether State Machine is referenced by a substate
    protected boolean tMachineTerminable = false;
    // Flag indicating whether or NOT inside Active object, for Ortho regions
    protected boolean tIsHsm = false;

    /**
     * Main constructor, creates a writer given a UML Model Group.
     * 
     * @param modelGrp  the group of UML model(s) used for writing target output.
     * @param mapper    instance of TargetLanguageMapper subclass
     */
    public QuantumStateMachineWriter (ModelGroup modelGrp, T mapper) {
        super(modelGrp, mapper);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.IWriter#write()
     */
    public void write () {
        // create writer-specific files for each of the top-level state machines
        for (StateMachine stateMachine : tModelGrp.getStateMachines()) {
            writeStateMachine(stateMachine);
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getVelociMacroFile()
     */
    @Override
    protected String getVelociMacroFile () {
        return QP_MACROS;
    }

    /**
     * Target language-specific method for generating the code of a single
     * StateMachine.  Must assign parent data member <code>tModel</code>!
     * 
     * @param stateMachine  UML {@link StateMachine} object to generate code for
     */
    protected abstract void writeStateMachine (StateMachine stateMachine);

    /**
     * Returns the name of the Velocity template for stamping out inline main
     * application method, or null if not applicable for target language.
     * 
     * @return  String name of Velocity template name for inline main method;
     *      <code>null</code> if not applicable.
     */
    protected abstract String inlineMainTemplate ();

    /**
     * Enables adding more common methods to class for the given
     * {@link Namespace} <code>ns</code>, if necessary.
     * 
     * @param w   FileWriter to target code file.
     * @param ns  {@link StateMachine} or {@link Region} for which to define
     *      additional common class members.
     */
    protected abstract void makeMoreCommonMethods (Writer w, Namespace ns);

    /**
     * Builds the code file, starting with imports/includes, then composing
     * classes for the machines and orthogonal regions.
     * <p>
     * For each machine or orthogonal region, this method first composes the
     * class definitions (constructor, destructor, and common methods, as
     * applicable), then builds the state handler for each state.
     * </p>
     * @param w  FileWriter to target code file.
     */
    protected void composeCode (Writer w) {
        assert tModel != null :
            this.getClass().getSimpleName() + ".writeStateMachine() did NOT properly assign tModel!";

        // Set up some "global" context variables for later templates
        StateMachine sm = tModel.getStateMachine();
        tContext.put("sm", sm);
        String smName = evalExpr("#mapToTypeName($sm,\"\")", "composeCode", tMapper.mapToTypeName(sm));
        tContext.put("smName", smName);
        tContext.put("smGuiName", smName.toLowerCase());
        //- determine whether to enable BAIL_SIG/CompletionEvt/reinit/final
        tMachineTerminable = tModel.isMachineTerminable(sm);
        tContext.put("SM_TERMINABLE", tMachineTerminable);
        //- determine whether to enable BAIL_EVENT
        tContext.put("SM_TERMINATOR", tModel.isMachineTerminator(sm));
        //- determine whether explicit final state exists
        tContext.put("SM_HAS_FINAL_STATE", !tModel.getFinalStates(sm).isEmpty());

        // Write file header and imports/includes
        markCodeUnitBegin();
        writeCodePart(w, QP_INCLUDES);
        markCodeUnitEnd(w);

        // Iterate through and handle top-level StateMachine and the orthogonal regions
        //- DO NOT sort, use order as returned
        for (Namespace machineOrRegion : tModel.getAllClassLevelElements()) {
            //- first stamp out the class code
            if (machineOrRegion instanceof StateMachine) {
                tIsHsm = false;
                tContext.put("isQHsm", tIsHsm);  // flag affects Active object access 
                tContext.put("typeName", evalExpr("#mapToTypeName($sm,\"\")", "composeCode", tMapper.mapToTypeName(sm)));
                makeActiveClass(w, (StateMachine) machineOrRegion);
            } else if (machineOrRegion instanceof Region) {
                Region region = (Region) machineOrRegion;
                tIsHsm = true;
                tContext.put("isQHsm", tIsHsm);  // flag affects Active object access 
                tContext.put("typeName", tMapper.mapToQualifiedName(region));
                makeHsmClass(w, region);
            } else {  // report error and continue
                if (machineOrRegion == null) {
                    Util.error("Error! Class code generation: encountered NULL machine/region!");
                } else {
                    Util.error("Error! Class code generation: encountered "
                            + machineOrRegion.getQualifiedName()
                            + " : " + machineOrRegion.getClass().getName() + "!");
                }
                continue;
            }

            //- now iterate through states in the scope of the class
            for (State state : tModel.getStates(machineOrRegion, false)) {
                makeState(w, state);
            }

            tContext.remove("isQHsm");
            tContext.remove("typeName");
            tContext.remove("SM_TERMINABLE");
        }

        if (inlineMainTemplate() != null) {
            // Write the application main method
            writeCodePart(w, inlineMainTemplate());
        }

        // remove context vars
        tContext.remove("sm");
        tContext.remove("smName");
        tContext.remove("smGuiName");
    }

    /**
     * Composes the class definition for a StateMachine QActive class.
     * 
     * @param w        FileWriter to target code file.
     * @param machine  the {@link StateMachine} for which to define QActive class.
     */
    private void makeActiveClass (Writer w, StateMachine machine) {
        markCodeUnitBegin();

        tContext.put("machine", machine);
        writeCodePart(w, QP_CONSTRUCTOR_ACTIVE);
        tContext.remove("machine");

        makeCommonMethods(w, machine);

        markCodeUnitEnd(w);
    }

    /**
     * Composes the class definition for an Orthogonal Region QHsm class.
     * 
     * @param w       FileWriter to target code file.
     * @param region  the {@link Region} for which to define QHsm class.
     */
    private void makeHsmClass (Writer w, Region region) {
        markCodeUnitBegin();

        tContext.put("region", region);
        writeCodePart(w, QP_CONSTRUCTOR_HSM);
        tContext.remove("region");

        makeCommonMethods(w, region);

        markCodeUnitEnd(w);
    }

    /**
     * Composes the common class member methods for the QActive/QHsm classes,
     * including the "initial"/"final" transitions and the guard and action
     * functions.
     * 
     * @param w   FileWriter to target code file.
     * @param ns  {@link StateMachine} or {@link Region} for which to define class members.
     */
    private void makeCommonMethods (Writer w, Namespace ns) {
        // Submachine entry/exitPoint utility methods, if any
        if (tModel.hasMachineEntryPoint()) {
            writeCodePart(w, QP_SUBMACHINE_METHODS);
        }

        // Initial (top) state
        tContext.put("typeObj", ns);
        writeCodePart(w, QP_METHOD_INITIAL);
        if (tModel.hasMachineEntryPoint()) {
            // stamp out an initial entry per entry point
            boolean firstBlock = true;
            for (Pseudostate pseudo : tModel.getConnectionPoints(ns, false)) {
                if (pseudo.getKind() == PseudostateKind.entryPoint) {
                    // use the appropriate 'if' keyword
                    if (firstBlock) {
                        tContext.put("if", tMapper.getGuardKeyword());
                        firstBlock = false;  // "else if" blocks afterward
                    } else {
                        tContext.put("if", tMapper.getElseGuardKeyword());
                    }
                    tContext.put("pseudo", pseudo);
                    writeCodePart(w, QP_SUBMACHINE_ENTRY);
                    tContext.remove("pseudo");
                    tContext.remove("if");

                    // process the entry transition
                    Transition entryTransition = tModel.getInitialTransition(pseudo);
                    if (entryTransition != null) {
                        tContext.put("isInitTran", true);
                        tMapper.incIndent();
                        makeStateTransition(w, pseudo, entryTransition);
                        tMapper.decIndent();
                        tContext.remove("isInitTran");
                        // entry transition should NOT have guards
                    } else {  // empty entry transition, add code for "handled"
                        invokeVmacro(w, VMACRO_FOOTER_HANDLED, "makeCommonMethods", EMPTY_PARAMS);
                    }
                }
            }

            // add "else" and indent for default initial transition
            invokeVmacro(w, VMACRO_ELSE, "makeCommonMethods", EMPTY_PARAMS);
            tMapper.incIndent();
            clearCodeWritten();  // so we know if there's any default initial code
        }
        // finally the default initial transition
        makeInitialStateTransition(w, ns);
        if (tModel.hasMachineEntryPoint()) {  // wrap-up and decrease indent
            if (!wasCodeWritten()) {  // return "handled" status, as applicable
                invokeVmacro(w, VMACRO_FOOTER_HANDLED, "makeCommonMethods", EMPTY_PARAMS);
            }
            tMapper.decIndent();
            invokeVmacro(w, VMACRO_FOOTER_ENDGUARDEDCASE, "makeCommonMethods", EMPTY_PARAMS);
        }
        invokeVmacro(w, VMACRO_FOOTER_INITIAL, "makeCommonMethods", EMPTY_PARAMS);
        tContext.remove("typeObj");

        if (tMachineTerminable || tIsHsm) {
            // Final state
            writeCodePart(w, QP_METHOD_FINAL);
        }

        // Additional, target language-specific, common methods
        makeMoreCommonMethods(w, ns);
    }

    /**
     * Builds a State Handler method, taking care of the INIT, ENTRY, EXIT,
     * and Signal- and TimeEvent transitions, as well as dispatches to
     * orthogonal regions and submachines.
     * 
     * @param w      FileWriter to the code file.
     * @param state  the {@link State} for which to define a state handler
     */
    private void makeState (Writer w, State state) {
        markCodeUnitBegin();

        tContext.put("state", state);
        tContext.put("stateName", tMapper.mapToFunctionName(state));

        // Determine if we need to declare a newEv var before the switch cases.
        // Desired-event query builds set of trigger events applicable to
        //  sub-orthogonal regions and submachines.
        // This is done first to figure out if any var declaration is needed!
        tModel.clearDesiredEvents(QueryPolicy.NO_NULL_EV);
        IDesiredEvent desiredEventQuery = tModel.queryDesiredEvents(state, QueryPolicy.NO_NULL_EV);
        desiredEventQuery.sort();
        tContext.put("query", desiredEventQuery);
        Util.debug("Got desired events for state: " + state.getQualifiedName());
        Set<String> processedSignals = Util.newSet();
        boolean shouldDeclareEv = false;
        for (EventTransitionPair pair : desiredEventQuery.getEventTransitions()) {
            Event ev = pair.getEvent();
            String[] evPrefix = desiredEventQuery.getSubstatePrefixOfEvent(pair);
            String evName = tMapper.mapEventToName(ev, evPrefix);

            // skip if prefixed-signal is already processed
            if (processedSignals.contains(evName)) continue;
            processedSignals.add(evName);

            if (state.isSubmachineState() && ev instanceof TimeEvent) {
                shouldDeclareEv = true;
                break;  // done searching!
            }
        }
        //- also need to scan for fired signals
        if (tModel.getActionEventSignals(state).size() > 0) {
            shouldDeclareEv = true;
        }
        tContext.put("shouldDeclareEv", shouldDeclareEv);
        processedSignals.clear();  // make sure to clear this set!

        // Next, determine if any state/substate with exit point(s), since for
        //   a substate, we need to declare an exitPoint variable first!
        Collection<Transition> emptyTrans = tModel.getEmptyTransitions(state);
        boolean hasCompletionTransition = false;
        if (!state.isSimple()
                && emptyTrans.size() > 0
                && tModel.getStatesExpectingCompletionEvent(state).size() > 0) {
            // need to code transition for completion event
            hasCompletionTransition = true;
            // for C/C++ sake, prepare signal name, flag if submachine state
            if (state.isSubmachineState()) {
                tContext.put("sigName", tMapper.mapToSignalEnum(state.getSubmachine()));
                tContext.put("checkSubmInstance", true);
            } else {
                tContext.put("sigName", tMapper.mapToSignalEnum(state));
                tContext.put("checkSubmInstance", false);
            }
        }

        // Begin code of a State-handler
        writeCodePart(w, QP_STATE_BEGIN);

        // State-handler Entry processing
        writeCodePart(w, QP_STATE_ENTRY_BEGIN);
        tMapper.incIndent();
        makeAction(w, state.getEntry());
        tMapper.decIndent();
        writeCodePart(w, QP_STATE_ENTRY_END);

        // State-handler Exit processing
        writeCodePart(w, QP_STATE_EXIT_BEGIN);
        tMapper.incIndent();
        makeAction(w, state.getExit());
        tMapper.decIndent();
        writeCodePart(w, QP_STATE_EXIT_END);

        // State-handler initial transition, if any
        //   Only applicable if state is composite and contains an initial state
        //   Not applicable if state is orthogonal or a submachine state!
        if (state.isComposite()
                && !state.isOrthogonal() && !state.isSubmachineState()
                && tModel.getInitialState(state) != null) {
            // composite-state initial transition
            invokeVmacro(w, VMACRO_INIT_TRANS, "makeState", EMPTY_PARAMS);
            tMapper.incIndent();  // indent one level
            makeInitialStateTransition(w, state);
            // end case branch; initial state should NOT have guards
            invokeVmacro(w, VMACRO_FOOTER_ENDCASE, "makeState", EMPTY_PARAMS);
            tMapper.decIndent();
        }

        // State Bail and completion-event processing; already begun above
        //   with shouldDeclareExitPoint, but here we continue it...
        // If FinalState, see if parent handles it, or should self-consume?
        //   Self-consumption prevents dropped event!
        String consumeCompletionSig = null;
        if (state instanceof FinalState) {
            State containingState = state.getContainer().getState();
            if (containingState == null) {
                // FinalState is in a machine, check if this is a submachine?
                if (!tModel.isMachineTerminable(tModel.getStateMachine())) {
                    // no! self-consume completion ev
                    consumeCompletionSig = tMapper.mapToSignalEnum(tModel.getStateMachine());
                }
            } else {
                // FinalState is in a composite state, any empty transition?
                if (tModel.getEmptyTransitions(containingState).size() == 0) {
                    // no! parent composite state has no empty transition
                    consumeCompletionSig = tMapper.mapToSignalEnum(containingState);
                }
            }
        }
        tContext.put("hasCompletionTransition", hasCompletionTransition);
        tContext.put("consumeCompletionSig", consumeCompletionSig);
        writeCodePart(w, QP_STATE_BAIL);
        tContext.remove("consumeCompletionSig");
        tContext.remove("hasCompletionTransition");
        if (hasCompletionTransition) {  // make transition for completion event
            tContext.remove("sigName");
            tMapper.incIndent();
            if (state.isSubmachineState()) {
                // see if we need to transition from exit points
                Set<ConnectionPointReference> exitSet = Util.newSortedSet();
                for (ConnectionPointReference cpr : state.getConnection()) {
                    if (cpr.getExit().size() > 0) {
                        exitSet.add(cpr);
                    }
                }
                if (exitSet.size() > 0) {  // yes we have exit point(s)!
                    // we need to set up a switch statement and iterate through
                    // the ConnectionPointReferences and stamp out a transition
                    Iterator<ConnectionPointReference> cprefIter = exitSet.iterator();
                    ConnectionPointReference cpref = cprefIter.next();
                    String[] params = { "exitPoint" };
                    //- switch statement begins
                    tContext.put("exitPoint", cpref.getExit().iterator().next());
                    invokeVmacro(w, VMACRO_EXIT_CONN_BEGIN, "makeState", params);
                    //- first state transition
                    tMapper.incIndent();
                    makeStateTransition(w, cpref, cpref.getOutgoing().iterator().next());
                    tMapper.decIndent();
                    //- process all subsequent exit transition cases
                    while (cprefIter.hasNext()) {
                        cpref = cprefIter.next();
                        tContext.put("exitPoint", cpref.getExit().iterator().next());
                        invokeVmacro(w, VMACRO_EXIT_CONN_CASE, "makeState", params);
                        tMapper.incIndent();
                        makeStateTransition(w, cpref, cpref.getOutgoing().iterator().next());
                        tMapper.decIndent();
                    }
                    tContext.remove("exitPoint");
                    invokeVmacro(w, VMACRO_EXIT_CONN_END, "makeState", EMPTY_PARAMS);
                } else {  // no, just a single transition out of substate
                    makeStateTransition(w, state, emptyTrans.iterator().next());
                }
                // end case branch for empty transition; substate has if-block
                invokeVmacro(w, VMACRO_FOOTER_ENDGUARDEDCASE, "makeState", EMPTY_PARAMS);
            } else {  // end case branch for empty transition with "handled"
                makeStateTransition(w, state, emptyTrans.iterator().next());
                invokeVmacro(w, VMACRO_FOOTER_ENDCASE, "makeState", EMPTY_PARAMS);
            }
            tMapper.decIndent();
            tContext.remove("checkSubmInstance");  // remove this last!
        }

        // State transition event processing (both SignalEvent and TimeEvent)
        for (EventTransitionPair pair : desiredEventQuery.getEventTransitions()) {
            Event ev = pair.getEvent();
            Transition transition = pair.getTransition();
            String[] evPrefix = desiredEventQuery.getSubstatePrefixOfEvent(pair);
            String evName = tMapper.mapEventToName(ev, evPrefix);

            // skip if prefixed-signal is already processed
            if (processedSignals.contains(evName)) continue;
            processedSignals.add(evName);

            tContext.put("eventTransPair", pair);
            tContext.put("eventName", evName);
            tContext.put("traceName", tMapper.mapEventToLiteral(ev, evPrefix));
            if (Util.isDebugLevel()) {
                Util.debug("  - " + evName + " => state is '"
                        + state.getQualifiedName() + "', transition source: "
                        + transition.getSource().getQualifiedName());
            }

            boolean needFooter = false;
            if (state.isOrthogonal()) {
                // Dispatch Signal- and Time-events to child orthogonal regions
                writeCodePart(w, QP_STATE_ORTHOGONAL);
                needFooter = true;
            } else if (state.isSubmachineState()) {
                tContext.put("isTimerEvent", ev instanceof TimeEvent);
                // Dispatch events to submachine
                writeCodePart(w, QP_STATE_SUBMACHINE);
                tContext.remove("isTimerEvent");
                needFooter = true;
            } else {  // simple or non-orth, non-submachine composite
                if (state.equals(transition.getSource())) {
                    // otherwise, don't stamp extraneous cases
                    writeCodePart(w, QP_STATE_EVENTTRANS);
                }
            }

            // Stamp out code for state transition only if applicable to state
            tMapper.incIndent();  // indent one level
            if (state.equals(transition.getSource())) {
                makeStateTransition(w, state, transition);
                // end case branch, check if guarded
                if (tModel.hasGuard(transition)) {
                    invokeVmacro(w, VMACRO_FOOTER_ENDGUARDEDCASE, "makeState", EMPTY_PARAMS);
                } else {
                    invokeVmacro(w, VMACRO_FOOTER_ENDCASE, "makeState", EMPTY_PARAMS);
                }
            } else {
                if (needFooter) {
                    // solely orthogonal/submachine dispatch, so need footer
                    if (state.isSubmachineState()) {
                        // submachine dispatch requires checking 'handled' flag
                        invokeVmacro(w, VMACRO_FOOTER_ENDCASESUBM, "makeState", EMPTY_PARAMS);
                    } else {  // straight "handled" is OK for orthogonal regions
                        invokeVmacro(w, VMACRO_FOOTER_HANDLED, "makeState", EMPTY_PARAMS);
                    }
                }
            }
            tMapper.decIndent();
        }

        // End definition of State-handler
        writeCodePart(w, QP_STATE_END);

        markCodeUnitEnd(w);

        tContext.remove("shouldDeclareEv");
        tContext.remove("eventName");
        tContext.remove("eventTransPair");
        tContext.remove("query");
        tContext.remove("stateName");
        tContext.remove("state");
    }

    /**
     * Builds the initial state transition code looking within the given
     * {@link Namespace}.
     * 
     * @param w   FileWriter to the code file.
     * @param ns  {@link State}, {@link StateMachine}, or {@link Region}
     *            within which to find initial transition.
     */
    private void makeInitialStateTransition (Writer w, Namespace ns) {
        Pseudostate initialState = tModel.getInitialState(ns);
        if (initialState == null) {
            Util.error("Error! Initial state expected but NOT found within " + ns.getQualifiedName());
            // return Q_HANDLED
            invokeVmacro(w, VMACRO_FOOTER_HANDLED, "makeInitialStateTransition", EMPTY_PARAMS);
        } else {
            Transition initialTransition = tModel.getInitialTransition(initialState);
            if (initialTransition != null) {
                tContext.put("isInitTran", true);
                makeStateTransition(w, initialState, initialTransition);
                tContext.remove("isInitTran");
            } else {  // return Q_HANDLED
                invokeVmacro(w, VMACRO_FOOTER_HANDLED, "makeInitialStateTransition", EMPTY_PARAMS);
            }
        }
    }

    // stores the stack of guard flag used
    private Stack<Boolean> guardFlagStack = new Stack<Boolean>();

    /**
     * Builds the code for the state transition, taking into account transition
     * guard, transition actions, and then the target node type
     * ({@link Pseudostate} or {@link State}) of the transition edge.
     * <p>
     * Note that recursion occurs when a <i>junction</i> or <i>choice</i>
     * target node is encountered, in order to handle the next segment of
     * transition, etc.
     * </p>
     * @param w           FileWriter to the code file.
     * @param source      Source {@link Vertex} to make transition.
     * @param transition  {@link Transition} object for the state transition.
     */
    private void makeStateTransition (Writer w, Vertex source, Transition transition) {
        if (guardFlagStack.empty()) {
            guardFlagStack.push(false);
        }
        tMapper.setAlreadyGuarded(guardFlagStack.peek());  // 'if' or 'else if'

        if (tModel.hasGuard(transition)) {
            // grab the guard expression, and make sure it's a function
            String expr = transition.getGuard().getSpecification().stringValue();
            if (FunctionCall.isFunctionCall(expr)
                    || Util.isLiteral(expr)) {  // provide expr as is
                tContext.put("exprStr", expr);
            } else {  // an identifier, so likely a function?!
                // TODO revisit whether treating identifier as a function missing () is right
                tContext.put("exprStr", expr + "()");
            }
            tContext.put("if", tMapper.guardKeyword());
            writeCodePart(w, QP_TRANS_GUARD_BEGIN);
            tMapper.incIndent();
            tContext.remove("exprStr");
        }

        // Do the transition effect action-list
        makeAction(w, transition.getEffect());

        // Handle target state(s) by type, but only if transition is not "internal"
        //  (Pseudostate not supported:  join, fork)
        if (transition.isInternal()) {
            // No target state to transition to, so just return "handled"
            invokeVmacro(w, VMACRO_FOOTER_HANDLED, "makeStateTransition", EMPTY_PARAMS);
        } else {
            Vertex tgtState = tModel.getTarget(source, transition);
            tContext.put("target", tgtState);
            if (tgtState instanceof Pseudostate) {
                // Pseudostate:  deep history, junction, choice,
                //    entryPoint, exitPoint, terminate
                Pseudostate tgtPseudo = (Pseudostate) tgtState;
                switch (tgtPseudo.getKind()) {
                case deepHistory:
                    writeCodePart(w, QP_TRANS_DEEPHISTORY);
                    break;

                case choice:
                    writeCodePart(w, QP_TRANS_CHOICE);
                    // INTENTIONAL FALL-THRU

                case junction:
                    // check out-transition count to handle degenrate case of 1
                    List<Transition> juncTransList = tModel.getJunctionTransitions(tgtPseudo);
                    if (juncTransList.size() == 1) {  // junction merge, no ifs
                        Transition juncTrans = juncTransList.iterator().next();
                        makeStateTransition(w, tgtState, juncTrans);
                    } else {
                        guardFlagStack.push(false);  // start inner branches with 'if'
                        Iterator<Transition> junctionIter = juncTransList.iterator();
                        while (junctionIter.hasNext()) {
                            Transition juncTrans = junctionIter.next();
                            if (junctionIter.hasNext()) {  // not yet last one
                                // take the junction transition!
                                makeStateTransition(w, tgtState, juncTrans);
                                // change guard flag to alreadyGuarded!
                                guardFlagStack.pop();
                                guardFlagStack.push(true);
                            } else {  // last transition out, the "else"!
                                invokeVmacro(w, VMACRO_ELSE, "makeStateTransition", EMPTY_PARAMS);
                                if (juncTrans != null) {  // valid no-guard transition
                                    tMapper.incIndent();
                                    // take the else junction transition!
                                    makeStateTransition(w, tgtState, juncTrans);
                                    tMapper.decIndent();
                                } else {  // uh-oh, unknown!
                                    invokeVmacro(w, VMACRO_CHOICE_UNKNOWN, "makeStateTransition", EMPTY_PARAMS);
                                    tMapper.incIndent();
                                    invokeVmacro(w, VMACRO_FOOTER_HANDLED, "makeStateTransition", EMPTY_PARAMS);
                                    tMapper.decIndent();
                                }
                                invokeVmacro(w, VMACRO_FOOTER_GUARD, "makeStateTransition", EMPTY_PARAMS);
                            }
                        }
                        guardFlagStack.pop();  // restore last guard state in stack
                    }
                    break;

                case entryPoint:
                    // StateMachine entry points shouldn't be encountered here
                    // transition to its internal entry target
                    Transition entryTran = tModel.getInitialTransition(tgtPseudo);
                    makeStateTransition(w, tgtPseudo, entryTran);
                    break;

                case exitPoint:
                    if (tgtPseudo.getStatemachine() != null) {
                        // StateMachine exitPoint, emit completion event
                        tContext.put("exitPoint", tgtPseudo);
                        String[] params = { "exitPoint" };
                        invokeVmacro(w, VMACRO_COMPLETION_MACHINE, "makeStateTransition", params);
                        tContext.remove("exitPoint");
                        // then go to hidden final state!
                        invokeVmacro(w, VMACRO_HIDDEN_FINAL, "makeStateTransition", EMPTY_PARAMS);
                    } else {  // composite state exitPoint
                        // transition to its external exit target
                        Transition exitTran = tModel.getInitialTransition(tgtPseudo);
                        makeStateTransition(w, tgtPseudo, exitTran);
                    }
                    break;

                case terminate:
                    invokeVmacro(w, VMACRO_PSEUDO_TERMINATE, "makeStateTransition", EMPTY_PARAMS);
                    break;

                default:
                    String[] params = { "target" };
                    invokeVmacro(w, VMACRO_TARGET_UNKNOWN, "makeStateTransition", params);
                    break;
                }
            } else if (tgtState instanceof ConnectionPointReference) {
                // find the target entryPoint Pseudostate, only consider 1!
                ConnectionPointReference cpr = (ConnectionPointReference) tgtState;
                if (cpr.getState().isSubmachineState()
                        && cpr.getEntry().size() != 0) {
                    Pseudostate cpfTarget = cpr.getEntry().get(0);
                    if (cpfTarget != null) {
                        tContext.put("substate", cpr.getState());
                        // override target state with cpfTarget
                        tContext.put("target", cpfTarget);
                        writeCodePart(w, QP_TRANS_CONNPTREF);
                        tContext.remove("substate");
                    } else {
                        // error!
                    }
                } else {
                    Util.error("ERR! Either state '" + tgtState.getQualifiedName()
                            + "' is NOT a submachine state, OR NO entry point has been specified for connection point reference '"
                            + cpr.getName() + "'!");
                    Util.error("  Please fix in model and regenerate!");
                }
            } else if (tgtState instanceof FinalState) {
                // emit a completion event before entering the final state
                State containingState = tgtState.getContainer().getState();
                if (containingState == null) {
                    // FinalState is in a machine
                    invokeVmacro(w, VMACRO_COMPLETION_MACHINE, "makeStateTransition", EMPTY_PARAMS);
                } else {
                    // FinalState is in a composite state
                    tContext.put("containingState", containingState);
                    String[] params = { "containingState" };
                    invokeVmacro(w, VMACRO_COMPLETION_STATE, "makeStateTransition", params);
                }
                writeCodePart(w, QP_TRANS_STATE);
            } else if (tgtState instanceof State) {  // must come AFTER Final
                writeCodePart(w, QP_TRANS_STATE);
            } else {  // unsupported/unknown target state type
                String[] params = { "target" };
                invokeVmacro(w, VMACRO_TARGET_UNKNOWN, "makeStateTransition", params);
            }
            tContext.remove("target");
        }

        if (tModel.hasGuard(transition)) {
            tMapper.decIndent();
        }
        tContext.remove("if");
    }

    /**
     * Builds code for a function or event action, depending on whether the
     * given {@link Behavior} is a function call or a SignalEvent to publish.
     * </p> 
     * We also support handling an action that is entirely quoted in single or
     * double quotes.  This is particularly useful for target languages of a
     * dynamic nature, such as python.
     * </p> 
     * @param w         FileWriter to the code file.
     * @param activity  UML {@link Behavior} object from which to compose action invocation
     */
    private void makeAction (Writer w, Behavior activity) {
        if (activity != null) {
            for (String actionStr : activity.actionList()) {
                if (actionStr != null && actionStr.length() > 0) {
                    if (Util.isQuotedString(actionStr)) {
                        // stamp out literal code
                        String[] params = { "codeStr"};
                        String newStr = actionStr.trim();
                        tContext.put("codeStr", newStr.substring(1, newStr.length()-1));
                        invokeVmacro(w, VMACRO_LITERAL_CODE, "makeAction", params);
                        tContext.remove("codeStr");
                    } else if (FunctionCall.isFunctionCall(actionStr)) {
                        // treat action as function call
                        tContext.put("funcCall", tModel.getCallParts(actionStr));
                        writeCodePart(w, QP_ACTION_FUNCTION);
                        tContext.remove("funcCall");
                    } else {  // Event action, so publish as signal event
                    	// find signal object from string identifier
                    	Signal sig = tModel.findSignalByName(actionStr,
                    			UMLModelGroup.element2Model(tModel.getStateMachine()).getModelScape().getCachedSignalsByName());
                        tContext.put("actionSig", sig);
                        writeCodePart(w, QP_ACTION_EVENT);
                        tContext.remove("actionSig");
                    }
                }
            }
        }
    }

}
