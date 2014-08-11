#!/usr/bin/env python
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
'''
Created on Sep 15, 2010

This module performs unittest of Autocoder results on the Spin suite of test
XMLs, using the Pexpect module.

@author: Leonard Reder <reder@jpl.nasa.gov>
'''
from verify import spinv


class SpinSuite1(spinv.SpinVerifier):
    """
    The set of SimpleX test cases.
    The Suite naming is so that Simple cases will get run before composites.
    """
    def __init__(self, methodName='runTest'):
        # Must call super class init to properly initialize unittest class
        spinv.SpinVerifier.__init__(self, methodName)
        #
        # Add suite info for reporting
        if self.reporter is not None:
            self.reporter.addSuite(self, "Spin Simple Test Suite")


    def testSimple1(self):
        """
        Spin Simple1, Simple State-machine
        """
        self.doTestCase("Simple", "Simple1, Simple, Simple State Machine",
                        dir="Simple1")


    def testSimple2(self):
        """
        Spin Simple2, Guarded Transition
        """
        self.doTestCase("Guard", "Simple2, Guard, Guarded Transition",
                        dir="Simple2")


    def testSimple3(self):
        """
        Spin Simple3, Junction Transition
        """
        self.doTestCase("Junction", "Simple3, Junction, Junction Transition",
                        dir="Simple3")


    def testSimple4(self):
        """
        Spin Simple4, Publish/Subscribe
        """
        self.doTestCase("PubSub", "Simple4, PubSub, Publish/Subscribe Test",
                        dir="Simple4")


class SpinSuite2(spinv.SpinVerifier):
    """
    The set of CompositeX test cases.
    """
    def __init__(self, methodName='runTest'):
        # Must call super class init to properly initialize unittest class
        spinv.SpinVerifier.__init__(self, methodName)
        #
        # Add suite info for reporting
        if self.reporter is not None:
            self.reporter.addSuite(self, "Spin Composite Test Suite")


    def testComposite01(self):
        """
        Spin Composite1, Hsm, Composite state with Simple Transitions
        """
        self.doTestCase("Hsm", "Composite1, Hsm, Composite State with Simple Transitions",
                        dir="Composite1")


    def testComposite02(self):
        """
        Spin Composite2, Hsm2 
        """
        self.doTestCase("Hsm2", "Composite2, Hsm2, Deeper composite state and transitions",
                        dir="Composite2")


    def testComposite03(self):
        """
        Spin Composite3, 2 Orthogonal Regions
        """
        self.doTestCase("OrthoRegion", "Composite3, OrthoRegion, 2 Orthogonal Regions",
                        dir="Composite3")


    def testComposite04(self):
        """
        Spin Composite4, ThreeDeep
        """
        self.doTestCase("ThreeDeep", "Composite4, ThreeDeep Composite State",
                        dir="Composite4")


    def testComposite05a(self):
        """
        Spin Composite5a, Timer Events test run
        """
        self.doTestCase("Timers", "Composite5, Timer Events",
                        dir="Composite5")


    def testComposite05b(self):
        """
        Spin Composite5b, Timer Events, never see s114TimerEv/s12TimerEv/s13Entry
        """
        self.doTestCase("Timers", "Composite5b, Timer Events, never see s114TimerEv/s12TimerEv/s13Entry",
                        dir="Composite5",
                        expectFile="TimersNever-expect.txt",
                        spinSteps=0)  # run "forever" for this test


    def testComposite06(self):
        """
        Spin Composite6, Repeat actions without duplicate definition
        """
        self.doTestCase("RepeatAction", "Composite6, Repeat actions without duplicate definition",
                        dir="Composite6",
                        spinSteps=0)  # run "forever" for this test


    def testComposite07(self):
        """
        Spin Composite7, Internal transitions in 2-deep states
        """
        self.doTestCase("InternalTransFix", "Composite7, Internal transitions in 2-deep states",
                        dir="Composite7")


    def testComposite08(self):
        """
        Spin Composite8, Pseudostate super-state entry action
        """
        self.doTestCase("PseudostateEntryFix", "Composite8, Pseudostate super-state entry action",
                        dir="Composite8")


##############################################################################
# Executing this module from the command line
##############################################################################

if __name__ == "__main__":
    spinv.SpinVerifier.mainCall(globals())
