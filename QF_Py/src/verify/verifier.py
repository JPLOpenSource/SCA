'''
Created on Dec 11, 2009

@author: Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
'''
import os
import sys
import stat
import time
import re
import atexit

from unittest import TestCase, main
from optparse import OptionParser
from types import ClassType
from util import color
try:
    from util import pexpect
except ImportError:
    print "WARNING! pexpect could NOT be imported. Unit tests requiring it will FAIL!"


def setupPythonEnvPaths():
    """
    Add QF bin dir to system path so that sim_state_start works properly.
    Also set up path to the Autocoder jar.
    """
    # Assume QF source is relative to this test module.
    pyRoot = os.sep.join([sys.path[0], ".."])
    Verifier.PYTHON_ROOT = pyRoot
    #
    # Add bin/ to path to get to sim_state_start
    binPath = os.path.abspath(os.sep.join([pyRoot, "bin"]))
    if binPath not in sys.path:
        sys.path.append(binPath)
    #
    # Setup path to the Autocoder Jar
    acPath = os.path.realpath(os.sep.join([pyRoot, ".." , "autocoder"]))
    if sys.platform == 'cygwin':
        acPath = re.sub("^/cygdrive/([^/]+)/", "\\1:/", acPath)
    jarPath = os.sep.join([acPath, "autocoder.jar"])
    Verifier.AUTOCODER_HOME = acPath
    Verifier.AUTOCODER_JAR = jarPath


class TestReporter(object):
    """
    Class to handle test reporting, taking care of storing results per test,
    and reporting final summary report.  This class is meant to persist across
    unittest runs to collect overall report.
    """
    # Class static constants
    TEST_SKIP = "SKIPPED"  # skipped string
    RESULT_PASS = "PASSED"  # pass string
    RESULT_FAIL = "FAILED"  # fail string
    RESULT_UNKNOWN = "Unknown"
    #
    # Dictionary key constants
    __KEY_SUITE_DESC = '_suitedesc_'
    __KEY_TEST_LIST = '_testlist_'
    __KEY_TEST_DESC = '_desc_'
    __KEY_RESULT = '_result_'

    def __init__(self, colors):
        self.COLORS = colors
        # initialize a 3-level dictionary of
        #   { suite => { test => { desc => <desc>, result => <result> } } } 
        self.__suites = dict()
        self.__suiteList = list()  # keep an insertion-ordered list of suites

    def __suiteName(self, suite):
        return type(suite).__name__

    def addSuite(self, suite, desc):
        """
        Adds a Suite to map, which will be specially handled when reporting.
        """
        suiteName = self.__suiteName(suite)
        if suiteName not in self.__suites.keys():  # don't duplicate adding suite
            self.__suiteList.append(suiteName)
            self.__suites[suiteName] = dict()
            # store suite description and ordered list of tests
            self.__suites[suiteName][TestReporter.__KEY_SUITE_DESC] = desc
            self.__suites[suiteName][TestReporter.__KEY_TEST_LIST] = list()

    def addTest(self, suite, test, desc):
        """
        Adds the test and its description to the test-stat mapping.
        """
        suiteName = self.__suiteName(suite)
        if suiteName not in self.__suites.keys():
            print "TestReporter Warning: addTest() done before addSuite(), suite has no description!"
            self.addSuite(suiteName, "Nondescript Test Suite!")
        #
        self.__suites[suiteName][TestReporter.__KEY_TEST_LIST].append(test)
        self.__suites[suiteName][test] = dict()
        self.__suites[suiteName][test][TestReporter.__KEY_TEST_DESC] = desc
        self.__suites[suiteName][test][TestReporter.__KEY_RESULT] = TestReporter.RESULT_UNKNOWN

    def addResult(self, suite, test, result):
        """
        Adds test result to the test-stat result map.
        """
        suiteName = self.__suiteName(suite)
        if suiteName not in self.__suites.keys()\
                or test not in self.__suites[suiteName]:
            self.addTest(suiteName, test, "Nondescript TestCase!")
        self.__suites[suiteName][test][TestReporter.__KEY_RESULT] = result

    def getResult(self, suite, test):
        """
        Returns result for test in suite.
        """
        rv = TestReporter.RESULT_UNKNOWN
        suiteName = self.__suiteName(suite)
        if suiteName in self.__suites.keys() and test in self.__suites[suiteName]:
            rv = self.__suites[suiteName][test][TestReporter.__KEY_RESULT]
        return rv

    def printReport(self):
        """
        Prints a summary report of all the tests and their results.
        """
        print self.COLORS['bold'] + "\nFinal Test Results:\n" + self.COLORS['default']
        notFirstLine = False
        for suite in self.__suiteList:
            # print heading for test suite
            if notFirstLine:
                print
            else:  # no blank line the first time
                notFirstLine = True
            # print TestSuite description, if any
            suiteName = "Nondescript Test Suite!"
            if TestReporter.__KEY_SUITE_DESC in self.__suites[suite]:
                suiteName = self.__suites[suite][TestReporter.__KEY_SUITE_DESC]
            hdrLen = min(8, (78-len(suiteName))/2)  # max length of ===..
            print "%s %s %s"\
                    % ('='*hdrLen, suiteName, '='*hdrLen) 
            # iterate through the known tests in this suite
            for test in self.__suites[suite][TestReporter.__KEY_TEST_LIST]:
                if TestReporter.__KEY_TEST_DESC not in self.__suites[suite][test]:
                    continue  # not a test, skip
                # print TestCase description
                print self.__suites[suite][test][TestReporter.__KEY_TEST_DESC] + ":",
                if TestReporter.__KEY_RESULT in self.__suites[suite][test]:
                    result = self.__suites[suite][test][TestReporter.__KEY_RESULT]
                    # Either RESULT_PASS or 0 test value is a pass!
                    if result == TestReporter.RESULT_PASS or result == 0:
                        print self.COLORS['green'],
                    else:
                        print self.COLORS['red'],
                    if type(result) is type(0):
                        if result == 0:
                            print TestReporter.RESULT_PASS,
                        else:
                            print TestReporter.RESULT_FAIL,
                        print "(err: %s)" % result,
                    else:
                        print result,
                else:
                    print self.COLORS['red'],
                    print TestReporter.RESULT_UNKNOWN,
                print self.COLORS['default']
        print


## NOTE: ORDER is important! Must be defined after TestReporter class
class Verifier(TestCase):
    '''
    "Abstract" base test case class for the verification module.
    Provides common method for setUp and tearDown, running the autocoder,
    invoking Pexpect, checking results against expect files, and
    running test cases.
    '''
    # Class static constants
    AUTOCODE_DIR = "autocode"  # defines default autocode dir
    RESULT_PASS = TestReporter.RESULT_PASS  # pass string
    RESULT_FAIL = TestReporter.RESULT_FAIL  # fail string
    # Constants initialized in setupPythonEnvPaths
    AUTOCODER_HOME = None
    AUTOCODER_JAR = None
    PYTHON_ROOT = None
    # Initialize test base dir and test dir (may be used by subclass)
    BASEDIR = None
    TESTDIR = None
    #
    # Start with color disabled and no reporter
    COLORS = { 'default':'', 'bold':'' , 'green':'', 'red':'' }
    reporter = None  # reporter is used only if executed from Main
    # Static vars for command-line options set in mainCall
    __buildAndStop = False
    __cleanAndStop = False
    __inputOnly = False
    __rerunOnly = False
    __keepGen = False
    __useSimStateStart = False
    __enableGui = False
    __verbose = False
    __nocolors = False
    __execFromMain = False

    def __init__(self, methodName='runTest'):
        # Must call super class init to properly initialize unittest class
        TestCase.__init__(self, methodName)
        #
        # initialize colors and reporter properly if executed from Main
        if Verifier.__execFromMain:
            # enable color (unless nocolors) and summary report
            if not Verifier.__nocolors:
                Verifier.COLORS = color.colors
            if Verifier.reporter is None:
                # make reporter static so that it remains across runs
                Verifier.reporter = TestReporter(self.COLORS)
            #
            # copy to this instance
            self.reporter = Verifier.reporter
        #
        # Protected variables are set per run to facilitate test-harness methods.
        self._dir = None
        self._testSM = None
        self._smApp = None
        self._dontSendQuit = False  # used in rare cases to NOT send 'quit'
        self._preserveImpl = False
        self._subdirsToClean = []
        self._keepGen = False
        self._useSimStateStart = False
        self._enableGui = False
        self._verbose = False

    @staticmethod
    @atexit.register
    def terminate():
        """
        Clean-up routine invoked before system exits, primarily for reporting.
        """
        if Verifier.reporter is not None: Verifier.reporter.printReport()


    def _commonSetUp(self):
        """
        Common set-up routine used by subclass testcases.
        """
        # transfer static-member flags to instance
        self._buildAndStop = Verifier.__buildAndStop
        self._cleanAndStop = Verifier.__cleanAndStop
        self._inputOnly = Verifier.__inputOnly
        self._rerunOnly = Verifier.__rerunOnly
        self._keepGen = Verifier.__keepGen
        self._useSimStateStart = Verifier.__useSimStateStart
        self._enableGui = Verifier.__enableGui
        self._verbose = Verifier.__verbose

    def _commonTearDown(self):
        """
        Common tear-down routine used by subclass testcases.
        """
        # terminate the StateMachine app
        if self._smApp is not None:
            self._endApp()  # will unset self.__smApp
        # delete autocode subdir in the test dir
        if self.reporter is not None and\
                self.reporter.getResult(self, self._testMethodName) != self.RESULT_PASS and\
                self.reporter.getResult(self, self._testMethodName) != TestReporter.TEST_SKIP and\
                self.reporter.getResult(self, self._testMethodName) != 0:
            print "Test failed! Keeping autocode directory for diagnosis."
        else:
            if not self._keepGen and not self._buildAndStop:
                self._cleanTestCase()
        # unset local vars to prevent confusion across runs
        self._dir = None
        self._testSM = None
        self._dontSendQuit = False
        self._preserveImpl = False
        self._subdirsToClean = []
        self._keepGen = False
        self._useSimStateStart = False
        self._enableGui = False
        self._verbose = False

    def __cleanSubDir(self, dir, subPath):
        """
        Convenience function to recursively delete the supplied directory.
        """
        dirPath = os.sep.join([dir, subPath])
        for f in os.listdir(dirPath):
            fPath = os.sep.join([dirPath, f])
            # somewhat of a hack, since we're checking for all backends here
            m = re.match(r"(.+Impl|.+_impl|ManStubs|IdRange|.*id_range)[.](\w+)$", f)
            #- filter out object files and all C/profiling files
            if self._preserveImpl and m is not None and\
                    m.group(2) != "o" and m.group(2) != "d" and\
                    m.group(2) != "gcno" and m.group(2) != "gcda" and\
                    m.group(2) != "pyc":
                # don't remove Impl file
                print "..preserving '%s'" % os.sep.join([subPath, f])
            elif os.path.isdir(fPath):
                if subPath != "." or f in self._subdirsToClean:
                    # we can remove designated subdir, first the subcontents
                    self.__cleanSubDir(dir, os.sep.join([subPath, f]))
                    # then the subdir itself
                    if len(os.listdir(fPath)) == 0:
                        os.rmdir(fPath)
            else:
                os.unlink(fPath)

    def _cleanTestCase(self):
        """
        Cleans the autocoded directory, but preserving impls if requested.
        """
        acDir = os.sep.join([self._dir, self.AUTOCODE_DIR])
        if os.path.exists(acDir):
            print "Cleaning autocode directory '%s'..." % acDir
            self.__cleanSubDir(acDir, ".")
            if len(os.listdir(acDir)) == 0:  # can remove autocode directory
                os.rmdir(acDir)


    def _autocoder(self, file=None, ext=".xml", target="-python",
                   opts="", javaOpts=""):
        """
        Invoking shell, executes the autocoder to autocode the test file to Python
        """
        if file is None:
            file = self._testSM
        # create autocode dir if not exist
        acDir = os.sep.join([self._dir, self.AUTOCODE_DIR])
        if not os.path.exists(acDir):
            os.makedirs(acDir)
        #
        # set up and issue autocoding command
        if self._verbose:  # add verbose flag
            if len(opts) > 0:
                opts += " "
            opts += "-verbose"
        cmd = " ".join\
            (["cd", acDir, ";",
              "java", javaOpts,
                    "-jar", self.AUTOCODER_JAR,
                    target, opts,
                    "../"+file+ext,
              "; cd -"])
# Run... EMMA instrumented code
#        cmd = " ".join\
#            (["cd", acDir, ";",
#              "java", javaOpts,
#                    "-Demma.coverage.out.file="+self.AUTOCODER_HOME+"/coverage.ec",
#                    ":".join\
#                        (["-cp /Users/scheng/swdev/emma-2.0.5312/lib/emma.jar",
#                          self.AUTOCODER_HOME+"/bin",
#                          self.AUTOCODER_HOME+"/lib/log4j-1.2.15.jar",
#                          self.AUTOCODER_HOME+"/lib/velocity-1.6.2-dep.jar"]),
#                    "gov.nasa.jpl.statechart.Autocoder",
#                    target, opts,
#                    "../"+self._testSM+".xml",
#              "; cd -"])
        print cmd
        os.system(cmd)


    def _startApp(self, targetApp=None, cmdStr=None, timeOut=8):
        """
        Starts the Statemachine Application
        """
        if cmdStr is None:
            # define the appfile to spawn
            appFile = self._dir + os.sep + self.AUTOCODE_DIR + os.sep
            if targetApp is None:  # default to Python testing
                if self._useSimStateStart:
                    if self._enableGui:
                        guiOpt = ""
                    else:
                        guiOpt = "-n "
                    appFile = Verifier.PYTHON_ROOT + "/bin/sim_state_start.py " + guiOpt + "--notimeline -p "\
                        + self._dir + os.sep + self.AUTOCODE_DIR
                    cmdStr = "python -i " + appFile
                    timeOut = 15
                else:
                    # Spawn simply the *Active as app
                    appFile += self._testSM + 'Active.py'
                    cmdStr = "python " + appFile
            else:
                appFile += targetApp
                cmdStr = appFile
                # let's make sure appFile is executable
                os.chmod(appFile, stat.S_IRUSR | stat.S_IWUSR | stat.S_IXUSR)
        #
        print "Spawning '%s'" % cmdStr
        # N.B. the searchwindowsize allows ".*blah" to gobble up bytes _before_ blah 
        smApp = pexpect.spawn(cmdStr, timeout=timeOut, maxread=4096, searchwindowsize=256, env=os.environ)
        smApp.ignorecase = True  # ignore case in expect
        if self._verbose:  #print spawned output to stdout for debug
            smApp.logfile = sys.stdout
        time.sleep(1)
        
        self._smApp = smApp
        if smApp.isalive() == True:
            print "state-machine application started"
            return smApp
        else:
            print "*** state-machine application failed to start"
#            sys.exit()

    def _endAppExpect(self, str):
        """
        Polymorphic expect to do after sending 'quit', before closing app
        """
        pass

    def _endApp(self):
        """
        Cleanly shuts down the App
        """
        if not self._dontSendQuit:
            # don't use polymorphic _sendEvent() call
            if self._useSimStateStart:  # send a destroy event
                self._smApp.send('quit()\n')
                # to verify GUI destroy msg, or timeout in 4 secs
                self._smApp.expect([".*Destroyed QF GUI.*", pexpect.TIMEOUT, pexpect.EOF], timeout=4)
            else:
                self._smApp.send('quit\n')
            self._endAppExpect("")
        try:
            # wait longer, half a second, to terminate
            self._smApp.delayafterterminate = 0.25
            self._smApp.close()
        except pexpect.ExceptionPexpect:
            pass  # ignore; closure failure shouldn't fail the test
        except OSError:
            pass  # igore OSErrors like 'No such process'
        self._smApp = None

    def _sendEvent(self, event):
        """
        Sends event to the StateMachine app process.
        Subclass should override with custom behaviors.
        """
        print "> Sending pExpect:", event
        self._smApp.send(event + '\n')


    def _checkResults(self, expectFile=None):
        # Read the data text file for expected results
        #
        testSmExp = self._dir + os.sep
        if expectFile is None:  # default to <SM>-expect.txt
            testSmExp += self._testSM + "-expect.txt"
        else:
            testSmExp += expectFile
        #
        #- open file for read
        try:
            txtInput = open(testSmExp, 'r')
            print "Opened expect file '%s'" % testSmExp
        except IOError:
            print "*** Could not find file '%s'" % testSmExp
            self._endApp()
            sys.exit()
        #
        #- read lines from expect file and check
        while 1:
            data = txtInput.readline()
            #
            if not data:
                break
            # Strip off end CR character
            data = data[:-1]
            timeout = -1
            # Determine what to do based on first character.
            #- For ease of constructing expect files, we alter syntax slightly
            #  for the expected output, checking for them as follows:
            #  = '#' precedes a comment, so the whole line is ignored
            #    * a blank line is also ignored
            #    * there is no other support for comments
            #  = '>' must precede an input line
            #  = '<' may precede an output line
            #  = all others are treated as output line
            if len(data) == 0:  # blank line, ignore
                #print "# ignoring blank line"
                continue
            elif data[0] == '#':  # comment line, ignore
                #print "# ignoring comment: %s" % data[1:]
                continue
            elif data[0] == '=':  # remark line, print to console
                print "\n** %s" % data[1:]
                continue
            elif data[0] == '@':  # special timeout info, strip and set timeout
                m = re.match(r"^@(\d+)@(.+)$", data)
                if m is None:  # uh oh... assume this was anomalous line
                    data = data[1:]  # strip leading '@'
                else:
                    timeout = int(m.group(1))
                    print "Setting Expect timeout to %d seconds for this read." % timeout
                    data = m.group(2)
            #        
            if data[0] == '>':  # input line, send it to pExpect process
                self._sendEvent(data[1:])  # polymorphic call
                if self._inputOnly:  # wait 1/2 sec for output to get produced
                    self._smApp.expect(pexpect.TIMEOUT, timeout=0.5)
            else:  # treat as output to expect
                if self._inputOnly:  # don't expect on output
                    continue
                ###
                #
                expectTimeout = False  # marks whether to expect time-out
                # separate if's, in case we have "<!..."
                if data[0] == '<':  # strip off the optional output char
                    data = data[1:]
                #
                if data[0] == '!':  # strip off bang and mark to expect timeout
                    expString = data[1:]
                    expectTimeout = True
                else:  # finally, treat rest as expect string
                    expString = data
                #print "< expecting string: %s" % expString
                try:
                    # call pexpect, never match "assert"
                    assertString = "ASSERT|FATAL"
                    rv = self._smApp.expect([expString,assertString], timeout=timeout)
                    if expectTimeout:  # Failed, we should NOT have matched
                        print '!'+ expString+'|'+assertString + self.COLORS['red'] + " *** Fail: should NOT have matched!" + self.COLORS['default']
                        return self.RESULT_FAIL
                    elif rv > 0:  # Failed, since we matched an ASSERT!
                        print expString+'|'+assertString + self.COLORS['red'] + " *** Fail: software ASSERT matched!" + self.COLORS['default']
                        return self.RESULT_FAIL
                    else:
                        print expString + self.COLORS['green'] + " Pass" + self.COLORS['default']
                except pexpect.TIMEOUT, info:
                    if expectTimeout:  # Passed, because didn't match within timer
                        print '!' + expString + self.COLORS['green'] + " Pass" + self.COLORS['default']
                    else:
                        print expString + self.COLORS['red'] + " *** Fail:", info, self.COLORS['default']
                        return self.RESULT_FAIL
                except pexpect.EOF, info:
                    print expString + self.COLORS['red'] + " *** Fail:", info, self.COLORS['default']
                    return self.RESULT_FAIL

        return self.RESULT_PASS


    def shortDescription(self):
        """
        Return the test description for verbose reporting, taking the first
        line of the docstring that has non-zero-length text.
        """
        doc = self._testMethodDoc
        if doc:
            strs = doc.split('\n')
            for str in strs:
                str = str.strip()
                if len(str) > 0:
                    return str
        return None


    def doTestCase(self, testSM, desc="Nondescript TestCase!",
                   dir=None, smList=[], script=None,
                   preserveImpl=False, useSimState=False,
                   testAutocodeFailure=False):
        """
        The core of a testcase to coordinate the actions of an individual
        test case.  Following unittest convention, each TestCase is named
        "test<SM>", where <SM> is the name of the StateMachine to test.
        It should invoke this method with the SM name and a concise description.
        
        Subclass should _extend_ this method with specific behaviors.
        This parent class registers the test case with the reporter and
        sets up a few class members.

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
        """
        if self.reporter is not None:  # supply self as suite
            self.reporter.addTest(self, self._testMethodName, desc)
        #
        self._dir = dir
        self._testSM = testSM
        self._preserveImpl = preserveImpl
        # enabling GUI requires use of sim_state_start
        self._useSimStateStart = useSimState \
            or self._useSimStateStart or self._enableGui
        #
        if self._cleanAndStop:  # set result to skip to induce just cleaning
            self._endTestCase(TestReporter.TEST_SKIP)
            return False  # bail from testing
        #
        print
        print "-" * 80
        # clean the target autocode dir first
        if self._rerunOnly:
            print "Rerunning results of the last build..."
            print "** CAUTION! Model changes will NOT manifest in code if not re-autocoding! **"
        else:  # clean only if not re-running last build
            self._cleanTestCase()
        # proceed with testing
        return True


    def _endTestCase(self, result):
        """
        Record result and assert PASSing the test.
        """
        # store result
        if self.reporter is not None: self.reporter.addResult(self, self._testMethodName, result)
        #
        # assert that test passed
        self.assertTrue(result == self.RESULT_PASS or result == 0\
                        or result == TestReporter.TEST_SKIP)


    @classmethod
    def mainCall(cls, globalVars):
        """
        Implements the common main method that should be invoked from
        main in subclasses.
        """
        # enumerate suite of test cases by introspecting passed in globals
        if globalVars is None:
            globalVars = globals()
        suites = {}
        for k in globalVars.keys():
            o = globalVars[k]
            if (isinstance(o, (type, ClassType)) and
                    issubclass(o, Verifier) and
                    o is not Verifier):
                name = o.__name__
                # collect test cases
                cases = []
                for t in sorted(o.__dict__.keys()):
                    if t[:4] == 'test':
                        cases += [t]
                if len(cases) > 0:  # add cases to their suit
                    suites[name] = cases
        epilog = "Available tests:"
        for k in sorted(suites.keys()):
            epilog += "  %s %s" % (k, suites[k])

        usage = "usage: %prog [options] [test_1] [...] [test_N]\n" +\
                "  where test_i can be a single test (SuiteX.testY) or a whole suite (SuiteX)"
        vers  = "%prog 1.0.0"
        parser = OptionParser(usage, version=vers, epilog=epilog)
        parser.add_option("-b", "--build", dest="buildOnly",
                          help="ONLY builds but NOT run the test; implies --keep",
                          action="store_true", default=False)
        parser.add_option("-c", "--clean", dest="cleanOnly",
                          help="Cleans auto-generated files of selected tests and stops",
                          action="store_true", default=False)
        parser.add_option("-n", "--input", dest="inputOnly",
                          help="Sends input '>' lines to target app; NO output expects (useful for building expect files)",
                          action="store_true", default=False)
        parser.add_option("-r", "--rerun", dest="rerunOnly",
                          help="Reruns test without regenerating and building files; assumes -k!",
                          action="store_true", default=False)
        parser.add_option("-g", "--gui", dest="enable_gui",
                          help="Python: uses sim_state_start.py and enables GUI",
                          action="store_true", default=False)
        parser.add_option("-k", "--keep", dest="keep",
                          help="Keep auto-generated files",
                          action="store_true", default=False)
        parser.add_option("-p", "--plain", dest="nocolors",
                          help="Uses plain output, NO terminal colors",
                          action="store_true", default=False)
        parser.add_option("-s", "--sim-state", dest="use_sim_state_start",
                          help="Python: uses sim_state_start rather than run *Active.py",
                          action="store_true", default=False)
        parser.add_option("-q", "--quiet", dest="quiet",
                          help="Minimal output",
                          action="store_true", default=False)
        parser.add_option("-v", "--verbose", dest="verbose",
                          help="Verbose output",
                          action="store_true", default=False)
        (opt, args) = parser.parse_args()
        #
        # set flag indicating whether to just clean autogen'd files and stop
        Verifier.__buildAndStop = opt.buildOnly
        Verifier.__cleanAndStop = opt.cleanOnly
        Verifier.__inputOnly = opt.inputOnly
        Verifier.__rerunOnly = opt.rerunOnly
        # set flag indicating whether to keep autogen'd files
        Verifier.__keepGen = opt.keep or opt.rerunOnly
        # set state on whether to run sim_state_start and/or GUI
        Verifier.__enableGui = opt.enable_gui
        Verifier.__useSimStateStart = opt.use_sim_state_start
        # set flag indicating whether to be verbose
        Verifier.__verbose = opt.verbose
        Verifier.__nocolors = opt.nocolors
        #
        # reconstruct argv for unittest.main()
        argv = [sys.argv[0]]
        if opt.verbose:
            argv += ['-v']
        if opt.quiet:
            argv += ['-q']
        argv += args
        #print "reconstructed argv:", argv

        # set up system Python paths for spawned processes to work
        setupPythonEnvPaths()

        Verifier.__execFromMain = True

        # Run the unit test!
        main(argv=argv)
