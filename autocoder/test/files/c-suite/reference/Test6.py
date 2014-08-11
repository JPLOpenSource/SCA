#!/tps/bin/python
# GUI application to trace the execution of state machine "Test6".
#
from Tkinter import *

if (sys.hexversion <= 0x010502f0):
    FIRST = 'first'

class Test6:
  def __init__(self, name, wparam=1168, hparam=1007):
    self.win = Tk()
    self.label = Label(self.win, text = name, font = ("Times", 16, "bold"))
    self.label.pack()
    self.can = Canvas(self.win, width=wparam, height=hparam, background='white')
    self.can.pack()

    # Map state names to fill colors:
    self.colorDict = {'Test6T6S1': '#ffffcc',
                       'Test6T6S2': '#ffffcc',
                       'Test6Initial1': '#0000ff',
                       }

    # State diagram elements:
    self.T6S1 = self.can.create_rectangle(336, 203, 448, 273, fill="#ffffcc", width=2, outline = "blue")
    self.stateText1 = self.can.create_text(392, 208, text="T6S1", anchor=N, font = ("Times", 12, "bold"));
    self.T6S2 = self.can.create_rectangle(343, 350, 455, 441, fill="#ffffcc", width=2, outline = "blue")
    self.stateText2 = self.can.create_text(399, 355, text="T6S2", anchor=N, font = ("Times", 12, "bold"));
    self.Initial1 = self.can.create_oval(280, 119, 298, 137, fill="#0000ff")

    # Separator elements:

    # Transition diagram elements:
    self.tran1 = self.can.create_line(396, 203, 396, 128, 298, 128, width=2, arrow=FIRST, fill = "red")
    self.tran2 = self.can.create_line(455, 410, 529, 410, 529, 242, 448, 242, width=2, arrow=FIRST, fill = "red")
    self.tran3 = self.can.create_line(336, 238, 263, 238, 263, 403, 343, 403, width=2, arrow=FIRST, fill = "red")
    self.tran4 = self.can.create_line(431, 441, 431, 481, 364, 481, 364, 441, width=2, arrow=FIRST, fill = "red")
    self.tran5 = self.can.create_line(397, 314, 208, 239, 18, 36, width=2, arrow=LAST, smooth=TRUE, fill = "red")

    # Text diagram elements:
    self.textBox1 = self.can.create_text(406, 287, text="Ev2", anchor=N, font = ("Times", 11));
    self.textBox2 = self.can.create_text(529, 284, text="Ev1", anchor=N, font = ("Times", 11));
    self.textBox3 = self.can.create_text(263, 287, text="Ev1", anchor=N, font = ("Times", 11));
    self.textBox4 = self.can.create_text(398, 464, text="Ev3", anchor=N, font = ("Times", 11));

    # Map state names to states:
    self.stateDict = {'Test6T6S1': self.T6S1,
                      'Test6T6S2': self.T6S2,
                      'Test6Initial1': self.Initial1,
                      }

  def ExitState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill=self.colorDict[state])
  def EnterState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill="green")
