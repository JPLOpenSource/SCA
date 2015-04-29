/**
 * Created Aug 24, 2009.
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
package gov.nasa.jpl.statechart.autocode.oldpython;

import gov.nasa.jpl.statechart.autocode.IGenerator;
import gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.template.OOVelocityModel;
import gov.nasa.jpl.statechart.uml.StateMachine;

import java.io.File;

/**
 * <p>
 * This class writes out a state machine in the form of Python code based on the
 * Quantum Framework model developed by Miro Samek.  The code is generated
 * from an internal representation of UML metaobject classes, written out via
 * Velocity templates.
 * </p>
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class PythonQuantumStateMachineWriter extends UMLStateChartTemplateWriter {
    /** Filename for velocity macro */
    protected static final String QPPY_MACROS = "qppy-velocimacros.vm";
    /** Filename for main template */
    protected static final String QPPY_CODE = "qppy-code.vm";
    /** Filename for impl-class template */
    protected static final String QPPY_IMPL = "qppy-implclass.vm";

    /**
     * Main constructor, creates a writer given a UML Model Group.
     * 
     * @param modelGrp  the group of UML model(s) used for writing target output.
     */
    public PythonQuantumStateMachineWriter (ModelGroup modelGrp) {
        super(modelGrp, new UMLToPythonMapper());
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.IWriter#write()
     */
    public void write () {
        // create writer-specific files for each of the top-level state machines
        for (StateMachine stateMachine : tModelGrp.getStateMachines()) {
            writeStateMachine(stateMachine);
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getVelociMacroFile()
     */
    @Override
    protected String getVelociMacroFile () {
        return QPPY_MACROS;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getTargetLabel()
     */
    @Override
    protected String getTargetLabel () {
        return IGenerator.Kind.Python.label();
    }

    private void writeStateMachine (StateMachine stateMachine) {
        String namestub = tMapper.mapToFileName(stateMachine);

        // Create the proxy model query object for Velocity
        OOVelocityModel qm = new OOVelocityModel(stateMachine);
        tContext.put("model", qm);

        // Write Python code to a file
        String pyFilename = namestub + "Active.py";
        writeCode(pyFilename, QPPY_CODE);

        // Write impl-class code, but only if there's one or more call-actions
        if (qm.getAllCallActions().size() > 0) {
            String implFilename = namestub + "Impl.py";
            if (new File(implFilename).exists()) {  // do not override
                System.out.println(getTargetLabel() + " target " + implFilename + " exists, will not override!");
            } else {
                writeCode(implFilename, QPPY_IMPL);
            }
        }
    }

}
