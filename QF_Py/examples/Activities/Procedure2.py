'''
Created on Jul 22, 2010

@author: wye

Call action of Process1. Contains actdict and determines if inputs are 
sufficient. Simple input parameter, step3, step4, outputParameter model.
'''

import logging

LOG = logging.getLogger("Procedure2Logger")  

from af import ActivityBase


class Procedure2 (ActivityBase.ActivityBase):
     
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
        # current action to be run, and marked True if executed.
        # 'tok_list' is list of input tokens for the action.
        # 'controlInput marks input control pins.
        self.actdict = {'argument' : {'stdin' : 'no',
                                      'next_step' : ['Step3']
                                      },
                         'Step3' : {'argument' : {'source' : 'argument', 
                                                  'min_tok' : 1, 
                                                  'max_tok' : 3,
                                                  'tok_list' : []},
                                    'method' : self.Step3,
                                    'next_step' : 'Step4',
                                    'exec_status' : 'Current'
                                    },
                         'Step4' : {'argument' : {'source' : 'Step3', 
                                                  'min_tok' : 1,
                                                  'max_tok' : 4,
                                                  'tok_list' : []},
                                    'method' : self.Step4,
                                    'next_step' : 'return output parameters',
                                    'exec_status' : 'Current'
                                    },
                         'result': {'stdout' : 'yes',   # If yes returns output and exit, else query
                                    'final' : {'source' : 'Step4', 
                                               'tok_list' : []}
                                    }
                         }
        
        self.result_map_dict = {"argument" : ["Step3.argument"],
                                "Step3.result" : ["Step4.argument"],
                                "Step4.result" : ["result.final"]}
    
  
    def Step3(self):
        """
        Step3 action from model.
        """
        if self.inRange('Step3', 'argument'): 
            r = self.__impl.step3Behav(argument=self.actdict['Step3']['argument']['tok_list'])
            r['status'] = ['Procedure2', 'Step3', 'on']

            self.clear_tok('Step3')
            self.parseResult(r)
            return True
        else:
            insuff_pins = self.wrong_tok('Step3')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Step3", self.actdict['Step3'][pin]['tok_list']))
            return False
            

    def Step4(self):
        """
        Step4 action from model.
        """
        if self.inRange('Step4', 'argument'): 
            r= self.__impl.step4Behav(argument=self.actdict['Step4']['argument']['tok_list'])      
            r['status'] = ['Procedure2', 'Step4', 'on']

            self.clear_tok('Step4')
            self.parseResult(r)
            return True
        else:
            insuff_pins = self.wrong_tok('Step4')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Step4", self.actdict['Step4'][pin]['tok_list']))
            return False
