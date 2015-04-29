
XMLNAME = Simple1
# The proposed complete list with no spaces (23)
# CLASSNAME = RS422_FC_CTC AM EPS FC_AGTU FCRM Flight_System_Safety FS GbE GNC_Guidance GNC_Control GNC_Navigation GNC_Steering J2XEngine FS1553 US1553 MissionManager Rate_Gyro_Assembly ReCS RF RINU RoCS US_MPS US_TVC
# Those you're currently working on
CLASSNAME = RS422_FC_CTC AM EPS FC_AGTU FCRM Flight_System_Safety FS GbE GNC_Guidance GNC_Control GNC_Navigation GNC_Steering J2XEngine FS1553 US1553 MissionManager Rate_Gyro_Assembly ReCS RF RINU RoCS US_MPS US_TVC
CLASSNAME = Simple1 Simple2

CLASSNAME_SM := $(foreach SM, $(CLASSNAME), -sm $(SM))

PLATFORM := $(shell uname)

APPJAR = ../../../../autocoder/autocoder.jar

AUTODIR  = ./autocode

AUTOCODE = java -Xmx512m -jar $(APPJAR) -python $(CLASSNAME_SM)

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
	rm -f $(AUTODIR)/*.py* $(AUTODIR)/velocity.* $(AUTODIR)/*.h

