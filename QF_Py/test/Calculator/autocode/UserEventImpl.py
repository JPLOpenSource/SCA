'''
Created on Apr 15, 2010

@author: Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
'''
from qf import event


class KeyEvent(event.Event):
    '''
    User event for a key press.
    '''

    def __init__(self, signal, keyId=''):
        '''
        Instantiates this event with a signal and key ID
        '''
        event.Event.__init__(self, signal)
        self.keyId = keyId

class ResultEvent(event.Event):
    '''
    User event for computation result.
    '''

    def __init__(self, result):
        '''
        Instantiates this event with a computation result
        '''
        event.Event.__init__(self, "ReportResult")
        self.result = result
