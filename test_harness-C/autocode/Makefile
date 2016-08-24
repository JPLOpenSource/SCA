#//////////////////////////////////////////////////////////////////////////////
#
# Description:  Makefile to build auto-generated C StateMachines.
#     See comments for configuration.
#
# Autocode command line: -c -sm Simple1 ../Simple.mdxml
#//////////////////////////////////////////////////////////////////////////////
AC_SOURCES = Simple1
IMPL_SOURCES = Simple1Impl

# Directory of auto-generated products.
AUTODIR := .
# Directory of generated trace GUI code
GUIDIR  := .
# SET environment variable QFROOT to the QF_Py root path of the QF installation
ifndef QFROOT
  QFROOT := ../../../../QF_Py
endif
ifndef QF_BASEDIR
  QF_BASEDIR := $(QFROOT)/../QF_C
endif
XMLRPC_DIR := $(QF_BASEDIR)/lib/xmlrpc

NULLSTRING :=
SPACE := $(NULLSTRING) # a hack to make reference to a space character
DEFAULT_TARGET := linux
#DEFAULT_CC := /usr/bin/g++
#CEXT := .cpp
DEFAULT_CC := /usr/bin/gcc
CEXT := .c

_LD_OPTS := -m32 -lqf -lqep -lpthread
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

# C compilation configurations
TARGET := $(DEFAULT_TARGET)

ifndef CPLUSPLUS
  CC := $(DEFAULT_CC)
else
  CC := $(CPLUSPLUS)
endif

ifndef CCFLAGS
  CCFLAGS := -DDEFINE_MAIN -m32 -g -c -Wall -std=c99
endif
ifdef DEFINE_XMLRPC
  CCFLAGS := $(CCFLAGS) -DDEFINE_XMLRPC
endif

ifndef INCLUDEDIRS
  INCLUDEDIRS := -I$(AUTODIR) \
                 -I$(XMLRPC_DIR) \
                 -I$(QF_BASEDIR)/include
endif

ifndef LINKDIRS
  LINKDIRS := -L$(QF_BASEDIR)/linux
endif

BINDIR  := $(TARGET)

SIGNALFILE   := StatechartSignals
SIGNALFILEH  := $(addsuffix .h, $(SIGNALFILE))

#VPATH =  
# You can have a lot of vpath directives
#vpath %.py $(AUTODIR)
#vpath %.pyc $(AUTODIR)
#vpath %$(CEXT) $(AUTODIR)
#vpath %.h $(AUTODIR)

AUTOSRCS = $(addsuffix $(CEXT), $(AC_SOURCES)) \
           $(addsuffix $(CEXT), $(IMPL_SOURCES))
OTHERSRCS = log_event$(CEXT) \
	        main$(CEXT)
SRCS = $(OTHERSRCS) $(AUTOSRCS)

EXECUTABLE = active

OBJS = $(SRCS:$(CEXT)=.o)
BINOBJS = $(addprefix $(BINDIR)/, $(OBJS))
AUTOHEADERS = $(addprefix $(AUTODIR)/, $(AUTOSRCS:$(CEXT)=.h))
AUTOBINOBJS = $(addprefix $(BINDIR)/, $(AUTOSRCS:$(CEXT)=.o))

all: auto bins

auto:
	@if (test -d $(BINDIR)) then :;\
    else \
    	echo "Creating directory "$(BINDIR)... ; \
        mkdir -p $(BINDIR); \
	fi

bins: $(BINDIR)/$(EXECUTABLE)

$(BINDIR)/$(EXECUTABLE): $(BINOBJS)
	$(CC) -m32 -o $@ $(BINOBJS) $(LINKDIRS) $(LD_OPTS)

$(AUTOBINOBJS): $(BINDIR)/%.o: $(AUTODIR)/%$(CEXT) $(AUTODIR)/$(SIGNALFILEH)
	if (test ! -d $(dir $@)) then mkdir -p $(dir $@); fi
	$(CC) $(CCFLAGS) $(INCLUDEDIRS) $(subst $(SPACE),\$(SPACE),$<) -o $@

$(BINDIR)/log_event.o: $(BINDIR)/%.o: $(XMLRPC_DIR)/%$(CEXT) $(XMLRPC_DIR)/%.h
	$(CC) $(CCFLAGS) $(INCLUDEDIRS) $(subst $(SPACE),\$(SPACE),$<) -o $@

$(BINDIR)/main.o: $(BINDIR)/%.o: $(AUTODIR)/%$(CEXT) $(AUTOHEADERS) $(XMLRPC_DIR)/log_event.h $(AUTODIR)/$(SIGNALFILEH)
	$(CC) $(CCFLAGS) $(INCLUDEDIRS) $(subst $(SPACE),\$(SPACE),$<) -o $@

.PHONY: clean
clean: auto-clean

auto-clean:
	rm -rf $(BINDIR) $(AUTODIR)/*.o
#	rm -f $(filter-out $(wildcard $(AUTODIR)/*Impl.h), $(wildcard $(AUTODIR)/*.h))
#	rm -f $(filter-out $(wildcard $(AUTODIR)/*Impl$(CEXT)), $(wildcard $(AUTODIR)/*$(CEXT)))
	rm -f $(GUIDIR)/*.py $(GUIDIR)/*.pyc $(AUTODIR)/velocity.*
