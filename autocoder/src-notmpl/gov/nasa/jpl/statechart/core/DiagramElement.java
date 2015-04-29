package gov.nasa.jpl.statechart.core;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Defines GUI display elements of a state transition diagram.
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
 * CVS Identification: $Id: DiagramElement.java,v 1.13.2.1 2005/11/23 18:15:02
 * kclark Exp $
 * </p>
 */
public class DiagramElement
{
   public DiagramElement()
   {
      try
      {
         jbInit();
      } catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }

   public static final String      canvasName = "can";
   protected List<CoordinatePoint> pointList  = new ArrayList<CoordinatePoint>();

   /**
    * Adds a coordinate point to the point list for this diagram element. The
    * points are used to specify the diagram element's size and location.
    * 
    * @param point
    *           CoordinatePoint
    * @throws Exception
    */
   public void addPoint(CoordinatePoint point) throws Exception
   {
      this.pointList.add(point);
   }

   /**
    * Returns the point list for this diagram element.
    * 
    * @return List - the list of coordinate points defining the diagram element.
    */
   public List pointList()
   {
      return this.pointList;
   }

   /**
    * Generates and returns an array of strings containing lines of Python code
    * which display the diagram element. Subclasses should override this method
    * to return the appropriate Python code.
    * 
    * @return String[] Array containing lines of Python code.
    * @throws Exception
    */
   public String[] toPython() throws Exception
   {
      return null;
   }

   public String toHexDigit(int value, int digitPosition)
   {
      int digitValue = (value >>> (digitPosition * 4)) & 0xf;
      return Integer.toHexString(digitValue);
   }

   public String toHexString(int value, int numDigits)
   {
      String result = new String("");
      for (int i = (numDigits - 1); i >= 0; i--)
      {
         String digit = toHexDigit(value, i);
         result += digit;
      }
      return result;
   }

   private void jbInit() throws Exception
   {
   }
}
