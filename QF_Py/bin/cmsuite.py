#!/usr/bin/env python
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
'''
Created on Dec 11, 2009

This module performs unittest of Autocoder results on the C (old) suite of test
XMLs, using the Pexpect module.

@author: Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
'''
from verify import cmv


class CSuite(cmv.CVerifier):
    """
    The entire set of C (old) test cases.
    """
    def __init__(self, methodName='runTest'):
        # Must call super class init to properly initialize unittest class
        cmv.CVerifier.__init__(self, methodName)
        #
        # Add suite info for reporting
        if self.reporter is not None:
            self.reporter.addSuite(self, "C (old) Test Suite")


    def testExample1(self):
        """
        C (old) Test1, Simple State-machine
        """
        self.doTestCase("Test1", "Simple State-machine")


    def testExample2(self):
        """
        C (old) Test2, Hierarchy
        """
        self.doTestCase("Test2", "Hierarchy")


    def testExample3(self):
        """
        C (old) Test3, History States
        """
        self.doTestCase("Test3", "History States")


    def testExample4(self):
        """
        C (old) Test4, Orthogonal Region
        """
        self.doTestCase("Test4", "Orthogonal Region")


    def testExample5(self):
        """
        C (old) Test5, Internal Events
        """
        self.doTestCase("Test5", "Internal Events")


    def testExample6(self):
        """
        C (old) Test6, Self-loop Transitions
        """
        self.doTestCase("Test6", "Self-loop Transitions")


    def testExample7(self):
        """
        C (old) Test7, Inter-level Transitions
        """
        self.doTestCase("Test7", "Inter-level Transitions")


    def testExample8(self):
        """
        C (old) Test8, Timers
        """
        self.doTestCase("Test8", "Timers")


    def testExample9(self):
        """
        C (old) Test9, Multiple State-machines
        """
        self.doTestCase("Test9", "Multiple State-machines",
                        smList=['Test9', 'Test9_1'])


##############################################################################
# Executing this module from the command line
##############################################################################

if __name__ == "__main__":
    cmv.CVerifier.mainCall(globals())
