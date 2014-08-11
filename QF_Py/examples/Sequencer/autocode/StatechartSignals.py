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
        b5 = Button(frm, text = "Error", command=callback)
        b5.pack()
        callback = ButtonCallback(self.doButton, "6")
        b6 = Button(frm, text = "Event0", command=callback)
        b6.pack()
        callback = ButtonCallback(self.doButton, "7")
        b7 = Button(frm, text = "Event1", command=callback)
        b7.pack()
        callback = ButtonCallback(self.doButton, "8")
        b8 = Button(frm, text = "Event2", command=callback)
        b8.pack()
        callback = ButtonCallback(self.doButton, "9")
        b9 = Button(frm, text = "EventN", command=callback)
        b9.pack()
        callback = ButtonCallback(self.doButton, "10")
        b10 = Button(frm, text = "Recover", command=callback)
        b10.pack()
        callback = ButtonCallback(self.doButton, "11")
        b11 = Button(frm, text = "RepeatAction", command=callback)
        b11.pack()
        callback = ButtonCallback(self.doButton, "12")
        b12 = Button(frm, text = "Reset", command=callback)
        b12.pack()

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
