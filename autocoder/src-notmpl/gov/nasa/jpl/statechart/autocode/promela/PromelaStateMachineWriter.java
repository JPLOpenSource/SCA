package gov.nasa.jpl.statechart.autocode.promela;

import gov.nasa.jpl.statechart.core.Action;
import gov.nasa.jpl.statechart.core.CallAction;
import gov.nasa.jpl.statechart.core.CompositeState;
import gov.nasa.jpl.statechart.core.EventAction;
import gov.nasa.jpl.statechart.core.InitialState;
import gov.nasa.jpl.statechart.core.JunctionState;
import gov.nasa.jpl.statechart.core.SimpleState;
import gov.nasa.jpl.statechart.core.State;
import gov.nasa.jpl.statechart.core.StateMachine;
import gov.nasa.jpl.statechart.core.Transition;
import gov.nasa.jpl.statechart.input.StateMachineXmiReader;
import gov.nasa.jpl.statechart.input.magicdraw.MagicDrawUmlReader;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/** @author Eddie Benowitz */

public class PromelaStateMachineWriter
{

   private StateMachine          machine;
   private StateMachineXmiReader reader;
   private PrintStream           promelafile;
   private Set<State> allstates;
   private Set<EventAction> alleventactions;
   private Set<CallAction> allcallactions;
   private Set<String> allpublishedevents;


   public PromelaStateMachineWriter(StateMachineXmiReader reader) throws Exception
   {
      this.reader = reader;
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
	  String filename = machine.name() + ".pml";
	  promelafile = new PrintStream(new FileOutputStream(filename));
      System.out.println("Writing statechart " + machine.name() + " to files "
            + filename );
      
      allstates = new HashSet<State>();
      alleventactions = new HashSet<EventAction>();
      allcallactions = new HashSet<CallAction>();
      allpublishedevents = new HashSet<String>();
      
      
      // find transitive closure of all states
      for(State s : machine.states())
      {
    	  addKids(s);    	  
      }
      
      // compute the transitive closure of all action events
      for(Transition transition : machine.transitions())
      {
    	  for(Action action: transition.getActions())
    	  {
    		  if(action instanceof EventAction)
    		  {
    			  alleventactions.add((EventAction) action);
    		  }
    	  }
      }
      
      for(State s : machine.states())
      {
    	  addActionEvents(s);
      }
      // computation of action events done
      
      // find all published events
      for(Transition transition: machine.transitions())
      {
    	  allpublishedevents.add(transition.signalName());
      }
      
      
      promelafile.println("#include \"" + machine.name()  +"_actions.h\"");
      writeReachabilityDefine();
      promelafile.println();
      writeReachibilityBooleans();
      promelafile.println();
      writeInstatiateStateMachines();
      for(State s : allstates)
      {
          promelafile.println();    	  
    	  writeEntryRoutines(s);
      }
      
      promelafile.println();
      for(EventAction actionevent : alleventactions)
      {
    	  writeActionEvents(actionevent);
          promelafile.println();
          promelafile.println();
      }      
      
      for(CallAction callaction : allcallactions)
      {
    	  writeCallAction(callaction);
          promelafile.println();    	  
      }
      
      writeProcessActions();
      promelafile.println();
      writeMainProc();
      writeXecProcess();
      
      writeActionsFile();
      
      allstates.clear();
      alleventactions.clear();
      promelafile.close();
   }

   private void writeMainProc()
   {
	   String mainheader = 
"proctype SMM_proc()" + "\n" +
"{" + "\n" +
"     int act;" + "\n" +
"     int i;" + "\n" +
"     int SMId;" + "\n" + // don't replace SM here
"     int event;" + "\n\n" +     
"     d_step {" + "\n" +
"     // Initialize the statemachine" + "\n" +
"     c_code"  + "\n" +
"     {" + "\n" +
"       SMM_Constructor(&SMM_);" + "\n" +
"       QHsmInit((QHsm *)&SMM_, 0);" + "\n" +
"     };" + "\n" +
"     SMM_ProcessActions(i, act)" + "\n" +
"     }" + "\n" +
"     // Subscribe to Events";
	   
	   mainheader = mainheader.replaceAll("SMM", machine.name());		   
	   promelafile.println(mainheader);
	   for(String eventname : allpublishedevents)
	   {
		   if(eventname != null)
			   promelafile.println("     subscribe(" + machine.name() + "_id, "+ eventname  +   ");");		   
	   }
      // subscribe to timers
      writeTimerRegistration(machine);
      
String footer=
"     // Construction complete, send message" + "\n" + 
"     initQ!true;" + "\n" + 
"        do" + "\n" + 
"        :: evQ[SM_id]?event ->" + "\n" + 
"           atomic {" + "\n" + 
"             DISPATCH(SM_);" + "\n" + 
"             SM_ProcessActions(i, act)" + "\n" + 
"           }" + "\n" + 
"        od;" + "\n" + 
"}";
	footer = footer.replaceAll("SM", machine.name());
	promelafile.println(footer);
	   
   }
   
   private void writeProcessActions()
   {
	   String header = 
"inline SM_ProcessActions(i, act) {" + "\n" + 
"" + "\n" + 
"  i = 0;" + "\n" + 
"" + "\n" + 
"  do" + "\n" + 
"    :: i < ACTION_LIST_SIZE ->" + "\n" +  
"       c_code {PSM_proc->act = SM_.super_.actions[PSM_proc->i];};" + "\n" + 
"       if"+ "\n" ;
		   
	   
	   String footer = 
"        :: act == 0 -> break" + "\n" +
"        :: else -> assert(0)" + "\n" +
"       fi;" + "\n" +
"       i++" + "\n" +
"    :: else -> break" + "\n" +
"  od; skip" + "\n" +
"}";
		   
		   
	   header = header.replaceAll("SM", machine.name());
	   
	   promelafile.println(header);
	   String stateprototype = "        :: act == SMCAPS_STATECAPS_ENTRY  -> SMLOWER_STATELOWEREntry()";	 
	   String eventprototype = "        :: act == SEND_EVENTNAME  -> SMLOWER_SendEVENTNAME()";
	   //ine " + "SEND_" + callevent.name.replaceAll("\\(\\)", "")
	   String callprototype =  "        :: act == SEND_CALLNAME  -> SMLOWER_CALLNAME()";
	   
	   for(State state: allstates)
	   {
		   String text = stateprototype.replaceAll("SMCAPS", machine.name().toUpperCase());
		   text = text.replaceAll("SMLOWER", machine.name());
		   text = text.replaceAll("STATECAPS", state.name().toUpperCase());
		   text = text.replaceAll("STATELOWER", state.name() );
		   promelafile.println(text);
	   }
      
	   for(EventAction actionevent : alleventactions)
	   {
		   String text = eventprototype.replaceAll("SMLOWER", machine.name());
		   text = text.replaceAll("EVENTNAME", actionevent.name());
		   promelafile.println(text);
	   }
/** Garth wanted these removed      
	   for(CallAction callaction : allcallactions)
	   {
		   String text = callprototype.replaceAll("SMLOWER", machine.name());
		   text = text.replaceAll("CALLNAME", callaction.name.replaceAll("\\(\\)", ""));
		   promelafile.println(text);
		   
	   }
**/      
	   
	   promelafile.println(footer);
	   
   }
   
   private void writeActionsFile()
   {
	   int count =0;
	   try
	   {
		   PrintStream actionsfile = new PrintStream(new FileOutputStream(machine.name() + "_actions.h"));	   
	   
		   for(State state: allstates)
		   {
			   count++;
			   
			   actionsfile.println("#define " + machine.name().toUpperCase() 
					   + "_" + state.name().toUpperCase() + "_ENTRY" + " " + count + "");
		   
		   }

         
		   for(EventAction actionevent : alleventactions)
		   {
			   count++;			   		   
			   actionsfile.println("#define " + "SEND_" + actionevent.name() + " " + count + "");			   			   
		   }
/**		   
// Garth asked that we remove non-entry actions
 		   for(CallAction callevent: allcallactions)
		   {
			   count++;			   
			   //SEND_set_EW_Green
			   actionsfile.println("#define " + "SEND_" + callevent.name.replaceAll("\\(\\)", "")
					   + " " + count + "");
		   }
**/         
	   }
	   catch(FileNotFoundException fne)
	   {
		   System.out.println("Error writing actions file.");
	   }	      
   }
     
   private void writeReachabilityDefine()
   {
	   promelafile.print("#define " + machine.name().toUpperCase() + "_REACHABILITY (");
	   boolean first = true;
	   
	   for(State s : allstates)
	   {
		   if(!first)
		   {
			   promelafile.print(" && ");
		   }
		   promelafile.print("Entered_" + machine.name() + "_" + s.name() + " ");
		   first = false;
	   }
	   promelafile.println(")");
   }
   
   private void writeEntryRoutines(State s)
   {
	   String text = 
	"inline SM_STATENAMEEntry() {" + "\n" + 
	"       printf(\"***SM_proc STATENAME Entry\\n\");" + "\n" +
	"       SET_STATE(SM_, SM_STATENAME);" + "\n" +
	"       Entered_SM_STATENAME = true" + "\n" +
	"}" + "\n";
	   text = text.replace("SM", machine.name());
	   text = text.replace("STATENAME", s.name());
	   promelafile.print(text);
   }
   
   private void writeReachibilityBooleans()
   {
	   for(State s : allstates)
	   {
		   promelafile.println("bool Entered_" + machine.name() + "_" + s.name() + ";");
	   }	   
   }
   
   private void addActionEvents(State s)
   {
	   if(s instanceof InitialState || s instanceof JunctionState)
	   {
		   return;
	   }
	   //
	   for(Action action : s.getDuringActions())
	   {
 		  if(action instanceof EventAction)
		  {
			  alleventactions.add((EventAction) action);
		  }		   
 		  else if(action instanceof CallAction)
 		  {
 			  allcallactions.add((CallAction) action); 			  
 		  }
	   }
	   for(Action action : s.getEntryActions())
	   {
 		  if(action instanceof EventAction)
		  {
			  alleventactions.add((EventAction) action);
		  }
 		  else if(action instanceof CallAction)
 		  {
 			  allcallactions.add((CallAction) action); 			  
 		  }
 		  
	   }
	   for(Action action : s.getExitActions())
	   {
 		  if(action instanceof EventAction)
		  {
			  alleventactions.add((EventAction) action);
		  }
 		  else if(action instanceof CallAction)
 		  {
 			  allcallactions.add((CallAction) action); 			  
 		  }
 		  
	   }
	   
	   //now check children states
	   if(s instanceof CompositeState)
	   {
		   CompositeState composite = (CompositeState) s;
		   for(State kid : composite.getChildren())
		   {
			   addActionEvents(kid);
		   }		   		   
	   }	  	   
   }
   
   private void writeActionEvents(EventAction event)
   {
	   String text = 
"inline SMM_SendEVENT() {"  + "\n" + 
/* "  printf(\"***SMM_proc Send EVENT\\n\");" + "\n" + */  
"  publish(SMId, EVENT)" + "\n" +   // don't change this SM
"}";
	   text = text.replace("SMM", machine.name());
	   text = text.replace("EVENT", event.name());
	   promelafile.print(text);
	   
   }
   
   private void writeCallAction(CallAction callaction)
   {
//	   inline TrafficLight_setAllRed() 
//	   {
//		   printf("***TrafficLight_proc setAllRed\n");
//	   }

      
// Garth asked that we not generate this
      
/*      
String text = 
	"inline SMM_CALLNAME{"   + "\n" + 
    "  printf(\"***SMM_proc CALLNAME\\n\");" +"\n" +
    "}\n";

	text = text.replace("SMM", machine.name());
	text = text.replace("CALLNAME", callaction.name);
	
    promelafile.print(text);
*/	
   }
   
   private void addKids(State s)
   {
	   if(s instanceof InitialState || s instanceof JunctionState)
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

   private void writeInstatiateStateMachines()
   {
	   String text = 
	   "//	 Instantiate each statemachine" + "\n" +  
	   "c_decl" + "\n" + 
	   "{" + "\n" + 
	   "  SM SM_;" + "\n" + 
	   "};" + "\n" + 
	   "c_track \"&SM_\" \"sizeof(SM)\";";
	   
	   text = text.replace("SM", machine.name());
	   promelafile.println(text);
	   
	}
   
   private void writeXecProcess()
   {
/////////////
      String header = "\n" +  
"proctype SMNAME_Xec_proc()" + "\n" +
"{"+ "\n" +
"int SMId;"+ "\n" +
"\n"+ "\n" +
"      do"+ "\n" +
"       // Block where all state-machines are waiting on the during event" + "\n"  
;

      
      String footer = 
"                :: else -> skip" + "\n" +
"      od" + "\n" +
"}" + "\n" ;      
      
////////      
      
      header = header.replace("SMNAME", machine.name());
      promelafile.println(header);
      for(State state : machine.states() )
      {
         writeCheckState(state);
      }      
      promelafile.println(footer);
      
   }
   
   private void writeCheckState(State state)
   {
      String template = 
"                :: CHECK_STATE(SMNAME_, SMNAME_STATENAME) -> publish(SMId, EVENTNAME)";         
      
      if(! (state instanceof SimpleState))
         return;
      
      
      SimpleState simplestate = (SimpleState) state;
      for(Transition transition : simplestate.getOutgoing() )
      {
         for(String signalname : transition.getSignalNames() )
         {
            String text = template.replaceAll("SMNAME", machine.name());
            text = text.replaceAll("STATENAME", state.name() );
            text = text.replaceAll("EVENTNAME", signalname );
            promelafile.println(text);
         }
         if(transition.timeout() != null)
         {
            String text = template.replaceAll("SMNAME", machine.name());
            text = text.replaceAll("STATENAME", state.name() );
            text = text.replaceAll("EVENTNAME", machine.name().toUpperCase() + "_" + state.name().toUpperCase() + "_TIMEOUT" );
            promelafile.println(text);
            
         }
      }
      
      if(state instanceof CompositeState)
      {
         CompositeState composite = (CompositeState) state;
         for(State child : composite.getChildren() )
         {
            writeCheckState(child);
         }
      }
   }
   
   private void writeTimerRegistration(StateMachine machine, State state)
   {
      if(! (state instanceof SimpleState) )
      {
         return;
      }
      SimpleState simplestate = (SimpleState) state;
      
      for(Transition transition : simplestate.getOutgoing())
      {
         if(transition.timeout() != null)
         {
            String eventname = machine.name().toUpperCase() +
            "_" + state.name().toUpperCase() + "_" + "TIMEOUT";
            promelafile.println("     subscribe(" + machine.name() + "_id, "+ eventname  +   ");");            
         }
      }
      
      if(state instanceof CompositeState)
      {
         CompositeState composite = (CompositeState) state;
         for(State child : composite.getChildren())
         {
            writeTimerRegistration(machine, child);
         }                           
      }            
   }
   
   private void writeTimerRegistration(StateMachine machine)
   {
      for(State state : machine.states())
      {
         writeTimerRegistration(machine, state);
      }
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
         new PromelaStateMachineWriter(reader).writeAllStateMachines();
         System.out.println("Finished.");
      } catch (Exception e)
      {
         e.printStackTrace(System.err);
      }
   }

}
