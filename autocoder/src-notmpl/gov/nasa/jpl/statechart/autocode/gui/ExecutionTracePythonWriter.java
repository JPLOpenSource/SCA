package gov.nasa.jpl.statechart.autocode.gui;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.autocode.IOldWriter;
import gov.nasa.jpl.statechart.autocode.IGenerator.Kind;
import gov.nasa.jpl.statechart.autocode.cppSIM.SimRtcSignalWriter;
import gov.nasa.jpl.statechart.core.CompositeState;
import gov.nasa.jpl.statechart.core.CompositeStateRegion;
import gov.nasa.jpl.statechart.core.ConcurrentCompositeState;
import gov.nasa.jpl.statechart.core.DiagramElement;
import gov.nasa.jpl.statechart.core.DiagramStateElement;
import gov.nasa.jpl.statechart.core.DiagramTextElement;
import gov.nasa.jpl.statechart.core.SimpleState;
import gov.nasa.jpl.statechart.core.State;
import gov.nasa.jpl.statechart.core.StateMachine;
import gov.nasa.jpl.statechart.core.Transition;
import gov.nasa.jpl.statechart.input.StateMachineXmiReader;
import gov.nasa.jpl.statechart.template.TargetLanguageMapper;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * <p>
 * This class creates Python-based GUI files for state machines. An internal
 * representation of state charts, in the form of StateMachine, State, and
 * Transition objects, is used to write an <i>execution trace file</i> which
 * implements an animated GUI display of the various elements of the state
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
 * CVS Identification: $Id: ExecutionTracePythonWriter.java,v 1.4 2008/10/01 21:56:15 wkiri Exp $
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
public class ExecutionTracePythonWriter implements IOldWriter
{
   private StateMachineXmiReader reader;
   private StateMachine          machine;
   private PrintStream           executionTraceFile;
   private PrintStream           signalTraceFile;
   private String                classname;
   private String                indent = "";
   static int                    Q_USER_SIG_VALUE;

   /**
    * @note The following constant must match the integer value of the
    *       Q_USER_SIG C++ enumerated value declared in qevent.h
    */
   static
   {
      if (Autocoder.autocodingTarget() == Kind.C || Autocoder.autocodingTarget() == Kind.CNonTemplate)
      {
         Q_USER_SIG_VALUE = 4;
      } else
      {
         Q_USER_SIG_VALUE = 6;
      }
   }

   public ExecutionTracePythonWriter (StateMachineXmiReader reader)
   {
      this.reader = reader;
   }

   /* (non-Javadoc)
    * @see gov.nasa.jpl.statechart.autocode.IWriter#write()
    */
   public void write () {
       // output trace file for the state machines
       try {
           writeAllStateMachineTraceFiles();
       } catch (Exception e) {
           e.printStackTrace();
       }

//       // output GUI signal trace file
//       try {
//           writeSignalTraceFile();
//       } catch (Exception e) {
//           e.printStackTrace();
//       }
   }

/**
    * Writes out the Python GUI execution trace file for the set of statechart
    * signals contained in this instance's reader object;
    * 
    * @throws Exception
    */
   public void writeSignalTraceFile() throws Exception
   {
      writeTraceFileHeader();
      // Write out python for each signal, with the C++ enumerated values
      // starting at the following value.
      int enumIntValue = Q_USER_SIG_VALUE;
      // Write out the "during" signal, which is part of the SIM's version of
      // the Quantum Framework architecture:
      writeSignalDiagramElement(enumIntValue++, SimRtcSignalWriter.duringName);
      // Get the set of signal names found in the complete set of input XML
      // files:
      HashMap<String, String> signalMap = reader.getSignalMap();
      // Sort the signal list alphabetically, and iterate through the list:
      TreeSet<String> sortedSignals = new TreeSet<String>(signalMap.keySet());
      Iterator signalNameIter = sortedSignals.iterator();
      while (signalNameIter.hasNext())
      {
         // Write out Python code for this signal:
         String sigName = (String) signalNameIter.next();
         if (!sigName.contains("|"))
         {
            writeSignalDiagramElement(enumIntValue++, sigName);
         }
      }
      writeTraceFileFooter();
   }

   /**
    * Writes out the Python GUI trace files for the set of state machines
    * contained in this instance's reader object.
    * 
    * @throws Exception
    */
   public void writeAllStateMachineTraceFiles() throws Exception
   {
      Map<String, StateMachine> machines = reader.getStateMachineMap();
      Iterator i = machines.values().iterator();
      while (i.hasNext())
      {
         StateMachine nextStateMachine = (StateMachine) i.next();
         this.machine = nextStateMachine;
         this.classname = TargetLanguageMapper.sanitize(nextStateMachine.name());
         writeExecutionTraceFile();
      }
   }

   /**
    * Creates and writes out the state machine Python GUI code used for
    * execution tracing.
    * 
    * @throws Exception
    */
   private void writeExecutionTraceFile() throws Exception
   {
      // Open a new Python file for this state machine:
      String executionTraceFilename = classname + ".py";
      System.out.println("Writing execution trace file "
            + executionTraceFilename + "...");
      executionTraceFile = new PrintStream(new FileOutputStream(
            executionTraceFilename));
      indent = "";

      // Write out header stuff:
      executionTraceFile.println(indent + "#!/tps/bin/python");
      executionTraceFile.println(indent
            + "# GUI application to trace the execution of state machine \""
            + TargetLanguageMapper.sanitize(machine.name()) + "\".");
      executionTraceFile.println(indent + "#");
      executionTraceFile.println(indent + "from Tkinter import *");
      executionTraceFile.println("");
      // for kiri's development machine 
      executionTraceFile.println(indent + "if (sys.hexversion <= 0x010502f0):");
      executionTraceFile.println(indent + "    " + "FIRST = 'first'");
      executionTraceFile.println("");
      // end for kiri
      
      executionTraceFile.println(indent + "class " + TargetLanguageMapper.sanitize(machine.name()) + ":");
      indent = "  ";
      
      if (machine.canvasSize() == null
              || (machine.canvasSize().x == 0 && machine.canvasSize().y == 0))
      {
         System.out.println("Warning. Using a default canvas size for the python file.  Please re-save the XML file with the diagram open.");
         executionTraceFile.println(indent + "def __init__(self, name, wparam="
               + "1280" + ", hparam=" + "1024"
               + "):");         
      }
      else
      {
         executionTraceFile.println(indent + "def __init__(self, name, wparam="
               + machine.canvasSize().x + ", hparam=" + machine.canvasSize().y
               + "):");
      }
      
      
      
//      executionTraceFile.println(indent + "def __init__(self, name, wparam="
//            + machine.canvasSize().x + ", hparam=" + machine.canvasSize().y
//            + "):");
      indent = "    ";
      executionTraceFile.println(indent + "self.win = Tk()");
      executionTraceFile
            .println(indent
                  + "self.label = Label(self.win, text = name, font = (\"Times\", 16, \"bold\"))");
      executionTraceFile.println(indent + "self.label.pack()");
      executionTraceFile
            .println(indent
                  + "self."
                  + DiagramElement.canvasName
                  + " = Canvas(self.win, width=wparam, height=hparam, background='white')");
      executionTraceFile.println(indent + "self." + DiagramElement.canvasName
            + ".pack()");
      executionTraceFile.println("");

      /**
       * Write out a mapping between this machine's state names and 
       * their fill colors (must happen before states are drawn):
       */
      executionTraceFile.println(indent + "# Map state names to fill colors:");
      executionTraceFile.print(indent + "self.colorDict = {");
      writeStateColors(machine.states(), indent + "                   ");
      executionTraceFile.println("}");
      executionTraceFile.println("");

      // Write out Python code defining state diagram elements:
      executionTraceFile.println(indent + "# State diagram elements:");
      writeStates(machine.states());

      executionTraceFile.println("");
      // Write out Python code defining separator elements:
      executionTraceFile.println(indent + "# Separator elements:");
      Iterator sepIter = machine.separators().iterator();
      while (sepIter.hasNext())
      {
         writeDiagramElement((DiagramElement) sepIter.next());
      }

      // Write out Python code defining transition diagram elements:
      executionTraceFile.println("");
      executionTraceFile.println(indent + "# Transition diagram elements:");
      Iterator tranIter = machine.transitions().iterator();
      while (tranIter.hasNext())
      {
         writeDiagramElement(((Transition) tranIter.next()).diagramElement());
      }
//      executionTraceFile.println(indent + "}");

      // Write out Python code defining text boxes:
      executionTraceFile.println("");
      executionTraceFile.println(indent + "# Text diagram elements:");
      writeText(machine.textElementList());
      executionTraceFile.println("");

      /**
       * Write out a mapping between this machine's state names and the
       * corresponding Python state diagram elements
       * (must happen after states are drawn):
       */
      executionTraceFile.println(indent + "# Map state names to states:");
      executionTraceFile.print(indent + "self.stateDict = {");
      writeStateMapNames(machine.states(), indent + "                  ");
      executionTraceFile.println("}");
      executionTraceFile.println("");

      /**
       * Write out the ExitState and EnterState methods, which is the pricipal
       * interface used by the application to display state changes in the GUI.
       */
      indent = "  ";
      // When exiting a state, change its fill color back to its default
      executionTraceFile.println(indent + "def ExitState(self, state):");
      indent = "    ";
      executionTraceFile
	.println(indent
		 + "self."
		 + DiagramElement.canvasName
		 + ".itemconfigure(self.stateDict[state], "
		 + "fill=self.colorDict[state])");
      indent = "  ";
      // When entering a state, change its fill color to green
      executionTraceFile.println(indent + "def EnterState(self, state):");
      indent = "    ";
      executionTraceFile
	.println(indent
		 + "self."
		 + DiagramElement.canvasName
		 + ".itemconfigure(self.stateDict[state], "
		 + "fill=\"green\")");
   }

   /**
    * Recursively writes out the machine's states - including all child states
    * of the specified state list.
    * 
    * @param stateList
    *           List The list of states.
    * @throws Exception
    */
   private void writeStates(List<State> stateList) throws Exception
   {
     Iterator stateIter = stateList.iterator();
     while (stateIter.hasNext())
       {
         State state = (State) stateIter.next();
	 if (state.name() == null)
	   continue;
	 writeDiagramElement(state.diagramElement());
	 // Recurse into children
	 if (state instanceof CompositeState)
	   writeStates(((CompositeState)state).getChildren());
	 // Recurse into sub-states
         if (state instanceof CompositeStateRegion)
	   {
	     Set<CompositeStateRegion> regions =
	       ((CompositeState) state).getAllSubRegions();
	     for ( CompositeStateRegion c : regions )
	       {
		 List<State> children = c.getChildren();
		 writeStates(children);
	       }
	   }
       }
   }

   /**
    * Writes out a mapping between state names and the corresponding Python
    * state diagram elements. This method is called recursively for all child
    * states of the specified list of states.
    * 
    * @param stateList
    *           List The list of states for which to write a mapping.
    * @param indent
    *           String A string used to indent the Python code for each of the
    *           state map names.
    * @throws Exception
    */
   private void writeStateMapNames(List<State> stateList, String indent)
         throws Exception
   {
      Iterator stateIter = stateList.iterator();
      while (stateIter.hasNext())
      {
         State state = (State) stateIter.next();
         boolean stateHasChildren = state instanceof CompositeState;
	 if (state.name() == null)
	   continue;

	 // klw: if it's not composite, just print out the state name,
	 // fully scoped if necessary
	 if (!(state instanceof CompositeStateRegion))
	   {
	     CompositeStateRegion enclosingRegion = machine.getEnclosingRegion(state);
	     String fullPath = "";
	     if (enclosingRegion != null) {
	       // Keep the names shorter by not sending in the machine name: ""
	       fullPath = machine.getQualifiedPath( enclosingRegion, "");
	       fullPath = fullPath.replace( ":", "_" ) + TargetLanguageMapper.sanitize(state.name());
	       fullPath = fullPath.substring(fullPath.lastIndexOf("_")+1);
	       executionTraceFile.print("'" + fullPath +
					"': self." + fullPath + ',');
	     } else {
	       // We do need machine names here!
	       fullPath = machine.getQualifiedPath( enclosingRegion, "");
	       fullPath = TargetLanguageMapper.sanitize(machine.name() + state.name());
	       executionTraceFile.print("'" + fullPath + "': self."
	               + TargetLanguageMapper.sanitize(state.name()) + ',');
	     }
	     executionTraceFile.print("\n" + indent);
	   }
	 // Recurse into children
	 if (stateHasChildren)
	   writeStateMapNames(((CompositeState)state).getChildren(), indent);
	 // Recurse into sub-states
	 if (state instanceof CompositeStateRegion)
	   { 
	     Set<CompositeStateRegion> regions =
	       ((CompositeState) state).getAllSubRegions();
	     for ( CompositeStateRegion c : regions )
	       {
		 List<State> children = c.getChildren();
		 writeStateMapNames(children, indent);
	       }
	   }
      }
   }

   /**
    * Writes out a mapping between state names and their fill colors.
    * This method is called recursively for all child states of
    * the specified list of states. 
    * 
    * @param stateList
    *           List The list of states for which to write a mapping.
    * @param indent
    *           String A string used to indent the Python code for each of the
    *           state map names.
    * @throws Exception
    */
   private void writeStateColors(List<State> stateList, String indent)
         throws Exception
   {
      Iterator stateIter = stateList.iterator();
      while (stateIter.hasNext())
      {
         State state = (State) stateIter.next();
         boolean stateHasChildren = state instanceof CompositeState;
	 if (state.name() == null)
	   continue;

	 // klw: if it's not composite, just print out the state name,
	 // fully scoped if necessary
	 if (!(state instanceof CompositeStateRegion))
	   {
	     CompositeStateRegion enclosingRegion = machine.getEnclosingRegion(state);
	     String fullPath = "";
	     if (enclosingRegion != null) {
	       // Keep the names shorter by not sending in the machine name: ""
	       fullPath = machine.getQualifiedPath( enclosingRegion, "");
	       fullPath = fullPath.replace( ":", "_" ) + TargetLanguageMapper.sanitize(state.name());
	       fullPath = fullPath.substring(fullPath.lastIndexOf("_")+1);
	     } else {
	       // We do need machine names here!
	       fullPath = machine.getQualifiedPath( enclosingRegion, "");
	       fullPath = TargetLanguageMapper.sanitize(machine.name() + state.name());
	     }
	     DiagramStateElement s = (DiagramStateElement)(state.diagramElement());
	     // klw: toHexString is non-static, so must call it from a
	     // DiagramElement object.
	     executionTraceFile.print("'" + fullPath + "': '#"
				      + s.toHexString(s.getFillColor(), 6) + "',");
	     executionTraceFile.print("\n" + indent);
	   }
	 // Recurse into children
	 if (stateHasChildren)
	   writeStateColors(((CompositeState)state).getChildren(), indent);
	 // Recurse into sub-states
	 if (state instanceof CompositeStateRegion)
	   { 
	     Set<CompositeStateRegion> regions =
	       ((CompositeState) state).getAllSubRegions();
	     for ( CompositeStateRegion c : regions )
	       {
		 List<State> children = c.getChildren();
		 writeStateColors(children, indent);
	       }
	   }
      }
   }

   /**
    * Writes out the specified list of text diagram elements.
    * 
    * @param textList
    *           List The list of diagram text elements.
    * @throws Exception
    */
   private void writeText(List<DiagramTextElement> textList) throws Exception
   {
      Iterator textIter = textList.iterator();
      while (textIter.hasNext())
      {
         DiagramTextElement text = (DiagramTextElement) textIter.next();
         writeDiagramElement(text);
      }
   }

   /**
    * Writes out the Python code for the specified diagram element.
    * 
    * @param element
    *           DiagramElement GUI element to write out to trace file.
    * @throws Exception
    */
   private void writeDiagramElement(DiagramElement element) throws Exception
   {
      if (null != element)
      {
         // Generate element's Python code used for GUI display:
         String[] linesOfCode = element.toPython();
         // Write out each separate line of code:
         for (int i = 0; i < linesOfCode.length; i++)
         {
            if (null != linesOfCode[i])
            {
               executionTraceFile.println(indent + linesOfCode[i]);
            }
         }
      }
   }

   /**
    * Writes out the header (starting) Python code for an execution trace file.
    * 
    * @throws Exception
    */
   private void writeTraceFileHeader() throws Exception
   {
      // Create/open a file to which we'll write Python code for execution trace
      // support:
      String signalTraceFilename = SimRtcSignalWriter.signalEnumName + ".py";
      System.out.println("Writing execution trace file " + signalTraceFilename
            + "...");
      signalTraceFile = new PrintStream(new FileOutputStream(
            signalTraceFilename));
      signalTraceFile.println("#!/tps/bin/python");
      signalTraceFile
            .println("# GUI application to issue signals to a state machine.");
      signalTraceFile.println("#");
      signalTraceFile.println("from Tkinter import *");
      signalTraceFile.println("");
      signalTraceFile.println("class StatechartSignals:");
      signalTraceFile.println("  def __init__(self, clientSocket):");
      signalTraceFile.println("    self.clientSocket = clientSocket");
      signalTraceFile.println("    win = Tk()");
      signalTraceFile
            .println("    label = Label(win, text = \"Statechart Signals\", font = (\"Times\", 16, \"bold\"))");
      signalTraceFile.println("    label.pack()");
      signalTraceFile.println("    " + DiagramElement.canvasName
            + " = Canvas(win, width=200, height=1000, background='white')");
      signalTraceFile.println("    " + DiagramElement.canvasName + ".pack()");
      signalTraceFile.println("");
   }

   /**
    * Writes out the footer (ending) Python code for an execution trace file.
    * 
    * @throws Exception
    */
   private void writeTraceFileFooter()
   {
      signalTraceFile.println("");
      signalTraceFile.println("  def doButton(self,enumValue):");
      signalTraceFile.println("    enumValue = enumValue + \"\\n\"");
      signalTraceFile.println("    self.clientSocket.send(enumValue)");
      signalTraceFile.println("");
      signalTraceFile.println("class ButtonCallback:");
      signalTraceFile.println("  def __init__(self, callback, *firstArgs):");
      signalTraceFile.println("    self.__callback = callback");
      signalTraceFile.println("    self.__firstArgs = firstArgs");
      signalTraceFile.println("");
      signalTraceFile.println("  def __call__(self, *args):");
      /*
      signalTraceFile
            .println("    return self.__callback (*(self.__firstArgs + args))");
      */  // changed for compatibility with old python
      signalTraceFile
      .println("    return apply(self.__callback, self.__firstArgs + args)");
      
      
   }

   /**
    * Writes out the Python GUI code for the specified signal (with the
    * specified C/C++ enumerated value).
    * 
    * @param enumValue
    *           int The integer value of the corresponding statechart siganl
    *           enumerated value.
    * @param signalName
    *           String The name of the signal.
    */
   private void writeSignalDiagramElement(int enumValue, String signalName)
   {
      if (signalName.contains("|"))
         return;
      signalTraceFile.println("    callback = ButtonCallback(self.doButton, \""
            + enumValue + "\")");
      signalTraceFile.println("    b" + enumValue + " = Button("
            + DiagramElement.canvasName + ", text = \"" + signalName
            + "\", command=callback).pack()");
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
}
