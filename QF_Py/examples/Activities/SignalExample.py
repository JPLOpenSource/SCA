"""
Created on Aug 23, 2010

@author: wye

Contains actdict and determines if inputs are sufficient. Contains signals that
interact with state machines.
"""

import logging
import threading

LOG = logging.getLogger("SignalExampleLogger")  

from af import ActivityBase
from qf import event
from qf import framework
import SignalExampleImpl

class SignalExample (ActivityBase.ActivityBase):
     
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

        self._subscribe("SystemOn")

        # 'exec_status' is initially marked "Current" or "Continuous" (if the 
        # action will run multiple times) and marked True if executed.
        # 'tok_list' is list of input tokens for the action.
        # 'controlInput marks input control pins.
        # Set stdin to 'auto' and first input pin to 'controlInput' if action 
        # starts by control flow.
        self.actdict = {'inputParameter' : {'stdin' : 'auto',
                                            'next_step' : ['TurnON']
                                            },
                        'TurnON' : {'controlInput' : {'source' : 'inputParameter', 
                                                         'min_tok' : 1, 
                                                         'max_tok' : 1,
                                                         'tok_list' : []},
                                           'method' : self.TurnON,
                                           'next_step' : 'Action',
                                           'exec_status' : 'Current'
                                           },
                        'Action' : {'argument' : {'source' : 'TurnON', 
                                                 'min_tok' : 1,
                                                 'max_tok' : 1,
                                                 'tok_list' : []},
                                   'method' : self.Action,
                                   'next_step' : 'return output parameters',
                                   'exec_status' : 'Current'
                                   },
                        'outputParameter': {'stdout' : 'no',   # If yes returns output and exit, else query
                                            'final' : {'source' : 'Action', 
                                                       'tok_list' : []}
                                            }
                         }
        
        self.result_map_dict = {"inputParameter" : ["TurnON.controlInput"],
                                "TurnON.signalValue" : ["Action.argument"],
                                "Action.result" : ["outputParameter.final"]  }
    
    def TurnON(self):
        """
        TurnON action from model.
        """
        if self.inRange('TurnON', 'controlInput'):
            r = self.__impl.turnONBehav(argument=self.actdict['TurnON']['controlInput']['tok_list'])
            r['status'] = ['SignalExample', 'TurnON', 'on']
            
            self.clear_tok('TurnON')
            self.parseResult(r)
            
            # Send ON signal to state machine Simple4.
            e = event.Event("OnSignal")
            e.data = r['TurnON.signalValue'][0]
            self._publish(e)
            # Wait for and receive signal from state machine Simple4.
            eventObj = self._receiveOneEvent()
            LOG.info("Got event of signal '%s' with data '%s'" % (eventObj.signal, eventObj.data))
            return True    
        else:
            insuff_pins = self.wrong_tok('TurnON')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "TurnON", self.actdict['TurnON'][pin]['tok_list']))
            return False
            

    def Action(self):
        """
        Action action from model.
        """
        if self.inRange('Action', 'argument'): 
            r = self.__impl.actionBehav(argument=self.actdict['Action']['argument']['tok_list'])
            r['status'] = ['SignalExample', 'Action', 'on']
            
            self.clear_tok('Action')
            self.parseResult(r)
            self._publish(event.Event("OffSignal"))
            return True    
        else:
            insuff_pins = self.wrong_tok('Action')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Action", self.actdict['Action'][pin]['tok_list']))
            return False


if __name__ == "__main__":
    # Initialize the Quantum Framework for state machine interactions
    qf = framework.QF.getInstance()
    qf.init(qf_gui=False)
    # Invoke common activity main
    opt = ActivityBase.ActivityBase.main(LOG)
    # Instantiate the impl Activity class
    impl = SignalExampleImpl.SignalExampleImpl()
    # Instantiate the Activity main class and start it
    a = SignalExample(impl, opt.stepMode)
    
    # A kludge:  Start the related state machine
    def smSpawner():
        import os
        import sys
        import sim_state_start
        sim_state_start.main([], nogui_flag=False,
                             path=os.sep.join([sys.path[0], "DemoSM", "autocode"]))
    threading.Thread(target=smSpawner).start()
    
    # Finally, start the activity executable
    a.run()
