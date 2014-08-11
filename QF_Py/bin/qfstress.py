#!/usr/bin/env python
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
'''
Created on Nov 12, 2009

This module performs unittest that stresses/verifies the QF framework.

@author: Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
'''
from verify import pythonv


class SpecialQFSuite(pythonv.PythonVerifier):
    """
    Special set of test cases to verify the Python QF framework works, and is
    unlikely to have race conditions, which may manifest themselves when
    stress tested with multiple threads for many iterations.
    """
    def __init__(self, methodName='runTest'):
        # Must call super class init to properly initialize unittest class
        #- skipping PythonVerifier to skip adding that suite to TestReporter
        pythonv.PythonVerifier.__init__(self, methodName)
        #
        # Add suite info for reporting
        if self.reporter is not None:
            self.reporter.addSuite(self, "QF Stress Test Suite")
        #
        # Python base directory
        self.BASEDIR = pythonv.PythonVerifier.PYTHON_ROOT


    def testStressTicks(self):
        """
        QF Stress Test PubSub3, continuous Tick test using a Master + 3 Slaves
        """
        self.doTestCase("PubSub3",
                        "PubSub3, continuous Tick test using a Master + 3 Slaves",
                        dir="PubSub3",
                        script="stressTest.py")

    def testRaceCondition(self):
        """
        QF Stress Test PubSub-7Agents, event handshake test using a Master + 7 Agents
        """
        self.doTestCase("PubSub_7agents",
                        "PubSub-7Agents, event handshake test using a Master + 7 Agents",
                        dir="PubSub7Agents",
                        script="testRaceCondition.py",
                        preserveImpl=True)


if __name__ == '__main__':
    pythonv.PythonVerifier.mainCall(globals())
