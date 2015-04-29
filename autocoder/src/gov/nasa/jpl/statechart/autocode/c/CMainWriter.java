/**
 * Created Jan 14, 2010.
 * <p>
 * Copyright 2010, by the California Institute of Technology. ALL RIGHTS
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
import gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.template.GlobalVelocityModel;

import java.util.Collection;

/**
 * Generates a main file for C StateMachine to be executed standalone.
 * <p>
 * Copyright &copy; 2010-2011 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public class CMainWriter extends UMLStateChartTemplateWriter<UMLToCMapper> {

    protected static final String QP_MAIN       = "qp-main-qvanilla.vm";
    protected static final String QP_MAIN_LINUX = "qp-main-linux.vm";
    protected static final String MAIN_FILENAME = "main";

    protected static final String QP_MAKEFILE   = "qp-makefile.vm";
    protected static final String MAKE_FILENAME = "Makefile";

    /**
     * Main constructor, calls super with a Model Group and mapper.
     * 
     * @param modelGrp  Group of UML models
     */
    public CMainWriter (ModelGroup modelGrp) {
        super(modelGrp, new UMLToCMapper());
        // set any "autocode" designation
        tMapper.setAutocodeDesignation(evalExpr("#autocodeDesignation", "Constructor", null));
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getTargetLabel()
     */
    @Override
    protected String getTargetLabel () {
        return IGenerator.Kind.C.label();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getVelociMacroFile()
     */
    @Override
    protected String getVelociMacroFile () {
        return QuantumStateMachineWriter.QP_MACROS;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.IWriter#write()
     */
    public void write () {
        String mainFilename = tMapper.sanitize(MAIN_FILENAME) + ".c";
        String makeFilename = tMapper.sanitize(MAKE_FILENAME);

        // Create the proxy objects that expose the type information
        GlobalVelocityModel model = new GlobalVelocityModel(tModelGrp);
        tContext.put("model", model);
        tContext.put("sigFileName", CLocalStateChartSignalWriter.SIGNAL_FILENAME);
        tContext.put("emptyString", new String[0]);
        String[] modulePkgs = Util.splitPreservingQuotes(Autocoder.inst().getSigBaseNamespace(), Util.PACKAGE_SEP);
        // determine file path of the signal file depending on namespace type
        SignalNamespaceType nsType = Autocoder.inst().getSignalNamespaceType();
        if (nsType == SignalNamespaceType.GLOBAL && modulePkgs.length > 0 && modulePkgs[0].length() > 0) {
            // namespace path of the base signal file as module prefix
            //- do nothing
        } else {  // let's see if all state machines yield only a single package
        	Collection<String[]> sigPkgs = model.getRequiredSignalPackagePaths(tModelGrp, tMapper);
        	if (sigPkgs.size() > 1) {
        	    System.out.println("WARNING! Makefile/main does not well support setup with multiple local packages (" + sigPkgs.size() + "), so anticipate compilation issues!");
        	    // leave modulePkgs as the BASE pkg
        	} else if (sigPkgs.size() == 1) {  // yes, use this as prefix!
        	    modulePkgs = sigPkgs.iterator().next();
        	} else {  // no prefix
        	    modulePkgs = null;
        	}
        	tContext.put("pkgSet", sigPkgs);
        }
        String modulePrefix = tMapper.mapToNamespacePrefix(modulePkgs);
        String moduleDefPrefix = modulePrefix.toUpperCase();
        tContext.put("modulePkgs", modulePkgs);
        tContext.put("moduleType", Util.toCamelCase(Util.joinWithPrefixes(modulePkgs, "", "")));
        tContext.put("modulePrefix", modulePrefix);
        tContext.put("moduleDefPrefix", moduleDefPrefix);
        tContext.put("modulePathPrefix", tMapper.mapToNamespacePathPrefix(modulePkgs));

        // Write the main code to a file
        writeCode(mainFilename, QP_MAIN);

        // Check whether Makefile is to be preserved
        if (fileAsPath(makeFilename).exists() && Autocoder.configOptionTrue(Autocoder.AC_CONFIG_OPTIONS_KEY_CONFIG_MAKEFILE_PRESERVE)) {
            System.out.println(getTargetLabel() + " target " + makeFilename + " exists, will not override!");
        } else {
            // Write the Makefile
            writeCode(makeFilename, QP_MAKEFILE);
        }
    }

}
