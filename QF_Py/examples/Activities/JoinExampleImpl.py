'''
Created on Jul 20, 2010

@author: wye

User input section
'''
import logging
LOG = logging.getLogger("JoinExampleLogger")

class JoinExampleImpl(object):
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
        tok1 = 5                 
        resdict = {'Action1.result' : [tok1]}
        
        message = 'Action1 input: %s ; Action1 output: %s' % (argument, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict


    def action2Behav(self, argument):
        """
        User written behavior
        """
        tok1 = 6
        resdict = {'Action2.result' : [tok1]}

        message = 'Action2 input: %s ; Action2 output: %s' % (argument, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict
    
    
    def action3Behav(self, argument):
        """
        User written behavior
        """
        arg = argument[0]
        tok1 = arg[0] + arg[1]
        resdict = {'Action3.result' : [tok1]}

        message = 'Action3 input: %s ; Action3 output: %s' % (argument, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict
    