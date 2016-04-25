#!/tps/bin/python
# GUI application to issue signals to a state machine.
#
from Tkinter import *

class StatechartSignals:
    """
    """
    def __init__(self, clientSocket):
        self.clientSocket = clientSocket
        win = Tk()
        # Grid sizing behavior for this window
        win.grid_rowconfigure(1, weight=1)
        win.grid_columnconfigure(0, weight=1)
        label = Label(win, text = "Statechart Signals", font = ("Times", 16, "bold"))
        label.grid(row=0, column=0, sticky='ew')
        # Canvas
        cnv = Canvas(win)
        cnv.grid(row=1, column=0, sticky='nswe')
        # vertical scrollbar for the canvas
        vScroll = Scrollbar(win, orient=VERTICAL, command=cnv.yview)
        vScroll.grid(row=1, column=1, sticky='ns')
        cnv.configure(yscrollcommand=vScroll.set)
        # Frame in canvas
        frm = Frame(cnv)
        # This puts the frame in the canvas's scrollable zone
        cnv.create_window(0, 0, window=frm, anchor='nw')

        callback = ButtonCallback(self.doButton, "4")
        b4 = Button(frm, text = "Tick", command=callback)
        b4.pack()
        callback = ButtonCallback(self.doButton, "5")
        b5 = Button(frm, text = "Ev1", command=callback)
        b5.pack()

        # Update display to get correct dimensions
        frm.update_idletasks()
        # Configure size of frmvas's scrollable zone
        cnv.configure(scrollregion=(0, 0, frm.winfo_width(), frm.winfo_height()))


    def doButton(self,enumValue):
        """
        """
        enumValue = enumValue + "\n"
        self.clientSocket.send(enumValue)


class ButtonCallback:
    """
    """
    def __init__(self, callback, *firstArgs):
        """
        """
        self.__callback = callback
        self.__firstArgs = firstArgs

    def __call__(self, *args):
        """
        """
        return apply(self.__callback, self.__firstArgs + args)
