'''
Created on Jul 22, 2010

@author: wye

User input section
'''
import logging
LOG = logging.getLogger("Procedure2Logger")

class Procedure2Impl(object):
    """
    Describes behavior of acts
    """
     
    def __init__(self):
        '''
        Constructor
        '''


    def step3Behav(self, argument):
        """
        User written behavior
        """
        tok1 = argument + ['Step3 complete']
        resdict = {'Step3.result': tok1}     
        
        message = 'Step3 input: %s ; Step3 output: %s' % (argument, tok1)
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict


    def step4Behav(self, argument):
        """
        User written behavior
        """
        tok1 = argument + ['Step4 complete']
        resdict = {'Step4.result': tok1}

        message = 'Step4 input: %s ; Step4 output: %s' % (argument, tok1)
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict
    
