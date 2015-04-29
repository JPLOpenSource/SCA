package gov.nasa.jpl.statechart.autocode.cm;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.autocode.gui.ExecutionTracePythonWriter;
import gov.nasa.jpl.statechart.core.Action;
import gov.nasa.jpl.statechart.core.CallAction;
import gov.nasa.jpl.statechart.core.CompositeState;
import gov.nasa.jpl.statechart.core.CompositeStateRegion;
import gov.nasa.jpl.statechart.core.ConcurrentCompositeState;
import gov.nasa.jpl.statechart.core.InitialState;
import gov.nasa.jpl.statechart.core.JunctionState;
import gov.nasa.jpl.statechart.core.SimpleState;
import gov.nasa.jpl.statechart.core.State;
import gov.nasa.jpl.statechart.core.StateMachine;
import gov.nasa.jpl.statechart.core.Transition;
import gov.nasa.jpl.statechart.input.StateMachineXmiReader;
import gov.nasa.jpl.statechart.input.magicdraw.MagicDrawUmlReader;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


/** @author Eddie Benowitz */

public class KGStateMachineWriter
{

   private StateMachine          machine;
   private StateMachineXmiReader reader;
   private PrintStream           headerfile;
   private PrintStream           cfile;
   private Set<State> allstates;
   private Set<String> allsignals;
   private int indentcount;
   private static final String topregionname = "temp.state"; 
   private boolean               executionTraceOn = true;
   
   
   // least common ancestor data structures
   private Map<State, Integer> depthmap;
   
   
   // least common ancestor functions
   
   private int getStateDepth(State state)
   {
      return depthmap.get(state);
   }
   
   private void initializeDepthMap()
   {  
      depthmap = new HashMap<State, Integer>();
      for(State state: machine.states())
      {
         initializeDepthMap(state, 0);
      }
   }
   
   private void initializeDepthMap(State state, int depth)
   {
      depthmap.put(state, depth);
      if(state instanceof CompositeState)
      {
         CompositeState compositestate = (CompositeState) state;
         for(State kid : compositestate.getChildren())
         {
            initializeDepthMap(kid, depth + 1);
         }
      }      
   }
   
   private Set<State> getAncestors(State terminal)
   {
      Set<State> ancestors = new HashSet<State>();
      State parent = terminal.getParent();
      
      ancestors.add(terminal);
      while(parent != null)
      {
         ancestors.add(parent);
         parent = parent.getParent();
      }
      return ancestors;
   }
   
   private class StateScore implements Comparable
   {
      public State state;
      public int score;
      public int compareTo(Object arg0)
      {
         assert arg0 instanceof StateScore;
         StateScore them = (StateScore) arg0;
         return score - them.score;
      }
      
 
   }
   
   @SuppressWarnings("unchecked")
   private State getLeastCommonAncestor(State fromstate, State tostate)
   {
      Set<State> fromancestors = getAncestors(fromstate);
      Set<State> toancestors = getAncestors(tostate);
      
      // do a set intersection
      fromancestors.retainAll(toancestors);
      Set<State> commonancestors = fromancestors;
      
      List<StateScore> scores = new Vector<StateScore>();
      
      for(State state : commonancestors)
      {
         StateScore score = new StateScore();
         score.state = state;
         score.score = getStateDepth(state);
         scores.add(score);         
      }
      if(scores.size() == 0)
      {
         return null;
      }
      return Collections.<StateScore>max(scores).state;
      
   }

   // end least common ancestor   
   
   private String indent()
   {
      StringBuffer sb = new StringBuffer();
      
      for(int i=0; i < indentcount; i++)
      {
        sb.append("   ");
      }

      return sb.toString();            
   }


   public KGStateMachineWriter(StateMachineXmiReader reader) throws Exception
   {
      this.reader = reader;
      this.executionTraceOn = Autocoder.isExecutionTraceOn();
      
      indentcount = 0;
   }
   

   public void writeAllStateMachines() throws Exception
   {
      Map<String, StateMachine> machines = reader.getStateMachineMap();
      Iterator i = machines.values().iterator();
      while (i.hasNext())
      {
         StateMachine nextStateMachine = (StateMachine) i.next();
         this.machine = nextStateMachine;
         writeStateMachine();
      }
   }

   private void writeStateMachine() throws Exception
   {
      allstates = new HashSet<State>();
      allsignals = new HashSet<String>();
           
      // find transitive closure of all states
      for(State s : machine.states())
      {
        addKids(s);       
      }
          
      // find all events
      
      // TODO, need to find events on states too
      for(Transition transition : machine.transitions() )
      {
         for(String signal : transition.getSignalNames())
         {
            allsignals.add(signal);
         }            
      }
      initializeDepthMap();
        
      indentcount = 0;      
      writeStateMachineH();
      indentcount = 0;
      writeStateMachineC();
      indentcount = 0;
      if(executionTraceOn)
      {
         generateApplicationFiles();
      }
      
      allstates.clear();
      allsignals.clear();
      
   }
   
   /**
    * Does a breath-first search of the state tree looking for the first initial
    * state.
    * 
    * @param stateTree
    *           Collection
    * @return State Found intial state, or <B>null</B> if not found in tree.
    */
   private State findInitialState(Collection stateTree)
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

   private void writeInitBody(List<State> states, String regionname) throws Exception
   {
      State initial = findInitialState(states);
      if(initial == null)
      {
         throw new Exception("No inital state found");
      }
      Transition initialTransition = (Transition) initial.getOutgoing().get(0);
//      State initialState = machine.getTargetState((initialTransition).id());

      if(initialTransition.guard() != null)
      {
         throw new Exception ("Cannot have a guard on an initial transition");
      }
      writeTakenTransition(initial, initialTransition, regionname);
      //writeEntryAction(initialState);     
      //cfile.println(indent() + "m->state = " + initialState.name() + ";");
      
      
   }
   
   private void writeStateMachineC() throws Exception
   {
     String filename = machine.name() + ".c";      

     cfile = new PrintStream(new FileOutputStream(filename));
     System.out.println("Writing statechart " + machine.name() + " to files "
           + filename );
     
     String statemachinetype = "Machine" + machine.name(); 
     
     cfile.println("#include \"" + machine.name() + ".h\"");
     cfile.println("#include \"" + machine.name() + "Impl.h\"");
     if (executionTraceOn)
     {
        cfile.println("#include \"log_event.h\"");
        cfile.println("#include <string.h>");
     }     
     cfile.println("");
     // write init
     cfile.println("extern void " + machine.name().toLowerCase() + "_init(" + statemachinetype + " * m) {");
     indentcount ++;
     if(executionTraceOn)
     {
        cfile.println(indent() + "char stateName[256];");
     }
     
     writeInitBody(machine.states(), "m->state");
     indentcount --;
     cfile.println("}");

     cfile.println();
     for(String signalname : allsignals)
     {
        writeEventFunction(signalname);
     }
     
     cfile.close();
   }
   
   
   private Transition findMatchingTransition(State state, String signalname)
   {
      SimpleState simplestate = (SimpleState) state;           
      List<Transition> transitions  = simplestate.getOutgoing();
      
      if(state instanceof CompositeStateRegion)
      {
         //??
         return null;
      }
      
      for(Transition transition : transitions)
      {
         for(String transitionsignal : transition.getSignalNames())
         {
            if(transitionsignal.equals(signalname))
            {
               return transition;
            }                  
         }
      }
      
      if(state.getParent() != null)
      {
         return findMatchingTransition(state.getParent(), signalname);
      }      
      return null;
   }
   
   private void writeRegionEventHandling(CompositeStateRegion region, String signalname) throws Exception
   {
      cfile.println(indent() + "switch(m->"+ region.name().toLowerCase() + ") {");
      
      List<State> nosubregions = new Vector<State>();
      
      getStatesNoRegions(region.getChildren(), nosubregions);
      for(State child : nosubregions)
      {
         indentcount ++;
         cfile.println(indent() + "case " +  child.name() + ":" );     
         indentcount++;
         Transition transition = findMatchingTransition(child, signalname);
         if(transition != null)
         {
            writeGuardedTransition(child, transition, "temp." + region.name().toLowerCase() );
         }                        
         
         cfile.println(indent() + "break;" );
         
         indentcount -= 2;         
      }
      indentcount ++;      
      cfile.println(indent() + "default:");
      indentcount ++;
      cfile.println(indent() + "report_unrecognizeable_state(m);");
      indentcount -= 2; 
      cfile.println(indent() + "}");      
   }
   
   private void writeEventFunction(String signalname) throws Exception
   {
      String statemachinetype = "Machine" + machine.name();      
      cfile.println("extern void " + machine.name().toLowerCase() + "_event_" +  signalname + "(" + statemachinetype + " * m) {");
      indentcount ++;      
      cfile.println(indent() + statemachinetype + " temp = *m;");
      if(executionTraceOn)
      {
         cfile.println(indent() + "char stateName[256];");
      }

      cfile.println();
      cfile.println(indent() + "switch(m->state) {");
      indentcount++;
      
      List<State> statesnoregions = new Vector<State>();
      
      getStatesNoRegions(machine.states(), statesnoregions);
      
      for(State state : statesnoregions)
      {
         if(isBottomState(state) || state instanceof ConcurrentCompositeState)
         {
            cfile.println(indent() + "case " + state.name() + ":");            

            ///
            Transition transition = findMatchingTransition(state, signalname);
            if(transition != null)
            {
               indentcount ++;
               writeGuardedTransition(state, transition, topregionname);
               indentcount --;
            }                        
            ///
            else if ( state instanceof ConcurrentCompositeState)
            {
               ConcurrentCompositeState concomp = (ConcurrentCompositeState) state;
               for(State regionstate: concomp.getAllSubRegions())
               {
                  CompositeStateRegion region = (CompositeStateRegion) regionstate;
                  indentcount ++;
                  writeRegionEventHandling(region, signalname);
                  indentcount --;
               }               
            }
            
            indentcount ++;
            cfile.println(indent() + "break;");
            indentcount --;
         }
      }
      cfile.println(indent() + "default:");
      indentcount ++;
      cfile.println(indent() + "report_unrecognizeable_state(m);");
      indentcount -= 2;
      cfile.println(indent() + "}");      
      cfile.println(indent() + "*m = temp;");
      indentcount --;
      cfile.println("}");      
      cfile.println();
   }
      
   private boolean isBottomState(State s)
   {
      return (s instanceof SimpleState)
         && !(s instanceof CompositeState)
         && !(s instanceof CompositeStateRegion);
   }
   
   private void writeGuardedTransition(State fromstate, Transition transition, String regionvariable) throws Exception
   {
      if(transition.guard() != null)
      {
         cfile.println(indent() + "if(" + transition.guard().methodName() + " ) {");                                               
         indentcount ++;
      }
      writeTakenTransition(fromstate, transition, regionvariable);

      if(transition.guard() != null)
      {
         indentcount --;
         cfile.println(indent() + "}");                        
      }      
   }
   
   private void writeJunctionTransition(State fromstate, Transition transition, JunctionState junction, String regionvariable) throws Exception
   {
      Transition elsetransition=null;
      boolean first = true;
      
      for(Transition outgoingtransition: junction.getOutgoing() )
      {
         if(outgoingtransition.guard() == null)
         {
            elsetransition = outgoingtransition;            
         }
      }
      
      for(Transition outgoingtransition: junction.getOutgoing() )
      {
         if(outgoingtransition == elsetransition)
         {
            cfile.println(indent() + "else {");
            indentcount ++;
            writeTakenTransition(fromstate, outgoingtransition, regionvariable);
            indentcount --;
            cfile.println(indent() + "}");            
            
         }
         else if(first)
         {            
            first = false;
            cfile.println(indent() + "if(" + outgoingtransition.guard().methodName() + " ) {");
            indentcount ++;
            writeTakenTransition(fromstate, outgoingtransition, regionvariable);
            indentcount --;
            cfile.println(indent() + "}");            
         }
         else
         {
            cfile.println(indent() + "else if(" + outgoingtransition.guard().methodName() + " ) {");
            indentcount ++;
            writeTakenTransition(fromstate, outgoingtransition, regionvariable);
            indentcount --;
            cfile.println(indent() + "}");                                    
         }         
      }      
   }
   
   private class AncestorChain
   {
      public List<State> fromancestors;
      public List<State> toancestors;
   }
   
   private AncestorChain getAncestorChain(State fromstate, State tostate)
   {
      AncestorChain chain = new AncestorChain();
      
      chain.fromancestors = new Vector<State>();
      chain.toancestors = new Vector<State>();
      
      
      State leastcommon = getLeastCommonAncestor(fromstate, tostate);
      
      State current;
      
      current = fromstate;
      
      while(current != null && current != leastcommon)
      {
         chain.fromancestors.add(current);
         current = current.getParent();
      }
      
      current = tostate;
      while(current != null && current != leastcommon)
      {
         chain.toancestors.add(current);
         current = current.getParent();
      }
            
      Collections.reverse(chain.toancestors);
      return chain;
   }
   
  private State findNonCompositeInitial(CompositeState target) throws Exception
  {
     Vector<State> states = new Vector<State>();
     states.add(target);
     State initialpseudo = findInitialState(states);
     Transition toinitial = initialpseudo.getOutgoing().get(0);
     
     State initial = machine.getTargetState(toinitial.id());
     
     if(initial instanceof CompositeState)
     {
        return findNonCompositeInitial((CompositeState) initial);
     }
     return initial;
  }
   
  private void writeExitFromRegion(CompositeStateRegion region)
  {
     cfile.println(indent() + "switch( m->" + region.name().toLowerCase() + ") {");
     indentcount ++;
     
     List<State> nosubregions = new Vector<State>();
     
     getStatesNoRegions(region.getChildren(), nosubregions);
     for(State child : nosubregions)
     {
        cfile.println(indent() + "case " + child.name() + ":");
        indentcount ++;
        // exit all the way up to the top of the region
        writeExitAction(child);
        State parent = child.getParent();
        while(parent != region && parent != null)
        { 
           writeExitAction(parent);
           parent = parent.getParent();   
        }        
        cfile.println(indent() + "break;");
        indentcount--;
     }     
     cfile.println(indent() + "default:");
     indentcount ++;
     cfile.println(indent() + "report_unrecognizeable_state(m);");    
     indentcount -= 2;
     cfile.println(indent() + "}");
     
  }
  
  /**@param regionvariable is the structure prefix that should be appended to any state assignments. 
   * Usually this would be changed for a different region 
   */ 
   private void writeTakenTransition(State fromstate, Transition transition, String regionvariable) throws Exception
   {      
      State target = machine.getTargetState(transition.id());
      State selfloopstate = null;
                
      if(target == null)
      {
         throw new Exception("Bad state target " + transition.id());
      }
      
      if(transition.sourceId().equals(transition.targetId())  && (! transition.isInternal() ))
      {
         selfloopstate = machine.getTargetState(transition.id() );
         System.out.println(selfloopstate.name() );
      }
      
      if(target instanceof JunctionState)
      {
         writeJunctionTransition(fromstate, transition, (JunctionState) target, regionvariable);
      }
      else
      {
         if(target instanceof CompositeState  && ! (target instanceof ConcurrentCompositeState)) 
         {
            target = findNonCompositeInitial((CompositeState) target);
            if(target == null)
            {
               throw new Exception("Can't find initial state in Composite State");
            }
         }
         
         // exit from the regions
         if(fromstate instanceof ConcurrentCompositeState)
         {
            ConcurrentCompositeState composite = (ConcurrentCompositeState) fromstate;
            for(State regionstate :  composite.getAllSubRegions() )
            {
               CompositeStateRegion region = (CompositeStateRegion) regionstate;
               writeExitFromRegion(region);
            }                                    
         }
        
         AncestorChain chain;
         if( selfloopstate != null)
         {
            chain = getAncestorChain(fromstate, selfloopstate);
         }
         else
         {
            chain = getAncestorChain(fromstate, target);             
         }
         
         
         if(! transition.isInternal() )
         {
            for(State state : chain.fromancestors)
            {
               writeExitAction(state);   
            }
         }                  
         if(selfloopstate != null)
         {                       
           writeExitAction(selfloopstate);
         }
                  
         for(Action action: transition.getActions())
         {
            writeAction(action);
         }
         
         if(selfloopstate != null)
         {
            writeEntryAction(selfloopstate);
            chain = getAncestorChain(selfloopstate, target);            
         }
         
         if(! transition.isInternal() )
         {
            for(State state : chain.toancestors)
            {
               writeEntryAction(state);   
            }
            
            cfile.println(indent() + regionvariable + " = " + target.name() + ";");            
         }
    
         
         // entry to the regions
         if(target instanceof ConcurrentCompositeState)
         {
            ConcurrentCompositeState composite = (ConcurrentCompositeState) target;
            for(State regionstate :  composite.getAllSubRegions() )
            {
               CompositeStateRegion region = (CompositeStateRegion) regionstate;
               writeInitBody(region.getChildren(), "temp." + region.name().toLowerCase() );
            }            
         }
      }

   }
      
   private void writeEntryAction(State state)
   {              
      if(state.getEntryActions() != null)
      {
         for(Action action: state.getEntryActions())
         {
            writeAction(action);           
         }
      }
      if (executionTraceOn && state instanceof SimpleState)
      {
         cfile.println(indent() + "strcpy(stateName, m->objName);");
         cfile.println(indent() + "strcat(stateName, \" " + state.name()
               + " ENTRY\");");
         cfile.println(indent() + "LogEvent_log(stateName);");        
      }
      
   }
   
   private void writeExitAction(State state)
   {
      if(state.getExitActions() != null)
      {
         for(Action action: state.getExitActions())
         {
            writeAction(action);           
         }
      }
      if (executionTraceOn && state instanceof SimpleState)
      {
         cfile.println(indent() + "strcpy(stateName, m->objName);");
         cfile.println(indent() + "strcat(stateName, \" " + state.name()
               + " EXIT\");");
         cfile.println(indent() + "LogEvent_log(stateName);");        
      }
      
   }
   
   private void writeAction(Action action)
   {
      if(action instanceof CallAction)
      {
         
         cfile.println(indent() + action.name().replaceAll("\"", "") + ";");
      }
   }
   
   /** gets all states, but does not descend into concurrent composite states. Does store the
    *  concurrent composite state itself, but not lower.
    * @param returnlist must be empty, and is where the results are stored */
   private void getStatesNoRegions(List<State> states, List<State> returnlist)
   {
      for(State topstate: states)
      {
         if(topstate instanceof CompositeState && !(topstate instanceof ConcurrentCompositeState) )
         {
            CompositeState comp = (CompositeState) topstate;
            getStatesNoRegions(comp.getChildren(), returnlist);   
         }
         else if(topstate instanceof ConcurrentCompositeState)
         {
            returnlist.add(topstate);                      
         }
         else if(isBottomState(topstate))
         {
            returnlist.add(topstate);
         }
      }      
   }
   
   private void writeStateEnums(List<State> states, String name)
   {
      
      headerfile.println("typedef enum " + name.toLowerCase() + "_states {");
      boolean first = true;
      indentcount ++;
      
      for(State s: states)
      {
            if(!first)
               headerfile.println(",");         
            headerfile.print(indent() + s.name());         
            first = false;
      }            
      headerfile.println("");
      headerfile.println("} " + name + "States;");
      headerfile.println();
      indentcount --;
   }
   
   private void writeRegionEnums(CompositeStateRegion region)
   {
      List<State> nosubregions = new Vector<State>();
      
      getStatesNoRegions(region.getChildren(), nosubregions);
      writeStateEnums(nosubregions, machine.name() + region.name());
   }
   
   private void writeStateMachineH() throws Exception
   {
	  String filename = machine.name() + ".h";
	  headerfile = new PrintStream(new FileOutputStream(filename));
      System.out.println("Writing statechart " + machine.name() + " to files "
            + filename );
      
      String upper = machine.name().toUpperCase();
      headerfile.println("#ifndef " + upper + "_H" );
      headerfile.println("#define " + upper + "_H");
      headerfile.println();
      
      // write region enums
      for(State state : allstates)
      {
         if(state instanceof ConcurrentCompositeState)
         {
            ConcurrentCompositeState concurrent = (ConcurrentCompositeState) state;
            for(State region : concurrent.getAllSubRegions() )
            {
               CompositeStateRegion csregion = (CompositeStateRegion) region;
               writeRegionEnums(csregion);
            }
         }
      }
      
      // write the state enums
      List<State> stateswithoutregions = new Vector<State>();
      getStatesNoRegions(machine.states(), stateswithoutregions);

      writeStateEnums(stateswithoutregions, machine.name());
      
           
      // struct
      headerfile.println("typedef struct " + machine.name().toLowerCase() + "_machine {");
      indentcount ++;
      headerfile.println(indent() + machine.name() + "States state;");
      String statemachinetype = "Machine" + machine.name();
      
      // storage for all regions too
      
      for(State state : allstates)
      {
         if(state instanceof ConcurrentCompositeState)
         {
            ConcurrentCompositeState concurrent = (ConcurrentCompositeState) state;
            for(State region : concurrent.getAllSubRegions() )
            {
               CompositeStateRegion csregion = (CompositeStateRegion) region;
               headerfile.println(indent() + machine.name() + csregion.name() + "States " + csregion.name().toLowerCase() + ";");
            }
         }
      }
      if(executionTraceOn)
      {
         headerfile.println(indent() + "char objName[256];");
      }
      
      headerfile.println("} "+  statemachinetype + ";");
      indentcount --;
      
      headerfile.println();
      // prototypes
      headerfile.println("extern void " + machine.name().toLowerCase() + "_init("+ statemachinetype +   " * m);");
      
      for(String eventname : allsignals)
      {
         headerfile.println("extern void " + machine.name().toLowerCase() + "_event_" + eventname+ "("+ statemachinetype +   " * m);");         
      }
      
      headerfile.println("extern void report_unrecognizeable_state(" + statemachinetype + " * m);" );
      
      headerfile.println();      
      headerfile.println("#endif");
      
      headerfile.close();
   }
   
   private void addKids(State s)
   {
      if(s instanceof InitialState)
      {
         return;
      }
      allstates.add(s);
      if(s instanceof CompositeState)
      {
         CompositeState composite = (CompositeState) s;
         for(State kid : composite.getChildren())
         {
            addKids(kid);
         }                 
      }
   }

   private void generateApplicationFiles() throws Exception
   {     
      PrintStream appcfile = new PrintStream(new FileOutputStream("application.c"));
      
      String filename = machine.name() + ".h";
      appcfile.println("#include <stdio.h>");
      appcfile.println("#include <string.h>");
      appcfile.println("#include \"" +  filename  + "\"");
String header = "void testmachine()\n" + 
"{\n" +
"    MachineSMM m;\n"+
"    int exited;\n"+
"    char buffer[256];\n"+
"\n"    +
"    strcpy(m.objName, \"Test1\");\n"+
"\n"+ 
"    printf(\"Commands are:\\n\");\n";

      appcfile.println(header.replaceAll("SMM", machine.name() ));
      for(String signal : allsignals)
      {
        appcfile.println("    printf(\""+ signal +  "\\n\");");         
      }
      appcfile.println("    printf(\"exit\\n\");");
      appcfile.println("    "+ machine.name().toLowerCase()+ "_init(&m);");
      
String middle ="    exited = 0;\n" +  
"\n" +      
"    while(! exited)\n" +  
"    {\n" +  
"        printf(\">\");\n" +  
"        scanf(\"%s\", buffer);\n" +  
"        if( strcmp(buffer, \"exit\") == 0)\n" +
"        {\n" + 
"           exited = 1;\n" + 
"        }\n" ;

      appcfile.println(middle);
String elseif = "        else if( strcmp(buffer, \"EEVENT\") == 0)\n" + 
"        {\n" + 
"           MMACHINE_event_EEVENT(&m);\n" +      
"        }";

      for(String signal : allsignals)
      {
         String elseifnew = elseif.replaceAll("EEVENT", signal);
         elseifnew = elseifnew.replaceAll("MMACHINE", machine.name().toLowerCase());
         appcfile.println(elseifnew);
      }
String elsepart =      "        else\n" +
      "        {\n" + 
      "           printf(\"Invalid command\\n\");\n" + 
      "        }\n" ;
      appcfile.println(elsepart);
      appcfile.println("    }\n" );
      appcfile.println("}");
      appcfile.close();
   }
   
   
   public static void main(String[] args)
   {
      try
      {
//         System.setProperty("jpl.autocode.c", "true");
         // Create the UML reader object which is used for the SIM RTC project:
         MagicDrawUmlReader reader = new MagicDrawUmlReader();
         // Parse the XML files:
         reader.parseXmlFiles(args);
         // Create the C implementations of the state machines found in the
         // XML files:
         new KGStateMachineWriter(reader).writeAllStateMachines();
         
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

}
