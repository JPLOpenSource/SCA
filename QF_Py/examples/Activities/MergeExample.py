'''
Created on Jul 19, 2010

@author: wye

Contains actdict and determines if action input criteria are met. Contains 
decision and merge nodes.
'''

import logging

LOG = logging.getLogger("MergeExampleLogger")

from af import ActivityBase
import MergeExampleImpl    


class MergeExample (ActivityBase.ActivityBase):
     
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
        self.actdict = {'inputParameter' : {"stdin" : "no",
                                            'next_step' : ['Action1']
                                            },
                        'Action1' : {'argument' : {'source' : 'inputParameter', 
                                                   'min_tok' : 1, 
                                                   'max_tok' : 1,
                                                   'tok_list' : []},
                                     'method' : self.Action1,
                                     'next_step' : 'decision node',
                                     'exec_status' : 'Current'
                                     },
                        'decisionOver10' : {'decisionValue' : {'source' : 'Action1', 
                                                               'min_tok' : 1,
                                                               'max_tok' : 1,
                                                               'tok_list' : []},
                                            'method' : self.decisionOver10,
                                            'next_step' : 'Over10 if ans=True, else ansIsFalse',
                                            'exec_status' : 'Current'
                                            },
                        'Over10' : {'argument' : {'source' : 'Action1', 
                                                  'min_tok' : 1,
                                                  'max_tok' : 1,
                                                  'tok_list' : []},
                                         'method' : self.Over10,
                                         'next_step' : 'Action2',
                                         'exec_status' : 'Current'
                                     },
                        'Under10' : {'argument' : {'source' : 'Action1', 
                                                   'min_tok' : 1,
                                                   'max_tok' : 1,
                                                   'tok_list' : []},
                                     'method' : self.Under10,
                                     'next_step' : 'Action2',
                                     'exec_status' : 'Current'
                                     },
                        'Action2' : {'argument' : {'source' : 'Over10', 
                                                   'min_tok' : 1,
                                                   'max_tok' : 1,
                                                   'tok_list' : []},
                                     'method' : self.Action2,
                                     'next_step' : 'return output parameter',
                                     'exec_status' : 'Current'
                                     },
                        'finalNode' : {'controlInput' : {'source'  : 'Action2', 
                                                     'min_tok' : 1,
                                                     'max_tok' : 1,
                                                     'tok_list' : []},
                                   'method' : self.finalNode,
                                   'next_step' : None,
                                   'exec_status' : "Current"
                                   },
                        'outputParameter': {"stdout" : 'no',   # If yes returns output and exit, else query
                                            "final" : {'source' : 'Under10', 
                                                       'tok_list' : []}
                                             }
                         }
        
        # Need to change result_map_dict for fork and merge nodes.
        self.result_map_dict = {"inputParameter" : ["Action1.argument"],
                                "Action1.result"  : ["decisionOver10.decisionValue"],
                                "Over10.result"  : ["Action2.argument"],
                                "Under10.result" : ["Action2.argument"],
                                "Action2.result" : ["finalNode.controlInput", "outputParameter.final"]  }


    def Action1(self):  
        """
        Action1 action from model.
        """
        if self.inRange('Action1', 'argument'): 
            r = self.__impl.action1Behav(argument=self.actdict['Action1']['argument']['tok_list'])
            r['status'] = ['MergeExample', 'Action1', 'on']
            
            self.clear_tok('Action1')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('Action1')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Action1", self.actdict['Action1'][pin]['tok_list']))
            return False


    def decisionOver10(self):
        """
        Executes next action(s) from Action1 action.
        """
        if self.inRange('decisionOver10', 'decisionValue'):
            self.actdict['status'] = ['MergeExample', 'decisionOver10', 'on']
            # Maps token to destination and actions for condition = True.
            cond_map = {self.__impl.isOver10: {'destin': ['Over10.argument']},
                        self.__impl.isUnder10: {'destin': ['Under10.argument']}
                        }
            
            # Input definition of decision node key in actdict and cond_map.
            self.decision(self.actdict['decisionOver10'], cond_map)
            return True
        else:
            return False
        
        
    def Over10(self):
        """
        Over10 action from model.
        """
        if self.inRange('Over10', 'argument'):
            r = self.__impl.over10Behav(argument=self.actdict['Over10']['argument']['tok_list'])
            r['status'] = ['MergeExample', 'Over10', 'on']
            
            self.clear_tok('Over10')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('Over10')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Over10", self.actdict['Over10'][pin]['tok_list']))
            return False
            
            
    def Under10(self):  
        """
        Under10 action from model.
        """
        if self.inRange('Under10', 'argument'): 
            r = self.__impl.under10Behav(argument=self.actdict['Under10']['argument']['tok_list'])
            r['status'] = ['MergeExample', 'Under10', 'on']
            
            self.clear_tok('Under10')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('Under10')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Under10", self.actdict['Under10'][pin]['tok_list']))
            return False
            
            
    def Action2(self):
        """
        Action2 action from model.
        """
        if self.inRange('Action2', 'argument'): 
            r = self.__impl.action2Behav(argument=self.actdict['Action2']['argument']['tok_list'])
            r['status'] = ['MergeExample', 'Action2', 'on']
            
            self.clear_tok('Action2')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('Action2')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Action2", self.actdict['Action2'][pin]['tok_list']))
            return False
        
        
    def finalNode(self):
        """
        Final node from model.
        """
        if self.inRange("finalNode", "controlInput"):
            for ikey in self.actdict.keys():
                if 'exec_status' in self.actdict[ikey].keys() and self.actdict[ikey]['exec_status'] == "Continuous":
                    self.actdict[ikey]['exec_status'] = True
            self.actdict['status'] = ['MergeExample', 'finalNode', 'on']
            self.actdict['message'] = "All actions have executed."
            return True
        else:
            return False


if __name__ == "__main__":
    opt = ActivityBase.ActivityBase.main(LOG)
    # Instantiate the impl Activity class
    impl = MergeExampleImpl.MergeExampleImpl()
    # Instantiate the Activity main class and start it
    a = MergeExample(impl, opt.stepMode)
    a.run()
