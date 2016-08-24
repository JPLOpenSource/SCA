/**
 * Created Sep 9, 2009.
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
package gov.nasa.jpl.statechart.input.validator;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.input.identifiers.XMIIdentifiers;
import gov.nasa.jpl.statechart.model.ModelScape;
import gov.nasa.jpl.statechart.model.PrefixOrderedWalker;
import gov.nasa.jpl.statechart.model.UMLModelGroup;
import gov.nasa.jpl.statechart.model.visitor.AbstractVisitor;
import gov.nasa.jpl.statechart.template.FunctionCall;
import gov.nasa.jpl.statechart.template.TargetLanguageMapper;
import gov.nasa.jpl.statechart.uml.Behavior;
import gov.nasa.jpl.statechart.uml.ConnectionPointReference;
import gov.nasa.jpl.statechart.uml.Event;
import gov.nasa.jpl.statechart.uml.Model;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.PseudostateKind;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.Signal;
import gov.nasa.jpl.statechart.uml.SignalEvent;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.TimeEvent;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.Trigger;
import gov.nasa.jpl.statechart.uml.UMLElement;
import gov.nasa.jpl.statechart.uml.UMLModel;
import gov.nasa.jpl.statechart.uml.UMLStateMachine;
import gov.nasa.jpl.statechart.uml.ValueSpecification;
import gov.nasa.jpl.statechart.uml.Vertex;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;


/**
 * This class supports the validation of a UML Model.  Only critical validation
 * errors cause autocoding to terminate; all other validation problems report
 * a warning message to the user.
 * <p>
 * The approach of validation is to traverse the Model tree depth-first and
 * validate each vertex.  A validator method is easily declared by adding it as
 * a public member declaring the @{@link Validate} annotation (optionally
 * indicating an order), and specifying one parameter of the type of vertex it
 * is validating.  Unordered {@link Validate} methods are done after the ordered
 * ones, in whatever order was returned by the Java reflect infrastructure.
 * Methods not specifying a parameter are invoked for every types of Vertex.
 * Methods specifying any other kinds of parameters will be ignored (with warning).
 * </p>
 * Algorithm:<ol>
 * <li> When this class is first instantiated, it collects all the validator
 * methods and hashes them into a hash of list by the type of Vertex they
 * validate, ordered in each list by {@link Validate#order()}.
 * <li> Upon visiting a vertex in the tree, all {@link Validate} methods
 * applicable to that vertex are invoked in order, error message are reported
 * in the process, and any return value reported.
 * <li> Fatal errors do not terminate the validation process immediately, but
 * are flagged to cause the {@link #validate(Model)} method to return the
 * validation status to the caller at the very end of the validation process.
 * This allows all problems in the model to be reported in one go.
 * </ol>
 * TODO Errors to check on: <ul>
 * <li> Do we need to ensure that destination state of initial transition is
 * a child of the composite parent state?
 * </ul>
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class UMLValidator extends AbstractModelValidator {
    private static final String NL = System.getProperty("line.separator");

    /**
     * Cache model as class member of the concrete type both:
     * (a) for convenient access to non-UML-standard functions, and
     * (b) to avoid having to pass it around.
     * */
    private UMLModel model = null;
    /* no need to store a stack, as we don't traverse below submachines */
    private UMLStateMachine curMachine = null;

    /** Hash lists of validator methods by Vertex type. */
    private Map<Class<? extends NamedElement>, List<Method>> binByVertexType = null;
    /** Indicator of whether vertex has been visited; used only partially. */
    private Set<NamedElement> visitedSet = null;

    /**
     * Main constructor, bins the validator methods by Vertex (parameter) type.
     */
    public UMLValidator () {
        super();

        visitedSet = Util.newSet();

        // instantiate the bins for known vertex types
        binByVertexType = Util.newMap();
        Class<?>[] knownVertexTypes = {
                Model.class,
                StateMachine.class,
                Region.class,
                State.class,
                Pseudostate.class,
                Behavior.class
        };
        for (Class<?> vType : knownVertexTypes) {
            binByVertexType.put(vType.asSubclass(NamedElement.class), Util.<Method>newList());
        }

        // Iterate through the list of methods, already ordered properly.
        // The bin lists are guaranteed to be in proper order by construction.
        for (Iterator<Method> iter = methodIterator(); iter.hasNext(); ) {
            Method m = iter.next();
            Class<?>[] paramTypes = m.getParameterTypes();

            // check param length
            if (paramTypes.length > 1) {
                Util.error("V-warning: validator method '" + m.getName()
                        + "' has more than one parameter, ignoring all but the first!");
            }
            // check for zero param
            if (paramTypes.length == 0) {
                // add method to every list
                for (List<Method> mList : binByVertexType.values()) {
                    mList.add(m);
                }
            } else {
                // check expected param type
                if (!NamedElement.class.isAssignableFrom(paramTypes[0])) {
                    Util.error("V-error: first parameter of validator method '"
                            + m.getName() + "' NOT a UML NamedElement, skipping!");
                    continue;
                }
    
                // otherwise, we're OK to narrow the param Class
                Class<? extends NamedElement> paramClass = paramTypes[0].asSubclass(NamedElement.class);
                // now, retrieve list by vertex type (indicated by the param class)
                List<Method> mList = binByVertexType.get(paramClass);
                if (mList == null) {  // uh oh, unknown Vertex Type?!
                    Util.error("V-error: unknown Vertex type '"
                            + paramClass.getName() + "' specified as parameter of method '"
                            + m.getName() + "'!");
                    continue;
                }
    
                //- all good to add method to list...
                mList.add(m);
            }
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.validator.AbstractModelValidator#validate(gov.nasa.jpl.statechart.uml.Model)
     */
    @Override
    public Status validate (Model m) {
        model = (UMLModel) m;  // cache model for convenient access
        visitedSet.clear();

        // first validate the root Model vertex
        validateVertexOfType(Model.class, m);

        // now walk the Model tree and validate by vertex
        PrefixOrderedWalker.traverse(m, new AbstractVisitor<Void>() {
            private static final long serialVersionUID = 5620358804361064466L;
            @Override
            public void moveDown (NamedElement from, NamedElement to) {
                super.moveDown(from, to);
                if (from instanceof StateMachine) {  // store current machine
                    curMachine = (UMLStateMachine) from;
                    stateNameSet = Util.newMap();
                }
            }
            @Override
            public void moveUp (NamedElement from, NamedElement to) {
                super.moveUp(from, to);
                if (to instanceof StateMachine) {  // unset current machine
                    curMachine = null;
                    stateNameSet = null;
                }
            }
            @Override
            public void visit (StateMachine stateMachine) {
                Util.info(">v> Checking State Machine '" + stateMachine.getQualifiedName() + "'");
                validateVertexOfType(StateMachine.class, stateMachine);
            }
            @Override
            public void visit (Region region) {
                validateVertexOfType(Region.class, region);
            }
            @Override
            public void visit (State state) {
                validateVertexOfType(State.class, state);

                // check entry/exit and DO behaviors
                if (state.getEntry() != null) {
                    validateVertexOfType(Behavior.class, state.getEntry());
                }
                if (state.getDo() != null) {
                    validateVertexOfType(Behavior.class, state.getDo());
                }
                if (state.getExit() != null) {
                    validateVertexOfType(Behavior.class, state.getExit());
                }

                for (Transition t : state.getOutgoing()) {
                    if (t.getEffect() != null) {
                        validateVertexOfType(Behavior.class, t.getEffect());
                    }

                    // populate transition caches
                    t.getSignalEvents();
                    t.getTimeEvents();
                }
            }
            @Override
            public void visit (Pseudostate pseudostate) {
                validateVertexOfType(Pseudostate.class, pseudostate);
            }
        });

        visitedSet.clear();
//        model = null;  // unset model for good measures

        // Infinite recursion checker, descend into submachines!
        PrefixOrderedWalker.traverse(m, new AbstractVisitor<Void>(true) {
            private static final long serialVersionUID = 5620358804361064466L;
            /**
             * Intercept to detect infinite recursion!
             */
            @Override
            public boolean isVisiting (NamedElement ne) {
                boolean visiting = super.isVisiting(ne);
                if (visiting) {  // uh oh, detected potential infinite recursion!
                    setFatalError();
                    Util.error("Fatal: Infinite recursion detected trying to revisit '"
                            + ne.getQualifiedName()
                            + "'! Please check model for recursive submachines.");
                }
                return visiting;
            }
        });

        Status validStat = Status.OK;
        if (fatalError()) {
            validStat = Status.FATAL;
        } else if (validationIssue()) {
            validStat = Status.ERROR;
        }
        return validStat;
    }

    private void markVisited (NamedElement ne) {
        visitedSet.add(ne);
    }

    private boolean visited (NamedElement ne) {
        return visitedSet.contains(ne);
    }

    /**
     * Invokes the applicable list of validator methods based on the type of the
     * vertex.
     * @param vType   vertex type, must be specified since the vertex object may be a subclass.
     * @param vertex  A UML {@link NamedElement} vertex.
     */
    private void validateVertexOfType (Class<? extends NamedElement> vType, NamedElement vertex) {
        for (Method m : binByVertexType.get(vType)) {
            if (!invokeValidatorMethod(m, vertex)) {
                setValidationIssue();
            }
        }
    }


    private Map<String,StateMachine> smNameSet = Util.newMap();

    /**
     * Checks for State Machines with out names, report if found; fatal!
     * @param sm  UML {@link StateMachine} to check
     */
    @Validate
    public void checkMachineHasName (StateMachine sm) {
        if (sm.getName() == null || sm.getName().length() == 0) {
            setFatalError();
            Util.error("Fatal: State Machine must have a name! ID: " + sm.id());
        }
    }

    /**
     * Checks for State Machines with duplicate names, report if one is found;
     * fatal or not, depending on whether command-line option to allow duplicate
     * name is set.
     * 
     * @param sm  UML {@link StateMachine} to check
     */
    @Validate
    public void checkDuplicateMachineName (StateMachine sm) {
        String name = sm.getName();
        if (name != null) {
            if (smNameSet.containsKey(name)) {  // duplicate found!
                StateMachine otherSM = smNameSet.get(name);
                StringBuilder sb = new StringBuilder("Duplicate name '")
                        .append(name).append("' found for StateMachines '")
                        .append(otherSM.getQualifiedName()).append("' and '")
                        .append(sm.getQualifiedName()).append("'!");
                if (Autocoder.allowDupSMName()) {
                    setValidationIssue();
                    Util.warn(sb.insert(0, "Warning: ").toString());
                } else {
                    setFatalError();
                    Util.error(sb.insert(0, "Fatal: ").toString());
                }
            } else {  // we're OK
                smNameSet.put(name, sm);
            }
        }
    }

    /**
     * Checks whether state machine(s) in the model has initial state; not fatal.
     * @param sm  UML StateMachine to check.
     */
    @Validate(order=1)
    public void checkMachineHasInitialState (StateMachine sm) {
        boolean smHasInitState = false;
        // iterate through StateMachine's immediate regions
        for (Region r : sm.getRegion()) {
            markVisited(r);
            if (hasInitialState(r)) {
                smHasInitState = true;
                break;
            }
        }
        if (! smHasInitState) {
            setValidationIssue();
            Util.error("Warning: No Initial State found in State machine "
                    + sm.getQualifiedName() + ", perhaps an error in the diagram?" );
        }
    }

    /**
     * Checks whether state machine has more than one region; not fatal.
     * @param sm  UML {@link StateMachine} to check.
     */
    @Validate
    public void checkMachineHasOneRegion (StateMachine sm) {
        if (sm.getRegion().size() > 1) {
            setValidationIssue();
            Util.error("Warning: StateMachine " + sm.getQualifiedName()
                    + " has more than one region, is this intended?");
        }
    }

    /**
     * Checks that all referenced submachines have been loaded; fatal.
     * @param state  UML {@link State} whose submachines to check, if applicable.
     */
    @Validate
    public void checkSubmachinesLoaded (State state) {
        if (state.isSubmachineState()) {
            if (state.getSubmachine() == null) {
                setFatalError();
                Util.error("Fatal: SubMachine for State '" + state.getQualifiedName() + "' NOT loaded!");
            }
        }
    }

    /**
     * Checks whether Region has an initial state; not fatal.
     * @param region  UML {@link Region} to check.
     */
    @Validate
    public void checkForInitialState (Region region) {
        if (visited(region)) return;  // don't dupicate check

        if (! hasInitialState(region)) {
            StringBuilder sb = new StringBuilder();
            // determine if any incoming transition, because then it's FATAL
            boolean hasExternalIncomingTrans = false;
            for (Transition t : region.getState().getIncoming()) {
                if (!t.isInternal()) {
                    hasExternalIncomingTrans = true;
                    break;
                }
            }
            if (hasExternalIncomingTrans) {
                setFatalError();
                sb.append("Fatal: Region '")
                    .append(region.getQualifiedName())
                    .append("' MUST have initial state if it has incoming transitions!");
            } else {
                setValidationIssue();
                sb.append("Warning: Region '")
                    .append(region.getQualifiedName())
                    .append("' found with no initial state, is this intended?");
            }
            Util.error(sb.toString());
        }
    }
    
    private boolean hasInitialState (Region region) {
        boolean hasInitState = false;
        for (Pseudostate pseudo : Util.filter(region.getSubvertex(), Pseudostate.class)) {
            if (pseudo.getKind() == PseudostateKind.initial) {
                hasInitState = true;
                break;
            }
        }
        return hasInitState;
    }

    /**
     * Checks that initial state has one and only one outgoing transition,
     * and that it has NO signal nor guard of any kind;
     * FATAL if error.
     * @param pseudo  UML {@link Pseudostate} to check.
     */
    @Validate
    public void checkInitialStateOutgoing (Pseudostate pseudo) {
        if (pseudo.getKind() == PseudostateKind.initial) {
            Collection<Transition> outTrans = pseudo.getOutgoing();
            if (outTrans.size() == 0) {
                setValidationIssue();
                StringBuilder sb = new StringBuilder("Warning: Initial pseudostate '");
                sb.append(pseudo.getQualifiedName());
                sb.append("' with no outgoing transition, is this intended? ");
                sb.append("ID: ").append(pseudo.id());
                Util.error(sb.toString());
            } else if (outTrans.size() > 1) {
                setFatalError();
                StringBuilder sb = new StringBuilder("Fatal: Initial pseudostate '");
                sb.append(pseudo.getQualifiedName());
                sb.append("' must NOT have more than one outgoing transition! ");
                sb.append("ID: ").append(pseudo.id());
                Util.error(sb.toString());
            } else {  // one transition out, check for signal and guard
                Transition t = outTrans.iterator().next();
                boolean hasFatalError = false;
                StringBuilder sb = new StringBuilder();
                if (t.getAllEvents().size() > 0) {
                    hasFatalError = true;
                    sb.append("a trigger");
                }
                if (t.getGuard() != null) {
                    hasFatalError = true;
                    if (sb.length() > 0) {
                        sb.insert(0, "either ");
                        sb.append(" or ");
                    }
                    sb.append("a guard");
                }
                if (hasFatalError) {
                    setFatalError();
                    sb.insert(0, "Fatal: Initial transition is NOT allowed to define ")
                        .append("!");
                    appendStateTransitionInfo(sb, pseudo, t, null);
                    Util.error(sb.toString());
                }
            }
        }
    }

    /**
     * Checks that Junction state outgoing transitions has guards on all but
     * ONE transition; not fatal.
     * @param pseudo  UML {@link Pseudostate} to check.
     */
    @Validate
    public void checkJunctionStateGuards (Pseudostate pseudo) {
        if (pseudo.getKind() == PseudostateKind.junction) {
            int countLackingGuards = pseudo.getOutgoing().size();
            for (Transition t : pseudo.getOutgoing()) {
                if (t.getGuard() != null) {
                    --countLackingGuards;
                }
            }
            if (countLackingGuards > 1) {
                setValidationIssue();
                StringBuilder sb = new StringBuilder("Warning: junction state '");
                if (pseudo.getState() != null) {
                    sb.append(pseudo.getState().getQualifiedName());
                } else {
                    sb.append(pseudo.getQualifiedName());
                }
                sb.append("' ").append("has more than ONE outgoing transitions without a guard; transition might not behave properly!");
                Util.error(sb.toString());
            }
        }
    }

    /**
     * Checks that:<ul>
     * <li> Entry/exitPoints have no more than ONE outgoing transition; fatal.
     * <li> Outgoing transition should have NO guard or trigger; fatal.
     * <li> Entry/exitPoints be defined on EITHER a State or a StateMachine,
     *      not "ghost"!! Fatal.
     * <li> Trigger exists in the inner transition from a State to an exitPoint;
     *      fatal.
     * </ul>
     * @param pseudo  UML {@link Pseudostate} to check.
     */
    @Validate
    public void checkEntryExitPoints (Pseudostate pseudo) {
        if (pseudo.getKind() == PseudostateKind.entryPoint
                || pseudo.getKind() == PseudostateKind.exitPoint) {

            if (pseudo.getKind() == PseudostateKind.entryPoint) {
                // exactly ONE outgoing transition, without trigger
                if (pseudo.getOutgoing().size() == 1) {
                    Transition trans = pseudo.getOutgoing().iterator().next();
                    StringBuilder partialSb = new StringBuilder();
                    if (trans.getGuard() != null) {  // uh oh, a guard!
                        partialSb.append("a guard");
                    }
                    if (trans.getAllEvents().size() > 0) {  // uh oh, trigger!
                        if (partialSb.length() > 0) {
                            partialSb.insert(0, "either ");
                            partialSb.append(" or ");
                        }
                        partialSb.append("a trigger");
                    }
                    if (partialSb.length() > 0) {  // we have a problem
                        setFatalError();
                        StringBuilder sb = prepPseudostateErrorMsg(new StringBuilder("Fatal"), pseudo);
                        sb.append("should NOT define ")
                            .append(partialSb)
                            .append(" on its outgoing transition!");
                        Util.error(sb.toString());
                    }
                } else {  // uh oh, any other than one outgoing is unacceptable
                    setValidationIssue();
                    StringBuilder sb = prepPseudostateErrorMsg(new StringBuilder("Warning"), pseudo);
                    sb.append("must have exactly ONE outgoing transition!");
                    Util.error(sb.toString());
                }
            } else {  // check exit point
                // NO outgoing if composite and at least ONE incoming transition
                if (pseudo.getStatemachine() != null && pseudo.getOutgoing().size() > 0) {
                    setFatalError();
                    StringBuilder sb = prepPseudostateErrorMsg(new StringBuilder("Fatal"), pseudo);
                    sb.append("in a submachine must have NO outgoing transition!");
                    Util.error(sb.toString());
                }
                if (pseudo.getIncoming().size() == 0) {
                    // exit point needs at least an incoming
                    setFatalError();
                    StringBuilder sb = prepPseudostateErrorMsg(new StringBuilder("Fatal"), pseudo);
                    sb.append("should have at least ONE incoming transition!");
                    Util.error(sb.toString());
                }
            }

            // be defined EITHER on a State or a StateMachine
            if (pseudo.getState() == null && pseudo.getStatemachine() == null) {
                setFatalError();
                StringBuilder sb = prepPseudostateErrorMsg(new StringBuilder("Fatal"), pseudo);
                sb.append("is a ghost; define it on EITHER a State or a StateMachine!");
                Util.error(sb.toString());
            }
        }

        // inner transition to an exitPoint from a State MUST have a trigger
        if (pseudo.getKind() == PseudostateKind.exitPoint) {
            boolean missingInTrigger = false;
            for (Transition transition : pseudo.getIncoming()) {
                if (transition.getSource() != null
                        && transition.getSource() instanceof State
                        && transition.getAllEvents().size() == 0) {
                    missingInTrigger = true;
                    break;
                }
            }
            if (missingInTrigger) {
                setFatalError();
                StringBuilder sb = prepPseudostateErrorMsg(new StringBuilder("Fatal"), pseudo);
                sb.append("must have a trigger on any incoming transition from a State!");
                Util.error(sb.toString());
            }
        }
    }

    private StringBuilder prepPseudostateErrorMsg (StringBuilder sb, Pseudostate pseudo) {
        sb.append(": Pseudostate ");
        sb.append(pseudo.getKind().name()).append(" '");
        sb.append(pseudo.getQualifiedName()).append("' ");
        return sb;
    }

    private Map<String,State> stateNameSet = null;

    /**
     * Chekcs for States with duplicate names, report if found; error is fatal.
     * Ignore duplicates based on zero-length names.
     * @param s  UML {@link State} whose name to check for duplicates in
     *          {@link StateMachine} scope.
     */
    @Validate
    public void checkDuplicateStateName (State s) {
        String name = s.getName();
        if (name != null && name.length() > 0) {  // non-zero-length name only
            if (stateNameSet.containsKey(name)) {
                // potential state w/ duplicate name?
                State dupState = stateNameSet.get(name);
                if (s != dupState) {  // duplicate found!
                    setFatalError();
                    Util.error(new StringBuilder("Fatal: Duplicate name '")
                            .append(name).append("' found for States ")
                            .append(dupState.getQualifiedName()).append(" and ")
                            .append(s.getQualifiedName()).append("!")
                            .toString());
                } else {
                    // TODO A state has been visited twice! One case: caused by two initial states pointing to same state
                }
            } else {  // we're OK
                stateNameSet.put(name, s);
            }
        }
    }

    /**
     * Checks for States with Do activity to have single action; else complain.
     * Also checks that the single action is a function call.
     * @param s  UML {@link State} whose Do activity to check, if any.
     */
    @Validate
    public void checkSingleDoActivity (State s) {
        if (s.getDo() != null) { 
            List<String> actions = s.getDo().actionList();
            if (actions.size() > 0) {
                if (!FunctionCall.isFunctionCall(actions.get(0))) {
                    setValidationIssue();
                    Util.error("Warning: Do-Activity in State '"
                            + s.getQualifiedName() + "' should be a function call of the form 'action()'!");
                }
            }
            if (actions.size() > 1) {
                setValidationIssue();
                StringBuilder sb = new StringBuilder("Warning: Multiple actions in Do-Activity of State '")
                    .append(s.getQualifiedName())
                    .append("'; ignoring ").append(actions.get(1));
                for (int i=2; i < actions.size(); ++i) {
                    sb.append(", ").append(actions.get(i));
                }
                Util.error(sb.append("!").toString());
            }
        }
    }

    /**
     * Looking only at a state's outgoing transitions, checks to make sure
     * that a transition is OK: <ul>
     * <li> makes sure no duplicate signal events is found on two out-transitions
     * <li> makes sure transition target state is resolvable
     * <li> makes sure all signal events resolve to a signal object
     * <li> makes sure all transition signals resolve to a signal object
     * <li> makes sure all time events have a timer expression
     * <li> makes sure no simple state has empty out-transitions
     * <li> makes sure no non-simple state has multiple empty out-transitions
     * <li> makes sure NO state has more than one timer transitions
     * </ul>
     * N.B.: This means events that aren't used are ignored.
     * 
     * @param state  UML {@link State} whose outgoing transitions to check
     */
    @Validate
    public void checkTransitionOK (State state) {
        // struct of 2 data pieces:  transition and event
        final class TransitionEvent {
            public Transition transition;
            public Event event;
            public TransitionEvent (Transition t, Event e) {
                transition = t;
                event = e;
            }
        }
        Map<String,TransitionEvent> signalSeen = new HashMap<String,TransitionEvent>();
        List<Transition> emptyTrans = Util.newList();
        for (Transition transition : state.getOutgoing()) {
            // collect transitions that don't have any defined event
            if (transition.getAllEvents().size() == 0) {
                emptyTrans.add(transition);
            }

            // check transition target resolves to a Vertex
            checkTransitionTarget(state, transition);

            // check triggers
            boolean hasTimeout = false;
            boolean hasSignal = false;
            String signalName = null;
            for (Trigger t : transition.getTrigger()) {
                if (t.getEvent() instanceof SignalEvent) {
                    hasSignal = true;
                    SignalEvent ev = ((SignalEvent) t.getEvent());
                    if (!shouldSkipMethod("checkSignalEvent", state, transition, ev)
                            && checkSignalEvent(state, transition, ev))
                    {
                        signalName = ev.getSignal().getName();
                        if (signalSeen.containsKey(signalName)) {
                            // fatal! duplicate out-transition events
                            setFatalError();
                            StringBuilder sb = new StringBuilder("Fatal: Out-transitions with duplicate signal event!");
                            appendStateTransitionInfo(sb, state, transition, ev);
                            sb.append(NL).append("  (s,t2,s2):= ");
                            TransitionEvent teObj = signalSeen.get(signalName);
                            appendTransitionEventInfo(sb, state, teObj.transition, teObj.event);
                            Util.error(sb.toString());
                        } else {  // mark this signal as seen
                            signalSeen.put(signalName, new TransitionEvent(transition, ev));
                        }
                    }
                } else if (t.getEvent() instanceof TimeEvent) {
                    hasTimeout = true;
                    if (!shouldSkipMethod("checkTimeEvent", state, transition, t.getEvent())) {
                        checkTimeEvent(state, transition, (TimeEvent) t.getEvent());
                    }
                }
                if (hasSignal && hasTimeout) {  // error!
                    setFatalError();
                    Util.error("Fatal: Invalid transition "
                            + t.id() + " (signal " + signalName
                            + ") can't have both trigger event and timeout.");
                    break;  // done with triggers, next transition!
                }
            }
        }

        // check and validate empty out-transitions
        checkEmptyOutTransitions(state, emptyTrans);
    }

    private boolean checkTransitionTarget (State state, Transition t) {
        boolean rv = true;

        Vertex target = t.getTarget();
        if (target == null) {
            rv = false;
            setFatalError();
            if (Util.isErrorLevel()) {
                // distinguish between NO transition target defined, or unresolved target
                StringBuilder sb = new StringBuilder("Fatal: Transition ");
                String targetID = Util.getNodeAttribute(((UMLElement) t).getNode(),
                        UMLIdentifiers.inst().lit(UMLLabel.KEY_TARGET));
                if (targetID == null) {
                    sb.append("does NOT specify a target State!");
                } else {
                    sb.append("specifies an unresolvable target State ID '")
                        .append(targetID).append("'!");
                }
                appendStateTransitionInfo(sb, state, t, null);
                Util.error(sb.toString());
            }
        }
        
        return rv;
    }

    private boolean checkSignalEvent (State state, Transition t, SignalEvent ev) {
        boolean rv = true;

        Signal signal = ev.getSignal();
        if (signal == null) {
            rv = false;
            setFatalError();
            if (Util.isErrorLevel()) {
                // distinguish between NO signal defined at all, or unresolved Signal ID
                StringBuilder msgSB = new StringBuilder("Fatal: SignalEvent ");
                String signalID = Util.getNodeAttribute(((UMLElement) ev).getNode(),
                        UMLIdentifiers.inst().lit(UMLLabel.KEY_SIGNAL));
                if (signalID == null) {
                    signalID = UMLIdentifiers.inst().signalEvent_getReferencedSignalId(((UMLElement) ev).getNode());
                    if (signalID == null || signalID.length() == 0) {
                        // no signal defined at all!
                        msgSB.append("'").append(ev.getName()).append("' does NOT specify a Signal!");
                    } else {  // href! try and see if model file exists
                        int at = signalID.indexOf("#");
                        if (at > -1) {
                            String filename = signalID.substring(0, at);
                            String uid = signalID.substring(at+1);
                            if (UMLModelGroup.findModelOfName(filename) == null) {
                                msgSB.append("specifies a REMOTE Signal ID '")
                                    .append(uid).append("' in model file '")
                                    .append(filename)
                                    .append("', which need to be supplied as an input file!\n\t");
                            }
                        } else {  // make do with SignalEvent's name, if any
                            msgSB.append("'").append(ev.getName()).append("' does NOT specify a valid corresponding Signal!");
                        }
                    }
                } else {  // defined but can't dereference XMI ID!
                    msgSB.append("specifies an UNRESOLVABLE Signal ID '")
                        .append(signalID).append("'!");
                }
                appendStateTransitionInfo(msgSB, state, t, ev);
                Util.error(msgSB.toString());
            }
        }

        return rv;
    }

    private Map<State,Boolean > hasTimerTranMap = Util.newMap();

    private boolean checkTimeEvent (State state, Transition t, TimeEvent ev) {
        boolean rv = true;

        // keep a tally on number of timer tansitions per state
        Boolean hasTimerTran = hasTimerTranMap.get(state);
        if (hasTimerTran == null) {
            hasTimerTran = false;
        }
        if (hasTimerTran) {  // means more than 1 timer-event transition!
            rv = false;
            setFatalError();
            if (Util.isErrorLevel()) {
                StringBuilder msgSB = new StringBuilder("Fatal: More than ONE time event transition from a state!");
                appendStateTransitionInfo(msgSB, state, t, ev);
                Util.error(msgSB.toString());
            }
        } else {
            hasTimerTran = true;
            hasTimerTranMap.put(state, hasTimerTran);
        }

        // check valid time OpaqueExpression on timer transition
        ValueSpecification expr = ev.getWhen();
        if (expr == null) {
            rv = false;
            setFatalError();
            if (Util.isErrorLevel()) {
                StringBuilder msgSB = new StringBuilder("Fatal: TimeEvent with NO timer OpaqueExpression!");
                appendStateTransitionInfo(msgSB, state, t, ev);
                Util.error(msgSB.toString());
            }
        } else {
            checkExpressionSubtype(expr);
        }

        return rv;
    }

    private void checkEmptyOutTransitions (State state, List<Transition> emptyTrans) {
        // check and validate empty out-transitions
        StringBuilder sb = null;  // no fatal error if sb remains null
        if (state.isSimple()) {
            if (emptyTrans.size() > 0) {
                sb = new StringBuilder("Fatal: Simple state with empty outgoing transition(s)!");
            }
        } else {  // composite state
            // see if any connection point refs; if none check only ONE empty
            if (state.getConnection().size() == 0 && emptyTrans.size() > 1) {
                sb = new StringBuilder("Fatal: State with more than ONE empty outgoing transitions, but NO connection point references!");
            } else {
                // check each entry/exit conn point ref has ONE empty trans!
                for (ConnectionPointReference cpr : state.getConnection()) {
                    if (cpr.getKind() == PseudostateKind.exitPoint
                            && (cpr.getOutgoing().size() != 1
                                || /* ONE transition */ cpr.getOutgoing().iterator().next().getAllEvents().size() != 0)) {
                        setFatalError();
                        sb = new StringBuilder("Fatal: Exit connection point '");
                        sb.append(cpr.getQualifiedName()).append("' should have exactly ONE _empty_ outgoing transition!");
                        if (cpr.getOutgoing().size() == 1) {
                            Transition t = cpr.getOutgoing().iterator().next();
                            sb.append(NL).append("  ");
                            appendTransitionEventInfo(sb, cpr, t, t.getAllEvents().iterator().next());
                        } else {
                            for (Transition t : cpr.getOutgoing()) {
                                sb.append(NL).append("  ");
                                appendTransitionEventInfo(sb, cpr, t, t.getAllEvents().iterator().next());
                            }
                        }
                        Util.error(sb.toString());
                    }
                }
                sb = null;  // do not trigger additional output below!
            }
        }
        if (sb != null) {  // encountered fatal error
            setFatalError();
            sb.append(" Machine ").append(curMachine.getQualifiedName());
            for (int i=0; i < emptyTrans.size(); ++i) {
                sb.append(NL).append("  (s,t").append(i);
                sb.append(",s'").append(i).append("):= ");
                appendTransitionEventInfo(sb, state, emptyTrans.get(i), null);
            }
            Util.error(sb.toString());
        }
    }

    /**
     * Checks that given ValueSpecification is of a supported subtype.
     * 
     * @param spec  UML {@link ValueSpecification} to check.
     */
    private void checkExpressionSubtype (ValueSpecification spec) {
        if (spec.isSupported()) {
            // OK, pass
        } else {
            setValidationIssue();
            Util.error("Warning: ValueSpecification " + spec.getName() + ":" + spec.getType() + " is NOT supported!");
        }
    }

    /**
     * Given a StringBuilder object, a source State, a Transition, and the
     * Trigger Event, appends first the current StateMachine, and then a string
     * containing the source state and the full transition path to allow
     * engineer to debug their StateMachine model.
     * Uses {@link #appendTransitionEventInfo(StringBuilder, Transition, Event)}.
     * 
     * @param msgSB  {@link StringBuilder} containing the message string
     * @param v      source {@link Vertex}
     * @param t      {@link Transition} out of source <code>state</code>
     * @param ev     Trigger {@link Event} on transition <code>t</code>
     */
    private void appendStateTransitionInfo (StringBuilder msgSB, Vertex v, Transition t, Event ev) {
        // offending state machine
        msgSB.append(" Machine ").append(curMachine.getQualifiedName()).append(NL);

        // info on source state
        msgSB.append("  (s,t1,s1):= ");
        appendTransitionEventInfo(msgSB, v, t, ev);
    }

    /**
     * Given a StringBuilder object, a Transition, and the Trigger Event,
     * appends a string containing only the transition and target state,
     * followed by the ID of either the offending Event or Transition, whichever
     * is available.  This helps the engineer to debug the StateMachine model.
     *  
     * @param msgSB  {@link StringBuilder} containing the message string
     * @param v      source {@link Vertex}
     * @param t      {@link Transition} out of source <code>state</code>
     * @param ev     Trigger {@link Event} on transition <code>t</code>
     */
    private void appendTransitionEventInfo (StringBuilder msgSB, Vertex v, Transition t, Event ev) {
        msgSB.append("(");
        if (v.getName() != null && v.getName().length() > 0) {
            msgSB.append(v.getName());
        } else if (v instanceof Pseudostate) {
            msgSB.append("<")
                .append(((Pseudostate) v).getKind().name().toUpperCase())
                .append(" Pseudostate>");
        } else {
            msgSB.append("<SOURCE>");
        }
        msgSB.append(", ");
        if (t.isInternal()) {  // append if transition is internal
            msgSB.append("{internal} ");
        }
        // get what label we can for transition
        if (ev != null && ev.getName() != null && ev.getName().length() > 0) {
            msgSB.append(ev.getName());
        } else {
            Iterator<Trigger> trigIter = t.getTrigger().iterator();
            if (trigIter.hasNext()) {
                Trigger trigger = trigIter.next();
                if (trigger != null && trigger.getName() != null
                        && trigger.getName().length() > 0) {
                    msgSB.append(trigger.getName());
                } else if (t.getName() != null && !t.getName().equals("")) {
                    msgSB.append(t.getName());
                } else {
                    msgSB.append("<TRANSITION>");
                }
            } else {
                msgSB.append("<TRANSITION>");
            }
        }
        if (t.getEffect() != null) {
            msgSB.append(" / ").append(t.getEffect().getName());
        }
        // do target state if available
        msgSB.append(", ");
        if (t.getTarget() != null && t.getTarget().getName() != null
                && t.getTarget().getName().length() > 0) {
            msgSB.append(t.getTarget().getName());
        } else {
            msgSB.append("<TARGET>");
        }
        msgSB.append(")").append(NL).append("        ");

        if (ev != null) {  // also include event ID
            msgSB.append("Event ID '").append(ev.id()).append("'");
        } else {  // include transition ID otherwise
            msgSB.append("Transition ID '").append(t.id()).append("'");
        }
    }


    /**
     * Checks that given behavior is of a supported subtype:
     * Activity or OpaqueBehavior.
     * @param beh  UML {@link Behavior} to check.
     */
    @Validate
    public void checkBehaviorSubtype (Behavior beh) {
        String umlType = Util.getNodeAttribute(((UMLElement) beh).getNode(), XMIIdentifiers.type());
        if (umlType != null) {
            if (umlType.equals(UMLIdentifiers.inst().prefixed(UMLLabel.TYPE_ACTIVITY))
                    || umlType.equals(UMLIdentifiers.inst().prefixed(UMLLabel.TYPE_BEH_OPAQUE))
                    || umlType.equals(UMLIdentifiers.inst().prefixed(UMLLabel.TYPE_BEH_FUNCTION))) {
                // OK, pass
            } else {
                setValidationIssue();
                Util.error("Warning: Behavior of type " + umlType
                        + " is NOT supported!");
            }
        } else {
            setValidationIssue();
            Util.error("Warning: Behavior " + beh.getQualifiedName()
                    + " (ID " + beh.id() + ") does NOT define an XMI type!");
        }
    }

    // set of function calls seen so far
    private SortedMap<String,FunctionCall> funcsDeclared = Util.newSortedMap();

    /**
     * Checks for, and warns about, impl-class guard and action functions that
     * have the same name but has been overloaded with different parameter
     * signatures; not fatal.
     * 
     * @param beh  UML {@link Behavior} to check.
     */
    @Validate
    public void checkImplFunctionOverloading (Behavior beh) {
        for (String fname : beh.actionList()) {
            if (! FunctionCall.isFunctionCall(fname)) {
                // skip check on non-function calls
                continue;
            }

            boolean overloaded = false;
            FunctionCall fc = new FunctionCall(fname);
            if (funcsDeclared.containsKey(fc.name())) {  // see if arg list different!
                FunctionCall existFc = funcsDeclared.get(fc.name());
                String[] existArgs = existFc.argList();
                String[] newArgs = fc.argList();
                if (existArgs.length != newArgs.length) {  // arg list sizes differ!
                    overloaded = true;
                } else {  // compare args individually
                    for (int i=0; i < newArgs.length; ++i) {
                        boolean isALit = Util.isLiteral(existArgs[i]);
                        boolean isBLit = Util.isLiteral(newArgs[i]);
                        if (isALit && isBLit) {  // both literals, check type
                            if ((Util.isQuotedString(existArgs[i])
                                    && Util.isQuotedString(newArgs[i]))
                                    ||
                                    (Util.isNumber(existArgs[i])
                                    && Util.isNumber(newArgs[i]))) {
                                // both are string literals, so OK
                            } else {
                                overloaded = true;
                                break;
                            }
                        } else if (!isALit && !isBLit) {  // both identifiers
                            // we cannot infer types, so nothing can be checked.
                            // let the user worry about this at compile time.  :-)
                        } else {  // literal vs identifier, assume NOT same function
                            overloaded = true;
                            break;
                        }
                    }
                }

                if (overloaded) {
                    setFatalError();
                    Util.error("Fatal! Function-overloading NOT fully checkable and may NOT behave!\n"
                            + "  Overloaded: " + fc.toString() + " vs. " + existFc.toString());
                }
            } else {
                // place new FunctionCall in map of seen functions
                funcsDeclared.put(fc.name(), fc);
            }
        }
    }

    /**
     * Checks that all action events within StateMachine have a corresponding,
     * defined Signal (by name), considering namespaces if enabled.
     * For namespace consideration, it'll be smart about checking the no-namespace
     * vs. the namespace of the state machine
     * 
     * @param sm  UML {@link StateMachine} on which to validate action signals.
     */
    @Validate
    public void checkActionSignalsDefined (StateMachine sm) {
        Map<String,Behavior> invalidSignal2Beh = Util.newSortedMap();

        // first, retrieve all signals we fire as action event
        ModelScape modelScape = model.getModelScape();
        Map<String,Behavior> firedSignals = modelScape.getCachedFiredSignals(sm);
        if (firedSignals.size() > 0) {  // now we do the heavier lifting
            // search by name in all signals within model group
            Map<String,Signal> signalsByName = modelScape.getCachedSignalsByName();
            for (String fSig : firedSignals.keySet()) {
                boolean matchedSig = false;
                // we're try 3 possible matches
                // (1) direct match,
                // (2) namespace removal match if there's namespace, and
                // (3) namespace prepend match if no namespace
                if (signalsByName.containsKey(fSig)) {
                    matchedSig = true;  // direct match!
                }
                if (!matchedSig) {  // no direct match
                    int sepIdx = fSig.lastIndexOf(TargetLanguageMapper.UML_SEPARATOR);
                    if (sepIdx > -1) {
                        // try removing namespace
                        String strippedSig = fSig.substring(sepIdx+TargetLanguageMapper.UML_SEPARATOR.length());
                        if (signalsByName.containsKey(strippedSig)) {
                            matchedSig = true;  // ns-removal match!
                        }
                    } else {
                        // try tacking on SM's namespace
                        String nsSig = Util.joinWithPrefixes(sm.getPackageNames(), fSig, TargetLanguageMapper.UML_SEPARATOR);
                        if (signalsByName.containsKey(nsSig)) {
                            matchedSig = true;  // ns-prepend match!
                        }
                    }
                }
                if (!matchedSig) {
                    invalidSignal2Beh.put(fSig, firedSignals.get(fSig));
                }
            }
        }

        if (invalidSignal2Beh.size() > 0) {
            setFatalError();
            StringBuilder sb = new StringBuilder("Fatal! Action signals in StateMachine '")
                    .append(sm.getQualifiedName())
                    .append("' with no corresponding Signal object defined!");
            // report fired signals that have no corresponding signal objects!
            for (String sig : invalidSignal2Beh.keySet()) {
                sb.append(NL).append("  -> '").append(sig)
                        .append("' defined by behavior: ")
                        .append(firedSignals.get(sig).getQualifiedName());
            }

            Util.error(sb.toString());
        }
    }
}
