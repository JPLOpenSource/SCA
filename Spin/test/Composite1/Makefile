# --------------------------------------------------------
# Build Definitions
# --------------------------------------------------------
# Configure this to the base name of model xmi file.
XMLNAME = Test
# For Safety properties (using never claim)                                                                                                    
CCFLAGS   = -DSAFETY
# For Aliveness properties
#CCFLAGS   = -DNP
#
# Modify APPJAR to point to your autocoder.jar location.
#
APPJAR = ../../../autocoder/autocoder.jar
#
# Execution of autocoder.jar
#
AUTOCODE = java -Xmx512m -jar $(APPJAR) -promela
#
# Model XML file
#
XMLFILES = $(addsuffix .xml, $(XMLNAME))
#
#
#
PMLFILES = Test.pml Init.pml ManStubs.pml

all: autocode spin

autocode: $(XMLFILES)
	$(AUTOCODE) $(XMLFILES);
		
spin: pan.c
	gcc $(CCFLAGS) -o model pan.c 

pan.c: Main.pml
	spin -a Main.pm


clean:
	rm -f pan
	rm -f pan.*
	rm -f model
	rm -f *.trail




