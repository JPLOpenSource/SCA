package gov.nasa.jpl.statechart.core;

import java.util.*;

/**
 * <p>
 * Designate an alternate entry point into a submachine.
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
 * CVS Identification: $Id: EntryPoint.java,v 1.1 2008/01/31 01:28:43 lscharen Exp $
 * </p>
 */
public class EntryPoint extends State
{
   public EntryPoint(String name, String id)
   {
      super(name, id);

      outgoing = new ArrayList<Transition>();
      exitActions = new ArrayList<Action>();
   }
}
