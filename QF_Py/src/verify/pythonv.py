'''
Refactored on Sep 21, 2010 from verify.py

@author: Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
'''
import os
import sys

from verify import verifier

class PythonVerifier(verifier.Verifier):
    """
    This unittest class uses Pexpect to perform blackbox input-output testing
    on the Python suite of Autocoder tests.  Support functions take care of
    invoking the autocoder, starting the autocoded python as an app in Pexpect
    (or via sim_state_start), and reporting nicely colored results.
    Individual testcases just run a given test XML file.
    """
    def __init__(self, methodName='runTest'):
        # Must call super class init to properly initialize unittest class
        verifier.Verifier.__init__(self, methodName)
        verifier.Verifier.AUTOCODE_DIR = "autocode"
        #
        # Python base directory
        self.BASEDIR = verifier.Verifier.PYTHON_ROOT
        self.TESTDIR = os.sep.join([self.BASEDIR, "test"])

    def setUp(self):
        self._commonSetUp()

    def tearDown(self):
        self._commonTearDown()


    def _sendEvent(self, event):
        cmdString = event
        if event[0] == '=':  # send event as-is if first char is '='
            cmdString = event[1:]
        elif self._useSimStateStart:
            cmdString = "sendEvent('%s')" % event
        print "> Sending pExpect:", cmdString
        # send to pexpect App
        self._smApp.send(cmdString + '\n')


    def doTestCase(self, testSM, desc="Nondescript TestCase!",
                   dir=None, smList=[], script=None,
                   preserveImpl=False, useSimState=False,
                   testAutocodeFailure=False,
                   expectFile=None, autocodeOpts=""):
        """
        The core of a testcase, it coordinates an individual test case,
        running either Pexpect or the supplied test script against a test
        StateMachine of the supplied name.  Following unittest convention,
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
        if script is not None:  # using script forces useSimState to be True
            useSimState = True
        # call parent doTestCase
        if not verifier.Verifier.doTestCase(self, testSM, desc,
                                   os.sep.join([self.TESTDIR, dir]),
                                   smList, script, preserveImpl,
                                   useSimState, testAutocodeFailure):
            return
        print
        #
        if not self._rerunOnly:  # don't regen if re-running last build
            print "Autocoding " + self._testSM + " and fetching its signals..."
            # if 'smList' supplied, construct a list of StateMachine options
            opts=autocodeOpts
            for sm in smList:
                opts += " -sm %s" % sm
            self._autocoder(ext=".zip", opts=opts)
        #
        result = self.RESULT_FAIL
        if testAutocodeFailure:  # Only check if autocode failed
            file = os.sep.join([self._dir, self.AUTOCODE_DIR, self._testSM+'Active.py'])
            if not os.path.exists(file):  # not finding it is a PASS
                result = self.RESULT_PASS
        else:  # Run the autocoded product
            if self._buildAndStop:  # don't run test
                self._endTestCase(verifier.TestReporter.TEST_SKIP)
                return
            print
            print "Starting test: " + self.COLORS['bold'] + desc + self.COLORS['default']
            if script is None:  # run the test via Pexpect
                self._startApp()
                result = self._checkResults(expectFile=expectFile)
            else:  # run the test via supplied test script
                print "Testing using script '%s'..." % script
                # add test dir to Python path
                sys.path.append(self._dir)
                # invoke sim_state_start with/without GUI (depending on flag)
                import sim_state_start
                sim_state_start.main([], nogui_flag=not self._enableGui,
                                     path=os.sep.join([self._dir, self.AUTOCODE_DIR]))
                #
                # find, import the test module, and run it by invoking "run()"
                (module,ext) = os.path.splitext(script)
                exec("import " + module)
                rv = locals()[module].run()
                if rv and type(rv) is type(True):  # boolean type return
                    result = self.RESULT_PASS
                else:  # store the return value
                    result = rv
                sim_state_start.StartStateSim.getInstance().destroy()
                # make sure we remove test dir from Python path, and remove module
                sys.path.remove(self._dir)
                del sys.modules[module]
                del sys.modules['sim_state_start']
        #
        self._endTestCase(result)
