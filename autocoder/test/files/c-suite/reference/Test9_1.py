#!/tps/bin/python
# GUI application to trace the execution of state machine "Test9_1".
#
from Tkinter import *

if (sys.hexversion <= 0x010502f0):
    FIRST = 'first'

class Test9_1:
  def __init__(self, name, wparam=1168, hparam=1007):
    self.win = Tk()
    self.label = Label(self.win, text = name, font = ("Times", 16, "bold"))
    self.label.pack()
    self.can = Canvas(self.win, width=wparam, height=hparam, background='white')
    self.can.pack()

    # Map state names to fill colors:
    self.colorDict = {'Test9_1T9_1S1': '#ffffcc',
                       'Test9_1T9_1S2': '#ffffcc',
                       'Test9_1Initial1': '#0000ff',
                       }

    # State diagram elements:
    self.T9_1S1 = self.can.create_rectangle(350, 196, 455, 259, fill="#ffffcc", width=2, outline = "blue")
    self.stateText1 = self.can.create_text(402, 201, text="T9_1S1", anchor=N, font = ("Times", 12, "bold"));
    self.T9_1S2 = self.can.create_rectangle(357, 336, 455, 399, fill="#ffffcc", width=2, outline = "blue")
    self.stateText2 = self.can.create_text(406, 341, text="T9_1S2", anchor=N, font = ("Times", 12, "bold"));
    self.Initial1 = self.can.create_oval(252, 154, 270, 172, fill="#0000ff")

    # Separator elements:

    # Transition diagram elements:
    self.tran1 = self.can.create_line(441, 336, 441, 259, width=2, arrow=FIRST, fill = "red")
    self.tran2 = self.can.create_line(382, 259, 382, 336, width=2, arrow=FIRST, fill = "red")
    self.tran3 = self.can.create_line(417, 196, 417, 163, 270, 163, width=2, arrow=FIRST, fill = "red")

    # Text diagram elements:
    self.textBox1 = self.can.create_text(441, 280, text="Ev2", anchor=N, font = ("Times", 11));
    self.textBox2 = self.can.create_text(382, 280, text="Ev2", anchor=N, font = ("Times", 11));

    # Map state names to states:
    self.stateDict = {'Test9_1T9_1S1': self.T9_1S1,
                      'Test9_1T9_1S2': self.T9_1S2,
                      'Test9_1Initial1': self.Initial1,
                      }

  def ExitState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill=self.colorDict[state])
  def EnterState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill="green")
