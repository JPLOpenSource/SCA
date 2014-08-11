#!/tps/bin/python
# GUI application to trace the execution of state machine "Test4".
#
from Tkinter import *

if (sys.hexversion <= 0x010502f0):
    FIRST = 'first'

class Test4:
  def __init__(self, name, wparam=1168, hparam=1007):
    self.win = Tk()
    self.label = Label(self.win, text = name, font = ("Times", 16, "bold"))
    self.label.pack()
    self.can = Canvas(self.win, width=wparam, height=hparam, background='white')
    self.can.pack()

    # Map state names to fill colors:
    self.colorDict = {'Test4T4S1': '#ffffcc',
                       'Test4T4S2': '#ffffcc',
                       'Region1T4S21': '#ffffcc',
                       'Region1Initial1': '#0000ff',
                       'Region1T4S22': '#ffffcc',
                       'Region2T4S23': '#ffffcc',
                       'Region2T4S24': '#ffffcc',
                       'Region2Initial2': '#0000ff',
                       'Test4Initial3': '#0000ff',
                       }

    # State diagram elements:
    self.T4S1 = self.can.create_rectangle(441, 238, 553, 308, fill="#ffffcc", width=2, outline = "blue")
    self.stateText1 = self.can.create_text(497, 243, text="T4S1", anchor=N, font = ("Times", 12, "bold"));
    self.T4S2 = self.can.create_rectangle(329, 413, 714, 714, fill="#ffffcc", width=2, outline = "blue")
    self.stateText2 = self.can.create_text(521, 418, text="T4S2", anchor=N, font = ("Times", 12, "bold"));
    self.Region1T4S21 = self.can.create_rectangle(406, 476, 490, 518, fill=self.colorDict['Region1T4S21'], width=2, outline = "blue")
    self.stateText3 = self.can.create_text(448, 481, text="Region1T4S21", anchor=N, font = ("Times", 12, "bold"));
    self.Region1Initial1 = self.can.create_oval(348, 453, 366, 471, fill=self.colorDict['Region1Initial1'])
    self.Region1T4S22 = self.can.create_rectangle(560, 476, 644, 518, fill=self.colorDict['Region1T4S22'], width=2, outline = "blue")
    self.stateText4 = self.can.create_text(602, 481, text="Region1T4S22", anchor=N, font = ("Times", 12, "bold"));
    self.Region2T4S23 = self.can.create_rectangle(406, 616, 483, 658, fill=self.colorDict['Region2T4S23'], width=2, outline = "blue")
    self.stateText5 = self.can.create_text(444, 621, text="Region2T4S23", anchor=N, font = ("Times", 12, "bold"));
    self.Region2T4S24 = self.can.create_rectangle(560, 616, 651, 658, fill=self.colorDict['Region2T4S24'], width=2, outline = "blue")
    self.stateText6 = self.can.create_text(605, 621, text="Region2T4S24", anchor=N, font = ("Times", 12, "bold"));
    self.Region2Initial2 = self.can.create_oval(350, 581, 368, 599, fill=self.colorDict['Region2Initial2'])
    self.Initial3 = self.can.create_oval(455, 154, 473, 172, fill="#0000ff")

    # Separator elements:
    self.separator0 = self.can.create_line(335,553,708,559, width=1, fill = "blue", stipple="gray25")

    # Transition diagram elements:
    self.tran1 = self.can.create_line(452, 476, 452, 462, 366, 462, width=2, arrow=FIRST, fill = "red")
    self.tran2 = self.can.create_line(560, 487, 490, 487, width=2, arrow=FIRST, fill = "red")
    self.tran3 = self.can.create_line(490, 511, 560, 511, width=2, arrow=FIRST, fill = "red")
    self.tran4 = self.can.create_line(441, 616, 441, 590, 368, 590, width=2, arrow=FIRST, fill = "red")
    self.tran5 = self.can.create_line(560, 627, 483, 627, width=2, arrow=FIRST, fill = "red")
    self.tran6 = self.can.create_line(483, 651, 560, 651, width=2, arrow=FIRST, fill = "red")
    self.tran7 = self.can.create_line(506, 238, 506, 163, 473, 163, width=2, arrow=FIRST, fill = "red")
    self.tran8 = self.can.create_line(623, 413, 623, 273, 553, 273, width=2, arrow=FIRST, fill = "red")
    self.tran9 = self.can.create_line(441, 270, 357, 270, 357, 413, width=2, arrow=FIRST, fill = "red")

    # Text diagram elements:
    self.textBox1 = self.can.create_text(525, 470, text="Ev2", anchor=N, font = ("Times", 11));
    self.textBox2 = self.can.create_text(525, 494, text="Ev2", anchor=N, font = ("Times", 11));
    self.textBox3 = self.can.create_text(521, 610, text="Ev3", anchor=N, font = ("Times", 11));
    self.textBox4 = self.can.create_text(521, 634, text="Ev3", anchor=N, font = ("Times", 11));
    self.textBox5 = self.can.create_text(623, 291, text="Ev1", anchor=N, font = ("Times", 11));
    self.textBox6 = self.can.create_text(357, 282, text="Ev1", anchor=N, font = ("Times", 11));

    # Map state names to states:
    self.stateDict = {'Test4T4S1': self.T4S1,
                      'Test4T4S2': self.T4S2,
                      'Region1T4S21': self.Region1T4S21,
                      'Region1Initial1': self.Region1Initial1,
                      'Region1T4S22': self.Region1T4S22,
                      'Region2T4S23': self.Region2T4S23,
                      'Region2T4S24': self.Region2T4S24,
                      'Region2Initial2': self.Region2Initial2,
                      'Test4Initial3': self.Initial3,
                      }

  def ExitState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill=self.colorDict[state])
  def EnterState(self, state):
    self.can.itemconfigure(self.stateDict[state], fill="green")
