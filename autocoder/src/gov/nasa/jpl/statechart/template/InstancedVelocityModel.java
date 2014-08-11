/**
 * Created Jul 10, 2013.
 * <p>
 * Copyright 2009-2013, by the California Institute of Technology. ALL RIGHTS
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
package gov.nasa.jpl.statechart.template;

import gov.nasa.jpl.statechart.Pair;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.model.visitor.AbstractVisitor.OrthoRegion;
import gov.nasa.jpl.statechart.uml.Classifier;
import gov.nasa.jpl.statechart.uml.Element;
import gov.nasa.jpl.statechart.uml.FinalState;
import gov.nasa.jpl.statechart.uml.InstanceSpecification;
import gov.nasa.jpl.statechart.uml.LiteralInteger;
import gov.nasa.jpl.statechart.uml.Namespace;
import gov.nasa.jpl.statechart.uml.Property;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.Slot;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.Stereotype;
import gov.nasa.jpl.statechart.uml.UMLElement;
import gov.nasa.jpl.statechart.uml.ValueSpecification;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This Query class provides query functions to get instance information from
 * the supplied UML Model.
 * <br/>
 * <p>
 * Copyright 2009-2013, by the California Institute of Technology. ALL RIGHTS
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
 *
 * @author Michael Pellegrin <Michael.Pellegrin@jpl.nasa.gov>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 */
public class InstancedVelocityModel extends GlobalVelocityModel {

    /**
     * Default constructor.
     * 
     * @param myModelGrp  The ModelGroup containing UML Models to query against
     */
    public InstancedVelocityModel (ModelGroup myModelGrp) {
        super(myModelGrp);
    }


    /**
     * Given a name, finds and returns the Stereotype object, if any.
     *
     * @param name  Name of the Stereotype to find
     * @return  {@link Stereotype} object matching the supplied stereotype name.
     */
    public Stereotype findStereotype (String name) {
        Stereotype stereotype = null;

        if (getStateMachine() != null) {
            stereotype = UMLElement.stereotypeByName.get(name);

            if (stereotype == null) {
                Util.error("ERROR! Expected stereotype '" + name + "' NOT found in the model! Autocode may contain errors.");
            }
        }

        return stereotype;
    }


    /**
     * Checks and returns whether the property is stereotyped by the given
     * Stereotype name.
     *
     * @param prop            UML {@link Property} to check stereotype
     * @param stereotypeName  the applied {@link Stereotype} to determine applicability
     * @return  boolean <code>true</code> if property is has been applied with stereotype
     */
    public boolean isVarStereotypedBy (Property prop, String stereotypeName) {
        boolean applies = false;

        for (Stereotype stereotype : prop.getAppliedStereotypes()) {
            if (stereotype.getName().equals(stereotypeName)) {
                applies = true;
                break;
            }
        }

        return applies;
    }

    /**
     * Checks and returns whether the parent level of the supplied state is
     * the top, the given StateMachine.
     * 
     * @param state    UML {@link State} to check its parent level
     * @param machine  UML {@link StateMachine} to check against
     * @return
     */
    public boolean isParentLevelTheMachine (State state, StateMachine machine) {
        return getParentState(state).equals(machine);
    }

    private Property cachedInstanceVar = null;

    /**
     * Returns the designated instance variable from the given state machine
     * by querying for the attribute stereotyped with the given stereotype name.
     * 
     * @param machine         UML {@link StateMachine} to query for instance variable
     * @param stereotypeName  the applied {@link Stereotype} to determine the instance variable
     * @return Instance variable {@link Property} of the State Machine {@link Classifier}.
     */
    public Property getStateMachineInstanceVar (StateMachine machine, String stereotypeName) {
        if (cachedInstanceVar == null) {
            OUTER: for (Property attr : machine.getAttributesMap().values()) {
                for (Stereotype stereotype : attr.getAppliedStereotypes()) {
                    if (stereotype.getName().equals(stereotypeName)) {
                        cachedInstanceVar = attr;
                        break OUTER;
                    }
                }
            }
        }

        if (cachedInstanceVar == null) {  // uh oh! not found; report error!
            Util.error("ERROR! Could NOT determine instance variable from model! Is property stereotyped with <<"
                    + stereotypeName + ">>?");
        }

        return cachedInstanceVar;
    }


    // Cached set of State Machine instance specifications
    private Set<InstanceSpecification> cachedSMInstances = null;

    private Collection<InstanceSpecification> getStateMachineInstances (StateMachine machine) {
        if (cachedSMInstances == null) {  // lazy init, populate set
            cachedSMInstances = Util.newSet();

            for (String id : machine.getInstanceIds()) {
                Element instSpecPotential = ((UMLElement) machine).xmi2uml(id);
                if (instSpecPotential instanceof InstanceSpecification) {
                    InstanceSpecification instSpec = (InstanceSpecification) instSpecPotential;
                    cachedSMInstances.add(instSpec);
                }
            }
        }

        return cachedSMInstances;
    }


    // Cached map of StateMachine Instance names to instances
    private Map<String,InstanceSpecification> cachedSMInstanceByName = null;

    /**
     * This provides a list of instantiated StateMachine names.
     * Names are stored as a HashSet since instantiation entries are expected to be unique 
     * eg. utilization:
     *    for (String sName : getStateMachineInstanceNames()) {
     *        System.out.println("StateMachine Inst name: " + sName);
     *    }
     */
    public Collection<String> getStateMachineInstanceNames (StateMachine machine) {
        if (cachedSMInstanceByName == null) {  // lazy init
            cachedSMInstanceByName = Util.newSortedMap();

            boolean sortById = false;
            Pattern regex = Pattern.compile("(\\w+)(\\d+)");

            // first acqurie instances and their names
            for (InstanceSpecification instSpec : getStateMachineInstances(machine)) {
                cachedSMInstanceByName.put(instSpec.getName(), instSpec);

                if (!sortById && regex.matcher(instSpec.getName()).matches()) {
                    sortById = true;  // second pass needed to sort by ID stem
                }
            }

            if (sortById) {  // sort instances by name<ID>
                Map<String,InstanceSpecification> linkedMap = new LinkedHashMap<String,InstanceSpecification>();

                // process each name
                for (String instName : Util.newSet(cachedSMInstanceByName.keySet())) {
                    Matcher regexMatcher = regex.matcher(instName);
                    if (regexMatcher.matches()) {
                        String newName = String.format("%s%08d",
                                regexMatcher.group(1),
                                Integer.parseInt(regexMatcher.group(2)));
                        // replace entry in Map with new name as key
                        cachedSMInstanceByName.put(newName, cachedSMInstanceByName.remove(instName));
                    }
                }

                // now transfer sorted map to insertion-ordered Map using original name
                for (String instName : cachedSMInstanceByName.keySet()) {
                    InstanceSpecification instSpec = cachedSMInstanceByName.get(instName);
                    linkedMap.put(instSpec.getName(), instSpec);
                }

                // replace the old map entirely with new map!
                cachedSMInstanceByName = linkedMap;
            }                
        }
        
        return cachedSMInstanceByName.keySet();
    }

    /**
     * Returns the {@link InstanceSpecification} of the given {@link StateMachine}
     * identified by the given instance name.
     * 
     * @param machine       {@link StateMachine} whose instance to query
     * @param instanceName  Name of the specified instance
     * @return  {@link InstanceSpecification} of the StateMachine instance queried
     */
    public InstanceSpecification getStateMachineInstanceSpec (StateMachine machine, String instanceName) {
        if (cachedSMInstanceByName == null) {
            getStateMachineInstanceNames(machine);
        }

        return cachedSMInstanceByName.get(instanceName);
    }

    // Cached map of instance name to ID
    private Map<String,Integer> cachedSMInstanceOrdinal = null;

    /**
     * Returns the ordinal value (the ID) of the given State Machine instance.
     * This ordinal is computed by sorting the instances alphabetically, then
     * assigning a ID sequentially.
     *
     * @param machine       {@link StateMachine} whose ID to query
     * @param instanceName  Name of the specified instance to query ID
     * @return  integer ID of the instance
     */
    public int getStateMachineInstanceId (StateMachine machine, String instanceName) {
        if (cachedSMInstanceOrdinal == null) {
            cachedSMInstanceOrdinal = Util.newMap();

            for (String instName : getStateMachineInstanceNames(machine)) {
                cachedSMInstanceOrdinal.put(instName, cachedSMInstanceOrdinal.size());
            }
        }

        int id = -1;
        if (cachedSMInstanceOrdinal.containsKey(instanceName)) {
            id = cachedSMInstanceOrdinal.get(instanceName);
        }
        return id;
    }

    /**
     * This provides a map of attributes {variable names-key} to value
     * {default or overridden variable values} and type for a specified
     * StateMachine instantiation.
     * 
     * @param machine       StateMachine object upon which to make query
     * @param instanceName  String name of the state-machine instanciation
     * @return {@link Map} of attribute key to value+type
     */
    public Map<String,Pair<String,String>> getInstanceValues (StateMachine machine, String instanceName) {
        Map<String,Pair<String,String>> specificAttributes = Util.newMap(); 

        for (Property p : machine.getAttributesMap().values()) {
            specificAttributes.put(p.getName(), new Pair<String,String>(p.getValueString(), p.type()));
        }
 
        // override any applicable field(s)
        InstanceSpecification instSpec = getStateMachineInstanceSpec(machine, instanceName);
        if (instSpec != null && cachedInstanceVar != null) {
            for (Slot slot : instSpec.slot()) {  // get Slot value
                String propName = slot.definingFeature().getName();

                // assume one value per slot
                ValueSpecification valueSpec = null;
                if (slot.value().size() > 0) {
                    valueSpec = slot.value().iterator().next();
                }

                // get type from Property originally
                specificAttributes.put(propName, new Pair<String,String>(valueSpec.stringValue(), slot.definingFeature().type()));
            }

            // set instance ID value
            specificAttributes.put(cachedInstanceVar.getName(),
                    new Pair<String,String>(Integer.toString(getStateMachineInstanceId(machine, instanceName)),
                            LiteralInteger.TYPE));
        }

        return specificAttributes;     
    }


    /**
     * Given a state, returns either the Orthogonal Region enclosing it, or the
     * top-level State Machine.
     *
     * @param state  {@link State} whose enclosing region or machine to return.
     * @return  {@link Region} or {@link StateMachine} (common supertype {@link Namespace})
     *          that immediately encloses the {@link State}
     */
    public Namespace getEnclosingOrthogonalRegionOrMachine (State state) {
        Region region = getEnclosingOrthogonalRegion(state);

        if (region == null) {
            return state.getContainingStatemachine();
        } else {
            return region;
        }
    }

    /**
     * Gets descendant states <i>without</i> including orthogonal regions,
     * self included if a State.  FinalStates ARE included.
     * Boolean <code>descend</code> determines if submachines are checked.
     * 
     * @param ns       Namespace within which to gather States.
     * @param descend  Flag to indicate whether to include SubMachines.
     * @return Collection of UML {@link State}s that are descendants of the
     *         given state, but without going into orthogonal regions.
     */
    public Collection<State> getStatesAboveOrtho (Namespace ns, boolean descend) {
        // Filter out the FinalStates
        return super.getStates(ns, descend, OrthoRegion.STOP_AT_ORTHO);
    }

    public Collection<State> getNonFinalStates (Namespace ns, boolean descend) {
        // Filter out the FinalStates
        return Util.filterOut(super.getStates(ns, descend, OrthoRegion.INCLUDE_BELOW_ORTHO), FinalState.class);
    }

}
