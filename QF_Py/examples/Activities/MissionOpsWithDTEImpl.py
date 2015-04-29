"""
Created on Aug 6, 2010

@author: wye

User input section
"""
import logging
LOG = logging.getLogger("MissionOpsWithDTEImplLogger")

class MissionOpsWithDTEImpl(object):
    """
    Describes behavior of acts
    """
     
    def __init__(self):
        '''
        Constructor
        '''
        # If history is set to True, then will only return last 5 entries in 
        # output. Else, returns entire output.
        self.history = True
        if self.history == True:
            LOG.info("History setting is on.")
        else:
            LOG.info("History setting is off.")
    
    
    def connect(self, argument):
        if argument != []:
            for i in argument:
                arg = [] + i
        else:
            arg = []
        return arg
    
    
    def list_append(self, activity, arg):
        largest_no = 1
        if arg == []:
            tok = [activity + '.1']
        else:
            for event in arg:
                if type(event) == list:
                    for subevent in event:
                        k = subevent.split('.')
                        event_name = k[0]
                        event_iter = k[-1]
                        if event_name == activity:
                            if event_iter >= largest_no:    # If action has already been executed.
                                largest_no = int(event_iter) + 1
            tok = event + [activity + '.' + str(largest_no)]
            if largest_no == 1: # If activity has not been executed.
                tok = event + [activity + '.1']
        if self.history == True:
            return tok[-5:]
        else:
            return tok


    def strategicPlanningBehav(self, argument1, argument2, argument3):
        """
        User written behavior
        """
        # Connect all input tokens together to be a list within a list.
        arg1 = self.connect(argument1)
        arg2 = self.connect(argument2)
        arg3 = self.connect(argument3)
        argument = [arg1 + arg2 + arg3]
                    
        tok = self.list_append('strategicPlanningTimeEvent', argument)
                    
        resdict = {'strategicPlanning.reconciledScience' : [tok],
                   'strategicPlanning.requestedGroundStation' : [tok],
                   'strategicPlanning.predictedMission' : [tok]}              
        
        message = 'strategicPlanning input: %s ; strategicPlanning output: %s' % (argument, [tok])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict


    def strategicAnalysisBehav(self, argument1, argument2, argument3):
        """
        User written behavior
        """
        # Connect all input tokens together to be a list within a list.
        arg1 = self.connect(argument1)
        arg2 = self.connect(argument2)
        arg3 = self.connect(argument3)
        argument = [arg1 + arg2 + arg3]
        
        tok = self.list_append('strategicAnalysis', argument)
            
        resdict = {'strategicAnalysis.actualScience' : [tok],
                   'strategicAnalysis.reconciledMission' : [tok]}
        
        message = 'strategicAnalysis input: %s ; strategicAnalysis output: %s' % (argument, [tok])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict
    
    
    def tacticalBehav(self, argument1, argument2, argument3, argument4):
        """
        User written behavior
        """
        # Connect all input tokens together to be a list within a list.
        arg1 = self.connect(argument1)
        arg2 = self.connect(argument2)
        arg3 = self.connect(argument3)
        arg4 = self.connect(argument4)
        argument = [arg1 + arg2 + arg3 + arg4]
        
        tok = self.list_append('Tactical', argument)
            
        resdict = {'Tactical.reconciledScience' : [tok],
                   'Tactical.predictedMission' : [tok, tok, tok],
                   'Tactical.approvedGroundStation' : [tok],
                   'Tactical.approvedMission' : [tok]}              
        
        message = 'Tactical input: %s ; Tactical output: %s' % (argument, [tok])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict
    
    
    def tactical1Behav(self, argument1, argument2, argument3):
        """
        User written behavior
        """
        # Connect all input tokens together to be a list within a list.
        arg1 = self.connect(argument1)
        arg2 = self.connect(argument2)
        arg3 = self.connect(argument3)
        argument = [arg1 + arg2 + arg3]
        
        tok = self.list_append('Tactical1', argument)
            
        resdict = {'Tactical1.actualScience' : [tok, tok],
                   'Tactical1.reconciledMissionSegment' : [tok],
                   'Tactical1.reconciledMission' : [tok]}              
        
        message = 'Tactical1 input: %s ; Tactical1 output: %s' % (argument, [tok])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict
    
    
    def DTETEBehav(self, argument1, argument2, argument3):
        """
        User written behavior
        """
        # Connect all input tokens together to be a list within a list.
        arg1 = self.connect(argument1)
        arg2 = self.connect(argument2)
        arg3 = self.connect(argument3)
        argument = [arg1 + arg2 + arg3]
        
        tok = self.list_append('DTETE', argument)
            
        resdict = {'DTETE.approvedFlightSystem' : [tok],
                   'DTETE.approvedGroundStation' : [tok],
                   'DTETE.actualMissionSegment' : [tok]}              
        
        message = 'DTETE input: %s ; DTETE output: %s' % (argument, [tok])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict
    
    
    def GSStratBehav(self, argument):
        """
        User written behavior
        """
        tok = self.list_append('GSStrat', argument)
        resdict = {'GSStrat.Reconciled' : [tok]}

        message = 'GSStrat input: %s ; GSStrat output: %s' % (argument, [tok])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict
    
    
    def GSTactBehav(self, argument):
        """
        User written behavior
        """
        tok = self.list_append('GSTact', argument)
        resdict = {'GSTact.predictedGroundStation' : [tok]}

        message = 'GSTact input: %s ; GSTact output: %s' % (argument, [tok])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict
    
    
    def GSControlBehav(self, argument):
        """
        User written behavior
        """
        tok = self.list_append('GSControl', argument)
        resdict = {'GSControl.actualGroundStation' : [tok]}

        message = 'GSControl input: %s ; GSControl output: %s' % (argument, [tok])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict
    
    
    def GSUplinkBehav(self, argument):
        """
        User written behavior
        """
        tok = self.list_append('GSUplink', argument)
        resdict = {'GSUplink.uplinkedFlightSystem' : [tok]}

        message = 'GSUplink input: %s ; GSUplink output: %s' % (argument, [tok])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict
    
    
    def GSFlightBehav(self, argument):
        """
        User written behavior
        """
        tok = self.list_append('GSFlight', argument)
        resdict = {'GSFlight.downlinkedTimeline' : [tok]}

        message = 'GSFlight input: %s ; GSFlight output: %s' % (argument, [tok])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict
    
    
    def GSDownlinkBehav(self, argument):
        """
        User written behavior
        """
        tok = self.list_append('GSDownlink', argument)
        resdict = {'GSDownlink.flightSystemTimeline' : [tok]}

        message = 'GSDownlink input: %s ; GSDownlink output: %s' % (argument, [tok])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict
    
    
    def sciSPABehav(self, argument1, argument2, argument3):
        """
        User written behavior
        """
        # Connect all input tokens together to be a list within a list.
        arg1 = self.connect(argument1)
        arg2 = self.connect(argument2)
        arg3 = self.connect(argument3)
        argument = [arg1 + arg2 + arg3]
        
        tok = self.list_append('sciSPA', argument)
        
        resdict = {'sciSPA.scienceDataProducts' : [tok],
                   'sciSPA.reconciledScience1' : [tok],
                   'sciSPA.predictedScience' : [tok],
                   'sciSPA.predictedScienceSegment' : [tok]}              
        
        message = 'sciSPA input: %s ; sciSPA output: %s' % (argument, [tok])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict
    
    
    def sciTPABehav(self, argument1, argument2, argument3):
        """
        User written behavior
        """
        # Connect all input tokens together to be a list within a list.
        arg1 = self.connect(argument1)
        arg2 = self.connect(argument2)
        arg3 = self.connect(argument3)
        argument = [arg1 + arg2 + arg3]
        
        tok = self.list_append('sciTPA', argument)
        
        resdict = {'sciTPA.predictedScience' : [tok],
                   'sciTPA.reconciledScience' : [tok],
                   'sciTPA.reconciledScienceSegment' : [tok]}              
        
        message = 'sciTPA input: %s ; sciTPA output: %s' % (argument, [tok])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message

        return resdict
    
    
    def sciCatBehav(self, argument):
        """
        User written behavior
        """
        tok = self.list_append('sciCat', argument)
        resdict = {'sciCat.flightSystemTimeline' : [tok]}

        message = 'sciCat input: %s ; sciCat output: %s' % (argument, [tok])
        LOG.info(message)
        # Adds message entry into resdict.
        resdict['message'] = message
        
        return resdict
    
    