###############################################################################
#
# Description:  Makefile to generate and build C++ StateMachines
#     using autocoder.jar.  See comments for configuration.
#
###############################################################################
# Configure this to the base name of model xmi file.
XMLNAME = Simple5
# 
# Edit CLASSNAME to the state-machines needed; this will minimize signals.
CLASSNAME = Simple5

CLASSNAME_SM := $(foreach SM, $(CLASSNAME), -sm $(SM))

QF_BASEDIR := ../../../QF_Cpp
XMLRPC_DIR := $(QF_BASEDIR)/lib/xmlrpc

DEFAULT_TARGET := linux
DEFAULT_CC := /usr/bin/g++
CEXT := .cpp
#DEFAULT_CC := /usr/bin/gcc
#CEXT := .c

_LD_OPTS := -lqf -lqep -lpthread
ifdef DEFINE_XMLRPC
  _LD_OPTS := $(_LD_OPTS) `xmlrpc-c-config client --ldadd`
endif
# Determine if platform-specific compile flags necessary
PLATFORM := $(shell uname)
ifeq ($(PLATFORM),Darwin)
  LD_OPTS := -Wl,-all_load $(_LD_OPTS)
else
  LD_OPTS := $(_LD_OPTS)
endif

# C++ compilation configurations
TARGET := $(DEFAULT_TARGET)

ifndef CPLUSPLUS
  CC := $(DEFAULT_CC)
else
  CC := $(CPLUSPLUS)
endif

ifndef CCFLAGS
  CCFLAGS := -DDEFINE_MAIN -g -c -Wall
endif
ifdef DEFINE_XMLRPC
  CCFLAGS := $(CCFLAGS) -DDEFINE_XMLRPC
endif

ifndef INCLUDEDIRS
  INCLUDEDIRS := -I. \
                 -I./autocode \
                 -I$(XMLRPC_DIR) \
                 -I$(QF_BASEDIR)/include
endif

ifndef LINKDIRS
  LINKDIRS := -L$(QF_BASEDIR)/linux
endif

# Modify APPJAR to point to your autocoder.jar location.
APPJAR := ../../../../autocoder/autocoder.jar

# Directory to put all auto-generated producs into.
AUTODIR := ./autocode
BINDIR  := $(AUTODIR)/$(TARGET)

AUTOCODE := java -DDEFINE_MAIN -Xmx256m -jar $(APPJAR) -cpp $(CLASSNAME_SM)

SIGNALFILE   := StatechartSignals
SIGNALFILEPY := $(addsuffix .py, $(SIGNALFILE))
SIGNALFILEH  := $(addsuffix .h, $(SIGNALFILE))

VPATH =  
# You can have a lot of vpath directives
vpath %.py $(AUTODIR) 
vpath %.pyc $(AUTODIR)
vpath %$(CEXT) $(AUTODIR)
vpath %.h $(AUTODIR)

XMLFILES = $(addsuffix .mdxml, $(XMLNAME))
BACKXMLFILES = $(addprefix ../, $(XMLFILES))

PYFILES = $(addsuffix .py, $(CLASSNAME))
IMPLFILES = $(addsuffix Impl$(CEXT), $(CLASSNAME))
AUTOSRCS = $(addsuffix $(CEXT), $(CLASSNAME)) \
           $(IMPLFILES)
OTHERSRCS = log_event$(CEXT) \
	        main$(CEXT)
SRCS = $(OTHERSRCS) $(AUTOSRCS)

EXECUTABLE = active

TEMPOBJS = $(notdir $(SRCS))
OBJS = $(TEMPOBJS:$(CEXT)=.o)
BINOBJS = $(addprefix $(BINDIR)/, $(OBJS))
AUTOHEADERS = $(addprefix $(AUTODIR)/, $(AUTOSRCS:$(CEXT)=.h))
AUTOBINOBJS = $(addprefix $(BINDIR)/, $(AUTOSRCS:$(CEXT)=.o))

all: auto libs bins

auto:
	@if (test -d $(BINDIR)) then :;\
    else \
    	echo "Creating directory "$(BINDIR)... ; \
        mkdir -p $(BINDIR); \
	fi

libs: xmlfiles
bins: $(BINDIR)/$(EXECUTABLE)

$(BINDIR)/$(EXECUTABLE): $(BINOBJS)
	$(CC) -o $@ $(BINOBJS) $(LINKDIRS) $(LD_OPTS)

$(AUTOBINOBJS): $(BINDIR)/%.o: $(AUTODIR)/%$(CEXT) $(AUTODIR)/$(SIGNALFILEH)
	$(CC) $(CCFLAGS) $(INCLUDEDIRS) $< -o $@

$(BINDIR)/log_event.o: $(BINDIR)/%.o: $(XMLRPC_DIR)/%$(CEXT) $(XMLRPC_DIR)/%.h
	$(CC) $(CCFLAGS) $(INCLUDEDIRS) $< -o $@

$(BINDIR)/main.o: $(BINDIR)/%.o: $(AUTODIR)/%$(CEXT) $(AUTOHEADERS) $(XMLRPC_DIR)/log_event.h $(AUTODIR)/$(SIGNALFILEH)
	$(CC) $(CCFLAGS) $(INCLUDEDIRS) $< -o $@

xmlfiles: $(XMLFILES)
	cd $(AUTODIR); \
	$(AUTOCODE) $(BACKXMLFILES); \
	cd ..

.PHONY: clean
clean: auto-clean

auto-clean:
	rm -rf $(BINDIR)
	rm -f $(filter-out $(wildcard $(AUTODIR)/*Impl.h), $(wildcard $(AUTODIR)/*.h))
	rm -f $(filter-out $(wildcard $(AUTODIR)/*Impl$(CEXT)), $(wildcard $(AUTODIR)/*$(CEXT)))
	rm -f $(AUTODIR)/*.py $(AUTODIR)/*.pyc $(AUTODIR)/velocity.* $(PYFILES) $(AUTODIR)/$(SIGNALFILEPY) gui.py
