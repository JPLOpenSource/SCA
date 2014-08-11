#!/tps/bin/python
# GUI application to trace the execution of state machine "Test8".
#
from Tkinter import *

if (sys.hexversion <= 0x010502f0):
    FIRST = 'first'

class Test8:
  def __init__(self, name, wparam=1168, hparam=1007):
    self.win = Tk()
    self.label = Label(self.win, text = name, font = ("Times", 16, "bold"))
    self.label.pack()
    self.can = Canvas(self.win, width=wparam, height=hparam, background='white')
    self.can.pack()

    # Map state names to fill colors:
    self.colorDict = {'Test8T8S1': '#ffffcc',
                       'Test8T8S2': '#ffffcc',
                       'Test8T8S21': '#ffffcc',
                       'Test8T8S22': '#ffffcc',
                       'Test8Initial1': '#0000ff',
                       'Test8Initial2': '#0000ff',
                       }

    # State diagram elements:
    self.T8S1 = self.can.create_rectangle(287, 196, 427, 280, fill="#ffffcc", width=2, outline = "blue")
    self.stateText1 = self.can.create_text(357, 201, text="T8S1", anchor=N, font = ("Times", 12, "bold"));
    self.T8S2 = self.can.create_rectangle(245, 350, 504, 546, fill="#ffffcc", width=2, outline = "blue")
    self.stateText2 = self.can.create_text(374, 355, text="T8S2", anchor=N, font = ("Times", 12, "bold"));
    self.T8S21 = self.can.create_rectangle(266, 427, 343, 469, fill="#ffffcc", width=2, outline = "blue")
    self.stateText3 = self.can.create_text(304, 432, text="T8S21", anchor=N, font = ("Times", 12, "bold"));
    self.T8S22 = self.can.create_rectangle(399, 427, 476, 469, fill="#ffffcc", width=2, outline = "blue")
    self.stateText4 = self.can.create_text(437, 432, text="T8S22", anchor=N, font = ("Times", 12, "bold"));
    self.Initial1 = self.can.create_oval(257, 390, 275, 408, fill="#0000ff")
    self.Initial2 = self.can.create_oval(224, 133, 242, 151, fill="#0000ff")

    # Separator elements:

    # Transition diagram elements:
    self.tran1 = self.can.create_line(298, 427, 298, 399, 275, 399, width=2, arrow=FIRST, fill = "red")
    self.tran2 = self.can.create_line(441, 427, 441, 403, 322, 403, 322, 427, width=2, arrow=FIRST, fill = "red")
    self.tran3 = self.can.create_line(308, 469, 308, 504, 448, 504, 448, 469, width=2, arrow=FIRST, fill = "red")
    self.tran4 = self.can.create_line(364, 196, 364, 142, 242, 142, width=2, arrow=FIRST, fill = "red")
    self.tran5 = self.can.create_line(406, 350, 406, 280, width=2, arrow=FIRST, fill = "red")
    self.tran6 = self.can.create_line(326, 280, 326, 350, width=2, arrow=FIRST, fill = "red")
    self.tran7 = self.can.create_line(427, 242, 574, 242, 574, 459, 504, 459, width=2, arrow=FIRST, fill = "red")
    self.tran8 = self.can.create_line(245, 466, 133, 466, 133, 238, 287, 238, width=2, arrow=FIRST, fill = "red")

    # Text diagram elements:
    self.textBox1 = self.can.create_text(381, 386, text="at (3)", anchor=N, font = ("Times", 11));
    self.textBox2 = self.can.create_text(378, 487, text="at (1)", anchor=N, font = ("Times", 11));
    self.textBox3 = self.can.create_text(406, 298, text="at (1)", anchor=N, font = ("Times", 11));
    self.textBox4 = self.can.create_text(326, 298, text="at (5)", anchor=N, font = ("Times", 11));
    self.textBox5 = self.can.create_text(574, 286, text="Ev1", anchor=N, font = ("Times", 11));
    self.textBox6 = self.can.create_text(133, 314, text="Ev1", anchor=N, font = ("Times", 11));

    # Map state names to states:
    self.stateDict = {'Test8T8S1': self.T8S1,
                      'Test8T8S2': self.T8S2,
                      'Test8T8S21': self.T8S21,
                      'Test8T8S22': self.T8S22,
                      'Test8Initial1': self.Initial1,
                      'Test8Initial2': self.Initial2,
                      }

  def ExitState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill=self.colorDict[state])
  def EnterState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill="green")
