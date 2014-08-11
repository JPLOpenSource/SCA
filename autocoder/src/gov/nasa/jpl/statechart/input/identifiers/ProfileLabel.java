/**
 * Created Apr 7, 2011.
 * <p>
 * Copyright 2009-2011, by the California Institute of Technology. ALL RIGHTS
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
package gov.nasa.jpl.statechart.input.identifiers;

/**
 * Enumerations of labels for supported profiles.
 * Currently, that includes C ANSI,
 * an as-yet-unused StateChart Autocoder profile,
 * and any default stereotype set in the current model file.
 * <p>
 * Copyright &copy; 2009-2011 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public enum ProfileLabel {

    TAGATTR_BASE_PACKAGE { public String defaultLiteral () { return "base_Package"; } },
    TAGATTR_BASE_ELEMENT { public String defaultLiteral () { return "base_Element"; } },
    TAGATTR_BASE_STATEMACHINE { public String defaultLiteral () { return "base_StateMachine"; } },

    /** Added to support the C ANSI profile */
    PREFIX_C_ANSI_PROFILE { public String defaultLiteral () { return "c___ANSI_profile"; } },

    C_ANSI_CPP_NAMESPACE { public String defaultLiteral () { return "C__Namespace"; }
                           public ProfileLabel prefix () { return PREFIX_C_ANSI_PROFILE; } },

    /** Added to support the StateChart Autocoder profile */
    PREFIX_SCAPROFILE { public String defaultLiteral () { return "SCAProfile"; } },

    SCA_FRIENDS { public String defaultLiteral () { return "SCAFriends"; }
                  public ProfileLabel prefix () { return PREFIX_SCAPROFILE; } },
    TAGATTR_SCA_FRIENDSLIST { public String defaultLiteral () { return "friendsList"; } }
    ;

    public abstract String defaultLiteral ();

    /** Returns the Namespace prefix label */
    public ProfileLabel prefix () { return null; }

}
