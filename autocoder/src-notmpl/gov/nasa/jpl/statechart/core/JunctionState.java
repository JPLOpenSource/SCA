package gov.nasa.jpl.statechart.core;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * A junction state has a single incoming transition, and two outgoing
 * transition, one of which is selected based on a specified guard condition.
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
 * CVS Identification: $Id: JunctionState.java,v 1.1.2.1 2005/11/23 18:15:03
 * kclark Exp $
 * </p>
 */
public class JunctionState extends State
{
   private List<Transition> incoming;

   public JunctionState(String name, String id)
   {
      super(name, id);
      outgoing = new ArrayList<Transition>();
      incoming = new ArrayList<Transition>();
   }

   @Override
   public void addIncoming(Transition t) throws Exception
   {
      if (t == null) return;
      incoming.add(t) ;
   }

   @Override
   public List<Transition> getIncoming()
   {
      return incoming;
   }

   /**
    * Adds the passed transition to this state's list of outgoing transitions.
    * 
    * @param t
    *           The outgoing transion to add.
    */
   @Override
   public void addOutgoing(Transition t) throws Exception
   {
      if (t == null) return;
      if (outgoing.size() >= 2)
         throw new Exception(
               "Tried to add more than two outgoing transitions to junction state "
                     + name + " - invalid!");
      super.addOutgoing(t);
   }
}
