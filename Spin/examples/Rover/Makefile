#######################################################
#
# Description:  Makefile to generate python
# using autocoder.jar.  See comments for configuration.
#
#######################################################
# Configure this to the base name of model xmi file.
#XMLNAME = Pump
XMLNAME = Rover
# 
# Edit CLASSNAME to the state-machines needed.
# This will minimize signals.
CLASSNAME = Rover Arm
CLASSNAME_SM := $(foreach SM, $(CLASSNAME), -sm $(SM))

PLATFORM := $(shell uname)
#
# Modify APPJAR to point to your autocoder.jar location, relative to ./autocoder directory.
#
APPJAR = ../../../../autocoder/autocoder.jar
#
# Directory to put all auto-generated producs into.
AUTODIR  = ./autocode

AUTOCODE = java -jar $(APPJAR) -promela $(CLASSNAME_SM)

SIGNALFILE = StatechartSignals.py
SIGNALFILEPY = $(addsuffix .py, $(SIGNALFILE))
SIGNALFILEH = $(addsuffix .h, $(SIGNALFILE))
APPFILES = Application.py

VPATH =  
# You can have a lot of vpath directives
vpath %.py $(AUTODIR) 
vpath %.pyc $(AUTODIR)

XMLFILES = $(addsuffix .xml, $(XMLNAME))
BACKXMLFILES = $(addprefix ../, $(XMLFILES))


all: auto xmlfiles

auto:
	@if (test -d $(AUTODIR)) then :;\
               else echo "Creating directory "$(AUTODIR)... ; \
                      mkdir $(AUTODIR); \
	fi

xmlfiles: $(XMLFILES)
	cd $(AUTODIR); \
	$(AUTOCODE) $(BACKXMLFILES); \
	cd ..
	
clean: auto-clean
                                                                                                    
auto-clean:
	rm -rf autocode

