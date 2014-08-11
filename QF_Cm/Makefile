DEFAULT_TARGET = linux

CC = /usr/bin/gcc
                                                                                                    
ifndef YAM_TARGET
  TARGET = $(DEFAULT_TARGET)
else
  TARGET = $(YAM_TARGET)
endif
                                                                                                                                                                                                          
  CCFLAGS   = -c -Wall


ifndef INCLUDEDIRS
  INCLUDEDIRS = -I. 
endif

ifndef LINKDIRS
  LINKDIRS = 
endif

ifndef AR
  AR = ar
endif

ifndef ARFLAGS
  ARFLAGS = r
endif

ifndef CLASSPATH
  CLASSPATH = ../../../bin/xmlToQf.jar:/usr/share/java/libgcj-3.2.2.jar:/home/kclark/Apache_Xerces/resolver.jar:/home/kclark/Apache_Xerces/xercesImpl.jar:/home/kclark/Apache_Xerces/xml-apis.jar
endif

BINDIR        = ./$(TARGET)

# List all the Statechart XML files here:
XMLFILES = 

# List all the manually coded c files here:
OTHERSRCS = qa_ctor.c   qa_lifo.c   qep.c       qf_act.c  \
            qf_run.c    qhsm_ini.c  qmp_put.c   qf_port.c \
            qa_defer.c  qeq_fifo.c  qf_gc.c   \
            qf_psini.c  qfsm_dis.c  qhsm_top.c  qte_arm.c \
            qa_fifo.c   qa_sub.c    qeq_get.c   qf_log2.c \
            qf_pspub.c  qfsm_ini.c  qhsm_tra.c  qte_ctor.c\
            qa_usub.c   qeq_init.c  qf_new.c  \
            qf_pwr2.c   qhsm_dis.c  qmp_get.c   qte_darm.c\
            qa_get_.c   qa_usuba.c  qeq_lifo.c  qf_pool.c \
            qf_tick.c   qhsm_in.c   qmp_init.c  qte_rarm.c

#OTHERSRCS = qa_ctor.c


          
# Name of the library to build without the suffix here:
TARGET_LIBRARY = libqf-c.a

SRCS = $(OTHERSRCS) $(XMLFILES:.xml=.c)

OBJS = $(SRCS:.c=.o)
BINOBJS = $(addprefix $(BINDIR)/, $(OBJS))

all: links libs bins
links:
	@if (test -d $(BINDIR)) then :;\
               else echo "Creating directory "$(BINDIR)... ; \
                      mkdir $(BINDIR); \
	fi
                                                                                                     
                                                                                                     
libs: $(TARGET_LIBRARY)

bins: 
                                                                                                     
clean:
	rm -rf $(BINDIR)
	rm -rf doxygen

$(TARGET_LIBRARY) : $(BINOBJS)
	$(AR)  $(ARFLAGS)  $(BINDIR)/$(TARGET_LIBRARY) $(BINOBJS)

$(BINDIR)/%.o : %.c
	$(CC) $(CCFLAGS) $(INCLUDEDIRS) $< -o $@

