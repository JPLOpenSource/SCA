package StateChart;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.autocode.IWriter;
import gov.nasa.jpl.statechart.autocode.cm.CQuantumStateMachineWriter;
import gov.nasa.jpl.statechart.autocode.gui.ExecutionTracePythonWriter;
import gov.nasa.jpl.statechart.core.DiagramElement;
import gov.nasa.jpl.statechart.core.State;
import gov.nasa.jpl.statechart.core.StateMachine;
import gov.nasa.jpl.statechart.core.Transition;
import gov.nasa.jpl.statechart.input.magicdraw.MagicDrawReader;
import gov.nasa.jpl.statechart.input.magicdraw.MagicDrawUmlReader;

/**
 * Main class for CQuantum3_2StateMachineWriter.
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
 * CVS Identification: $Id: CSimQuantumStateMachineWriter.java,v 1.7 2005/10/11
 * 18:08:09 kclark Exp $
 * </p>
 * 
 * @see StateMachine
 * @see State
 * @see Transition
 * @see DiagramElement
 */
public abstract class CQuantum3_2StateMachineWriter implements IWriter {
    /**
     * Transforms the state machine(s) specified in the <code>args</code> XML
     * file(s) into C implementations based upon the Quantum Framework. Also,
     * optionally generates the corresponding execution trace (Python) files.
     * 
     * @param args
     *           String[] List of XML files containing state machine
     *           specifications.
     */
    @SuppressWarnings("deprecation")
    public static void main (String[] args) throws Exception {
        System.setProperty("jpl.autocode.c", "true");

        // If -dom is an argument, just process the last file
        for (String arg : args) {
            if (arg.equals("-dom")) {
                MagicDrawReader reader = new MagicDrawReader();
                // Read the UML model and write out the state machines
                new CQuantumStateMachineWriter(reader.read(args[args.length - 1])).write();
                return;
            }
        }

        // Create the UML reader object which is used for the SIM RTC project:
        MagicDrawUmlReader reader = new MagicDrawUmlReader();
        // Parse the XML files:
        reader.parseXmlFiles(args);

        // Create the C implementations of the state machines found in the XML files:
        new gov.nasa.jpl.statechart.autocode.cm.CQuantum3_2StateMachineWriter(reader).writeAllStateMachines();

        // Check a system property to see if we should generate the execution trace files:
        if (Autocoder.isExecutionTraceOn()) {
            new ExecutionTracePythonWriter(reader).writeAllStateMachineTraceFiles();
        }

        System.out.println("Finished.");
    }
}
