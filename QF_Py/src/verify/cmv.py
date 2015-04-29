'''
Refactored on Sep 21, 2010 from verify.py

@author: Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
'''
import os
import sys
import string

from verify import verifier

class CVerifier(verifier.Verifier):
    """
    This unittest class uses Pexpect to perform blackbox input-output testing
    on the C suite of Autocoder tests.  Support functions take care of
    processing the StateChartSignals.h file and compiling the C code, in
    addition to the common functions provided by the base class.

    Individual testcases just run a given test XML file.
    """
    # Include and library directories of QF_C
    QF_INCLUDE = None
    QF_LIB = None
    XMLRPC_LIB = None 

    def __init__(self, methodName='runTest'):
        # Must call super class init to properly initialize unittest class
        verifier.Verifier.__init__(self, methodName)
        #
        # C base directory
        self.BASEDIR = os.path.abspath(os.sep.join([verifier.Verifier.PYTHON_ROOT, "..", "QF_Cm"]))
        # Include and library directories
        self.QF_INCLUDE = self.BASEDIR
        self.QF_LIB = os.sep.join([self.BASEDIR, "linux"])
        self.XMLRPC_LIB = os.sep.join([self.BASEDIR, "xmlrpc"]) 
        #
        self._cmdMap = None

    def setUp(self):
        self._commonSetUp()
        # add build target directory
        self._subdirsToClean += [ "linux" ]

    def tearDown(self):
        self._commonTearDown()


    def _processStatechartSignals(self):
        acDir = os.sep.join([self._dir, self.AUTOCODE_DIR])
        # Read the StatechartSignals.h to get the signal numbers
        try:
            input = open(os.sep.join([acDir, "StatechartSignals.h"]), 'r')
        except IOError:
            raise self.failureException, "*** Could NOT find StatechartSignals.h"
        #
        # Used to map from enumeration type to signal integer
        self._cmdMap = {}
        #
        # Map the During signal (tick for C++)
        self._cmdMap["DURING"] = 4
        self._cmdMap["tick"] = 4
        #
        # Loop until the user defined signals are found
        # Reading StatechartSignals.h
        while 1:
            data = input.readline()
            self.assertTrue(data, "*** Error in StatechartSignals.h - expected a string: 'User defined signals'")
            if string.find(data, "User defined signals") != -1:
                break
        #
        # Map each signal to an integer
        ival = 5
        while 1:
            data = input.readline()
            if not data:
                break
            if string.find(data, "MAX_SIG") != -1:
                break
            data = string.strip(data)
            if not data: continue
            # Skip this entire line if it starts with a '/'
            if data[0] == '/': continue
            data = data.split(',')
            self._cmdMap[data[0]] = ival
            ival = ival + 1

    def _compileSM(self, smList=[]):
        acDir = os.sep.join([self._dir, self.AUTOCODE_DIR])

        darwinOpts = ""
        if sys.platform == 'darwin':
            darwinOpts = "-Wl,-all_load "
        #
        # set up and issue compilation command
        compileCmd = " ".join\
            (["cd", acDir, ";",
              "gcc -DDEFINE_MAIN -DDEFINE_CPP_UNITTEST -c -Wall -g",
                    "-I.",
                    "-I"+self.QF_INCLUDE,
                    "-I"+self.XMLRPC_LIB,
                "main.c",
                self.XMLRPC_LIB+"/log_event.c"])
        if len(smList) == 0:  # compile files based on testSM name
            compileCmd = " ".join([compileCmd, self._testSM+".c", self._testSM+"Impl.c"])
        else:
            for f in smList:
                compileCmd = " ".join([compileCmd, f+".c", f+"Impl.c"])
        compileCmd = " ".join\
            ([compileCmd, "; gcc",
              "-o", self._testSM,
              "*.o", "-L"+self.QF_LIB,
              darwinOpts+"-lqf-c",
              "; cd -"])
        print compileCmd
        os.system(compileCmd)

    def _sendEvent(self, event):
        cmdString = event
        if event[0] == '=':  # send event as-is if first char is '='
            cmdString = event[1:]
        else:
            if event not in self._cmdMap:
                print "** WARNING! Test script uses an undefined SignalEvent '%s', ignored." % event
                return
            else:
                cmdString = self._cmdMap[event]
        #
        print "> Sending pExpect:", cmdString
        self._smApp.send(str(cmdString) + '\n')


    def doTestCase(self, testSM, desc="Nondescript TestCase!",
                   dir=None, smList=[], script=None,
                   preserveImpl=False, useSimState=False,
                   testAutocodeFailure=False,
                   expectFile=None, autocodeOpts=""):
        """
        The core of a testcase, it coordinates an individual test, running
        Pexpect with the supplied StateMachine.  Following unittest convention,
        each TestCase is named "test<SM>", where <SM> is StateMachine to test.
        It should invoke this method with the SM name and a concise description.
        
        @param testSM: name of the StateMachine model file; required.
        @param desc: a concise, informative description of the TestCase for
            test reporting; default: "Nondescript TestCase!"
        @param dir: directory under which testSM can be found; default: None
        @param smList: list of StateMachine names within the testSM to test,
            or all if unspecified; default: []
        @param script: a Python test script to run; default: None.
            If supplied, Pexpect is NOT used, instead the StateMachine(s) will
            be invoked via sim_state_start, and the test script run against QF
            to determine if desired conditions are met for passing the test.
            NOTE: the test script must implement the "run" method.
        @param preserveImpl: flag indicating whether NOT to delete the Impl
            files when cleaning up after the test, which is critical for test
            cases that use a custom Impl file; default: False
        @param useSimState: flag indicating whether to use sim_state_start to
            invoke the test; default: False
        @param testAutocodedFailure: flag indicating whether the test is
            simply to see if autocoding fails, which would NOT attempt to
            invoke the StateMachine code; default: False
        @param expectFile: name of text file to use for pexpect, or default to
            testSM name appended with "-expect.txt"; default: None
        @param autocodeOpts: additional options to supply to the Autocoder;
            default: ""

        @see: verifier.Verifier.doTestCase()
        """
        # configure a few optional parameters
        if dir is None:  # set dir same as testSM if not set
            dir = testSM
        # call parent doTestCase
        if not verifier.Verifier.doTestCase(self, testSM, desc,
                                   os.sep.join([self.BASEDIR, "test"]),
                                   smList, script, preserveImpl,
                                   useSimState, testAutocodeFailure):
            return
        #
        # Check if C test suite can proceed, checking for QF_C lib
        self.assertTrue(os.path.exists(os.sep.join([self.BASEDIR, "linux", "libqf-c.a"])),\
                        "*** QF_Cm library not compiled, could not proceed!\n" +\
                        "==> Please go to QF_Cm/ and execute 'make clean all' first")
        #
        print
        print "Autocoding " + self._testSM + " and fetching its signals..."
        # if 'smList' supplied, construct a list of StateMachine options
        opts = autocodeOpts + " -csignals"
        for sm in smList:
            opts += " -sm %s" % sm
        self._autocoder(target="-cm", opts=opts, javaOpts="-DDEFINE_MAIN")
        #
        print "Compiling State Machine application..."
        self._compileSM(smList)
        if self._buildAndStop:  # don't run test
            self._endTestCase(verifier.TestReporter.TEST_SKIP)
            return
        #
        print "Process StateChartSignals..."
        self._processStatechartSignals()
        #
        print
        print "Starting test: " + self.COLORS['bold'] + desc + self.COLORS['default']
        self._startApp(targetApp=self._testSM)
        result = self._checkResults(expectFile=expectFile)
        self._endTestCase(result)
