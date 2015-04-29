package gov.nasa.jpl.statechart.core;

/**
 * <p>
 * A line which separates concurrent regions within a state machine.
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
 * CVS Identification: $Id: DiagramSeparatorElement.java,v 1.1.2.1 2005/11/23
 * 18:15:03 kclark Exp $
 * </p>
 */
public class DiagramSeparatorElement extends DiagramElement
{
   private int[]      points;
   private static int sepcount = 0;

   public DiagramSeparatorElement(int newpoints[])
   {
      assert (newpoints.length == 4);
      points = newpoints;
   }

   public String[] toPython() throws Exception
   {
      // self.separator = self.can.create_line(233, 422, 233 + 584, 422,
      // width=1, fill = "blue", stipple="gray25")
      String[] strings = new String[1];
      int x, y, x2, y2;
      x = points[0];
      y = points[1];
      x2 = x + points[2];
      y2 = y + points[3];
      strings[0] = "self.separator" + sepcount + " = self.can.create_line(" + x
            + "," + y + "," + x2 + "," + y2
            + ", width=1, fill = \"blue\", stipple=\"gray25\")";
      sepcount++;
      return strings;
   }
}
