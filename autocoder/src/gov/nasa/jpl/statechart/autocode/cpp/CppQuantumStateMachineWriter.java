/**
 * Created Oct 26, 2009.
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
package gov.nasa.jpl.statechart.autocode.cpp;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.Autocoder.SignalNamespaceType;
import gov.nasa.jpl.statechart.autocode.IGenerator;
import gov.nasa.jpl.statechart.autocode.QuantumStateMachineWriter;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.template.OOVelocityModel;
import gov.nasa.jpl.statechart.uml.Namespace;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.StateMachine;

import java.io.Writer;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * This class writes out a state machine in the form of C++ code based on the
 * Quantum Framework model developed by Miro Samek.  The code is generated
 * from an internal representation of UML metaobject classes, written out via
 * Velocity templates.
 * </p><p>
 * [2010.01.20] Previously, the logic for generating code resided largely in
 * the Velocity templates, resulting in compact templates, but making
 * target-code pattern not easily discernible.  The new implementation pulled
 * the logic into Java code, dicing up target-code patterns in many more chunks
 * of Velocity files.
 * </p><p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class CppQuantumStateMachineWriter extends QuantumStateMachineWriter<UMLToCppMapper> {
    protected static final String VMACRO_FOOTER_NAMESPACES = "footerNamespaces";
    protected static final String VMACRO_FOOTER_HEADERDEF = "footerHeaderDef";

    /** Filename for impl header template */
    protected static final String QP_H_IMPL = "qp.h-impl.vm";

    /** Filename for header template parts */
    protected static final String QP_H_INCLUDES       = "qp.h-includes.vm";
    protected static final String QP_H_STATE_ENUM     = "qp.h-stateenum.vm";
    protected static final String QP_H_CLASS_ACTIVE   = "qp.h-class-active.vm";
    protected static final String QP_H_CLASS_HSM      = "qp.h-class-hsm.vm";
    protected static final String QP_H_CLASS_STATES   = "qp.h-class-states.vm";
    protected static final String QP_H_CLASS_PRIVATE  = "qp.h-class-private.vm";

    /**
     * Main constructor, creates a writer given a UML Model Group.
     * 
     * @param modelGrp  the group of UML model(s) used for writing target output.
     */
    public CppQuantumStateMachineWriter (ModelGroup modelGrp) {
        super(modelGrp, new UMLToCppMapper());
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getTargetLabel()
     */
    @Override
    protected String getTargetLabel () {
        return IGenerator.Kind.Cpp.label();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.QuantumStateMachineWriter#inlineMainTemplate()
     */
    @Override
    protected String inlineMainTemplate () {
        return null;  // C++ main method is a separate file
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.QuantumStateMachineWriter#makeMoreCommonMethods(java.io.Writer, gov.nasa.jpl.statechart.uml.Namespace)
     */
    @Override
    protected void makeMoreCommonMethods (Writer w, Namespace ns) {
        // no additional common methods
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.QuantumStateMachineWriter#writeStateMachine(gov.nasa.jpl.statechart.uml.StateMachine)
     */
    protected void writeStateMachine (StateMachine stateMachine) {
        // Create the proxy objects that expose the type information
        tModel = new OOVelocityModel(stateMachine);

        String[] pkgNames = null;
        if (Autocoder.isNamespaceEnabled()) {
            pkgNames = stateMachine.getPackageNames();
        }
        String classname = tMapper.mapToFileName(stateMachine, pkgNames);
        String declarationsFilename = classname + ".h";
        String definitionsFilename = classname + ".cpp";

        // set up some "global" context variables for later templates
        tContext.put("model", tModel);
        tContext.put("sm", stateMachine);
        tContext.put("smName", tMapper.mapToTypeName(stateMachine));
        // Define vars for the StateMachine namespaces, if applicable
        if (Autocoder.isNamespaceEnabled()) {
            tContext.put("qfNs", tMapper.mapToNamespacePrefix(Autocoder.inst().getQfNamespace()));
            tContext.put("nsPrefix", tMapper.mapToNamespacePrefix(pkgNames));
            tContext.put("nsDefPrefix", Util.join(pkgNames, ""));
            tContext.put("nsPathPrefix", tMapper.mapToNamespacePathPrefix(pkgNames));
            tContext.put("smDefName", tMapper.mapToDefName(stateMachine, pkgNames));
        } else {  // no namespace
            tContext.put("qfNs", "");
            tContext.put("nsPrefix", "");
            tContext.put("nsDefPrefix", "");
            tContext.put("nsPathPrefix", "");
            tContext.put("smDefName", tMapper.mapToDefName(stateMachine));
        }
        if (Autocoder.inst().getSignalNamespaceType() == SignalNamespaceType.NONE) {
            tContext.put("baseSigNs", "");
            tContext.put("baseSigNsPath", "");
        } else {
            // determine the file path of the base signal file
            String[] sigBasePkgs = Util.splitPreservingQuotes(Autocoder.inst().getSigBaseNamespace(), Util.PACKAGE_SEP);
            tContext.put("baseSigNs", tMapper.mapToNamespacePrefix(sigBasePkgs));
            tContext.put("baseSigNsPath", tMapper.mapToNamespacePathPrefix(sigBasePkgs));
        }

        // Compose and write the header file
        Writer w = beginWriteCode(declarationsFilename);
        composeHeader(w);
        endWriteCode(declarationsFilename);

        // Compose and write the code file
        w = beginWriteCode(definitionsFilename);
        composeCode(w);
        //- complete the source code file
        tContext.put("sm", stateMachine);
        tContext.put("smName", tMapper.mapToTypeName(stateMachine));
        if (Autocoder.isNamespaceEnabled()) {
            invokeVmacro(w, VMACRO_FOOTER_NAMESPACES, "composeCode", EMPTY_PARAMS);
        }
        endWriteCode(definitionsFilename);

        // Write the Impl class files, _always_, but do not override!
        String implDeclarationsFilename = classname + "Impl.h";
        String implDefinitionsFilename = classname + "Impl.cpp";
        if (fileAsPath(implDeclarationsFilename).exists()) {  // do not override
            System.out.println(getTargetLabel() + " target " + implDeclarationsFilename + " exists, will not override!");
        } else {
            writeCode(implDeclarationsFilename, QP_H_IMPL);
        }
        if (fileAsPath(implDefinitionsFilename).exists()) {  // do not override
            System.out.println(getTargetLabel() + " target " + implDefinitionsFilename + " exists, will not override!");
        } else {
            writeCode(implDefinitionsFilename, QP_IMPL);
        }

        // remove context vars
        tContext.remove("model");
        tContext.remove("sm");
        tContext.remove("smName");
        tContext.remove("qfNs");
        tContext.remove("nsPrefix");
        tContext.remove("nsDefPrefix");
        tContext.remove("nsFilePrefix");
        tContext.remove("smDefName");
        tContext.remove("baseSigNs");
    }

    /**
     * Builds the C++ header file, starting with the includes, then composing
     * State enums and class declarations for the State Machine and orthogonal
     * regions.
     * 
     * @param w  FileWriter to the C++ header file.
     */
    private void composeHeader (Writer w) {
        // Header imports
        writeCodePart(w, QP_H_INCLUDES);

        // Enum of all states
        if (tModel.hasMachineEntryPoint() || tModel.hasMachineExitPoint()) {
            // submachine with entry/exit points; pass in all connection points
            tContext.put("connPointSet", tModel.getConnectionPoints(tModel.getStateMachine(), false));
        } else {
            tContext.put("connPointSet", Collections.emptySet());
        }
        writeCodePart(w, QP_H_STATE_ENUM);

        boolean isHsm = false;  // flag indicating whether or NOT inside Active
        // Class declarations for the State Machine and regions
        for (Namespace machineOrRegion : tModel.getAllClassLevelElements()) {
            //- determine whether to enable BAIL_SIG/CompletionEvt/reinit/final
            tContext.put("SM_TERMINABLE", tModel.isMachineTerminable(machineOrRegion));
            //- determine whether to enable BAIL_EVENT
            tContext.put("SM_TERMINATOR", tModel.isMachineTerminator(machineOrRegion));
            //- determine whether explicit final state exists
            tContext.put("SM_HAS_FINAL_STATE", !tModel.getFinalStates(machineOrRegion).isEmpty());
            tContext.put("typeObj", machineOrRegion);

            if (machineOrRegion instanceof StateMachine) {
                // generate Active class declaration, data member portion
                StateMachine m = (StateMachine) machineOrRegion;
                isHsm = false;
                tContext.put("isQHsm", isHsm);  // flag affects Active param type
                tContext.put("typeName", evalExpr("#mapToTypeName($sm,\"\")", "composeHeader", tMapper.mapToTypeName(tModel.getStateMachine())));
                // see if we need to declare friend region classes
                List<String> friends = Util.newList();
                tContext.put("friends", friends);
                if (tModel.getTransitionTimeEvents(m).size() > 0) {
                    // collect and store regions to declare as friend classes
                    for (Region r : Util.filter(tModel.getAllClassLevelElements(), Region.class)) {
                        friends.add(tMapper.mapToQualifiedName(r));
                    }
                }
                //- collect other designated friend class(es)
                for (String friend : m.getFriends()) {
                    friends.add(friend);
                }

                writeCodePart(w, QP_H_CLASS_ACTIVE);
                tContext.remove("friends");
            } else if (machineOrRegion instanceof Region) {
                // generate Hsm class declaration, data member portion
                isHsm = true;
                tContext.put("isQHsm", isHsm);  // flag affects Active param type
                tContext.put("region", machineOrRegion);
                tContext.put("typeName", tMapper.mapToQualifiedName(machineOrRegion));
                writeCodePart(w, QP_H_CLASS_HSM);
                tContext.remove("region");
            } else {  // report error and continue
                if (machineOrRegion == null) {
                    Util.error("Error! Class header generation: encountered NULL machine/region!");
                } else {
                    Util.error("Error! Class header generation: encountered "
                            + machineOrRegion.getQualifiedName()
                            + " : " + machineOrRegion.getClass().getName() + "!");
                }
                continue;
            }

            // generate the common state-handler portion
            writeCodePart(w, QP_H_CLASS_STATES);
            // finally, generate the private section
            writeCodePart(w, QP_H_CLASS_PRIVATE);

            tContext.remove("isQHsm");
            tContext.remove("typeObj");
            tContext.remove("typeName");
        }

        // complete the header file
        if (Autocoder.isNamespaceEnabled()) {
            invokeVmacro(w, VMACRO_FOOTER_NAMESPACES, "composeHeader", EMPTY_PARAMS);
        }
        invokeVmacro(w, VMACRO_FOOTER_HEADERDEF, "composeHeader", EMPTY_PARAMS);
    }

}
