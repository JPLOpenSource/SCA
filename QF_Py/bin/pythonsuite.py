#!/usr/bin/env python
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
'''
Created on Oct 16, 2009

This module performs unittest of Autocoder results on the Python suite of test
XMLs, using the Pexpect module, optionally combined with sim_state_start.

To run test showing GUI for any cases that use sim_state_start, precede test
invocation of this test on the command-prompt with "GUI=True"; that is,
    "GUI=True ./autocoder/verify/python-suite.py ..."

@author: Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
'''
from verify import pythonv


class PythonSuite1(pythonv.PythonVerifier):
    """
    The set of SimpleX test cases.
    The Suite naming is so that Simple cases will get run before composites.
    """
    def __init__(self, methodName='runTest'):
        # Must call super class init to properly initialize unittest class
        pythonv.PythonVerifier.__init__(self, methodName)
        #
        # Add suite info for reporting
        if self.reporter is not None:
            self.reporter.addSuite(self, "Python Simple Test Suite")


    def testSimple1(self):
        """
        Python Simple1, Simple State-machine
        """
        self.doTestCase("Simple1", "Simple1, Simple State-machine")


    def testSimple2(self):
        """
        Python Simple2, Internal Transition
        """
        self.doTestCase("Simple2", "Simple2, Internal Transition")


    def testSimple3(self):
        """
        Python Simple3, Self-transition
        """
        self.doTestCase("Simple3", "Simple3, Self-transition")


    def testSimple4(self):
        """
        Python Simple4, Transition Effect
        """
        self.doTestCase("Simple4", "Simple4, Transition Effect")


    def testSimple5(self):
        """
        Python Simple5, Transition Guards
        """
        self.doTestCase("Simple5", "Simple5, Transition Guards")


    def testSimple5b(self):
        """
        Python Simple5b, Empty outgoing transition FATAL
        """
        self.doTestCase("Simple5-bad", "Simple5b, Empty outgoing transition FATAL",
                        dir="Simple5",
                        testAutocodeFailure=True)


    def testSimple6(self):
        """
        Python Simple6, Duplicate Guards/Actions
        """
        self.doTestCase("Simple6", "Simple6, Duplicate Guards/Actions",
                        useSimState=True)


    def testSimple6b(self):
        """
        Python Simple6b, Duplicate Transition Event from Same State
        """
        self.doTestCase("Simple6b-DupEv", "Simple6b, Duplicate Transition Event from Same State",
                        dir="Simple6",
                        testAutocodeFailure=True)


    def testSimple7(self):
        """
        Python Simple7, Peer Pair with Infinite Events Back-and-Forth
        """
        self.doTestCase("Simple7", "Simple7, Peer Pair with Infinite Events Back-and-Forth",
                        dir="Simple7-PeerPair",
                        useSimState=True)


    def testSimple8(self):
        """
        Python Simple8, Do Activity
        """
        self.doTestCase("DoEx", "Simple8, Do Activity",
                        dir="Simple8",
                        preserveImpl=True,
                        useSimState=True)


    def testSimple9(self):
        """
        Python Simple9, Python-code Actions and Multi-arg Impl Functions
        """
        self.doTestCase("Simple9", "Simple9, Python-code Actions and Multi-arg Impl Functions",
                        dir="Simple9",
                        preserveImpl=True,
                        useSimState=True)


class PythonSuite2(pythonv.PythonVerifier):
    """
    The set of CompositeX test cases.
    """
    def __init__(self, methodName='runTest'):
        # Must call super class init to properly initialize unittest class
        pythonv.PythonVerifier.__init__(self, methodName)
        #
        # Add suite info for reporting
        if self.reporter is not None:
            self.reporter.addSuite(self, "Python Composite Test Suite")


    def testComposite01(self):
        """
        Python Composite1, Composite state with Simple Transitions
        """
        self.doTestCase("Composite1", "Composite1, Composite State with Simple Transitions")


    def testComposite02(self):
        """
        Python Composite2, Deep History
        """
        self.doTestCase("Composite2", "Composite2, Deep History")


    def testComposite03a(self):
        """
        Python Composite3a, 2 Orthogonal Regions
        """
        self.doTestCase("Composite3", "Composite3a, 2 Orthogonal Regions")


    def testComposite03b(self):
        """
        Python Composite3b, Orthogonal Regions, same inner+outer event
        """
        self.doTestCase("Composite3b", "Composite3b, Orthogonal Regions, same inner+outer event",
                        dir="Composite3")


    def testComposite04(self):
        """
        Python Composite4a, Cross-hierarchy Transitions
        """
        self.doTestCase("Composite4", "Composite4a, Cross-hierarchy Transitions")


    def testComposite04b(self):
        """
        Python Composite4b, Cross-hierarchy Transitions: local vs. self
        """
        self.doTestCase("Composite4b", "Composite4b, Cross-hierarchy Transitions: local vs. self",
                        dir="Composite4")


    def testComposite04c(self):
        """
        Python Composite4c, More than 1 empty outgoing transitions NOT allowed
        """
        self.doTestCase("Composite4-bad", "Composite4c, More than 1 empty outgoing transitions NOT allowed",
                        dir="Composite4",
                        testAutocodeFailure=True)


    def testComposite05a(self):
        """
        Python Composite5a, Timer Events
        """
        self.doTestCase("Composite5a", "Composite5a, Timer Events",
                        dir="Composite5")


    def testComposite05b(self):
        """
        Python Composite5b, Timer Events (both AT and AFTER)
        """
        self.doTestCase("Composite5b", "Composite5b, Timer Events (both AT and AFTER)",
                        dir="Composite5")


    def testComposite05c(self):
        """
        Python Composite5c, Timer Events in an Orthogonal Submachine
        """
        self.doTestCase("Composite5c", "Composite5c, Timer Events in an Orthogonal Submachine",
                        dir="Composite5")


    def testComposite05d(self):
        """
        Python Composite5d, Timer Events in double-Ortho + Ortho-Submachine-Ortho
        """
        self.doTestCase("Composite5d", "Composite5d, Timer Events in double-Ortho + Ortho-Submachine-Ortho",
                        dir="Composite5")


    def testComposite05e(self):
        """
        Python Composite5e, Timer Events in Submachine one-level down
        """
        self.doTestCase("Composite5e", "Composite5e, Timer Events in Submachine one-level down",
                        dir="Composite5")


    def testComposite06a(self):
        """
        Python Composite6a, Top-level Orthogonal, Cross-Dispatch, Hidden Region
        """
        self.doTestCase("Composite6_1",
                        "Composite6a, Top-level Orthogonal, Cross-Dispatch, Hidden Region",
                        dir="Composite6")


    def testComposite06b(self):
        """
        Python Composite6b, Inner Orthogonal Regions
        """
        self.doTestCase("Composite6_2",
                        "Composite6b, Inner Orthogonal Regions",
                        dir="Composite6")


    def testComposite06c(self):
        """
        Python Composite6c, Inner Orthogonal Regions & Unnamed+Hidden State
        """
        self.doTestCase("Composite6_3",
                        "Composite6c, Inner Orthogonal Regions & Unnamed+Hidden State",
                        dir="Composite6")


    def testComposite07(self):
        """
        Python Composite7, Init Action, Internal Transition in Super/Orthogonal/Leaf States
        """
        self.doTestCase("Composite7_1",
                        "Composite7, Init Action, Internal Transition in Super/Orthogonal/Leaf States",
                        dir="Composite7")


    def testComposite08a(self):
        """
        Python Composite8a, 3 Orthogonal Rs, Inner Composite, Multi-level Trans, Action List
        """
        self.doTestCase("Composite8_1",
                        "Composite8a, 3 Orthogonal Rs, Inner Composite, Multi-level Trans, Action List",
                        dir="Composite8")


    def testComposite08b(self):
        """
        Python Composite8b, Wrapped in a super-state, 3 Orthogonal Rs, Inner Composite, Multi-level Trans, Action List
        """
        self.doTestCase("Composite8_2",
                        "Composite8b, Wrapped in a super-state, 3 Orthogonal Rs, Inner Composite, Multi-level Trans, Action List",
                        dir="Composite8")


    def testComposite08c(self):
        """
        Python Composite8c, Exit from Inner Composite within Orthogonal Regions
        """
        self.doTestCase("Composite8_3",
                        "Composite8c, Exit from Inner Composite within Orthogonal Regions",
                        dir="Composite8")


    def testComposite09(self):
        """
        Python Composite9, TimerEvents in Orthogonal Regions
        """
        self.doTestCase("Composite9",
                        desc="Composite9, TimerEvents in Orthogonal Regions",
                        script="testScript.py")


    def testComposite10(self):
        """
        Python Composite10, Choice and Compound Junctions
        """
        self.doTestCase("Composite10", "Composite10, Choice and Compound Junctions",
                        useSimState=True)


    def testComposite11(self):
        """
        Python Composite11, Final State
        """
        self.doTestCase("FinalState", "Composite11, Final State",
                        dir="Composite11",
                        useSimState=True)


    def testComposite12(self):
        """
        Python Composite12, Composite State entry-/exitPoint and completion event 
        """
        self.doTestCase("Composite12", "Composite12, Composite State entry-/exitPoint and completion event",
                        useSimState=True)


    def testSubmachine1(self):
        """
        Python SubMachine1, 3 Instances of 2 Sub-StateMachines
        """
        self.doTestCase("SubMachine1", "SubMachine1, 3 Instances of 2 Sub-StateMachines",
                        dir="Submachine1")


    def testSubmachine2(self):
        """
        Python SubMachine1, Sub-StateMachine within Orthogonal Regions
        """
        self.doTestCase("SubMachine2", "SubMachine2, Sub-StateMachine within Orthogonal Regions",
                        dir="Submachine2",
                        preserveImpl=True,
                        useSimState=True)


    def testNestedOrthoSubmBad(self):
        """
        Python NestedOrthoSubmBad, NO Infinite Submachine Recursion
        """
        self.doTestCase("NestedOrthoSubm-bad",
                        "NestedOrthoSubmBad, NO Infinite Submachine Recursion",
                        dir="NestedOrthoSubm",
                        preserveImpl=True,
                        testAutocodeFailure=True)


    def testNestedOrthoSubm(self):
        """
        Python NestedOrthoSubm, Nesting Orthogonal Regions, Submachines, and History
        """
        self.doTestCase("NestedOrthoSubm",
                        "NestedOrthoSubm, Nesting Orthogonal Regions, Submachines, and History",
                        preserveImpl=True,
                        useSimState=True)


    def testUMLValidation(self):
        """
        Python UMLValidation, Model validation fatal errors/warnings
        """
        self.doTestCase("ValidationErrors", "UMLValidation, Model validation fatal errors/warnings",
                        dir="UMLValidation",
                        testAutocodeFailure=True)


class PythonSuite3(pythonv.PythonVerifier):
    """
    A set of test cases testing functionalities based on StateMachines
    """
    def __init__(self, methodName='runTest'):
        # Must call super class init to properly initialize unittest class
        pythonv.PythonVerifier.__init__(self, methodName)
        #
        # Add suite info for reporting
        if self.reporter is not None:
            self.reporter.addSuite(self, "Python Functional Test Suite")


    def testCalculator0(self):
        """
        Python Calculator0, Validation error for entry-/exitPoints
        """
        self.doTestCase("Calculator-bad", "Calculator0, Entry-exit validation error",
                        dir="Calculator",
                        preserveImpl=True,
                        testAutocodeFailure=True)


    def testCalculator1(self):
        """
        Python Calculator1, Entry-/ExitPoint behavior for SubMachine states 
        """
        self.doTestCase("Calculator", "Calculator1, Entry-/ExitPoint behavior for Composite or SubM states",
                        preserveImpl=True,
                        useSimState=True)


    def testCalculator2(self):
        """
        Python Calculator2, Calculator function using SubMachine entry-/exitPoints 
        """
        self.doTestCase("Calculator", "Calculator2, Calculator functionality using entry-/exitPoints",
                        script="testCalculations.py",
                        preserveImpl=True,
                        useSimState=True)


    def testUserEvent0(self):
        """
        Python UserEvent0, Validation error for overloaded impl functions
        """
        self.doTestCase("UserEventTest", "UserEvent0, Validation error for overloaded impl functions",
                        dir="UserEvent",
                        preserveImpl=True,
                        smList=["UserEventTest-Bad"],
                        testAutocodeFailure=True)


    def testUserEvent1(self):
        """
        Python UserEvent1, Proper user event behavior on impl functions 
        """
        self.doTestCase("UserEventTest", "UserEvent1, Proper user event behavior on impl functions",
                        dir="UserEvent",
                        preserveImpl=True,
                        useSimState=True,
                        smList=["UserEventTest"])


    def testUserEvent2(self):
        """
        Python UserEvent2, Impl function user-event is 2.1-compatible
        """
        self.doTestCase("UserEventTest", "UserEvent2, Impl function user-event is 2.1-compatible",
                        dir="UserEvent",
                        preserveImpl=True,
                        useSimState=True,
                        smList=["UserEventTest-Autocoder2.1"],
                        expectFile="UserEventTest-expect.txt")


    def testUserEvent3(self):
        """
        Python UserEvent3, User-event functionality in all Impl functions
        """
        self.doTestCase("UserEventTest", "UserEvent3, User-event functionality in all Impl functions",
                        dir="UserEvent",
                        preserveImpl=True,
                        useSimState=True,
                        smList=["UserEventTest", "ZTestUserEvents"],
                        expectFile="userevents-expect.txt")


class PythonSuiteMDFormats(pythonv.PythonVerifier):
    """
    A set of test cases testing the four MagicDraw file formats.
    """
    def __init__(self, methodName='runTest'):
        # Must call super class init to properly initialize unittest class
        pythonv.PythonVerifier.__init__(self, methodName)
        #
        # Add suite info for reporting
        if self.reporter is not None:
            self.reporter.addSuite(self, "Python MagicDraw Formats Test Suite")

    def testMDFormatMdxml(self):
        """
        MDFormat MDXML, Tests the MDXML format of MagicDraw UML Model
        """
        self.doTestCase("Simple1", "MDFormatMdxml, Tests the MDXML format of MagicDraw UML Model",
                        dir="MDFormats",
                        ext=".mdxml")

    def testMDFormatMdzip(self):
        """
        MDFormat MDZIP, Tests the MDZIP format of MagicDraw UML Model
        """
        self.doTestCase("Simple1", "MDFormatMdzip, Tests the MDZIP format of MagicDraw UML Model",
                        dir="MDFormats",
                        ext=".mdzip")

    def testMDFormatXml(self):
        """
        MDFormat XML, Tests the XML format of MagicDraw UML Model
        """
        self.doTestCase("Simple1", "MDFormatXml, Tests the XML format of MagicDraw UML Model",
                        dir="MDFormats",
                        ext=".xml")

    def testMDFormatZip(self):
        """
        MDFormat XML.ZIP, Tests the XML.ZIP format of MagicDraw UML Model
        """
        self.doTestCase("Simple1", "MDFormatZip, Tests the XML ZIP format of MagicDraw UML Model",
                        dir="MDFormats",
                        ext=".xml.zip")


class PythonSuite4(pythonv.PythonVerifier):
    """
    A set of test cases testing functionalities based on State Machine Example Designs
    """
    def __init__(self, methodName='runTest'):
        # Must call super class init to properly initialize unittest class
        pythonv.PythonVerifier.__init__(self, methodName)
        #
        # Add suite info for reporting
        if self.reporter is not None:
            self.reporter.addSuite(self, "Python Examples Test Suite")
            
    def testDriving(self):
        """
        Python Driving, A test generated from a simple Yed model and Jumbl
        """
        self.doTestCase("Driving", "Driving, minimal tests generated case",
                        dir="Driving",
                        preserveImpl=True,
                        useSimState=True,
                        smList=[],
                        expectFile="driving_test_min.txt")

    def testDrivingRandum(self):
        """
        Python Driving, A test generated from a simple Yed model and Jumbl
        """
        self.doTestCase("DrivingRandum", "Driving, randum tests generated case",
                        dir="DrivingRandum",
                        preserveImpl=True,
                        useSimState=True,
                        smList=[],
                        expectFile="driving_test_random.txt")

    def testDriving2(self):
        """
        Python Driving2, A test tobe generated from a simple Yed model and Jumbl
        """
        self.doTestCase("Driving2", "Driving2, tests demonstrating guards and actions cases",
                        dir="Driving2",
                        preserveImpl=True,
                        useSimState=True,
                        smList=[],
                        expectFile="driving2_test.txt")


    def testDriving3(self):
        """
        Python Driving3, A test tobe generated from a simple Yed model and Jumbl
        """
        self.doTestCase("Driving3", "Driving3, tests demonstrating internal events and orthogonal region cases",
                        dir="Driving3",
                        preserveImpl=True,
                        useSimState=True,
                        smList=[],
                        expectFile="driving3_test.txt")
        

##############################################################################
# Executing this module from the command line
##############################################################################

if __name__ == "__main__":
    pythonv.PythonVerifier.mainCall(globals())
