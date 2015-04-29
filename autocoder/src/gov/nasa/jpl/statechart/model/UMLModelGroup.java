/**
 * Created Sep 28, 2009.
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
package gov.nasa.jpl.statechart.model;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.validator.FatalValidationException;
import gov.nasa.jpl.statechart.input.validator.IModelValidator;
import gov.nasa.jpl.statechart.input.validator.IModelValidator.Status;
import gov.nasa.jpl.statechart.input.validator.UMLValidator;
import gov.nasa.jpl.statechart.input.validator.XMIValidator;
import gov.nasa.jpl.statechart.model.diagram.DiagramData;
import gov.nasa.jpl.statechart.model.visitor.AnonymousNameInjector;
import gov.nasa.jpl.statechart.uml.Element;
import gov.nasa.jpl.statechart.uml.Model;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.UMLModel;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

/**
 * Class for encapsulating a group of UML models for an Autocoding session.
 * See {@link ModelGroup}.
 * <p>
 * This class also contains convenient helper/adapter methods over UMLModels
 * to access semantic information that is useful to the code generation backend.
 * </p>
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 * TODO Optimize code generation process by caching model info at validation time!
 */
public class UMLModelGroup implements ModelGroup {

    /**
     * Stores a mapping of UML elements to their parent model, so that each
     * element doesn't have to keep around that info nor have Model passed
     * around at UML element instantiation time. 
     */
    public static final Map<Element,UMLModel> element2Model = Util.newMap();

    /** An insertion-ordered map of filename to UML models */
    private Map<String,UMLModel> filename2Model = null;
    /** Nickname (filename) for each model **/
    private Map<Model,String> model2Filename = null;
    /** Hash map of UML Model to DOM Document */
    private Map<Model,Document> model2Doc = null;
    /** Hash map of UML Model to Diagram Data */
    private Map<Model,DiagramData> model2DiagramData = null;
    private boolean fatalModelException = false;

    private String[] skipValidationList = null;

    /** This flag prevents validation on subsequent calls to validate model */
    private boolean validated = false;
    /** This flag prevents name injection on subsequent calls to inject names */
    private boolean namesInjected = false;

    /**
     * Returns the UML Model which contains the supplied UML element.
     * @param element  the UML element whose containing Model to find
     * @return  the containing UML Model
     */
    public static UMLModel element2Model (Element element) {
        return element2Model.get(element);
    }

    public static void mapElement2Model (Element element, UMLModel model) {
        element2Model.put(element, model);
    }

    /**
     * Clears out the static element2Model mapping.
     */
    public static void clearStaticMaps () {
        element2Model.clear();
    }

    /**
     * Returns a candidate model for the named file, if null if no such file
     * or model exists.
     * 
     * @param filename  Name of file to search for corresponding model
     * @return  {@link UMLModel} instance corresponding to the filename
     */
    public static UMLModel findModelOfName (String filename) {
        // grab any model
        if (!element2Model.isEmpty()) {
            UMLModel model = element2Model.values().iterator().next();
            UMLModelGroup mgrp = (UMLModelGroup )model.getModelGroup();
            for (UMLModel m : mgrp.models()) {
                if (mgrp.getModelFilename(m).endsWith(filename)) {
                    return m;
                }
            }
        }
        return null;
    }

    /**
     * Default constructor.
     */
    public UMLModelGroup () {
        filename2Model = new LinkedHashMap<String,UMLModel>();
        model2Filename = new HashMap<Model,String>();
        model2Doc = Util.newMap();
        model2DiagramData = Util.newMap();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.ModelGroup#getModel(java.lang.String)
     */
    public Model getModel (String filename) {
        return filename2Model.get(filename);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.ModelGroup#containsModel(java.lang.String)
     */
    public boolean existsModel (String filename) {
        return filename2Model.containsKey(filename);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.ModelGroup#models()
     */
    public Collection<UMLModel> models () {
        return filename2Model.values();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.ModelGroup#getStateMachines()
     */
    public Collection<StateMachine> getStateMachines () {
        List<StateMachine> stateMachines = Util.newList();

        for (UMLModel model : models()) {
            stateMachines.addAll(model.getStateMachines());
        }

        return stateMachines;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.ModelGroup#hasException()
     */
    public boolean hasFatalException () {
        return fatalModelException && !Autocoder.isNoAbort();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.ModelGroup#getDiagramData(gov.nasa.jpl.statechart.uml.Model)
     */
    public DiagramData getDiagramData (Model model) {
        return model2DiagramData.get(model);
    }

    /**
     * Sets whether model exception has occurred.
     * @param b
     */
    public void setFatalException (boolean b) {
        fatalModelException = b;
    }

    /**
     * Creates a mapping of filename to UML Model instance,
     * @param filename  name of input model file
     * @param model  UML Model corresponding to <code>filename</code>.
     */
    public void mapFilenameToModel (String filename, UMLModel model) {
        filename2Model.put(filename, model);
        // find last segment of filename
        int lastIdx = filename.lastIndexOf(File.separator);
        if (lastIdx < 0) {  // then grab the whole name string
            lastIdx = 0;
        } else {  // don't include the separator itself
            ++lastIdx;
        }
        model2Filename.put(model, filename.substring(lastIdx));

        setMembersChanged();  // the only time we consider members changed
        if (model.hasException()) {
            setFatalException(true);
        }
    }
    /**
     * Returns the nickname, based on filename, of the given model.
     * @param model  UML {@link Model} whose nickname to obtain.
     * @return  Nickname String for this model, if any, or the model's own
     *      qualified name if none.
     */
    public String getModelFilename (Model model) {
        String name = model2Filename.get(model);
        if (name == null) {
            name = model.getQualifiedName();
        }
        return name;
    }

    /**
     * Creates a mapping of UML Model to DOM Document for convenient access.
     * @param model  UML Model to establish mapping for.
     * @param doc    DOM Document to map model to.
     */
    public void mapModelToDocument (Model model, Document doc) {
        model2Doc.put(model, doc);
    }
    /**
     * Retrieves the DOM Document associated with the supplied UML Model.
     * @param model
     * @return
     */
    public Document getDocument (Model model) {
        return model2Doc.get(model);
    }

    /**
     * 
     * Creates a mapping of UML Model to Diagram Element data.
     * @param model  UML Model to establish mapping for.
     * @param data   Diagram element data for that model.
     */
    public void mapModelToDiagramData (Model model, DiagramData data) {
        model2DiagramData.put(model, data);
    }

    /**
     * Validates all the models in this group. and throw fatal exception at end.
     */
    public void validateModels () {
        if (validated) return;

        StringBuilder fmeBuf = new StringBuilder();
        final String NL = System.getProperty("line.separator");

        IModelValidator xmiValidator = new XMIValidator();
        IModelValidator umlValidator = new UMLValidator();
        for (UMLModel model : models()) {
            if (Util.isWarningLevel()) {
                Util.warn("Validating Model '" + getModelFilename(model) + "'");
            }

            // TODO a hackish check for XMI validation...
            if (XMIValidator.hasXmiValidation) {
                if (xmiValidator.validate(getDocument(model)) == Status.OK) {
                    Util.warn("Other relevant XMI extensions validated OK.");
                } else {
                    Util.error("XMI extensions did NOT validate!");
                }
            }

            // Add validation methods to skip
            if (skipValidationList != null) {
                umlValidator.skipValidation(skipValidationList);
            }
            Status validStat = umlValidator.validate(model);
            // set model status and print message according to status
            switch (validStat) {
            case FATAL:
                fmeBuf.append("UML Model '")
                    .append(getModelFilename(model))
                    .append("' invalid for Autocoding, details above!")
                    .append(NL);
                setFatalException(true);
                break;
            case OK:
                if (Util.isWarningLevel()) {
                    Util.warn("UML Model '" + getModelFilename(model)
                            + "' validated OK.");
                }
                break;
            default:  // handle the ERROR case and any other future ones
                if (Util.isWarningLevel()) {
                    Util.warn("UML Model '" + getModelFilename(model)
                            + "' did not validate successfully, but nothing fatal.");
                }
                break;
            }
        }
        validated = true;

        // throw FatalModelException if occurred
        if (hasFatalException()) {
            throw new FatalValidationException(fmeBuf.toString());
        }
    }

    /**
     * Sets the list of validation functions to skip; default to none.
     *
     * @param names  Array of String names identifying validation methods to skip.
     */
    public void setUMLValidationSkipList (String... names) {
        skipValidationList = names;
    }

    /**
     * This function walks the entire UML structure of every model in the group,
     * adding names to elements that have no name.  This function breaks the
     * encapsulation slightly by directly altering the underlying DOM and
     * assuming knowledge of the implementation of the UML classes.
     */
    public void injectAnonymousNames () {
        if (namesInjected) return;

        for (StateMachine stateMachine : getStateMachines()) {
            BreadthFirstWalker.traverse(stateMachine, new AnonymousNameInjector());
        }

        namesInjected = true;
    }

    /**
     * Indicates that this model group has changed; unsets the flags that
     * prevent duplicate model validation and name injection.
     */
    private void setMembersChanged () {
        validated = false;
        namesInjected = false;
    }

}
