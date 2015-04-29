#!/usr/bin/env python
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
'''
Created on Feb 25, 2010.

This module performs unittest of Autocoder results on the C++ suite of test
XMLs, using the pexpect module.

@author: Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
'''
from verify import cppv


class CppSuite1(cppv.CppVerifier):
    """
    The entire set of C test cases.
    """
    def __init__(self, methodName='runTest'):
        # Must call super class init to properly initialize unittest class
        cppv.CppVerifier.__init__(self, methodName)
        #
        # Add suite info for reporting
        if self.reporter is not None:
            self.reporter.addSuite(self, "C++ Simple Test Suite")


    def testSimple1(self):
        """
        C++ Simple1, Simple State-machine
        """
        self.doTestCase("Simple1", "Simple1, Simple State-machine")


    def testSimple2(self):
        """
        C++ Simple2, Internal Transition
        """
        self.doTestCase("Simple2", "Simple2, Internal Transition")


    def testSimple3(self):
        """
        C++ Simple3, Self-transition
        """
        self.doTestCase("Simple3", "Simple3, Self-transition")


    def testSimple4(self):
        """
        C++ Simple4, Transition Effect
        """
        self.doTestCase("Simple4", "Simple4, Transition Effect")


    def testSimple5(self):
        """
        C++ Simple5, Transition Guards
        """
        self.doTestCase("Simple5", "Simple5, Transition Guards")


    def testSimple5b(self):
        """
        C++ Simple5b, Empty outgoing transition FATAL
        """
        self.doTestCase("Simple5-bad", "Simple5b, Empty outgoing transition FATAL",
                        dir="Simple5",
                        testAutocodeFailure=True)


    def testSimple6(self):
        """
        C++ Simple6, Duplicate Guards/Actions
        """
        self.doTestCase("Simple6", "Simple6, Duplicate Guards/Actions")


    def testSimple6b(self):
        """
        C++ Simple6b, Duplicate Transition Event from Same State
        """
        self.doTestCase("Simple6b-DupEv", "Simple6b, Duplicate Transition Event from Same State",
                        dir="Simple6",
                        testAutocodeFailure=True)


class CppSuite2(cppv.CppVerifier):
    """
    The set of CompositeX test cases.
    """
    def __init__(self, methodName='runTest'):
        # Must call super class init to properly initialize unittest class
        cppv.CppVerifier.__init__(self, methodName)
        #
        # Add suite info for reporting
        if self.reporter is not None:
            self.reporter.addSuite(self, "C++ Composite Test Suite")


    def testComposite01(self):
        """
        C++ Composite1, Composite state with Simple Transitions
        """
        self.doTestCase("Composite1", "Composite1, Composite State with Simple Transitions")


    def testComposite02(self):
        """
        C++ Composite2, Deep History
        """
        self.doTestCase("Composite2", "Composite2, Deep History")


    def testComposite03a(self):
        """
        C++ Composite3a, 2 Orthogonal Regions
        """
        self.doTestCase("Composite3", "Composite3a, 2 Orthogonal Regions")


    def testComposite03b(self):
        """
        C++ Composite3b, Orthogonal Regions, same inner+outer event
        """
        self.doTestCase("Composite3b", "Composite3b, Orthogonal Regions, same inner+outer event",
                        dir="Composite3")


    def testComposite04(self):
        """
        C++ Composite4, Cross-hierarchy Transitions
        """
        self.doTestCase("Composite4", "Composite4, Cross-hierarchy Transitions")


    def testComposite04b(self):
        """
        C++ Composite4b, Cross-hierarchy Transitions: local vs. self
        """
        self.doTestCase("Composite4b", "Composite4b, Cross-hierarchy Transitions: local vs. self",
                        dir="Composite4")


    def testComposite04c(self):
        """
        C++ Composite4c, More than 1 empty outgoing transitions NOT allowed
        """
        self.doTestCase("Composite4-bad", "Composite4c, More than 1 empty outgoing transitions NOT allowed",
                        dir="Composite4",
                        testAutocodeFailure=True)


    def testComposite05a(self):
        """
        C++ Composite5a, Timer Events
        """
        self.doTestCase("Composite5a", "Composite5a, Timer Events",
                        dir="Composite5")


    def testComposite05b(self):
        """
        C++ Composite5b, Timer Events (both AT and AFTER)
        """
        self.doTestCase("Composite5b", "Composite5b, Timer Events (both AT and AFTER)",
                        dir="Composite5")


    def testComposite05c(self):
        """
        C++ Composite5c, Timer Events in an Orthogonal Submachine
        """
        self.doTestCase("Composite5c", "Composite5c, Timer Events in an Orthogonal Submachine",
                        dir="Composite5",
                        smList=['Composite5c', 'SubM'])


    def testComposite05d(self):
        """
        C++ Composite5d, Timer Events in double-Ortho + Ortho-Submachine-Ortho
        """
        self.doTestCase("Composite5d", "Composite5d, Timer Events in double-Ortho + Ortho-Submachine-Ortho",
                        dir="Composite5",
                        smList=['Composite5d', 'SubM'])


    def testComposite05e(self):
        """
        C++ Composite5e, Timer Events in Submachine one-level down
        """
        self.doTestCase("Composite5e", "Composite5e, Timer Events in Submachine one-level down",
                        dir="Composite5",
                        smList=['Composite5e', 'SubM'])


    def testComposite06a(self):
        """
        C++ Composite6a, Top-level Orthogonal, Cross-Dispatch, Hidden Region
        """
        self._dontSendQuit = True  # this prevents hanging in Cygwin
        self.doTestCase("Composite6_1",
                        "Composite6a, Top-level Orthogonal, Cross-Dispatch, Hidden Region",
                        dir="Composite6")


    def testComposite06b(self):
        """
        C++ Composite6b, Inner Orthogonal Regions
        """
        self.doTestCase("Composite6_2",
                        "Composite6b, Inner Orthogonal Regions",
                        dir="Composite6")


    def testComposite06c(self):
        """
        C++ Composite6c, Inner Orthogonal Regions & Unnamed+Hidden State
        """
        self.doTestCase("Composite6_3",
                        "Composite6c, Inner Orthogonal Regions & Unnamed+Hidden State",
                        dir="Composite6")


    def testComposite07(self):
        """
        C++ Composite7, Init Action, Internal Transition in Super/Orthogonal/Leaf States
        """
        self.doTestCase("Composite7_1",
                        "Composite7, Init Action, Internal Transition in Super/Orthogonal/Leaf States",
                        dir="Composite7")


    def testComposite08a(self):
        """
        C++ Composite8a, 3 Orthogonal Rs, Inner Composite, Multi-level Trans, Action List
        """
        self.doTestCase("Composite8_1",
                        "Composite8a, 3 Orthogonal Rs, Inner Composite, Multi-level Trans, Action List",
                        dir="Composite8")


    def testComposite08b(self):
        """
        C++ Composite8b, Wrapped in a super-state, 3 Orthogonal Rs, Inner Composite, Multi-level Trans, Action List
        """
        self.doTestCase("Composite8_2",
                        "Composite8b, Wrapped in a super-state, 3 Orthogonal Rs, Inner Composite, Multi-level Trans, Action List",
                        dir="Composite8")


    def testComposite08c(self):
        """
        C++ Composite8c, Exit from Inner Composite within Orthogonal Regions
        """
        self.doTestCase("Composite8_3",
                        "Composite8c, Exit from Inner Composite within Orthogonal Regions",
                        dir="Composite8")


    def testComposite09(self):
        """
        C++ Composite9, TimerEvents in Orthogonal Regions
        """
        self.doTestCase("Composite9",
                        "Composite9, TimerEvents in Orthogonal Regions",
                        smList=['Composite9', 'Agent1', 'Agent2'])


    def testComposite10(self):
        """
        C++ Composite10, Choice and Compound Junctions
        """
        self.doTestCase("Composite10", "Composite10, Choice and Compound Junctions")


    def testComposite11(self):
        """
        C++ Composite11, Final State
        """
        self.doTestCase("FinalState", "Composite11, Final State",
                        dir="Composite11")


    def testComposite12(self):
        """
        C++ Composite12, Composite State entry-/exitPoint and completion event 
        """
        self.doTestCase("Composite12", "Composite12, Composite State entry-/exitPoint and completion event")


    def testSubmachine1(self):
        """
        C++ SubMachine1, 3 Instances of 2 Sub-StateMachines
        """
        self.doTestCase("SubMachine1",
                        "SubMachine1, 3 Instances of 2 Sub-StateMachines",
                        dir="Submachine1",
                        smList=['SubMachine1', 'SubState1', 'SubState2']);


    def testSubmachine2(self):
        """
        C++ SubMachine1, Sub-StateMachine within Orthogonal Regions
        """
        self.doTestCase("SubMachine2",
                        "SubMachine2, Sub-StateMachine within Orthogonal Regions",
                        dir="Submachine2",
                        preserveImpl=True,
                        smList=['SubMachine2', 'SubM']);


class CppSuite3(cppv.CppVerifier):
    """
    A set of test cases testing functionalities based on StateMachines
    """
    def __init__(self, methodName='runTest'):
        # Must call super class init to properly initialize unittest class
        cppv.CppVerifier.__init__(self, methodName)
        #
        # Add suite info for reporting
        if self.reporter is not None:
            self.reporter.addSuite(self, "C++ Functional Test Suite")


    def testCalculator0(self):
        """
        C++ Calculator0, Validation error for entry-/exitPoints
        """
        self.doTestCase("Calculator-bad", "Calculator0, Entry-exit validation error",
                        dir="Calculator",
                        preserveImpl=True,
                        testAutocodeFailure=True)


    def testCalculator1(self):
        """
        C++ Calculator1, Entry-/ExitPoint behavior for SubMachine states
        """
        self.doTestCase("Calculator", "Calculator1, Entry-/ExitPoint behavior for Composite or SubM states",
                        preserveImpl=True,
                        smList=['Keyboard', 'Calculator', 'OperandX'])


    def testCalculator2(self):
        """
        C++ Calculator2, Calculator function using SubMachine entry-/exitPoints
        """
        self.doTestCase("Calculator", "Calculator2, Calculator functionality using entry-/exitPoints",
                        preserveImpl=True,
                        smList=['Keyboard', 'Calculator', 'OperandX', 'TestCalculations'],
                        expectFile="calculations-expect.txt")


    def testUserEvent0(self):
        """
        C++ UserEvent0, Validation error for overloaded impl functions
        """
        self.doTestCase("UserEventTest", "UserEvent0, Validation error for overloaded impl functions",
                        dir="UserEvent",
                        preserveImpl=True,
                        smList=["UserEventTest-Bad"],
                        testAutocodeFailure=True)


    def testUserEvent1(self):
        """
        C++ UserEvent1, Proper user event behavior on impl functions 
        """
        self.doTestCase("UserEventTest", "UserEvent1, Proper user event behavior on impl functions",
                        dir="UserEvent",
                        preserveImpl=True,
                        smList=["UserEventTest"])


# This test unnecessary in C++
#    def testUserEvent2(self):
#        """
#        C++ UserEvent2, Impl function user-event is 2.1-compatible
#        """

    def testUserEvent3(self):
        """
        C++ UserEvent3, User-event functionality in all Impl functions
        """
        self.doTestCase("UserEventTest", "UserEvent3, User-event functionality in all Impl functions",
                        dir="UserEvent",
                        preserveImpl=True,
                        smList=["UserEventTest", "ZTestUserEvents"],
                        expectFile="userevents-expect.txt")


    def testCalculatorNS1(self):
        """
        C++ CalculatorNS1, same Calculator functions as Calc2, namespaced but NS disabled! 
        """
        self.doTestCase("NsCalculator", "CalculatorNS1, same Calculator functions as Calc2, namespaced but NS disabled!",
                        dir="NsCalculatorD",
                        preserveImpl=True,
                        expectFile="calculations-expect.txt")


    def testCalculatorNS2(self):
        """
        C++ CalculatorNS2, same Calculator functions as Calc2, but namespaced, sig global! 
        """
        self._subdirsToClean += ["gui", "My", "Test"]
        self.doTestCase("NsCalculator", "CalculatorNS2, same Calculator functions as Calc2, but namespaced, sig global!",
                        dir="NsCalculatorG",
                        preserveImpl=True,
                        smList=['UI::Keyboard', 'Calculator', 'My::.*::OperandX', 'Test::TestCalculations'],
                        expectFile="calculations-expect.txt",
                        autocodeOpts="-cppqfns '' -cppsig global -guidir gui")


    def testCalculatorNS3(self):
        """
        C++ CalculatorNS3, same Calculator functions as Calc2, but namespaced, sig local! 
        """
        self._subdirsToClean += ["gui", "My", "Test"]
        self.doTestCase("NsCalculator", "CalculatorNS3, same Calculator functions as Calc2, but namespaced, sig local!",
                        preserveImpl=True,
                        expectFile="calculations-expect.txt",
                        autocodeOpts="-cppqfns '' -cppsig local -guidir gui")


##############################################################################
# Executing this module from the command line
##############################################################################

if __name__ == "__main__":
    cppv.CppVerifier.mainCall(globals())
