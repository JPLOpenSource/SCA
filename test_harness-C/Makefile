DEFAULT_TARGET = linux
DEFAULT_CC = /usr/bin/gcc

# This is the name of the MagicDraw model - assumed to have the suffix .mdxml
CLASSNAME = Simple

# This is the state-machine name
SMNAME = Simple1

PLATFORM := $(shell uname)
ifeq ($(PLATFORM),Darwin)
  LD_OPTS = -Wl,-all_load
else
  LD_OPTS = 
endif

ifndef YAM_TARGET
  TARGET = $(DEFAULT_TARGET)
else
  TARGET = $(YAM_TARGET)
endif

ifndef CPLUSPLUS
  CC = $(DEFAULT_CC)
else
  CC = $(CPLUSPLUS)
endif

ifndef CCFLAGS
  CCFLAGS   = -m32 -std=c99 -c -Wall -g
endif

ifndef YAM_ROOT
   YAM_ROOT = ../..
endif

ifndef INCLUDEDIRS
INCLUDEDIRS = -I. \
              -I./autocode \
              -I../QF_C/include
endif

ifndef LINKDIRS
  LINKDIRS = -L../QF_C/linux
endif

ifndef APPJAR
  APPJAR = ../../autocoder/autocoder.jar
endif

BINDIR        = ./$(TARGET)

AUTOCODE = java -DDEFINE_MAIN -jar $(APPJAR) -c -sm $(SMNAME)

VPATH =

# You can have a lot of vpath directives
vpath %.c autocode
vpath %.h autocode

IMPLFILES = $(addsuffix Impl.c, $(SMNAME))

AUTOSRCS = $(addsuffix .c, $(SMNAME))


OTHERSRCS = log_event.c \
	    application.c \
	    main.c \
        $(IMPLFILES)

SRCS = $(OTHERSRCS) $(AUTOSRCS)

EXECUTABLE = active

AUTOGENERATED = $(addprefix autocode/, $(AUTOSRCS))

TEMPOBJS = $(notdir $(SRCS))
OBJS = $(TEMPOBJS:.c=.o)
BINOBJS = $(addprefix $(BINDIR)/, $(OBJS))

all: links auto libs bins

links:
	@if (test -d $(BINDIR)) then :;\
               else echo "Creating directory "$(BINDIR)... ; \
                      mkdir $(BINDIR); \
	fi
	
libs: $(BINOBJS)

bins: $(BINDIR)/$(EXECUTABLE)

autoclean:
	rm -f autocode/*
	
clean:
	rm -rf $(BINDIR)
	mkdir linux

test:

auto: $(AUTOGENERATED)

$(BINDIR)/$(EXECUTABLE) : $(BINOBJS)
	$(CC) -m32 -o $(BINDIR)/$(EXECUTABLE) $(BINOBJS) $(LINKDIRS) $(LD_OPTS) -lqf -lqep

$(BINDIR)/%.o : %.c
	$(CC) $(CCFLAGS) $(INCLUDEDIRS) $< -o $@

autocode/%.c : $(CLASSNAME).mdxml
		 cd autocode; \
                 $(AUTOCODE) ../$<; \
                 echo "Done Autocoding"; \
                 cd ..

