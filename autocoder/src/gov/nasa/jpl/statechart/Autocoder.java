/**
 * Created Aug 5, 2009.
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
package gov.nasa.jpl.statechart;

import gov.nasa.jpl.statechart.autocode.GeneratorKind;
import gov.nasa.jpl.statechart.autocode.IGenerator;
import gov.nasa.jpl.statechart.autocode.IGenerator.Kind;
import gov.nasa.jpl.statechart.autocode.IWriter;
import gov.nasa.jpl.statechart.input.IReader;
import gov.nasa.jpl.statechart.input.validator.FatalModelException;
import gov.nasa.jpl.statechart.input.validator.FatalValidationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The main class for invoking autocoders and querying system configurations.
 * <p>
 * Copyright &copy; 2009-2011 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class Autocoder {
    public static final String AC_VERSION = version();
    public static final String AC_COPYRIGHTS = "Copyright (C) 2009-%d California Institute of Technology";
    public static final String AC_CONFIG_OPTIONS_FILE = "options.properties";
    public static final String AC_CONFIG_OPTIONS_KEY_CONFIG_NAME = "config.name";
    public static final String AC_CONFIG_OPTIONS_KEY_CONFIG_UTILCLASS = "config.utilclass";
    public static final String AC_CONFIG_OPTIONS_KEY_CONFIG_QASUBS_SUPPRESS = "config.suppress.qasubs";
    public static final String AC_CONFIG_OPTIONS_KEY_CONFIG_GETIMPL_SUPPRESS = "config.suppress.getimpl";
    public static final String AC_CONFIG_OPTIONS_KEY_CONFIG_MAKEFILE_PRESERVE = "config.makefile.preserve";
    public static final String AC_CONFIG_OPTIONS_STEM_OPT   = ".option";
    public static final String AC_CONFIG_OPTIONS_STEM_CNT   = ".count";
    public static final String AC_CONFIG_OPTIONS_STEM_NAME  = ".name";
    public static final String AC_CONFIG_OPTIONS_STEM_DESC  = ".desc";
    public static final String AC_CONFIG_OPTIONS_STEM_INFO  = ".info";
    public static final String AC_CONFIG_OPTIONS_STEM_NOARG = ".noarg";
    public static final String AC_CONFIG_OPTIONS_STEM_REQD  = ".required";
    public static final String AC_CONFIG_OPTIONS_STEM_VALUE = ".value";

    public static final String OPT_USAGE = "-h";
    public static final String OPT_DEBUG = "-debug";
    public static final String OPT_VERBOSE = "-verbose";
    public static final String OPT_VERSION = "-version";
    public static final String OPT_CUSTOM_CONFIG = "-config";
    public static final String OPT_NO_IMPL_CALLS = "-noimplcalls";
    public static final String OPT_NO_TRACE = "-notrace";
    public static final String OPT_STATE_MACHINE = "-sm";
    public static final String OPT_CHECK_ONLY = "-check";
    public static final String OPT_NO_ABORT = "-noabort";
    public static final String OPT_SCL_DB_ID = "-scl_db_id";
    public static final String OPT_SCL_Rule_ID = "-scl_rule_id";


    /** Used for C/C++, flag defined to turn on stamping out the Main code. */
    public static final String FLAG_DEFINE_MAIN = "DEFINE_MAIN";
    public static final String FLAG_DEFINE_C_UNITTEST = "DEFINE_C_UNITTEST";
    public static final String FLAG_DEFINE_CPP_UNITTEST = "DEFINE_CPP_UNITTEST";
    // C++ options
    public static final String OPT_QF_NAMESPACE = "-cppqfns";
    public static final String OPT_QF_INCLUDE = "-cppqfinc";
    public static enum SignalNamespaceType {
        /** default */
        NONE,
        /** use single, global namespace; requires -cppqfns */
        GLOBAL,
        /** use model-specified, local namespaces; requires -cppqfns OR -cppsigbase */
        LOCAL
    };
    public static final String OPT_SIG_NAMESPACE = "-cppsig";
    public static final String OPT_SIG_BASE_NAMESPACE = "-cppsigbase";
    public static final String OPT_OUTPUT_DIR = "-dir";
    public static final String OPT_GUI_OUTPUT_DIR = "-guidir";

    // deprecated command-line options
    public static final String OPT_DUP_SM = "-dup-smnames";
    public static final String OPT_IGNORE_DUP_IDS = "-ignore-dup-ids";
    public static final String OPT_IMPL_CALLS = "-implcalls";
    public static final String OPT_QUALIFY_SIGNALS = "-qualify-signals";
    public static final String PROP_NO_TRACE = "jpl.autocode.noTrace";
    public static final String PROP_QUALIFY_SIGNALS = "jpl.autocode.qualifySignals";
    public static final String PROP_IGNORE_DUP_IDS = "jpl.autocode.ignoreDuplicateXmiIDs";
    public static final String PROP_TARGET_C = "jpl.autocode.c";

    public static int scl_db_id = 0;
    public static int scl_rule_id = 0;
    
    /** Map of option to kinds of Generator supported by this Autocoder */
    private static final Map<String,Kind> opt2GenKindMap = new LinkedHashMap<String,Kind>();
    static {
        opt2GenKindMap.put("-c", Kind.C);
        opt2GenKindMap.put("-cpp", Kind.Cpp);
        opt2GenKindMap.put("-java", Kind.Java);
        opt2GenKindMap.put("-promela", Kind.Promela);
        opt2GenKindMap.put("-python", Kind.Python);
        opt2GenKindMap.put("-scl", Kind.SCL);
        opt2GenKindMap.put("-cm", Kind.Cm);
        opt2GenKindMap.put("-signals", Kind.Signals);
        opt2GenKindMap.put("-csignals", Kind.CSignals);
        opt2GenKindMap.put("-cppsignals", Kind.CppSignals);
        opt2GenKindMap.put("-timeline", Kind.Timeline);
    }

    /** Singleton instance of the Autocoder application object */
    private static Autocoder singleton = null;
    public static Autocoder inst () {
        if (singleton == null) {
            singleton = new Autocoder();
        }
        return singleton;
    }

    private static String version () {
        return "2.7.1 (build 20150629-2335)";   // Updated Promela backend; add MD 17.0.5 support; fixed exactly-one xmi:Documentation limitation
//        return "2.7.0 beta (build 20140321-2333)";   // Merged SMAP-adaptation changes, plus a fix that propagates multiple back-ends
//        return "2.6.7 beta (build 20140306-0020)";   // Promela backend improvements to support Boeing model
//        return "2.6.7 beta (build 20140227-1900)";   // Promela backend updates to support Boeing model
//        return "2.6.6 beta (build 20131120-2000)"; // SCL-backend fix: fixed default entry transition to check that all children states are exited; updated UTs
//        return "2.6.6 beta (build 20131120-1245)"; // UML: UML: Added support to obtain OpaqueBehavior body and language specification.
//        return "2.6.6 beta (build 20131114-2330)"; // SCL-backend fix: guard-only transition exit-rule and state EntryAction activation
//        return "2.6.6 beta (build 20131031-1410)"; // SCL-backend fix: various guard and exit/entry rule issues for RevK5 and Junction
//        return "2.6.5 beta (build 20131023-1620)"; // SCL-backend improvement: support SclFunction stereotype to code out direct SCL code
//        return "2.6.5 beta (build 20131023-1520)"; // SCL-backend fix: only initial pseudo states have default_entry_rule
//        return "2.6.5 beta (build 20131023-1245)"; // UML warn if no outgoing branch; SCL-backend fixes: SclEvtMsgTopic stereotype, string DB, boolean value
//        return "2.6.5 beta (build 20131018-0200)"; // Completed near-term SCL-backend changes
//        return "2.6.5 beta (build 20131016-1645)"; // SCL-backend + UML: Support for TransitionPath Query
//        return "2.6.5 beta (build 20131015-1230)"; // SCL-backend bugfixes: DB tag, clear_event_signal 2nd arg, removal of Initialize of topic attr, spacing in SML state enum
//        return "2.6.5 beta (build 20131011-0330)"; // Refactored, and more bugfixes for Boeing SCL
//        return "2.6.5 beta (build 20131009-1700)"; // Bugfixes for Boeing SCL
//        return "2.6.5 beta (build 20131008-1600)"; // Stereotype hardwire fix, internal transitions, orthogonal regions
//        return "2.6.5 beta (build 20131003-1000)"; // Misc fixes - see ReleaseNotes sent to Boeing
//        return "2.6.5 beta (build 20130930-1500)"; // Misc fixes - see ReleaseNotes sent to Boeing
//        return "2.6.5 beta (build 20130930-1000)"; // Misc fixes - see ReleaseNotes sent to Boeing
//        return "2.6.5 beta (build 20130928-0330)"; // MD 17.0.2 Diagram extension XMI structural change; SCL bugfix
//        return "2.6.5 beta (build 20130921-0330)"; // SCL back-end instance var support improved; added UML Slot type
//        return "2.6.5 beta (build 20130920-1557)"; // SCL back-end various bugfixes 
//        return "2.6.4 beta (build 20130916-1519)"; // SCL back-end null-event transition and guard logic.
//        return "2.6.4 beta (build 20130909-1310)"; // Support K3 Boeing reference model - see ReleaseNotes sent to Boeing
//        return "2.6.3 beta (build 20130904-1310)"; // Misc fixes - see ReleaseNotes sent to Boeing
//        return "2.6.3 beta (build 20130811-0630)"; // Added support for UML Enumeration and improved stereotype recognition; SCL delivery to RevJ2
//        return "2.6.2 beta (build 20130724-0830)"; // Fixed guard names, added stereotyped action support, and fixed SCL back-end SM Init file
//        return "2.6.2 beta (build 20130722-1230)"; // Added UML Expression support; SCL delivery to RevH
//        return "2.6.1 beta (build 20130711-1230)"; // Major SCL implementation milestone
//        return "2.6.0 beta (build 20130523-1530)"; // Added SCL backend placeholder + -noabort option
//        return "2.5.9 beta (build 20140305-1515)"; // SMAP back-end improvement: allow suppressing Unreachable Computation and MostlySimilarFile
//        return "2.5.9 beta (build 20130829-1111)"; // SMAP back-end improvement: allow specifying Signal holes and Signal append to end, made declared States Enum a typedef
//        return "2.5.9 beta (build 20130814-1240)"; // SMAP back-end improvement: allow specifying EVR holes and EVR append to end
//        return "2.5.8 beta (build 20130621-1443)"; // Minor improvement: added support for project customization to inject text into intermediate output
//        return "2.5.8 beta (build 20130426-1040)"; // Minor fix: ortho 'final' function decl in header
//        return "2.5.8 beta (build 20130220-1234)"; // Minor fix: Region name in C prepended with package name and optional _ac_
//        return "2.5.8 beta (build 20130218-0000)"; // Minor fix: BAIL SIG in C should execute Completion Transition code
//        return "2.5.8 beta (build 20130213-1500)"; // Fix: Submachine dispatch case fixed to check handled flags only when no internal transition
//        return "2.5.8 beta (build 20130212-1630)"; // Major mod: Added ignore_dropped and handled flags to QHsm, enhanced submachine dispatch pattern
//        return "2.5.8 beta (build 20130208-1535)"; // Minor updates: Small update to eliminate repetitious #include of _prot.h.
//        return "2.5.8 beta (build 20121206-1234)"; // Minor updates: aligned SM IPC enums; removed _impl getter; C .h macro def added _
//        return "2.5.8 beta (build 20121114-1700)"; // Minor fix to C is_substate and re-init functions for orthogonal regions
//        return "2.5.7 beta (build 20121101-1700)"; // Minor cleanup to autocoded C impl stub; fixed incoming-trans XPath search scope; fixed merge junction autocode
//        return "2.5.7 beta (build 20121019-1145)"; // Minor fix to C re-init function to also reinit submachines
//        return "2.5.7 beta (build 20121010-1300)"; // Minor fix to completion event usage to get rid of pointer casts
//        return "2.5.7 beta (build 20121008-0900)"; // Ensured portability of multi-exit points to other backends
//        return "2.5.6 beta (build 20121005-1500)"; // Added support for multiple exit points to different targets
//        return "2.5.6 beta (build 20121002-1145)"; // Minor mission-specific module-ID fix: hex digits!
//        return "2.5.6 beta (build 20120928-2245)"; // Fixed duplicate states (case Composite04d)!
//        return "2.5.6 beta (build 20120928-1500)"; // Mission-specific module-ID fixes for local-namespaced unit test
//        return "2.5.6 beta (build 20120920-1500)"; // Various submachine and mission-specific fixes for unit test suites; added FinalState self-consumption of completion event
//        return "2.5.5 beta (build 20120911-1348)"; // Added SMAP option -nosmip to suppress SM IPC; cleaned up more autocode scrub warnings
//        return "2.5.5 beta (build 20120910-1623)"; // Fixed config problems for SMAP_LINUX
//        return "2.5.5 beta (build 20120906-1600)"; // Tightened/eliminated unnecessary code: destructor/QActive_subscribe call/final/BAIL_SIG/CompletionEvt...
//        return "2.5.5 beta (build 20120831-1700)"; // Added support mission-specific Util class, and checking of UNITTEST env-var
//        return "2.5.4 beta (build 20120124-1200)"; // Prepended _BAIL_/_COMPLETION_ w/ Q or SM name; more mission enhancements, incl. EVRs; added generated command line
//        return "2.5.3 beta (build 20120105-1200)"; // Beefed up Generator subclass search; mission-specific enhancements
//        return "2.5.2 beta (build 20111003-1230)"; // C backend update: prepend type/function/file with module name
//        return "2.5.1 beta (build 20110827-0100)"; // Fix: BehaviorVisitor more precise in determining relevant action
//        return "2.5.1 beta (build 20110823-1000)"; // C backend upgrade: all unit tests added, release ready
//        return "2.5 beta (build 20110811-1600)"; // C backend upgrade: ported from C++ templates
//        return "2.4.3 beta (build 20110730-1530)"; // Promela bugfix: internal trans + source/target pseudostates
//        return "2.4.2 beta (build 20110513-1400)"; // Added ConnectionPointReference entry point check
//        return "2.4.2 beta (build 20110509-1200)"; // Added overridable velocimacro project-velocimacros.vm
//        return "2.4.2 beta (build 20110505-1300)"; // Added option to support custom configuration directory.
//        return "2.4.2 beta (build 20110503-1300)"; // <class>Spy friend decl; took out pointer use for internal event types.
//        return "2.4.1 beta (build 20110426-1400)"; // Added support for new SCAProfile to specify friends list.
//        return "2.4 beta (build 20110426-0930)"; // Reorder C++ sections for coding std conformance
//        return "2.4 beta (build 20110425-0930)"; // Fixes to namespace support
//        return "2.4 beta (build 20110420-1400)"; // C++Namespace & namespaced file output support
//        return "2.3 beta (build 20101012-1530)"; // Patch: C++ new Makefile gen
//        return "2.3 beta (build 20100928-1700)"; // Sep minor release: Spin ortho support, bugfixes, usability
//        return "2.2 beta (build 20100628-1200)"; // June bugfix release: MD 16.8 support and submachine timer events!
//        return "2.2 beta (build 20100602-1700)"; // June update release: bugfixes, MD HREF, and "e" now must be explicit
//        return "2.1 beta (build 20100518-1530)"; // May update release: C++ complete feature set
//        return "2.1 beta (build 20100510-1740)"; // C++ unit-test completion
//        return "2.1 beta (build 20100408-0715)"; // April update release
//        return "2.1 beta (build 20100329-1645)"; // March release for CMU
//        return "2.1 beta (build 20100322-1745)"; // promoted: ortho of SubM + UserEvent propagation
//        return "2.1 beta (build 20100301-1630)"; // March release for Ares, no C++, added unsupported Java option
//        return "2.1 beta (build 20100224-1000)"; // C++ templates initial full-set
//        return "2.1 beta (build 20100222-1200)"; // Timeline addition
//        return "2.1 beta (build 20100202-0840)"; // FindBug fixes
//        return "2.1 beta (build 20100126-1230)"; // released, major Velocity refactoring
//        return "2.1 beta (build 20100113-1930)"; // promoted
//        return "2.1 beta (build 20091230-1320)"; // promoted
//        return "2.1 beta (build 20091215-1030)"; // promoted
//        return "2.1 beta (build 20091210-1300)"; // promoted
//        return "2.1 beta (build 20091202-1300)"; // promoted
//        return "2.1 beta (build 20091113-0841)"; // promoted
//        return "2.1 beta (build 20091028-0900)"; // promoted
//        return "2.1 beta (build 20091014-1030)"; // promoted
//        return "2.x beta (build 20091007-2026)"; // promote date, but forgot to update
//        return "2.x beta (build 20091005-1015)"; // promoted
//        return "2.x beta (build 20091002-1300)"; // promoted
//        return "2.x beta (build 20090930-1524)"; // promoted
//        return "2.x beta (build 20090929-1249)"; // not promoted
//        return "2.x beta (build 20090924-1645)";
    }

    public static boolean isVerbose () {
        return inst().optVerbose;
    }

    public static boolean isDebugOn () {
        return inst().optDebug && isVerbose();
    }

    public static boolean isCheckOnly () {
        return inst().optCheckOnly;
    }

    public static boolean isNoAbort () {
        return inst().optNoAbort;
    }

    /**
     * Returns whether the state machine should trace its execution.
     * 
     * @return <code>true</code> if execution trace should be ON, <code>false</code> otherwise.  
     */
    public static boolean isExecutionTraceOn () {
        if (inst().optNoTrace == null) {
            // read system property, default to "false" as trace is on by default
            inst().optNoTrace = Util.getSysBoolWithDefault(PROP_NO_TRACE, false);
        }
        return !inst().optNoTrace;
    }

    /**
     * Returns whether to ignore duplicates in XMI IDs across multiple state machines.
     * 
     * @return <code>true</code> if duplicate XMI IDs across multiple state
     * machines should be ignored, <code>false</code> otherwise.
     */
    public static boolean ignoreDuplicateXmiIds () {
        if (inst().optIgnoreDupXmiIDs == null) {
            // read system property, default to NOT ignoring duplicate XMI IDs
            inst().optIgnoreDupXmiIDs = Util.getSysBoolWithDefault(PROP_IGNORE_DUP_IDS, false);
        }
        return inst().optIgnoreDupXmiIDs;
    }

    /**
     * Returns whether to qualify the state transition signal names.
     *  
     * @return <code>true</code> if signal names should be fully-qualified, <code>false</code> otherwise.
     */
    public static boolean qualifySignals () {
        if (inst().optQualifySignals == null) {
            // read system property, default to NOT qualifying signals
            inst().optQualifySignals = Util.getSysBoolWithDefault(PROP_QUALIFY_SIGNALS, false);
        }
        return inst().optQualifySignals;
    }

    /**
     * Returns whether system property "DEFINE_MAIN" has been set, indicating
     * if main method should be generated.
     * 
     * @return <code>true</code> if main method should be generated, <code>false</code> otherwise.  
     */
    public static boolean ifDefineMain () {
        return System.getProperty(FLAG_DEFINE_MAIN) != null;
    }

    /**
     * Returns whether system property "DEFINE_*_UNITTEST" has been set, meaning
     * we're autocoding to a unit-test harness, which affects how the _reinit
     * function body will be autocoded.
     * 
     * @return <code>true</code> if coding for unit-test harness, <code>false</code> otherwise.  
     */
    public static boolean ifDefineUnitTest () {
        return System.getProperty(FLAG_DEFINE_C_UNITTEST) != null
                || System.getProperty(FLAG_DEFINE_CPP_UNITTEST) != null;
    }

    /**
     * Returns target kind for which the autocoder is currently generating code.
     * @return Kind of generator currently in process
     */
    public static Kind autocodingTarget () {
        if (Util.getSysBoolWithDefault(PROP_TARGET_C, false)) {
            return Kind.CNonTemplate;  // backward compatibility for old writers
        } else {
            return inst().curGenTarget;
        }
    }

    public static File configPath () {
        return inst().optConfigPath;
    }

    /**
     * Checks and returns whether the supplied State Machine name may have been
     * one of the specifically requested State Machines.
     * Supplied State Machine name should be the fully qualified name, while
     * requests may specify a regex pattern to match desired State Machines.
     * The regex supplied <re> is placed within "^.*<re>$", so at least the
     * entire short name of the StateMachine needs to match.
     * For example, if the requested set contains <blockquote>
     * { "SM", "p.*::SM", "p1::ASM" }
     * </blockquote>
     * the following StateMachines (provided they exist in the model) would
     * return <code>true</code>: <blockquote>
     * specificSMRequested("SM")<br>
     * specificSMRequested("p1::p2::SM")<br>
     * specificSMRequested("p1::SM")<br>
     * specificSMRequested("p1::ASM")
     * </blockquote>
     * while the following would return <code>false</code>: <blockquote>
     * specificSMRequested("M")<br>
     * specificSMRequested("p1::M")
     * specificSMRequested("p1::p2::ASM")<br>
     * </blockquote>
     * If the requested set is empty, any <code>name</code> would return <code>false</code>. 
     * If the requested set is nonempty, a <code>null</code> would return <code>true</code>. 
     * 
     * @return  <code>true</code> if the supplied State Machine name may be one
     *      of the specifically requested State Machines; <code>false</code> otherwise
     */
    public static boolean specificSMRequested (String name) {
        boolean rv = false;
        if (inst().smToGen.size() > 0) {
            if (name == null) {
                rv = true;
            } else {
                for (String reqName : inst().smToGen) {
                    if (Util.isDebugLevel()) {
                        Util.debug(">*> Matching SM pattern '" + reqName + "' in name: " + name);
                    }
                    if (Pattern.matches("^.*"+reqName+"$", name)) {  // found
                        rv = true;
                        break;  // done
                    }
                }
            }
        }
        return rv;
    }

    /**
     * Returns whether QF namespace was supplied in command-line option, which
     * also enables (in fact, requires) the use of namespaces in the generated
     * code.  This is primarily for the C++ backend.
     * 
     * @return  <code>true</code> if namespace has been supplied, <code>false</code> otherwise.
     */
    public static boolean isNamespaceEnabled () {
        return inst().getQfNamespace() != null;
    }

    /**
     * Returns whether custom QF include file was supplied in command-line
     * option.  This is primarily for the C++ backend.
     * 
     * @return  <code>true</code> if custom include file has been supplied, <code>false</code> otherwise.
     */
    public static boolean hasCustomQfInclude () {
        return inst().getQfInclude() != null;
    }

    /**
     * Returns whether a different output directory has been specified.
     * 
     * @return <code>true</code> if a different output directory was set, <code>false</code> otherwise.
     */
    public static boolean hasOutputDir () {
        return inst().getOutputDir() != null;
    }

    /**
     * Returns whether a different trace GUI output directory has been specified.
     * 
     * @return <code>true</code> if a different trace GUI output directory was set, <code>false</code> otherwise.
     */
    public static boolean hasGuiOutputDir () {
        return inst().getGuiOutputDir() != null;
    }
    public static String guiDirPrefix () {
        String prefixPath = "";
        if (hasGuiOutputDir()) {
            prefixPath += inst().getGuiOutputDir() + "/";
        }
        return prefixPath;
    }

    /**
     * Returns whether to filter only relevant events, which is based on whether
     * request has been made to generate specific State Machines, and whether
     * we're autocoding signals.
     * @return  <code>true</code> if only Transition events should be filtered,
     *  <code>false</code> otherwise.
     */
    public static boolean filterTransitionEvents () {
        return inst().smToGen.size() > 0 && !signalGenRequested();
    }

    /**
     * Returns whether implementation function calls should be "live."
     * @return  <code>true</code> if impl-calls should be uncommented, <code>false</code> otherwise.
     */
    public static boolean isImplCallLive () {
        return inst().optImplCall;
    }

    /**
     * Returns whether duplicate StateMachine names are allowed.
     * @return  <code>true</code> if duplicate SM names allowed, <code>false</code> otherwise.
     */
    public static boolean allowDupSMName () {
        return inst().optDupSMName;
    }
    
    /**
     * Returns the value for -scl_db_id
     */
    public static int get_scl_db_id () {
        return scl_db_id;
    }

    
    /**
     * Returns the value for -scl_rule_id
     */
    public static int get_scl_rule_id () {
        return scl_rule_id;
    }
    
    /**
     * Returns whether signals are being generated as a separate step; if so,
     * other generators can skip that step to avoid duplicate processing.
     * @return  <code>true</code> if signal generation has been requested separately,
     *   <code>false</code> otherwise.
     */
    public static boolean signalGenRequested () {
        return inst().propGenTargets.contains(Kind.Signals)
            || inst().propGenTargets.contains(Kind.CppSignals);
    }

    public static String configOptionValue (String optName) {
    	return (inst().configOptProps == null
    			? null
    			: inst().configOptProps.getProperty(optName+AC_CONFIG_OPTIONS_STEM_VALUE));
    }
    public static boolean configOptionTrue (String optName) {
        return ((inst().configOptProps != null)
                && Boolean.parseBoolean(inst().configOptProps.getProperty(optName)));
    }
    public static Object configUtil () {
        if (inst().configUtilClass == null) {  // lazy-init config util object
            String className = (inst().configOptProps == null ? null
                    : inst().configOptProps.getProperty(AC_CONFIG_OPTIONS_KEY_CONFIG_UTILCLASS));
            if (className != null) {
                try {
                    Class<?> c = Class.forName(className);
                    inst().configUtilClass = c.newInstance();
                    Util.debug("Util Class " + className + "%s instantiated!");
                } catch (ClassNotFoundException e) {  //ignore, move on
                    Util.debug("Autocoder.configUtil(): " + e.getLocalizedMessage());
                } catch (InstantiationException e) {  // ignore
                    Util.debug("Autocoder.configUtil(): " + e.getLocalizedMessage());
                } catch (IllegalAccessException e) {  // ignore
                    Util.debug("Autocoder.configUtil(): " + e.getLocalizedMessage());
                }
            }
        }

        return inst().configUtilClass;
    }

    // stores the command-line string invoked
    private String cmdLineInvoked = null;
    /* command-line options
     * - default null to allow old way of setting via system properties 
     */
    private Boolean optNoTrace = null;
    private Boolean optQualifySignals = null;
    private Boolean optIgnoreDupXmiIDs = null;
    // verbosity of Autocoder diagnostic output
    private boolean optVerbose = false;
    // debug flag for Autocoder debug output
    private boolean optDebug = false;
    // flag indicating whether to just perform a check and stop
    private boolean optCheckOnly = false;
    // flag indicating whether to never abort
    private boolean optNoAbort = false;

    // custom configuration directory path
    private File optConfigPath = null;
    // list of generator kinds this autocoder is to target 
    private List<Kind> propGenTargets = null;
    // set of specific state machines to output
    private Set<String> smToGen = null;
    // flag indicating whether to make impl-calls "live" (as in, uncommented)
    private boolean optImplCall = true;
    // flag indicating whether to allow duplicate StateMachine names
    private boolean optDupSMName = false;

    // String to use as namespace prefix of Quantum classes
    private String optQfNamespace = null;
    // custom include file to use for Quantum classes
    private String optQfInclude = null;
    // type of signal namespace to apply
    private SignalNamespaceType optSigNsType = SignalNamespaceType.NONE;
    // String to use as namespace prefix of base Signal header file
    private String optSigBaseNamespace = null;
    // Path to use as output directory
    private String optOutputDir = null;
    // Relative path to use as GUI output directory
    private String optGuiOutputDir = null;

    // System properties obj to store additional config-specific options
    private Properties configOptProps = null;
    private List<String> configOptions = null;
    private Object configUtilClass = null;

    // current generator target, used by old writers to see if they're autocoding C
    private Kind curGenTarget = null;
    // map of generator Kind to class
    private Map<Kind,Class<? extends IGenerator>> generatorMap = null;
    // list of input files to process
    private List<String> fileList = null;

    private boolean aborted = false;
    private Timer timer = null;


    /**
     * Default private constructor.
     */
    private Autocoder () {
        propGenTargets = new ArrayList<Kind>();
        generatorMap = new HashMap<Kind, Class<? extends IGenerator>>();
        fileList = new ArrayList<String>();
        smToGen = new LinkedHashSet<String>();  // preserve insertion order
        timer = new Timer();  // init timer to time whole process
    }

    /**
     * Initializes the Autocoder application, including processing the
     * command-line arguments, finding the available generators in classpaths,
     * and determining the output targets designated by command-line options.
     * 
     * @param args  input command-line arguments 
     */
    public void init (String [] args) {
        // initialize the switch options with defaults
        if (optIgnoreDupXmiIDs == null) {
            optIgnoreDupXmiIDs = false;
        }
        if (optNoTrace == null) {
            optNoTrace = false;
        }
        if (optQualifySignals == null) {
            optQualifySignals = false;
        }

        // first, find all generators available in "scope" (i.e., classpaths)
        //- this way, usage help correctly reflects unavailable generators
        findGenerators();

        // process command-line arguments and get input files ready
        processCommandArguments(args);

        // print option status to stdout
        System.out.println("Autocoder " + version());
        System.out.println("/ CWD " + System.getProperty("user.dir"));
        if (optCheckOnly)       System.out.println("|*opt: Enabling check only, stopping before writing");
        if (optNoAbort)         System.out.println("|*opt: Keep going without aborting");
        if (optVerbose)         System.out.println("|*opt: Enabling verbose output");
        if (optDebug)           System.out.println("|*opt: Enabling debug output");
        if (optConfigPath != null && configOptProps != null) {
            String configName = configOptProps.getProperty(AC_CONFIG_OPTIONS_KEY_CONFIG_NAME);
            System.out.println("|*opt: Configuring '" + configName + "' to " + optConfigPath);

            // report mission configuration boolean options
            if (optOutputDir == null) {  // Makefile gen NOT disabled
                System.out.println("|**" + AC_CONFIG_OPTIONS_KEY_CONFIG_MAKEFILE_PRESERVE + ": " + configOptionTrue(AC_CONFIG_OPTIONS_KEY_CONFIG_MAKEFILE_PRESERVE));
            }
            System.out.println("|**" + AC_CONFIG_OPTIONS_KEY_CONFIG_GETIMPL_SUPPRESS + ": " + configOptionTrue(AC_CONFIG_OPTIONS_KEY_CONFIG_GETIMPL_SUPPRESS));
            System.out.println("|**" + AC_CONFIG_OPTIONS_KEY_CONFIG_QASUBS_SUPPRESS + ": " + configOptionTrue(AC_CONFIG_OPTIONS_KEY_CONFIG_QASUBS_SUPPRESS));

            if (configUtil() != null) {
                System.out.println("|**" + configName + ": Util class " + configUtil().getClass().getCanonicalName());
            }

            // report custom options that define .value
            for (String key : configOptions) {
                if (configOptProps.containsKey(key+AC_CONFIG_OPTIONS_STEM_VALUE)) {
                    System.out.println("|**" + configName + configOptProps.getProperty(key)
                            + ": " + configOptionValue(key));
                }
            }
        }
        if (optIgnoreDupXmiIDs) System.out.println("|*opt: Ignoring duplicate XMI IDs");
        if (optNoTrace)         System.out.println("|*opt: Turning off execution tracing");
        if (optQualifySignals)  System.out.println("|*opt: Qualifying signals");
        if (optImplCall)        System.out.println("|*opt: Turning on implementation function calls");
        if (optDupSMName)       System.out.println("|*opt: Allowing duplicate StateMachine names");
        if (smToGen.size() > 0) {
            System.out.println("|*opt: Generating only specific State Machine"
                    + (smToGen.size() > 1 ? "s" : "")
                    + " " + smToGen.toString());
        }
        if (optQfNamespace != null) {
            System.out.println("|*opt: Using QF namespace: " + optQfNamespace);
        }
        if (optQfInclude != null) {
            System.out.println("|*opt: Using QF include file: <" + optQfInclude + ">");
        }
        if (optSigNsType != SignalNamespaceType.NONE) {
            System.out.println("|*opt: Setting namespace of signals: " + optSigNsType.name());
        }
        if (optSigBaseNamespace != null) {
            System.out.println("|*opt: Using as base signal namespace: " + optSigBaseNamespace);
        }
        if (optOutputDir != null) {
            System.out.println("|*opt: Disabling Makefile/main + autocoding to dir: " + optOutputDir);
        }
        if (!optNoTrace && optGuiOutputDir != null) {
            System.out.println("|*opt: Writing trace GUI products to dir: " + optGuiOutputDir);
        }
        System.out.println("\\ Found generator kind(s) " + Arrays.toString(generatorMap.keySet().toArray()));

        // if no output target is found, print usage and exit
        if (propGenTargets.isEmpty()) {
            System.err.println("**No generator target specified, nothing to do!");
            aborted = true;
            printUsageAndExit();
        }
    }

    /**
     * No real disposing necessary, just outputs any final messages before finishing.
     *
     * @return boolean <code>true</code> if no issues, <code>false</code> if aborted.
     */
    public boolean dispose () {
        long dur = timer.markTime();
        StringBuilder sb = new StringBuilder("Autocoding ");
        if (optCheckOnly) {
            sb.append("Check-Only ");
        }
        if (aborted) {
            sb.append("Aborted");
        } else {
            sb.append("Finished");
        }
        sb.append(" (" + dur + "ms).");
        System.out.println(sb.toString());
        System.out.println();

        return !aborted;
    }


    /**
     * Sets the customization configuration path to read specific configurations
     * (e.g., additional command-line options)
     * as well as look for overriding velocity template files.
     * @param path  String of the file path
     */
    public void setConfigPath (String path) {
    	optConfigPath = new File(path);

    	// check config path
        if (!optConfigPath.isAbsolute()) {  // prepend the CWD
        	optConfigPath = new File(System.getProperty("user.dir"), path);
        }
        if (optConfigPath.exists() && optConfigPath.isDirectory()) {
        	// search config path for additional command-line options
        	configOptProps = new Properties();
        	try {
				configOptProps.load(new FileInputStream(new File(optConfigPath, AC_CONFIG_OPTIONS_FILE)));
				// process the additional options
				configOptions = Util.newList();
				String configName = configOptProps.getProperty(AC_CONFIG_OPTIONS_KEY_CONFIG_NAME);
				int optCnt = Integer.parseInt(configOptProps.getProperty(configName+AC_CONFIG_OPTIONS_STEM_OPT+AC_CONFIG_OPTIONS_STEM_CNT));
				for (int i=1; i <= optCnt; ++i) {
					String optName = (String) configOptProps.getProperty(configName+AC_CONFIG_OPTIONS_STEM_OPT+"."+i+AC_CONFIG_OPTIONS_STEM_NAME);
					String optKey = configName+AC_CONFIG_OPTIONS_STEM_OPT+"."+optName;
					configOptProps.setProperty(optKey, "-"+optName);
					configOptions.add(optKey);
				}
			} catch (FileNotFoundException e) {  // just print error at this stage
				e.printStackTrace();
				configOptProps = null;
			} catch (IOException e) {  // just print error at this stage
				e.printStackTrace();
				configOptProps = null;
			}
        } else {
        	Util.warn("WARNING! " + Autocoder.OPT_CUSTOM_CONFIG + " directory '"
        			+ optConfigPath.getAbsolutePath()
        			+ "' is not a valid directory!");
        }
    }

    /**
     * Sets the flag that controls whether to disable GUI execution trace.
     * @param b  new boolean state to set to
     */
    public void setNoTrace (boolean b) {
        optNoTrace = b;
    }

    /**
     * Sets the flag that controls output verbosity of Autocoder.
     * @param b  new boolean state to set to
     */
    public void setVerbose (boolean b) {
        optVerbose = b;
    }

    /**
     * Sets the flag that controls debug output of Autocoder.
     * @param b  new boolean state to set to
     */
    public void setDebugOn (boolean b) {
        optDebug = b;
    }

    /**
     * Sets the flag that controls check-only mode of Autocoder.
     * @param b  new boolean state to set to
     */
    public void setCheckOnly (boolean b) {
        optCheckOnly = b;
    }

    /**
     * Sets the flag that controls the no-abort mode of Autocoder.
     * @param b  new boolean state to set to
     */
    public void setNoAbort (boolean b) {
        optNoAbort = b;
    }

    /**
     * Sets the flag that controls whether ImplClass function calls are
     * written out uncommented, <code>true</code> to uncomment.
     * @param b  new boolean state to set to
     */
    public void setImplCall (boolean b) {
        optImplCall = b;
    }

    /**
     * Sets the flag that allows duplicate StateMachine names.
     * @param b  new boolean state to set to
     */
    public void setDupSMName (boolean b) {
        optDupSMName = b;
    }

    /**
     * Sets the namespace string for Quantum Framework classes that should be
     * stamped out in relevant target languages (e.g., C++ code).
     * 
     * TODO A better way to do QF namespace that doesn't pollute the Autocoders class?
     * 
     * @param ns  string to use as namespace
     */
    public void setQfNamespace (String ns) {
        if (ns != null
                && (ns.length() == 0 || ns.equals("''")
                        || ns.equals(Util.PACKAGE_SEP))) {
            // store a blank namespace string to enable namespace usage
            optQfNamespace = "";
        } else {
            optQfNamespace = ns;
        }
    }
    /**
     * Returns the namespace string for the Quantum Framework classes.
     * @return  String identifying the QF namespace, in the form of &lt;NS_1&gt;::..::&lt;NS_n&gt;.
     */
    public String getQfNamespace () {
        return optQfNamespace;
    }

    /**
     * Sets the custom include file for Quantum Framework classes that should be
     * stamped out in relevant target languages (e.g., C++ code).
     * 
     * TODO A better way to do QF include that doesn't pollute the Autocoders class?
     * 
     * @param ns  string to use as include file
     */
    public void setQfInclude (String incFile) {
        optQfInclude = incFile;
    }
    /**
     * Returns the include file name for custom Quantum Framework classes.
     * 
     * @return  String of QF include file.
     */
    public String getQfInclude () {
        return optQfInclude;
    }

    /**
     * Returns the type of signal namespacing to use.
     * 
     * @return  An enum value of GLOBAL, LOCAL, or NONE.
     */
    public SignalNamespaceType getSignalNamespaceType () {
        return optSigNsType;
    }
    /**
     * Sets the namespace string for the base signal header file, which is
     * required if -cppsig is specified as either local or global.
     * 
     * @param ns  string to use as signal base namespace.
     */
    public void setSigBaseNamespace (String ns) {
        optSigBaseNamespace = ns;
    }
    /**
     * Returns the namespace string for the base signal header file
     * @return  String identifying the base signal namespace, in the form of &lt;NS_1&gt;::..::&lt;NS_n&gt;.
     */
    public String getSigBaseNamespace () {
        return optSigBaseNamespace;
    }

    /**
     * Sets the path string to use as output directory for autocoded code. 
     * 
     * @param path  string of output path
     */
    public void setOutputDir (String path) {
        optOutputDir = path;
    }
    /**
     * Returns the path to use for output directory.
     * @return  String of output path.
     */
    public String getOutputDir () {
        return optOutputDir;
    }

    /**
     * Sets the path string to use as output directory for autocoded trace GUI products. 
     * 
     * @param path  string of trace GUI output path
     */
    public void setGuiOutputDir (String path) {
        optGuiOutputDir = path;
    }
    /**
     * Returns the path to use for trace GUI output directory.
     * @return  String of output path for trace GUI.
     */
    public String getGuiOutputDir () {
        return optGuiOutputDir;
    }

    /**
     * Returns the command-line string invoked on this autocode run.
     * @return  String of the full command line invocation.
     */
    public String getCmdLineInvoked () {
    	return cmdLineInvoked;
    }

    /**
     * Invokes designated generators on all inputs.
     */
    public void generate () {
        IReader reader = null;
        String[] inputs = fileList.toArray(new String[0]);

        for (Kind target : propGenTargets) {
            System.out.println("Autocoding to target " + target.label());
            curGenTarget = target;

            // Instantiate generator class
            try {
                Class<? extends IGenerator> genClass = generatorMap.get(target);
                if (genClass == null) {  // can't continue
                    System.err.println("**Sorry! No generator found for target "
                            + target.label());
                    System.err.println("** Make sure that the generator exists in the Jar or classpath.");
                    continue;
                }
                IGenerator generator = genClass.newInstance();
                // Invoke generator using existing reader, if any, on the entire list of input sources
                generator.generate(reader, inputs);
                if (reader == null) {
                    reader = generator.reader();  // save the reader
                }
            } catch (InstantiationException e) {  // ignore
                Util.debug("Autocoder.generate() of target " + target.label()
                        + ": " + e.getLocalizedMessage());
            } catch (IllegalAccessException e) {  // ignore
                Util.debug("Autocoder.generate() of target " + target.label()
                        + ": " + e.getLocalizedMessage());
            } catch (FatalModelException e) {
                aborted = true;
                System.out.println("**ABORT target " + target.label()
                        + "! Fatal model error(s) prevented Autocoder from generating proper code!");
                throw e;  // propagate error to abnormally terminate Autocoder
            }
        }
    }

    /**
     * Finds all generator classes within this Autocoder class hierarchy that
     * implement the {@link IWriter} interface and are annotated with {@link GeneratorKind},
     * and populate the generator map of {@link Kind} to {@link Class}.
     */
    private void findGenerators () {
        for (Class<? extends IGenerator> c
                : Util.findSubclassesUnder(IGenerator.class, ".+Gen.+", true)) {
            // get class and read its annotations for Generator Kind
            GeneratorKind kindAnnote = c.getAnnotation(GeneratorKind.class);
            if (kindAnnote != null) {
            	if (generatorMap.containsKey(kindAnnote.value())) {
            		Util.error("Autocoder.findGenerators() found more than one generator for GeneratorKind '"
            				+ kindAnnote.value() + "', the last one found takes effect! ["
            				+ generatorMap.get(kindAnnote.value()).getCanonicalName()
            				+ ", " + c.getCanonicalName() + "]");
            	}
                generatorMap.put(kindAnnote.value(), c);
            }
        }
    }

    private void processCommandArguments (String [] args) {
        // store argument list
        List<String> argList = new LinkedList<String>();
        argList.addAll(Arrays.asList(args));

        /* process the options, using array index
         * - index is always incremented at the beginning of the next loop
         * - after an arg is removed, we must decrement index to account for it
         */
        ARGLOOP: for (int i=0; i < argList.size(); ++i) {
            String arg = argList.remove(i--);  // decrement idx _after_ removal
            // OPT_CUSTOM_CONFIG must go first, as it affects 'usage' output
            if (arg.equals(OPT_CUSTOM_CONFIG)) {
                String path = argList.remove(i+1);
                setConfigPath(path);
                continue;
                
            } else if (arg.equals(OPT_SCL_DB_ID)) {
                String dataBaseID = argList.remove(i+1);
                scl_db_id = Integer.valueOf(dataBaseID);
                continue;
                
            } else if (arg.equals(OPT_SCL_Rule_ID)) {
                String ruleID = argList.remove(i+1);
                scl_rule_id = Integer.valueOf(ruleID);
                continue;

            } else if (arg.equals(OPT_USAGE)) {
                printUsageAndExit();

            } else if (arg.equals(OPT_CHECK_ONLY)) {
                setCheckOnly(true);
                continue;

            } else if (arg.equals(OPT_NO_ABORT)) {
                setNoAbort(true);
                continue;

            } else if (arg.equals(OPT_VERBOSE)) {
                setVerbose(true);
                continue;

            } else if (arg.equals(OPT_DEBUG)) {
                setDebugOn(true);
                continue;

            } else if (arg.equals(OPT_VERSION)) {
                printVersionAndExit();

            } else if (arg.equals(OPT_NO_IMPL_CALLS)) {
                setImplCall(false);
                continue;

            } else if (arg.equals(OPT_NO_TRACE)) {
                setNoTrace(true);
                continue;

            } else if (arg.equals(OPT_STATE_MACHINE)) {  // fetch option argument
                String smName = argList.remove(i+1);
                if (smName.startsWith("-")) {  // error?!
                    System.err.println("Error! " + arg + " should be followed by a State Machine name, not another option!");
                    printUsageAndExit();
                }
                smToGen.add(smName);
                continue;

            } else if (arg.equals(OPT_QF_NAMESPACE)) {
                String nsStr = argList.remove(i+1);
                setQfNamespace(nsStr);
                continue;

            } else if (arg.equals(OPT_QF_INCLUDE)) {
                String incFile = argList.remove(i+1);
                setQfInclude(incFile);
                continue;

            } else if (arg.equals(OPT_SIG_NAMESPACE)) {
                String nsType = argList.remove(i+1);
                try {
                    optSigNsType = SignalNamespaceType.valueOf(nsType.toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.err.println("Error! " + arg + " supplied with unrecognized type '" + nsType + "'!");
                    printUsageAndExit();
                }
                continue;

            } else if (arg.equals(OPT_SIG_BASE_NAMESPACE)) {
                String nsStr = argList.remove(i+1);
                setSigBaseNamespace(nsStr);
                continue;

            } else if (arg.equals(OPT_OUTPUT_DIR)) {
                String dir = argList.remove(i+1);
                setOutputDir(dir);
                continue;

            } else if (arg.equals(OPT_GUI_OUTPUT_DIR)) {
                String dir = argList.remove(i+1);
                setGuiOutputDir(dir);
                continue;

            } else if (arg.equals(OPT_DUP_SM)) {
                setDupSMName(true);
                continue;

            } else if (arg.equals(OPT_IGNORE_DUP_IDS)) {
                optIgnoreDupXmiIDs = true;
                continue;
                
            } else if (arg.equals(OPT_IMPL_CALLS)) {
                setImplCall(true);
                continue;

            } else if (arg.equals(OPT_QUALIFY_SIGNALS)) {
                optQualifySignals = true;
                continue;

            } else if (opt2GenKindMap.containsKey(arg)) {  // generator option
                propGenTargets.add(opt2GenKindMap.get(arg));

            } else {
            	if (configOptProps != null) {
            		// attempt to process additional options
            		for (String moreOpt : configOptions) {
            			if(arg.equals(configOptProps.getProperty(moreOpt))) {
            			    if (Boolean.parseBoolean(configOptProps.getProperty(moreOpt+AC_CONFIG_OPTIONS_STEM_NOARG))) {
            			        // no-arg option, store "true"
                                configOptProps.setProperty(moreOpt+AC_CONFIG_OPTIONS_STEM_VALUE, "true");
            			    } else {
            			        // by default, option takes an argument
                                configOptProps.setProperty(moreOpt+AC_CONFIG_OPTIONS_STEM_VALUE, argList.remove(i+1));
            			    }
            				continue ARGLOOP;
            			}
            		}
            	}
            	// warn about unknown options, but skip input files
                if (arg.startsWith("-")) {
                    System.err.println("**Unknown option: " + arg);
                } else {  // treat as input file
                    fileList.add(arg);
                }
            }
        }

        // Additional option sanity checks:
        // make sure any required additional opts are defined
        if (configOptProps != null) {
        	for (String moreOpt : configOptions) {
        		if (Boolean.parseBoolean(configOptProps.getProperty(moreOpt+AC_CONFIG_OPTIONS_STEM_REQD))) {
        			String optVal = configOptProps.getProperty(moreOpt+AC_CONFIG_OPTIONS_STEM_VALUE);
        			if (optVal == null) {  // uh oh! option not set!
        				System.err.println("Error! Please specify the required option "
        						+ configOptProps.getProperty(moreOpt));
        				printUsageAndExit();
        			}
        		}
        	}
        }
        // cross-check against -cppnsqf option if global or none
        if (optSigNsType == SignalNamespaceType.GLOBAL
                && optQfNamespace == null) {
            System.err.println("Error! Please specify option "
                    + OPT_QF_NAMESPACE + " in order to use "
                    + OPT_SIG_NAMESPACE + " " + optSigNsType.name() + "!");
            printUsageAndExit();
        } else if (optSigNsType == SignalNamespaceType.NONE
                && !Util.isVarArgsEmpty(optQfNamespace)) {
            System.err.println("Warning! Specifying " + OPT_QF_NAMESPACE
                    + " without " + OPT_SIG_NAMESPACE
                    + " ledas to broken signal references; overriding "
                    + OPT_QF_NAMESPACE
                    + " with " + SignalNamespaceType.GLOBAL + "!");
            optSigNsType = SignalNamespaceType.GLOBAL;
        }
        // make sure base signal namespace is set if -cppsig set
        if (optSigNsType != SignalNamespaceType.NONE
                && optSigBaseNamespace == null) {  // see if there's -cppqfns
            if (optQfNamespace == null) {
                System.err.println("Error! Please specify either option "
                        + OPT_SIG_BASE_NAMESPACE + " or " + OPT_QF_NAMESPACE
                        + " if you specify "
                        + OPT_SIG_NAMESPACE + "!");
                printUsageAndExit();
            } else {  // use the Qf namespace as signal base namespace
                setSigBaseNamespace(optQfNamespace);
            }
        }
        // make sure we have at least one input file
        if (fileList.size() == 0) {  // no input files?!
            System.err.println("Error! No input model file was specified, nothing to generate!");
            printUsageAndExit();
        }
        if (argList.size() > 0) {  // check for unprocessed args
            System.err.println("**Unprocessed arguments?! " + argList.toString());
        }

        // for backward compat: make sure system properties are checked
        optIgnoreDupXmiIDs |= Util.getSysBoolWithDefault(PROP_IGNORE_DUP_IDS, false);
        optNoTrace |= Util.getSysBoolWithDefault(PROP_NO_TRACE, false);
        optQualifySignals |= Util.getSysBoolWithDefault(PROP_QUALIFY_SIGNALS, false);
        if (Boolean.valueOf(System.getProperty(PROP_TARGET_C)) == true) {
            // add (to front of list) non-template version C as gen target
            propGenTargets.add(0, Kind.CNonTemplate);
        }

        // store the full command line option invoked
        cmdLineInvoked = Util.join(args, " ");
    }

    /**
     * Prints the application version.
     */
    private void printVersionAndExit () {
        System.out.println("JPL Autocoder version " + AC_VERSION);
        System.out.printf(AC_COPYRIGHTS, Calendar.getInstance().get(Calendar.YEAR));
        System.out.println();
        System.out.println("ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.");
        System.exit(0);
    }

    /**
     * Prints the usage and exits application.
     */
    private void printUsageAndExit () {
        final String NEWL = System.getProperty("line.separator");
        final String PAD = "    ";
        int len2ExtraTab = 8-PAD.length();  // to determine if extra tab needed 

        String cmdStr = System.getenv("0");
        if (cmdStr == null) {  // use our own default
            cmdStr = "java -jar <path>/autocoder.jar";
        }

        System.err.println();
        System.err.println("Usage:");
        System.err.println(PAD + cmdStr + " <generator>+ [<option>]* <file.xml>+");
        System.err.println(PAD + cmdStr + " " + OPT_USAGE + " | " + OPT_VERSION);
        System.err.println();
        System.err.println("Generator back-ends supported (^ means unavailable in classpath):");
        for (Map.Entry<String,Kind> optEntry : opt2GenKindMap.entrySet()) {
            String optStr = optEntry.getKey();
            Kind genKind = optEntry.getValue();
            String append = "";
            String className = null;
            if (generatorMap.containsKey(genKind)) {  // autocoder supports it
            	String fullName = generatorMap.get(genKind).getCanonicalName();
            	className = fullName.substring(fullName.lastIndexOf(".", fullName.lastIndexOf(".")-1)+1);
            } else {
                append += "^";
            }
            if ((optStr.length() + append.length()) < len2ExtraTab) {
                append += "\t";
            }
            System.err.println(PAD + optStr + append + "\t\t" + genKind.label()
            		+ (className == null ? "" : " (" + className + ")"));
        }
        System.err.println();
        System.err.println("Options, switch default is FALSE if not set (^ means as-yet-unimplemented):");
//        System.err.println(PAD + OPT_
//                + "\t\t\t");
        System.err.println(PAD + OPT_USAGE
                + "\t\t\tprint this usage help and exit");
        System.err.println(PAD + OPT_DEBUG
                + "\t\tenable Autocoder debug output");
        System.err.println(PAD + OPT_VERBOSE
                + "\t\tenable verbose Autocoder output");
        System.err.println(PAD + OPT_VERSION
                + "\t\tprint product version & copyrights and exit");
        System.err.println();
        System.err.println(PAD + OPT_CUSTOM_CONFIG
                + " <path>\tspecify a custom config directory for template overrides" + NEWL
                + "\t\t\t(override templates by package path and name)");
        System.err.println(PAD + OPT_NO_IMPL_CALLS
                + "\tdeactivate implementation function calls");
        System.err.println(PAD + OPT_NO_TRACE
                + "\t\tturn StateMachine execution trace off" + NEWL
                + "\t\t\t(equiv. VM prop: -D" + PROP_NO_TRACE + "=true)");
        System.err.println(PAD + OPT_STATE_MACHINE
                + " <SM_i>\t\tgenerate a specific State Machine (may use 1+ times)");
        System.err.println(PAD + OPT_CHECK_ONLY
                + "\t\tread & check input files, then exit (won't write files)");
        System.err.println(PAD + OPT_NO_ABORT
                + "\t\tkeep running despite model aborts" + NEWL
                + "\t\t\t(Note: Java RunTimeException abort may be unavoidable)");
        System.err.println();
        System.err.println(PAD + OPT_QF_NAMESPACE
                + " <ns>\tuse as C++ namespace for QF classes; default NONE" + NEWL
                + "\t\t\t(required to enable package namespaces; may use '')");
        System.err.println(PAD + OPT_QF_INCLUDE
                + " <inc>\tuse <inc> as include file for customized C++ QF classes");
        System.err.println(PAD + OPT_SIG_NAMESPACE
                + " global\tuse <ns> namespace for all signals");
        System.err.println(PAD + OPT_SIG_NAMESPACE
                + " local\tuse model-defined namespace for signals" + NEWL
                + "\t\t\t(requires <sns>, or default to <ns>, for base header file)");
        System.err.println(PAD + OPT_SIG_BASE_NAMESPACE
                + " <sns>\tuse as namespace for base signal header file");
        System.err.println(PAD + OPT_OUTPUT_DIR
                + " <out>\t\tOutput directory for autocoded products; default '.'" + NEWL
                + "\t\t\t(automatically disables Makefile and main in C/C++)");
        System.err.println(PAD + OPT_GUI_OUTPUT_DIR
                + " <gui>\tOutput dir for trace GUIs, if enabled; rel. to <out>");
        System.err.println();
        System.err.println("Deprecated, unused options:");
        System.err.println(PAD + OPT_DUP_SM
                + "\tallow duplicate StateMachine names");
        System.err.println(PAD + OPT_IGNORE_DUP_IDS
                + "\tignore duplicate XMI IDs," + NEWL
                + "\t\t\tuseful when generating signals from multiple input files" + NEWL
                + "\t\t\t(VM prop: -D" + PROP_IGNORE_DUP_IDS + "=true)");
        System.err.println(PAD + OPT_IMPL_CALLS
                + "\t\tactivate implementation function calls");
        System.err.println(PAD + OPT_QUALIFY_SIGNALS
                + "\tprint fully-qualified signal names (deprecated?!)" + NEWL
                + "\t\t\t(VM prop: -D" + PROP_QUALIFY_SIGNALS + "=true)");
        System.err.println();

        if (configOptProps != null) {  // print usage for additional options
            System.err.println("Configuration '"
            		+ configOptProps.getProperty(AC_CONFIG_OPTIONS_KEY_CONFIG_NAME)
            		+ "' options (+ means required):");
        	for (String optName : configOptions) {
        		String info = configOptProps.getProperty(optName+AC_CONFIG_OPTIONS_STEM_INFO);
        		System.err.println(PAD + configOptProps.getProperty(optName)+" "
        				+ configOptProps.getProperty(optName+AC_CONFIG_OPTIONS_STEM_DESC)
        				+ (Boolean.parseBoolean(configOptProps.getProperty(optName+AC_CONFIG_OPTIONS_STEM_REQD)) ? "+" : "")
        				+ (info == null ? "" : NEWL + "\t\t\t" + info));
        	}
        }

        // terminate upon printing usage
        System.exit(aborted ? -1 : 0);
    }


    /**
     * Main method for the StateChart Autocoders application.  Parses arguments
     * to determine what target programming language to generate.
     * 
     * @param args  command-line arguments
     */
    public static void main (String[] args) {
        Autocoder autocoder = Autocoder.inst();
        autocoder.init(args);
        try {
            autocoder.generate();
        } catch (FatalValidationException e) {
            // just print the model validation message and not the stack.
            System.err.println(e.getMessage());
        } catch (Throwable t) {  // catch RuntimeException and anything else!
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {  // ignore
            }
            System.out.println();  // use stdout to order output in context 
            t.printStackTrace();  // this should include output of nested ex's.
        } finally {
            // return any error code to system
            System.exit(autocoder.dispose() ? 0 : -1);
        }
    }

}
