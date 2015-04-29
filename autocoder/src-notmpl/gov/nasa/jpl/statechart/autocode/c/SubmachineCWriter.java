/**
 * Specialized C code writer for expanded submachine.
 *
 * This class overrides the full state chart code generator to print out
 * the reduced code for the submachine implementations
 */
package gov.nasa.jpl.statechart.autocode.cm;

import gov.nasa.jpl.statechart.core.StateMachine;

import java.io.*;
import java.util.*;


public class SubmachineCWriter extends StateChartCWriter
{
   // Submachines exist within their own namespace
   protected final String prefix;

   public SubmachineCWriter( StateMachine stateMachine, String classname, String prefix, String myType )
   {
      super( stateMachine, classname, myType );
      this.prefix = prefix;
   }

   public SubmachineCWriter( StateMachine stateMachine, String classname, String prefix )
   {
      this( stateMachine, classname, prefix, classname );
   }

   @Override
   public String getClassPrefix()
   {
      return prefix;
   }

   /**
    * The top level state for a submachine is defined with the
    * submachine state name in the enclosing state machine      
    */
   @Override
   protected String getTopLevelState()
   {
      return getClassPrefix().replace( ":", "_" );
   }
      
   /**
    * The enclosing state will have subscribed to all the events
    */
   @Override
   protected void writeQActiveInitialMethod()
   {
   }

   /**
    * No includes needed
    */
   @Override
   protected void writeIncludes()
   {
   }

   /**
    * No constructor needed
    */
   @Override
   protected void writeStateMachineConstructor()
   {
   }
}
