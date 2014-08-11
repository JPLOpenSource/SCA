'''
Created on Jul 22, 2010

@author: wye

Call action of Process1. Contains actdict and determines if inputs are 
sufficient. Simple input parameter, step5, outputParameter model.
'''

import logging

LOG = logging.getLogger("Procedure3Logger")  

from af import ActivityBase


class Procedure3 (ActivityBase.ActivityBase):
     
    def __init__(self, impl=None, stepMode=True):
        """
        Constructor
        """
        ActivityBase.ActivityBase.__init__(self, LOG, stepMode)

        if impl != None:
            self.__impl = impl
        else:
            self.__impl = None
        
        if self.stepMode == True:
            LOG.info('Step mode on')
        else:
            LOG.info('Step mode off')
            
        ActivityBase.ActivityBase.main(LOG)
            
        # 'exec_status' is marked False initially, marked 'Current' if it is the
        # current step to be run, and marked True if executed.
        # 'tok_list' is list of input tokens for the action.
        # 'controlInput marks input control pins.
        self.actdict = {'argument' : {'stdin' : 'no',
                                      'next_step' : ['step5RetrieveDataValue']
                                      },
                         'step5RetrieveDataValue' : {'argument' : {'source' : 'argument', 
                                                                   'min_tok' : 1, 
                                                                   'max_tok' : 5,
                                                                   'tok_list' : []},
                                                     'method' : self.step5RetrieveDataValue,
                                                     'next_step' : 'Step4',
                                                     'exec_status' : 'Current'
                                                     },
                         'result': {'stdout' : 'yes',   # If yes returns output and exit, else query
                                    'final' : {'source' : 'Step4', 
                                               'tok_list' : []}
                                    }
                         }
        
        self.result_map_dict = {"argument" : ["step5RetrieveDataValue.argument"],
                                "step5RetrieveDataValue.result" : ["result.final"]}
    
  
    def step5RetrieveDataValue(self):
        """
        step5RetrieveDataValue action from model.
        """
        if self.inRange('step5RetrieveDataValue', 'argument'):
            r = self.__impl.step5RetrieveDataValueBehav(argument=self.actdict['step5RetrieveDataValue']['argument']['tok_list'])
            r['status'] = ['Procedure3', 'step5RetrieveDataValue', 'on']

            self.clear_tok('step5RetrieveDataValue')
            self.parseResult(r)
            return True
        else:
            insuff_pins = self.wrong_tok('step5RetrieveDataValue')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "step5RetrieveDataValue", self.actdict['step5RetrieveDataValue'][pin]['tok_list']))
            return False