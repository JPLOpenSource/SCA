'''
Created on Jul 12, 2010

@author: wye

User input section
'''
import logging
LOG = logging.getLogger("CapabilityExampleLogger")

class CapabilityExampleImpl(object):
    """
    Describes behavior of acts
    """
     
    def __init__(self):
        '''
        Constructor
        '''
        

    def generateProcessInputParametersBehav(self, argument):
        """
        User written behavior
        """ 
        tok1 = 'Input Parameters'                       
        resdict = {'generateProcessInputParameters.result':[tok1]}          
        
        message = 'generateProcessInputParameters input: %s ; generateProcessInputParameters output: %s' % (argument, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict


    def process1Behav(self, argument):
        """
        User written behavior
        """
        tok1 = 'Result 1'
        resdict = {'Process1.result':[tok1]}

        message = 'Process1 input: %s ; Process1 output: %s' % (argument, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict
    
    
    def process2Behav(self, argument):
        """
        User written behavior
        """
        tok1 = 'Result 2'
        resdict = {'Process2.result':[tok1]}

        message = 'Process2 input: %s ; Process2 output: %s' % (argument, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict
    
    
    def process3Dot1Behav(self, argument):
        """
        User written behavior
        """
        tok1 = 'Result 3.1'
        resdict = {'process3Dot1.result':[tok1]}

        message = 'process3Dot1 input: %s ; process3Dot1 output: %s' % (argument, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict
    
    
    def process3Dot2Behav(self, argument):
        """
        User written behavior
        """
        tok1 = 'Result 3.2'
        resdict = {'process3Dot2.result':[tok1]}

        message = 'process3Dot2 input: %s ; process3Dot2 output: %s' % (argument, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict
    