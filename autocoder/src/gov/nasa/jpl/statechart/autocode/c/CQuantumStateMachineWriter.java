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
package gov.nasa.jpl.statechart.autocode.c;

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
import java.util.Collection;
import java.util.Collections;

/**
 * <p>
 * This class writes out a state machine in the form of C code based on the
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
 * Copyright &copy; 2009-2011 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class CQuantumStateMachineWriter extends QuantumStateMachineWriter<UMLToCMapper> {
    protected static final String VMACRO_FOOTER_HEADERDEF = "footerHeaderDef";

    /** Filename for impl header template */
    protected static final String QP_H_IMPL = "qp.h-impl.vm";

    /** Filename for header template parts */
    protected static final String QP_H_INCLUDES       = "qp.h-includes.vm";
    protected static final String QP_H_STATE_ENUM     = "qp.h-stateenum.vm";
    protected static final String QP_H_CLASS_ACTIVE   = "qp.h-class-active.vm";
    protected static final String QP_H_CLASS_HSM      = "qp.h-class-hsm.vm";
    protected static final String QP_H_CLASS_STATES   = "qp.h-class-states.vm";

    /**
     * Main constructor, creates a writer given a UML Model Group.
     * 
     * @param modelGrp  the group of UML model(s) used for writing target output.
     */
    public CQuantumStateMachineWriter (ModelGroup modelGrp) {
        super(modelGrp, new UMLToCMapper());
        // set any "autocode" designation
        tMapper.setAutocodeDesignation(evalExpr("#autocodeDesignation", "Constructor", null));
        // set C basic types
        tMapper.setBasicTypes(evalExpr("#dataTypeBoolean", "Constructor", null),
                evalExpr("#dataTypeInt8", "Constructor", null),
                evalExpr("#dataTypeUInt8", "Constructor", null),
                evalExpr("#dataTypeInt16", "Constructor", null),
                evalExpr("#dataTypeUInt16", "Constructor", null),
                evalExpr("#dataTypeInt32", "Constructor", null),
                evalExpr("#dataTypeUInt32", "Constructor", null),
                evalExpr("#dataTypeDouble", "Constructor", null),
                evalExpr("#dataTypeDouble64", "Constructor", null));
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getTargetLabel()
     */
    @Override
    protected String getTargetLabel () {
        return IGenerator.Kind.C.label();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.QuantumStateMachineWriter#inlineMainTemplate()
     */
    @Override
    protected String inlineMainTemplate () {
        return null;  // C main method is a separate file
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

        String[] pkgNames = new String[0];
        if (Autocoder.isNamespaceEnabled()) {
            pkgNames = stateMachine.getPackageNames();
        }

        // set up some "global" context variables for later templates
        tContext.put("model", tModel);
        tContext.put("sm", stateMachine);
        String smName = evalExpr("#mapToTypeName($sm,\"\")", "writeStateMachine", tMapper.mapToTypeName(stateMachine));
        tContext.put("smPackages", pkgNames);
        tContext.put("smName", smName);
        tContext.put("typeName", smName);
        tContext.put("implName", evalExpr("#mapToTypeName($sm,\"impl\")", "writeStateMachine", tMapper.mapToImplTypeName(stateMachine)));
        // munge the statechart signal file name
        tContext.put("pkgs", new String[0]);
        tContext.put("sigFileName", CLocalStateChartSignalWriter.SIGNAL_FILENAME);
        tContext.put("sigFileName", evalExpr("#mapToFileName($sigFileName,$pkgs,'signals')", "writeStateMachine", CLocalStateChartSignalWriter.SIGNAL_FILENAME));
        tContext.remove("pkgs");
        // Define vars for the StateMachine namespaces, if applicable
        if (Autocoder.isNamespaceEnabled()) {
            String module = tMapper.mapToNamespacePrefix(pkgNames);
            tContext.put("modulePrefix", module);
            tContext.put("moduleDefPrefix", module.toUpperCase());
            tContext.put("modulePathPrefix", tMapper.mapToNamespacePathPrefix(pkgNames));
            tContext.put("smDefName", tMapper.mapToDefName(stateMachine, pkgNames));
        } else {  // let's see if all state machines yield only a single package
        	Collection<String[]> sigPkgs = tModel.getRequiredSignalPackagePaths(tModelGrp, tMapper);
        	if (sigPkgs.size() == 1) {  // yes, use this unique pkg as prefix!
        		String[] sigPkg = sigPkgs.iterator().next();
        		String module = Util.joinWithPrefixes(sigPkg, "" ,"_");
        		tContext.put("modulePrefix", module);
                tContext.put("moduleDefPrefix", module.toUpperCase());
                tContext.put("smDefName", tMapper.mapToDefName(stateMachine, sigPkg));
        	} else {
        		tContext.put("modulePrefix", "");
                tContext.put("moduleDefPrefix", "");
                tContext.put("smDefName", tMapper.mapToDefName(stateMachine));
        	}
            tContext.put("modulePathPrefix", "");
        }
        if (Autocoder.inst().getSignalNamespaceType() == SignalNamespaceType.NONE) {
            tContext.put("baseSigModule", "");
            tContext.put("baseSigModulePath", "");
        } else {
            // determine the file path of the base signal file
            String[] sigBasePkgs = Util.splitPreservingQuotes(Autocoder.inst().getSigBaseNamespace(), Util.PACKAGE_SEP);
            tContext.put("baseSigModule", tMapper.mapToNamespacePrefix(sigBasePkgs));
            tContext.put("baseSigModulePath", tMapper.mapToNamespacePathPrefix(sigBasePkgs));
        }

        // Craft the filenames
        String classname = evalExpr("#mapToFileName($sm,$smPackages,\"\")", "writeStateMachine", tMapper.mapToFileName(stateMachine, pkgNames));
        String declarationsFilename = classname + ".h";
        String definitionsFilename = classname + ".c";

        // Compose and write the header file
        Writer w = beginWriteCode(declarationsFilename);
        composeHeader(w);
        endWriteCode(declarationsFilename);

        // Compose and write the code file
        w = beginWriteCode(definitionsFilename);
        composeCode(w);
        //- complete the source code file
        endWriteCode(definitionsFilename);

        // Write the Impl class files, _always_, but do not override!
        tContext.put("sm", stateMachine);
        smName = evalExpr("#mapToTypeName($sm,\"\")", "writeStateMachine", tMapper.mapToTypeName(stateMachine));
        tContext.put("smName", smName);
        tContext.put("typeName", smName);
        classname = evalExpr("#mapToFileName($sm,$smPackages,\"impl\")", "writeStateMachine", tMapper.mapToFileName(stateMachine, pkgNames)+"_impl");
        String implDeclarationsFilename = classname + ".h";
        String implDefinitionsFilename = classname + ".c";
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
        tContext.remove("smPackages");
        tContext.remove("smName");
        tContext.remove("typeName");
        tContext.remove("implName");
        tContext.remove("modulePrefix");
        tContext.remove("moduleDefPrefix");
        tContext.remove("moduleFilePrefix");
        tContext.remove("smDefName");
        tContext.remove("baseSigModule");
    }

    /**
     * Builds the C header file, starting with the includes, then composing
     * State enums and class declarations for the State Machine and orthogonal
     * regions.
     * 
     * @param w  FileWriter to the C header file.
     */
    private void composeHeader (Writer w) {
        // Set up some "global" context variables for later templates
        StateMachine sm = tModel.getStateMachine();
        //- determine whether to enable BAIL_SIG/CompletionEvt/reinit/final
        tContext.put("SM_TERMINABLE", tModel.isMachineTerminable(sm));
        //- determine whether to enable BAIL_EVENT
        tContext.put("SM_TERMINATOR", tModel.isMachineTerminator(sm));
        //- determine whether explicit final state exists
        tContext.put("SM_HAS_FINAL_STATE", !tModel.getFinalStates(sm).isEmpty());

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
            tContext.put("typeObj", machineOrRegion);

            if (machineOrRegion instanceof StateMachine) {
                // generate Active class declaration, data member portion
                isHsm = false;
                tContext.put("isQHsm", isHsm);  // flag affects Active param type
                tContext.put("typeName", evalExpr("#mapToTypeName($sm,\"\")", "composeHeader", tMapper.mapToTypeName(tModel.getStateMachine())));
                writeCodePart(w, QP_H_CLASS_ACTIVE);
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

            tContext.remove("isQHsm");
            tContext.remove("typeObj");
            tContext.remove("typeName");
        }

        // complete the header file
        invokeVmacro(w, VMACRO_FOOTER_HEADERDEF, "composeHeader", EMPTY_PARAMS);
    }

}
