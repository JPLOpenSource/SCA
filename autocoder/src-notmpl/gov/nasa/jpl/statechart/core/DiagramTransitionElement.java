package gov.nasa.jpl.statechart.core;

/**
 * <p>
 * Subclass of DiagramElement, extended to represent a state transition.
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
 * CVS Identification: $Id: DiagramTransitionElement.java,v 1.1.2.1 2005/11/23
 * 18:15:03 kclark Exp $
 * </p>
 */
public class DiagramTransitionElement extends DiagramElement
{
   static int anonymousTranNum = 0;
   String     name;
   boolean    toSelf           = false;

   public String name()
   {
      return this.name;
   }

   public DiagramTransitionElement(String name)
   {
      this.name = name;
   };

   public String[] toPython() throws Exception
   {
      anonymousTranNum++;
      CoordinatePoint pMid; // The midpoint in the transition line...
      String pythonLines[] = new String[1];
      pythonLines[0] = "self.tran" + anonymousTranNum + " = self." + canvasName
            + ".create_line(";
      if (this.toSelf && 2 == pointList.size())
      {
         CoordinatePoint p0 = (CoordinatePoint) pointList.get(0);
         CoordinatePoint p1 = (CoordinatePoint) pointList.get(1);
         pMid = new CoordinatePoint(p0.x + ((p1.x - p0.x) / 2), p0.y - 75);
         pointList.add(1, pMid);
      }
      int pointIndex;
      for (pointIndex = 0; pointIndex < pointList.size(); pointIndex++)
      {
         CoordinatePoint p = (CoordinatePoint) pointList.get(pointIndex);
         String pointString = new String(p.x + ", " + p.y + ", ");
         pythonLines[0] += pointString;
      }
      if (this.toSelf)
      {
         pythonLines[0] += "width=2, arrow=LAST, smooth=TRUE, fill = \"red\")";
      } else
      {
         pythonLines[0] += "width=2, arrow=FIRST, fill = \"red\")";
      }
      return pythonLines;
   }
}
