'''
Refactored on Sep 21, 2010 from verify.py

@author: Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
'''
import re
import os
import sys
import string

from verify import cv, verifier

class CppVerifier(cv.CVerifier):
    """
    This unittest class uses Pexpect to perform blackbox input-output testing
    on the C++ suite of Autocoder tests.  Support functions take care of
    processing the StateChartSignals.h file and compiling the C++ code, in
    addition to the common functions provided by the base class.

    Individual testcases just run a given test XML file.
    """
    # Include and library directories of QF_C++
    QF_INCLUDE = None
    QF_LIB = None
    XMLRPC_LIB = None 

    def __init__(self, methodName='runTest'):
        # Must call super class init to properly initialize unittest class
        cv.CVerifier.__init__(self, methodName)
        #
        # C++ base directory
        self.BASEDIR = os.path.abspath(os.sep.join([verifier.Verifier.PYTHON_ROOT, "..", "QF_Cpp"]))
        # Include and library directories
        self.QF_INCLUDE = os.sep.join([self.BASEDIR, "include"])
        self.QF_LIB = os.sep.join([self.BASEDIR, "linux"])
        self.XMLRPC_LIB = os.sep.join([self.BASEDIR, "lib", "xmlrpc"]) 
        #
        self._cmdMap = None

    def setUp(self):
        self._commonSetUp()
        # add build target directory
        self._subdirsToClean += [ "linux" ]

    def tearDown(self):
        self._commonTearDown()


    def __findAllSignalFiles(self, dir):
        for f in os.listdir(dir):
            fPath = os.sep.join([dir, f])
            m = re.match(r"StatechartSignals.h$", f)
            if m is not None:
                # add signal file to list
                self.__signalFiles.append(fPath)
            elif os.path.isdir(fPath):
                self.__findAllSignalFiles(fPath)

    def __parseIdRange(self, f):
        try:
            input = open(f, 'r')
        except IOError:
            raise self.failureException, "*** Could NOT open '%s' for read" % f
        #
        # Reset hash val dict
        self.__hashVal = {}
        # Read until all var = vals are processed
        while 1:
            data = input.readline()
            if not data:
                break  # more more lines
            m = re.match(r"(?P<name>[\w\d_]+).*?/\*.*?(?P<hex>0x)?(?P<val>[0-9A-Fa-f]+).*?\*/", string.strip(data))
            if m is not None:  # store as hash value
                if m.group('hex') is not None:  # parse as hex value
                    val = int(m.group('val'), 16)
                else:
                    val = int(m.group('val'))
                self.__hashVal[m.group('name')] = val
        if self._verbose:
            print "(From %s, acquired hash %s)" % (f, repr(self.__hashVal))

    def _processStatechartSignals(self):
        # Used to map from enumeration type to signal integer
        self._cmdMap = {}
        #
        # Map the During signal (tick for C++)
        self._cmdMap["DURING"] = 4
        self._cmdMap["tick"] = 4
        #
        acDir = os.sep.join([self._dir, self.AUTOCODE_DIR])
        # start with empty list
        self.__signalFiles = []
        self.__hashVal = {}
        self.__findAllSignalFiles(acDir)
        # Read all StatechartSignals.h to get the signal numbers
        for sigFile in self.__signalFiles:
            if self._verbose:
                print " .. %s" % sigFile
            try:
                input = open(sigFile, 'r')
            except IOError:
                raise self.failureException, "*** Could NOT open '%'s for read" % sigFile
            #
            # Loop until the user defined signals are found
            # Reading StatechartSignals.h
            while 1:
                data = input.readline()
                self.assertTrue(data, "*** Error in StatechartSignals.h - expected a string: 'User defined signals'")
                m = re.match(r"^#include\s+[<\"](.*?IdRange.h)[>\"]", data)
                if m is not None:
                    self.__parseIdRange(os.sep.join([acDir, m.group(1)]))
                elif string.find(data, "User defined signals") != -1:
                    break
            #
            # Map each signal to an integer
            while 1:
                data = input.readline()
                if not data:
                    break  # no more lines
                if string.find(data, "MAX_SIG") != -1:
                    break  # done with signals!
                data = string.strip(data)
                if not data: continue
                # Skip this entire line if it starts with a '/'
                if data[0] == '/': continue
                # search for the value assigned, be it after '=' or ','
                m = re.match(r"(?P<name>[\w\d_]+)\s*[,=].*?((?P<depvar>[\w\d_]+)\s*(?P<op>[+-]))?\s*(?P<hex>0x)?(?P<val>[0-9A-Fa-f]+\b)", data)
                if m is not None:
                    sigName = m.group('name')
                    if m.group('hex') is not None:  # parse as hex value
                        sigVal = int(m.group('val'), 16)
                    else:
                        sigVal = int(m.group('val'))
                    if m.group('depvar') is not None:  # 'op' exists too
                        if m.group('depvar') in self.__hashVal.keys():
                            depVar = self.__hashVal[m.group('depvar')]
                            if m.group('op') == '+':
                                sigVal = depVar + sigVal
                            elif m.group('op') == '-':
                                sigVal = depVar - sigVal
                    if self._verbose:
                        print "  > got sig %s == %d" % (sigName, sigVal)
                    self._cmdMap[sigName] = sigVal

    def _compileSM(self, smList=[]):
        acDir = os.sep.join([self._dir, self.AUTOCODE_DIR])

        if os.path.exists(os.sep.join([acDir, "Makefile"])):
            # just invoke the autogen'd make file
            compileCmd = " ".join\
                (["cd", acDir, ";",
                  "CCFLAGS='-DDEFINE_MAIN -DDEFINE_CPP_UNITTEST -m32 -c -Wall -g' make clean all",
                  "; cd -"])
        else:
            darwinOpts = ""
            if sys.platform == 'darwin':
                darwinOpts = "-Wl,-all_load "
            #
            # set up and issue compilation command
            compileCmd = " ".join\
                (["cd", acDir, ";",
                  "g++ -DDEFINE_MAIN -DDEFINE_CPP_UNITTEST -c -m32 -Wall -g",
                        "-I.",
                        "-I"+self.QF_INCLUDE,
                        "-I"+self.XMLRPC_LIB,
                    "main.cpp",
                    self.XMLRPC_LIB+"/log_event.cpp"])
            if len(smList) == 0:  # compile files based on testSM name
                compileCmd = " ".join([compileCmd, self._testSM+".cpp", self._testSM+"Impl.cpp"])
            else:
                for f in smList:
                    compileCmd = " ".join([compileCmd, f+".cpp", f+"Impl.cpp"])
            compileCmd = " ".join\
                ([compileCmd, "; g++ -m32",
                  "-o", self._testSM,
                  "*.o", "-L"+self.QF_LIB,
                  darwinOpts+"-lqf -lqep -lpthread",
                  "; cd -"])
        print compileCmd
        os.system(compileCmd)


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
        @param endApp

        @see: verifier.Verifier.doTestCase()
        """
        # configure a few optional parameters
        if dir is None:  # set dir same as testSM if not set
            dir = testSM
        # call parent doTestCase
        if not verifier.Verifier.doTestCase(self, testSM, desc,
                                   os.sep.join([self.BASEDIR, "test", dir]),
                                   smList, script, preserveImpl,
                                   useSimState, testAutocodeFailure):
            return
        #
        # Check if C test suite can proceed, checking for QF-C lib
        self.assertTrue(os.path.exists(os.sep.join([self.BASEDIR, "linux", "libqf.a"])),\
                        "*** QF_C++ library not compiled, could not proceed!\n" +\
                        "==> Please go to QF_Cpp/ and execute 'make clean all' first")
        print
        #
        if not self._rerunOnly:  # don't regen if re-running last build
            print "Autocoding " + self._testSM + " and fetching its signals..."
            # if 'smList' supplied, construct a list of StateMachine options
            opts = autocodeOpts + " -cppsignals"
            for sm in smList:
                opts += " -sm %s" % sm
            self._autocoder(ext=".mdxml", target="-cpp",
                            opts=opts, javaOpts="-DDEFINE_MAIN")
        #
        result = self.RESULT_FAIL
        if testAutocodeFailure:  # Only check if autocode failed
            file = os.sep.join([self._dir, self.AUTOCODE_DIR, self._testSM+'.cpp'])
            if not os.path.exists(file):  # not finding it is a PASS
                result = self.RESULT_PASS
        else:  # Compile and run the autocoded product
            if not self._rerunOnly:  # don't recompile if re-running last build
                print "Compiling State Machine application..."
                self._compileSM(smList)
            # This if should be executed even for re-run, to prevent bad feature interaction
            if self._buildAndStop:  # don't run test
                self._endTestCase(verifier.TestReporter.TEST_SKIP)
                return
            #
            # This needs to be done even if re-running
            print "Process StateChartSignals..."
            self._processStatechartSignals()
            #
            print
            print "Starting test: " + self.COLORS['bold'] + desc + self.COLORS['default']
            if os.path.exists(os.sep.join([self._dir, self.AUTOCODE_DIR, self._testSM])):
                self._startApp(targetApp=self._testSM)
                result = self._checkResults(expectFile=expectFile)
            elif os.path.exists(os.sep.join([self._dir, self.AUTOCODE_DIR, "linux", "active"])):
                self._startApp(targetApp="linux/active")
                result = self._checkResults(expectFile=expectFile)
            else:
                print "ERROR! No executable exists, please autocode/build first!"
                result = verifier.TestReporter.RESULT_UNKNOWN
        #
        self._endTestCase(result)
