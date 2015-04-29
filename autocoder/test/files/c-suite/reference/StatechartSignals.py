#!/tps/bin/python
# GUI application to issue signals to a state machine.
#
from Tkinter import *

class StatechartSignals:
  def __init__(self, clientSocket):
    self.clientSocket = clientSocket
    win = Tk()
    label = Label(win, text = "Statechart Signals", font = ("Times", 16, "bold"))
    label.pack()
    can = Canvas(win, width=200, height=1000, background='white')
    can.pack()

    callback = ButtonCallback(self.doButton, "4")
    b4 = Button(can, text = "DURING", command=callback).pack()
    callback = ButtonCallback(self.doButton, "5")
    b5 = Button(can, text = "Ev1", command=callback).pack()
    callback = ButtonCallback(self.doButton, "6")
    b6 = Button(can, text = "Ev2", command=callback).pack()
    callback = ButtonCallback(self.doButton, "7")
    b7 = Button(can, text = "Ev3", command=callback).pack()

  def doButton(self,enumValue):
    enumValue = enumValue + "\n"
    self.clientSocket.send(enumValue)

class ButtonCallback:
  def __init__(self, callback, *firstArgs):
    self.__callback = callback
    self.__firstArgs = firstArgs

  def __call__(self, *args):
    return apply(self.__callback, self.__firstArgs + args)
