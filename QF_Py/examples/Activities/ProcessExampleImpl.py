'''
Created on Jul 12, 2010

@author: wye

User input section
'''
import logging
LOG = logging.getLogger("ProcessExampleLogger")

class ProcessExampleImpl(object):
    """
    Describes behavior of acts
    """
     
    def __init__(self):
        '''
        Constructor
        '''
        

    def generateCapabilityMetricBehav(self, argument):
        """
        User written behavior
        """ 
        tok1 = 'Result 1.1' 
        tok2 = 'Result 1.2'                    
        resdict = {'generateCapabilityMetric.result1' : [tok1],
                   'generateCapabilityMetric.result2' : [tok2]}          
        
        message = 'generateCapabilityMetric input: %s ; generateCapabilityMetric output: %s' % (argument, [tok1, tok2])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict


    def capabilityBehav(self, argument):
        """
        User written behavior
        """
        tok1 = 'Result 2'
        resdict = {'Capability.result':[tok1]}

        message = 'Capability input: %s ; Capability output: %s' % (argument, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict
    
    
    def additionalCapabilityBehav(self, argument):
        """
        User written behavior
        """
        tok1 = 'Result 3'
        resdict = {'additionalCapability.result':[tok1]}

        message = 'additionalCapability input: %s ; additionalCapability output: %s' % (argument, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict
    
    
    def generatePredictionBehav(self, argument1, argument2):
        """
        User written behavior
        """
        tok1 = 'Result 4'
        resdict = {'generatePrediction.result':[tok1]}
        argument = argument1 + argument2

        message = 'generatePrediction input: %s ; generatePrediction output: %s' % (argument, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict
    
    