#!/tps/bin/python
# GUI application to trace the execution of state machine "Test5".
#
from Tkinter import *

if (sys.hexversion <= 0x010502f0):
    FIRST = 'first'

class Test5:
  def __init__(self, name, wparam=1168, hparam=1007):
    self.win = Tk()
    self.label = Label(self.win, text = name, font = ("Times", 16, "bold"))
    self.label.pack()
    self.can = Canvas(self.win, width=wparam, height=hparam, background='white')
    self.can.pack()

    # Map state names to fill colors:
    self.colorDict = {'Test5T5S1': '#ffffcc',
                       'Test5Initial1': '#0000ff',
                       'Test5T5S2': '#ffffcc',
                       }

    # State diagram elements:
    self.T5S1 = self.can.create_rectangle(420, 217, 539, 301, fill="#ffffcc", width=2, outline = "blue")
    self.stateText1 = self.can.create_text(479, 222, text="T5S1", anchor=N, font = ("Times", 12, "bold"));
    self.Initial1 = self.can.create_oval(369, 173, 387, 191, fill="#0000ff")
    self.T5S2 = self.can.create_rectangle(392, 378, 588, 504, fill="#ffffcc", width=2, outline = "blue")
    self.stateText2 = self.can.create_text(490, 383, text="T5S2", anchor=N, font = ("Times", 12, "bold"));

    # Separator elements:

    # Transition diagram elements:
    self.tran1 = self.can.create_line(480, 217, 480, 182, 387, 182, width=2, arrow=FIRST, fill = "red")
    self.tran2 = self.can.create_line(515, 378, 515, 301, width=2, arrow=FIRST, fill = "red")
    self.tran3 = self.can.create_line(448, 301, 448, 378, width=2, arrow=FIRST, fill = "red")

    # Text diagram elements:
    self.textBox1 = self.can.create_text(515, 323, text="Ev1", anchor=N, font = ("Times", 11));
    self.textBox2 = self.can.create_text(448, 322, text="Ev1", anchor=N, font = ("Times", 11));

    # Map state names to states:
    self.stateDict = {'Test5T5S1': self.T5S1,
                      'Test5Initial1': self.Initial1,
                      'Test5T5S2': self.T5S2,
                      }

  def ExitState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill=self.colorDict[state])
  def EnterState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill="green")
