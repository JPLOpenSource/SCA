package gov.nasa.jpl.statechart.core;

import java.util.List;
import java.util.ArrayList;
import java.util.*;

/**
 * <p>
 * A transition from one state to another, which can be triggered by an event
 * (signal), guard, or timeout.
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
 * CVS Identification: $Id: Transition.java,v 1.8.2.1 2005/11/23 18:15:04 kclark
 * Exp $
 * </p>
 * 
 * @see TransitionGuard
 * @see TransitionTimeout
 */
public class Transition
{
   private String                   id;
   private String                   signalName;
   private String                   sourceId;
   private String                   targetId;
   private TransitionGuard          guard          = null;
   private TransitionTimeout        timeout        = null;
   private List<Action>             actions;
   private DiagramTransitionElement diagramElement = null;
   private boolean                  isInternal     = false;
   private boolean                  isLocalEvent   = false;
   private Set<String>              signalnames;

   /**
    * Constructor.
    * 
    * @param id
    *           String A unique cross reference ID.
    * @param signalName
    *           String The name a the signal which triggers the transition.
    * @param sourceId
    *           String The x-ref ID of the state from which this transition
    *           originates.
    * @param targetId
    *           String The x-ref ID of the state to which this transition goes.
    * @throws Exception
    */
   public Transition(String id, String signalName, String sourceId, String targetId)
         throws Exception
   {
      this.id = id;
      this.signalName = signalName;
      this.sourceId = sourceId;
      this.targetId = targetId;
      this.actions = new ArrayList<Action>();
      // Check for special naming convention that indicates this transition is
      // for a local event:
      if (isLocalSignalName(signalName))
      {
         isLocalEvent = true;
      }
      Set<String> set = new HashSet<String>();
      if (signalName != null)
      {
         StringTokenizer tok = new StringTokenizer(signalName, "|");
         while (tok.hasMoreTokens())
         {
            set.add(tok.nextToken().trim());
         }
      }
      signalnames = set;
   }

   /**
    * Returns a boolean flag indicating whether the passed signal name conforms
    * to syntax which indiates that this signal name is <i>local</i>, which
    * means that it is visible only to its enclosing state (i.e. not a globally
    * visible signal).
    * 
    * @param signalName
    *           String The signal name to check.
    * @return boolean True if the signal name looks like a local signal, false
    *         otherwise.
    */
   static public boolean isLocalSignalName(String signalName)
   {
      return (null != signalName && signalName.length() > 3 && signalName
            .substring(signalName.length() - 3).equals("Lev"));
   }

   public boolean isLocalEvent()
   {
      return isLocalEvent;
   }

   /**
    * Adds a diagram element to this transition, which is used for the GUI trace
    * execution.
    * 
    * @param element
    *           DiagramTransitionElement The diagram element to add.
    * @throws Exception
    */
   public void add(DiagramTransitionElement element) throws Exception
   {
      if (null == element)
         return;
      if (null != diagramElement)
      {
         throw new Exception(
               "Tried to add more than one DiagramElement to transition "
                     + signalName);
      }
      diagramElement = element;
   }

   public DiagramTransitionElement diagramElement()
   {
      return diagramElement;
   }

   /**
    * Adds a transition guard to this transition.
    * 
    * @param guard
    *           TransitionGuard The guard to add.
    * @throws Exception
    */
   public void add(TransitionGuard guard) throws Exception
   {
      if (null == guard)
         return;
      // it's ok time have a timeout with a guard now      
      this.guard = guard;
   }
   
   /**
    * Adds a transition timeout to this transition.
    * 
    * @param timeout
    *           TransitionTimeout The timeout to add.
    * @throws Exception
    */
   public void add(TransitionTimeout timeout) throws Exception
   {
      if (null == timeout)
         return;
      if (null != guard)
      {
         throw new Exception("Transition " + id
               + " already has a guard, can't add timeout.");
      }
      this.timeout = timeout;
   }

   /**
    * Adds an action to this transition.
    * 
    * @param action
    *           Action The action to add.
    */
   public void add(Action action)
   {
      if (null != action)
         actions.add(action);
   }

   /**
    * Adds a list of actions to this transition.
    * 
    * @param actionList
    *           Action The list of actions to add.
    */
   public void add(List<Action> actionList)
   {
      if (null != actionList)
         actions.addAll(actionList);
   }

   /**
    * Internal transitions don't result in a change of state, but can cause
    * actions to occur as a result of an event.
    */
   public void setInternal()
   {
      isInternal = true;
   }

   public boolean isInternal()
   {
      return isInternal;
   }

   public List<Action> getActions()
   {
      return this.actions;
   }

   public String id()
   {
      return this.id;
   }

   public String signalName()
   {
      return this.signalName;
   }

   public Set<String> getSignalNames()
   {
      return signalnames;
   }

   public TransitionGuard guard()
   {
      return this.guard;
   }

   public TransitionTimeout timeout()
   {
      return this.timeout;
   }

   public String targetId()
   {
      return this.targetId;
   }

   public String sourceId()
   {
      return this.sourceId;
   }

   public String toString()
   {
      return "XACT" + this.signalName;
   }
}
