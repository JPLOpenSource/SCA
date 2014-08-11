/**
 * Created Aug 5, 2009.
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

import gov.nasa.jpl.statechart.autocode.IGenerator;
import gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.template.VelocityModel;
import gov.nasa.jpl.statechart.uml.StateMachine;

/**
 * <p>
 * This class writes out a state machine in the form of C code based on the
 * Quantum Framework model developed by Miro Samek. The code is generated
 * from an internal representation of UML metaobject classes, written out via
 * Velocity templates.
 * </p>
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author adapted and refactored from original by Shang-Wen Cheng <scheng@jpl.nasa.gov>
 */
public class CQuantumStateMachineWriter extends UMLStateChartTemplateWriter<UMLToCMapper> {
    // Filename extensions for submachines
    protected static final String submachineCodeExtension = "subc";
    protected static final String submachineHeaderExtension = "subh";

    // Filenames for main templates
    protected static final String QPC_MACROS = "qpc-velocimacros.vm";
    protected static final String QPC_HEADER = "qpc-header.vm";
    protected static final String QPC_CODE = "qpc-code.vm";
    protected static final String QPC_IMPL_HEADER = "qpc.h-implfile.vm";
    protected static final String QPC_IMPL_CODE = "qpc.c-implfile.vm";

    /**
     * Main constructor, calls super with a Model Group.
     */
    public CQuantumStateMachineWriter (ModelGroup modelGrp) {
        super(modelGrp, new UMLToCMapper());
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
        return QPC_MACROS;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getTargetLabel()
     */
    @Override
    protected String getTargetLabel () {
        return IGenerator.Kind.Cm.label();
    }

    /**
     * Writes the supplied state machine to target file.
     * @param stateMachine  the UML StateMachine for which to write target file.
     */
    private void writeStateMachine (StateMachine stateMachine) {
        String namestub = tMapper.mapToFileName(stateMachine);
        String declarationsFilename = namestub + ".h";
        String definitionsFilename = namestub + ".c";

        // Create the proxy objects that expose the type information
        tContext.put("model", new VelocityModel(stateMachine));

        // Write the code to a file
        writeCode(declarationsFilename, QPC_HEADER);
        writeCode(definitionsFilename, QPC_CODE);

        // Recursively generate the code for all the submachines contained
        // within this state machine.
        /*
        for ( StateChart.uml.State submachineState : helper.getSubmachineStates( stateMachine ))
        {
           writeSubmachine( submachineState );
        }
        */

        // Write the impl files, _always_, but do not override!
        String implDeclarationsFilename = namestub + "Impl.h";
        String implDefinitionsFilename = namestub + "Impl.c";
        if (fileAsPath(implDeclarationsFilename).exists()) {  // do not override
            System.out.println(getTargetLabel() + " target " + implDeclarationsFilename + " exists, will not override!");
        } else {
            writeCode(implDeclarationsFilename, QPC_IMPL_HEADER);
        }
        if (fileAsPath(implDefinitionsFilename).exists()) {  // do not override
            System.out.println(getTargetLabel() + " target " + implDefinitionsFilename + " exists, will not override!");
        } else {
            writeCode(implDefinitionsFilename, QPC_IMPL_CODE);
        }
    }

}
