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

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.template.TargetLanguageMapper;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.TimeEvent;

import java.util.Map;

/**
 * Mapper of state object labels from UML representation to Python code,
 * depending on what part of code is being generated.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class UMLToPythonMapper extends TargetLanguageMapper {
    public static final String QP_HSM_TOP = "top";

    private final Map<TimeEvent, String> timerNames = Util.newMap();

    /**
     * Default constructor
     */
    public UMLToPythonMapper () {
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapTimeEventToTimer(gov.nasa.jpl.statechart.uml.TimeEvent)
     */
    @Override
    public String mapTimeEventToTimer (TimeEvent event) {
        return event.getName().toLowerCase() + "_timeout";
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapTimeEventToName(gov.nasa.jpl.statechart.uml.TimeEvent)
     */
    @Override
    public String mapTimeEventToName (TimeEvent event) {
        String name = event.getName();

        if (name == null || name.length() == 0) {
            if (!timerNames.containsKey(event))
                timerNames.put(event, "AnonTimer" + timerNames.size());

            name = timerNames.get(event);
        }

        return name + "TimerEv";
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapToVarName(gov.nasa.jpl.statechart.uml.NamedElement)
     */
    @Override
    public String mapToVarName (NamedElement elem) {
        return super.mapToVarName(elem).toLowerCase();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#umlToTargetLang(gov.nasa.jpl.statechart.uml.NamedElement)
     */
    @Override
    public String umlToTargetLang (NamedElement ne) {
        String str = null;
        if (ne == null) {
            str = QP_HSM_TOP;
        } else {
            // First check to see if it a UML State
            if (ne instanceof State) {
                if (ne.getName() == null || ne.getName().length() == 0) {
                    throw new RuntimeException("ERROR: States must have names");
                } else {
                    str = ne.getName();
                }
            } else if (ne instanceof Region) {
                // If it is a region, determine if we have to append an anonymous name
                if (ne.getName() == null || ne.getName().length() == 0) {
                    str += "Region";
                } else {
                    str = ne.getName();
                }
            } else if (ne instanceof StateMachine) {
                str = QP_HSM_TOP;
            } else {
                str = ne.getName();
            }
        }
        return sanitize(str);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#getIndent()
     */
    @Override
    protected String getIndent () {
        return "    ";
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#implCallComment()
     */
    @Override
    public String implCallComment () {
        return "";  // impl calls do NOT need to be commented out in Python.
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#getSingleLineComment()
     */
    @Override
    protected String getSingleLineComment () {
        return "#";
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#getElseGuardKeyword()
     */
    @Override
    public String getElseGuardKeyword () {
        return "elif";
    }

    public String mapToRegionClass (NamedElement elem) {
        return cleanQualifiedName(elem).replace(UML_SEPARATOR, "");
    }

}
