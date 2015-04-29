#!/tps/bin/python
# GUI application to trace the execution of state machine "Test2".
#
from Tkinter import *

if (sys.hexversion <= 0x010502f0):
    FIRST = 'first'

class Test2:
  def __init__(self, name, wparam=1168, hparam=1007):
    self.win = Tk()
    self.label = Label(self.win, text = name, font = ("Times", 16, "bold"))
    self.label.pack()
    self.can = Canvas(self.win, width=wparam, height=hparam, background='white')
    self.can.pack()

    # Map state names to fill colors:
    self.colorDict = {'Test2T2S1': '#ffffcc',
                       'Test2T2S2': '#ffffcc',
                       'Test2T2S21': '#ffffcc',
                       'Test2T2S22': '#ffffcc',
                       'Test2Initial1': '#0000ff',
                       'Test2Initial2': '#0000ff',
                       }

    # State diagram elements:
    self.T2S1 = self.can.create_rectangle(399, 259, 504, 315, fill="#ffffcc", width=2, outline = "blue")
    self.stateText1 = self.can.create_text(451, 264, text="T2S1", anchor=N, font = ("Times", 12, "bold"));
    self.T2S2 = self.can.create_rectangle(336, 413, 623, 637, fill="#ffffcc", width=2, outline = "blue")
    self.stateText2 = self.can.create_text(479, 418, text="T2S2", anchor=N, font = ("Times", 12, "bold"));
    self.T2S21 = self.can.create_rectangle(378, 469, 444, 490, fill="#ffffcc", width=2, outline = "blue")
    self.stateText3 = self.can.create_text(411, 474, text="T2S21", anchor=N, font = ("Times", 12, "bold"));
    self.T2S22 = self.can.create_rectangle(511, 539, 577, 560, fill="#ffffcc", width=2, outline = "blue")
    self.stateText4 = self.can.create_text(544, 544, text="T2S22", anchor=N, font = ("Times", 12, "bold"));
    self.Initial1 = self.can.create_oval(364, 441, 382, 459, fill="#0000ff")
    self.Initial2 = self.can.create_oval(341, 180, 359, 198, fill="#0000ff")

    # Separator elements:

    # Transition diagram elements:
    self.tran1 = self.can.create_line(417, 469, 417, 450, 382, 450, width=2, arrow=FIRST, fill = "red")
    self.tran2 = self.can.create_line(410, 490, 410, 549, 511, 549, width=2, arrow=FIRST, fill = "red")
    self.tran3 = self.can.create_line(546, 539, 546, 479, 444, 479, width=2, arrow=FIRST, fill = "red")
    self.tran4 = self.can.create_line(487, 413, 487, 315, width=2, arrow=FIRST, fill = "red")
    self.tran5 = self.can.create_line(441, 259, 441, 189, 359, 189, width=2, arrow=FIRST, fill = "red")
    self.tran6 = self.can.create_line(431, 315, 431, 413, width=2, arrow=FIRST, fill = "red")

    # Text diagram elements:
    self.textBox1 = self.can.create_text(431, 532, text="Ev2", anchor=N, font = ("Times", 11));
    self.textBox2 = self.can.create_text(525, 462, text="Ev1", anchor=N, font = ("Times", 11));
    self.textBox3 = self.can.create_text(487, 349, text="Ev1", anchor=N, font = ("Times", 11));
    self.textBox4 = self.can.create_text(431, 348, text="Ev1", anchor=N, font = ("Times", 11));

    # Map state names to states:
    self.stateDict = {'Test2T2S1': self.T2S1,
                      'Test2T2S2': self.T2S2,
                      'Test2T2S21': self.T2S21,
                      'Test2T2S22': self.T2S22,
                      'Test2Initial1': self.Initial1,
                      'Test2Initial2': self.Initial2,
                      }

  def ExitState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill=self.colorDict[state])
  def EnterState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill="green")
