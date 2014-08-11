package gov.nasa.jpl.statechart.core;

import java.util.ArrayList;

/**
 * <p>
 * This is a state that can have incoming and outgoing transitions, and actions
 * which are performed upon entry to the state, exit from the state, or performed
 * "during" the (rate group) execution of a state machine.
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
 * CVS Identification: $Id: SimpleState.java,v 1.1.2.1 2005/11/23 18:15:04
 * kclark Exp $
 * </p>
 */
public class SimpleState extends State
{
   public class NotNamedException extends Exception
   {
      static final long serialVersionUID = -3387516993124229948L;

      public NotNamedException(String text)
      {
         super(text);
      }
   }

   private boolean hasGuard   = false;
   private boolean hasTimeout = false;

   public SimpleState(String name, String id) throws Exception
   {
      super();
      if (null == name)
         throw new NotNamedException("Invalid state found (ID=" + id
               + ") - must be named.");
      incoming = new ArrayList<Transition>();
      outgoing = new ArrayList<Transition>();
      entryActions = new ArrayList<Action>();
      duringActions = new ArrayList<Action>();
      exitActions = new ArrayList<Action>();
      this.name = name;
      this.id = id;
   }

   public SimpleState()
   {
      super();
   }

   @Override
   public void addOutgoing(Transition t) throws Exception
   {
      if (t == null) return;
      if (-1 == outgoing.indexOf(t))
      {
         outgoing.add(t);
         if (null != t.guard())
         {
            this.hasGuard = true;
         }
         if (null != t.timeout())
         {
            if (this.hasTimeout)
            {
               throw new Exception("State " + name
                     + " can't have more than one transition timeout!");
            }
            this.hasTimeout = true;
         }
      }
   }

   public boolean hasGuard()
   {
      return this.hasGuard;
   }

   public boolean hasTimeout()
   {
      return this.hasTimeout;
   }
}
