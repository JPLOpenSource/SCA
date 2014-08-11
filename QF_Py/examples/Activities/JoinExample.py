'''
Created on Jul 20, 2010

@author: wye

Contains actdict and determines if action input criteria are met. Contains 
join node. Takes 2 numbers produced in Action1 and Action2, synchronizes them,
and adds the values together in Action3.
'''

import logging

LOG = logging.getLogger("JoinExampleLogger")   

from af import ActivityBase
import JoinExampleImpl 


class JoinExample (ActivityBase.ActivityBase):
     
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
                                         'next_step' : ['Action1','Action2']
                                         },
                        'Action1' : {'controlInput' : {'source' : 'initialNode', 
                                                    'min_tok'  : 1, 
                                                    'max_tok'  : 1,
                                                    'tok_list' : []},
                                     'method' : self.Action1,
                                     'next_step' : 'joinNode',
                                     'exec_status' : 'Current'
                                     },
                        'Action2' : {'controlInput' : {'source'  : 'initialNode', 
                                                     'min_tok'  : 1,
                                                     'max_tok'  : 1,
                                                     'tok_list' : []},
                                     'method' : self.Action2,
                                     'next_step' : 'joinNode',
                                     'exec_status' : 'Current'
                                     },
                        'joinNode' : {'joinValue1' : {'source'  : 'Action1', 
                                                     'min_tok'  : 1,
                                                     'max_tok'  : 1,
                                                     'tok_list' : []},
                                        'joinValue2' : {'source'  : 'Action2', 
                                                     'min_tok'  : 1,
                                                     'max_tok'  : 1,
                                                     'tok_list' : []},
                                        'method' : self.joinNode,
                                        'next_step' : 'Action3',
                                        'exec_status' : 'Current'
                                     },
                        'Action3' : {'argument' : {'source'  : 'join node', 
                                                     'min_tok'  : 1,
                                                     'max_tok'  : 1,
                                                     'tok_list' : []},
                                         'method' : self.Action3,
                                         'next_step' : 'return output parameter',
                                         'exec_status' : 'Current'
                                     },
                        'finalNode' : {'controlInput' : {'source'  : 'Action3', 
                                                     'min_tok' : 1,
                                                     'max_tok' : 1,
                                                     'tok_list' : []},
                                   'method' : self.finalNode,
                                   'next_step' : None,
                                   'exec_status' : "Current"
                                   },
                        'outputParameter': {"stdout" : 'no',   # If yes returns output and exit, else query
                                            "final" : {'source' : 'Action3', 
                                                       'tok_list' : []}
                                             }
                         }
        
        self.result_map_dict = {"initialNode" : ["Action1.controlInput", "Action2.controlInput"],
                                "Action1.result"  : ["joinNode.joinValue1"],
                                "Action2.result" : ["joinNode.joinValue2"],
                                "joinNode.result" : ["Action3.argument"],
                                "Action3.result" : ["finalNode.controlInput", "outputParameter.final"]  }


    def Action1(self):  
        """
        Action1 action from model.
        """
        if self.inRange('Action1', 'controlInput'): 
            r = self.__impl.action1Behav(argument=self.actdict['Action1']['controlInput']['tok_list'])
            r['status'] = ['JoinExample', 'Action1', 'on']
            
            self.clear_tok('Action1')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('Action1')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Action1", self.actdict['Action1'][pin]['tok_list']))
            return False


    def Action2(self):
        """
        Action2 action from model.
        """
        if self.inRange('Action2', 'controlInput'): 
            r = self.__impl.action2Behav(argument=self.actdict['Action2']['controlInput']['tok_list'])
            r['status'] = ['JoinExample', 'Action2', 'on']
            
            self.clear_tok('Action2')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('Action2')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Action2", self.actdict['Action2'][pin]['tok_list']))
            return False
            

    def joinNode(self):
        """
        joinNode action from model.
        """
        if self.inRange('joinNode', 'joinValue1') and self.inRange('joinNode', 'joinValue2'):
                tok1 = self.actdict['joinNode']['joinValue1']['tok_list'] + self.actdict['joinNode']['joinValue2']['tok_list']
                resdict = {'joinNode.result' : [tok1]}
        
                message = 'joinNode input: %s ; joinNode output: %s' % (tok1, [tok1])
                LOG.info(message)
                # Adds message entry into resdict.
                resdict['message'] = message
                resdict['status'] = ['JoinExample', 'joinNode', 'on']
            
                self.clear_tok('joinNode')
                self.parseResult(resdict)
                return True    
        else:
            insuff_pins = self.wrong_tok('joinNode')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "joinNode", self.actdict['joinNode'][pin]['tok_list']))
            return False
            
            
    def Action3(self): 
        """
        Action3 action from model.
        """
        if self.inRange('Action3', 'argument'):
            r = self.__impl.action3Behav(argument=self.actdict['Action3']['argument']['tok_list'])
            r['status'] = ['JoinExample', 'Action3', 'on']
            
            self.clear_tok('Action3')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('Action3')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Action3", self.actdict['Action3'][pin]['tok_list']))
            return False
        
        
    def finalNode(self):
        """
        Final node from model.
        """
        if self.inRange("finalNode", "controlInput"):
            for ikey in self.actdict.keys():
                if 'exec_status' in self.actdict[ikey].keys() and self.actdict[ikey]['exec_status'] == "Continuous":
                    self.actdict[ikey]['exec_status'] = True
            self.actdict['status'] = ['JoinExample', 'finalNode', 'on']
            self.actdict['message'] = "All actions have executed."
            return True
        else:
            return False
            

if __name__ == "__main__":
    opt = ActivityBase.ActivityBase.main(LOG)
    # Instantiate the impl Activity class
    impl = JoinExampleImpl.JoinExampleImpl()
    # Instantiate the Activity main class and start it
    a = JoinExample(impl, opt.stepMode)
    a.run()
