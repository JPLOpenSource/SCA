package gov.nasa.jpl.statechart.template;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.validator.FatalModelException;
import gov.nasa.jpl.statechart.uml.Behavior;
import gov.nasa.jpl.statechart.uml.Class;
import gov.nasa.jpl.statechart.uml.Event;
import gov.nasa.jpl.statechart.uml.Model;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Package;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.Signal;
import gov.nasa.jpl.statechart.uml.SignalEvent;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.TimeEvent;
import gov.nasa.jpl.statechart.uml.UMLNamedElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This is an abstract class that is meant to be used as a basis for any
 * language-specific target.  This class contains functions that are useful
 * for manipulating the expanded names returned by the Autocoder mid-layer.
 *
 * There is a separate function for mapping a UML Element into each
 * distinct language construct. The constructs are C-centric and are 
 * broken down into three categories: types, declarations and varnames.
 *
 * Enumerated Types
 *
 *   enum $enumType { $enumDecl } $enumName;
 *
 * Structures
 *
 *   struct $structType {} $structName;
 *
 * Functions
 *
 *   void $functionName(void);
 *
 */
public abstract class TargetLanguageMapper {
    public static final String UML_SEPARATOR = Util.PACKAGE_SEP;

    /**
     * Returns string with non-legal name characters stripped away.
     * @param str  input string to sanitize for non-legal characters
     * @return  sanitized string
     */
    public String sanitize (String str) {
        if (str == null) {
            return null;
        } else {
            return str.replaceAll("\\W", "");
        }
    }

    /**
     * Many of the UML elements may be unnamed which produced many qualified
     * and expanded names with multiple concatenations of the UML separator
     * "::".  This function replaces these repeats and returns a canonical
     * string representation of the UML path, e.g.
     *
     *  Input:  ::A::::B::C::::D::
     *  Output: A::B::C::D
     */
    public String umlToCanonical (String qname) {
        // First remove any leading or trailing separators
        while (qname.startsWith(UML_SEPARATOR))
            qname = qname.substring(UML_SEPARATOR.length());

        while (qname.endsWith(UML_SEPARATOR))
            qname = qname.substring(0, qname.length() - UML_SEPARATOR.length());

        // Replace any double-separators sequences with a single sequence
        // until the string does not changes
        String dbl = UML_SEPARATOR + UML_SEPARATOR;
        String str;

        do {
            str = qname;
            qname = qname.replace(dbl, UML_SEPARATOR);

        } while (!str.equals(qname));

        return qname;
    }

    public int getLongestString (Collection<String> c) {
        return Util.getLongestString(c);
    }

    public Collection<String> getNamesInTargetLang (Collection<NamedElement> collection) {
        List<String> newList = new ArrayList<String>();
        for (NamedElement ne : collection) {
            if (ne instanceof SignalEvent) {
            	newList.add(mapSignalEventToName((SignalEvent) ne));
            } else if (ne instanceof TimeEvent) {
                newList.add(mapTimeEventToName((TimeEvent) ne));
            } else if (ne instanceof Signal) {
            	newList.add(mapSignalToName((Signal) ne));
            } else {
                newList.add(umlToTargetLang(ne));
            }
        }
        return newList;
    }

    public String getPadding (String str, int maxlen) {
        if (str.length() >= maxlen)
            return "";

        return Util.strrep(' ', maxlen - str.length());
    }


    private Map<NamedElement,String> elementNameMap = Util.newMap();
    private Map<String,NamedElement> nameElementMap = Util.newMap();
    private Map<String,Integer> dupNameCount = Util.newMap();

    /**
     * Takes a NamedElement and map it to the shortest unique filename, taking
     * into account other NamedElement names.  The file name is derived from
     * {@link #mapToTypeName(NamedElement)}, which typically capitalizes the
     * first letter.  If another element of the same name exists, this method
     * will attempt to prepend the previous model-path segment, and then
     * increment a counter.
     * <p>
     * For example, A::foo maps to the file name "Foo".  If a b::foo exists,
     * and it is mapped after A::foo, then b::foo maps to "bFoo".  Finally,
     * A::b::Foo will become "bFoo2".
     * </p>
     * 
     * @param ne  UML NamedElement to map to a filename
     * @return  a filename string
     */
    public String mapToFileName (NamedElement ne, String... packages) {
        String filename = elementNameMap.get(ne);
        if (filename == null) {  // determine shortest unique name
            filename = mapToNamespacePathPrefix(packages) + sanitize(mapToTypeName(ne));
            if (nameElementMap.containsKey(filename)) {
                // uh oh, duplicate! look at qualified name and fetch previous path segment
                String oldName = mapToTypeName(ne);
                filename = ne.getQualifiedName();
                int pathSeg = -1;  // default to case of no "::"
                int idx = filename.lastIndexOf(UML_SEPARATOR);  // last "::"
                if (idx >= 0) {  // then find prior "::"
                    pathSeg = filename.lastIndexOf(UML_SEPARATOR, idx-1);
                } else {
                    idx = filename.length();
                }
                if (pathSeg == -1) {  // guard for no "::"
                    pathSeg = 0;
                }
                filename = filename.substring(pathSeg, idx) + UML_SEPARATOR + oldName;

                if (sanitize(filename).equals(oldName)) {  // append count
                    Integer i = dupNameCount.get(oldName);
                    if (i == null) {
                        i = 2;  // 1st use means there are already 2
                    } else {
                        ++i;
                    }
                    dupNameCount.put(oldName, i);
                    filename = oldName + i;
                }

                // finally, sanitize name
                filename = mapToNamespacePathPrefix(packages) + sanitize(filename);
            }
            // store mappings for later
            elementNameMap.put(ne, filename);
            nameElementMap.put(filename, ne);
        }
        return filename;
    }
    
    public String mapToSignalFileName (String sigFile, String...packages) {
    	return joinWithPrefix(sigFile, packages);
    }


    /**
     * Given an array of packages, joins them with, and appends "::" for proper
     * packaged reference of a type.
     * 
     * @param ns  variable length list of namespaces to chunk together
     * @return  A namespace reference string, ready to be used as a type prefix.
     */
    public String mapToNamespacePrefix (String... packages) {
        return Util.joinWithPrefixes(packages, "", null);
    }

    /**
     * Given an array of packages, joins with, and appends file separator,
     * for handling proper include paths.
     * 
     * @param ns  variable length list of namespaces to chunk together
     * @return  A directory path prefix corresponding to the namespaces
     */
    public String mapToNamespacePathPrefix (String... packages) {
        return Util.joinWithPrefixes(packages, "", "/");
    }

//// Methods for mapping Events to their names

    /**
     * Method to map a Signal to a corresponding name string
     */
    public String mapSignalToName (Signal sig) {
    	return mapToEnumDecl(sig);
    }

    /**
     * Method to map SignalEvent to its name
     */
    public String mapSignalEventToName (SignalEvent event) {
        return mapSignalEventToLiteral(event);
    }

    /**
     * Method to map SignalEvent to its equivalent string literal
     */
    public String mapSignalEventToLiteral (SignalEvent event) {
        String name = null;
        if (event.getSignal() == null) {
            name = event.getName();
            StringBuffer errMsgSB = new StringBuffer("Error: SignalEvent does not designate a Signal! ID '");
            errMsgSB.append(event.id()).append("'.");
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

    // To store names of anonymous timer event objects
    private final Map<TimeEvent, String> timerNames = Util.newMap();

    /**
     * Method to map TimeEvent to its Timer variable name, used by
     * mapTimeEventToName.
     */
    public String mapTimeEventToTimer (TimeEvent event, String...prefix) {
        String name = event.getName();
        if (name == null || name.length() == 0) {  // just in case this happens
            // give it an anonymous name
            if (!timerNames.containsKey(event)) {
                timerNames.put(event, "anonTimer" + timerNames.size());
            }
            name = timerNames.get(event);
        }
        // prepend with prefixes
        name = joinWithPrefix(name, prefix);

        // make first letter lowercase
        return name.substring(0,1).toLowerCase() + name.substring(1) + "Timer";
    }

    /**
     * Method to map TimeEvent to its proper name; uses mapTimeEventToTimer()
     * as a basis.
     */
    public String mapTimeEventToName (TimeEvent event, String...prefix) {
        return mapTimeEventToLiteral(event, prefix);
    }

    /**
     * Method to map TimeEvent to its equivalent string literal
     * 
     * @see #mapTimeEventToTimer(TimeEvent, String...)
     */
    public String mapTimeEventToLiteral (TimeEvent event, String...prefix) {
        // make first letter uppercase
        String name = mapTimeEventToTimer(event, prefix);
        return name.substring(0,1).toUpperCase() + name.substring(1) + "Ev";
    }

    /**
     * Method for to map TimeEvent to its time out value, if any.
     */
    public String mapTimeEventToTimeout (TimeEvent event) {
        String timeout = null;
        if (event.getWhen() == null) {
            timeout = "";
            throw new FatalModelException("Error: TimeEvent does not specify an OpaqueExpression! ID '"
                    + event.id() + "'");
        } else {
            timeout = event.getWhenExpr();
        }

        // The timeout will either be a number corresponding to an "at(x)"
        // expressions, or a quoted string which is stripped of its quotes
        // and passed through.
        if (timeout.startsWith("\"") && timeout.endsWith("\"")) {
            return timeout.substring(1, timeout.length() - 1);
        }

        return timeout;
    }

    /**
     * Method to map either a {@link SignalEvent} or a {@link TimeEvent} to
     * its name, esp. as given in the model, prepending prefix if any.
     * 
     * @param ev  UML Signal- or Time- {@link Event} object to map to name.
     * @param prefix  optional array of Strings to prepend as prefix.
     * @return  String identifier of the event name supplied in the model 
     */
    public String mapEventToName (Event ev, String... prefix) {
        if (ev instanceof TimeEvent) {
            return mapTimeEventToName((TimeEvent) ev, prefix);
        } else {  // otherwise, assume SignalEvent
            return mapSignalEventToName((SignalEvent) ev);
        }
    }

    /**
     * Method to map either a {@link SignalEvent} or a {@link TimeEvent} to
     * its equivalent string literal, prepending prefix if any.
     * 
     * @param ev  UML Signal- or Time- {@link Event} object to map to name.
     * @param prefix  optional array of Strings to prepend as prefix.
     * @return  String literal of the composed string name of the event.
     */
    public String mapEventToLiteral (Event ev, String...prefix) {
        if (ev == null) {
            return "null event";
        } else {
            if (ev instanceof TimeEvent) {
                return mapTimeEventToLiteral((TimeEvent) ev, prefix);
            } else {  // assume SignalEvent
                return mapSignalEventToLiteral((SignalEvent) ev);
            }
        }
    }

//// Methods for mapping NamedElements to a few language construct names

    /**
     * Returns the simple name of the element, forcing uppercase first letter.
     */
	public String mapToSimpleTypeName(NamedElement elem) {
        String name = sanitize(elem.getName());
        return name.substring(0,1).toUpperCase() + name.substring(1);
	}

    public String mapToTypeName (NamedElement elem) {
		return mapToSimpleTypeName(elem);
    }

    public String mapToFunctionName (NamedElement elem) {
        return umlToTargetLang(elem);
    }

    public String mapToVarName (NamedElement elem, String...prefix) {
        if (elem instanceof TimeEvent) {
            return mapTimeEventToTimer((TimeEvent) elem, prefix);
        } else {
            return joinWithPrefix(umlToTargetLang(elem), prefix);
        }
    }

    public String mapToQualifiedName (NamedElement elem) {
        return cleanQualifiedName(elem).replace(UML_SEPARATOR, "");
    }

    public String mapToEnumDecl (NamedElement elem) {
        // To make this a constant name, just remove all the UML
        // separators from the declaration name and convert to upper case
        return cleanQualifiedName(elem).replace(UML_SEPARATOR, "_").toUpperCase();
    }

    /**
     * Maps a UML {@link NamedElement} to a Signal enum, based on whether or
     * not the NamedElement is a {@link StateMachine}; otherwise, mapping is
     * done via mapToEnumDecl().
     * 
     * @param ne  {@link UMLNamedElement} to map to a signal name.
     * @return  an all uppercase Signal enum identifier.
     */
    public String mapToSignalEnum (NamedElement ne) {
        String sigPart = null;
        if (ne instanceof StateMachine) {
            sigPart = mapToTypeName(ne).toUpperCase();
        } else {
            sigPart = mapToEnumDecl(ne);
        }
        return "_SIG_" + sigPart + "_COMPLETE_";
    }

    /**
     * Given an UML NamedElement, returns an appropriate string representation
     * for the target language, used by the other umlToX functions.
     * 
     * @param ne  UMLNamedElement whose name to map.
     * @return  String representation for the target language.
     */
    public abstract String umlToTargetLang (NamedElement ne);

    /**
     * Returns the default indentation spacing for the target language.
     * @return  indentation, usually 4 spaces, but could be 3 or 2.
     */
    protected abstract String getIndent ();

    public String indentation (int lvl) {
        StringBuffer isb = new StringBuffer();
        for (int i=0; i < lvl; ++i) {
            isb.append(getIndent());
        }
        return isb.toString();
    }

    private int indentLevel = 0;
    public String indentation () {
        return indentation(indentLevel);
    }

    public int incIndent () {
        return ++indentLevel;
    }

    public int decIndent () {
        return --indentLevel;
    }

    public String implCallComment () {
        if (Autocoder.isImplCallLive()) {
            return "";
        } else {
            return getSingleLineComment();
        }
    }

    /**
     * Returns element's qualified name stripped of segments that are not
     * essential statemachine element names, e.g., Data::T1::Region::T11 becomes T1::T11,
     * unless the region is orthogonal, in which case it's left in.
     * 
     * @param ne  UML NamedElement whose qualified name to clean
     * @return  cleaned-up qualified name
     */
    protected String cleanQualifiedName (NamedElement ne) {
        List<String> nameParts = new ArrayList<String>();

        do {
            if (ne instanceof Model) {  // don't add name part
                // should we show region names if orthogonal???
            } else if (ne instanceof Region) {  // show region name if orthogonal
                State parentState = ((Region) ne).getState();
                if (parentState != null && parentState.isOrthogonal()) {
                    nameParts.add(sanitize(ne.getName()));
                }
            } else if ((ne instanceof Class && !(ne instanceof Behavior))
                    || ne instanceof Package) {  // package/class is too far up
                break;
            } else {
                // sanitize (chug out) characters to produce legal names
                nameParts.add(sanitize(ne.getName()));
            }
            ne = ne.getParent();
        } while (ne != null);

        Collections.reverse(nameParts);  // flip list items around
        return Util.join(nameParts, UML_SEPARATOR);
    }

    protected abstract String getSingleLineComment ();

    /**
     * Returns the initial guard keyword, e.g., 'if'.
     * 
     * @return  language keyword used for a transition guard.
     */
    public String getGuardKeyword () {
        return "if";
    }

    /**
     * Returns the secondary guard keyword, e.g., 'elif'.
     * Subclass should override this method if the language provides such a
     * keyword.
     * 
     * @return  language keyword used for guards after the initial one.
     */
    public String getElseGuardKeyword () {
        return null;
    }

    /**
     * Returns the default 'else' keyword.
     * @return  language keyword used for the otherwise condition.
     */
    public String getElseKeyword () {
        return "else";
    }

    private boolean alreadyGuarded = false;

    /**
     * Returns the currently applicable guard keyword; default to the initial
     * guard keyword.
     * 
     * @return  language keyword for guards.
     */
    public String guardKeyword () {
        if (alreadyGuarded) {
            return getElseGuardKeyword();
        } else {
            return getGuardKeyword();
        }
    }

    /**
     * Sets the flag indicating whether we've already used an initial guard
     * keyword, which affects subsequent calls to {@link #guardKeyword()}.
     * For example, when this flag is set, {@link #guardKeyword()} will return
     * "elif" instead of "if" in Python.
     * 
     * @param f  boolean flag indicating if we've already used initial guard.
     */
    public void setAlreadyGuarded (boolean f) {
        alreadyGuarded = f;
    }

    protected String joinWithPrefix (String name, String...prefix) {
        String preStr = Util.join(prefix, UML_SEPARATOR);
        return umlToCanonical(preStr + UML_SEPARATOR + name).replace(UML_SEPARATOR, "_");
    }

    protected String[] addToArray (String toAdd, String...array) {
    	if (toAdd != null && toAdd.length() > 0) {
    		// only add to array if non null and non empty
    		List<String> list = Util.newList(Arrays.asList(array));
    		list.add(toAdd);
    		return list.toArray(new String[0]);
    	} else {
    		return array;
    	}
    }

}
