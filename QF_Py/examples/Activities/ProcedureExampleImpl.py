"""
Created on Jun 25, 2010

@author: wye and reder

ActivityImpl class. User input section. Instanced by Activity class.
"""
import logging
LOG = logging.getLogger("ProcedureExampleLogger")

class ProcedureExampleImpl(object):
    """
    Describes behavior of acts
    """
     
    def __init__(self):
        '''
        Constructor
        '''

    def step1Behav(self, argument):
        """
        User written behavior
        """
        in1 = argument[0]                          # User codes inputs in1,2,3... and outputs tok1,2,3...
        tok1 = int(in1) * 2                        
        resdict = {'Step1.result':[tok1]}          # User fills in resdict with connections of tokens to outputs
        
        message = 'Step1 input: %s ; Step1 output: %s' % (argument, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict


    def step2Behav(self, argument):
        """
        User written behavior
        """
        in1 = argument[0]
        tok1 = int(in1) + 3
        resdict = {'Step2.result':[tok1]}

        message = 'Step2 input: %s ; Step2 output: %s' % (argument, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict
    
    