/**
 * The class is used to write out the .genh header files for submachines
 * instances contained within a state machine
 */
package gov.nasa.jpl.statechart.autocode.cm;

import gov.nasa.jpl.statechart.core.StateMachine;

import java.io.*;
import java.util.*;


public class SubmachineHeaderWriter extends StateChartHeaderWriter
{
   ////
   // Let there be a two submachine states named "Sub1" and "Sub2" which
   // are both instances of the diagram "Submachine".  These two states
   // are contained withing another diagram names "Top"
   //
   // For all the states within the submachines there will be two
   // prefix paths:
   //
   //   prefix    = "Top:Sub1" and "Top:Sub2"
   //   classname = "Top"
   //   stateMachine.name() = "Submachine" in both cases
   //
   // The classname holds the name of the type that is used for the "me"
   // pointers. 

   protected final String prefix;

   public SubmachineHeaderWriter( StateMachine stateMachine, String classname, String prefix, String myType )
   {
      super( stateMachine, classname, myType );
      this.prefix = prefix;

      System.out.println( "Instantiated Submachine Header Writer with meType = " + meType );
   }

   public SubmachineHeaderWriter( StateMachine stateMachine, String classname, String prefix )
   {
      this( stateMachine, classname, prefix, classname );
   }

   @Override
      public String getClassPrefix()
   {
      return prefix;
   }

   @Override
      protected String getMnemonic()
   {
      return "_" + getClassPrefix().replace(":","_").toLowerCase() + "_h";
   }
   /**
    * Change the prologue to avoid pulling in the Implementations class
    */
   @Override
      protected void writePrologue()
   {
      String mnemonic  = getMnemonic();

      out.println("#ifndef " + mnemonic);
      out.println("#define " + mnemonic);
      out.println();
      out.println("#include \"qf_port.h\"");
      out.println("#include \"qassert.h\"");
      out.println();
   }
      
   /**
    * Do not write any data type declarations except regions
    * that are specific to this submachine
    */
   @Override
      protected void writeDataTypes()
   {
      writeStateRegionDeclarations();
   }     

   /**
    * Do not write any constructor and initial functions
    */
   @Override
      protected void writeInitializationPrototypes()
   {
   }
}
