#!/usr/bin/env python
#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
'''
Created on Dec 11, 2009

Runs ALL unit tests.

To execute this make sure you are in <Root dir>/QF-Py/test and
execute:

./verify/all.py

at the command line.

@author: Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
'''

from verify import verifier

# import all the suites to run
# from cmsuite import CSuite
from csuite import CSuite1, CSuite2, CSuite3
from cppsuite import CppSuite1, CppSuite2, CppSuite3
from pythonsuite import PythonSuite1, PythonSuite2, PythonSuite3
from spinsuite import SpinSuite1, SpinSuite2
#from qfstress import SpecialQFSuite

if __name__ == '__main__':
    verifier.Verifier.mainCall(globals())
