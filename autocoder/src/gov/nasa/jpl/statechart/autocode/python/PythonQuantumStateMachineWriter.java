/**
 * Created Aug 24, 2009.
 * <p>
 * Copyright 2009-2010, by the California Institute of Technology. ALL RIGHTS
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
package gov.nasa.jpl.statechart.autocode.python;

import gov.nasa.jpl.statechart.autocode.IGenerator;
import gov.nasa.jpl.statechart.autocode.QuantumStateMachineWriter;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.template.FunctionCall;
import gov.nasa.jpl.statechart.template.OOVelocityModel;
import gov.nasa.jpl.statechart.uml.Namespace;
import gov.nasa.jpl.statechart.uml.StateMachine;

import java.io.Writer;

/**
 * <p>
 * This class writes out a state machine in the form of Python code based on the
 * Quantum Framework model developed by Miro Samek.  As handled by the parent
 * {@link QuantumStateMachineWriter} class, Python code is generated from an
 * internal representation of UML metaclasses, written out via Velocity templates.
 * </p><p>
 * Copyright &copy; 2009--2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class PythonQuantumStateMachineWriter extends QuantumStateMachineWriter<UMLToPythonMapper> {
    protected static final String QP_MAIN = "qp-main.vm";
    /** Filenames for code template parts */
    protected static final String QP_METHOD_ACTION       = "qp-method-action.vm";
    protected static final String QP_METHOD_GUARD        = "qp-method-guard.vm";

    /**
     * Main constructor, creates a writer given a UML Model Group.
     * 
     * @param modelGrp  the group of UML model(s) used for writing target output.
     */
    public PythonQuantumStateMachineWriter (ModelGroup modelGrp) {
        super(modelGrp, new UMLToPythonMapper());
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getTargetLabel()
     */
    @Override
    protected String getTargetLabel () {
        return IGenerator.Kind.Python.label();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.QuantumStateMachineWriter#inlineMainTemplate()
     */
    @Override
    protected String inlineMainTemplate () {
        return QP_MAIN;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.QuantumStateMachineWriter#writeStateMachine(gov.nasa.jpl.statechart.uml.StateMachine)
     */
    protected void writeStateMachine (StateMachine stateMachine) {
        String namestub = tMapper.mapToFileName(stateMachine);

        // Create the proxy model query object for Velocity
        tModel = new OOVelocityModel(stateMachine);
        tContext.put("model", tModel);
        // flag indicating if this StateMachine has a corresponding Impl class
        boolean hasImplClass = tModel.getCallActions(stateMachine, true).size() > 0;
        tContext.put("hasImpl", hasImplClass);

        // Write Python code to a file
        String pyFilename = namestub + "Active.py";
        Writer w = beginWriteCode(pyFilename);
        composeCode(w);
        endWriteCode(pyFilename);

        // Write impl-class code, but only if there's one or more call-actions
        if (hasImplClass) {
            String implFilename = namestub + "Impl.py";
            if (fileAsPath(implFilename).exists()) {  // do not override
                System.out.println(getTargetLabel() + " target " + implFilename + " exists, will not override!");
            } else {
                writeCode(implFilename, QP_IMPL);
            }
        }

        tContext.remove("hasImpl");
        tContext.remove("model");
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.QuantumStateMachineWriter#makeMoreCommonMethods(java.io.Writer, gov.nasa.jpl.statechart.uml.Namespace)
     */
    @Override
    protected void makeMoreCommonMethods (Writer w, Namespace ns) {
        // Methods corresponding to guards and Entry/Exit actions
        for (FunctionCall action : tModel.getCallActions(ns, false)) {
            tContext.put("funcCall", action);
            if (action.isGuard()) {
                writeCodePart(w, QP_METHOD_GUARD);
            } else {
                writeCodePart(w, QP_METHOD_ACTION);
            }
            tContext.remove("actionName");
        }
    }

}
