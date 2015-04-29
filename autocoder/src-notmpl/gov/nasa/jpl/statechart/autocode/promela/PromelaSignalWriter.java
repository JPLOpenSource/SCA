package gov.nasa.jpl.statechart.autocode.promela;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.autocode.gui.ExecutionTracePythonWriter;
import gov.nasa.jpl.statechart.core.CompositeState;
import gov.nasa.jpl.statechart.core.SimpleState;
import gov.nasa.jpl.statechart.core.State;
import gov.nasa.jpl.statechart.core.StateMachine;
import gov.nasa.jpl.statechart.core.Transition;
import gov.nasa.jpl.statechart.input.StateMachineXmiReader;
import gov.nasa.jpl.statechart.input.magicdraw.MagicDrawUmlReader;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.w3c.dom.Node;


/**
 * <p>
 * Finds all specified signals (events) in a set on input XML files, and
 * generates a corresponding C++ enumerated type declaration. Also, optionally
 * generates the corresponding execution trace (Python) file.
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
 * CVS Identification: $Id: SimRtcSignalWriter.java,v 1.18 2005/10/11 18:08:09
 * kclark Exp $
 * </p>
 */
class PromelaSignalWriter
{
   StateMachineXmiReader reader;
   static final String   signalEnumName = "StatechartSignals";
   static final String   duringName     = "DURING";
   // Max characters on a line in signal declaration file:
   final int             lineLength     = 77;
   private PrintStream   signalDeclaration;
   int count;

   /**
    * @note The following constant must match the integer value of the
    *       Q_USER_SIG C++ enumerated value declared in qevent.h
    */
   static
   {
      Q_USER_SIG_VALUE = 5;	   
      c_mode = true;
   }
   static int            Q_USER_SIG_VALUE;
   static final boolean  c_mode;

   /**
    * Constructor.
    * 
    * @param reader
    *           StateMachineXmiReader The reader object containing the set of
    *           state machine representations.
    */
   public PromelaSignalWriter(StateMachineXmiReader reader)
   {
      this.reader = reader;
   }

   private void writeTimerSignals(StateMachine machine, State state)
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
            signalDeclaration.println("#define " + machine.name().toUpperCase() +
                  "_" + state.name().toUpperCase() + "_" + "TIMEOUT" + 
                  " " + count + "");
            count++;
         }
      }
      
      if(state instanceof CompositeState)
      {
         CompositeState composite = (CompositeState) state;
         for(State child : composite.getChildren())
         {
            writeTimerSignals(machine, child);
         }                           
      }            
   }
   
   private void writeTimerSignals(StateMachine machine)
   {
      for(State state : machine.states())
      {
         writeTimerSignals(machine, state);
      }
   }
   
   /**
    * Writes out a C/C++ enumerated type which lists the set of statechart
    * signals contained in this instance's reader object.
    * 
    * @throws Exception
    */
   public void writeSignalEnum() throws Exception
   {

	   
      // Create/open the signal declaration file:
      String signalFilename = signalEnumName + ".h";
      System.out.println("Writing " + signalFilename + "...");
      signalDeclaration = new PrintStream(new FileOutputStream(signalFilename));
      // Construct a map which associates signal names with corresponding user
      // comments:
      HashMap<String, String> signalMap = new HashMap<String, String>();
      Iterator signalIter = reader.getSignalList().iterator();
      while (signalIter.hasNext())
      {
         Node sig = (Node) signalIter.next();
         if (null != sig.getAttributes().getNamedItem("name")
               &&
               null != sig.getAttributes().getNamedItem("name").getNodeValue())
         {
            String name = (String) sig.getAttributes().getNamedItem("name")
               .getNodeValue().trim();
            String comment = reader.findComment(sig);
            if (!Transition.isLocalSignalName(name))
            { // Filter out local events...
               signalMap.put(name, comment);
            }
         }
      }
      // Start the header comment:
      String indent = "      ";
      signalDeclaration.println("/**");
      signalDeclaration.println(" * @file " + signalFilename);
      signalDeclaration.println(" *");
      signalDeclaration
            .println(" * This file was generated by the SIM MagicDraw statechart converter,");
      signalDeclaration
            .println(" * and contains an enumerated type listing all signals referenced by ");
      signalDeclaration.println(" * the following state machines:");
      signalDeclaration.println(" *");
      signalDeclaration.print(" * ");
      int length = 0;
      /**
       * Get the HashMap of state machines, where the keys are the state machine
       * names, and write out the names of the state machines from which this
       * set of signals is generated:
       */
      Iterator smIter = reader.getStateMachineMap().keySet().iterator();
      while (smIter.hasNext())
      {
         String name = (String) smIter.next();
         if (length > 0)
         {
            signalDeclaration.print(", ");
         }
         length += name.length();
         if (length <= lineLength)
         {
            signalDeclaration.print(name);
         } else
         {
            signalDeclaration.println("");
            signalDeclaration.print(" * ");
            signalDeclaration.print(name);
            length = name.length();
         }
      }
      // Finish the header comment:
      signalDeclaration.println("");
      signalDeclaration.println(" *");
      GregorianCalendar calendar = new GregorianCalendar();
      signalDeclaration.println(" * &copy "
            + calendar.get(GregorianCalendar.YEAR)
            + " Jet Propulsion Lab / California Institute of Technology");
      signalDeclaration.println(" */");
      signalDeclaration.println("");
      // Start declaration of enumerated type:
      indent = "   ";

      // First, write the standard During signal:
      // Now write the user defined signals:
      signalDeclaration.println(indent + "// User defined signals:");
      // Sort the signal list alphabetically, and iterate through the list:
      TreeSet<String> sortedSignals = new TreeSet<String>(signalMap.keySet());
      Iterator signalNameIter = sortedSignals.iterator();
      
      count = 5; // starting values
      while (signalNameIter.hasNext())
      {
         // Write out code for this signal:
         String sigName = (String) signalNameIter.next();
         if (!sigName.contains("|"))
         {
            // signalDeclaration.print(indent + sigName + ",");
        	signalDeclaration.print("#define " + sigName + " " + count + ""); 
        	count++;
            String comment = (String) signalMap.get(sigName);
            if (null != comment)
            {
               signalDeclaration.println("  // " + comment);
            } else
            {
               signalDeclaration.println("");
            }
         }
      }
      // write any timer signals
      
      for(StateMachine machine : reader.getStateMachineMap().values())
      {
         writeTimerSignals(machine);
      }
      
      // Finish up the enumerated type declaration:
      signalDeclaration.println("");
      if (PromelaSignalWriter.c_mode)
      {
         // more for C
         signalDeclaration.println("#define Q_EVAL " + count + ""  );
         count++;
         signalDeclaration.println("#define Q_DURING " + count + ""  );
         count++;
         // end for C
      }
      signalDeclaration.println("#define MAX_SIG " + count + ""  );

   }
     
   /**
    * Finds all specified signals (events) in the <code>args</code> XML
    * file(s), and generates a corresponding C++ enumerated type declaration.
    * Also, optionally generates the corresponding execution trace (Python)
    * file.
    * 
    * @param args
    *           String[] List of XML files containing signal specifications.
    */
   public static void main(String[] args)
   {
      try
      {
         // Create the UML reader object which is used for the SIM RTC project:
         MagicDrawUmlReader reader = new MagicDrawUmlReader();
         //reader.setSignalsOnly();
         // Parse the XML files:
         reader.parseXmlFiles(args);
         // Create the enumerated type listing all signals found in the XML
         // files:
         new PromelaSignalWriter(reader).writeSignalEnum();
         // Check a system property to see if we should generate the execution
         // trace file:
         if (Autocoder.isExecutionTraceOn())
         {
            new ExecutionTracePythonWriter(reader).writeSignalTraceFile();
         }
         System.out.println("Finished.");
      } catch (Exception e)
      {
         e.printStackTrace(System.err);
      }
   }
}
