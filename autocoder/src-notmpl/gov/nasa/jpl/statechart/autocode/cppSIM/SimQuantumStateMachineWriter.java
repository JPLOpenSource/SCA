package gov.nasa.jpl.statechart.autocode.cppSIM;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.autocode.gui.ExecutionTracePythonWriter;
import gov.nasa.jpl.statechart.core.Action;
import gov.nasa.jpl.statechart.core.CallAction;
import gov.nasa.jpl.statechart.core.CompositeState;
import gov.nasa.jpl.statechart.core.CompositeStateRegion;
import gov.nasa.jpl.statechart.core.ConcurrentCompositeState;
import gov.nasa.jpl.statechart.core.DeepHistoryState;
import gov.nasa.jpl.statechart.core.DiagramElement;
import gov.nasa.jpl.statechart.core.EventAction;
import gov.nasa.jpl.statechart.core.FinalState;
import gov.nasa.jpl.statechart.core.InitialState;
import gov.nasa.jpl.statechart.core.JunctionState;
import gov.nasa.jpl.statechart.core.SimpleState;
import gov.nasa.jpl.statechart.core.State;
import gov.nasa.jpl.statechart.core.StateMachine;
import gov.nasa.jpl.statechart.core.Transition;
import gov.nasa.jpl.statechart.core.TransitionGuard;
import gov.nasa.jpl.statechart.core.TransitionTimeout;
import gov.nasa.jpl.statechart.input.StateMachineXmiReader;
import gov.nasa.jpl.statechart.input.magicdraw.MagicDrawUmlReader;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;



/**
 * <p>
 * This class writes out a state machine in the form of C++ code based on the
 * Quantum Framework model developed by Miro Samek. The code is generated from
 * an internal representation of StateMachine, State, and Transition objects.
 * </p>
 * 
 * <p>
 * Copyright 2005, by the California Institute of Technology. ALL RIGHTS
 * RESERVED. United States Government Sponsorship acknowledged. Any commercial
 * use must be negotiated with the Office of Technology Transfer at the
 * California Institute of Technology.
 * </p>
 * 
 * <p>
 * This software is subject to U.S. export control laws and regulations and has
 * been classified as 4D993. By accepting this software, the user agrees to
 * comply with all applicable U.S. export laws and regulations. User has the
 * responsibility to obtain export licenses, or other export authority as may be
 * required before exporting such information to foreign countries or providing
 * access to foreign persons.
 * </p>
 * 
 * <p>
 * CVS Identification: $Id: SimQuantumStateMachineWriter.java,v 1.26 2005/10/11
 * 18:08:09 kclark Exp $
 * </p>
 * 
 * @see StateMachine
 * @see State
 * @see Transition
 * @see DiagramElement
 * 
 * @author Alex Murray not attributable
 * @author Ken Clark not attributable
 * @author Eddie Benowitz not attributable
 */
public class SimQuantumStateMachineWriter {
	private StateMachine machine;

	private StateMachineXmiReader reader;

	private PrintStream declarations;

	private PrintStream definitions;

	private String classname;

	private String implName;

	private String declarationsFilename;

	private String definitionsFilename;

	private String indent = "";

	private boolean executionTraceOn = true;

	private static final int STRINGSIZE = 256;

	/**
	 * Constructor.
	 * 
	 * @param reader
	 *            StateMachineXmiReader The reader object containing the set of
	 *            state machine representations.
	 */
	SimQuantumStateMachineWriter(StateMachineXmiReader reader) throws Exception {
		this.reader = reader;
		this.executionTraceOn = Autocoder.isExecutionTraceOn();
	}

	/**
	 * Writes out the C++ implementation files for the set of state machines
	 * contained in this instance's reader.
	 * 
	 * @throws Exception
	 */
	public void writeAllStateMachines() throws Exception {

		Map<String, StateMachine> machines = reader.getStateMachineMap();
		Iterator i = machines.values().iterator();
		while (i.hasNext()) {
			StateMachine nextStateMachine = (StateMachine) i.next();
			this.machine = nextStateMachine;
			this.classname = nextStateMachine.name();
			writeStateMachine();
			getTerminalStates(machine);
		}
	}

	/**
	 * Creates and writes out the Quantum Framework C++ code which implements
	 * the state machine.
	 * 
	 * @throws Exception
	 */
	private void writeStateMachine() throws Exception {
		implName = classname + "Impl";
		declarationsFilename = classname + ".h";
		definitionsFilename = classname + ".cpp";
		declarations = new PrintStream(new FileOutputStream(
				declarationsFilename));
		definitions = new PrintStream(new FileOutputStream(definitionsFilename));
		System.out.println("Writing statechart " + machine.name()
				+ " to files " + declarationsFilename + " and "
				+ definitionsFilename + "...");
		writeDeclarations();
		writeDefinitions();
	}

	/**
	 * Creates and writes out the C++ Quantum Framework header file code for the
	 * state machine.
	 * 
	 * @throws Exception
	 */
	private void writeDeclarations() throws Exception {

		declarations.println("/**");
		declarations.println(" * @file " + declarationsFilename);
		declarations.println(" *");
		declarations
				.println(" * This file was generated by the SIM MagicDraw statechart converter,");
		declarations
				.println(" * which converts MagicDraw statecharts expressed in XML to Miro Samek's");
		declarations.println(" * C++ Quantum Framework.");
		declarations.println(" *");
		GregorianCalendar calendar = new GregorianCalendar();
		declarations.println(" * &copy " + calendar.get(GregorianCalendar.YEAR)
				+ " Jet Propulsion Lab / California Institute of Technology");
		declarations.println(" */");
		declarations.println("");
		String mnemonic = "_" + machine.name().toLowerCase() + "_h";
		declarations.println("#ifndef " + mnemonic);
		declarations.println("#define " + mnemonic);
		declarations.println("");
		declarations.println("#include \"port.h\"");
		declarations.println("#include <string.h>");
      declarations.println("#include \"my_timer.h\"");


		declarations.println("");
		writeStateEnumDeclaration();
	
		// declarations.println("#include <string>");
		declarations.println("");
		// Forward declaration of impl class:
		declarations.println(indent + "class " + implName + ";");
		declarations.println("");
		// Check if we're doing concurrent regions for this state machine:
		indent = "  ";
		Iterator newstateIter = machine.states().iterator();
		while (newstateIter.hasNext()) {
			State state = (State) newstateIter.next();
			if (state instanceof CompositeState) {
				for (CompositeStateRegion s : ((CompositeState) state).getAllSubRegions())
            {
					writeStateEnumDeclarationRegion( s);
					writeOneRegionDeclaration( s);
				}
			}
		}
		// Write QActive class declaration:
		declarations.println("class " + classname + " : public QActive {");
		declarations.println("");
		indent = "  ";
		declarations.println(indent + "public:");
		indent = "    ";
		declarations.println(indent + classname + "(char* newobjName, "
				+ implName + "* implPtr)");
		indent = "          ";
		declarations.println(indent + ": QActive((QPseudoState)&" + classname
				+ "::initial)");
		// declarations.println(indent + ", objName(objName)");
      ///// timers
      
      for(State s: machine.states())
      {
         writeTimerDefinitionForSubtree(s, true);         
      }
      
      ////
		declarations.println(indent + ", impl(implPtr)");
		indent = "    ";
		declarations.println(indent + "{");
		declarations.println(indent + "   strncpy(objName, newobjName, "
				+ (STRINGSIZE - 1) + ");");
		newstateIter = machine.states().iterator();
		while (newstateIter.hasNext()) {
			State state = (State) newstateIter.next();
			if (state instanceof CompositeState) {
				for ( State s : ((CompositeState) state).getAllSubRegions())
            {
					String name = s.name();
					declarations.println(indent + "  " + name.toLowerCase()
							+ " = new " + s.name() + "(objName, implPtr);");
				}
			}
		}
		List<CompositeState> compositeswithhistory = getHistoryContainers(machine
				.states());
		for (State compwithistory : compositeswithhistory) {
			declarations.println(indent + "  my" + compwithistory.name()
					+ "history = NULL;");
		}
		declarations.println(indent + "}");
		declarations.println("");
		// Remove the "protected" keyword below so that the state method
		// declarations
		// will be public. This is because the *Impl class, which is not derived
		// from this class, needs to be able to reference the address of the
		// member
		// functions.
		// indent = " ";
		// declarations.println(indent + "protected:");
		// indent = " ";
		declarations.println(indent + "void " + "initial(QEvent const* e);");
		Iterator stateIter = machine.states().iterator();
		while (stateIter.hasNext()) {
			State state = (State) stateIter.next();
			// if (null == stateWithRegions || state != stateWithRegions) {
			if (!(state instanceof ConcurrentCompositeState)) {
				writeMethodPrototypesForSubtree(state);
			} else {
				writeOneMethodPrototype(state);
			}
		}
		declarations.println("");
		// ///// private sub parallel state machines.
		stateIter = machine.states().iterator();
		while (stateIter.hasNext()) {
			State state = (State) stateIter.next();
			if (state instanceof CompositeState) {
				for (State s : ((CompositeState) state).getAllSubRegions())
            {
					declarations.println("    " + s.name() + " * "
							+ s.name().toLowerCase() + ";");
				}
			}
		}
		
		declarations.println(indent + "StateEnum" + machine.name() + " getCurrentState(){ return mystate;}");
		declarations.println(indent + "char * getCurrentStateName();" );		
		indent = "  ";
		declarations.println(indent + "private:");
		indent = "    ";
		declarations.println(indent + "char objName[" + STRINGSIZE + "];");
		declarations.println(indent + implName + "* impl;");
		// for each composite state with a history, write a history variable
		List<CompositeState> compositeswithhistory2 = getHistoryContainers(machine
				.states());
		for (CompositeState mycomposite : compositeswithhistory2) {
			declarations.println(indent + "QState my" + mycomposite.name()
					+ "history;");
		}
		declarations.println(indent + "StateEnum" + machine.name() + " mystate;");		
      
      // timer declarations
      stateIter = machine.states().iterator();      
      while (stateIter.hasNext()) {
         State state = (State) stateIter.next();
         // if (null == stateWithRegions || state != stateWithRegions) {
         if (!(state instanceof ConcurrentCompositeState)) {
            writeTimerDeclarationForSubtree(state);
         } else {
            writeOneTimerDeclaration(state);
         }
      }
      
      //
      
      
		declarations.println("");
		declarations.println("};");
		declarations.println("#endif // " + mnemonic);
		indent = "";
	}

   private void writeTimerDefinitionForSubtree(State state, boolean declarationsfile)
   {
      writeOneTimerDefinition(state, declarationsfile);
      if (!(state instanceof ConcurrentCompositeState)) {
         if (state instanceof CompositeState) {
            Iterator child = ((CompositeState) state).getChildren()
                  .iterator();
            while (child.hasNext()) {
               State childState = (State) child.next();
               writeTimerDefinitionForSubtree(childState, declarationsfile);
            }
         }
      }      
   }
   
   private void writeOneTimerDefinition(State state, boolean declarationsfile)
   {     
      if (!(state instanceof SimpleState))
         return;

      List outList = ((SimpleState) state).getOutgoing();

      if (((SimpleState) state).hasTimeout())
      {
         Iterator i = outList.iterator();
         while (i.hasNext())
         {
            Transition t = (Transition) i.next();
            if (null != t.timeout())
            {
               if(declarationsfile)
               {
                  declarations.println(indent + "  ," + state.name()
                     + "Timer(" + t.timeout().getTimeout() + ") ");
               }
               else
               {
                  definitions.println(indent + "  ," + state.name()
                        + "Timer(" + t.timeout().getTimeout() + ") ");
                  
               }
            }
         }
      }
   }

   
   private void writeTimerDeclarationForSubtree(State state)
   {
      writeOneTimerDeclaration(state);
      if (!(state instanceof ConcurrentCompositeState)) {
         if (state instanceof CompositeState) {
            Iterator child = ((CompositeState) state).getChildren()
                  .iterator();
            while (child.hasNext()) {
               State childState = (State) child.next();
               writeTimerDeclarationForSubtree(childState);
            }
         }
      }
      
   }
   
   private void writeOneTimerDeclaration(State state)
   {     
      if (!(state instanceof SimpleState))
         return;

      List outList = ((SimpleState) state).getOutgoing();

      if (((SimpleState) state).hasTimeout())
      {
         Iterator i = outList.iterator();
         while (i.hasNext())
         {
            Transition t = (Transition) i.next();
            if (null != t.timeout())
            {
               declarations.println(indent + "MyTimer " + state.name()
                     + "Timer;");
            }
         }
      }
   }
   
	private List<CompositeState> getHistoryContainers(List<State> states) {

		List<CompositeState> historystates = new ArrayList<CompositeState>();
		for (State s : states) {
			if (s instanceof CompositeState) {
				CompositeState composite = (CompositeState) s;
				if (composite.containsHistoryState()) {
					historystates.add(composite);
				}
				if (!(s instanceof ConcurrentCompositeState)) {
					historystates.addAll(getHistoryContainers(composite
							.getChildren()));
				}
			}
		}
		return historystates;
	}

	private void writeOneRegionDeclaration(CompositeStateRegion region)
			throws Exception {
      
      Iterator stateIter;      

		declarations.println("class " + region.name() + ": public QHsm {");
		indent = "  ";
		declarations.println(indent + "public:");
		indent = "    ";
		declarations.println(indent + region.name() + "(char * objName, "
				+ implName + "* implPtr);");
		declarations.println(indent + "void " + "initial(QEvent const* e);");
		Iterator childIter = region.getChildren().iterator();
		while (childIter.hasNext()) {
			writeMethodPrototypesForSubtree((State) childIter.next());
		}
		declarations.println(indent + "StateEnum" + region.name() + " getCurrentState(){ return mystate;}" );		
		declarations.println(indent + "char * getCurrentStateName();" );		
		declarations.println("");
		indent = "  ";
		declarations.println(indent + "private:");
		indent = "    ";
      //// timer
      stateIter = region.getChildren().iterator();      
      while (stateIter.hasNext()) {
         State state = (State) stateIter.next();
         // if (null == stateWithRegions || state != stateWithRegions) {
         if (!(state instanceof ConcurrentCompositeState)) {
            writeTimerDeclarationForSubtree(state);
         } else {
            writeOneTimerDeclaration(state);
         }
      }            
      ///
      
		declarations.println(indent + "StateEnum" + region.name() + " mystate;" );		
		declarations.println(indent + "char objName[" + STRINGSIZE + "];");
		declarations.println(indent + implName + "* impl;");
		declarations.println(indent + "friend class " + classname + ";");
		List<CompositeState> compositeswithhistory = getHistoryContainers(region
				.getChildren());
		for (State compwithistory : compositeswithhistory) {
			declarations.println(indent + "QState my" + compwithistory.name()
					+ "history;");
		}
		indent = "";
		declarations.println("};");
      
		declarations.println("");
	}

	private void writeMethodPrototypesForSubtree(State state) throws Exception {

		writeOneMethodPrototype(state);
		if (!(state instanceof ConcurrentCompositeState)) {
			if (state instanceof CompositeState) {
				String saveIndent = indent;
				indent = indent + "  ";
				Iterator child = ((CompositeState) state).getChildren()
						.iterator();
				while (child.hasNext()) {
					State childState = (State) child.next();
					writeMethodPrototypesForSubtree(childState);
				}
				indent = saveIndent;
			}
		}
	}

	/**
	 * Writes the corresponding method prototype for the specified state.
	 * 
	 * @param s
	 *            State Starting state.
	 * @throws Exception
	 */
	private void writeOneMethodPrototype(State s) throws Exception {

		// If this is simple or composite, it needs a method.
		if (s instanceof SimpleState || s instanceof CompositeState) {
			declarations.println(indent + "QSTATE " + s.name()
					+ "(QEvent const *e);\t\t//state handler");
		}
	}

	/**
	 * Creates and writes out the C++ Quantum Framework body file code for the
	 * state machine.
	 * 
	 * @throws Exception
	 */
	private void writeDefinitions() throws Exception {

		definitions.println("/**");
		definitions.println(" * @file " + definitionsFilename);
		definitions.println(" *");
		definitions
				.println(" * This file was generated by the SIM MagicDraw statechart converter,");
		definitions
				.println(" * which converts MagicDraw statecharts expressed in XML to Miro Samek's");
		definitions.println(" * C++ Quantum Framework.");
		definitions.println(" *");
		GregorianCalendar calendar = new GregorianCalendar();
		definitions.println(" * &copy " + calendar.get(GregorianCalendar.YEAR)
				+ " Jet Propulsion Lab / California Institute of Technology");
		definitions.println(" */");
		definitions.println("");
		definitions.println("#include <stdlib.h>");
		definitions.println("#include <string.h>");
		definitions.println("#include \"qassert.h\"");
		definitions.println("#include \"port.h\"");
		definitions.println("#include \"qevent.h\"");
		definitions.println("#include \"qequeue.h\"");
		definitions.println("#include \"qactive.h\"");
		definitions.println("#include \"qf.h\"");
		definitions.println("#include \"my_timer.h\"");
		definitions.println("#include \"" + SimRtcSignalWriter.signalEnumName
				+ ".h" + "\"");
		definitions.println("#include \"" + classname + ".h\"");
		definitions.println("#include \"" + implName + ".h\"");
		if (executionTraceOn) {
			definitions.println("#include \"log_event.h\"");
		}
		definitions.println("");
		// Write initial method for state machine:
		writeQActiveInitialMethod();
		// Get any other top-level states.
		Iterator stateIter = machine.states().iterator();
		while (stateIter.hasNext()) {
			State state = (State) stateIter.next();
			// if (null == stateWithRegions || state != stateWithRegions) {
			writeMethodBodiesForSubtree(null, this.classname, state);
			// }
		}
		writeStateEnumDefinition();
	}

	private void writeInitialMethod(CompositeStateRegion region)
			throws Exception {

		definitions.println("");
		definitions.println("void " + region.name()
				+ "::initial(QEvent const *e) {");
		// Find the initial state for this region:
		List<State> statelist = new ArrayList<State>();
		statelist.add(region);
		State initial = findInitialState(statelist);
		if (null == initial)
			throw new Exception("Composite state region " + region.name()
					+ " must specify an initial substate.");
		State initialState = machine.getTargetState(((Transition) initial
				.getOutgoing().get(0)).id());
		definitions.println(indent + "  Q_INIT(&" + region.name() + "::"
				+ initialState.name() + ");");
		definitions.println("}");
		definitions.println("");
	}

	private void writeQHSMConstructor(CompositeStateRegion region) {

		definitions.println("");
		definitions.println(region.name() + "::" + region.name()
				+ "(char * objName, " + classname + "Impl * implPtr) ");
		definitions.println("  : QHsm((QPseudoState) & " + region.name()
				+ "::initial)");
      ////////////// timer
      for(State s:region.getChildren())
      {
         writeTimerDefinitionForSubtree(s, false);         
      }
      
      /////////////// timer       
      
      definitions.println("{");
		char firstchar = classname.charAt(0);
		firstchar = Character.toLowerCase(firstchar);
		// String name = Character.toString(firstchar) + classname.substring(1);
		definitions.println("   this->objName = objName;");
		definitions.println("   impl = implPtr;");
		List<CompositeState> containers = getHistoryContainers(region
				.getChildren());
		for (CompositeState composite : containers) {
			definitions.println("   my" + composite.name() + "history = NULL;");
		}
		definitions.println("}");
		definitions.println("");
	}

	private void writeQActiveInitialMethod() throws Exception {

		Set<String> subscribeset = new HashSet<String>();
		definitions.println("");
		definitions.println("void " + classname
				+ "::initial(QEvent const *e) {");
		indent = "   ";

		// write any actions on the state machine's initial transition

		State state = findInitialState(machine.states());
		if (null == state || state.getOutgoing().size() != 1) {
			throw new Exception("Invalid state machine " + machine.name()
					+ ": Initial pseudostate either not found or invalid.");
		}

		InitialState startstate = (InitialState) state;
		Transition initialtransition = startstate.getOutgoing().get(0);
		writeActionList(initialtransition.getActions());

		// Subscribe to list of global events referenced by this statechart:
		Iterator tranIter = machine.transitions().iterator();
		while (tranIter.hasNext()) {
			Transition tran = (Transition) tranIter.next();
			if (!tran.isLocalEvent() && null != tran.signalName()) {
				for (String eventname : tran.getSignalNames()) {
					subscribeset.add(eventname);
				}
			}
		}
		for (String name : subscribeset) {
			definitions.println(indent + "QF::subscribe(this, " + name + ");");
		}
		// Check if we're doing regions:
		// Now initialize this state machine to an initial state:
		definitions.print(indent + "Q_INIT(&" + classname + "::");
		/**
		 * Find the outermost initial type pseudostate for this machine, find
		 * its outgoing transition, and then the actual initial state which is
		 * the target of the transition. This is the state we want to Q_INIT
		 * to...
		 */
		State initialState = machine.getTargetState(((Transition) state
				.getOutgoing().get(0)).id());
		definitions.println(initialState.name() + ");");
		definitions.println("}");
		definitions.println("");
		indent = "";
	}

	/**
	 * Does a breath-first search of the state tree looking for the first
	 * initial state.
	 * 
	 * @param stateTree
	 *            Collection
	 * @return State Found intial state, or <B>null</B> if not found in tree.
	 */
	private State findInitialState(Collection stateTree) {

		Iterator stateIter = stateTree.iterator();
		while (stateIter.hasNext()) {
			State s = (State) stateIter.next();
			if (s instanceof InitialState) {
				return s;
			}
		}
		// Not found at this level, so search subtree(s):
		stateIter = stateTree.iterator();
		while (stateIter.hasNext()) {
			State s = (State) stateIter.next();
			if (s instanceof CompositeState) {
				Collection c = ((CompositeState) s).getChildren();
				if (null != c) {
					State s2 = findInitialState(c);
					if (null != s2) {
						return s2;
					}
				}
			}
		}
		// Initial state not found...
		return null;
	}

	private void writeOneRegionDefinition(CompositeStateRegion region)
			throws Exception {

		writeQHSMConstructor(region);
		writeInitialMethod(region);
		// Get any other top-level states.
		Iterator stateIter = region.getChildren().iterator();
		while (stateIter.hasNext()) {
			State state = (State) stateIter.next();
			// if (null == stateWithRegions || state != stateWithRegions) {
			writeMethodBodiesForSubtree(null, region.name(), state);
			// }
		}
		
		writeStateEnumDefinition(region); 
	}

	/**
	 * Recursively creates and writes out state method bodies for the subtree
	 * starting at the specified state.
	 * 
	 * @param parent
	 *            State Parent of starting state.
	 * @param state
	 *            State Starting state.
	 * @throws Exception
	 */
	private void writeMethodBodiesForSubtree(State parent, String classname,
			State state) throws Exception {

		if (state instanceof CompositeStateRegion) {
			writeOneRegionDefinition((CompositeStateRegion) state);
		} else {
			writeOneMethodBody(parent, classname, state);
			if (state instanceof CompositeState) {
				Iterator child = ((CompositeState) state).getChildren()
						.iterator();
				while (child.hasNext()) {
					State childState = (State) child.next();
					writeMethodBodiesForSubtree(state, classname, childState);
				}
			}
		}
	}

	/**
	 * Creates and writes out the Quantum Framework C++ code which implements
	 * the specified state.
	 * 
	 * @param parentState
	 *            State Parent of state to implement.
	 * @param state
	 *            State State to implement.
	 * @throws Exception
	 */
	private void writeOneMethodBody(State parentState, String classname,
			State state) throws Exception {

		if (state instanceof InitialState || state instanceof FinalState
				|| state instanceof JunctionState
				|| state instanceof DeepHistoryState) {
			// Nothing to do for these states...
			return;
		}
		List outList = ((SimpleState) state).getOutgoing();
		definitions.println("QSTATE " + classname + "::" + state.name()
				+ "(QEvent const *e) {");
		indent = "  ";
		if (executionTraceOn) {
			definitions.println(indent + "char stateName[" + STRINGSIZE
					+ "];\n");
			definitions.println(indent + "strncpy(stateName, objName, "
					+ (STRINGSIZE - 1) + ");");
			definitions.println(indent + "strncat(stateName, \" "
					+ state.name() + "\", " + (STRINGSIZE - 1) + ");");
		}
/***		if (((SimpleState) state).hasTimeout()) {
			Iterator i = outList.iterator();
			while (i.hasNext()) {
				Transition t = (Transition) i.next();
				if (null != t.timeout()) {
					definitions.println(indent + "static MyTimer timer("
							+ t.timeout().getTimeout() + ");");
				}
			}
		}
***/      
		// Write main switch statement for all events handled by this state:
		definitions.println(indent + "switch (e->sig) {");
		/**
		 * Generate Entry condition code: (Note: even if we don't have any entry
		 * actions, we still want to signal to the user that we've entered a new
		 * state.)
		 */
		indent = "    ";
		definitions.println(indent + "case Q_ENTRY_SIG:");
		indent = "      ";
		
		boolean compositestate = state instanceof ConcurrentCompositeState;
		boolean simplestate = ! (state instanceof CompositeState);
		if(simplestate || compositestate)
		{		
			definitions.println(indent + "mystate = " + classname.toUpperCase() 
				+ "_" + state.name().toUpperCase() +   ";");
		}
		
		if (executionTraceOn) {
			definitions.println(indent + "strncat(stateName, \" ENTRY\", "
					+ (STRINGSIZE - 1) + ");");
			definitions.println(indent + "LogEvent::log(stateName);");
		}
		if (((SimpleState) state).hasTimeout()) {
			definitions.println(indent + state.name() + "Timer.startTimer();");
		}
		if (state instanceof ConcurrentCompositeState) {
			List<State> kids = ((ConcurrentCompositeState) state).getChildren();
			for (State kidstate : kids) {
				definitions.println(indent + kidstate.name().toLowerCase()
						+ "->init(e);");
			}
		}
		if (null != state.getEntryActions()) {
			writeActionList(state.getEntryActions());
		}
		definitions.println(indent + "return 0;");
		if (!state.getDuringActions().isEmpty()
				|| state instanceof ConcurrentCompositeState) {
			// Generate "During" code:
			indent = "    ";
			definitions.println(indent + "case Q_DURING:");
			indent = "      ";
			writeActionList(state.getDuringActions());
			if (state instanceof ConcurrentCompositeState) {
				List<State> kids = ((ConcurrentCompositeState) state)
						.getChildren();
				for (State kidstate : kids) {
					definitions.println(indent + kidstate.name().toLowerCase()
							+ "->dispatch(e);");
				}
			}
			definitions.println(indent + "return 0;");
		}
		// Generate Exit condition code:
		indent = "    ";
		definitions.println(indent + "case Q_EXIT_SIG:");
		indent = "      ";
		if (executionTraceOn) {
			definitions.println(indent + "strncat(stateName, \" EXIT\", "
					+ (STRINGSIZE - 1) + ");");
			definitions.println(indent + "LogEvent::log(stateName);");
		}
		if (null != state.getExitActions()) {
			writeActionList(state.getExitActions());
		}
		if (state instanceof ConcurrentCompositeState) {
			ConcurrentCompositeState concurrent = (ConcurrentCompositeState) state;
			List<State> kids = concurrent.getChildren();
			for (State kidstate : kids) {
				// definitions.println(indent + kidstate.name().toLowerCase() +
				// "->dispatch(e);");
				definitions.println(indent + kidstate.name().toLowerCase()
						+ "->Clear( (QPseudoState) &" + kidstate.name()
						+ "::initial);");
			}
		}
		if (state instanceof CompositeState) {
			CompositeState mycomposite = (CompositeState) state;
			if (mycomposite.containsHistoryState()) {
				definitions.println(indent + "my" + state.name()
						+ "history = getState();");
			}
		}
		definitions.println(indent + "return 0;");
		indent = "    ";
		// Check if we need a Q_EVAL event switch case:
		boolean hasGuardOrTimeout = ((SimpleState) state).hasGuard()
				|| ((SimpleState) state).hasTimeout()
				|| state instanceof ConcurrentCompositeState;
		if (hasGuardOrTimeout) {
			// Yes, we need a Q_EVAL case:
			indent = "    ";
			definitions.println(indent + "case Q_EVAL:");
			indent = "      ";
			Iterator i = outList.iterator();
			while (i.hasNext()) {
				Transition t = (Transition) i.next();
				if (null != t.guard() && null == t.signalName()) {
					// Generate code for transition guard evaluations:
               if(t.timeout() == null)
               {
                  writeTransitionGuard(t.guard());
               }
               else
               {
                  writeTransitionGuardWithTimeout(t.guard(), t.timeout() , state);  
               }
					indent = "        ";
					writeActionList(t.getActions());
					State targetstate = machine.getTargetState(t.id());
					if (targetstate instanceof DeepHistoryState) {
						writeToDeepHistory((DeepHistoryState) targetstate,
								classname);
					} else if (targetstate instanceof JunctionState) {
						writeJunctionState((JunctionState) targetstate,
								classname);
					} else if( ! t.isInternal() ){
						definitions.println(indent + "Q_TRAN(&" + classname
								+ "::" + machine.getTargetState(t.id()).name()
								+ ");");
					}
					definitions.println(indent + "return 0;");
					indent = "      ";
					definitions.println(indent + "}");
				} else if (null != t.timeout()) {
					// Generate code for transition timeout evaluations:
					if (null != t.signalName()) {
						throw new Exception("Invalid transition "
								+ t.signalName()
								+ " can't have both trigger event and timeout.");
					}
					definitions
							.println(indent + "if ("+ state.name() + "Timer.timedOut()) {" + "");
					indent = "        ";
					writeActionList(t.getActions());
					State targetstate = machine.getTargetState(t.id());
					if (targetstate instanceof DeepHistoryState) {
						writeToDeepHistory((DeepHistoryState) targetstate,
								classname);
					} else if (targetstate instanceof JunctionState) {
						writeJunctionState((JunctionState) targetstate,
								classname);
					} else if(! t.isInternal() ){
						definitions.println(indent + "Q_TRAN(&" + classname
								+ "::" + machine.getTargetState(t.id()).name()
								+ ");");
					}
               if(t.isInternal())
               {
                  definitions.println(indent + "break;");                  
               }
               else
               {
                  definitions.println(indent + "return 0;");
               }
					indent = "      ";
					definitions.println(indent + "}");
				}
			}
			// Check if we have subregions which need to process the Q_EVAL
			// event:
			if (state instanceof ConcurrentCompositeState) {
				List<State> kids = ((ConcurrentCompositeState) state)
						.getChildren();
				for (State kidstate : kids) {
					definitions.println(indent + kidstate.name().toLowerCase()
							+ "->dispatch(e);");
				}
			}
			definitions.println(indent + "break;");
		}
		indent = "    ";
		/**
		 * Now check for child states which we need to Q_INIT (initialize) to:
		 */
		if (state instanceof CompositeState
				&& !(state instanceof ConcurrentCompositeState)) {
			Iterator child = ((CompositeState) state).getChildren().iterator();
			Transition itran = null;
			while (child.hasNext()) {
				State childState = (State) child.next();
				if (childState instanceof InitialState) {
					itran = (Transition) ((InitialState) childState)
							.getOutgoing().get(0);
					break;
				}
			}
			if (null == itran) {
				throw new Exception("Composite state " + state.name()
						+ " must specify an initial substate.");
			} else {
				// Now find the child consumer of this event.
				child = ((CompositeState) state).getChildren().iterator();
				while (child.hasNext()) {
					State childState = (State) child.next();
					if (childState.hasIncoming(itran.id())) {
						// Must handle the Q_INIT_SIG:
						definitions.println(indent + "case Q_INIT_SIG:");
						indent = "      ";
                  Transition transition =  findInitialTransitionToMe(childState);
                  if(transition != null)
                  {
                     writeActionList(transition.getActions());
                  }                  
						definitions.println(indent + "Q_INIT(&" + classname
								+ "::" + childState.name() + ");");
						definitions.println(indent + "return 0;");
						indent = "    ";
						break;
					}
				}
			}
		}
		// Need a case branch for each out-going transition.
		List outgoings = null;
		if (state instanceof CompositeState) {
			outgoings = ((CompositeState) state).getOutgoing();
		} else if (state instanceof SimpleState) {
			outgoings = ((SimpleState) state).getOutgoing();
		}
		if (null != outgoings) {
			Iterator out = outgoings.iterator();
			while (out.hasNext()) {
				Transition t = (Transition) out.next();
				if (null != t.signalName()) {
					writeSwitchCase(t, classname);
				}
			}
		}
		// Handle dispatch to child subregions:
		if (state instanceof ConcurrentCompositeState) {
			indent = "  ";
			List<State> kids = ((ConcurrentCompositeState) state).getChildren();
			Set<String> alldesiredevents = new HashSet<String>();
			for (State kidstate : kids) {
				Set<String> desiredevents = kidstate.getAllDesiredEvents();
				alldesiredevents.addAll(desiredevents);
			}
			if (!alldesiredevents.isEmpty()) {
				for (String name : alldesiredevents) {
					if (name != null) {
						definitions.println(indent + "  case " + name + ":");
					}
				}
				for (State kidstate : kids) {
					definitions.println(indent + "    "
							+ kidstate.name().toLowerCase() + "->dispatch(e);");
				}
				definitions.println(indent + "    return 0;");
			}
		}
		// Finish up this method body:
		indent = "  ";
		definitions.println(indent + "}");
		definitions.println(indent
				+ "return (QSTATE)&"
				+ classname
				+ ((null == parentState) ? "::top"
						: ("::" + parentState.name())) + ";");
		indent = "";
		definitions.println(indent + "}");
		definitions.println("");
	}

	private void writeActionList(List<Action> actions) {

		Iterator i = actions.iterator();
		while (i.hasNext()) {
			Action action = (Action) i.next();
			writeAction(action);
		}
	}

	private void writeAction(Action action) {

		if (action instanceof EventAction) {
			definitions.println(indent + "QF::publish( Q_NEW(QEvent, "
					+ action.name() + ") );");
		} else if (action instanceof CallAction) {
			String[] calls;

			calls = action.name().split(";");
			for (String call : calls) {
				call = call.trim();
				if (call.length() > 0) {
					definitions.println(indent + "impl->" + call + ";");
				}
			}
		}
	}

	/**
	 * Writes out code for a transition guard. Note below that this method
	 * leaves opens the "if" code block, so the caller must close it by writing
	 * the closing curly brace.
	 * 
	 * @param tg
	 *            TransitionGuard
	 */
	private void writeTransitionGuard(TransitionGuard tg) {

		definitions.print(indent + "if (");
		if (tg.notOp()) {
			definitions.print("!");
		}
		definitions.println("impl->" + tg.methodName() + ") {");
	}

   private void writeTransitionGuardWithTimeout(TransitionGuard tg, TransitionTimeout timeout, State state) {

      definitions.print(indent + "if ( (");
      if (tg.notOp()) {
         definitions.print("!");
      }
      definitions.println("impl->" + tg.methodName() + " ) && ( " + state.name() + "Timer.timedOut() " +  ")) {");
   }
   
   
	private void writeSwitchCase(Transition transition, String classname)
			throws Exception {

		for (String signalname : transition.getSignalNames()) {
			indent = "    ";
			definitions.println(indent + "case " + signalname + ":");
			indent = "      ";
			if (executionTraceOn) {
				// PR #10, remove prints of everything except entry and exit
				// definitions.println(indent + "LogEvent::log(stateName + \" "
				// + signalname + "\");");
			}
			TransitionGuard guard = transition.guard();
			if (null != guard) {
				writeTransitionGuard(guard);
				indent = "        ";
			}
			if (null != transition.getActions()) {
				writeActionList(transition.getActions());
			}
			if (!transition.isInternal()) {
				State targetState = machine.getTargetState(transition.id());
				// //
				if (targetState == null) {
					throw new Exception(
							"Could not find target state of transition with ID= "
									+ transition.id() + ".");
				}
				// /
				if (targetState instanceof JunctionState) {
					writeJunctionState((JunctionState) targetState, classname);
				} else if (targetState instanceof FinalState) {
					definitions.println(indent + "this->stop();");
				} else if (targetState instanceof DeepHistoryState) {
					writeToDeepHistory((DeepHistoryState) targetState,
							classname);
				} else {
					String prefix = classname;
					definitions.println(indent + "Q_TRAN(&" + prefix + "::"
							+ targetState.name() + ");");
				}
			}
			if (null != guard) {
				indent = "      ";
				definitions.println(indent + "}");
			}
			definitions.println(indent + "return 0;");
			indent = "  ";
		}
	}

	private void writeToDeepHistory(DeepHistoryState state, String classname)
			throws Exception {
		String historyvariable = "my" + state.getParent().name() + "history";
		definitions.println(indent + "if(" + historyvariable + " == NULL){");
		if (!state.hasOutgoing())
			throw new Exception("History state " + state.name()
					+ " must have an initial outgoing transition.");
		Transition transition = (Transition) state.getOutgoing().get(0);
		State targetState = machine.getTargetState(transition.id());
		definitions.println(indent + "  Q_TRAN(&" + classname + "::"
				+ targetState.name() + ");");
		definitions.println(indent + "}");
		definitions.println(indent + "else{");
		definitions.println(indent + "  Q_TRAN_DYN(" + historyvariable + ");");
		definitions.println(indent + "}");
	}

	private void writeJunctionState(JunctionState js, String classname)
			throws Exception {

		List outgoing = js.getOutgoing();
		if (outgoing.size() != 2) {
			throw new Exception("Junction state " + js.name()
					+ " must have exactly two outgoing transitions.");
		}
		Transition t0 = (Transition) outgoing.get(0);
		Transition t1 = (Transition) outgoing.get(1);
		if ((null == t0.guard() && null == t1.guard()) // Neither transition
														// has
				// a guard
				|| (null != t0.guard() && null != t1.guard())) { // Both
																	// transitions
																	// have
																	// guards
			throw new Exception("Exactly one transition from junction state "
					+ js.name() + " can have a guard.");
		}
		Transition guardTransition;
		Transition otherTransition;
		if (null != t0.guard()) {
			guardTransition = t0;
			otherTransition = t1;
		} else {
			guardTransition = t1;
			otherTransition = t0;
		}
		writeTransitionGuard(guardTransition.guard());
		String saveIndent = indent;
		indent = saveIndent + "  ";
		writeActionList(guardTransition.getActions());
		State targetState = machine.getTargetState(guardTransition.id());
		if (targetState instanceof DeepHistoryState) {
			writeToDeepHistory((DeepHistoryState) targetState, classname);
		} else {
			definitions.println(indent + "Q_TRAN(&" + classname + "::"
					+ targetState.name() + ");");
		}
		indent = saveIndent;
		definitions.println(indent + "}");
		definitions.println(indent + "else {");
		indent = saveIndent + "  ";
		targetState = machine.getTargetState(otherTransition.id());
		if (targetState instanceof DeepHistoryState) {
			writeToDeepHistory((DeepHistoryState) targetState, classname);
		} else {
         writeActionList(otherTransition.getActions());         
			definitions.println(indent + "Q_TRAN(&" + classname + "::"
					+ targetState.name() + ");");
		}
		indent = saveIndent;
		definitions.println(indent + "}");
	}

	/**
	 * Transforms the state machine(s) specified in the <code>args</code> XML
	 * file(s) into C++ implementations based upon the Quantum Framework. Also,
	 * optionally generates the corresponding execution trace (Python) files.
	 * 
	 * @param args
	 *            String[] List of XML files containing state machine
	 *            specifications.
	 */
	public static void main(String[] args) {
		try {
			// Create the UML reader object which is used for the SIM RTC
			// project:
			MagicDrawUmlReader reader = new MagicDrawUmlReader();
			// Parse the XML files:
			reader.parseXmlFiles(args);
			// Create the C++ implementations of the state machines found in the
			// XML files:
			new SimQuantumStateMachineWriter(reader).writeAllStateMachines();
			// Check a system property to see if we should generate the
			// execution
			// trace files:
			if (Autocoder.isExecutionTraceOn()) {
				new ExecutionTracePythonWriter(reader).writeAllStateMachineTraceFiles();
			}
			System.out.println("Finished.");
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/**
	 * @return A list of states. Each state is guaranteed to either have no
	 *         children, or to be the container state of an orthogonal region
	 */

	private List<State> getTerminalStates(StateMachine machine) {
		List<State> terminalstates = new LinkedList<State>();

		for (State state : machine.states()) 
		{
			getTerminalStates(state, terminalstates);
		}
		return terminalstates;
	}

	private List<State> getTerminalStatesForRegion(CompositeStateRegion region) {
		List<State> terminalstates = new LinkedList<State>();

		for (State state : region.getChildren() ) 
		{
			getTerminalStates(state, terminalstates);
		}
		return terminalstates;
	}

	
	private void getTerminalStates(State state, List<State> terminalstates)
	{
		if(state instanceof CompositeState && !(state instanceof ConcurrentCompositeState))
		{
			// don't write composite states themselves
			CompositeState comp = (CompositeState) state;
			for(State child : comp.getChildren()  )
			{
				getTerminalStates(child, terminalstates);
			}
		}
		else if(state instanceof ConcurrentCompositeState)
		{
			terminalstates.add(state);
		}
		else if(state instanceof SimpleState)
		{
			terminalstates.add(state);
		}
	}

	private void writeStateEnumDeclaration()
	{
		List<State> terminals;
		int count = 0;
		
		terminals = getTerminalStates(machine);
		indent = "   ";		
		declarations.println("enum StateEnum" + machine.name() );
		declarations.println("{");
		for(State s : terminals)
		{
		   declarations.print(indent + machine.name().toUpperCase()+ "_"  + s.name().toUpperCase()+ " /* "+ count + "*/" );
		   if(count != terminals.size() -1)
		   {
				   declarations.println(",");
		   }
		   else
		   {
			   declarations.println(" ");			   
		   }
		   
		   count ++;
		}
		declarations.println("};");
		declarations.println();
		indent = "";
	}

	private void writeStateEnumDefinition()
	{
		List<State> terminals;
		int count = 0;
		
		terminals = getTerminalStates(machine);
		definitions.println("char * " + machine.name() + "::" + "getCurrentStateName()");
		definitions.println("{");
		indent = "   ";		
		definitions.println(indent + "switch(mystate)" );
		definitions.println(indent + "{");
		indent = indent + "   ";
		for(State s : terminals)
		{
			definitions.println(indent + "case " + machine.name().toUpperCase()+ "_"  + s.name().toUpperCase() + ":" );
			definitions.println(indent + "   return \""+ s.name() + "\";" );			
   
		   count ++;
		}
		definitions.println(indent + "default:" );
		definitions.println(indent + "   return \"\";" );			
		
		indent = "   ";
		definitions.println(indent + "};");
		definitions.println();
		indent = "";
		definitions.println("}");
	}

	private void writeStateEnumDefinition(CompositeStateRegion region)
	{
		List<State> terminals;
		int count = 0;
		
		terminals = getTerminalStatesForRegion(region);
		definitions.println("char * " + region.name() + "::" + "getCurrentStateName()");
		definitions.println("{");
		indent = "   ";		
		definitions.println(indent + "switch(mystate)" );
		definitions.println(indent + "{");
		indent = indent + "   ";
		for(State s : terminals)
		{
			definitions.println(indent + "case " + region.name().toUpperCase()+ "_"  + s.name().toUpperCase() + ":" );
			definitions.println(indent + "   return \""+ s.name() + "\";" );			
   
		   count ++;
		}
		definitions.println(indent + "default:" );
		definitions.println(indent + "   return \"\";" );			
		
		indent = "   ";
		definitions.println(indent + "};");
		definitions.println();
		indent = "";
		definitions.println("}");
	}

	
	
	private void writeStateEnumDeclarationRegion(CompositeStateRegion region)
	{
		List<State> terminals;
		int count = 0;
		
		indent = "   ";
		terminals = getTerminalStatesForRegion(region);
		
		declarations.println("enum StateEnum" +  region.name() );
		declarations.println("{");
		for(State s : terminals)
		{
		   declarations.print(indent + region.name().toUpperCase()
				   +  "_"+ s.name().toUpperCase()+ " /* "+ count + "*/" );
		   if(count != terminals.size() -1)
		   {
			   declarations.println(",");
		   }
		   else
		   {
			   declarations.println("");
		   }
		   count ++;
		}
		declarations.println("};");
		declarations.println();
		indent = "";
	}
   /** @return can return null if none found */
   private Transition findInitialTransitionToMe(State mestate)
   {
      State parent = mestate.getParent();
      
      if(parent == null)
      {
         return null;
      }
      
      CompositeState composite = (CompositeState) parent;
      InitialState initial = (InitialState) findInitialState(composite.getChildren());
      if(initial == null)
      {
         return null;
      }
      
      for(Transition transition: initial.getOutgoing() )
      {
         State target = machine.getTargetState(transition.id() );
         if(target == mestate)
         {
            return transition;
         }
      }
      return null;
      
   }

}
