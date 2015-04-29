package gov.nasa.jpl.statechart.autocode.cm;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.autocode.cppSIM.SimRtcSignalWriter;
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
 * This class writes out a state machine in the form of C code based on the
 * Quantum Framework model developed by Miro Samek. The code is generated from
 * an internal representation of StateMachine, State, and Transition objects. It
 * also writes an <i>execution trace file</i> which contains Python code used
 * to create an animated GUI display of the various elements of the state
 * machine.
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
 * This software is subject to U.S. export control laws and regulations and 
 * has been classified as 4D993.  By accepting this software, the user agrees 
 * to comply with all applicable U.S. export laws and regulations.  User has 
 * the responsibility to obtain export licenses, or other export authority as 
 * may be required before exporting such information to foreign countries or 
 * providing access to foreign persons.
 * </p>
 * 
 * <p>
 * CVS Identification: $Id: CSimQuantumStateMachineWriter.java,v 1.7 2005/10/11
 * 18:08:09 kclark Exp $
 * </p>
 * 
 * @see StateMachine
 * @see State
 * @see Transition
 * @see DiagramElement
 */
public class CSimQuantumStateMachineWriter
{
   private StateMachine          machine;
   private StateMachineXmiReader reader;
   private PrintStream           declarations;
   private PrintStream           definitions;
   private String                classname;
   private String                implName;
   private String                declarationsFilename;
   private String                definitionsFilename;
   private String                indent           = "";
   private boolean               executionTraceOn = true;

   /**
    * Constructor.
    * 
    * @param reader
    *           StateMachineXmiReader The reader object containing the set of
    *           state machine representations.
    */
   CSimQuantumStateMachineWriter(StateMachineXmiReader reader) throws Exception
   {
      this.reader = reader;
      this.executionTraceOn = Autocoder.isExecutionTraceOn();
   }

   /**
    * Writes out the C implementation files for the set of state machines
    * contained in this instance's reader.
    * 
    * @throws Exception
    */
   public void writeAllStateMachines() throws Exception
   {
      Map<String, StateMachine> machines = reader.getStateMachineMap();
      Iterator i = machines.values().iterator();
      while (i.hasNext())
      {
         StateMachine nextStateMachine = (StateMachine) i.next();
         this.machine = nextStateMachine;
         this.classname = nextStateMachine.name();
         writeStateMachine();
      }
   }

   /**
    * Creates and writes out the Quantum Framework C code which implements the
    * state machine.
    * 
    * @throws Exception
    */
   void writeStateMachine() throws Exception
   {
      classname = machine.name();
      implName = classname + "Impl";
      declarationsFilename = classname + ".h";
      definitionsFilename = classname + ".c";
      declarations = new PrintStream(new FileOutputStream(declarationsFilename));
      definitions = new PrintStream(new FileOutputStream(definitionsFilename));
      System.out.println("Writing statechart " + machine.name() + " to files "
            + declarationsFilename + " and " + definitionsFilename + "...");
      writeDeclarations();
      writeDefinitions();
   }

   /**
    * Creates and writes out the C Quantum Framework header file code for the
    * state machine.
    * 
    * @throws Exception
    */
   void writeDeclarations() throws Exception
   {
      declarations.println("/**");
      declarations.println(" * @file " + declarationsFilename);
      declarations.println(" *");
      declarations
            .println(" * This file was generated by the SIM MagicDraw statechart converter,");
      declarations
            .println(" * which converts MagicDraw statecharts expressed in XML to Miro Samek's");
      declarations.println(" * C Quantum Framework.");
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
      declarations.println("");
      // Forward declaration of impl class:
      declarations.println("#include \"" + classname + "Impl.h\"");
      declarations.println("");
      
      writeStateEnumDeclaration();
      // Check if we're doing  regions for this state machine:
      indent = "  ";
      Iterator newstateIter = machine.states().iterator();
      while (newstateIter.hasNext())
      {
         State state = (State) newstateIter.next();
         if (state instanceof CompositeState)
         {
            for (CompositeStateRegion s : ((CompositeState) state).getAllSubRegions())
            {
               writeStateEnumDeclarationRegion( s);               
               writeOneRegionDeclaration( s);
            }
         }
      }
      // Write QActive class declaration:
      declarations.println("SUBCLASS(" + classname + " , QActive)");
      declarations.println("");
      indent = "  ";
      declarations.println(indent + "/*private:*/");
      indent = "    ";
      if(executionTraceOn)
      {
         declarations.println(indent + "char objName[256];");
      }
      declarations.println(indent + implName + "* impl;");
      Iterator stateIter = machine.states().iterator();
      while (stateIter.hasNext())
      {
         State state = (State) stateIter.next();
         if (state instanceof CompositeState)
         {
            for ( State s : ((CompositeState) state).getAllSubRegions() )
            {
               declarations.println("    " + s.name() + " * "
                     + s.name().toLowerCase() + ";");
            }
         }
      }
      /*
       * newstateIter = machine.states().iterator(); while
       * (newstateIter.hasNext()) { State state = (State)newstateIter.next();
       * if(state instanceof CompositeState) { Set<State> concurrent =
       * ((CompositeState)state).getAllSubRegions(); for(State s : concurrent) {
       * String name = s.name(); declarations.println(indent + " " +
       * name.toLowerCase() + " = new " + s.name() + "(objName, implPtr);"); } } }
       */
      // for each composite state with a history, write a history variable
      List<CompositeState> compositeswithhistory2 = getHistoryContainers(machine
            .states());
      for (CompositeState mycomposite : compositeswithhistory2)
      {
         declarations.println(indent + "QState my" + mycomposite.name()
               + "history;");
      }
      
      declarations.println(indent + "enum StateEnum" + machine.name() + " mystate;");      
      declarations.println("");
      declarations.println("METHODS");
      indent = "  ";
      indent = "    ";
      declarations.println(indent + classname + " * " + classname
            + "_Constructor(" + classname + " * me, char* objNameNew, "
            + implName + "* implPtr);");
      // Remove the "protected" keyword below so that the state method
      // declarations
      // will be public. This is because the *Impl class, which is not derived
      // from this class, needs to be able to reference the address of the
      // member
      // functions.
      // indent = " ";
      // declarations.println(indent + "protected:");
      // indent = " ";
      declarations.println(indent + "void " + classname + "_initial("
            + classname + " * me, QEvent const* e);");
      stateIter = machine.states().iterator();
      while (stateIter.hasNext())
      {
         State state = (State) stateIter.next();
         // if (null == stateWithRegions || state != stateWithRegions) {
         if (!(state instanceof ConcurrentCompositeState))
         {
            writeMethodPrototypesForSubtree(state, classname);
         } else
         {
            writeOneMethodPrototype(state, classname);
         }
      }
      declarations.println("");
      declarations.println("END_CLASS");
      declarations.println("#endif /* " + mnemonic + "*/");
      indent = "";
   }

   private List<CompositeState> getHistoryContainers(List<State> states)
   {
      List<CompositeState> historystates = new ArrayList<CompositeState>();
      for (State s : states)
      {
         if (s instanceof CompositeState)
         {
            CompositeState composite = (CompositeState) s;
            if (composite.containsHistoryState())
            {
               historystates.add(composite);
            }
            if (!(s instanceof ConcurrentCompositeState))
            {
               historystates.addAll(getHistoryContainers(composite
                     .getChildren()));
            }
         }
      }
      return historystates;
   }

   void writeOneRegionDeclaration(CompositeStateRegion region) throws Exception
   {
      declarations.println("SUBCLASS(" + region.name() + ", QHsm)");
      indent = "    ";
      if(executionTraceOn)
      {
         declarations.println(indent + "char objName[256];");
      }
      declarations.println(indent + implName + "* impl;");
      List<CompositeState> compositeswithhistory = getHistoryContainers(region
            .getChildren());
      for (State compwithistory : compositeswithhistory)
      {
         declarations.println(indent + "QState my" + compwithistory.name()
               + "history;");
      }
      declarations.println(indent + "enum StateEnum" + region.name() + " mystate;" );
      indent = "  ";
      indent = "    ";
      declarations.println("METHODS");
      declarations.println(indent + "void " + region.name() + "_initial("
            + region.name() + " * me, QEvent const* e);");
      declarations.println(indent + region.name() + " * " + region.name()
            + "_Constructor(" + region.name() + " * me, char* objNameNew, "
            + implName + "* implPtr);");
      declarations.println(indent + "void " + "initial(QEvent const* e);");
      Iterator childIter = region.getChildren().iterator();
      while (childIter.hasNext())
      {
         writeMethodPrototypesForSubtree((State) childIter.next(), region
               .name());
      }
      declarations.println("END_CLASS");
      declarations.println("");
      indent = "  ";
      indent = "";
   }

   void writeMethodPrototypesForSubtree(State state, String classname)
         throws Exception
   {
      writeOneMethodPrototype(state, classname);
      if (!(state instanceof ConcurrentCompositeState))
      {
         if (state instanceof CompositeState)
         {
            String saveIndent = indent;
            indent = indent + "  ";
            Iterator child = ((CompositeState) state).getChildren().iterator();
            while (child.hasNext())
            {
               State childState = (State) child.next();
               writeMethodPrototypesForSubtree(childState, classname);
            }
            indent = saveIndent;
         }
      }
   }

   /**
    * Writes the corresponding method prototype for the specified state.
    * 
    * @param s
    *           State Starting state.
    * @throws Exception
    */
   void writeOneMethodPrototype(State s, String classname) throws Exception
   {
      // If this is simple or composite, it needs a method.
      if (s instanceof SimpleState || s instanceof CompositeState)
      {
         declarations.println(indent + "QSTATE " + classname + "_" + s.name()
               + "(" + classname
               + " * me, QEvent const *e);\t\t/*state handler*/");
      }
   }

   /**
    * Creates and writes out the C Quantum Framework body file code for the
    * state machine.
    * 
    * @throws Exception
    */
   void writeDefinitions() throws Exception
   {
      definitions.println("/**");
      definitions.println(" * @file " + definitionsFilename);
      definitions.println(" *");
      definitions
            .println(" * This file was generated by the SIM MagicDraw statechart converter,");
      definitions
            .println(" * which converts MagicDraw statecharts expressed in XML to Miro Samek's");
      definitions.println(" * C/C Quantum Framework.");
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
      definitions.println("#include \"" + classname + "Impl" + ".h\"");
      if (executionTraceOn)
      {
         definitions.println("#include \"log_event.h\"");
      }
      definitions.println("");
      // constructor
      definitions.println(indent + classname + " * " + classname
            + "_Constructor(" + classname + " * me, char* objNameNew, "
            + implName + "* implPtr)");
      definitions.println(indent + "{");
      definitions.println(indent
            + "   QActiveCtor_(&me->super_, (QPseudoState)" + classname
            + "_initial);");
      if(executionTraceOn)
      {
         definitions.println(indent + "   strcpy(me->objName, objNameNew);");
      }
      definitions.println(indent + "   me->impl = implPtr;");
      // allocate the child machines
      Iterator newstateIter = machine.states().iterator();
      while (newstateIter.hasNext())
      {
         State state = (State) newstateIter.next();
         if (state instanceof CompositeState)
         {
            for (State s :((CompositeState) state).getAllSubRegions())
            {
               definitions.println("   me->" + s.name().toLowerCase() + "= ("
                     + s.name() + " *) malloc( sizeof(" + s.name() + ") );");
               definitions.println("   " + s.name() + "_Constructor( me->"
                     + s.name().toLowerCase() + ", objNameNew, implPtr);");
            }
         }
      }
      List<CompositeState> compositeswithhistory = getHistoryContainers(machine
            .states());
      for (State compwithistory : compositeswithhistory)
      {
         definitions.println(indent + "   me->my" + compwithistory.name()
               + "history = NULL;");
      }
      definitions.println(indent + "   return me;");
      definitions.println(indent + "}");
      // Write initial method for state machine:
      writeQActiveInitialMethod();
      // Get any other top-level states.
      Iterator stateIter = machine.states().iterator();
      while (stateIter.hasNext())
      {
         State state = (State) stateIter.next();
         // if (null == stateWithRegions || state != stateWithRegions) {
         writeMethodBodiesForSubtree(null, this.classname, state);
         // }
      }
   }

   private void writeInitialMethod(CompositeStateRegion region)
         throws Exception
   {
      definitions.println("");
      definitions.println("void " + region.name() + "_initial(" + region.name()
            + " * me,  QEvent const *e) {");
      // Find the initial state for this region:
      List<State> statelist = new ArrayList<State>();
      statelist.add(region);
      State initial = findInitialState(statelist);
      if (null == initial)
         throw new Exception("Composite state region " + region.name()
               + " must specify an initial substate.");
      State initialState = machine.getTargetState(((Transition) initial
            .getOutgoing().get(0)).id());
      definitions.println(indent + "  Q_INIT(&" + region.name() + "_"
            + initialState.name() + ");");
      definitions.println("}");
      definitions.println("");
   }

   private void writeQHSMConstructor(CompositeStateRegion region)
   {
      definitions.println("");
      definitions.println(indent + region.name() + " * " + region.name()
            + "_Constructor(" + region.name() + " * me, char* objNameNew, "
            + implName + "* implPtr)");
      definitions.println(indent + "{");
      definitions.println(indent + "   QHsmCtor_(&me->super_, (QPseudoState)"
            + region.name() + "_initial);");
      if(executionTraceOn)
      {
         definitions.println(indent + "   strcpy(me->objName, objNameNew);");
      }
      definitions.println(indent + "   me->impl = implPtr;");
      List<CompositeState> containers = getHistoryContainers(region
            .getChildren());
      for (CompositeState composite : containers)
      {
         definitions
               .println("   me->my" + composite.name() + "history = NULL;");
      }
      definitions.println(indent + "   return me;");
      definitions.println(indent + "}");
   }

   void writeQActiveInitialMethod() throws Exception
   {
      Set<String> subscribeset = new HashSet<String>();
      definitions.println("");
      definitions.println("void " + classname + "_initial(" + classname
            + " * me,  QEvent const *e) {");
      indent = "   ";
      
      // write any actions on the state machine's initial transition
      State state = findInitialState(machine.states());
      if (null == state || state.getOutgoing().size() != 1)
      {
         throw new Exception("Invalid state machine " + machine.name()
               + ": Initial pseudostate either not found or invalid.");
      }
      InitialState startstate = (InitialState) state;
      Transition initialtransition = startstate.getOutgoing().get(0);
      writeActionList(initialtransition.getActions() );
      
      
      // Subscribe to list of global events referenced by this statechart:
      Iterator tranIter = machine.transitions().iterator();
      while (tranIter.hasNext())
      {
         Transition tran = (Transition) tranIter.next();
         if (!tran.isLocalEvent() && null != tran.signalName())
         {
            for (String eventname : tran.getSignalNames())
            {
               subscribeset.add(eventname);
            }
         }
      }
      for (String name : subscribeset)
      {
         definitions.println(indent + "QFsubscribe(& me->super_, " + name
               + ");");
      }
      // subscribe to default events
      definitions.println(indent + "QFsubscribe(& me->super_, " + "Q_EVAL"
            + ");");
      definitions.println(indent + "QFsubscribe(& me->super_, " + "Q_DURING"
            + ");");
      // Check if we're doing regions:
      // Now initialize this state machine to an initial state:
      definitions.print(indent + "Q_INIT(" + classname + "_");
      /**
       * Find the outermost initial type pseudostate for this machine, find its
       * outgoing transition, and then the actual initial state which is the
       * target of the transition. This is the state we want to Q_INIT to...
       */
      State initialState = machine.getTargetState(((Transition) state
            .getOutgoing().get(0)).id());
      definitions.println(initialState.name() + ");");
      definitions.println("}");
      definitions.println("");
      indent = "";
   }

   /**
    * Does a breath-first search of the state tree looking for the first initial
    * state.
    * 
    * @param stateTree
    *           Collection
    * @return State Found intial state, or <B>null</B> if not found in tree.
    */
   State findInitialState(Collection stateTree)
   {
      Iterator stateIter = stateTree.iterator();
      while (stateIter.hasNext())
      {
         State s = (State) stateIter.next();
         if (s instanceof InitialState)
         {
            return s;
         }
      }
      // Not found at this level, so search subtree(s):
      stateIter = stateTree.iterator();
      while (stateIter.hasNext())
      {
         State s = (State) stateIter.next();
         if (s instanceof CompositeState)
         {
            Collection c = ((CompositeState) s).getChildren();
            if (null != c)
            {
               State s2 = findInitialState(c);
               if (null != s2)
               {
                  return s2;
               }
            }
         }
      }
      // Initial state not found...
      return null;
   }

   void writeOneRegionDefinition(CompositeStateRegion region) throws Exception
   {
      writeQHSMConstructor(region);
      writeInitialMethod(region);
      // Get any other top-level states.
      Iterator stateIter = region.getChildren().iterator();
      while (stateIter.hasNext())
      {
         State state = (State) stateIter.next();
         // if (null == stateWithRegions || state != stateWithRegions) {
         writeMethodBodiesForSubtree(null, region.name(), state);
         // }
      }
   }

   /**
    * Recursively creates and writes out state method bodies for the subtree
    * starting at the specified state.
    * 
    * @param parent
    *           State Parent of starting state.
    * @param state
    *           State Starting state.
    * @throws Exception
    */
   void writeMethodBodiesForSubtree(State parent, String classname, State state)
         throws Exception
   {
      if (state instanceof CompositeStateRegion)
      {
         writeOneRegionDefinition((CompositeStateRegion) state);
      } else
      {
         writeOneMethodBody(parent, classname, state);
         if (state instanceof CompositeState)
         {
            Iterator child = ((CompositeState) state).getChildren().iterator();
            while (child.hasNext())
            {
               State childState = (State) child.next();
               writeMethodBodiesForSubtree(state, classname, childState);
            }
         }
      }
   }

   /**
    * Creates and writes out the Quantum Framework C code which implements the
    * specified state.
    * 
    * @param parentState
    *           State Parent of state to implement.
    * @param state
    *           State State to implement.
    * @throws Exception
    */
   void writeOneMethodBody(State parentState, String classname, State state)
         throws Exception
   {
      String timeouttext = "";
      
      if (state instanceof InitialState || state instanceof FinalState
            || state instanceof JunctionState
            || state instanceof DeepHistoryState)
      {
         // Nothing to do for these states...
         return;
      }
      List outList = ((SimpleState) state).getOutgoing();
      definitions.println("QSTATE " + classname + "_" + state.name() + "("
            + classname + " * me, QEvent const *e) {");
      indent = "  ";
      if (executionTraceOn)
      {
         definitions.println(indent + "char stateName[256];");
         definitions.println();
      }
      if (((SimpleState) state).hasTimeout())
      {
         Iterator i = outList.iterator();
         while (i.hasNext())
         {
            Transition t = (Transition) i.next();
            if (null != t.timeout())
            {
               timeouttext = t.timeout().getTimeout();
               // definitions.println(indent + "static MyTimer timer = {"+ t.timeout().getTimeout() +  ", 0};\n");
               // variable may be not a constant, so just initialize to zero, and set it on entry to a state
               definitions.println(indent + "static MyTimer timer = {0, 0};\n");
            }
         }
      }
      
      if (executionTraceOn)
      {
         definitions.println(indent + "strcpy(stateName, me->objName);");
         definitions.println(indent + "strcat(stateName, \" " + state.name()
               + "\");");
      }
      
      
      
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
         definitions.println(indent + "me->mystate = " + classname.toUpperCase() 
            + "_" + state.name().toUpperCase() +   ";");
      }
      
      
      if (executionTraceOn)
      {
         definitions.println(indent + "strcat(stateName, \" ENTRY\");");
         definitions.println(indent + "LogEvent_log(stateName);");
      }
      if (((SimpleState) state).hasTimeout())
      {
         definitions.println(indent + "timer.timeout = " + timeouttext + ";");
         definitions.println(indent + "MyTimer_startTimer(&timer);");
      }
      if (state instanceof ConcurrentCompositeState)
      {
         List<State> kids = ((ConcurrentCompositeState) state).getChildren();
         for (State kidstate : kids)
         {
            definitions.println(indent + "QHsmInit( &(me->"
                  + kidstate.name().toLowerCase() + "->super_), e);");
         }
      }
      if (null != state.getEntryActions())
      {
         writeActionList(state.getEntryActions());
      }
      definitions.println(indent + "return 0;");
      if (!state.getDuringActions().isEmpty()
            || state instanceof ConcurrentCompositeState)
      {
         // Generate "During" code:
         indent = "    ";
         definitions.println(indent + "case Q_DURING:");
         indent = "      ";
         writeActionList(state.getDuringActions());
         if (state instanceof ConcurrentCompositeState)
         {
            List<State> kids = ((ConcurrentCompositeState) state).getChildren();
            for (State kidstate : kids)
            {
               definitions.println(indent + "    QHsmDispatch( &(me->"
                     + kidstate.name().toLowerCase() + "->super_), e);");
            }
         }
         definitions.println(indent + "return 0;");
      }
      // Generate Exit condition code:
      indent = "    ";
      definitions.println(indent + "case Q_EXIT_SIG:");
      indent = "      ";
      if (executionTraceOn)
      {
         definitions.println(indent + "strcat(stateName, \" EXIT\");");
         definitions.println(indent + "LogEvent_log(stateName);");
      }
      if (null != state.getExitActions())
      {
         writeActionList(state.getExitActions());
      }
      if (state instanceof ConcurrentCompositeState)
      {
         List<State> kids = ((ConcurrentCompositeState) state).getChildren();
         for (State kidstate : kids)
         {
            /*
             * definitions.println(indent + kidstate.name().toLowerCase() +
             * "->Clear( (QPseudoState) &" + kidstate.name() + "::initial);");
             */
            definitions.println(indent + "QHsmClear( &(me->"
                  + kidstate.name().toLowerCase()
                  + "->super_)    ,  (QPseudoState) " + kidstate.name()
                  + "_initial);");
         }
      }
      if (state instanceof CompositeState)
      {
         CompositeState mycomposite = (CompositeState) state;
         if (mycomposite.containsHistoryState())
         {
            // call getstate
            if (classname.equals(this.classname)) // it's a qactive
            {
               definitions.println(indent + "me->my" + state.name()
                     + "history = QHsmGetState_(&(me->super_.super_) );");
            } else
            // its a qhsm
            {
               definitions.println(indent + "me->my" + state.name()
                     + "history = QHsmGetState_(&(me->super_) );");
            }
         }
      }
      definitions.println(indent + "return 0;");
      boolean hasGuardOrTimeout = ((SimpleState) state).hasGuard()
            || ((SimpleState) state).hasTimeout()
            || state instanceof ConcurrentCompositeState;
      if (hasGuardOrTimeout)
      {
         // Generate code for transition guard evaluations:
         indent = "    ";
         definitions.println(indent + "case Q_EVAL:");
         indent = "      ";
         Iterator i = outList.iterator();
         while (i.hasNext())
         {
            Transition t = (Transition) i.next();
            if (null != t.guard() && null == t.signalName())
            {
               writeTransitionGuard(t.guard());
               indent = "        ";
               writeActionList(t.getActions());
               State targetstate = machine.getTargetState(t.id());
               if (targetstate instanceof DeepHistoryState)
               {
                  writeToDeepHistory((DeepHistoryState) targetstate, classname);
               }
               else if (targetstate instanceof JunctionState)
               {
                  writeJunctionState((JunctionState) targetstate, classname);
               }
               else
               {
                  definitions.println(indent + "Q_TRAN(" + classname + "_"
                        + machine.getTargetState(t.id()).name() + ");");
               }
               definitions.println(indent + "break;");
               indent = "      ";
               definitions.println(indent + "}");
            } else if (null != t.timeout())
            {
               if (null != t.signalName())
               {
                  throw new Exception("Invalid transition " + t.signalName()
                        + " can't have both trigger event and timeout.");
               }
               definitions.println(indent + "if (MyTimer_timedOut(&timer)) {" + "");
               indent = "        ";
               writeActionList(t.getActions());
               State targetstate = machine.getTargetState(t.id());
               if (targetstate instanceof DeepHistoryState)
               {
                  writeToDeepHistory((DeepHistoryState) targetstate, classname);
               }
               else if (targetstate instanceof JunctionState)
               {
                  writeJunctionState((JunctionState) targetstate, classname);
               }
               else
               {
                  definitions.println(indent + "Q_TRAN(" + classname + "_"
                        + machine.getTargetState(t.id()).name() + ");");
               }
               definitions.println(indent + "break;");
               indent = "      ";
               definitions.println(indent + "}");
            }
         }
         // Check if we have subregions which need to process the Q_EVAL event:
         if (state instanceof ConcurrentCompositeState)
         {
            List<State> kids = ((ConcurrentCompositeState) state).getChildren();
            for (State kidstate : kids)
            {
               definitions.println(indent + "    QHsmDispatch( &(me->"
                     + kidstate.name().toLowerCase() + "->super_), e);");
            }
         }
         definitions.println(indent + "break;");
      }
      indent = "    ";
      /**
       * Now check for child states which we need to Q_INIT (initialize) to:
       */
      if (state instanceof CompositeState
            && !(state instanceof ConcurrentCompositeState))
      {
         Iterator child = ((CompositeState) state).getChildren().iterator();
         Transition itran = null;
         while (child.hasNext())
         {
            State childState = (State) child.next();
            if (childState instanceof InitialState)
            {
               itran = (Transition) ((InitialState) childState).getOutgoing()
                     .get(0);
               break;
            }
         }
         if (null == itran)
         {
            throw new Exception("Composite state " + state.name()
                  + " must specify an initial substate.");
         } else
         {
            // Now find the child consumer of this event.
            child = ((CompositeState) state).getChildren().iterator();
            while (child.hasNext())
            {
               State childState = (State) child.next();
               if (childState.hasIncoming(itran.id()))
               {
                  // Must handle the Q_INIT_SIG:
                  definitions.println(indent + "case Q_INIT_SIG:");
                  indent = "      ";
                  definitions.println(indent + "Q_INIT(" + classname + "_"
                        + childState.name() + ");");
                  definitions.println(indent + "return 0;");
                  indent = "    ";
                  break;
               }
            }
         }
      }
      // Need a case branch for each out-going transition.
      List outgoings = null;
      if (state instanceof CompositeState)
      {
         outgoings = ((CompositeState) state).getOutgoing();
      } else if (state instanceof SimpleState)
      {
         outgoings = ((SimpleState) state).getOutgoing();
      }
      if (null != outgoings)
      {
         Iterator out = outgoings.iterator();
         while (out.hasNext())
         {
            Transition t = (Transition) out.next();
            if (null != t.signalName())
            {
               writeSwitchCase(t, classname);
            }
         }
      }
      // Handle dispatch to child subregions:
      if (state instanceof ConcurrentCompositeState)
      {
         ConcurrentCompositeState concurrent = (ConcurrentCompositeState) state;
         List<State> kids = concurrent.getChildren();
         Set<String> alldesiredevents = new HashSet<String>();
         for (State kidstate : kids)
         {
            Set<String> desiredevents = kidstate.getAllDesiredEvents();
            alldesiredevents.addAll(desiredevents);
         }
         if (!alldesiredevents.isEmpty())
         {
            for (String name : alldesiredevents)
            {
               if (name != null)
               {
                  definitions.println(indent + "  case " + name + ":");
               }
            }
            for (State kidstate : kids)
            {
               definitions.println(indent + "    QHsmDispatch( &(me->"
                     + kidstate.name().toLowerCase() + "->super_), e);");
            }
            definitions.println(indent + "    return 0;");
         }
      }
      // Finish up this method body:
      indent = "  ";
      definitions.println(indent + "}");
      /*
       * definitions.println(indent + "return (QSTATE)" + classname + ((null ==
       * parentState) ? "_top" : ("_" + parentState.name())) + ";");
       */
      if (parentState == null)
      {
         definitions.println(indent + "return (QSTATE)" + "QHsm_top" + ";");
      } else
      {
         definitions.println(indent + "return (QSTATE)" + classname
               + ("_" + parentState.name()) + ";");
      }
      indent = "";
      definitions.println(indent + "}");
      definitions.println("");
   }

   void writeActionList(List actions)
   {
      Iterator i = actions.iterator();
      while (i.hasNext())
      {
         Action action = (Action) i.next();
         writeAction(action);
      }
   }

   void writeAction(Action action)
   {
      if (action instanceof EventAction)
      {
         definitions.println(indent + "QFpublish( Q_NEW(QEvent, " + action.name()
               + ") );");
      } 
      else if (action instanceof CallAction)
      {
         if(action.name().contains("\""))
         {
            String noquotes = action.name().replace('\"', ' ');
            definitions.println(indent + noquotes);                     
         }
         else
         {
            String tempname = action.name().replaceAll("\\)", "");
            String [] split = action.name().split("\\(|\\)");
         
         
            if(split.length > 1)
            {
               String callname  = action.name().split("\\(|\\)")[0].trim();
               String params  = action.name().split("\\(|\\)")[1].trim();
        	
               if(params.length() > 0)
               {
                  System.out.println(callname);
                  definitions.println(indent + classname + "Impl_" + callname        			 
                        + "(me->impl," + params + ")" + ";");
               }
               else
               {
                  definitions.println(indent + classname + "Impl_" + tempname
                        + "me->impl)" + ";");            	             	 
               }        	 
            }
            else
            {
               definitions.println(indent + classname + "Impl_" + tempname
                     + "me->impl)" + ";");
            }
         }
      }
   }

   /**
    * Writes out code for a transition guard. Note below that this method leaves
    * opens the "if" code block, so the caller must close it by writing the
    * closing curly brace.
    * 
    * @param tg
    *           TransitionGuard
    */
   void writeTransitionGuard(TransitionGuard tg)
   {
      definitions.print(indent + "if (");
      
      if(tg.methodName().contains("\""))
      {
         String noquotes = tg.methodName().replace('\"', ' ');
         definitions.println(noquotes + ")  {");         
      }
      else
      {
      
         if (tg.notOp())
         {
            definitions.print("!");
         }
         // String tempmethodname = tg.methodName().replaceFirst("\\)", " ");
         // definitions.println(classname + "Impl_" + tempmethodname
         //       + "me->impl) ) {");
         // //
         // //System.out.println( "int " + classname + "Impl_" + tempmethodname +
         // classname + "Impl * impl, ...)\n{\n}\n");
         // ////////////////////////////////////////
         String tempname = tg.methodName().replaceAll("\\)", "");
         String [] split = tg.methodName().split("\\(|\\)");
      
      
         if(split.length > 1)
         {
            String callname  = tg.methodName().split("\\(|\\)")[0].trim();
            String params  = tg.methodName().split("\\(|\\)")[1].trim();
     	
            if(params.length() > 0)
            {
               System.out.println(callname);
               definitions.println( classname + "Impl_" + callname        			 
                     + "(me->impl," + params + ")) {" );
            }
            else
            {
               definitions.println( classname + "Impl_" + tempname
                     + "me->impl)) {");            	             	 
            }        	 
         }
         else
         {
            definitions.println(classname + "Impl_" + tempname
                  + "me->impl)) {" );
         }
      }
      
   }

   void writeSwitchCase(Transition transition, String classname)
         throws Exception
   {
      for (String signalname : transition.getSignalNames())
      {
         indent = "    ";
         definitions.println(indent + "case " + signalname + ":");
         indent = "      ";
         if (executionTraceOn)
         {
            definitions.println(indent + "strcat(stateName, \" " + signalname
                  + "\");");
            definitions.println(indent + "LogEvent_log(stateName);");
         }
         TransitionGuard guard = transition.guard();
         if (null != guard)
         {
            writeTransitionGuard(guard);
            indent = "        ";
         }
         if (null != transition.getActions())
         {
            writeActionList(transition.getActions());
         }
         if (!transition.isInternal())
         {
            State targetState = machine.getTargetState(transition.id());
            if (targetState instanceof JunctionState)
            {
               writeJunctionState((JunctionState) targetState, classname);
            } else if (targetState instanceof FinalState)
            {
               definitions.println(indent + "this->stop();");
            } else if (targetState instanceof DeepHistoryState)
            {
               writeToDeepHistory((DeepHistoryState) targetState, classname);
            } else
            {
               String prefix = classname;
               definitions.println(indent + "Q_TRAN(" + prefix + "_"
                     + targetState.name() + ");");
            }
         }
         if (null != guard)
         {
            indent = "      ";
            definitions.println(indent + "}");
         }
         definitions.println(indent + "return 0;");
         indent = "  ";
      }
   }

   private void writeToDeepHistory(DeepHistoryState state, String classname)
         throws Exception
   {
      String historyvariable = "my" + state.getParent().name() + "history";
      definitions.println(indent + "if(me->" + historyvariable + " == NULL){");
      if (!state.hasOutgoing())
         throw new Exception("History state " + state.name()
               + " must have an initial outgoing transition.");
      Transition transition = (Transition) state.getOutgoing().get(0);
      State targetState = machine.getTargetState(transition.id());
      definitions.println(indent + "  Q_TRAN(" + classname + "_"
            + targetState.name() + ");");
      definitions.println(indent + "}");
      definitions.println(indent + "else{");
      definitions
            .println(indent + "  Q_TRAN_DYN(me->" + historyvariable + ");");
      definitions.println(indent + "}");
   }

   void writeJunctionState(JunctionState js, String classname) throws Exception
   {
      List outgoing = js.getOutgoing();
      if (outgoing.size() != 2)
      {
         throw new Exception("Junction state " + js.name()
               + " must have exactly two outgoing transitions.");
      }
      Transition t0 = (Transition) outgoing.get(0);
      Transition t1 = (Transition) outgoing.get(1);
      if ((null == t0.guard() && null == t1.guard()) // Neither transition has
            // a guard
            || (null != t0.guard() && null != t1.guard()))
      { // Both transitions have guards
         throw new Exception("Exactly one transition from junction state "
               + js.name() + " can have a guard.");
      }
      Transition guardTransition;
      Transition otherTransition;
      if (null != t0.guard())
      {
         guardTransition = t0;
         otherTransition = t1;
      } else
      {
         guardTransition = t1;
         otherTransition = t0;
      }
      writeTransitionGuard(guardTransition.guard());
      String saveIndent = indent;
      indent = saveIndent + "  ";
      writeActionList(guardTransition.getActions());
      State targetState = machine.getTargetState(guardTransition.id());
      if (targetState instanceof DeepHistoryState)
      {
         writeToDeepHistory((DeepHistoryState) targetState, classname);
      } else
      {
         definitions.println(indent + "Q_TRAN(" + classname + "_"
               + targetState.name() + ");");
      }
      indent = saveIndent;
      definitions.println(indent + "}");
      definitions.println(indent + "else {");
      indent = saveIndent + "  ";
      targetState = machine.getTargetState(otherTransition.id());
      if (targetState instanceof DeepHistoryState)
      {
         writeToDeepHistory((DeepHistoryState) targetState, classname);
      } else
      {
         definitions.println(indent + "Q_TRAN(" + classname + "_"
               + targetState.name() + ");");
      }
      indent = saveIndent;
      definitions.println(indent + "}");
   }

   /**
    * Creates and writes out the machine's states - including all child states
    * of the specified state list.
    * 
    * @param stateList
    *           List The list of states.
    * @throws Exception
    */
   void writeStates(List stateList) throws Exception
   {
      Iterator stateIter = stateList.iterator();
      while (stateIter.hasNext())
      {
         State state = (State) stateIter.next();
         if (state instanceof CompositeState)
         {
            writeStates(((CompositeState) state).getChildren());
         }
      }
   }

   /**
    * Transforms the state machine(s) specified in the <code>args</code> XML
    * file(s) into C implementations based upon the Quantum Framework. Also,
    * optionally generates the corresponding execution trace (Python) files.
    * 
    * @param args
    *           String[] List of XML files containing state machine
    *           specifications.
    */
   public static void main(String[] args)
   {
      try
      {
         System.setProperty("jpl.autocode.c", "true");
         // Create the UML reader object which is used for the SIM RTC project:
         MagicDrawUmlReader reader = new MagicDrawUmlReader();
         // Parse the XML files:
         reader.parseXmlFiles(args);
         // Create the C implementations of the state machines found in the
         // XML files:
         new CSimQuantumStateMachineWriter(reader).writeAllStateMachines();
         // Check a system property to see if we should generate the execution
         // trace files:
         if (Autocoder.isExecutionTraceOn())
         {
            new ExecutionTracePythonWriter(reader).writeAllStateMachineTraceFiles();
         }
         System.out.println("Finished.");
      } catch (Exception e)
      {
         e.printStackTrace(System.err);
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

}
