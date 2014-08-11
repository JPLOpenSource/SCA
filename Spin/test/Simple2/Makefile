# --------------------------------------------------------
# Build Definitions
# --------------------------------------------------------
# For Safety properties (using never claim)                                                                                                    
CCFLAGS   = -DSAFETY
# For Aliveness properties
#CCFLAGS   = -DNP

all: spin

spin: 
	spin -a Main.pml
	gcc $(CCFLAGS) -o model pan.c 

clean:
	rm -f pan
	rm -f pan.*
	rm -f model
	rm -f *.trail




