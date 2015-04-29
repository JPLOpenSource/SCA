"""
Created on Jul 7, 2010

@author: wye

Contains actdict and determines if action input criteria are met. Has decision node.
"""

import logging

LOG = logging.getLogger("IncrementExampleLogger")

from af import ActivityBase
import IncrementExampleImpl    


class IncrementExample (ActivityBase.ActivityBase):
     
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
                                        'next_step' : ['countEqZero']
                                        },
                        'countEqZero' : {'controlInput' : {'source' : 'initialNode', 
                                                        'min_tok'  : 1, 
                                                        'max_tok'  : 1,
                                                        'tok_list' : []},
                                         'method' : self.countEqZero,
                                         'next_step' : 'countPlusOne',
                                         'exec_status' : "Current"
                                     },
                        'countPlusOne' : {'initialValue' : {'source'  : 'countEqZero', 
                                                         'min_tok'  : 1,
                                                         'max_tok'  : 1,
                                                         'tok_list' : []},
                                         'inputValue' : {'source'  : 'countPlusOne', 
                                                         'min_tok'  : 1,
                                                         'max_tok'  : 1,
                                                         'tok_list' : []},
                                         'method' : self.countPlusOne,
                                         'next_step' : 'decision node',
                                         'exec_status' : "Continuous"
                                     },
                        'decisionIsCount10' : {'decisionValue' : {'source'  : 'countPlusOne', 
                                                                 'min_tok'  : 1,
                                                                 'max_tok'  : 1,
                                                                 'tok_list' : []},
                                                'method' : self.decisionIsCount10,
                                                'next_step' : 'countPlusOne if count=10, else countEqTen',
                                                'exec_status' : "Continuous"
                                                },
                        'countEqTen' : {'inputValue' : {'source' : 'countPlusOne', 
                                                         'min_tok' : 1,
                                                         'max_tok' : 1,
                                                         'tok_list' : []},
                                         'method' : self.countEqTen,
                                         'next_step' : 'return output parameter',
                                         'exec_status' : "Current"
                                     },
                        'finalNode' : {'controlInput' : {'source'  : 'countEqTen', 
                                                     'min_tok' : 1,
                                                     'max_tok' : 1,
                                                     'tok_list' : []},
                                   'method' : self.finalNode,
                                   'next_step' : None,
                                   'exec_status' : "Current"
                                   },
                        'outputParameter' : {"stdout" : 'no',   # If yes returns output and exit, else query
                                            "final" : {'source' : 'countEqTen', 
                                                       'tok_list' : []}
                                             }
                         }
        
        self.result_map_dict = {"initialNode" : ["countEqZero.controlInput"],
                                "countEqZero.outputValue" : ["countPlusOne.initialValue"],
                                "countPlusOne.outputValue" : ["decisionIsCount10.decisionValue"],
                                "countEqTen.outputValue" : ["finalNode.controlInput", "outputParameter.final"]}


    def countEqZero(self):  
        """
        countEqZero action from model.
        """
        if self.inRange('countEqZero', 'controlInput'): 
            r = self.__impl.countEqZeroBehav(initialValue=self.actdict['countEqZero']['controlInput']['tok_list'])
            r['status'] = ['IncrementExample', 'countEqZero', 'on']
            
            self.clear_tok('countEqZero')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('countEqZero')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "countEqZero", self.actdict['countEqZero'][pin]['tok_list']))
            return False


    def decisionIsCount10(self):
        """
        Executes next action(s) from countPlusOne action.
        """
        if self.inRange('decisionIsCount10', 'decisionValue'):
            self.actdict['status'] = ['IncrementExample', 'decisionIsCount10', 'on']
            # Maps token to destination for True condition.
            cond_map = {self.__impl.isCount10: {'destin': ['countEqTen.inputValue']},
                        self.__impl.isCountLess10: {'destin': ['countPlusOne.inputValue']}
                        }
            
            # Input definition of decision node key in actdict and cond_map.
            self.decision(self.actdict['decisionIsCount10'], cond_map)
            return True
        else:
            return False
        
        
    def countPlusOne(self):
        """
        countPlusOne action from model.
        """
        if self.inRange('countPlusOne', 'initialValue') or self.inRange('countPlusOne', 'inputValue'):
            r = self.__impl.countPlusOneBehav(initialValue=self.actdict['countPlusOne']['initialValue']['tok_list'],
                                              inputValue=self.actdict['countPlusOne']['inputValue']['tok_list'])
            r['status'] = ['IncrementExample', 'countPlusOne', 'on']

            self.clear_tok('countPlusOne')
            self.parseResult(r)
            return True
        else:
            insuff_pins = self.wrong_tok('countPlusOne')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "countPlusOne", self.actdict['countPlusOne'][pin]['tok_list']))
            return False
            
            
    def countEqTen(self):
        """
        countEqTen action from model.
        """
        if self.inRange('countEqTen', 'inputValue'):
            r = self.__impl.countEqTenBehav(inputValue=self.actdict['countEqTen']['inputValue']['tok_list'])
            r['status'] = ['IncrementExample', 'countEqTen', 'on'] 

            self.clear_tok('countEqTen')      
            self.parseResult(r)
            return True
        else:
            insuff_pins = self.wrong_tok('countEqTen')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "countEqTen", self.actdict['countEqTen'][pin]['tok_list']))
            return False
        

    def finalNode(self):
        """
        Final node from model.
        """
        if self.inRange("finalNode", "controlInput"):
            for ikey in self.actdict.keys():
                if 'exec_status' in self.actdict[ikey].keys() and self.actdict[ikey]['exec_status'] == "Continuous":
                    self.actdict[ikey]['exec_status'] = True
            self.actdict['status'] = ['IncrementExample', 'finalNode', 'on']
            self.actdict['message'] = "All actions have executed."
            return True
        else:
            return False


if __name__ == "__main__":
    opt = ActivityBase.ActivityBase.main(LOG)
    # Instantiate the impl Activity class
    impl = IncrementExampleImpl.IncrementExampleImpl()
    # Instantiate the Activity main class and start it
    a = IncrementExample(impl, opt.stepMode)
    a.run()
