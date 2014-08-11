#!/tps/bin/python
# GUI application to trace the execution of state machine "Test1".
#
from Tkinter import *

if (sys.hexversion <= 0x010502f0):
    FIRST = 'first'

class Test1:
  def __init__(self, name, wparam=1168, hparam=1007):
    self.win = Tk()
    self.label = Label(self.win, text = name, font = ("Times", 16, "bold"))
    self.label.pack()
    self.can = Canvas(self.win, width=wparam, height=hparam, background='white')
    self.can.pack()

    # Map state names to fill colors:
    self.colorDict = {'Test1S1': '#ffffcc',
                       'Test1S2': '#ffffcc',
                       'Test1Initial1': '#0000ff',
                       }

    # State diagram elements:
    self.S1 = self.can.create_rectangle(399, 259, 504, 315, fill="#ffffcc", width=2, outline = "blue")
    self.stateText1 = self.can.create_text(451, 264, text="S1", anchor=N, font = ("Times", 12, "bold"));
    self.S2 = self.can.create_rectangle(406, 392, 518, 462, fill="#ffffcc", width=2, outline = "blue")
    self.stateText2 = self.can.create_text(462, 397, text="S2", anchor=N, font = ("Times", 12, "bold"));
    self.Initial1 = self.can.create_oval(341, 180, 359, 198, fill="#0000ff")

    # Separator elements:

    # Transition diagram elements:
    self.tran1 = self.can.create_line(487, 392, 487, 315, width=2, arrow=FIRST, fill = "red")
    self.tran2 = self.can.create_line(441, 259, 441, 189, 359, 189, width=2, arrow=FIRST, fill = "red")
    self.tran3 = self.can.create_line(438, 315, 438, 392, width=2, arrow=FIRST, fill = "red")

    # Text diagram elements:
    self.textBox1 = self.can.create_text(487, 339, text="Ev1", anchor=N, font = ("Times", 11));
    self.textBox2 = self.can.create_text(438, 339, text="Ev1", anchor=N, font = ("Times", 11));

    # Map state names to states:
    self.stateDict = {'Test1S1': self.S1,
                      'Test1S2': self.S2,
                      'Test1Initial1': self.Initial1,
                      }

  def ExitState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill=self.colorDict[state])
  def EnterState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill="green")
