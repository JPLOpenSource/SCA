package gov.nasa.jpl.statechart.core;


/**
 * <p>
 * A submachine state is one that contains a reference to a complete state
 * chart.  Multiple submahcine states can reference the same chart.
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
 * CVS Identification: $Id: SubmachineState.java,v 1.1 2008/01/31 01:28:43 lscharen Exp $
 * </p>
 */

/**
 * The choice was made to make SubmachineState a subclass of Composite
 * state based on the Andromeda OMG implementation.
 */ 
public class SubmachineState extends CompositeState
{
   protected String submachine = null;
   protected StateMachine stateMachine = null;

   /**
    * Constructor.
    * 
    * @param name
    *           String state name.
    * @param id
    *           String state cross-reference ID.
    * @throws Exception
    */
   public SubmachineState(String name, String id) throws Exception
   {
      super(name, id);
   }

   public SubmachineState()
   {
      super();
   }

   public void setStateMachine( StateMachine sm )
   {
      this.stateMachine = sm;
   }

   public StateMachine getStateMachine()
   {
      return stateMachine;
   }

   /**
    * Sets the submachine state machine for this state
    * 
    * @param submachine
    *           The state machine to add
    */
   public void setSubmachine(String submachine) 
   {
     this.submachine = submachine;
   }

   /**
    * Returns the submachine for this state.
    * 
    * @return The submachine
    */
   public String getSubmachine()
   {
      return submachine;
   }
}
