
XMLNAME = Sequencer
# Name of State Machine
CLASSNAME = Sequencer

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
	rm -f $(filter-out $(wildcard $(AUTODIR)/*Impl.py),  $(wildcard $(AUTODIR)/*.py))
	rm -f $(AUTODIR)/*.pyc $(AUTODIR)/velocity.* $(AUTODIR)/*.h

