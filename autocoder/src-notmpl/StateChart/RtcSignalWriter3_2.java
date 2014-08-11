package StateChart;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.autocode.gui.ExecutionTracePythonWriter;
import gov.nasa.jpl.statechart.input.magicdraw.MagicDrawUmlReader;

/**
 * <p>
 * Finds all specified signals (events) in a set on input XML files, and
 * generates a corresponding C++ enumerated type declaration. Also, optionally
 * generates the corresponding execution trace (Python) file.
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
 * CVS Identification: $Id: SimRtcSignalWriter.java,v 1.18 2005/10/11 18:08:09
 * kclark Exp $
 * </p>
 */
public abstract class RtcSignalWriter3_2 {
    /**
     * Finds all specified signals (events) in the <code>args</code> XML
     * file(s), and generates a corresponding C++ enumerated type declaration.
     * Also, optionally generates the corresponding execution trace (Python)
     * file.
     * 
     * @param args
     *           String[] List of XML files containing signal specifications.
     */
    @SuppressWarnings("deprecation")
    public static void main (String[] args) throws Exception {
        // Create the UML reader object which is used for the SIM RTC project:
        MagicDrawUmlReader reader = new MagicDrawUmlReader();

        // reader.setSignalsOnly();
        // Parse the XML files:
        reader.parseXmlFiles(args);

        // Create the enumerated type listing all signals found in the XML files:
        new gov.nasa.jpl.statechart.autocode.cm.RtcSignalWriter3_2(reader).writeSignalEnum();

        // Check a system property to see if we should generate the execution trace file:
        if (Autocoder.isExecutionTraceOn()) {
            new ExecutionTracePythonWriter(reader).writeSignalTraceFile();
        }

        System.out.println("Finished.");
    }
}
