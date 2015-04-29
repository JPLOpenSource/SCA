"""
Created on Jul 7, 2010

@author: wye

User input section
"""

import logging
LOG = logging.getLogger("IncrementExampleLogger")

class IncrementExampleImpl(object):
    """
    Describes behavior of acts
    """
     
    def __init__(self):
        '''
        Constructor
        '''
        

    def countEqZeroBehav(self, initialValue):
        """
        User written behavior
        """ 
        tok1 = 0                       
        resdict = {'countEqZero.outputValue' : [tok1]}          
        
        message = 'countEqZero input: %s ; countEqZero output: %s' % (initialValue, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict


    def countPlusOneBehav(self, initialValue, inputValue):
        """
        User written behavior
        """
        inp = 0
        if len(initialValue) > 0:  # we got an initial value
            inp = initialValue[0]
            del initialValue[0]
        elif len(inputValue) > 0:  # we got an increment value
            inp = inputValue[0]
            del inputValue[0]
        else:
            LOG.error("ERROR! Did NOT receive either initial or increment values.")

        tok1 = int(inp) + 1
        resdict = {'countPlusOne.outputValue':[tok1]}

        message = 'countPlusOne input: %s ; countPlusOne output: %s' % (inp, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict
    
    # Decision node conditions.
    def isCount10(self, token):
        ''' 
        Decision node condition 1
        '''
        if token == 10:
            return True

    def isCountLess10(self, token):
        ''' 
        Decision node condition 2
        '''
        if token < 10:
            return True
    
    
    def countEqTenBehav(self, inputValue):
        """
        User written behavior
        """
        inp = inputValue[0]
        del inputValue[0]
        tok1 = 'End actions'
        resdict = {'countEqTen.outputValue':[tok1]}

        message = 'countEqTen input: %s ; countEqTen output: %s' % (inp, [tok1])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict
    
    