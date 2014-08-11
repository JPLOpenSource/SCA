'''
Created on Jun 28, 2010

@author: wye, reder, owen

4th generation version of base class. Contains common functions to be used by
all activity diagrams as well as the execution control functions: start(), 
step(), stop(), and run().
'''
import sys
import logging
from qf import active

LOG = logging.getLogger("ActivityBase")

class ActivityBase(active.Active):

    def __init__(self, logger=None, stepMode=True):
        """
        Constructor
        """
        active.Active.__init__(self)    # Needs to be commented out if not running SignalExample.
        
        if logger is not None:
            LOG = logger
        self.actdict = dict()
        self.result_map_dict = dict()
        self.stepMode = stepMode
        
        # Keeps track of whether this activity instance has "started".
        self.__instStarted = False
        self.__instStopped = False

    def setStepMode(self, stepMode):
        self.stepMode = stepMode

    def min_tok(self, step, input):
        return self.actdict[step][input]['min_tok']
    
    def max_tok(self, step, input):
        return self.actdict[step][input]['max_tok']
    
    def tok_count(self, step, input):
        return len(self.actdict[step][input]['tok_list'])
    
    def inRange(self, step, input):
        """
        Returns True if input pins are satisfied.
        """
        if self.min_tok(step, input) <= self.tok_count(step, input)\
                and self.max_tok(step, input) >= self.tok_count(step, input):
            return True
        
    def clear_tok(self, dict_key):
        """
        Clears tok_list for given dict_key.
        """
        for input in self.actdict[dict_key].keys():
            if type(self.actdict[dict_key][input]) == dict:
                if len(self.actdict[dict_key][input]['tok_list']) != 0:
                    self.actdict[dict_key][input]['tok_list'] = []
                    
    def wrong_tok(self, dict_key):
        """
        Returns list of input pins that are not satisfied.
        """
        insuffPin = []
        for input in self.actdict[dict_key].keys():
            if type(self.actdict[dict_key][input]) == dict:
                if not self.inRange(dict_key, input):
                    insuffPin.append(input)
        return insuffPin
    
    
    def parseResult(self, r):      
        """
        Parse the result dict from impl behavior to the action dictionary.
        """
        bad_key = True
        for resdict_key in r.keys():  # r = resdict
            for result_key in self.result_map_dict.keys():
                if resdict_key == result_key:
                    bad_key = False
                    for output in self.result_map_dict[result_key]:
                        k = output.split('.')
                        step = k[0]
                        arg = k[-1]
                        self.actdict[step][arg]['tok_list'].extend(r[resdict_key])
        # Create temporary storage area for message and status in actdict.
        for otherAttribute in ['message', 'status']:
            if otherAttribute in r.keys():
                self.actdict[otherAttribute] = r[otherAttribute]
                bad_key = False
        if bad_key:  # If there is a bad key.
            LOG.error('ERROR: Result keys do not match!.')


    def isDone(self):
        '''
        Check to see whether all executable actions have finished.
        '''
        self.__all_done = True  # Assume all done at first.
        for ikey in self.actdict.keys():
            if 'method' in self.actdict[ikey].keys() and\
                    'exec_status' in self.actdict[ikey].keys():
                if not (self.actdict[ikey]['exec_status'] == True or\
                        self.actdict[ikey]['exec_status'] == 'Inactive'):
                    self.__all_done = False  # so we're NOT all done.
                    break
        return self.__all_done


    def isStopped(self):
        return self.__instStopped

    
    def start(self, tok_dict):
        """
        This function collects input tokens and maps them to the correct input pins.
        """
        if self.__instStarted:
            return

        # Clears all tok_lists.
        for key in self.actdict.keys():
            self.clear_tok(key)
        
        # For when user needs to input parameters in the MagicDraw animator. 
        # Maps tok_dict to the input pins of the first action(s).
        if tok_dict == None:
            pass
        elif type(tok_dict) == dict:
            # Find first action after input parameter.
            for key in self.actdict.keys():
                for input in self.actdict[key].keys():
                    if input == 'stdin':
                        for action in self.actdict[key]['next_step']:
                            # Find first action's input pin.
                            for arg in self.actdict[action].keys():
                                if arg in tok_dict.keys():
                                    LOG.info('Now entering called action %s' % action)
                                    self.actdict[action][arg]['tok_list'] = tok_dict[arg]
            tok_dict = None
        else:
            LOG.error('ERROR: Cannot pass on token list: %s' % tok_dict)
            
        # Get input parameter.
        for ikey in self.actdict.keys():
            if 'stdin' in self.actdict[ikey].keys():
                # If activity only requires a control input (if stdin is set to 
                # 'auto'), will automatically set token for first action to be True.         
                if self.actdict[ikey]['stdin'] == 'auto':
                    # Find first action.
                    for stepkey in self.actdict.keys():
                        for input in self.actdict[stepkey].keys():
                            if input == 'stdin':
                                for action in self.actdict[stepkey]['next_step']:
                                    if 'controlInput' in self.actdict[action].keys():
                                        self.actdict[action]['controlInput']['tok_list'].append(True)
                                            
                # If another action provides the input parameter or if inputing
                # parameter from MD.
                elif self.actdict[ikey]['stdin'] == 'no':
                    pass
                
                # If none of the above options are entered.
                else:
                    LOG.error('ERROR: Do not recognize stdin status!')

        if self.stepMode:
            print "Enter a.step() to step through the activity."                            
        # Activity instance has started.
        self.__instStarted = True


    def stop(self, showOutcome=None):
        '''
        Returns result dict of action name and tok_lists if stdout is yes or if
        ans == yes. Otherwise, returns nothing.
        '''
        retDict = dict()
        if self.__instStopped:
            return retDict

        # showOutcome is True if answer should be printed and False otherwise.
        ans = showOutcome
        for ikey in self.actdict.keys():
            if 'stdout' in self.actdict[ikey].keys():
                # Store output tokens to be passed up
                retDict[ikey] = self.actdict[ikey]['final']['tok_list']
                if self.actdict[ikey]['stdout'] == 'yes':
                    pass
                # If 'stdout' is 'no', then query.
                elif self.actdict[ikey]['stdout'] == 'no':
                    while ans is None:
                        print 'Print output parameters?'
                        ans = sys.stdin.readline()
                        ans = ans.strip()
                        if ans == 'yes':
                            ans = True
                        elif ans == 'no':
                            ans = False
                        else:
                            ans = None
                else:
                    LOG.error('ERROR: Do not recognize stdout status!')
        if ans:
            if not self.stepMode:
                print retDict

        self.__instStopped = True
        return retDict
    

    def run(self, tok_dict=None):
        """
        This function runs the entire activity executable, unless stepMode is 
        True, in which case it will terminate after mapping the input parameters.
        """
        wasStarted = self.__instStarted
        self.start(tok_dict)
        # Executes if stepMode is True.
        if self.stepMode:
            if wasStarted:  # After the first time thru "run".
                if self.isDone():
                    return self.stop()
                else:
                    return self.step()
            else:  # No step if we just did a start.
                return {'status' : "",
                        'message' : "Activity started"}
        else:
            while True:
                self.step()
                self.__all_done = self.isDone()
                if self.__all_done:  # If all actions are done.
                    break
            return self.stop()


    def step(self):
        """
        Single step through a run.
        
        Each step returns a dictionary containing entries that the caller needs:
        = "status" : indication of which action is currently "on" (i.e., active)
        = "message" : logged messages from executing the step
        """
        retDict = {"status" : "", "message" : ""}

        if self.isDone():  # If all actions are done.
            retDict["message"] = 'All executable actions have finished.'
            return retDict
        
        # Loops through exec_status and activates step that has "Current"
        # or "Continuous" status.
        for ikey in self.actdict.keys():
            if 'method' in self.actdict[ikey].keys() and\
                    'exec_status' in self.actdict[ikey].keys():
                if self.actdict[ikey]['exec_status'] == "Current" or self.actdict[ikey]['exec_status'] == "Continuous":
                    actioned = self.actdict[ikey]['method']()  # Runs though method.
                    
                    # Process message and status return values.
                    for otherAttribute in ['message', 'status']:
                        if otherAttribute in self.actdict.keys():
                            retDict[otherAttribute] = self.actdict[otherAttribute]
                            del self.actdict[otherAttribute]
                    if actioned:
                        if self.actdict[ikey]['exec_status'] != "Continuous":
                            self.actdict[ikey]['exec_status'] = True
                        break

        if len(retDict['message']) == 0:
            retDict['message'] = "ERROR! Nothing executed in step!"
        return retDict
        
        
    def decision(self, dict, map):
        '''
        Decision node maps token to input pin of satisfied condition's action.
        '''
        if len(dict['decisionValue']['tok_list']) == 0:
            LOG.error("ERROR! No decision value supplied to decision!")

        token = dict['decisionValue']['tok_list'][0]
        del dict['decisionValue']['tok_list'][0]

        trueBranches = list()
        
        for condition in map.keys():
            if condition(token) == True:    
                trueBranches.extend(map[condition]['destin'])
                for destin in map[condition]['destin']:
                    k = destin.split('.')
                    step = k[0]
                    arg = k[-1]
                    self.actdict[step][arg]['tok_list'].append(token)
                self.actdict['message'] = 'True branches: %s' % trueBranches
            else:
                false_cond = map[condition]['func']
                self.actdict[false_cond]['exec_status'] = 'Inactive'

        if len(trueBranches) > 1:
            LOG.warning("Warning! More than one condition is true: %s" % trueBranches)
   

    @staticmethod
    def main(logObj=LOG):
        """
        Utility method for common setup routine of the Activity executables.
        """
        from optparse import OptionParser
    
        usage = "Usage:  %prog [options]"
        vers  = "%prog 0.1a"
        parser = OptionParser(usage, version=vers)
        parser.add_option("-l", "--level", dest="logLevel",
                          help="Logger level: FATAL/ERROR/WARNING/INFO/DEBUG/NOTSET",
                          action="store", default="INFO")
        parser.add_option("-L", "--log", "--logfile", dest="log", type="string",
                          help="Log all console output to a file. (default: %s)." % None)
        parser.add_option("-s", "--step", dest="stepMode",
                          help="Turn step mode on",
                          action="store_true", default=False)
                      
        (opt, args) = parser.parse_args()
        args  # gets rid of unused var warning
    
        # Enable logger as info messages only.
        logObj.setLevel(eval("logging.%s" % opt.logLevel))
        # Log to stdout and stderr.
        if opt.log is None:
            logger_output_handler = logging.StreamHandler(sys.stdout)
        else:
            logger_output_handler = logging.StreamHandler(opt.log)
        # Include only message in output.
        logger_formatter = logging.Formatter('%(message)s')
        logger_output_handler.setFormatter(logger_formatter)
        logObj.addHandler(logger_output_handler)

        return opt
