package gov.nasa.jpl.statechart.core;

import java.util.*;

/**
 * <p>
 * Designate a transition into or out of a submachine state
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
 * CVS Identification: $Id: ConnectionPointReference.java,v 1.1 2008/01/31 01:28:43 lscharen Exp $
 * </p>
 */
public class ConnectionPointReference extends State
{
   protected String link = null;

   public ConnectionPointReference(String name, String id)
   {
      super(name, id);

      incoming = new ArrayList<Transition>();
      outgoing = new ArrayList<Transition>();
      entryActions = new ArrayList<Action>();
      duringActions = new ArrayList<Action>();
      exitActions = new ArrayList<Action>();
   }

  public void setLink( String state )
  {
    link = state;
  }

  public String getLink()
  {
    return link;
  }
}
