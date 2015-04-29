/**
 * Created Dec 8, 2009.
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
package gov.nasa.jpl.statechart.autocode.promela;

import gov.nasa.jpl.statechart.template.FunctionCall;
import gov.nasa.jpl.statechart.template.TargetLanguageMapper;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.TimeEvent;

/**
 * Mapper of element labels from UML representation to Promela code.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class UMLToPromelaMapper extends TargetLanguageMapper {

    /**
     * Default constructor
     */
    public UMLToPromelaMapper () {
    }

    /*
     * (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapTimeEventToName(gov.nasa.jpl.statechart.uml.TimeEvent, java.lang.String[])
     */
    @Override
    public String mapTimeEventToLiteral (TimeEvent event, String...prefix) {
        return joinWithPrefix(event.getName(), prefix) + "TimerEv";
    }

    /*
     * (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapTimeEventToTimer(gov.nasa.jpl.statechart.uml.TimeEvent, java.lang.String[])
     */
    @Override
    public String mapTimeEventToTimer (TimeEvent event, String...prefix) {
        return joinWithPrefix(event.getName().toLowerCase(), prefix) + "_timeout";
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#umlToTargetLang(gov.nasa.jpl.statechart.uml.NamedElement)
     */
    @Override
    public String umlToTargetLang (NamedElement ne) {
        return sanitize((ne == null) ? "NULL" : ne.getName());
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#getIndent()
     */
    @Override
    protected String getIndent () {
        return "    ";
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#getSingleLineComment()
     */
    @Override
    protected String getSingleLineComment () {
        return "//";
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#getElseGuardKeyword()
     */
    @Override
    public String getElseGuardKeyword () {
        return "";  // no need for else-if in Promela
    }

    public String mapArgsToDeclarations (FunctionCall func) {
        StringBuilder sb = new StringBuilder();
        String[] args = func.argList();
        for (int i=0; i < args.length; ++i) {
            if (args[i].equals("e")) {  // 'e' is reserved for event arg
                sb.append("e");
            } else {
                sb.append("arg").append(i+1);
            }
            if (i < args.length-1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String mapArgsToInvocation (FunctionCall func) {
        StringBuilder sb = new StringBuilder();
        String[] args = func.argList();
        for (int i=0; i < args.length; ++i) {
            if (args[i].equals("e")) {  // 'e' is reserved for event arg
                sb.append("self.tEvt");
            } else {  // insert identifier or literal directly
                sb.append(args[i]);
            }
            if (i < args.length-1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

}
