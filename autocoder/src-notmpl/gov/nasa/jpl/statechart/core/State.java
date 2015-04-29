package gov.nasa.jpl.statechart.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Abstract class for state - as in a UML state machine.
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
 * CVS Identification: $Id: State.java,v 1.3 2008/02/20 05:56:42 lscharen Exp $
 * </p>
 */
public abstract class State
{
   protected String              name;
   protected String              id;
   protected List<Transition>    incoming;
   protected List<Transition>    outgoing;
   protected List<Action>        duringActions;
   protected List<Action>        exitActions;
   protected List<Action>        entryActions;
   protected DiagramStateElement diagramElement;
   protected State               parent;
   protected StateMachine        stateMachine = null;

   /**
    * Constructor.
    * 
    * @param name
    *           String The name of this state instance.
    * @param id
    *           String The cross-reference ID of this state instance.
    */
   State(String name, String id)
   {
      this.name = name;
      this.id = id;
   }

   State()
   {
   }

   /**
    * Sets the parent state to this state.
    * 
    * @param parentstate
    *           State The parent state to this state.
    */
   public void setParent(State parentstate)
   {
      if (null != parent)
      {
         throw new RuntimeException("Tried to add more than one parent to state "
                                    + name);
      }

      parent = parentstate;
   }

   // Returns the parent state to this state.
   public State getParent()
   {
      return parent;
   }

   public void setEnclosingStateMachine( StateMachine sm )
   {
      stateMachine = sm;
   }

   public StateMachine getEnclosingStateMachine()
   {
      return stateMachine;
   }

   // Adds the passed diagram state element to this state.
   public void add(DiagramStateElement element) throws Exception
   {
      if (null != diagramElement)
      {
         throw new Exception(
               "Tried to add more than one DiagramElement to state " + name);
      }
      diagramElement = element;
   }

   // Returns this diagram state element for this state.
   public DiagramElement diagramElement()
   {
      return diagramElement;
   }

   // Returns the state's name.
   public String name()
   {
      return name;
   }

   // Returns the state's cross-reference ID.
   public String id()
   {
      return id;
   }

   /**
    * Finds and returns a transition with the specified ID from the specified
    * list.
    * 
    * @param transList
    *           List The list of transitions to search.
    * @param ID
    *           String Transition ID to search for.
    * @return Transition The matching transition, or null if not found.
    */
   static public Transition findTransition(List<Transition> transList, String ID)
   {
      Iterator i = transList.iterator();
      while (i.hasNext())
      {
         Transition t = (Transition) i.next();
         if (t.id().equals(ID))
         {
            return t;
         }
      }
      return null;
   }

   /**
    * Returns the set of signal names for all outgoing transitions from this
    * state.
    * 
    * @return Set Signal names of outgoing transitions.
    */
   public Set<String> getAllDesiredEvents()
   {
      Set<String> set = new HashSet<String>();

      if (null != getOutgoing())
      {
         for ( Transition t : getOutgoing() )
         {
            for (String name : t.getSignalNames())
            {
               set.add(name);
            }
         }
      }

      //

      return set;
   }

   /**
    * Adds a single exit action to this state.
    * @param action
    *           Exit action to add to this state.
    * @throws Exception
    */
   public void addExitAction(Action action) throws Exception
   {
      if (null == getExitActions())
         throw new Exception("Tried to add an exit action to a "
               + this.getClass().getName() + " object - invalid!");
      getExitActions().add(action);
   }

   /**
    * Adds a list of actions which are to be performed when the state is exited.
    * 
    * @param actions
    *           List The list of actions to add to this state.
    */
   public void addExitAction(List<Action> actions) throws Exception
   {
      if (null == actions)
         return;
      Iterator i = actions.iterator();
      while (i.hasNext())
      {
         Action action = (Action) i.next();
         this.addExitAction(action);
      }
   }

   /**
    * Returns the list of exit actions for this state.
    * @return list of Action objects
    */
   public List<Action> getExitActions()
   {
      return exitActions;
   }

   /**
    * Adds a single entry action to this state.
    * @param action
    *           Entry action to add to this state.
    * @throws Exception
    */
   public void addEntryAction(Action action) throws Exception
   {
      if (null == getEntryActions())
         throw new Exception("Tried to add an entry action to a "
               + this.getClass().getName() + " object - invalid!");
      getEntryActions().add(action);
   }

   /**
    * Adds a list of actions which are to be performed when the state is
    * entered.
    * 
    * @param actions
    *           List The list of actions to add to this state.
    */
   public void addEntryAction(List<Action> actions) throws Exception
   {
      if (null == actions)
         return;
      Iterator i = actions.iterator();
      while (i.hasNext())
      {
         Action action = (Action) i.next();
         this.addEntryAction(action);
      }
   }

   /**
    * Returns the list of entry actions for this state.
    * @return list of Action objects
    */
   public List<Action> getEntryActions()
   {
      return entryActions;
   }

   public void addDuringAction(Action action) throws Exception
   {
      if (null == getDuringActions())
         throw new Exception("Tried to add a During action to a "
               + this.getClass().getName() + " object - invalid!");
      getDuringActions().add(action);
   }

   /**
    * Adds a list of actions which are to be performed "during" the state's
    * periodic rategroup execution.
    * 
    * @param actions
    *           List The list of actions to add to this state.
    */
   public void addDuringAction(List<Action> actions) throws Exception
   {
      if (null == actions)
         return;
      Iterator i = actions.iterator();
      while (i.hasNext())
      {
         Action action = (Action) i.next();
         this.addDuringAction(action);
      }
   }

   /**
    * Returns the list of During actions for this state.
    * @return list of Action objects
    */
   public List<Action> getDuringActions()
   {
      return duringActions;
   }

   /**
    * Adds the passed transition to this state's list of incoming transitions.
    * 
    * @param t
    *           The incoming transion to add.
    */
   public void addIncoming(Transition t) throws Exception
   {
      if (t == null) return;
      if (null == incoming)
         throw new Exception("Tried to add an incoming transition to a "
               + this.getClass().getName() + " object - invalid!");
      if (-1 == incoming.indexOf(t))
      {
         incoming.add(t);
      }
   }

   /**
    * Adds the passed transition to this state's list of outgoing transitions.
    * 
    * @param t
    *           The outgoing transion to add.
    */
   public void addOutgoing(Transition t) throws Exception
   {
      if (t == null) return;
      if (null == outgoing)
         throw new Exception("Tried to add an outgoing transition to a "
               + this.getClass().getName() + " object - invalid!");
      if (-1 == outgoing.indexOf(t))
      {
         outgoing.add(t);
      }
   }

   /**
    * Returns the list of incoming transitions for this state.
    * @return list of Transition objects
    */
   public List<Transition> getIncoming()
   {
      return incoming;
   }

   /**
    * Returns the list of outgoing transitions for this state.
    * @return list of Transition objects
    */
   public List<Transition> getOutgoing()
   {
      return outgoing;
   }

   /**
    * Returns a flag to indicate whether this state has the specified incoming
    * transition.
    * 
    * @param transitionId
    *           String The transition ID to check for.
    * @return boolean True if found, false otherwise.
    */
   public boolean hasIncoming(String transitionId)
   {
      if (null != getIncoming())
         return (null != findTransition(getIncoming(), transitionId));
      else
         return false;
   }

   /**
    * Returns a flag indicating whether this state has any incoming transitions.
    * 
    * @return boolean Flag
    */
   public boolean hasIncoming()
   {
      if (null == getIncoming() || getIncoming().isEmpty())
         return false;
      else
         return true;
   }

   /**
    * Returns a flag to indicate whether this state has the specified outgoing
    * transition.
    * 
    * @param transitionId
    *           String The transition ID to check for.
    * @return True if found, false otherwise.
    */
   public boolean hasOutgoing(String transitionId)
   {
      if (null != getOutgoing())
         return (null != findTransition(getOutgoing(), transitionId));
      else
         return false;
   }

   /**
    * Returns a flag indicating whether this state has any outgoing transitions.
    * 
    * @return boolean Flag
    */
   public boolean hasOutgoing()
   {
      if (null == getOutgoing() || getOutgoing().isEmpty())
         return false;
      else
         return true;
   }

   @Override
   public String toString()
   {
      return name;
   }
}
