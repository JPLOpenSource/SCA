# $Id$
# ==================================================================
"""
Hsmtst - Interactive example using the Hierarchical State Machine (HSM)
class defined in the "miros" Python module  (i.e. one that implements
behavioral inheritance). It implements the UML state chart shown in the
accompanying image "hsmtst-chart.gif". 
    
It is based on the excellent work of Miro Samek (hence the module name
"miros"). This implementation closely follows an older C/C++
implementation published in the 8/2000 edition of "Embedded Systems"
magazine by Miro Samek and Paul Montgomery under the title "State
Oriented Programming". The article and code can be found here:
http://www.embedded.com/2000/0008.

A wealth of more current information can be found at Miro's well kept
site: http://www.state-machine.com/.
 
As far as I know this is the first implementation of Samek's work in
Python. It was tested with Python 2.5
 
It is licensed under the same terms as Python itself.

-----------------------------------------------------------
Change history.
Date    Comments
-----------------------------------------------------------
2/15/09    Tom Schmit, Began porting from Lua version.
2/22/09    TS, test_non_interactive() and test_interactive() run as expected.
7/6/09     LJR, update for use with revised Hsm class in miros that uses inheritance only.
"""
# Dependent modules
from qf import miros
import sys


def printf(format, *args):
    sys.stdout.write(format % args)

class HsmImpl(miros.Hsm):
    """
    Define event handlers for states. For clarity, the functions are named
    for the states they handle (though this is not required). For details
    see the UML state chart shown in the accompanying image "hsmtst-chart.gif".
    """
    def __init__(self):
        """
        Constructor
        """
        miros.Hsm.__init__(self)
        # --------------------------------------------------------------------
        #               name                                   parent's
        #                of              event                 event
        #               state            handler               handler
        # --------------------------------------------------------------------
        self.addState ( "top",           self.top,             None)
        self.addState ( "d1",            self.d1,              self.top)
        self.addState ( "d11",           self.d11,             self.d1)
        self.addState ( "d2",            self.d2,              self.top)
        self.addState ( "d21",           self.d21,             self.d2)
        self.addState ( "d211",          self.d211,              self.d21)


    def top(self):
        """
        Top/root state is state d.
        """
        if self.tEvt['sType'] == "init":
            # display event
            printf("top-%s;", self.tEvt['sType'])
            # transition to state d2.
            self.stateStart(self.d2)
            # returning a 0 indicates event was handled
            return 0
        elif self.tEvt['sType'] == "entry":
            # display event, do nothing
            # else except indicate it was handled
            printf("top-%s;", self.tEvt['sType'])
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("top-%s;", self.tEvt['sType'])
            self.tEvt['nFoo'] = 0
            return 0
        elif self.tEvt['sType'] == "e":
            printf("top-%s;", self.tEvt['sType'])
            self.stateTran(self.d11)
            return 0
        return self.tEvt['sType']

              
    def d1(self):
        """
        State d1
        """
        if self.tEvt['sType'] == "init":
            printf("d1-%s;", self.tEvt['sType'])
            self.stateStart(self.d11)
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("d1-%s;", self.tEvt['sType'])
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("d1-%s;", self.tEvt['sType'])
            return 0
        elif self.tEvt['sType'] == "a":
            printf("d1-%s;", self.tEvt['sType'])
            self.stateTran(self.d1)
            return 0
        elif self.tEvt['sType'] == "b":
            printf("d1-%s;", self.tEvt['sType'])
            self.stateTran(self.d11)
            return 0
        elif self.tEvt['sType'] == "c":
            printf("d1-%s;", self.tEvt['sType'])
            self.stateTran(self.d2)
            return 0
        elif self.tEvt['sType'] == "f":
            printf("d1-%s;", self.tEvt['sType'])
            self.stateTran(self.d211)
            return 0
        return self.tEvt['sType']


    def d11(self):
        """
        State d11
        """
        if self.tEvt['sType'] == "entry":
            printf("d11-%s;", self.tEvt['sType'])
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("d11-%s;", self.tEvt['sType'])
            return 0
        elif self.tEvt['sType'] == "d":
            printf("d11-%s;", self.tEvt['sType'])
            self.stateTran(self.d1)
            self.tEvt['nFoo'] = 0
            return 0
        elif self.tEvt['sType'] == "g":
            printf("d11-%s;", self.tEvt['sType'])
            self.stateTran(self.d211)
            return 0
        elif self.tEvt['sType'] == "h":
            printf("d11-%s;", self.tEvt['sType'])
            self.stateTran(self.top)
            return 0
        return self.tEvt['sType']


    def d2(self):
        """
        State d2
        """
        if self.tEvt['sType'] == "init":
            printf("d2-%s;", self.tEvt['sType'])
            self.stateStart(self.d211)
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("d2-%s;", self.tEvt['sType'])
            if self.tEvt['nFoo'] != 0:
                self.tEvt['nFoo'] = 0
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("d2-%s;", self.tEvt['sType'])
            return 0
        elif self.tEvt['sType'] == "c":
            printf("d2-%s;", self.tEvt['sType'])
            self.stateTran(self.d1)
            return 0
        elif self.tEvt['sType'] == "f":
            printf("d2-%s;", self.tEvt['sType'])
            self.stateTran(self.d11)
            return 0
        return self.tEvt['sType']     


    def d21(self):
        """
        State d21
        """
        if self.tEvt['sType'] == "init":
            printf("d21-%s;", self.tEvt['sType'])
            self.stateStart(self.d211)
            return 0
        elif self.tEvt['sType'] == "entry":
            printf("d21-%s;", self.tEvt['sType'])
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("d21-%s;", self.tEvt['sType'])
            return 0
        elif self.tEvt['sType'] == "a":
            printf("d21-%s;", self.tEvt['sType'])
            self.stateTran(self.d21)
            return 0
        elif self.tEvt['sType'] == "b":
            printf("d21-%s;", self.tEvt['sType'])
            self.stateTran(self.d211)
            return 0
        elif self.tEvt['sType'] == "g":
            printf("d21-%s;", self.tEvt['sType'])
            self.stateTran(self.d1)
            return 0
        return self.tEvt['sType']


    def d211(self):
        """
        State d211
        """
        if self.tEvt['sType'] == "entry":
            printf("d211-%s;", self.tEvt['sType'])
            return 0
        elif self.tEvt['sType'] == "exit":
            printf("d211-%s;", self.tEvt['sType'])
            return 0
        elif self.tEvt['sType'] == "d":
            printf("d211-%s;", self.tEvt['sType'])
            self.stateTran(self.d21)
            return 0
        elif self.tEvt['sType'] == "h":
            printf("d211-%s;", self.tEvt['sType'])
            self.stateTran(self.top)
            return 0
        return self.tEvt['sType']
#
# ==============================================
# create HSM instance and populate it with states
hsm = HsmImpl()

import pprint
# pprint.pprint( hsm.hsm )

# ====================================
# Non-interactive test.
     
def test_non_interactive():
    # hsm.dump()
    print("\nNon-interactive Hierarchical State Machine Example")
    print "Miros revision: " + hsm.revision + "\n"
    print("The following pairs of lines should all match each other and")
    print("the accompanying UML state chart 'hsmtst-chart.gif'.\n")
    # start/initialize HSM
    hsm.onStart(hsm.top)
    print("\ntop-entry;top-init;d2-entry;d2-init;d21-entry;d211-entry;\n")
    hsm.onEvent("a")
    print("\nd21-a;d211-exit;d21-exit;d21-entry;d21-init;d211-entry;\n")
    hsm.onEvent("b")
    print("\nd21-b;d211-exit;d211-entry;\n")
    hsm.onEvent("c")
    print("\nd2-c;d211-exit;d21-exit;d2-exit;d1-entry;d1-init;d11-entry;\n")
    hsm.onEvent("d")
    print("\nd11-d;d11-exit;d1-init;d11-entry;\n")
    hsm.onEvent("e")
    print("\ntop-e;d11-exit;d1-exit;d1-entry;d11-entry;\n")
    hsm.onEvent("f")
    print("\nd1-f;d11-exit;d1-exit;d2-entry;d21-entry;d211-entry;\n")
    hsm.onEvent("g")
    print("\nd21-g;d211-exit;d21-exit;d2-exit;d1-entry;d1-init;d11-entry;\n")
    hsm.onEvent("h")
    print("\nd11-h;d11-exit;d1-exit;top-init;d2-entry;d2-init;d21-entry;d211-entry;\n")
    hsm.onEvent("g")
    print("\nd21-g;d211-exit;d21-exit;d2-exit;d1-entry;d1-init;d11-entry;\n")
    hsm.onEvent("f")
    print("\nd1-f;d11-exit;d1-exit;d2-entry;d21-entry;d211-entry;\n")
    hsm.onEvent("e")
    print("\ntop-e;d211-exit;d21-exit;d2-exit;d1-entry;d11-entry;\n")
    hsm.onEvent("d")
    print("\nd11-d;d11-exit;d1-init;d11-entry;\n")
    hsm.onEvent("c")
    print("\nd1-c;d11-exit;d1-exit;d2-entry;d2-init;d21-entry;d211-entry;\n")
    hsm.onEvent("b")
    print("\nd21-b;d211-exit;d211-entry;\n")
    hsm.onEvent("a")
    print("\nd21-a;d211-exit;d21-exit;d21-entry;d21-init;d211-entry;\n")

#====================================
# Interactive tester/explorer.
     
def test_interactive():
    # hsm.dump()
    print("\nInteractive Hierarchical State Machine Example")
    print "Miros revision: " + hsm.revision + "\n"
    print("Enter 'quit' to end.\n")
    # start/initialize HSM
    hsm.onStart(hsm.top)
    while True:
        # get letter of event
        sType = raw_input("\nEvent<-")
        if sType == "quit":
            return
        if len(sType) != 1 or sType < "a" or sType > "h":
            print "Event not defined.",
        else:
            # dispatch event and display results
            hsm.onEvent(sType)
#====================================
     
#test_non_interactive()
test_interactive()


raw_input("Press return to continue")

