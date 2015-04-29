#######################################################
#
# Description:  Makefile to generate python
# using autocoder.jar.  See comments for configuration.
#
#######################################################
# Configure this to the base name of model xmi file.
XMLNAME = EWTrafficLight NSTrafficLight
# The proposed complete list of state-machines with no spaces (23)
# CLASSNAME = RS422_FC_CTC AM EPS FC_AGTU FCRM Flight_System_Safety FS GbE GNC_Guidance GNC_Control GNC_Navigation GNC_Steering J2XEngine FS1553 US1553 MissionManager Rate_Gyro_Assembly ReCS RF RINU RoCS US_MPS US_TVC
# 
# Edit CLASSNAME to the state-machines needed.
# This will minimize signals.
#CLASSNAME = EWTrafficLight.xml	NSTrafficLight
#CLASSNAME_SM := $(foreach SM, $(CLASSNAME), -sm $(SM))

PLATFORM := $(shell uname)
#
# Modify APPJAR to point to your autocoder.jar location.
#
APPJAR = ../../../../autocoder/autocoder.jar
#
# Directory to put all auto-generated producs into.
AUTODIR  = ./autocode

AUTOCODE = java -Xmx512m -jar $(APPJAR) -python

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
	rm -f $(filter-out $(wildcard $(AUTODIR)/*Impl.py),  $(wildcard $(AUTODIR)/*.py))
	rm -f $(AUTODIR)/*.pyc $(AUTODIR)/velocity.* $(AUTODIR)/*.h gui.py


