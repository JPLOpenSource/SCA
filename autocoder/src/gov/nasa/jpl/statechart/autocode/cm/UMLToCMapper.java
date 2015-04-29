/**
 * This class is responsible for implementing the TargetLanguageMapper interface
 * for the C language.
 */
package gov.nasa.jpl.statechart.autocode.cm;

import gov.nasa.jpl.statechart.input.validator.FatalModelException;
import gov.nasa.jpl.statechart.template.TargetLanguageMapper;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Namespace;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.SignalEvent;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.TimeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class UMLToCMapper extends TargetLanguageMapper {
    public static final String QP_HSM_TOP = "QHsm_top";

    /**
     * Convenience function for appending a new Namespace to an existing
     * path 
     */
    public static String append (String qname, NamedElement ns) {
        if (qname.length() > 0)
            qname += TargetLanguageMapper.UML_SEPARATOR;
    
        return qname + ns.getName();
    }

    public static String append (Map.Entry<String, ? extends Namespace> entry,
            NamedElement ns) {
        return append(entry.getKey(), ns);
    }

    /**
     * Convenience function for removing a Namespace from an existing path
     */
    public static String remove (String qname, NamedElement ns) {
        String name = ns.getName();
    
        if (name.length() == 0)
            return qname;
    
        // Make sure the name matched the end
        if (!qname.endsWith(name))
            throw new RuntimeException("Bad suffix. qname = " + qname
                    + ", ns = " + name);
    
        qname = qname.substring(0, qname.length() - name.length());
    
        if (qname.endsWith(TargetLanguageMapper.UML_SEPARATOR))
            qname = qname.substring(0, qname.length() - TargetLanguageMapper.UML_SEPARATOR.length());
    
        return qname;
    }

    /**
     * Returns a new qualified name stripped of the innermost segment (right-most).
     * @param qname  qualified name to strip segment
     * @return a new qualified name of the "parent" scope
     */
    public static String remove (String qname) {
        // Find the last UML_SEPARATOR
        int last = qname.lastIndexOf(TargetLanguageMapper.UML_SEPARATOR);
        if (last == -1) {  // nothing to strip
            return "";
        }
        // return new name with last segment stripped
        return qname.substring(0, last);
    }

    public Collection<String> getNamesInTargetLang (Map<String,? extends Namespace> map) {
        List<String> newList = new ArrayList<String>();
        for (Entry<String, ? extends Namespace> entry : map.entrySet()) {
            Namespace ns = entry.getValue();
            if (ns instanceof TimeEvent) {
                newList.add(mapTimeEventToName((TimeEvent) ns));
            } else {
                newList.add(umlToTargetLang(entry.getKey(), ns));
            }
        }
        return newList;
    }

    /**
     * If the namespace is a UML State, then it must have a name.  If it
     * does not it is an error. If it is a Region, then a name is optional.
     * If a name does not exist, the the string "Region" is appended to the
     * expanded name.
     * <p>
     * Given that MagicDraw adds hierarchical levels to the qualified name,
     * starting from the tree root, we'll strip out those intermediate name
     * levels when converting to C names.
     * </p>
     */
    protected String umlToC (String str, NamedElement ns) {
        if (ns == null) {  // most likely a top-level element, return TOP
            str = QP_HSM_TOP;
        } else {
            // First check to see if it a UML State
            if (ns instanceof State) {
                if (ns.getName() == null || ns.getName().length() == 0) {
                    throw new RuntimeException("ERROR: States must have names");
                }
            } else if (ns instanceof Region) {
                // If it is a region, determine if we have to append an anonymous name
                if (ns.getName() == null || ns.getName().length() == 0) {
                    str += "Region";
                }
            } else if (ns instanceof StateMachine) {
                str = QP_HSM_TOP;
            }
        }
        if (str != QP_HSM_TOP) {
            str = cleanQualifiedName(ns);
        }
        return str;
    }

    /**
     * Given the qualified name of a State and its UML State, return an
     * appropriate function declaration for implementing the state chart
     * logic.
     * 
     * There are several possibilities here.  If the namespace is
     * a UML State, then it must have a name.  If it does not it 
     * is an error.
     *
     * If it is a Region, then a name is optional.  If a name does not
     * exist, the the string "Region" is appended to the expanded name,
     */
    public String umlToFunctionDecl (String qname, Namespace ns) {
        // Clean up the name
        String decl = umlToC(umlToCanonical(qname), ns);

        // To make this a legal C function name, just remove all the UML
        // separators from the declaration name
        return decl.replace(UML_SEPARATOR, "");
    }

    /**
     * Given the qualified name of a State and its UML State, return an
     * appropriate constant string that references the state.
     * 
     * Map a qualified name string to a C constant declaration. Make
     * sure that the separator character can be the start of a valid
     * variable because there may be empty nams at the front
     */
    public String umlToConstDecl (String qname, Namespace ns) {
        // Clean up the name
        String decl = umlToC(umlToCanonical(qname), ns);

        // To make this a constant name, just remove all the UML
        // separators from the declaration name and convert to
        // upper case
        return decl.replace(UML_SEPARATOR, "_").toUpperCase();
    }

    /**
     * Given the qualified name of a State and its UML State, return an
     * appropriate literal string when referring to this state as a
     * variable.
     * 
     * Map a qualified name string to a C variable declaration.
     * This is just the lower-case name of the namespace.
     */
    public String umlToVariableDecl (String qname, Namespace ns) {
        return ns.getName().toLowerCase();
    }

    /**
     * Given the qualified name of an UML NamedElement and the element itself,
     * returns an appropriate string for the target language, used by the other
     * umlToX functions.
     * 
     * @param qname qualified name of State
     * @param ne  NamedElement reference to UML State
     * @return new, target-language-specific form of qualified name
     * 
     * TODO get rid of Qualified Name as additional parameter!
     */
    public String umlToTargetLang (String qname, NamedElement ne) {
        return umlToC(qname, ne);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#umlToTargetLang(gov.nasa.jpl.statechart.uml.NamedElement)
     */
    @Override
    public String umlToTargetLang (NamedElement ne) {
        return umlToC(ne.getQualifiedName(), ne);
    }

    /**
     * Method for to map TimeEvent to its time out value, if any
     */
    public String mapTimeEventToTimeout (String srcQname, TimeEvent event) {
        String timeout = null;
        if (event.getWhen() == null) {
            timeout = "";
            throw new FatalModelException("Error: TimeEvent does not specify an OpaqueExpression! ID '"
                    + event.id() + "', used in transition from '" + srcQname + "'");
        } else {
            timeout = event.getWhenExpr();
        }

        // The timeout will either be a number corresponding to an "at(x)"
        // expressions, or a quoted string which is stripped of its quotes
        // and passed through.
        if (timeout.startsWith("\"") && timeout.endsWith("\""))
            return timeout.substring(1, timeout.length() - 1);

        return timeout;
    }

    /**
     * Method to map SignalEvent to its name
     */
    public String mapSignalEventToName (String srcQname, SignalEvent event) {
        String name = null;
        if (event.getSignal() == null) {
            name = event.getName();
            StringBuffer errMsgSB = new StringBuffer("Error: SignalEvent does not designate a Signal! ID '");
            errMsgSB.append(event.id()).append("', used in transition from '" + srcQname + "'.");
            if (name != null && name.length() > 0) {
                errMsgSB.append(" Perhaps need to define a Signal named '").append(name).append("'?");
            } else {
                name = null;
            }
            throw new FatalModelException(errMsgSB.toString());
        } else {
            name = event.getSignal().getName();
        }
        return sanitize(name);
    }

    // TODO Do we need most of these?!
    public String mapToEnumType (Map.Entry<String, ? extends NamedElement> elem) {
        return elem.getValue().getName();
    }

    public String mapToEnumDecl (Map.Entry<String, ? extends NamedElement> elem) {
        return super.mapToEnumDecl(elem.getValue());
    }

    public String mapToEnumName (Map.Entry<String, ? extends NamedElement> elem) {
        return elem.getValue().getName();
    }

    public String mapToStructType (Map.Entry<String, ? extends NamedElement> elem) {
        // Clean up the name
        String decl = umlToC(umlToCanonical(elem.getKey()), elem.getValue());

        // To make this a legal C function name, just remove all the UML
        // separators from the declaration name
        return decl.replace(UML_SEPARATOR, "");
    }

    public String mapToStructDecl (Map.Entry<String, ? extends NamedElement> elem) {
        return elem.getValue().getName();
    }

    public String mapToStructName (Map.Entry<String, ? extends NamedElement> elem) {
        return sanitize(elem.getValue().getName());
    }

    public String mapToFunctionType (Map.Entry<String, ? extends NamedElement> elem) {
        return elem.getValue().getName();
    }

    public String mapToFunctionDecl (Map.Entry<String, ? extends NamedElement> elem) {
        return elem.getValue().getName();
    }

    public String mapToFunctionName (Map.Entry<String, ? extends NamedElement> elem) {
        // Clean up the name
        String decl = umlToC(umlToCanonical(elem.getKey()), elem.getValue());

        // To make this a function name, replace all the UML separators with '_'
        decl = decl.replace(UML_SEPARATOR, "_");
        if (decl.equals(mapToEnumDecl(elem))) {  // lowercase last part of name
            int idx = decl.lastIndexOf("_") + 1 + 1;
            decl = decl.substring(0,idx) + decl.substring(idx).toLowerCase();
        }
        return decl;
    }

    public String mapToVarType (Map.Entry<String, ? extends NamedElement> elem) {
        return elem.getValue().getName();
    }

    public String mapToVarDecl (Map.Entry<String, ? extends NamedElement> elem) {
        return elem.getValue().getName();
    }

    public String mapToVarName (Map.Entry<String, ? extends NamedElement> elem) {
        return sanitize(elem.getValue().getName()).toLowerCase();
    }

    /*
     * (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapToVarName(gov.nasa.jpl.statechart.uml.NamedElement, java.lang.String[])
     */
    @Override
    public String mapToVarName (NamedElement elem, String...prefix) {
        return joinWithPrefix(sanitize(elem.getName()), prefix).toLowerCase();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#getIndent()
     */
    @Override
    protected String getIndent () {
        return "   ";
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
        return "else if";
    }

}
