package gov.nasa.jpl.statechart.core;

/**
 * <p>
 * Subclass of DiagramElement, extended to represent a text box.
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
 * CVS Identification: $Id: DiagramTextElement.java,v 1.2 2008/04/22 00:16:50 wkiri Exp $
 * </p>
 */
public class DiagramTextElement extends DiagramElement
{
   static int anonymousTextNum = 0;
   String     text;

   public DiagramTextElement(String text, CoordinatePoint p0) throws Exception
   {
      this.text = text;
      addPoint(p0);
   }

   public String[] toPython() throws Exception
   {
      String pythonLines[] = new String[1];
      CoordinatePoint p0 = (CoordinatePoint) pointList.get(0);
      text = text.replace('"', ' ');
      text = text.replace("\n", "\\n");
      pythonLines[0] = "self.textBox" + ++anonymousTextNum + " = self."
	+ canvasName + ".create_text(" + p0.x + ", " + (p0.y-5) + ", text=\""
	+ text + "\"" + ", anchor=N, font = (\"Times\", 11));";

      return pythonLines;
   }
}
