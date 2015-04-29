'''
Created on Jul 22, 2010

@author: wye

User input section
'''
import logging
LOG = logging.getLogger("Procedure3Logger")

class Procedure3Impl(object):
    """
    Describes behavior of acts
    """
     
    def __init__(self):
        '''
        Constructor
        '''
        

    def step5RetrieveDataValueBehav(self, argument):
        """
        User written behavior
        """
        tok1 = argument + ['step5RetrieveDataValue complete']
        resdict = {'step5RetrieveDataValue.result': tok1}     
        
        message = 'step5RetrieveDataValue input: %s ; step5RetrieveDataValue output: %s' % (argument, tok1)
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict

