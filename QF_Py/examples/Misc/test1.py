 
from qf import miros
 
import sys


def printf(format, *args):
    sys.stdout.write(format % args)

#
#========================================================================
# Define event handlers for states. For clarity, the functions are named
# for the states they handle (though this is not required). For details
# see the UML state chart shown in the accompanying image
# "hsmtst-chart.gif". 
#========================================================================


class Test1(miros.Hsm):
  """
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
      self.addState ( "top",           self.top,                None)
      self.addState ( "s1",            self.s1,                self.top)
      self.addState ( "s2",            self.s2,                self.top)      
        
  def top(self):
      """
      """
      if self.tEvt['sType'] == "init":
          self.stateStart(self.s1)
          return 0
      elif self.tEvt['sType'] == "entry":
          return 0
      elif self.tEvt['sType'] == "exit":
          return 0
      return self.tEvt['sType']

                
  def s1(self):
      """
      State s1
      """
      stateName = "s1"
      if self.tEvt['sType'] == "init":
          return 0
      elif self.tEvt['sType'] == "entry":
          printf("%s %s\n", stateName, "ENTRY")
          return 0
      elif self.tEvt['sType'] == "exit":
          printf("%s %s\n", stateName, "EXIT")
          return 0
      elif self.tEvt['sType'] == "a":
          printf("%s %s\n", stateName, self.tEvt['sType'])
          self.stateTran(self.s2)
          return 0
      return self.tEvt['sType']


  def s2(self):
      """
      State s2
      """
      stateName = "s2"
      if self.tEvt['sType'] == "init":
          return 0
      elif self.tEvt['sType'] == "entry":
          printf("%s %s\n", stateName, "ENTRY")
          return 0
      elif self.tEvt['sType'] == "exit":
          printf("%s %s\n", stateName, "EXIT")
          return 0
      elif self.tEvt['sType'] == "a":
          printf("%s %s\n", stateName, self.tEvt['sType'])
          self.stateTran(self.s1)
          return 0
      return self.tEvt['sType']

    
#
# ==============================================
# create HSM instance and populate it with states
test1 = Test1()
        
# ====================================
# Non-interactive test.
     
def test_non_interactive():
    test1.onStart(test1.top)
    test1.onEvent("a")
    test1.onEvent("a")


#====================================
# Interactive tester/explorer.
     
def test_interactive():
    print("\nInteractive Hierarchical State Machine Example")
    print "Miros revision: " + test1.revision + "\n"
    print("Enter 'quit' to end.\n")
    # start/initialize HSM
    test1.onStart(test1.top)
    while True:
        # get letter of event
        sType = raw_input("\nEvent<-")
        if sType == "quit":
            return
        if len(sType) != 1 or sType < "a" or sType > "a":
            print "Event not defined.",
        else:
            # dispatch event and display results
            test1.onEvent(sType)
#====================================
     
#test_non_interactive()
test_interactive()


raw_input("Press return to continue")

