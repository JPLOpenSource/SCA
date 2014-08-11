'''
Created on Jul 19, 2010

@author: wye

User input section
'''
import logging
LOG = logging.getLogger("MergeExampleLogger")

class MergeExampleImpl(object):
    """
    Describes behavior of acts
    """
     
    def __init__(self):
        '''
        Constructor
        '''
        

    def action1Behav(self, argument):
        """
        User written behavior
        """ 
        tok1 = int(argument[0]) + 2               
        resdict = {'Action1.result' : [tok1]}
        
        message = 'Action1 input: %s ; Action1 output: %s' % (argument, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict


    def isUnder10(self, token):
        if token <= 10:
            return True
        else:
            return False
        
    def isOver10(self, token):
        if token > 10:
            return True
        else:
            return False


    def under10Behav(self, argument):
        """
        User written behavior
        """
        if argument[0] <= 10:
            tok1 = 'Under10 result'
            resdict = {'Under10.result':[tok1]}
    
            message = 'Under10 input: %s ; Under10 output: %s' % (argument, [tok1])
            LOG.info(message)
            # Adds message entry into resdict.
            resdict['message'] = message

            return resdict
        else:
            print 'Answer is not actually less than or equal to ten.'
    
    
    def over10Behav(self, argument):
        """
        User written behavior
        """
        if argument[0] > 10:
            tok1 = 'Over10 result'
            resdict = {'Over10.result':[tok1]}
    
            message = 'Over10 input: %s ; Over10 output: %s' % (argument, [tok1])
            LOG.info(message)
            # Adds message entry into resdict.
            resdict['message'] = message

            return resdict
        else:
            print 'Answer is not actually over 10'
    
    
    def action2Behav(self, argument):
        """
        User written behavior
        """
        tok1 = 'Final result'
        resdict = {'Action2.result':[tok1]}

        message = 'Action2 input: %s ; Action2 output: %s' % (argument, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict
    
    