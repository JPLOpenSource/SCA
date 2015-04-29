'''
Created on Sep 15, 2010

@author: Leonard Reder <reder@jpl.nasa.gov>
'''
import os
from verify import verifier

class SpinVerifier(verifier.Verifier):
    """
    This unittest class uses Pexpect to perform blackbox input-output testing
    on the Spin suite of Autocoder tests.  Support functions take care of
    invoking the autocoder, starting the autocoded promela as an app in Pexpect
    and reporting nicely colored results.
    Individual testcases just run a given test XML file.
    """
    def __init__(self, methodName='runTest'):
        # Must call super class init to properly initialize unittest class
        verifier.Verifier.__init__(self, methodName)
        verifier.Verifier.AUTOCODE_DIR = "autocode"
        #
        # Spin base directory
        self.BASEDIR = os.path.abspath(os.sep.join([verifier.Verifier.PYTHON_ROOT, "..", "Spin"]))
        self.TESTDIR = os.sep.join([self.BASEDIR, "test"])

    def setUp(self):
        self._commonSetUp();

    def tearDown(self):
        self._commonTearDown();


    def _sendEvent(self, event):
        cmdString = event
        if event[0] == '=':  # send event as-is if first char is '='
            cmdString = event[1:]
        elif self._useSimStateStart:
            cmdString = "sendEvent('%s')" % event
        print "> Sending pExpect:", cmdString
        # send to pexpect App
        self._smApp.send(cmdString + '\n')


    def _endApp(self):
        """
        Cleanly shuts down the App
        """
        try:
            self._smApp.close()
        except OSError:
            pass  # igore OSErrors like 'No such process'
        self._smApp = None


    def doTestCase(self, testSM, desc="Nondescript TestCase!",
                   dir=None, smList=[],
                   spinSteps=500,
                   preserveImpl=False,
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
        @param spinSteps: number of simulation steps to run, or forever if 0;
            default: 500 steps
        @param preserveImpl: flag indicating whether NOT to delete the Impl
            files when cleaning up after the test, which is critical for test
            cases that use a custom Impl file; default: False
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
                                                os.sep.join([self.TESTDIR, dir]),
                                                smList, None, preserveImpl,False, testAutocodeFailure):
            return
        print
        #
        if not self._rerunOnly:  # don't regen if re-running last build
            print "Autocoding " + self._testSM
            # if 'smList' supplied, construct a list of StateMachine options
            opts=autocodeOpts
            for sm in smList:
                opts += " -sm %s" % sm
            self._autocoder(ext=".zip", opts=opts, target="-promela")
        #
        result = self.RESULT_FAIL
        if testAutocodeFailure:  # Only check if autocode failed
            file = os.sep.join([self._dir, self.AUTOCODE_DIR, self._testSM+'.pml'])
            if not os.path.exists(file):  # not finding it is a PASS
                result = self.RESULT_PASS
        else:  # Run the autocoded product
            if self._buildAndStop:  # don't run test
                self._endTestCase(verifier.TestReporter.TEST_SKIP)
                return
            print
            print "Starting test: " + self.COLORS['bold'] + desc + self.COLORS['default']

            os.chdir(self._dir + os.sep + "autocode")
            if spinSteps is None or spinSteps == 0:
                cmd = "spin -n100 Main.pml"
            else:
                cmd = "spin -n100 -u500 Main.pml"

            self._startApp(cmdStr=cmd)
            result = self._checkResults(expectFile=expectFile)

        #
        self._endTestCase(result)
