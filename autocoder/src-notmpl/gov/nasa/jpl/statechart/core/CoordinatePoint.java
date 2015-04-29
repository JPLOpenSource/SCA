package gov.nasa.jpl.statechart.core;

/**
 * <p>
 * Specifies one (x,y) point on a GUI display.
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
 * CVS Identification: $Id: CoordinatePoint.java,v 1.1.2.1 2005/11/23 18:15:02
 * kclark Exp $
 * </p>
 */
public class CoordinatePoint
{
   public int x;
   public int y;

   public CoordinatePoint(int x, int y) throws Exception
   {
      if (x < 0 || y < 0)
      {
         throw new Exception(
               "Coordinate point x or y values cannot be negative.");
      }
      this.x = x;
      this.y = y;
   }
}
