'''
Created on Jul 22, 2010

@author: wye

Call action of Process1. Contains actdict and determines if inputs are 
sufficient. Simple input parameter, step1, step2, outputParameter model.
'''

import logging

LOG = logging.getLogger("Procedure1Logger")  

from af import ActivityBase


class Procedure1 (ActivityBase.ActivityBase):
     
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
        self.actdict = {'initialNode' : {'stdin' : 'no',
                                      'next_step' : ['step1SendData']
                                      },
                        'step1SendData' : {'controlInput' : {'source' : 'initialNode', 
                                                         'min_tok' : 1, 
                                                         'max_tok' : 1,
                                                         'tok_list' : []},
                                           'method' : self.step1SendData,
                                           'next_step' : 'Step2',
                                           'exec_status' : "Current"
                                           },
                        'Step2' : {'argument' : {'source' : 'step1SendData', 
                                                 'min_tok' : 1,
                                                 'max_tok' : 2,
                                                 'tok_list' : []},
                                   'method' : self.Step2,
                                   'next_step' : 'return output parameters',
                                   'exec_status' : "Current"
                                   },
                        'result' : {'stdout' : 'yes',   # If yes returns output and exit, else query
                                    'final' : {'source' : 'Step2', 
                                               'tok_list' : []}
                                    }
                        }
        
        self.result_map_dict = {"initialNode" : ["step1SendData.controlInput"],
                                "step1SendData.result" : ["Step2.argument"],
                                "Step2.result" : ["result.final"]}
    
  
    def step1SendData(self):
        """
        step1SendData action from model.
        """
        if self.inRange('step1SendData', 'controlInput'):
            # r is dict containing action_name.result, tok_list, message, and status.
            r = self.__impl.step1SendDataBehav(argument=self.actdict['step1SendData']['controlInput']['tok_list'])
            r['status'] = ['Procedure1', 'step1SendData', 'on']

            self.clear_tok('step1SendData')
            self.parseResult(r)
            return True
        else:
            insuff_pins = self.wrong_tok('step1SendData')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "step1SendData", self.actdict['step1SendData'][pin]['tok_list']))
            return False
            

    def Step2(self):
        """
        Step2 action from model.
        """
        if self.inRange('Step2', 'argument'):
            r= self.__impl.step2Behav(argument=self.actdict['Step2']['argument']['tok_list'])      
            r['status'] = ['Procedure1', 'Step2', 'on']

            self.clear_tok('Step2')
            self.parseResult(r)
            return True
        else:
            insuff_pins = self.wrong_tok('Step2')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Step2", self.actdict['Step2'][pin]['tok_list']))
            return False
