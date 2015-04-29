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
import gov.nasa.jpl.statechart.template.FunctionCall;
import gov.nasa.jpl.statechart.template.TargetLanguageMapper;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.Signal;
import gov.nasa.jpl.statechart.uml.SignalEvent;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.TimeEvent;

/**
 * Mapper of state object labels from UML representation to C++ code,
 * depending on what part of code is being generated.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class UMLToCppMapper extends TargetLanguageMapper {
    public static final String QP_HSM_TOP = "QHsm::top";

    /**
     * Default constructor
     */
    public UMLToCppMapper () {
    }

    /**
     * Overrides {@link TargetLanguageMapper#sanitize(String)} with support for
     * namespace-qualified string, so that {@link TargetLanguageMapper#UML_SEPARATOR}s
     * (i.e., ::) are treated as namespace separators and NOT sanitized away.
     */
    @Override
    public String sanitize (String str) {
        if (str != null && str.indexOf(UML_SEPARATOR) >= 0) {
            // sanitize segments individually
            String[] segs = str.split(UML_SEPARATOR);
            for (int i=0; i < segs.length; ++i) {
                segs[i] = super.sanitize(segs[i]);
            }
            return Util.join(segs, UML_SEPARATOR);
        } else {
            return super.sanitize(str);
        }
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
                    str = sanitize(ne.getName());
                }
            } else if (ne instanceof Region) {
                // If it is a region, determine if we have to append an anonymous name
                if (ne.getName() == null || ne.getName().length() == 0) {
                    str = sanitize(str + "Region");
                } else {
                    str = sanitize(ne.getName());
                }
            } else if (ne instanceof StateMachine) {
                str = QP_HSM_TOP;
            } else {
                str = sanitize(ne.getName());
            }
        }
        return str;
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
        return "} else if";
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#getElseKeyword()
     */
    @Override
    public String getElseKeyword () {
        return "} else";
    }

    /**
     * Given an array of namespaces, joins with "::" for proper
     * C++ namespace reference of a type.
     * 
     * @param ns  variable length list of namespaces to chunk together
     * @return  A namespace reference string, ready to be used as a type prefix.
     */
    public String mapToCppNamespace (String... cppNs) {
        return (cppNs == null) ? null : Util.join(cppNs, UML_SEPARATOR);
    }

    /*
     * (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapToVarName(gov.nasa.jpl.statechart.uml.NamedElement, java.lang.String[])
     */
    @Override
    public String mapToVarName (NamedElement elem, String...prefix) {
        return "m_"+joinWithPrefix(sanitize(elem.getName()), prefix).toLowerCase();
    }

    public String mapToVarName (String str, String...prefix) {
        String[] list = str.split(UML_SEPARATOR);
        if (list.length > 0 && list[list.length-1].length() > 0) {
            return "m_"+joinWithPrefix(sanitize(list[list.length-1]), prefix).toLowerCase();
        } else {
            return "m_"+joinWithPrefix(sanitize(str), prefix).toLowerCase();
        }
    }

    /**
     * Given a {@link NamedElement} and an optional array of C++ namespaces,
     * returns a macro definition name of the form <code>_Ns1..NsnElemname_h</code>.
     * @param elem  {@link NamedElement} whose name to map to a macro definition
     * @param cppNs an optional array of namespace strings
     * @return  a macro definition string
     */
    public String mapToDefName (NamedElement elem, String... cppNs) {
        return Util.join(cppNs, "") + mapToTypeName(elem) + "_h";
    }


    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapSignalEventToName(gov.nasa.jpl.statechart.uml.SignalEvent)
     */
    @Override
    public String mapSignalEventToLiteral (SignalEvent event) {
        String prefix = "";
        if (Autocoder.inst().getSignalNamespaceType() == SignalNamespaceType.LOCAL
                && event.getSignal() != null) {
            prefix = mapToNamespacePrefix(event.getSignal().getPackageNames());
        }
        return prefix + super.mapSignalEventToLiteral(event);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapSignalToName(gov.nasa.jpl.statechart.uml.Signal)
     */
    @Override
    public String mapSignalToName(Signal sig) {
        String prefix = "";
        if (Autocoder.inst().getSignalNamespaceType() == SignalNamespaceType.LOCAL) {
            prefix = mapToNamespacePrefix(sig.getPackageNames());
        }
        return sanitize(umlToCanonical(prefix + umlToTargetLang(sig)));
    }

    /**
     * Method to map a signal's name to C++-style <CamelCaseName>Sig enum.
     */
    public String mapSignalToEnum (Signal sig) {
        return mapSignalToEnum(Util.join(sig.getPackageNames(), UML_SEPARATOR) + UML_SEPARATOR + umlToTargetLang(sig));
    }

    /**
     * Method to map a signal name to C++-style <CamelCaseName>Sig enum.
     */
    public String mapSignalToEnum (String sigName) {
        // check for namespaces and get only the last segment, the name
        if (sigName.indexOf(UML_SEPARATOR) > -1) {
            int idx = sigName.lastIndexOf(UML_SEPARATOR)+UML_SEPARATOR.length();
            sigName = sigName.substring(idx);
        }
        return sanitize(sigName);
    }

    /**
     * Method to map a {@link SignalEvent} to a C++-style NS::<CamelCaseName>Sig enum literal.
     * This method does NOT override {@link #mapSignalEventToLiteral(TimeEvent, String...)}
     * because that method is used for trace GUI output of event name.
     * 
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapSignalEventToLiteral(gov.nasa.jpl.statechart.uml.SignalEvent)
     */
    public String mapSignalEventToName (SignalEvent event) {
        return mapSignalToName(event.getSignal());
    }

    public String mapArgsToDeclarations (FunctionCall func) {
        StringBuilder sb = new StringBuilder();
        String[] args = func.argList();
        for (int i=0; i < args.length; ++i) {
            if (args[i].equals("e")) {  // 'e' is reserved for event arg
                if (Autocoder.isNamespaceEnabled()) {
                    sb.append(mapToNamespacePrefix(Autocoder.inst().getQfNamespace()));
                }
                sb.append("QEvent const* e");
            } else {
                sb.append("const char* arg").append(i+1);
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
            if (args[i].equals("e") || Util.isLiteral(args[i])) {
                // 'e' is reserved for event arg
                // insert directly
                sb.append(args[i]);
            } else {  // insert access to the impl-object by supplied identifier
                sb.append("me->impl->").append(args[i]);
            }
            if (i < args.length-1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

}
