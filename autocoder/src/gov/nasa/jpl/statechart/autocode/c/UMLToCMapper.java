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

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.validator.FatalModelException;
import gov.nasa.jpl.statechart.template.FunctionCall;
import gov.nasa.jpl.statechart.template.TargetLanguageMapper;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.Signal;
import gov.nasa.jpl.statechart.uml.SignalEvent;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.TimeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Mapper of state object labels from UML representation to C code,
 * depending on what part of code is being generated.
 * <p>
 * Copyright &copy; 2009-2011 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class UMLToCMapper extends TargetLanguageMapper {
    public static final String QP_HSM_TOP = "QHsm_top";
    public static final String SEP = "_";

    protected String autocodeDesignation = null;
    protected String basicTypeBool = null;
    protected String basicTypeInt8 = null;
    protected String basicTypeUInt8 = null;
    protected String basicTypeInt16 = null;
    protected String basicTypeUInt16 = null;
    protected String basicTypeInt32 = null;
    protected String basicTypeUInt32 = null;
    protected String basicTypeDouble = null;
    protected String basicTypeDouble64 = null;

    /**
     * Default constructor
     */
    public UMLToCMapper () {
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

    /**
     * Overrides {@link TargetLanguageMapper} implementation by mapping a
     * collection of {@link State}s to their enum declaration names.
     * 
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#getNamesInTargetLang(java.util.Collection)
     */
    @Override
    public Collection<String> getNamesInTargetLang(Collection<NamedElement> collection) {
        if (!collection.isEmpty()
                && collection.iterator().next() instanceof State) {
            List<String> newList = new ArrayList<String>();
            for (NamedElement ne : collection) {
                newList.add(mapToEnumDecl((State) ne));
            }
            return newList;
        } else {
            return super.getNamesInTargetLang(collection);
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

    public void setAutocodeDesignation (String designation) {
    	if (designation.endsWith(SEP)) {
    		// get rid of any terminating '_'
    		autocodeDesignation = designation.split(SEP)[0];
    	} else {
    		autocodeDesignation = designation;
    	}
    }
    public String getAutocodeDesignation () {
    	if (autocodeDesignation == null) {
    		return "";
    	} else {
    		return autocodeDesignation;
    	}
    }

    public void setBasicTypes (String bool, String int8, String uint8, String int16, String uint16, String int32, String uint32, String fdouble, String fdouble64) {
        basicTypeBool = bool;
        basicTypeInt8 = int8;
        basicTypeUInt8 = uint8;
        basicTypeInt16 = int16;
        basicTypeUInt16 = uint16;
        basicTypeInt32 = int32;
        basicTypeUInt32 = uint32;
        basicTypeDouble = fdouble;
        basicTypeDouble64 = fdouble64;
    }

    /**
     * Overrides {@link TargetLanguageMapper#mapToFileName(NamedElement, String...)}
     * so that the filename derives from both the module package and the
     * {@link NamedElement} name.
     * <p>
     * File name should be &lt;module&gt;/&lt;module&gt;_&lt;NamedElement-name&gt;.
     * </p>
	 * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapToFileName(gov.nasa.jpl.statechart.uml.NamedElement, java.lang.String[])
	 */
	@Override
	public String mapToFileName (NamedElement ne, String... packages) {
		// Precede filename with module package name
		return mapToNamespacePathPrefix(packages)
				+ joinWithPrefix(super.mapToFileName(ne), packages);
	}

	/**
	 * Overrides {@link TargetLanguageMapper#mapToNamespacePrefix(String...)} so
	 * that packges are separated by '_'.
	 * 
	 * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapToNamespacePrefix(java.lang.String[])
	 */
	@Override
	public String mapToNamespacePrefix (String... packages) {
		return Util.joinWithPrefixes(packages, "", SEP);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapToSimpleTypeName(gov.nasa.jpl.statechart.uml.NamedElement)
	 */
	@Override
	public String mapToSimpleTypeName (NamedElement elem) {
		return super.mapToSimpleTypeName(elem);
	}

	/**
	 * Overrides {@link TargetLanguageMapper#mapToTypeName(NamedElement)} so
	 * that type name is prepended with the module name, and no letters are
	 * capitalized.  Another function should be called for that, e.g.,
	 * mapToStructType.
	 * 
	 * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapToTypeName(gov.nasa.jpl.statechart.uml.NamedElement)
	 */
	@Override
	public String mapToTypeName (NamedElement elem) {
		return joinWithPrefix(sanitize(elem.getName()),
				elem.getPackageNames());
	}

	public String mapToImplTypeName (NamedElement elem) {
		return mapToTypeName(elem)+"Impl";
	}

	/**
	 * Overrides {@link TargetLanguageMapper#mapToFunctionName(NamedElement)} so
	 * that the function name is prepended with the module name, and no letters
	 * are capitalized.
	 * 
	 * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapToFunctionName(gov.nasa.jpl.statechart.uml.NamedElement)
	 */
	@Override
	public String mapToFunctionName (NamedElement elem) {
		return mapToPrefixedFunctionName(elem);
	}

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapToVarName(gov.nasa.jpl.statechart.uml.NamedElement, java.lang.String[])
     */
    @Override
    public String mapToVarName (NamedElement elem, String...prefix) {
        return joinWithPrefix(sanitize(elem.getName()), prefix);
    }

	public String mapToVarName (String str, String...prefix) {
        String[] list = str.split(UML_SEPARATOR);
        if (list.length > 0 && list[list.length-1].length() > 0) {
            return joinWithPrefix(sanitize(list[list.length-1]), prefix);
        } else {
            return joinWithPrefix(sanitize(str), prefix);
        }
    }

    public String mapToPrefixedFunctionName (NamedElement elem, String...prefix) {
        String name = umlToTargetLang(elem);
        if (QP_HSM_TOP.equals(name)) {
            return name;
        } else {
            return joinWithPrefix(name, prefix);
        }
    }

    /* (non-Javadoc)
	 * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapToQualifiedName(gov.nasa.jpl.statechart.uml.NamedElement)
	 */
	@Override
	public String mapToQualifiedName(NamedElement elem) {
        return joinWithPrefix(cleanQualifiedName(elem),
                addToArray(autocodeDesignation, elem.getPackageNames()))
                    .replace(UML_SEPARATOR, SEP);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapToEnumDecl(gov.nasa.jpl.statechart.uml.NamedElement)
	 */
	@Override
	public String mapToEnumDecl(NamedElement elem) {
		return joinWithPrefix(super.mapToEnumDecl(elem),
				elem.getPackageNames()).toUpperCase();
	}

	/**
     * Given a {@link NamedElement} and an optional array of C module prefixes,
     * returns a macro definition name of the form <code>PREF1_..PREFn_ELEMNAME_H</code>.
     * @param elem  {@link NamedElement} whose name to map to a macro definition
     * @param prefix  an optional array of module prefix strings
     * @return  a macro definition string
     */
    public String mapToDefName (NamedElement elem, String... prefix) {
        return joinWithPrefix(elem.getName(),
        		addToArray(autocodeDesignation, prefix)).toUpperCase()
        		+ SEP + "H" + SEP;
    }


    /* (non-Javadoc)
	 * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapSignalToName(gov.nasa.jpl.statechart.uml.Signal)
	 */
	@Override
	public String mapSignalToName(Signal sig) {
		return mapSignalToEnum(sig);
	}

	/**
     * Method to map a signal's name to C-style <NAME>_SIG enum.
     */
    public String mapSignalToEnum (Signal sig) {
    	return mapSignalToEnum(joinWithPrefix(umlToTargetLang(sig),
    			addToArray(autocodeDesignation, sig.getPackageNames())));
    }

    /**
     * Method to map a signal name to C-style <NAME>_SIG enum.
     */
    public String mapSignalToEnum (String sigName) {
        // check for namespaces and get only the last segment, the name
        if (sigName.indexOf(UML_SEPARATOR) > -1) {
            int idx = sigName.lastIndexOf(UML_SEPARATOR)+UML_SEPARATOR.length();
            sigName = sigName.substring(idx);
        }
        return sanitize(sigName).toUpperCase()+SEP+"SIG";
    }

    /**
     * Method to map a {@link SignalEvent} to a C-style enum literal.
     * This method does NOT override {@link #mapSignalEventToLiteral(TimeEvent, String...)}
     * because that method is used for trace GUI output of event name.
     * 
     * <code>event</code> must NOT be NULL!
     * 
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapSignalEventToLiteral(gov.nasa.jpl.statechart.uml.SignalEvent)
     */
    public String mapSignalEventToName (SignalEvent event) {
        if (event == null) {
            throw new FatalModelException("Encountered NULL Signal Event! Check model for Transition(s) lacking a Trigger.");
        }
        return mapSignalToEnum(event.getSignal());
    }

    /**
     * Method to map a {@link TimeEvent} to a C-style enum literal.
     * This method does NOT override {@link #mapTimeEventToLiteral(TimeEvent, String...)}
     * because that method is used for trace GUI output of event name.
     * 
     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapTimeEventToLiteral(gov.nasa.jpl.statechart.uml.TimeEvent, java.lang.String[])
     */
    public String mapTimeEventToName (TimeEvent event, String... prefix) {
        String name = mapTimeEventToTimer(event, addToArray(autocodeDesignation, prefix));
        return mapSignalToEnum(name);
    }

    public String mapArgsToDeclarations (FunctionCall func) {
        StringBuilder sb = new StringBuilder();
        String[] args = func.argList();
        for (int i=0; i < args.length; ++i) {
            if (args[i].equals("e")) {  // 'e' is reserved for event arg
                sb.append("QEvent const* e");
            } else if (Util.isNumber(args[i]) || args[i].equals(args[i].toUpperCase())) {
                sb.append(basicTypeInt32).append(" arg").append(i+1);
            } else if (Util.isQuotedString(args[i])) {
                sb.append("const char* arg").append(i+1);
            } else {  // don't know, assume an unsigned integer
                sb.append(basicTypeUInt32).append(" arg").append(i+1);
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

            // TODO mapArgsToInvocation: need to insert basic boolean predicates directly
            if (args[i].equals("e") || Util.isLiteral(args[i]) || args[i].equals(args[i].toUpperCase())) {
                // 'e' is reserved for event arg; or a literal phrase, incl. all caps C enum!
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
