#!/tps/bin/python
# GUI application to trace the execution of state machine "Test7".
#
from Tkinter import *

if (sys.hexversion <= 0x010502f0):
    FIRST = 'first'

class Test7:
  def __init__(self, name, wparam=1168, hparam=1007):
    self.win = Tk()
    self.label = Label(self.win, text = name, font = ("Times", 16, "bold"))
    self.label.pack()
    self.can = Canvas(self.win, width=wparam, height=hparam, background='white')
    self.can.pack()

    # Map state names to fill colors:
    self.colorDict = {'Test7T7S1': '#ffffcc',
                       'Test7T7S2': '#ffffcc',
                       'Test7T7S21': '#ffffcc',
                       'Test7T7S22': '#ffffcc',
                       'Test7Initial1': '#0000ff',
                       'Test7Initial2': '#0000ff',
                       }

    # State diagram elements:
    self.T7S1 = self.can.create_rectangle(469, 231, 567, 322, fill="#ffffcc", width=2, outline = "blue")
    self.stateText1 = self.can.create_text(518, 236, text="T7S1", anchor=N, font = ("Times", 12, "bold"));
    self.T7S2 = self.can.create_rectangle(385, 406, 658, 574, fill="#ffffcc", width=2, outline = "blue")
    self.stateText2 = self.can.create_text(521, 411, text="T7S2", anchor=N, font = ("Times", 12, "bold"));
    self.T7S21 = self.can.create_rectangle(414, 476, 498, 525, fill="#ffffcc", width=2, outline = "blue")
    self.stateText3 = self.can.create_text(456, 481, text="T7S21", anchor=N, font = ("Times", 12, "bold"));
    self.T7S22 = self.can.create_rectangle(554, 476, 624, 525, fill="#ffffcc", width=2, outline = "blue")
    self.stateText4 = self.can.create_text(589, 481, text="T7S22", anchor=N, font = ("Times", 12, "bold"));
    self.Initial1 = self.can.create_oval(391, 439, 409, 457, fill="#0000ff")
    self.Initial2 = self.can.create_oval(399, 175, 417, 193, fill="#0000ff")

    # Separator elements:

    # Transition diagram elements:
    self.tran1 = self.can.create_line(431, 476, 431, 448, 409, 448, width=2, arrow=FIRST, fill = "red")
    self.tran2 = self.can.create_line(469, 287, 445, 287, 445, 476, width=2, arrow=FIRST, fill = "red")
    self.tran3 = self.can.create_line(515, 231, 515, 184, 417, 184, width=2, arrow=FIRST, fill = "red")
    self.tran4 = self.can.create_line(620, 406, 620, 238, 567, 238, width=2, arrow=FIRST, fill = "red")
    self.tran5 = self.can.create_line(589, 476, 589, 284, 567, 284, width=2, arrow=FIRST, fill = "red")
    self.tran6 = self.can.create_line(469, 242, 420, 242, 420, 406, width=2, arrow=FIRST, fill = "red")

    # Text diagram elements:
    self.textBox1 = self.can.create_text(445, 352, text="Ev2", anchor=N, font = ("Times", 11));
    self.textBox2 = self.can.create_text(620, 276, text="Ev1", anchor=N, font = ("Times", 11));
    self.textBox3 = self.can.create_text(589, 352, text="Ev2", anchor=N, font = ("Times", 11));
    self.textBox4 = self.can.create_text(420, 280, text="Ev1", anchor=N, font = ("Times", 11));

    # Map state names to states:
    self.stateDict = {'Test7T7S1': self.T7S1,
                      'Test7T7S2': self.T7S2,
                      'Test7T7S21': self.T7S21,
                      'Test7T7S22': self.T7S22,
                      'Test7Initial1': self.Initial1,
                      'Test7Initial2': self.Initial2,
                      }

  def ExitState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill=self.colorDict[state])
  def EnterState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill="green")
