package gov.nasa.jpl.statechart.core;

/**
 * <p>
 * A transition timeout specifies a duration value after which a transition is
 * to be taken.
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
 * This software is subject to U.S. export control laws and regulations and has
 * been classified as 4D993. By accepting this software, the user agrees to
 * comply with all applicable U.S. export laws and regulations. User has the
 * responsibility to obtain export licenses, or other export authority as may be
 * required before exporting such information to foreign countries or providing
 * access to foreign persons.
 * </p>
 * 
 * <p>
 * CVS Identification: $Id: TransitionTimeout.java,v 1.1.2.1 2005/11/23 18:15:04
 * kclark Exp $
 * </p>
 */
public class TransitionTimeout
{
   private int timeout;
   private boolean hasinttimeout;
   private String timeouttext;

   public TransitionTimeout(String text) 
   {
      try
      {
        timeout = Integer.parseInt(text);
        hasinttimeout = true;
      }
      catch(NumberFormatException exception)
      {
         hasinttimeout = false;
         timeouttext = text;                  
      }
   }
   
   public boolean isNumericTimeout()
   {
      return hasinttimeout;      
   }

   /**
    * Returned value is in units of system timer "ticks", which is
    * implementation dependent.
    * 
    * @return int Number of timer ticks after which the transition is taken.
    */
   public String getTimeout()
   {
      if(hasinttimeout)
      {
         return ""  + timeout;
      }
      else
      {
         return timeouttext;  
      }
    }
}
