package gov.nasa.jpl.statechart.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * A composite state is one which contains child states.
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
 * CVS Identification: $Id: CompositeState.java,v 1.1.2.1 2005/11/23 18:15:02
 * kclark Exp $
 * </p>
 */
public class CompositeState extends SimpleState
{
   protected List<State> childStates = new ArrayList<State>();

   /**
    * Constructor.
    * 
    * @param name
    *           String state name.
    * @param id
    *           String state cross-reference ID.
    * @throws Exception
    */
   public CompositeState(String name, String id) throws Exception
   {
      super(name, id);
   }

   public CompositeState()
   {
      super();
   }

   /**
    * Adds a child state to this state.
    * 
    * @param child
    *           The child state to add.
    */
   public void addChild(State child) throws Exception
   {
      // Check if new child is a composite state region, which can only be added
      // to a ConcurrentCompositeState:
      if (child instanceof CompositeStateRegion)
         throw new Exception(
               "Tried to add a CompositeStateRegion to composite state " + name
                     + " - invalid!");
      // Check if this state has a corresponding diagram element, and discard if
      // not.
      if (null != child.diagramElement())
      {
         childStates.add(child);
      }
   }

   /**
    * Returns the set of signal names for all outgoing transitions from this
    * state and all its child states.
    * 
    * @return Set Signal names of outgoing transitions.
    */
   public Set<String> getAllDesiredEvents()
   {
      Set<String> set = super.getAllDesiredEvents();
      for (State child : childStates)
      {
         set.addAll(child.getAllDesiredEvents());
      }
      return set;
   }

   /**
    * Returns the set of child states which are instances of
    * CompositeStateRegion.
    * 
    * @return Set of found regions.
    */
   public Set<CompositeStateRegion> getAllSubRegions()
   {
      Set<CompositeStateRegion> set = new HashSet<CompositeStateRegion>();

      for (State child : childStates)
      {
         if (child instanceof CompositeStateRegion)
         {
            set.add( (CompositeStateRegion) child );
         }

         if (child instanceof CompositeState)
         {
            CompositeState cchild = (CompositeState) child;
            set.addAll( cchild.getAllSubRegions() );
         }
      }
      return set;
   }

   /**
    * Returns the set of all child states.
    * 
    * @return List of child states.
    */
   public List<State> getChildren()
   {
      return childStates;
   }

   /**
    * Checks if this state contains the specifed target child state.
    * 
    * @param targetState
    *           The state in question.
    * @return boolean True if this state contains the specified child state,
    *         false otherwise.
    */
   public boolean contains(State targetState)
   {
      if (this == targetState)
         return true;
      else
      {
         Iterator childIter = this.getChildren().iterator();
         while (childIter.hasNext())
         {
            State child = (State) childIter.next();
            if (child instanceof CompositeState)
            {
               return ((CompositeState) child).contains(targetState);
            } else if (child == targetState)
               return true;
         }
         return false;
      }
   }

   /**
    * Returns a flag indicating whether this state has at least one child which
    * is an instance of DeepHistoryState.
    * 
    * @return boolean True if this state contains a history state, false
    *         otherwise.
    */
   public boolean containsHistoryState()
   {
      for (State child : childStates)
      {
         if (child instanceof DeepHistoryState)
         {
            return true;
         }
      }
      return false;
   }
}
