''' 
Created on Jul 12, 2010

@author: wye

Contains actdict and determines if action input criteria are met. Contains 
fork node.
'''

import logging

LOG = logging.getLogger("CapabilityExampleLogger")   

from af import ActivityBase
import CapabilityExampleImpl    


class CapabilityExample (ActivityBase.ActivityBase):
     
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
                                            'next_step' : ['generateProcessInputParameters']
                                            },
                         'generateProcessInputParameters' : {'controlInput' : {'source' : 'initialNode', 
                                                                            'min_tok'  : 1, 
                                                                            'max_tok'  : 1,
                                                                            'tok_list' : []},
                                                             'method' : self.generateProcessInputParameters,
                                                             'next_step' : 'Process1',
                                                             'exec_status' : 'Current'
                                                            },
                         'Process1' : {'argument' : {'source'  : 'generateProcessInputParameters', 
                                                     'min_tok'  : 1,
                                                     'max_tok'  : 1,
                                                     'tok_list' : []},
                                         'method' : self.Process1,
                                         'next_step' : 'Process2',
                                         'exec_status' : 'Current'
                                        },
                         'Process2' : {'argument' : {'source'  : 'Process1', 
                                                     'min_tok'  : 1,
                                                     'max_tok'  : 1,
                                                     'tok_list' : []},
                                     'method' : self.Process2,
                                     'next_step' : 'processes 3.1 and 3.2',
                                     'exec_status' : 'Current'
                                        },
                        'process3Dot1' : {'argument' : {'source'  : 'Process2', 
                                                         'min_tok'  : 1,
                                                         'max_tok'  : 1,
                                                         'tok_list' : []},
                                         'method' : self.process3Dot1,
                                         'next_step' : 'return output parameter 1',
                                         'exec_status' : 'Current'
                                         },
                        'process3Dot2' : {'argument' : {'source'  : 'Process2', 
                                                         'min_tok'  : 1,
                                                         'max_tok'  : 1,
                                                         'tok_list' : []},
                                         'method' : self.process3Dot2,
                                         'next_step' : 'return output parameter 2',
                                         'exec_status' : 'Current'
                                         },
                        'outputParameter1': {"stdout" : 'no',   # If yes returns output and exit, else query
                                             "final" : {'source' : ['process3Dot1'], 
                                                        'tok_list' : []}
                                             },
                        'outputParameter2': {"stdout" : 'no',   # If yes returns output and exit, else query
                                             "final" : {'source' : ['process3Dot2'], 
                                                        'tok_list' : []}
                                             }
                         }
        
        # Need to change result_map_dict for fork and merge nodes.
        self.result_map_dict = {"initialNode": ["generateProcessInputParameters.controlInput"],
                                "generateProcessInputParameters.result"  : ["Process1.argument"],
                                "Process1.result"  : ["Process2.argument"],
                                "Process2.result" : ["process3Dot1.argument", "process3Dot2.argument"],
                                "process3Dot1.result" : ["outputParameter1.final"], 
                                "process3Dot2.result" : ["outputParameter2.final"] }

 
    def generateProcessInputParameters(self):  
        """
        generateProcessInputParameters action from model.
        """
        if self.inRange('generateProcessInputParameters', 'controlInput'): 
            r = self.__impl.generateProcessInputParametersBehav(argument=self.actdict['generateProcessInputParameters']['controlInput']['tok_list'])
            r['status'] = ['CapabilityExample', 'generateProcessInputParameters', 'on']
            
            self.clear_tok('generateProcessInputParameters')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('generateProcessInputParameters')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "generateProcessInputParameters", self.actdict['generateProcessInputParameters'][pin]['tok_list']))
            return False
                    
        
    def Process1(self):
        """
        Process1 action from model.
        """
        if self.inRange('Process1', 'argument'):
            r = self.__impl.process1Behav(argument=self.actdict['Process1']['argument']['tok_list'])
            r['status'] = ['CapabilityExample', 'Process1', 'on']
            
            self.clear_tok('Process1')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('Process1')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Process1", self.actdict['Process1'][pin]['tok_list']))
            return False
            
            
    def Process2(self):
        """
        Process2 action from model.
        """
        if self.inRange('Process2', 'argument'):
            r = self.__impl.process2Behav(argument=self.actdict['Process2']['argument']['tok_list'])
            r['status'] = ['CapabilityExample', 'Process2', 'on']
            
            self.clear_tok('Process2')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('Process2')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Process2", self.actdict['Process2'][pin]['tok_list']))
            return False
            
    
    def process3Dot1(self):
        """
        process3Dot1 action from model.
        """
        if self.inRange('process3Dot1', 'argument'):
            r = self.__impl.process3Dot1Behav(argument=self.actdict['process3Dot1']['argument']['tok_list'])
            r['status'] = ['CapabilityExample', 'process3Dot1', 'on']
            
            self.clear_tok('process3Dot1')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('process3Dot1')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "process3Dot1", self.actdict['process3Dot1'][pin]['tok_list']))
            return False
            
        
    def process3Dot2(self):
        """
        process3Dot2 action from model.
        """
        if self.inRange('process3Dot2', 'argument'):
            r = self.__impl.process3Dot2Behav(argument=self.actdict['process3Dot2']['argument']['tok_list'])
            r['status'] = ['CapabilityExample', 'process3Dot2', 'on']
            
            self.clear_tok('process3Dot2')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('process3Dot2')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "process3Dot2", self.actdict['process3Dot2'][pin]['tok_list']))
            return False
            

if __name__ == "__main__":
    opt = ActivityBase.ActivityBase.main(LOG)
    # Instantiate the impl Activity class
    impl = CapabilityExampleImpl.CapabilityExampleImpl()
    # Instantiate the Activity main class and start it
    a = CapabilityExample(impl, opt.stepMode)
    a.run()

