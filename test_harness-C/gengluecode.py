#!/usr/bin/env python
# ----------------------------------------------------------------------------------------
# Filename gengluecode.py
# Purpose:
#     Generate the glue code for running a State Machine in the test harness.
#     Items that are generated:
#         - application.c - glue code that initializes and runs the state machines
#         - application.h - header file for the above
#         - Application.py - Python script for the GUI application.

#     The Python Cheetah templates are:
#         - application.c.tmpl
#         - application.h.tmpl
#         - Application.py.tmpl
#
#   Notes:
#     This program opens a configuration file called 'config.dat' that contains the
#     name of all the MagicDraw XML statecharts that are to run in the test harness.
#     The other fields contain the object names to be instantiated.
#
#   Calling syntax:
#     gengluecode.py
#
# ------------------------------------------------------------------------------------------
import sys
from Cheetah.Template import Template


# Open the configuration file
input = open('config.dat', 'r')

# Read Class name and Object name
classNameList = []
classObjectDict = {}
while 1:
  data = input.readline()
  if not data: break
  dataWords = data.split()
  ClassName = dataWords[0]
  objNameList = dataWords[1:]
  classNameList.append(ClassName) 
  classObjectDict.update({ClassName : objNameList})

# Write out to the application header file
outFile = "application.h"
appHTemplate = Template(file = outFile + ".tmpl")
appHTemplate.classNameList = classNameList
appHTemplate.classObjectDict = classObjectDict
print "Creating Application file: " + outFile
open(outFile, 'w').write(str(appHTemplate))

# Write out to the application C file
outFile = "application.c"
appCTemplate = Template(file = outFile + ".tmpl")
appCTemplate.classNameList = classNameList
appCTemplate.classObjectDict = classObjectDict
print "Creating Application file: " + outFile
open(outFile, 'w').write(str(appCTemplate))

# Write out to the Application Python file
outFile = "Application.py"
appPyTemplate = Template(file = outFile + ".tmpl")
appPyTemplate.classNameList = classNameList
appPyTemplate.classObjectDict = classObjectDict
print "Creating Application Python file: " + outFile
open(outFile, 'w').write(str(appPyTemplate))
