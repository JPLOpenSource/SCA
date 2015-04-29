"""
Created on Jun 25, 2010

@author: wye and reder

Activity class specific to each model. Contains actdict and determines if inputs
are sufficient. Simple input parameter, step1, step2, outputParameter model.
"""

import logging

LOG = logging.getLogger("ProcedureExampleLogger")  

from af import ActivityBase
import ProcedureExampleImpl

class ProcedureExample (ActivityBase.ActivityBase):
     
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
            
        # 'exec_status' is initially marked "Current" or "Continuous" (if the 
        # action will run multiple times) and marked True if executed.
        # 'tok_list' is list of input tokens for the action.
        # 'controlInput marks input control pins.
        # Need to set stdin to 'no' if you want to input parameters inside MD.
        self.actdict = {'inputParameter' : {'stdin' : 'no',
                                            'next_step' : ['Step1']
                                            },
                        'Step1' : {'argument' : {'source' : 'inputParameter', 
                                                 'min_tok' : 1, 
                                                 'max_tok' : 1,
                                                 'tok_list' : []},
                                   'method' : self.Step1,
                                   'next_step' : 'Step2',
                                   'exec_status' : 'Current'
                                   },
                        'Step2' : {'argument' : {'source' : 'Step1', 
                                                 'min_tok' : 1,
                                                 'max_tok' : 1,
                                                 'tok_list' : []},
                                   'method' : self.Step2,
                                   'next_step' : 'return output parameters',
                                   'exec_status' : 'Current'
                                   },
                        'outputParameter': {'stdout' : 'no',   # If yes returns output and exit, else query
                                            'final' : {'source' : 'Step2', 
                                                       'tok_list' : []}
                                            }
                         }
        
        self.result_map_dict = {"inputParameter": ["Step1.argument"],
                                "Step1.result"  : ["Step2.argument"],
                                "Step2.result"  : ["outputParameter.final"]  }
    
  
    def Step1(self):
        """
        Step1 action from model.
        """
        if self.inRange('Step1', 'argument'):
            r = self.__impl.step1Behav(argument=self.actdict['Step1']['argument']['tok_list'])
            r['status'] = ['ProcedureExample', 'Step1', 'on']
            
            self.clear_tok('Step1')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('Step1')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Step1", self.actdict['Step1'][pin]['tok_list']))
            return False
            

    def Step2(self):
        """
        Step2 action from model.
        """
        if self.inRange('Step2', 'argument'): 
            r = self.__impl.step2Behav(argument=self.actdict['Step2']['argument']['tok_list'])
            r['status'] = ['ProcedureExample', 'Step2', 'on']
            
            self.clear_tok('Step2')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('Step2')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Step2", self.actdict['Step2'][pin]['tok_list']))
            return False


if __name__ == "__main__":
    opt = ActivityBase.ActivityBase.main(LOG)
    # Instantiate the impl Activity class
    impl = ProcedureExampleImpl.ProcedureExampleImpl()
    # Instantiate the Activity main class and start it
    a = ProcedureExample(impl, opt.stepMode)
    a.run()
