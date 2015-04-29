package gov.nasa.jpl.statechart.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * A deep history state has n incoming and 0 or 1 outgoing transitions.
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
 * CVS Identification: $Id:
 * </p>
 */
public class DeepHistoryState extends State
{
   private Transition outgoing;

   public DeepHistoryState(String name, String id)
   {
      super(name, id.trim());
      incoming = new ArrayList<Transition>();
   }

   @Override
   public void addOutgoing(Transition t) throws Exception
   {
      if (t == null) return;
      if (outgoing != null)
         throw new Exception(
               "Tried to add more than one outgoing transition to deep history state "
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
