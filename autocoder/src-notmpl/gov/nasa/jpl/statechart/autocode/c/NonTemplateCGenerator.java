/**
 * Created Aug 6, 2009.
 * <p>
 * Copyright 2009, by the California Institute of Technology. ALL RIGHTS
 * RESERVED. United States Government Sponsorship acknowledged. Any commercial
 * use must be negotiated with the Office of Technology Transfer at the
 * California Institute of Technology.
 * </p>
 * <p>
 * This software is subject to U.S. export control laws and regulations and has
 * been classified as 4D993. By accepting this software, the user agrees to
 * comply with all applicable U.S. export laws and regulations. User has the
 * responsibility to obtain export licenses, or other export authority as may be
 * required before exporting such information to foreign countries or providing
 * access to foreign persons.
 * </p>
 */
package gov.nasa.jpl.statechart.autocode.cm;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.autocode.GeneratorKind;
import gov.nasa.jpl.statechart.autocode.IGenerator;
import gov.nasa.jpl.statechart.autocode.IWriter;
import gov.nasa.jpl.statechart.autocode.gui.ExecutionTracePythonWriter;
import gov.nasa.jpl.statechart.input.IReader;
import gov.nasa.jpl.statechart.input.magicdraw.MagicDrawUmlReader;

import java.util.ArrayList;
import java.util.List;

/**
 * This generator writes C output using the ad-hoc, print-statement style.
 * The workflow is simply:<ol>
 * <li> Generate C-header and code
 * <li> Generate global header of StateChart signals
 * </ol>
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
@GeneratorKind(IGenerator.Kind.CNonTemplate)
public class NonTemplateCGenerator implements IGenerator {

    private MagicDrawUmlReader reader = null;
    private List<IWriter> flowOfWriters = null;  // TODO pull up to abstract class

    /**
     * Default constructor, initializes writers.
     */
    public NonTemplateCGenerator () {
//        init();  // TODO pull up to abstract class
        // Create the UML reader object which is used for the SIM RTC project:
        reader = new MagicDrawUmlReader();
        flowOfWriters = new ArrayList<IWriter>();
        flowOfWriters.add(new CQuantum3_2StateMachineWriter(reader));
        flowOfWriters.add(new RtcSignalWriter3_2(reader));
        // Check a system property to see if we should generate the execution trace files:
        if (Autocoder.isExecutionTraceOn()) {
            flowOfWriters.add(new ExecutionTracePythonWriter(reader));
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.IGenerator#generate(java.lang.String[])
     */
    public void generate (String[] sources) {
        // Parse the XML files
        try {
            reader.parseXmlFiles(sources);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // after parsing all the files, linearly run the writers
        doLinearFlow();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.IGenerator#generate(gov.nasa.jpl.statechart.input.IReader, java.lang.String[])
     */
    public void generate (IReader newReader, String[] sources) {
        generate(sources);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.IGenerator#reader()
     */
    public IReader reader () {
        return null;  // not implemented, would need to change old reader's interface
    }

    protected void doLinearFlow () {
        for (IWriter w : flowOfWriters) {
            w.write();
        }
    }

}
