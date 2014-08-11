/**
 * Class that implements a state chart in the C language following Miro
 * Samek's Quantum Framework
 */
package gov.nasa.jpl.statechart.autocode.cm;

import gov.nasa.jpl.statechart.autocode.StateChartCodeWriter;
import gov.nasa.jpl.statechart.autocode.cppSIM.SimRtcSignalWriter;
import gov.nasa.jpl.statechart.core.Action;
import gov.nasa.jpl.statechart.core.CallAction;
import gov.nasa.jpl.statechart.core.CompositeState;
import gov.nasa.jpl.statechart.core.CompositeStateRegion;
import gov.nasa.jpl.statechart.core.ConcurrentCompositeState;
import gov.nasa.jpl.statechart.core.ConnectionPointReference;
import gov.nasa.jpl.statechart.core.DeepHistoryState;
import gov.nasa.jpl.statechart.core.EntryPoint;
import gov.nasa.jpl.statechart.core.EventAction;
import gov.nasa.jpl.statechart.core.ExitPoint;
import gov.nasa.jpl.statechart.core.FinalState;
import gov.nasa.jpl.statechart.core.InitialState;
import gov.nasa.jpl.statechart.core.JunctionState;
import gov.nasa.jpl.statechart.core.SimpleState;
import gov.nasa.jpl.statechart.core.State;
import gov.nasa.jpl.statechart.core.StateMachine;
import gov.nasa.jpl.statechart.core.SubmachineState;
import gov.nasa.jpl.statechart.core.Transition;
import gov.nasa.jpl.statechart.core.TransitionGuard;
import gov.nasa.jpl.statechart.input.StateMachineXmiReader;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Node;


public class StateChartCWriter extends StateChartCodeWriter
{ 
   // Holds the implementation class name.  This is usually the name of
   // the top-level state chart
   protected final String classname;

   // Holds the type of the "me" variable in the CQuantum framework
   protected final String meType;

   // Process the whole state machine and maintain a map of qualified
   // signal names.  This keeps signals in their own namespace.
   protected Map<String, String> signals = null;

   // A private variable
   private StateMachineXmiReader reader = CQuantum3_2StateMachineWriter.reader;

   
   public StateChartCWriter( StateMachine stateMachine, String classname, String meType )
   {
      super( stateMachine );

      this.classname = classname;
      this.meType    = meType;
      this.signals   = getSignalNamesAndComments();
   }
   
   public StateChartCWriter( StateMachine stateMachine, String classname )
   {
      this( stateMachine, classname, classname );
   }
   
   public StateChartCWriter( StateMachine stateMachine )
   {
      this( stateMachine, stateMachine.name() );      
   }

   /**
    * Top level subroutine that generated the code to implement the
    * state chart.  There are separate methods for generating each piece
    * of functionality.
    *
    * @see writeCommentBlock
    * @see writeIncludes
    * @see writeStateMachineConstructor
    * @see writeQActiveInitialMethod
    */
   @Override
   public void writeCode( String filename )
   {
      this.currentFilename = filename;
        
      try
      {
         out = new PrintStream( new FileOutputStream( filename ) );
       
         writeCommentBlock();
         writeIncludes();
         writeStateMachineConstructor();
         writeQActiveInitialMethod();
            
         // Get all the states in the tree, but only generate code for
         // the SimpleStates.  This excludes any code generation for 
         // pseudo-states like Junctions, Initial States, Connection
         // Point Refereneces, etc.
            
         Map<String, SimpleState> states = 
            filterByType( stateMachine.getAllStates( getClassPrefix() ), 
                          SimpleState.class );

         // Sort the paths to each state.  This has the benefit of
         // generating the code according to a depth-first search
            
         Set<String> sortedPaths =
            new TreeSet<String>( states.keySet() );

         // Generate the code for all the state
         for ( String path : sortedPaths )
         {
            SimpleState state = states.get( path );

            // If this is an orthogonal region, generate different code
            if ( state instanceof CompositeStateRegion )
            {
               writeOneRegionDefinition( path, (CompositeStateRegion) state );
            }
            else
            {                  
               writeOneMethodBody( path, state );
            }
         }

         writeSubmachineCIncludes();

         out.flush();
         out.close();
         out = null;
      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }

      this.currentFilename = "";
   }

   protected void writeIncludes()
   {
      out.println();
      out.println( indent + "#include <stdlib.h>" );
      out.println( indent + "#include <string.h>" );
      out.println( indent + "#include \"qep_port.h\"" );      
      out.println( indent + "#include \"qassert.h\"" );
         
      out.println( indent + "#include \"" + SimRtcSignalWriter.signalEnumName + ".h\"");
      out.println( indent + "#include \"" + classname + ".h\"" );
         
      if (executionTraceOn)
      {
         out.println( indent + "#include \"log_event.h\"" );
      }
         
      out.println();

      writeSubmachineIncludes();
   }

   /**
    * If this machine incorporates any submachines, we must generate a 
    * specialized header file that defines all the states on the
    * submachine.  This gets a bit more complicated since each
    * submachine must be uniquely named for each instantialtion,
    * i.e. includine the same state machine names Sub1 and Sub2
    * will create two sets of state functions for the two name
    * spaces.
    */
   protected void writeSubmachineIncludes()
   {
      // Get the prefix as a filename         
      String prefix = getClassPrefix();
         
      Map<String, SubmachineState> submachines = 
         filterByType( stateMachine.getAllStates( prefix, true ), SubmachineState.class );

      for ( Map.Entry<String, SubmachineState> entry : submachines.entrySet() )
      {
         String filename = 
            entry.getKey().replace( ":", "_" ) + "." + 
            CQuantum3_2StateMachineWriter.submachineHeaderExtension;

         out.println( indent + "#include \"" + filename + "\"" );
      }
   }

   /**
    * Include all the .subc files here as well
    */
   protected void writeSubmachineCIncludes()
   {
      // Get the prefix as a filename         
      String prefix = getClassPrefix();

      // Get all the state in this state diagram and select out the
      // submachine states
      Map<String, SubmachineState> submachines = 
         filterByType( stateMachine.getAllStates( prefix ), SubmachineState.class );

      for ( Map.Entry<String, SubmachineState> entry : submachines.entrySet() )
      {
         String filename = 
            entry.getKey().replace( ":", "_" ) + "." + 
            CQuantum3_2StateMachineWriter.submachineCodeExtension;
         
         out.println( indent + "#include \"" + filename + "\"" );
      }
   }

   /**
    * The class prefix is the namespace of the generating state
    * machine.  For normal state charts the class prefix is equal
    * to the classname, but when generating submachine, the classname
    * remains the same, but the prefix changes to the instantiated 
    * submachine path.
    */
   @Override
   public String getClassPrefix()
   {
      return classname;
   }

   protected void writeStateMachineConstructor() 
   {
      String prefix = getClassPrefix();

      String implName     = classname + "Impl";
      String returnType   = meType + "* ";
      String statePath    = prefix.replace( ":", "_" );
      String functionName = statePath + "_Constructor";

      // Create the function declaration and the initial call to the
      // Quantum Framework constructor to allocate this state machine

      out.println();
      out.println( indent + returnType + functionName + "(" 
                   + meType + "* me, char* objNameNew, "
                   + implName + "* implPtr)");

      out.println( indent + "{");

      incIndent();
      out.println( indent + "QActive_ctor_(&me->super_, (QState)" + 
                   statePath + "_initial);" );

      if ( executionTraceOn )
      {
          out.println( indent + "strcpy(me->objName, objNameNew);" );
          // Append the name of the state machine
          out.println( indent + "strcat(me->objName, \" " +
                  stateMachine.name() + "\");" );
      }

      out.println( indent + "me->impl = implPtr;" );

      // Grab all the states from the state machine, including those
      // contained within submachines.  All the functions used the
      // same type for the "me" pointer, so we have to perform *all* the
      // global allocations here.

      Map<String, State> allStates = 
         stateMachine.getAllStates( true );
                  
      // Allocate all the orthogonal region state machines.         

      Map<String, CompositeStateRegion> allRegions =
         filterByCompositeStateRegion( allStates );

      if ( !allRegions.isEmpty() )
      {
         out.println();
         out.println( indent + "/***" );
         out.println( indent + " * Allocate and initialize the orthogonal region machines" );
         out.println( indent + " */" );
      }

      for ( Map.Entry<String, CompositeStateRegion> entry : allRegions.entrySet() )
      {
         String regionPath = entry.getKey();
         String regionType = regionPath.replace( ":", "" ) + "Region";
         //String regionDecl = toCamelCase( entry.getValue().name() );
         String regionDecl = entry.getValue().name().toLowerCase();

         out.println( indent + "me->" + regionDecl + " = (" + regionType +
                      " *) malloc( sizeof( " + regionType + " ));");
            
         out.println( indent + regionType + "_Constructor( me->"
                      + regionDecl + ", objNameNew, implPtr, &me->super_);");
      }

      // Initialize the history records
      Map<String, CompositeState> allHistoryContainers = 
         filterByHistoryContainers( allStates );

      if ( !allHistoryContainers.isEmpty() )
      {
         out.println();
         out.println( indent + "/***" );
         out.println( indent + " * Initialize the deep histories " );
         out.println( indent + " */" );
      }

      for ( Map.Entry<String, CompositeState> entry : allHistoryContainers.entrySet() )
      {
         out.println( indent + "me->my" + entry.getValue().name() + "history = NULL;");
      }

      // Initialize the Timer constructors 
      Map<String, SimpleState> allTimeoutStates =
         filterByTimers( allStates );

      if ( !allTimeoutStates.isEmpty() )
      {
         out.println();
         out.println( indent + "/***" );
         out.println( indent + " * Initialize all the timers" );
         out.println( indent + " *" );
         out.println( indent + " * Each timer object is declared only once, within" );
         out.println( indent + " * the top-level state chaning or an orthogonal" );
         out.println( indent + " * region.  The top-level state captures each timer" );
         out.println( indent + " * event and passes it to the proper state machine." );
         out.println( indent + " */" );
      }

      // Sort the states into regions
      Map<CompositeStateRegion, Map<String, SimpleState>> statesByRegions =
         sortStatesByEnclosingRegion( allTimeoutStates );

      // Initialize all the timers contained within each region
      // machine
      for ( Map.Entry<CompositeStateRegion, Map<String, SimpleState>> bucket :
               statesByRegions.entrySet() )
      {         
         if ( bucket.getValue().isEmpty() )
            continue;

         CompositeStateRegion region = bucket.getKey();

         out.println();

         if ( region == null )
            out.println( indent + "/* Top-Level Timers */" );
         else
            out.println( indent + "/* " + region.name() + " Region Timers */" );

         String accessor = "me->";
         if ( region != null )
         {
            accessor = accessor + region.name().toLowerCase() + "->";
         }

         for ( Map.Entry<String, SimpleState> entry : bucket.getValue().entrySet() )
         {
            String timerName  = entry.getValue().name() + "_timer";
            String timerEvent = entry.getValue().name() + "_timerEv";
            
            out.println( indent + "QTimeEvt_ctor(&(" + accessor + timerName + "), "
                         + timerEvent + ");");
         } 
      }

      out.println();
      out.println( indent + "return me;" );

      decIndent();
      out.println( indent + "}");        
   }

   protected void writeQActiveInitialMethod() throws Exception
   {
      String prefix = getClassPrefix();
      String statePath = prefix.replace( ":", "_" );

      // Write any actions on the state machine's initial transition
      InitialState startState = stateMachine.getInitialState();

      // No initial state is not always an error, but it likly to be one
      if ( null == startState )
      {
         System.err.println( "Warning: No Initial State found in State machine " + stateMachine.name() + ".  This may be an error in the diagram." );
            
         return;
      }

      if ( startState.getOutgoing().size() != 1 )
      {
         throw new Exception( "Invalid State machine " + stateMachine.name() + ": The initial state must have exactly one outgoing transition." );
      }

      out.println();
      out.println( indent + "void " + statePath + "_initial(" + 
                   meType + "* me,  QEvent const *e)\n{");

      incIndent();
        
      Transition initialTransition = startState.getOutgoing().get(0);
      writeActionList(initialTransition.getActions() );
            
      // Subscribe to list of global events referenced by this statechart:
      Set<String> subscribeSet = signals.keySet();

      /*
        Set<String> subscribeset = new HashSet<String>();
           
        for ( Transition tran : machine.transitions() )
        {
        if (!tran.isLocalEvent() && null != tran.signalName())
        {
        for (String eventname : tran.getSignalNames())
        {
        subscribeset.add(eventname);
        '           }
        }
        }
      */

      for (String name : subscribeSet)
      {
         out.println(indent + "QActive_subscribe_(& me->super_, " + name + ");");
      }
         
      // subscribe to default events
      //      out.println(indent + "QFsubscribe(& me->super_, " + "Q_EVAL"
      //            + ");");
      //      out.println(indent + "QFsubscribe(& me->super_, " + "Q_DURING"
      //            + ");");
      // Check if we're doing regions:
      // Now initialize this state machine to an initial state:

      // Initialize all the concurrent subregions
      Map<String, State> allStates = 
         stateMachine.getAllStates( stateMachine.name(), true );
      
      Map<String, CompositeStateRegion> allRegions =
         filterByCompositeStateRegion( allStates );
      
      if ( !allRegions.isEmpty() )
      {
         out.println();
         out.println( indent + "/***" );
         out.println( indent + " * Initiate the orthogonal regions" );
         out.println( indent + " */" );
      }

      for ( Map.Entry<String, CompositeStateRegion> entry : allRegions.entrySet() )
      {
         String regionPath = entry.getKey();
         String regionType = regionPath.replace( ":", "" ) + "Region";
         //String regionDecl = toCamelCase( entry.getValue().name() );
         String regionDecl = entry.getValue().name().toLowerCase();

         out.println( indent + "QHsm_init( &(me->"
                      + regionDecl + "->super_), e );");
      }
       
      /**
       * Find the outermost initial type pseudostate for this machine, 
       * find its outgoing transition, and then the actual initial 
       * state which is the target of the transition. This is the state
       * we want to Q_INIT to... 
       */

      // State initialState = 
      //   stateMachine.getTargetState( initialTransition.id() );

      out.println();
      writeStateTransitionCode( initialTransition, "Q_INIT" );
      //out.println(indent + "Q_INIT(" + statePath + "_" + initialState.name() + ");");

      decIndent();
      out.println( indent + "}" );
      out.println();
   }
     
   protected void writeOneRegionDefinition( String path, CompositeStateRegion region ) throws Exception
   {
      writeQHSMConstructor( path, region );
      writeInitialMethod( path, region );
   }

   /**
    * Write a state method body.  This is by *far* the most complicated
    * piece of code in the entire system.  It is slowly being refactored
    * on an as-need basis.
    */
   protected void writeOneMethodBody( String path, SimpleState state )
      throws Exception
   {
      String timeouttext = "";
      
      // Write out the method declaration         
      List<Transition> outList = state.getOutgoing();
      String statePath = path.replace( ":", "_" );

      out.println( indent + "/**" );
      out.println( indent + " * Implementation of " + state.name() );
      out.println( indent + " */" );
         
      // If the state is contained within an Orthogonal region, then the
      // class type becomes the sub-region type
      String implType = meType;
      
      CompositeStateRegion enclosingRegion = stateMachine.getEnclosingRegion( state );
      if ( enclosingRegion != null )
      {
         String fullPath = stateMachine.getQualifiedPath( enclosingRegion, getClassPrefix() );
         implType = fullPath.replace( ":", "" ) + "Region";
      }
      
      out.println( indent + "QSTATE " + statePath + "("
                  + implType + "* me, QEvent const *e)\n{");
      
      incIndent();

      if (executionTraceOn)
      {
         out.println(indent + "char stateName[256];");
         out.println();
      }
         
      if ( state.hasTimeout() )
      {
         for ( Transition t : outList )
         {
            if ( null == t.timeout() )
               continue;
               
            // There is a syntax for the time-out strings.
            timeouttext = t.timeout().getTimeout();
             
            // Use the raw timeout text if the string is quoted
            if(timeouttext.contains("\"") || isNumeric(timeouttext))
            {
               timeouttext = timeouttext.replace('"', ' ');
            }
            // If the text contains a paraenthesis, assume it is a variable
            else if(! timeouttext.contains(")"))
            {                  
               timeouttext = "me->impl->" + timeouttext; 
            }
            // Otherwise generate code to all the implementation class
            else
            {
               timeouttext = getCallText(timeouttext);
            }
               
            // out.println(indent + "static MyTimer timer = {"+ t.timeout().getTimeout() +  ", 0};\n");
            // variable may be not a constant, so just initialize to zero, and set it on entry to a state            ////               out.println(indent + "static MyTimer timer = {0, 0};\n"); 
         }
      }
         
      if (executionTraceOn)
      {
	out.println(indent + "strcpy(stateName, me->objName);");
	// Append the name of the state
	out.println(indent + "strcat(stateName, \"" + state.name() + "\");");
      }
      
      // Write main switch statement for all events handled by this state:
      out.println();
      out.println(indent + "switch (e->sig)" );
      out.println(indent + "{");

      // Generate Entry condition code: (Note: even if we don't have any entry
      // actions, we still want to signal to the user that we've entered a new
      // state.)
      incIndent();
      decIndent( 1 );
      out.println(indent + "case Q_ENTRY_SIG:");
      incIndent( 1 );

      out.println( indent + "me->mystate = " + statePath.toUpperCase() +  ";" ); 
                           
      if (executionTraceOn)
      {
         out.println(indent + "strcat(stateName, \" ENTRY\");");
         out.println(indent + "LogEvent_log(stateName);");
      }
         
      // If the state is a concurrent state with orthogonal 
      // regions, then the regions must be initialized. 
      // 
      // This functionality has been moved into the QActive
      // constructor for scoping reasons.
         
      if (null != state.getEntryActions())
      {
         writeActionList(state.getEntryActions());
      }

      if ( state.hasTimeout() )
      {
         //String timerName = path.replace( ":", "" ) + "_timer";
         String timerName = state.name() + "_timer";

         // If we're in a region, use the parent
         if( stateMachine.getEnclosingRegion( state ) != null || this.isWithinRegion() ) 
         {               
            out.println( indent + "QTimeEvt_postIn(&me->" + timerName +
                         ", me->parent, " + timeouttext.trim() + ");" );
         }
         // not in a region
         else 
         {
            out.println( indent + "QTimeEvt_postIn(&me->" + timerName +
                         ", (QActive *) me, " + timeouttext.trim() + ");" );
         }
            
         // out.println(indent + "timer.timeout = " + timeouttext + ";");
         // out.println(indent + "MyTimer_startTimer(&timer);");
      }
         
      out.println(indent + "return 0;");

      /***      if (!state.getDuringActions().isEmpty()
                || state instanceof ConcurrentCompositeState)
                {
                // Generate "During" code:
                indent = INDENT_CASE;
                out.println(indent + "case Q_DURING:");
                indent = "      ";
                writeActionList(state.getDuringActions());
                if (state instanceof ConcurrentCompositeState)
                {
                List<State> kids = ((ConcurrentCompositeState) state).getChildren();
                for (State kidstate : kids)
                {
                out.println(indent + "    QHsmDispatch( &(me->"
                + kidstate.name().toLowerCase() + "->super_), e);");
                }
                }
                out.println(indent + "return 0;");
                }
      ***/      
         
      // Generate Exit condition code:

      decIndent( 1 );

      out.println();
      out.println( indent + "case Q_EXIT_SIG:" );
                 
      incIndent( 1 );
      
      if (executionTraceOn)
      {
         out.println(indent + "strcat(stateName, \" EXIT\");");
         out.println(indent + "LogEvent_log(stateName);");
      }
         
      if (null != state.getExitActions())
      {
         writeActionList(state.getExitActions());
      }
         
      // We are getting away with this for now because the naming
      // convention for the orthogonal regions in
      // writeQActiveDeclaration is the same
      //
      // WARNING: FRAGILE CODE
         
      if (state instanceof ConcurrentCompositeState)
      {
         List<State> children = ((ConcurrentCompositeState) state).getChildren();
         for ( State child : children )
         {
            /*
             * out.println(indent + kidstate.name().toLowerCase() +
             * "->Clear( (QPseudoState) &" + kidstate.name() + "::initial);");
             */
            out.println(indent + "QHsm_dispatch( &(me->"
                        + child.name().toLowerCase()
                        + "->super_),  e);");
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
               out.println(indent + "me->my" + state.name()
                           + "history = QHsm_getState_(&(me->super_.super_) );");
            } else
               // its a qhsm
            {
               out.println(indent + "me->my" + state.name()
                           + "history = QHsm_getState_(&(me->super_) );");
            }
         }
      }

      if ( state.hasTimeout() )
      {
         //String timerName = path.replace( ":", "" ) + "_timer";
         String timerName = state.name() + "_timer";
         out.println(indent + "QTimeEvt_disarm(&me->" + timerName + ");");
      }

      out.println(indent + "return 0;");

      //// End Q_EXIT_SIG codeing

      //// Begin timer handlers
         
      boolean needsFinalReturn = false;         
      if ( state.hasTimeout() )
      {
         String timerEvent = state.name() + "_timerEv";

         // Generate code for transition guard evaluations:
         decIndent( 1 );

         out.println();
         out.println(indent + "case " + timerEvent + ":");
                                             
         incIndent( 1 );

         for ( Transition t : outList )
         {
            if ( null == t.timeout() )
               continue;

            if ( null != t.timeout() && null != t.signalName() )
            {
               throw new Exception("Invalid transition " + t.signalName()
                                   + " can't have both trigger event and timeout.");
            }
            
            if (executionTraceOn)
            {
               out.println(indent + "strcat(stateName, \" " + state.name() + "_timerEv\");");
               out.println(indent + "LogEvent_log(stateName);");
            }
            
            writeStateTransitionCode( t );
            out.println(indent + "return 0;");
         }
            
         // Check if we have subregions which need to process the Q_EVAL event:
         if (state instanceof ConcurrentCompositeState)
         {
            List<State> kids = ((ConcurrentCompositeState) state).getChildren();
            for (State kidstate : kids)
            {
               out.println(indent + "QHsm_dispatch( &(me->"
                           + kidstate.name().toLowerCase() + "->super_), e);");
            }
         }
            
         if( needsFinalReturn )
         {
            out.println(indent + "return 0;");
         }
      }
         
      /**
       * Now check for child states which we need to Q_INIT (initialize) to:
       *
       * We handle CompositeStates that are not Concurrent and Submachine
       * states. The submachine states must be handled separately since we
       * have to cross-reference to a different state machine.
       */
      if (state instanceof CompositeState
          && !(state instanceof ConcurrentCompositeState)
          && !(state instanceof SubmachineState))
      {            
         // Find the initial state for this composite state
         InitialState initialState = stateMachine.getInitialState( state );
            
         // We should almost always find an initial state
         if ( initialState == null )
         {
            throw new Exception( "No initial state found for " + state.name() );
         }

         // Get the initial outgoing transition from the initial state
         List<Transition> transitions = initialState.getOutgoing();

         // Error check
         if ( transitions.size() != 1 )
         {
            throw new Exception( "The initial state within " + 
                                 state.name() + 
                                 " must have exactly one outgoing" + 
                                 " transition" );
         }

         Transition itran = transitions.get( 0 );

         // Perform a second check to ensure that the destination
         // state is a child of the composite state
         State childState = stateMachine.xmiStates.get( itran.targetId() );

         if ( childState == null || !childState.getParent().equals( state ))
         {
            System.err.println("transition target ID " + itran.targetId());
            throw new Exception("Destination of initial transition of" + 
                                " state " + state.name() + 
                                " not found as proper substate" + 
                                " accepting transition.\n" + 
                                "Check for malformed diagram or state" + 
                                " containment heirarchy in drawing.");
         }

         // Must handle the Q_INIT_SIG:
         decIndent( 1 );

         out.println();
         out.println(indent + "case Q_INIT_SIG:");

         incIndent( 1 );
            
         Transition transition = findInitialTransitionTo(childState);

         if(transition != null)
         {
            writeActionList(transition.getActions());
         }

         writeStateTransitionCode( transition, "Q_INIT" );
         // out.println(indent + "Q_INIT(" + statePath + "_" + childState.name() + ");");
         out.println(indent + "return 0;");
      }
      
      /**
       * Handle the submachine states here
       */
      submachine_block: 
      if ( (state instanceof CompositeState) &&
           !(state instanceof ConcurrentCompositeState) &&
           (state instanceof SubmachineState))
      {
         // Get the submachine and find the initial state and the
         // transition out of the initial state
         StateMachine submachine = 
            ((SubmachineState) state).getStateMachine();
            
         // Find the initial state for this submachine
         InitialState initialState = submachine.getInitialState();
            
         // It is not strictly an error to omit an initial state
         // from a submachine, but we want to at least warn
         if ( initialState == null )
         {
            System.err.println( "Warning: The submachine " + submachine.name() + " does not have an initial state.  This may be an error." );
            break submachine_block;
         }

         // Get the initial outgoing transition from the initial state
         List<Transition> transitions = initialState.getOutgoing();

         // Error check
         if ( transitions.size() != 1 )
         {
            throw new Exception( "The initial state within the submachine " + 
                                 submachine.name() + 
                                 " must have exactly one outgoing" + 
                                 " transition" );
         }
            
         Transition itran = transitions.get( 0 );
            
         // Perform a second check to ensure that the destination
         // state is a child of the submachine
         State childState = stateMachine.xmiStates.get( itran.targetId() );

         if ( childState == null || !(childState.getParent() == null ))
         {
            throw new Exception("Destination of initial transition of" + 
                                " state " + state.name() + 
                                " not found as proper substate" + 
                                " accepting transition.\n" + 
                                "Check for malformed diagram or state" + 
                                " containment heirarchy in drawing.");
         }

         out.println();

         // Must handle the Q_INIT_SIG:
         decIndent( 1 );
         out.println(indent + "case Q_INIT_SIG:");
         incIndent( 1 );
         
         writeActionList( itran.getActions() );            
        
         //writeStateTransitionCode( itran, "Q_INIT" );
         out.println(indent + "Q_INIT(" + statePath + "_" + childState.name() + ");" );
         out.println(indent + "return 0;");
      }

      // TODO [swc] Handle overlap of events with orthogonal regions:
      // It appears that the switch case below does not properly handle cases
      // when a transition out of this state is also dispatched to ortho regions.

      // Need a case branch for each out-going transition.  SimpleStates
      // are guaranteed to at least have an empty list
      //
      // Not sure why null signal names are allowed....
      for ( Transition t : state.getOutgoing() )
      {
         if ( null != t.signalName() )
         {
            writeSwitchCase(t, classname);
         }         
      }
         
      // Handle dispatch to child subregions:
      if (state instanceof ConcurrentCompositeState)
      {
//         // Try to get the set of enumerated signals.  This is a hack and
//         // needs to be cleaned up.  Signals were the first thing to be
//         // namespaced and, as such, so not conform to the rest of the code
//         // base
//         StateMachineXmiReader reader = 
//            CQuantum3_2StateMachineWriter.reader;
//
//         Map<String, String> signalMap = null;
//
//         try
//         {
//            signalMap = new RtcSignalWriter3_2(reader).getSignalNamesAndComments();
//         }
//         catch ( Exception e )
//         {
//            throw new RuntimeException( "Could not get signals names" );
//         }
//
//         if ( !signalMap.isEmpty())
          ConcurrentCompositeState concurrent = (ConcurrentCompositeState)state;
          List<State> kids = concurrent.getChildren();
          Set<String> allDesiredEvents = new HashSet<String>();
          for (State kidState : kids) {
              allDesiredEvents.addAll(kidState.getAllDesiredEvents());
          }
          if (!allDesiredEvents.isEmpty()) {
            out.println();

            decIndent( 1 );
            out.println( indent + "/**" );
            out.println( indent + " * Dispatch transition events to orthogonal regions" );
            out.println( indent + " */" );

            // For each signal, qualify the name and print it out
            for ( String signalName : allDesiredEvents )
            {
               out.println( indent + "case " + signalName + ":");
            }
            incIndent(  1 );

            // Pass each signal to all the orthogonal regions
            Map<CompositeStateRegion, Map<String, SimpleState>> statesByRegion = 
               sortStatesByEnclosingRegion( filterByType( stateMachine.getAllStates( true ), SimpleState.class ));

            for ( CompositeStateRegion region : statesByRegion.keySet() )
            {
               if ( region == null )
                  continue;

               out.println(indent + "QHsm_dispatch( &(me->"
                           + region.name().toLowerCase() + "->super_), e);");
            }
            out.println(indent + "return 0;");
         }
      }
         
      // Also dispatch timer events to orthogonal children
      if (state instanceof ConcurrentCompositeState)
      {
         ConcurrentCompositeState concurrent = 
            (ConcurrentCompositeState) state;

         // Grap all the timer states contained within this orthogonal
         // composite state
         Map<String, SimpleState> allTimeoutStates = 
            filterByTimers( stateMachine.getAllStates( state, "", true ));

         // Arrange the states by orthogonal region
         Map<CompositeStateRegion, Map<String, SimpleState>> statesByRegions =
            sortStatesByEnclosingRegion( allTimeoutStates );

         // Collect the events to dispatch to each orthogonal region
         for ( Map.Entry<CompositeStateRegion, Map<String, SimpleState>> bucket :
                  statesByRegions.entrySet() )
         {         
            // Skip empty regions or to top-level region
            if ( bucket.getKey() == null || bucket.getValue().isEmpty() )
               continue;

            CompositeStateRegion region = bucket.getKey();

            out.println();
            decIndent( 1 );
            out.println( indent + "/**" );
            out.println( indent + " * Dispatch timer events to " + region.name() );
            out.println( indent + " */" );

            for ( Map.Entry<String, SimpleState> entry : bucket.getValue().entrySet() )
            {
               String timerEvent = entry.getValue().name() + "_timerEv";
               out.println(indent + "case " + timerEvent + ":" );
            } 

            incIndent( 1 );
            out.println(indent + "QHsm_dispatch( &(me->"
                        + region.name().toLowerCase() + "->super_), e);");                           
            out.println(indent + "return 0;");            
         }         
      }
         
      // Finish up this method body:
      decIndent();
      out.println(indent + "}");

      // If the state is a top-level state (parent == null) or is the state
      // is a top-level state within an orthogonal region, terminate the 
      // signal processing hierarchy here.
      //
      if ( ( state.getParent() == null ) || 
           ( state.getParent() instanceof CompositeStateRegion ))
      {
         out.println(indent + "return (QSTATE) " + getTopLevelState() + ";");
      } 
      else
      {
         // Knock off the current state from the path, unless the parent is
         // a CompositeStateRegion, then skip up again
         int last = path.lastIndexOf( ":" );
         if ( last < 0 )
            last = path.length();

         String parentStatePath = path.substring( 0, last );

         if ( state.getParent() instanceof CompositeStateRegion )
         {
            last = parentStatePath.lastIndexOf( ":" );
            if ( last >= 0 )
               parentStatePath = parentStatePath.substring( 0, last );
         }

         out.println(indent + "return (QSTATE) " + 
                     parentStatePath.replace( ":", "_" ) + ";" );
      }
         
      decIndent();
      out.println(indent + "}");
      out.println();
   }

   protected State getExitPointTarget( ExitPoint state )
   {
      // Scan all the connection points to fund out which
      // one links to the ExitPoint
      ConnectionPointReference ref = null;

      for ( State s : stateMachine.xmiStates.values() ) 
      {            
         if (!( s instanceof ConnectionPointReference ))
            continue;
            
         ConnectionPointReference ref2 = (ConnectionPointReference) s;

         if ( ref2.getLink().equals( state.id())) 
         {
            ref = ref2;
            break;
         }         
      }

      // If we could not resolve the connection point reference, return
      // null
      if ( ref == null )
         return null;

      // If there is more than one transition from the Connection point,
      // raise an error
      if ( ref.getOutgoing().size() > 1 )
      {
         throw new RuntimeException( "More than one outgoing transition from a ConnectionPoint" );
      }

      // If there are no transitions, return null
      if ( ref.getOutgoing().size() == 0 )
         return null;

      // Otherwise resolve the one
      Transition t = ref.getOutgoing().get( 0 );
         
      return stateMachine.xmiStates.get( t.targetId() );
   }

   protected String getTopLevelState()
   {
      return "QHsm_top";
   }

   protected void writeSwitchCase(Transition transition, String classname)
      throws Exception
   {
      for (String signalname : transition.getSignalNames())
      {
         // This is a bit of a pain.  If we are qualifying the signals,
         // then we have to search up through the submachine
         // instantiation space until we find the first state machine
         // that defines this signal
         //
         // Note that all of this searching could be eliminated if we
         // just processed the XML DOM directly as we would have the
         // signal id and be able to walk up to tree from where the
         // signal was defined
         if ( qualifySignalNames )
         {
            Node   node = StateMachineXmiReader.xmiNodes.get( stateMachine.id() );
            String name = node.getAttributes().getNamedItem("name").getNodeValue() + "_";
            boolean resolved = false;

            for (;;)
            {
               // Does the current state machine define the signal?
               if ( signals.containsKey( name + signalname ))
               {
                  resolved = true;
                  break;
               }

               // Otherwise, find the enclosing uml:statemachine
               node = node.getParentNode();

               if ( node == null || 
                    node.getAttributes() == null || 
                    node.getAttributes().getNamedItem("name") == null )
               {
                  break;
               }

               name = node.getAttributes().getNamedItem("name").getNodeValue() + "_";

               // If this is a uml:model element, break
               if ( node.getNodeName().compareToIgnoreCase( "uml:model" ) == 0 )
                  name = "";
            }

            // If the signal was now defined locally, try to find *any*
            // namespace that contains the signal definition as a last
            // resort.
            if ( !resolved )
            {
               for ( StateMachine curr : reader.getStateMachineMap().values() )
               {
                  name = curr.name() + "_";
                  if ( signals.containsKey( name + signalname ))
                  {
                     resolved = true;
                     break;
                  }
               }
            }

            if ( resolved )
            {
               signalname = name + signalname;
            }
            else
            {
               throw new Exception( "Unresolved signal " + signalname );
            }
         }

         out.println();
         decIndent( 1 );
         out.println(indent + "case " + signalname + ":");
         incIndent( 1 );

         if (executionTraceOn)
         {
            out.println(indent + "strcat(stateName, \" " + signalname
                        + "\");");
            out.println(indent + "LogEvent_log(stateName);");
         }
            
         writeStateTransitionCode( transition );

         out.println( indent + "return 0;" );
      }
   }

   private boolean isNumeric(String str)
   {
      try 
      {
         Integer.parseInt(str);
         return true;
      } 
      catch (NumberFormatException nfe)
      {
         return false;
      }
   }


   protected void writeInitialMethod(String path, CompositeStateRegion region) throws Exception
   {
      String implName   = classname + "Impl";
      String regionType = path.replace( ":", "" ) + "Region";
         
      out.println();
      out.println( indent + "void " );
      out.println( indent + regionType + "_initial(" + 
                   regionType + " *me,  QEvent const* e)");
      out.println( indent + "{" );

      // Find the initial state for this region:
      InitialState initial = stateMachine.getInitialState( region );
      if (null == initial)
         throw new Exception("Composite state region " + region.name() + " must specify an initial substate.");
         
      // Remove the state's name from the path
      String path2 = path;
      int last = path.lastIndexOf( ":" );
      if ( last >= 0 )
         path2 = path.substring( 0, last );

      // Get the qualified name of the initial state
      Map<String, State> regionStates = 
         stateMachine.getAllStates( region, path2 );

      // Find the state
      String targetId = initial.getOutgoing().get( 0 ).id();
      State initialState = stateMachine.getTargetState( targetId );

      path = null;
      for ( Map.Entry<String, State> entry : regionStates.entrySet() )
      {
         if ( entry.getValue() != initialState )
            continue;

         path = entry.getKey();
         break;
      }

      if ( path == null )
      {
         throw new Exception( "Could not resolve qualified name of the initial state for " + region.name() );
      }

      incIndent();
      out.println(indent + "Q_INIT(&" + path.replace(":", "_") + ");");
      decIndent();

      out.println( indent + "}");
      out.println();
   }
      
   protected void writeQHSMConstructor(String path, CompositeStateRegion region)
   {
      String implName   = classname + "Impl";
      String endPath    = path.substring(path.lastIndexOf(":")+1);
      String regionType = path.replace( ":", "" ) + "Region";

      out.println();
      out.println( indent + regionType + " *" );
      out.println( indent + regionType + "_Constructor(" + 
                   regionType + " *me, char* objNameNew, " + 
                   implName + "* implPtr, QActive* parent)");
      out.println( indent + "{" );
      
      incIndent();
      out.println( indent + "QHsm_ctor_(&me->super_, (QState)" + 
                   regionType + "_initial);" );
         
      if(executionTraceOn)
      {
         out.println( indent + "strcpy(me->objName, objNameNew);" );
	 // Append the name of the state machine
	 out.println( indent + "strcat(me->objName, \" " +
		      endPath + "\");" );
      }

      out.println( indent + "me->impl = implPtr;" );
      out.println( indent + "me->parent = parent;" );
         
      // TODO: Do the othogonal subamachine need to handle their own
      // timers and histories?

      for ( CompositeState composite : stateMachine.getHistoryContainers(region.getChildren()))
      {
         out.println( indent + "me->my" + composite.name() + "history = NULL;" );
      }

      // Timer constructors
      for(State timedState : stateMachine.getTimerStates( region ))
      {
         out.println( indent + "QTimeEvt_ctor(&me->" + timedState.name() + "_timer, "
                     + timedState.name() + "_timerEv);" );
      }      
         
         
      out.println(indent + "return me;");
      decIndent();
      out.println( indent + "}");
   }

   protected void writeToExitPoint( ExitPoint exitPoint, String function )
   { 
      out.println( indent + "/* Alternate exit from submachine */" );
      State target = getExitPointTarget( exitPoint );

      if ( target != null )
      {  
         // Move one level up
         String prefix = getClassPrefix().replace( ":", "_" );
         int    index  = prefix.lastIndexOf( '_' );
         if ( index != -1 )
            prefix = prefix.substring( 0, index );
            
         out.println(indent + function + "(" + prefix + "_" + target.name() + ");");
      }
   }

   protected void writeToSubmachineState( SubmachineState state, String function )
   {
      // Get the initial state from the submachine
      String statePath = getClassPrefix().replace( ":", "_" );
                  
      out.println(indent + "/* Normal transition into submachine */" );
      out.println(indent + function + "(" + statePath + "_" + state.name() + ");");
   }

   protected void writeToConnectionPointReference( ConnectionPointReference ref ) throws Exception
   {
      // Need to search the other state machines to find the 
      // state to transition in to.
      //
      // 1. Search the for an EntryState that matches the <entry />
      //    of this state
      //
      // 2. Follow the ExtryState to the target state and verify
      //    there are no events on the transition
      //
      // 3. Write the state expansion
      //
         
      out.println( indent + "/* Alternate entry to submachine */" );

      // Get the parent state, which is the Submachine 
      SubmachineState state = (SubmachineState) ref.getParent();
      StateMachine submachine = state.getStateMachine();
      String classname = stateMachine.name();

      // Create a submachine writer to write this piece of code
      String path = stateMachine.getQualifiedPath( state, getClassPrefix() );

      SubmachineCWriter submachineWriter = 
         new SubmachineCWriter( submachine, classname, path );

      submachineWriter.setPrintStream( out );
      submachineWriter.setIndent( indent );
      submachineWriter.setWithinRegion( StateMachine.getEnclosingRegion( state ) != null );

      // Get the linked state in the submachine
      EntryPoint entry = 
         (EntryPoint) stateMachine.xmiStates.get( ref.getLink() );
                  
      // Get the transition out of this state.  There should be
      // only one, but let's be permissive
      for ( Transition nextTransition : entry.getOutgoing() )
         submachineWriter.writeStateTransitionCode( nextTransition );
   }


   protected void writeStateTransitionCode( Transition transition ) throws Exception
   {
     writeStateTransitionCode( transition, "Q_TRAN" );
   }

   protected void writeStateTransitionCode( Transition transition, String function ) throws Exception
   {
      writeTransitionGuard( transition.guard() );

      if ( transition.guard() != null )
      {
         incIndent();
      }

      writeActionList(transition.getActions());
         
      if ( !transition.isInternal() )
      {
         State targetState = stateMachine.getTargetState( transition.id() );
               
         // Having no target is a fatal error
         if ( targetState == null ) 
         {
            System.out.println( "*** ERROR ***\nUnresolved target for transition " + transition.id() );
            System.exit(1);
         }
               
         if (targetState instanceof JunctionState)
         {
             writeJunctionState((JunctionState) targetState, classname);
         } 
         else if (targetState instanceof FinalState)
         {
            out.println(indent + "this->stop();");
         } 
         else if (targetState instanceof DeepHistoryState)
         {
            writeToDeepHistory((DeepHistoryState) targetState, classname);
         } 
         else if (targetState instanceof ExitPoint)
         {
            writeToExitPoint((ExitPoint) targetState, function );
         } 
         else if ( targetState instanceof SubmachineState )
         {     
            writeToSubmachineState((SubmachineState) targetState, function);
         } 
         else if (targetState instanceof ConnectionPointReference)
         {       
            writeToConnectionPointReference((ConnectionPointReference) targetState);
         }           
         else
         {               
            String statePath = stateMachine.getQualifiedPath( targetState, getClassPrefix() );
            
            if ( statePath == null )
            {
               String stateName = targetState.name();
               throw new RuntimeException( "Cannot resolve path for state " + stateName);
            }

            out.println( indent + function + "(" + statePath.replace( ":", "_") + ");" );
         }
      }

      if ( transition.guard() != null )
      {
         decIndent();
      }

      writeTransitionGuardClose( transition.guard() );
   }

   protected void writeTransitionGuardClose( TransitionGuard guard )
   {
      if ( guard == null )
         return;

      out.println(indent + "}");
   }

   protected void writeJunctionState(JunctionState js, String classname) throws Exception
   {
      List<Transition> outgoing = js.getOutgoing();

      if (outgoing.size() != 2)
      {
         throw new Exception("Junction state " + js.name() + " must have exactly two outgoing transitions.");
      }

      Transition t0 = outgoing.get(0);
      Transition t1 = outgoing.get(1);
         
      // Error if neither transition has a guard or noth have guards
      if ( (null == t0.guard() && null == t1.guard()) || 
           (null != t0.guard() && null != t1.guard()))
      { 
         throw new Exception("Exactly one transition from junction state "
                             + js.name() + " can have a guard.");
      }
         
      Transition guardTransition;
      Transition otherTransition;
         
      if (null != t0.guard())
      {
         guardTransition = t0;
         otherTransition = t1;
      }
      else
      {
         guardTransition = t1;
         otherTransition = t0;
      }
         
      writeStateTransitionCode( guardTransition );

      out.println(indent + "else" );
      out.println(indent + "{");

      incIndent();
      writeStateTransitionCode( otherTransition );
      decIndent();

      out.println(indent + "}");
   }

   /**
    * Writes out code for a transition guard. Note below that this method leaves
    * opens the "if" code block, so the caller must close it by writing the
    * closing curly brace.
    * 
    * @param tg
    *           TransitionGuard
    */
   protected void writeTransitionGuard(TransitionGuard tg)
   {
      if ( tg == null )
         return;

      out.print(indent + "if (");
      
      if( tg.methodName().contains("\"") )
      {
         String noquotes = tg.methodName().replace('\"', ' ');
         out.println(noquotes + ")");
      }
      else
      {            
         if (tg.notOp())
         {
            out.print("!");
         }
         // String tempmethodname = tg.methodName().replaceFirst("\\)", " ");
         // out.println(classname + "Impl_" + tempmethodname
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
               out.println( classname + "Impl_" + callname + "(me->impl," + params + "))" );
            }
            else
            {
               out.println( classname + "Impl_" + tempname + "me->impl))");
            }        	 
         }
         else
         {
            out.println(classname + "Impl_" + tempname + "me->impl))" );
         }
      }         

      out.println(indent + "{");
   }
     
   protected Transition findInitialTransitionTo( State state )
   {
      State parent = state.getParent();
         
      if ( parent == null )
      {
         return null;
      }
         
      CompositeState composite = (CompositeState) parent;
      InitialState   initial   = stateMachine.getInitialState(composite.getChildren());

      if ( initial == null )
      {
         return null;
      }

      for( Transition transition: initial.getOutgoing() )
      {
         State target = stateMachine.getTargetState(transition.id() );
         if( target == state )
         {
            return transition;
         }
      }

      return null;      
   }
      
   protected void writeToDeepHistory(DeepHistoryState state, String classname)
      throws Exception
   {
      String historyVariable = "my" + state.getParent().name() + "history";

      out.println( indent + "if(me->" + historyVariable + " == NULL)");
      out.println( indent + "{" );

      if (!state.hasOutgoing())
         throw new Exception("History state " + state.name()
                             + " must have an initial outgoing transition.");

      Transition transition = state.getOutgoing().get(0);
      State targetState = stateMachine.getTargetState(transition.id());
      String statePath = stateMachine.getQualifiedPath(targetState, getClassPrefix());

      incIndent();
      out.println(indent + "Q_TRAN(" + statePath.replace( ":", "_" ) + ");");
      decIndent();
      out.println(indent + "}");
      out.println(indent + "else{");
      incIndent();
      out.println(indent + "Q_TRAN_DYN(me->" + historyVariable + ");");
      decIndent();
      out.println(indent + "}");
   }

   protected void writeActionList(List<Action> actions)
   {
      if ( actions == null )
         return;

      for ( Action action : actions )
         writeAction(action);         
   }

   protected void writeAction(Action action)
   {
      if (action instanceof EventAction)
      {
         out.println(indent + "QF_publish( Q_NEW(QEvent, " + action.name() + ") );");
      } 
      else if (action instanceof CallAction)
      {
         ////
         String text = getCallText(action.name());
         out.println(indent + text  + ";");
         /////
      }
   }

   protected Map<String, String> getSignalNamesAndComments()
   {
      // Construct a map which associates signal names with corresponding user
      // comments:
      HashMap<String, String> signalMap = new HashMap<String, String>();
         
      if ( !qualifySignalNames ) 
      {
         for ( Node sig : reader.getSignalList() )
            addSignalToMap( signalMap, sig );
      }
         
      // If we have to qualify the signal names, we must reprocess the
      // file.  The signals may be qualified within a state diagram, or
      // be defined at a top level.  We need to determine what the case
      // is.  This should probably be better about recursing though nested
      // diagrams, but we only go one deep now anyway
      else
      {
         List<Node> signalList = new ArrayList<Node>();
            
         // First, look for signals defined within the uml:model
         for ( Node xmixmi : reader.topLevelNodes )
         { 
            if ( xmixmi == null )
               continue;
               
            try
            {
               // Get the uml:model element
               Node modelTop = reader.modelTop( xmixmi );
                  
               signalList.clear();
                  
               // Look for signals at the top level
               reader.findNodes(signalList, "", "xmi:type", "uml:signal", modelTop, 1 );
                  
               for ( Node sig : signalList )            
                  addSignalToMap( signalMap, sig, "" );        
            }
            catch ( Exception e )
            {
               System.err.println( e );
            }
         }
            
         for ( Node stateMachineNode : reader.getStateMachineNodes() )
         {            
            signalList.clear();
               
            // Get the state machine name
            String name = stateMachineNode.getAttributes().getNamedItem("name").getNodeValue();
               
            // Search for all the signals within this state machine
            reader.findNodes(signalList, "", "xmi:type", "uml:signal", stateMachineNode, 1 );
               
            // Add the qualified signal names to the map
            for ( Node sig : signalList )            
               addSignalToMap( signalMap, sig, name + "_" );            
         }
      }
         
      return signalMap;
   }  
      
   /**
    * These three method are cut-and-paste from RtcSignalWrite.  Should
    * think about bringing them into the Reader
    */
   private void addSignalToMap( Map<String, String> signalMap, Node sig )
   {
      addSignalToMap( signalMap, sig, "" );
   }
      
   private void addSignalToMap( Map<String, String> signalMap, Node sig, String classname )
   {
      if (null == sig.getAttributes().getNamedItem("name") ||
          null == sig.getAttributes().getNamedItem("name").getNodeValue())
      {
         return;
      }
         
      String name    = (String) sig.getAttributes().getNamedItem("name").getNodeValue().trim();
      String comment = reader.findComment(sig);
         
      // Filter out local events...
      if (Transition.isLocalSignalName(name))
         return;
         
      signalMap.put(classname + name, comment);
   }

   protected String getCallText( String originalname )
   {
      if(originalname.startsWith("\""))
      {
         String noquotes = originalname.replace('\"', ' ');
         return noquotes.trim();                     
      }
      else
      {
         String tempname = originalname.replaceAll("\\)", "");
         String [] split = originalname.split("\\(|\\)");
                        
         if(split.length > 1)
         {
            String callname = originalname.split("\\(|\\)")[0].trim();
            String params   = originalname.split("\\(|\\)")[1].trim();
               
            if(params.length() > 0)
            {
               return  classname + "Impl_" + callname + "(me->impl," + params + ")" ;
            }
            else
            {
               return  classname + "Impl_" + tempname + "me->impl)";
            }            
         }
         else
         {
            return classname + "Impl_" + tempname + "me->impl)" ;
         }
      }
   }
}
