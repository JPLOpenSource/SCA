from qf import event

class DataEvent(event.Event):
    data = None
    def __init__(self, sig, data):
        self.signal = sig
        self.data = data
