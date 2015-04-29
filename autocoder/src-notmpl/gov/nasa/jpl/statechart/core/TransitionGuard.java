package gov.nasa.jpl.statechart.core;

import gov.nasa.jpl.statechart.input.StateMachineXmiReader;

/**
 * <p>
 * A transition guard is a user method, returning a boolean value, which is
 * periodically called to determine if a transtion should be taken.
 * </p>
 * 
 * <p>
 * Note: The accepted syntax for a transition guard is a method name, with an
 * optional logical NOT operator character "!" in front of the method name
 * (space separated).
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
 * CVS Identification: $Id: TransitionGuard.java,v 1.1.2.1 2005/11/23 18:15:04
 * kclark Exp $
 * </p>
 */
public class TransitionGuard
{
   private static final int NO_OP  = 0;
   private static final int NOT_OP = 1;
   private int              operator;
   private String           methodName;

   public TransitionGuard(String text) throws Exception
   {
      if (text.trim().length() < 1)
         throw new Exception(
               "Cannot instantiate a TransitionGuard with a blank guard expression!");
      operator = NO_OP;
      parseGuardText(text.trim());
   }

   public boolean notOp()
   {
      return (operator == NOT_OP);
   }

   public String methodName()
   {
      return methodName;
   }

   private void parseGuardText(String text) throws Exception
   {
      if (text.substring(0, 1).equals("!"))
      {
         operator = NOT_OP;
         methodName = text.substring(1).trim();
      } else
      {
         methodName = text;
      }
      if(methodName.startsWith("\""))
      {
         // skip the check
         return;
      }

      if (!StateMachineXmiReader.isMethodCallSyntax(methodName))
         throw new Exception("Invalid transition guard expression \"" + text
               + "\" - guard must be a method call.");
   }
}
