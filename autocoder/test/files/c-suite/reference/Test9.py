#!/tps/bin/python
# GUI application to trace the execution of state machine "Test9".
#
from Tkinter import *

if (sys.hexversion <= 0x010502f0):
    FIRST = 'first'

class Test9:
  def __init__(self, name, wparam=1168, hparam=1007):
    self.win = Tk()
    self.label = Label(self.win, text = name, font = ("Times", 16, "bold"))
    self.label.pack()
    self.can = Canvas(self.win, width=wparam, height=hparam, background='white')
    self.can.pack()

    # Map state names to fill colors:
    self.colorDict = {'Test9T9S1': '#ffffcc',
                       'Test9Initial1': '#0000ff',
                       'Test9T9S2': '#ffffcc',
                       }

    # State diagram elements:
    self.T9S1 = self.can.create_rectangle(343, 217, 448, 280, fill="#ffffcc", width=2, outline = "blue")
    self.stateText1 = self.can.create_text(395, 222, text="T9S1", anchor=N, font = ("Times", 12, "bold"));
    self.Initial1 = self.can.create_oval(308, 147, 326, 165, fill="#0000ff")
    self.T9S2 = self.can.create_rectangle(343, 357, 455, 434, fill="#ffffcc", width=2, outline = "blue")
    self.stateText2 = self.can.create_text(399, 362, text="T9S2", anchor=N, font = ("Times", 12, "bold"));

    # Separator elements:

    # Transition diagram elements:
    self.tran1 = self.can.create_line(396, 217, 396, 156, 326, 156, width=2, arrow=FIRST, fill = "red")
    self.tran2 = self.can.create_line(427, 357, 427, 280, width=2, arrow=FIRST, fill = "red")
    self.tran3 = self.can.create_line(368, 280, 368, 357, width=2, arrow=FIRST, fill = "red")

    # Text diagram elements:
    self.textBox1 = self.can.create_text(437, 301, text="Ev1 / Ev2", anchor=N, font = ("Times", 11));
    self.textBox2 = self.can.create_text(368, 301, text="Ev1", anchor=N, font = ("Times", 11));

    # Map state names to states:
    self.stateDict = {'Test9T9S1': self.T9S1,
                      'Test9Initial1': self.Initial1,
                      'Test9T9S2': self.T9S2,
                      }

  def ExitState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill=self.colorDict[state])
  def EnterState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill="green")
