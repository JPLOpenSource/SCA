'''
Created on Jul 22, 2010

@author: wye

Hierarchy example. Called actions are Procedure1, Procedure2, and Procedure3.
'''

import logging

LOG = logging.getLogger("Process1Logger")  

from af import ActivityBase
import Process1Impl

# Import call actions.
import Procedure1
import Procedure1Impl
import Procedure2
import Procedure2Impl
import Procedure3
import Procedure3Impl


class Process1 (ActivityBase.ActivityBase):
     
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

        # Set instances for actions
        self.__procedure1_impl = Procedure1Impl.Procedure1Impl()
        self.__procedure1 = Procedure1.Procedure1(self.__procedure1_impl, stepMode)
        self.__procedure2_impl = Procedure2Impl.Procedure2Impl()
        self.__procedure2 = Procedure2.Procedure2(self.__procedure2_impl, stepMode)
        self.__procedure3_impl = Procedure3Impl.Procedure3Impl()
        self.__procedure3 = Procedure3.Procedure3(self.__procedure3_impl, stepMode)
            
        # 'exec_status' is marked "Current" initially or "Continuous" if the 
        # action will run multiple times, and marked True if executed.
        # 'tok_list' is list of input tokens for the action.
        # 'controlInput marks input control pins.
        self.actdict = {'initialNode' : {'stdin' : 'auto',
                                      'next_step' : ['Procedure1']
                                     },
                        'Procedure1' : {'controlInput' : {'source' : 'initialNode', 
                                                      'min_tok'  : 1, 
                                                      'max_tok'  : 1,
                                                      'tok_list' : []},
                                        'method' : self.Procedure1,
                                        'next_step' : 'Procedure2',
                                        'exec_status' : "Current"
                                       },
                        'Procedure2' : {'argument' : {'source'  : 'Procedure1', 
                                                      'min_tok'  : 1,
                                                      'max_tok'  : 3,
                                                      'tok_list' : []},
                                        'method' : self.Procedure2,
                                        'next_step' : 'Procedure3',
                                        'exec_status' : "Current"
                                       },
                        'Procedure3' : {'argument' : {'source'  : 'Procedure2', 
                                                      'min_tok'  : 1,
                                                      'max_tok'  : 5,
                                                      'tok_list' : []},
                                        'method' : self.Procedure3,
                                        'next_step' : 'return output parameters',
                                        'exec_status' : "Current"
                                       },
                        'final' : {'controlInput' : {'source'  : 'Procedure3', 
                                                     'min_tok'  : 1,
                                                     'max_tok'  : 1,
                                                     'tok_list' : []},
                                   'method' : self.final,
                                   'next_step' : None,
                                   'exec_status' : "Current"
                                  },
                        'result' : {'stdout' : 'no',   # If yes returns output and exit, else query
                                    'final' : {'source' : 'Procedure3', 
                                               'tok_list' : []}
                                   }
                         }
        
        self.result_map_dict = {"initialNode" : ["Procedure1.controlInput"],
                                "Procedure1.result" : ["Procedure2.argument"],
                                "Procedure2.result" : ["Procedure3.argument"],
                                "Procedure3.controlOutput" : ["final.controlInput"],
                                "Procedure3.result" : ["result.final"]}

    def setStepMode(self, newStepMode):
        """
        Overrides base class definition, sets step mode on child activities.
        """
        self.stepMode = newStepMode
        self.__procedure1.setStepMode(newStepMode)
        self.__procedure2.setStepMode(newStepMode)
        self.__procedure3.setStepMode(newStepMode)


    def Procedure1(self):
        """
        Procedure1 action from model.
        """
        if self.inRange('Procedure1', 'controlInput'):
            msg = ""
            inp = self.actdict['Procedure1']['controlInput']['tok_list']
            tok_dict = {'controlInput': inp}
            # run() returns, at minimum, a dictionary with 'status' and 'message' entries
            resdict = self.__procedure1.run(tok_dict)

            if self.__procedure1.isStopped():
                # At this point, run() must have gone through stop(), which
                # resulted in output tokens being passed up in "resdict"
                msg += 'Now exiting %s.  ' % 'Procedure1'
                msg += 'Procedure1 input: %s ; Procedure1 output: %s' % (inp, resdict)
                LOG.info(msg)
                # Rename each output pin to <action>.<outpin>
                for outpin in resdict.keys():
                    resdict["Procedure1.%s" % outpin] = resdict[outpin]
                    del resdict[outpin]
                # Replace message entry in resdict.
                resdict['message'] = msg
        
                self.clear_tok('Procedure1')             
                self.parseResult(resdict)
                return True
            else:
                # Append any message
                resdict['message'] += msg
                self.parseResult(resdict)
                return False
        else:
            insuff_pins = self.wrong_tok('Procedure1')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Procedure1", self.actdict['Procedure1'][pin]['tok_list']))
            return False


    def Procedure2(self):
        """
        Procedure2 action from model.
        """
        if self.inRange('Procedure2', 'argument'):
            msg = ""
            inp = self.actdict['Procedure2']['argument']['tok_list']
            tok_dict = {'argument': inp}
            # run() returns, at minimum, a dictionary with 'status' and 'message' entries
            resdict = self.__procedure2.run(tok_dict)

            if self.__procedure2.isStopped():
                # At this point, run() must have gone through stop(), which
                # resulted in output tokens being passed up in "resdict"
                msg += 'Now exiting %s.  ' % 'Procedure2'
                msg += 'Procedure2 input: %s ; Procedure2 output: %s' % (inp, resdict)
                LOG.info(msg)
                # Rename each output pin to <action>.<outpin>
                for outpin in resdict.keys():
                    resdict["Procedure2.%s" % outpin] = resdict[outpin]
                    del resdict[outpin]
                # Replace message entry in resdict.
                resdict['message'] = msg
        
                self.clear_tok('Procedure2')             
                self.parseResult(resdict)
                return True
            else:
                # Append any message
                resdict['message'] += msg
                self.parseResult(resdict)
                return False
        else:
            insuff_pins = self.wrong_tok('Procedure2')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Procedure2", self.actdict['Procedure2'][pin]['tok_list']))
            return False


    def Procedure3(self):
        """
        Procedure3 action from model.
        """
        if self.inRange('Procedure3', 'argument'):
            msg = ""
            inp = self.actdict['Procedure3']['argument']['tok_list']
            tok_dict = {'argument': inp}
            # run() returns, at minimum, a dictionary with 'status' and 'message' entries
            resdict = self.__procedure3.run(tok_dict)

            if self.__procedure3.isStopped():
                # At this point, run() must have gone through stop(), which
                # resulted in output tokens being passed up in "resdict"
                msg += 'Now exiting %s.  ' % 'Procedure3'
                msg += 'Procedure3 input: %s ; Procedure3 output: %s' % (inp, resdict)
                LOG.info(msg)
                # Rename each output pin to <action>.<outpin>
                for outpin in resdict.keys():
                    resdict["Procedure3.%s" % outpin] = resdict[outpin]
                    del resdict[outpin]
                # Put a token on control output
                resdict['Procedure3.controlOutput'] = [True]
                # Replace message entry in resdict.
                resdict['message'] = msg
        
                self.clear_tok('Procedure3')             
                self.parseResult(resdict)
                return True
            else:
                # Append any message
                resdict['message'] += msg
                self.parseResult(resdict)
                return False
        else:
            insuff_pins = self.wrong_tok('Procedure3')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Procedure3", self.actdict['Procedure3'][pin]['tok_list']))
            return False


    def final(self):
        """
        Final node from model.
        """
        if self.inRange("final", "controlInput"):
            for ikey in self.actdict.keys():
                if 'exec_status' in self.actdict[ikey].keys() and self.actdict[ikey]['exec_status'] == "Continuous":
                    self.actdict[ikey]['exec_status'] = True
            self.actdict['status'] = ['Process1', 'final', 'on']
            self.actdict['message'] = "All actions have executed."
            return True
        else:
            return False


if __name__ == "__main__":
    opt = ActivityBase.ActivityBase.main(LOG)
    impl = Process1Impl.Process1Impl()
    # Instantiate the Activity main class and start it
    a = Process1(impl, opt.stepMode)
    a.run()

