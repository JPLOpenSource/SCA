package gov.nasa.jpl.statechart.autocode.cm;

import gov.nasa.jpl.statechart.core.Action;
import gov.nasa.jpl.statechart.core.CallAction;
import gov.nasa.jpl.statechart.core.CompositeState;
import gov.nasa.jpl.statechart.core.EventAction;
import gov.nasa.jpl.statechart.core.InitialState;
import gov.nasa.jpl.statechart.core.SimpleState;
import gov.nasa.jpl.statechart.core.State;
import gov.nasa.jpl.statechart.core.StateMachine;
import gov.nasa.jpl.statechart.core.Transition;
import gov.nasa.jpl.statechart.core.TransitionGuard;
import gov.nasa.jpl.statechart.input.StateMachineXmiReader;
import gov.nasa.jpl.statechart.input.magicdraw.MagicDrawUmlReader;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/** @author Eddie Benowitz */

public class SimplestStateMachineWriter
{

    //private StateMachine          machine;
    private StateMachineXmiReader reader;
    private PrintStream           headerfile;
    private PrintStream           cfile;
    private Set<State> allstates;
    private Set<EventAction> alleventactions;
    private Set<CallAction> allcallactions;
    private Set<String> allpublishedevents;
    private String indent;


    public SimplestStateMachineWriter(StateMachineXmiReader reader) throws Exception
    {
        this.reader = reader;      
    }
   

    public void writeAllStateMachines() throws Exception
    {
        Map<String, StateMachine> machines = reader.getStateMachineMap();

        for ( StateMachine stateMachine : machines.values() )
        {
            writeStateMachine( stateMachine );
        }

        /*
          Iterator i = machines.values().iterator();
          while (i.hasNext())
          {
          StateMachine nextStateMachine = (StateMachine) i.next();
          this.machine = nextStateMachine;
          writeStateMachine();
          }
        */
    }

    private void writeStateMachine( StateMachine machine ) throws Exception
    {
        allstates = new HashSet<State>();
        alleventactions = new HashSet<EventAction>();
        allcallactions = new HashSet<CallAction>();
        allpublishedevents = new HashSet<String>();
      
      
        // find transitive closure of all states
        for(State s : machine.states())
        {
            allstates.add(s);       
        }
          
        // find all published events
        for(Transition transition: machine.transitions())
        {
            allpublishedevents.add(transition.signalName());
        }
         
      
        writeStateMachineH( machine );
        writeStateMachineC( machine );    
      
        allstates.clear();
        alleventactions.clear();      
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

    private void writeStateMachineC( StateMachine machine ) throws Exception
    {
        String filename = machine.name() + ".c";      

        cfile = new PrintStream(new FileOutputStream(filename));
        System.out.println("Writing statechart " + machine.name() + " to files "
                           + filename );
     
        cfile.println("#include \"" + machine.name() + ".h\"");
        cfile.println("");
        // write init
        cfile.println("void " + machine.name() + "_init(" + machine.name() + " * me)");
        cfile.println("{");
        State initial = findInitialState(machine.states());
        if(initial == null)
        {
            throw new Exception("No inital state found");
        }
        State initialState = machine.getTargetState(((Transition) initial
                                                     .getOutgoing().get(0)).id());

        indent = "   ";
     
        cfile.println(indent + "me->state = " + initialState.name() + ";");
        writeEntryAction(initialState);
        cfile.println("}");

     
        // write each state's event handling
        for(State state: allstates)
        {
            if(state instanceof SimpleState)
            {
                cfile.println("void " + machine.name()  + "_dispatch"+ state.name() + "(" + machine.name() + " * me, "+ machine.name() + "Events event)");
                cfile.println("{");
                writeOneStateBody(machine, state);
                cfile.println("}");
                cfile.println();
            }
        }
     
        // write dispatch
        cfile.println("void " + machine.name() + "_dispatch(" + machine.name() + " * me, "+ machine.name() + "Events event)");
        cfile.println("{");
        cfile.println(indent + "switch(me->state)");
        cfile.println(indent + "{");     
        indent = indent + "   ";
        for(State s: allstates)
        {
            if(s instanceof SimpleState)
            {
                cfile.println(indent + "case " + s.name()+ ":");
                String oldindent = indent;
                indent += "   ";
                cfile.println(indent +  machine.name()  + "_dispatch" + s.name() + "(me, event); ");
                cfile.println(indent +  "break;");
                indent = oldindent;
            }
        }
     
 
     
     
        indent = "   ";
        cfile.println(indent + "};");
        cfile.println("}");     
     
        cfile.close();
    }
   
    private void writeOneStateBody(StateMachine machine, State state) throws Exception
    {
        String oldindent = indent;
        if(state.getOutgoing() != null)
        {
            cfile.println(indent + "switch(event)");         
            cfile.println(indent + "{");
            for(Transition transition: state.getOutgoing())
            {
                for(String signal : transition.getSignalNames())
                {
                    String oldindent2 = indent;
                    indent += "   ";
                    cfile.println(indent + "case " + signal + ":");
                    indent += "   ";               
                    if(transition.guard() != null)
                    {
                        TransitionGuard guard = transition.guard();
                        cfile.println(indent + "if(" + guard.methodName() + ")");
                        cfile.println(indent + "{");
                        String oldindent3 = indent;
                        indent += "   ";
                        writeTakenTransition(machine, state, transition);
                  
                        indent = oldindent3;
                        cfile.println(indent + "}");
                    }
                    else
                    {
                        writeTakenTransition(machine, state, transition);
                    }
                    cfile.println(indent + "break;");
                    indent = oldindent2;
                }
            }
            cfile.println(indent + "}");    
        }
        indent = oldindent;
    }
   
    void writeTakenTransition(StateMachine machine, State state, Transition transition) throws Exception
    {
        writeExitAction(state);

        for(Action action: transition.getActions())
        {
            writeAction(action);
        }

        State target = machine.getTargetState(transition.id());
        if(target == null)
        {
            throw new Exception("Bad state target " + transition.id());
        }

        cfile.println(indent + "me->state = " + target.name() + ";");
        writeEntryAction(target);
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
    }
   
    private void writeAction(Action action)
    {
        if(action instanceof CallAction)
        {
            cfile.println(indent + action.name() + ";");
        }
    }
   
    private void writeStateMachineH( StateMachine machine ) throws Exception
    {
        // Name of the header file
        String filename = machine.name() + ".h";

        headerfile = new PrintStream(new FileOutputStream(filename));
        System.out.println("Writing statechart " + machine.name() + " to files "
                           + filename );
      
        String upper = machine.name().toUpperCase();
        headerfile.println("#ifndef " + upper + "_H" );
        headerfile.println("#define " + upper + "_H");
        headerfile.println();
        // write the state enums
        headerfile.println("enum " + machine.name() + "States {");
        boolean first = true;
        indent = "   ";
        for(State s: allstates)
        {
            if(s instanceof SimpleState)
            {
                if(!first)
                    headerfile.println(",");         
                headerfile.print(indent + s.name());         
                first = false;
            }
        }            
        headerfile.println("");
        headerfile.println("};");
        headerfile.println();
        // write event enums, may want to take this out
        headerfile.println("enum " + machine.name() + "Events {");
        first = true;
        indent = "   ";
        for(String transitionname: allpublishedevents)
        {
            if(transitionname != null)
            {
                if(!first)
                    headerfile.println(",");         
                headerfile.print(indent + transitionname);         
                first = false;
            }
        }               
        headerfile.println("");
        headerfile.println("};");
      
        headerfile.println();
        // struct
        headerfile.println("typedef struct " + machine.name() + "_s {");
        headerfile.println(indent + machine.name() + "States state;");      
        headerfile.println("} " + machine.name() + ";");
      
        headerfile.println();
        // prototypes
        headerfile.println("void " + machine.name() + "_init(" + machine.name() + " * me);");
        headerfile.println("void " + machine.name() + "_dispatch(" + machine.name() + " * me, " + machine.name() + "Events event);");
        headerfile.println();      
        headerfile.println("#endif");
      
        headerfile.close();
    }
   
    public static void main(String[] args)
    {
        try
        {
            // System.setProperty("jpl.autocode.c", "true");
            // Create the UML reader object which is used for the SIM RTC project:
            MagicDrawUmlReader reader = new MagicDrawUmlReader();
            // Parse the XML files:
            reader.parseXmlFiles(args);
            // Create the C implementations of the state machines found in the
            // XML files:
            new SimplestStateMachineWriter(reader).writeAllStateMachines();
            System.out.println("Finished.");
        } catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

}
