package gov.nasa.jpl.statechart.core;

/**
 * <p>
 * A concurrent composite state contains multiple composite state regions.
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
 * CVS Identification: $Id: ConcurrentCompositeState.java,v 1.1.2.1 2005/11/23
 * 18:15:02 kclark Exp $
 * </p>
 * 
 * @see CompositeStateRegion
 */
public class ConcurrentCompositeState extends CompositeState
{
   public ConcurrentCompositeState(String name, String id) throws Exception
   {
      super(name, id);
   }

   /**
    * Adds a child state to this state. The super class' method is overridden to
    * make sure that only CompositeStateRegions can be added as child states.
    * 
    * @param child
    *           The child state to add.
    */
   @Override
   public void addChild(State child) throws Exception
   {
      // Make sure that its a region being added:
      if (!(child instanceof CompositeStateRegion))
      {
         throw new Exception(
               "Can only add a CompositeStateRegion to a ConcurrentCompositeState.");
      }
      childStates.add(child);
   }
}