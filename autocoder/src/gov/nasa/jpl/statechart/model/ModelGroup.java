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

import gov.nasa.jpl.statechart.model.diagram.DiagramData;
import gov.nasa.jpl.statechart.uml.Model;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.UMLModel;

import java.util.Collection;

/**
 * Interface to a group of UML Models, which provides encapsulation for a
 * collection of model input files in one autocoder session.  It serves as a
 * place to store cross-model information, in particular, ID-to-element mappings
 * and signal objects.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public interface ModelGroup {

    /**
     * Returns the UML Model corresponding to the supplied filename.
     * @param filename  name of input model file
     * @return  UML Model corresponding to <code>filename</code>.
     */
    public Model getModel (String filename);

    /**
     * Checks whether the UML Model as represented by the filename exists
     * already in this group.
     * @param filename  name of input model file
     * @return  <code>true</code> if UML Model exists in this group, <code>false</code> otherwise.
     */
    public boolean existsModel (String filename);

    /**
     * Returns the collection of UML Models in the group.
     * Since this class is used primarily internally, we return the concrete
     * UMLModel rather than the Model interface.
     * @return  collection of UML models in the group.
     */
    public Collection<UMLModel> models ();

    /**
     * Returns the collection of all UML state machines in this model group.
     * @return  collection of UML StateMachines within the models in this group.
     */
    public Collection<StateMachine> getStateMachines ();

    /**
     * Return whether any Model in this group manifests a fatal exception.
     * @return  <code>true</code> if any Model has a fatal exception, <code>false</code> if none.
     */
    public boolean hasFatalException ();

    /**
     * Returns the Diagram Element data for the supplied UML Model.
     * @param model  UML Model for which to get Diagram data 
     * @return  Diagram Data object containing GUI info
     */
    public DiagramData getDiagramData (Model model);

}
