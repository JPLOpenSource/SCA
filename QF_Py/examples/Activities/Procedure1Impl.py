'''
Created on Jul 22, 2010

@author: wye

User input section
'''
import logging
LOG = logging.getLogger("Procedure1Logger")

class Procedure1Impl(object):
    """
    Describes behavior of acts
    """
     
    def __init__(self):
        '''
        Constructor
        '''
        

    def step1SendDataBehav(self, argument):
        """
        User written behavior
        """
        tok1 = argument + ['step1SendData complete']
        resdict = {'step1SendData.result': tok1}
        
        message = 'step1SendData input: %s ; step1SendData output: %s' % (argument, tok1)
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict


    def step2Behav(self, argument):
        """
        User written behavior
        """
        tok1 = argument + ['Step2 complete']
        resdict = {'Step2.result': tok1}

        message = 'Step2 input: %s ; Step2 output: %s' % (argument, tok1)
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict
    
