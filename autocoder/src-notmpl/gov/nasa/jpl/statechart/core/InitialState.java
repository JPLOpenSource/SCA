package gov.nasa.jpl.statechart.core;

import java.util.*;

/**
 * <p>
 * No incoming transitions or entry actions for an initial state, just outgoing
 * transitions.
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
 * CVS Identification: $Id: InitialState.java,v 1.1.2.1 2005/11/23 18:15:03
 * kclark Exp $
 * </p>
 */
public class InitialState extends State
{
   private Transition outgoing;

   public InitialState(String name, String id)
   {
      super(name, id);
   }

   @Override
   public void addOutgoing(Transition t) throws Exception
   {
      if (t == null) return;
      if (outgoing != null)
         throw new Exception(
               "Tried to add more than one outgoing transition to initial state "
                     + name + " - invalid!");
      outgoing = t;
   }

   @Override
   public List<Transition> getOutgoing()
   {
      if (outgoing != null)
      {
         return Collections.singletonList(outgoing);
      } else
      {
         return new ArrayList<Transition>();
      }
   }

}
