"""
Created on Aug 23, 2010

@author: wye

User input section
"""
import logging
LOG = logging.getLogger("SignalExampleLogger")

class SignalExampleImpl(object):
    """
    Describes behavior of acts
    """
     
    def __init__(self):
        '''
        Constructor
        '''
        
    def turnONBehav(self, argument):
        """
        User written behavior
        """
        tok1 = "System is ON"
        resdict = {'TurnON.signalValue' : [tok1]}

        message = 'TurnON input: %s ; TurnON output: %s' % (argument, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict


    def actionBehav(self, argument):
        """
        User written behavior
        """
        tok1 = "Action performed"
        resdict = {'Action.result' : [tok1]}

        message = 'Action input: %s ; Action output: %s' % (argument, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict
    
    