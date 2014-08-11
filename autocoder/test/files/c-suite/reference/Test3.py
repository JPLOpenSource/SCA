#!/tps/bin/python
# GUI application to trace the execution of state machine "Test3".
#
from Tkinter import *

if (sys.hexversion <= 0x010502f0):
    FIRST = 'first'

class Test3:
  def __init__(self, name, wparam=1168, hparam=1007):
    self.win = Tk()
    self.label = Label(self.win, text = name, font = ("Times", 16, "bold"))
    self.label.pack()
    self.can = Canvas(self.win, width=wparam, height=hparam, background='white')
    self.can.pack()

    # Map state names to fill colors:
    self.colorDict = {'Test3T3S1': '#ffffcc',
                       'Test3T3S2': '#ffffcc',
                       'Test3T3S21': '#ffffcc',
                       'Test3T3S22': '#ffffcc',
                       'Test3Initial1': '#0000ff',
                       'Test3DeepHistory1': '#ffffcc',
                       'Test3Initial2': '#0000ff',
                       'Test3T3S3': '#ffffcc',
                       }

    # State diagram elements:
    self.T3S1 = self.can.create_rectangle(399, 259, 504, 315, fill="#ffffcc", width=2, outline = "blue")
    self.stateText1 = self.can.create_text(451, 264, text="T3S1", anchor=N, font = ("Times", 12, "bold"));
    self.T3S2 = self.can.create_rectangle(336, 413, 623, 637, fill="#ffffcc", width=2, outline = "blue")
    self.stateText2 = self.can.create_text(479, 418, text="T3S2", anchor=N, font = ("Times", 12, "bold"));
    self.T3S21 = self.can.create_rectangle(378, 469, 444, 490, fill="#ffffcc", width=2, outline = "blue")
    self.stateText3 = self.can.create_text(411, 474, text="T3S21", anchor=N, font = ("Times", 12, "bold"));
    self.T3S22 = self.can.create_rectangle(511, 539, 577, 560, fill="#ffffcc", width=2, outline = "blue")
    self.stateText4 = self.can.create_text(544, 544, text="T3S22", anchor=N, font = ("Times", 12, "bold"));
    self.Initial1 = self.can.create_oval(364, 441, 382, 459, fill="#0000ff")
    self.DeepHistory1 = self.can.create_oval(348, 530, 366, 548, fill="#ffffcc", width=2, outline = "blue")
    self.stateText5 = self.can.create_text(357, 535, text="H*", anchor=N, font = ("Times", 12, "bold"));
    self.Initial2 = self.can.create_oval(341, 180, 359, 198, fill="#0000ff")
    self.T3S3 = self.can.create_rectangle(84, 441, 210, 560, fill="#ffffcc", width=2, outline = "blue")
    self.stateText6 = self.can.create_text(147, 446, text="T3S3", anchor=N, font = ("Times", 12, "bold"));

    # Separator elements:

    # Transition diagram elements:
    self.tran1 = self.can.create_line(417, 469, 417, 450, 382, 450, width=2, arrow=FIRST, fill = "red")
    self.tran2 = self.can.create_line(410, 490, 410, 549, 511, 549, width=2, arrow=FIRST, fill = "red")
    self.tran3 = self.can.create_line(546, 539, 546, 479, 444, 479, width=2, arrow=FIRST, fill = "red")
    self.tran4 = self.can.create_line(378, 480, 357, 480, 357, 530, width=2, arrow=FIRST, fill = "red")
    self.tran5 = self.can.create_line(487, 413, 487, 315, width=2, arrow=FIRST, fill = "red")
    self.tran6 = self.can.create_line(441, 259, 441, 189, 359, 189, width=2, arrow=FIRST, fill = "red")
    self.tran7 = self.can.create_line(431, 315, 431, 413, width=2, arrow=FIRST, fill = "red")
    self.tran8 = self.can.create_line(210, 469, 336, 469, width=2, arrow=FIRST, fill = "red")
    self.tran9 = self.can.create_line(357, 548, 357, 613, 151, 613, 151, 560, width=2, arrow=FIRST, fill = "red")

    # Text diagram elements:
    self.textBox1 = self.can.create_text(431, 532, text="Ev2", anchor=N, font = ("Times", 11));
    self.textBox2 = self.can.create_text(525, 462, text="Ev1", anchor=N, font = ("Times", 11));
    self.textBox3 = self.can.create_text(487, 349, text="Ev1", anchor=N, font = ("Times", 11));
    self.textBox4 = self.can.create_text(431, 348, text="Ev1", anchor=N, font = ("Times", 11));
    self.textBox5 = self.can.create_text(273, 452, text="Ev3", anchor=N, font = ("Times", 11));
    self.textBox6 = self.can.create_text(260, 596, text="Ev3", anchor=N, font = ("Times", 11));

    # Map state names to states:
    self.stateDict = {'Test3T3S1': self.T3S1,
                      'Test3T3S2': self.T3S2,
                      'Test3T3S21': self.T3S21,
                      'Test3T3S22': self.T3S22,
                      'Test3Initial1': self.Initial1,
                      'Test3DeepHistory1': self.DeepHistory1,
                      'Test3Initial2': self.Initial2,
                      'Test3T3S3': self.T3S3,
                      }

  def ExitState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill=self.colorDict[state])
  def EnterState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill="green")
