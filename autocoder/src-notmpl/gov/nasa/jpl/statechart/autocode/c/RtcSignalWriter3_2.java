package gov.nasa.jpl.statechart.autocode.cm;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.autocode.IOldWriter;
import gov.nasa.jpl.statechart.autocode.IWriter;
import gov.nasa.jpl.statechart.autocode.IGenerator.Kind;
import gov.nasa.jpl.statechart.core.CompositeState;
import gov.nasa.jpl.statechart.core.ConcurrentCompositeState;
import gov.nasa.jpl.statechart.core.SimpleState;
import gov.nasa.jpl.statechart.core.State;
import gov.nasa.jpl.statechart.core.StateMachine;
import gov.nasa.jpl.statechart.core.Transition;
import gov.nasa.jpl.statechart.input.StateMachineXmiReader;
import gov.nasa.jpl.statechart.template.TargetLanguageMapper;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class RtcSignalWriter3_2 implements IOldWriter
{
   private static final String INDENT_STEP = "   ";
   private static final String INDENT_CASE = "    ";
   private static final String NO_INDENT   = "";

   StateMachineXmiReader reader;
   static final String   signalEnumName = "StatechartSignals";
   static final String   duringName     = "DURING";
   // Max characters on a line in signal declaration file:
   final int             lineLength     = 77;
   private PrintStream   signalDeclaration;
   private boolean       qualifySignalNames = false;

   /**
    * @note The following constant must match the integer value of the
    *       Q_USER_SIG C++ enumerated value declared in qevent.h
    */
   static
   {
      if (Autocoder.autocodingTarget() == Kind.CNonTemplate || Autocoder.autocodingTarget() == Kind.C)
      {
         Q_USER_SIG_VALUE = 4;
         c_mode = true;
      } else
      {
         Q_USER_SIG_VALUE = 6;
         c_mode = false;
      }
   }

   static final int      Q_USER_SIG_VALUE;
   static final boolean  c_mode;

   /**
    * Constructor.
    * 
    * @param reader
    *           StateMachineXmiReader The reader object containing the set of
    *           state machine representations.
    */
   public RtcSignalWriter3_2 (StateMachineXmiReader reader)
   {
      this.reader = reader;
      this.qualifySignalNames = Autocoder.qualifySignals();
   }

   /* (non-Javadoc)
    * @see gov.nasa.jpl.statechart.autocode.IWriter#write()
    */
   public void write () {
       try {
           writeSignalEnum();
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

   private String strrep( char c, int n )
   {
      char[] s = new char[n];
      for ( int i = 0; i < n; i++ )
         s[i] = c;
      
      return new String(s);
   }

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
      
      signalMap.put(TargetLanguageMapper.sanitize(classname + name), comment);
   }

   public Map<String, String> getSignalNamesAndComments()
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
         int depth = 200;
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

         for ( Node stateMachineNode : reader.getAllStateMachineNodes() )
         {            
            signalList.clear();

            // Get the state machine name
            String name = stateMachineNode.getAttributes().getNamedItem("name").getNodeValue();

            // Search for all the signals within this state machine
            reader.findNodes(signalList, "", "xmi:type", "uml:signal", stateMachineNode, depth );

            // Add the qualified signal names to the map
            for ( Node sig : signalList )            
               addSignalToMap( signalMap, sig, name + "_" );            
         }         
      }

      return signalMap;
   }

   /**
    * Writes out a C/C++ enumerated type which lists the set of statechart
    * signals contained in this instance's reader object.
    * 
    * @deprecated In favor of invocation via the {@link IWriter} interface.
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
      Map<String, String> signalMap = getSignalNamesAndComments();

      // Start the header comment:
      String indent = NO_INDENT;

      signalDeclaration.println("/**");
      signalDeclaration.println(" * @file " + signalFilename);
      signalDeclaration.println(" *");
      signalDeclaration.println(" * This file was generated by the SIM MagicDraw statechart converter,");
      signalDeclaration.println(" * and contains an enumerated type listing all signals referenced by ");
      signalDeclaration.println(" * the following state machines:");
      signalDeclaration.println(" *");
      signalDeclaration.print(" * ");

      int length = 0;

      /**
       * Get the HashMap of state machines, where the keys are the state machine
       * names, and write out the names of the state machines from which this
       * set of signals is generated:
       */
      for ( String name : reader.getStateMachineMap().keySet() )
      {
         if (length > 0)
         {
            signalDeclaration.print(", ");
         }

         length += name.length();
         if (length <= lineLength)
         {
            signalDeclaration.print(name);
         } 
         else
         {
            signalDeclaration.println("");
            signalDeclaration.print(" * ");
            signalDeclaration.print(name);
            length = name.length();
         }
      }
      signalDeclaration.println();

      // Finish the header comment:
      signalDeclaration.println(" *");

      int year = new GregorianCalendar().get(GregorianCalendar.YEAR);

      signalDeclaration.println(" * &copy; " + year +  " Jet Propulsion Lab / California Institute of Technology");
      signalDeclaration.println(" */");
      signalDeclaration.println();

      // State code for header file:
      String mnemonic = "_" + signalEnumName + "_h";
      signalDeclaration.println("#ifndef " + mnemonic);
      signalDeclaration.println("#define " + mnemonic);
      signalDeclaration.println();

      // Start declaration of enumerated type:
      signalDeclaration.println("enum " + signalEnumName );
      signalDeclaration.println("{");
      indent = INDENT_STEP;

      // First, write the standard During signal:
      signalDeclaration.println(indent + "/* \"During\" signal */");
      signalDeclaration.println(indent + duringName + " = Q_USER_SIG,");
      signalDeclaration.println();

      // Now write the user defined signals:
      signalDeclaration.println(indent + "/* User defined signals */");

      // Sort the signal list alphabetically, and iterate through the list:
      TreeSet<String> sortedSignals = new TreeSet<String>(signalMap.keySet());
      
      // First, compute the width of the largest signal name
      int maxWidth = 0;
      for ( String sigName : sortedSignals )
         maxWidth = Math.max( maxWidth, sigName.length() );

      // Create a string to pad the declaration
      String padding = strrep( ' ', maxWidth );

      // Q_USER_SIG is defined as 4 as of version 3.4.01 od the Quantum
      // Framework
      int count = 5;
      for ( String sigName : sortedSignals )
      {
         int    width = sigName.length();
         width        = Math.min( maxWidth, width );
         String pad   = padding.substring( 0, maxWidth - width );

         // Write out code for this signal:
         if (!sigName.contains("|"))
         {
            signalDeclaration.print(indent + sigName + "," + pad );
            signalDeclaration.printf("  /* 0x%02X ",  count);

            String comment = (String) signalMap.get(sigName);
            if (null != comment)
            {
               signalDeclaration.println( comment  + " " );
            }

            signalDeclaration.println( "*/" );
         }
         count++;
      }

      // Finish up the enumerated type declaration:
      signalDeclaration.println();

      if (RtcSignalWriter3_2.c_mode)
      {
         // more for C
         // signalDeclaration.println(indent + "Q_EVAL,");
         // signalDeclaration.println(indent + "Q_DURING,");
         // end for C
      }

      // Write all timer signals. **BUGBUG** may need to decend further
      // down using getAllStates()
      Set<String> timerStateNames = new HashSet<String>();

      for ( StateMachine nextStateMachine : reader.getStateMachineMap().values() )
      {
         for ( State s : nextStateMachine.states() )
         {
            getTimerSignals(s, timerStateNames);            
         }
      }

      if ( !timerStateNames.isEmpty() )
      {
         signalDeclaration.println( indent + "/* Timer Events */" );
      }

      // As with the signal names, sort the timers alphabetically and
      // align the comments
      TreeSet<String> sortedTimers = new TreeSet<String>( timerStateNames );

      // Compute the width of the widest timer name
      maxWidth = 0;
      for ( String timerName : sortedTimers )
         maxWidth = Math.max( maxWidth, timerName.length() );

      // Create a string to pad the comments
      padding = strrep( ' ', maxWidth );

      for ( String timerName : sortedTimers )
      {
         int    width = timerName.length();
         width        = Math.min( maxWidth, width );
         String pad   = padding.substring( 0, maxWidth - width );

         // Write out code for this timer:
         signalDeclaration.print( indent + timerName + "_timerEv," + pad );
         signalDeclaration.printf("  /* 0x%02X */\n",  count);

         count++;
      }
      
      if ( !timerStateNames.isEmpty() )
      {
         signalDeclaration.println();
      }

      // Write the last bit of the declaration
      signalDeclaration.println(indent + "/* Maximum signal id */");
      signalDeclaration.println(indent + "MAX_SIG");
      signalDeclaration.println("};");
      signalDeclaration.println("");
      signalDeclaration.println("#endif /* " + mnemonic + " */");
   }

   private void getTimerSignals(State state, Set<String> timerstates)
   {
      if(state instanceof CompositeState && !(state instanceof ConcurrentCompositeState))
      {
         // don't write composite states themselves
         CompositeState comp = (CompositeState) state;
         if(comp.hasTimeout())
         {
            timerstates.add(TargetLanguageMapper.sanitize(comp.name()));
         }
         for(State child : comp.getChildren()  )
         {
            getTimerSignals(child, timerstates);
         }
      }
      else if(state instanceof ConcurrentCompositeState)
      {
         ConcurrentCompositeState comp = (ConcurrentCompositeState) state;
         if(comp.hasTimeout())
         {
            timerstates.add(TargetLanguageMapper.sanitize(state.name()));
         }
         // we do want to descent to regions in this case
         for(State child : comp.getChildren()  )
         {
            getTimerSignals(child, timerstates);
         }
         
      }
      else if(state instanceof SimpleState)
      {
         SimpleState simple = (SimpleState) state;
         if(simple.hasTimeout())
         {
            timerstates.add(TargetLanguageMapper.sanitize(state.name()));
         }
      }      
   }

}
