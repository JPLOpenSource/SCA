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

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

//    /* If overridden, and no Signal object defined, Promela code will also not
//     * have event transition code!
//     * 
//     * @see gov.nasa.jpl.statechart.template.TargetLanguageMapper#mapSignalEventToLiteral(gov.nasa.jpl.statechart.uml.SignalEvent)
//     */
//    @Override
//    public String mapSignalEventToLiteral (SignalEvent event) {
//        String name = null;
//        if (event.getSignal() == null) {  // use SignalEvent name
//            name = event.getName();
//        } else {
//            name = event.getSignal().getName();
//        }
//        return sanitize(name);
//    }

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
    
    // This function scans the body of an Opaque Behavior for "ALH.sendSignal()" and 
    // returns the signal event in that function call. 
    // Current assumptions:
    // Assumes only one sendSignal in the body. This needs to be beefed-up to parse for
    // more than one, but this might have ramifications down the line in the template
    // code.
    // This also assumes that the signal body is in double quotes, which seem 
    // reasonable.
    // Currently this also does not parse the second parameter for the source of the
    // target. The event is simply placed on the queue of all state-machines that
    // subscribed to this signal event.
    //
    public String mapOpaqueBodyToEvents(String body) {
    	String eventString = "";
    	Scanner sc = new Scanner(body);
    	sc.useDelimiter("\n");
    	while (sc.hasNext()) {
    		String token = sc.next();
    		// Look for the sending of signals, ignoring the comment line
    		if ( (!token.trim().startsWith("//")) && (token.contains("ALH.sendSignal")) ) {
    			   eventString = token.split("\"")[1];
    			   break;
    		}
    	}
    	return eventString;
    }

    // Remove all the <CR> out of the guard spec so that it can be stamped out as comments to the Promela.
    public String CleanGuardSpec(String guardSpec) {
    	return guardSpec.replaceAll("[\n\r]", "");
    }
    
   
    // Translates the guardSpec into Promela
    // The state machine name is passed in as a parameter from the velocity template because
    // the guard spec does not contain the state name if the spec is local.
    // First split the guardSpec into functions separated by logical operators
    // Convert each function to Promela, then concatenate them back
    // together with the logical operators.
    public String TranslateGuardSpecToPromela(String stateMachineName, String guardSpec) {    	
    	// Split the string into individual guard tokens by && or ||
		String[] guardToken = guardSpec.split("&&|\\|\\|");
		int guardIndex = 0;
		
		// Translate the first guard token into Promela
		String retString = TranslateALHToPromela(stateMachineName, guardToken[guardIndex]);
		
		// Pick out the Operators && or ||
    	Pattern operatorSymbols = Pattern.compile("(&&|\\|\\|)");
    	Matcher operator = operatorSymbols.matcher(guardSpec);
    	
    	// Insert the operators between the translated guard tokens
    	while(operator.find()) {
    		guardIndex = guardIndex + 1;
    		retString = retString + " " + operator.group(1) + " " + TranslateALHToPromela(stateMachineName, guardToken[guardIndex]);
    	}
		return retString;
    }
    
    

    // Translate the ALH.inState call to Promela
    public String TranslateALHToPromela(String stateMachineName, String guardSpec) {
    	// Remove all spaces in the spec
    	String guardString = guardSpec.replaceAll("\\s+", "");
    	// Look for ALH.inState(<smName>, "yadayada::<leafState>")
    	// Group 1 is the smName but it's optional so it may be null
    	// Group 4 is the leaf state name
    	//Pattern pattern = Pattern.compile("ALH\\.inState\\((\\w+,)?\"(\\w+::)+(::)?(\\w+)\"");
    	Pattern pattern = Pattern.compile("ALH\\.inState\\((\\w+)?,?\"(\\w+::)+(::)?(\\w+)\"");
    	Matcher matcher = pattern.matcher(guardString);
    	if (matcher.find()) {
			String smName;
			if (matcher.group(1) == null) {
			    smName = stateMachineName;
			} else {
				smName = matcher.group(1);
			}
			String leafState = matcher.group(4);
			return smName + "@" + leafState;
    	} else {
    		// If the spec was not an "ALH.inState" then return the input guard spec;
    		// this will give a Promela compile error and we will have to fill it in manually.
    	    //
    	    // [SWC 2014.02.27] Return null, to code out simple guard pattern
    		return null;
    	}
    }
     
}
