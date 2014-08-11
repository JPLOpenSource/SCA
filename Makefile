#
# Makefile 
#
# DESCRIPTION
# Top level Makefile for building the Java Statechart Autocoder,
# the Quantum Framework library and the example models
#

 
modules = autocoder QF-C

all: 
	for X in $(modules); do \
		cd $$X; make all; cd ..; done
                
clean:
	for X in $(modules); do \
		cd $$X; make clean; cd ..; done

