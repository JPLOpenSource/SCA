'''
Created on Jul 12, 2010

@author: wye

Contains actdict and step functions. Contains action that requires 2 input pins
to be filled before activating.
'''

import logging

LOG = logging.getLogger("ProcessExampleLogger")

from af import ActivityBase
import ProcessExampleImpl  
  

class ProcessExample (ActivityBase.ActivityBase):
     
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
        self.actdict = {'initialNode' : {"stdin" : "auto",
                                            'next_step' : ['generateCapabilityMetric']
                                            },
                        'generateCapabilityMetric' : {'controlInput' : {'source' : 'initialNode', 
                                                                    'min_tok' : 1, 
                                                                    'max_tok' : 1,
                                                                    'tok_list' : []},
                                                      'method' : self.generateCapabilityMetric,
                                                      'next_step' : 'Capability and additionalCapability',
                                                      'exec_status' : 'Current'
                                                      },
                        'Capability' : {'argument' : {'source'  : 'generateCapabilityMetric', 
                                                                  'min_tok' : 1,
                                                                  'max_tok' : 1,
                                                                  'tok_list' : []},
                                                      'method' : self.Capability,
                                                      'next_step' : 'generatePrediction',
                                                      'exec_status' : 'Current'
                                                      },
                        'additionalCapability' : {'controlInput' : {'source' : 'generateCapabilityMetric', 
                                                                'min_tok' : 1, 
                                                                'max_tok' : 1,
                                                                'tok_list' : []},
                                                  'method' : self.additionalCapability,
                                                  'next_step' : 'generatePrediction',
                                                  'exec_status' : 'Current'
                                                  },
                        'generatePrediction' : {'argument1' : {'source' : 'Capability', 
                                                                'min_tok' : 1, 
                                                                'max_tok' : 1,
                                                                'tok_list' : []},
                                                'argument2' : {'source' : 'additionalCapability', 
                                                                'min_tok' : 1, 
                                                                'max_tok' : 1,
                                                                'tok_list' : []},
                                                'method' : self.generatePrediction,
                                                'next_step' : 'return output parameter',
                                                'exec_status' : 'Current'
                                                },
                        'outputParameter': {"stdout" : 'no',   # If yes returns output and exit, else query
                                            "final" : {'source' : 'generatePrediction', 
                                                       'tok_list' : []}
                                            }
                        }
        
        self.result_map_dict = {"initialNode" : ["generateCapabilityMetric.controlInput"],
                                "generateCapabilityMetric.result1" : ["Capability.argument"],
                                "generateCapabilityMetric.result2" : ['additionalCapability.controlInput'],
                                "Capability.result" : ["generatePrediction.argument1"],
                                "additionalCapability.result" : ["generatePrediction.argument2"],
                                "generatePrediction.result" : ["outputParameter.final"]  }


    def generateCapabilityMetric(self):
        """
        generateCapabilityMetric action from model.
        """
        if self.inRange('generateCapabilityMetric', 'controlInput'): 
            r = self.__impl.generateCapabilityMetricBehav(argument=self.actdict['generateCapabilityMetric']['controlInput']['tok_list'])
            r['status'] = ['ProcessExample', 'generateCapabilityMetric', 'on'] 

            self.clear_tok('generateCapabilityMetric')      
            self.parseResult(r)
            return True
        else:
            insuff_pins = self.wrong_tok('generateCapabilityMetric')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "generateCapabilityMetric", self.actdict['generateCapabilityMetric'][pin]['tok_list']))
            return False
            

    def Capability(self):
        """
        Capability action from model.
        """
        if self.inRange('Capability', 'argument'): 
            r = self.__impl.capabilityBehav(argument=self.actdict['Capability']['argument']['tok_list'])
            r['status'] = ['ProcessExample', 'Capability', 'on'] 

            self.clear_tok('Capability')      
            self.parseResult(r)
            return True
        else:
            insuff_pins = self.wrong_tok('Capability')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Capability", self.actdict['Capability'][pin]['tok_list']))
            return False


    def additionalCapability(self):
        """
        additionalCapability action from model.
        """
        if self.inRange('additionalCapability', 'controlInput'): 
            r = self.__impl.additionalCapabilityBehav(argument=self.actdict['additionalCapability']['controlInput']['tok_list'])
            r['status'] = ['ProcessExample', 'additionalCapability', 'on'] 

            self.clear_tok('additionalCapability')      
            self.parseResult(r)
            return True
        else:
            insuff_pins = self.wrong_tok('additionalCapability')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "additionalCapability", self.actdict['additionalCapability'][pin]['tok_list']))
            return False


    def generatePrediction(self):
        """
        generatePrediction action from model.
        """
        if self.inRange('generatePrediction', 'argument1') and self.inRange('generatePrediction', 'argument2'): 
                r = self.__impl.generatePredictionBehav(argument1=self.actdict['generatePrediction']['argument1']['tok_list'],
                                                        argument2=self.actdict['generatePrediction']['argument2']['tok_list'])
                r['status'] = ['ProcessExample', 'generatePrediction', 'on'] 
    
                self.clear_tok('generatePrediction')      
                self.parseResult(r)
                return True
        else:
            insuff_pins = self.wrong_tok('generatePrediction')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "generatePrediction", self.actdict['generatePrediction'][pin]['tok_list']))
            return False


if __name__ == "__main__":
    opt = ActivityBase.ActivityBase.main(LOG)
    # Instantiate the impl Activity class
    impl = ProcessExampleImpl.ProcessExampleImpl()
    # Instantiate the Activity main class and start it
    a = ProcessExample(impl, opt.stepMode)
    a.run()
