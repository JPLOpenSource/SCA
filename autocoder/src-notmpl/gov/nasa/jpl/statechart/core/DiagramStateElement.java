package gov.nasa.jpl.statechart.core;

/**
 * <p>
 * Subclass of DiagramElement, extended to represent a state.
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
 * CVS Identification: $Id: DiagramStateElement.java,v 1.1.2.1 2005/11/23
 * 18:15:03 kclark Exp $
 * </p>
 */
public class DiagramStateElement extends DiagramElement
{
   static int anonymousStateNum = 0;
   State      state;
  StateMachine machine; // Store the state machine as well
   boolean    hasFillColor      = false;
   int        fillColor;

  // Take in the machine as an argument so we can get fully-scoped names
  public DiagramStateElement(State state, StateMachine machine,
			     CoordinatePoint p0, CoordinatePoint p1)
    throws Exception
   {
      addPoint(p0);
      addPoint(p1);
      this.state = state;
      this.machine = machine;
   };

   public static final int defaultPseudostateFillColor = 0x0000ff;
   public static final int defaultStateFillColor       = 0xffffcc;

   /**
    * Method to specify a fill color for the diagram state element.
    * 
    * @param value
    *           int Integer representation of the fill color in the format
    *           "rrggbb", where "rr" are two hex digits (one byte) specifying
    *           red intensity, "gg" are two hex digits specifying green
    *           intensity, and "bb" are two hex digits specifying blue
    *           intensity. The six hex digits "rrggbb" together are specified as
    *           one 24 bit unsigned integer value.
    */
   public void addFillColor(int value)
   {

      fillColor = value;
      hasFillColor = true;
   }
   public int getFillColor ()
   {
      return fillColor;
   }

   private String fillColorText()
   {

      if (hasFillColor)
      {
	CompositeStateRegion enclosingRegion =
	  machine.getEnclosingRegion(state);
	if (enclosingRegion != null) {
	  String fullPath = machine.getQualifiedPath(enclosingRegion, "");
	  fullPath = fullPath.replace( ":", "_" ) + state.name();
	  fullPath = fullPath.substring(fullPath.lastIndexOf("_")+1);
	  return new String(", fill=self.colorDict['" + fullPath + "']");
	} else {
	  // These are things you cannot enter (and update color of),
	  // so we can hard-code their color.
	  return new String(", fill=\"#" + toHexString(fillColor, 6) + "\"");
	}
      } else
      {
         return new String("");
      }
   }

   public String[] toPython() throws Exception
   {

      String pythonLines[];
      CoordinatePoint p0 = (CoordinatePoint) pointList.get(0);
      CoordinatePoint p1 = (CoordinatePoint) pointList.get(1);
      // Put together the full path to this state, if needed
      CompositeStateRegion enclosingRegion = machine.getEnclosingRegion(state);
      String fullPath = "";
      if ( enclosingRegion != null ) {
	// Keep the names shorter by not sending in the machine name: ""
	fullPath = machine.getQualifiedPath( enclosingRegion, "");
	fullPath = fullPath.replace( ":", "_" ) + state.name();
	fullPath = fullPath.substring(fullPath.lastIndexOf("_")+1);
      } else {
	fullPath = state.name();
      }

      if (state instanceof InitialState || state instanceof JunctionState)
      {
         pythonLines = new String[1];
         pythonLines[0] = "self." + fullPath + " = self." + canvasName
               + ".create_oval(" + p0.x + ", " + p0.y + ", " + p1.x + ", "
               + p1.y + fillColorText() + ")";
      } else if (state instanceof FinalState)
      {
         final int circleOffset = 4;
         pythonLines = new String[2];
         pythonLines[0] = "self." + fullPath + " = self." + canvasName
               + ".create_oval(" + (p0.x + circleOffset) + ", "
               + (p0.y + circleOffset) + ", " + (p1.x - circleOffset) + ", "
               + (p1.y - circleOffset) + fillColorText() + ")";
         pythonLines[1] = "self." + fullPath + "2 = self." + canvasName
               + ".create_oval(" + p0.x + ", " + p0.y + ", " + p1.x + ", "
               + p1.y + ", outline = \"blue\")";

      } else if (state instanceof DeepHistoryState)
      {
         pythonLines = new String[2];
         pythonLines[0] = "self." + fullPath + " = self." + canvasName
               + ".create_oval(" + p0.x + ", " + p0.y + ", " + p1.x + ", "
               + p1.y + fillColorText() + ", width=2, outline = \"blue\")";
         pythonLines[1] = "self.stateText" + ++anonymousStateNum + " = self."
               + canvasName + ".create_text(" + (p0.x + ((p1.x - p0.x) / 2))
               + ", " + (p0.y + 5) + ", text=\"" + "H*" + "\""
               + ", anchor=N, font = (\"Times\", 12, \"bold\"));";

      } else
      {
         pythonLines = new String[2];
         pythonLines[0] = "self." + fullPath + " = self." + canvasName
               + ".create_rectangle(" + p0.x + ", " + p0.y + ", " + p1.x + ", "
               + p1.y + fillColorText() + ", width=2, outline = \"blue\")";
         pythonLines[1] = "self.stateText" + ++anonymousStateNum + " = self."
               + canvasName + ".create_text(" + (p0.x + ((p1.x - p0.x) / 2))
               + ", " + (p0.y + 5) + ", text=\"" + fullPath + "\""
               + ", anchor=N, font = (\"Times\", 12, \"bold\"));";

      }
      return pythonLines;
   }
}
