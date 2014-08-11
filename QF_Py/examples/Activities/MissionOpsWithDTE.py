"""
Created on Aug 6, 2010

@author: wye

Contains actdict and determines if inputs are sufficient. Contains 13 action
nodes and a fork node. Will run continuously until told to stop at which point
it will print a list of all output tokens ever produced.
"""

import logging

LOG = logging.getLogger("MissionOpsWithDTELogger")  

from af import ActivityBase
import MissionOpsWithDTEImpl

class MissionOpsWithDTE (ActivityBase.ActivityBase):
     
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
        self.actdict = {'initialNode' : {'stdin' : 'auto',
                                         'next_step' : ['strategicPlanning']
                                         },
                        'strategicPlanning' : {'controlInput' : {'source' : 'initialNode', 
                                                                 'min_tok' : 1, 
                                                                 'max_tok' : 1,
                                                                 'tok_list' : []},
                                               'predictedScience' : {'source' : 'sciSPA', 
                                                                 'min_tok' : 0, 
                                                                 'max_tok' : 100,
                                                                 'tok_list' : []},
                                               'reconciledGroundStation' : {'source' : 'GSStrat', 
                                                                 'min_tok' : 0, 
                                                                 'max_tok' : 100,
                                                                 'tok_list' : []},
                                               'reconciledMission' : {'source' : 'strategicAnalysis', 
                                                                 'min_tok' : 1, 
                                                                 'max_tok' : 100,
                                                                 'tok_list' : []},  
                                               'method' : self.strategicPlanning,
                                               'next_step' : 'sciSPA, GSStrat, and strategicAnalysis or Tactical',
                                               'exec_status' : 'Continuous'
                                               },
                        'strategicAnalysis' : {'reconciledMissionSegment' : {'source' : 'Tactical1', 
                                                                 'min_tok' : 1, 
                                                                 'max_tok' : 1,
                                                                 'tok_list' : []},
                                               'predictedMission' : {'source' : 'strategicPlanning', 
                                                                 'min_tok' : 1, 
                                                                 'max_tok' : 1,
                                                                 'tok_list' : []},
                                               'reconciledScience' : {'source' : 'sciSPA', 
                                                                 'min_tok' : 0, 
                                                                 'max_tok' : 100,
                                                                 'tok_list' : []},
                                               'method' : self.strategicAnalysis,
                                               'next_step' : 'sciSPA and strategicPlanning',
                                               'exec_status' : 'Continuous'
                                               },
                        'Tactical' : {'predictedMissionSegment' : {'source' : 'strategicPlanning', 
                                                                 'min_tok' : 1, 
                                                                 'max_tok' : 1,
                                                                 'tok_list' : []},
                                      'reconciledMissionSegment' : {'source' : 'Tactical1', 
                                                                 'min_tok' : 0, 
                                                                 'max_tok' : 100,
                                                                 'tok_list' : []},
                                      'predictedScience' : {'source' : 'sciTPA', 
                                                             'min_tok' : 0, 
                                                             'max_tok' : 100,
                                                             'tok_list' : []},
                                      'predictedGroundStation' : {'source' : 'GSTact', 
                                                             'min_tok' : 0, 
                                                             'max_tok' : 100,
                                                             'tok_list' : []},  
                                      'method' : self.Tactical,
                                      'next_step' : 'sciTPA, Tactical1, GSTact, and DTETE',
                                      'exec_status' : 'Continuous'
                                      },
                        'Tactical1' : {'actualMission' : {'source' : 'DTETE', 
                                                         'min_tok' : 1, 
                                                         'max_tok' : 1,
                                                         'tok_list' : []},
                                       'predictedMission' : {'source' : 'Tactical', 
                                                         'min_tok' : 3, 
                                                         'max_tok' : 3,
                                                         'tok_list' : []},
                                       'reconciledScience' : {'source' : 'sciTPA', 
                                                         'min_tok' : 0, 
                                                         'max_tok' : 100,
                                                         'tok_list' : []},
                                       'method' : self.Tactical1,
                                       'next_step' : 'sciTPA, strategicAnalysis, and Tactical',
                                       'exec_status' : 'Continuous'
                                       },
                        'DTETE' : {'approvedMissionSegment' : {'source' : 'Tactical', 
                                                                 'min_tok' : 1, 
                                                                 'max_tok' : 1,
                                                                 'tok_list' : []},
                                   'actualFlightSystem' : {'source' : 'GSDownlink', 
                                                             'min_tok' : 0, 
                                                             'max_tok' : 100,
                                                             'tok_list' : []},
                                   'actualGroundStation' : {'source' : 'GSControl', 
                                                             'min_tok' : 0, 
                                                             'max_tok' : 100,
                                                             'tok_list' : []},
                                   'method' : self.DTETE,
                                   'next_step' : 'GSUplink, GSControl, and Tactical1',
                                   'exec_status' : 'Continuous'
                                   },
                        'GSStrat' : {'Requested' : {'source' : 'strategicPlanning', 
                                                             'min_tok' : 1, 
                                                             'max_tok' : 1,
                                                             'tok_list' : []},
                                   'method' : self.GSStrat,
                                   'next_step' : 'strategicPlanning',
                                   'exec_status' : 'Continuous'
                                   },
                        'GSTact' : {'scheduleNeeds' : {'source' : 'Tactical', 
                                                         'min_tok' : 1, 
                                                         'max_tok' : 1,
                                                         'tok_list' : []},
                                   'method' : self.GSTact,
                                   'next_step' : 'Tactical',
                                   'exec_status' : 'Continuous'
                                   },
                        'GSControl' : {'approvedGroundStation' : {'source' : 'DTETE', 
                                                             'min_tok' : 1, 
                                                             'max_tok' : 1,
                                                             'tok_list' : []},
                                       'method' : self.GSControl,
                                       'next_step' : 'DTETE',
                                       'exec_status' : 'Continuous'
                                       },
                        'GSUplink' : {'approvedFlightSystem' : {'source' : 'DTETE', 
                                                             'min_tok' : 1, 
                                                             'max_tok' : 1,
                                                             'tok_list' : []},
                                       'method' : self.GSUplink,
                                       'next_step' : 'GSFlight',
                                       'exec_status' : 'Continuous'
                                       },
                        'GSFlight' : {'uplinkedFlightSystem' : {'source' : 'GSUplink', 
                                                             'min_tok' : 1, 
                                                             'max_tok' : 1,
                                                             'tok_list' : []},
                                       'method' : self.GSFlight,
                                       'next_step' : 'GSDownlink',
                                       'exec_status' : 'Continuous'
                                       },
                        'GSDownlink' : {'downlinkedTimeline' : {'source' : 'GSFlight', 
                                                             'min_tok' : 1, 
                                                             'max_tok' : 1,
                                                             'tok_list' : []},
                                        'method' : self.GSDownlink,
                                        'next_step' : 'DTETE',
                                        'exec_status' : 'Continuous'
                                        },
                        'sciSPA' : {'actualScience' : {'source' : 'strategicAnalysis', 
                                                         'min_tok' : 0, 
                                                         'max_tok' : 100,
                                                         'tok_list' : []},
                                    'reconciledScienceSegment' : {'source' : 'sciTPA', 
                                                         'min_tok' : 1, 
                                                         'max_tok' : 1,
                                                         'tok_list' : []},
                                    'reconciledScience' : {'source' : 'strategicPlanning', 
                                                         'min_tok' : 1, 
                                                         'max_tok' : 100,
                                                         'tok_list' : []},
                                   'method' : self.sciSPA,
                                   'next_step' : 'sciCat, strategicAnalysis, strategicPlanning, and sciTPA',
                                   'exec_status' : 'Continuous'
                                   },
                        'sciTPA' : {'predictedScienceSegment' : {'source' : 'sciSPA', 
                                                         'min_tok' : 1, 
                                                         'max_tok' : 1,
                                                         'tok_list' : []},
                                    'reconciledScience' : {'source' : 'Tactical', 
                                                         'min_tok' : 0, 
                                                         'max_tok' : 100,
                                                         'tok_list' : []},
                                    'actualScience' : {'source' : 'Tactical1', 
                                                         'min_tok' : 2, 
                                                         'max_tok' : 2,
                                                         'tok_list' : []},
                                   'method' : self.sciTPA,
                                   'next_step' : 'Tactical, Tactical1, and sciSPA',
                                   'exec_status' : 'Continuous'
                                   },
                        'sciCat' : {'scienceDataProducts' : {'source' : 'sciSPA', 
                                                         'min_tok' : 1, 
                                                         'max_tok' : 1,
                                                         'tok_list' : []},
                                   'method' : self.sciCat,
                                   'next_step' : None,
                                   'exec_status' : 'Continuous'
                                    },
                        'outputParameter' : {"stdout" : 'no',   # If yes returns output and exit, else query
                                            "final" : {'source' : 'sciCat', 
                                                       'tok_list' : []}}
                         }
        
        self.result_map_dict = {"initialNode" : ["strategicPlanning.controlInput"],
                                "strategicPlanning.reconciledScience" : ["sciSPA.reconciledScience", "outputParameter.final"],
                                "strategicPlanning.requestedGroundStation" : ["GSStrat.Requested"],
                                "strategicPlanning.predictedMission" : ["strategicAnalysis.predictedMission", 
                                                                        "Tactical.predictedMissionSegment"],
                                "strategicAnalysis.actualScience" : ["sciSPA.actualScience", "outputParameter.final"],
                                "strategicAnalysis.reconciledMission" : ["strategicPlanning.reconciledMission"],
                                "Tactical.reconciledScience" : ["sciTPA.reconciledScience", "outputParameter.final"],
                                "Tactical.predictedMission" : ["Tactical1.predictedMission"],
                                "Tactical.approvedGroundStation" : ["GSTact.scheduleNeeds"],
                                "Tactical.approvedMission" : ["DTETE.approvedMissionSegment"],
                                "Tactical1.actualScience" : ["sciTPA.actualScience", "outputParameter.final"],
                                "Tactical1.reconciledMissionSegment" : ["strategicAnalysis.reconciledMissionSegment"],
                                "Tactical1.reconciledMission" : ["Tactical.reconciledMissionSegment"],
                                "DTETE.approvedFlightSystem" : ["GSUplink.approvedFlightSystem", "outputParameter.final"],
                                "DTETE.approvedGroundStation" : ["GSControl.approvedGroundStation"],
                                "DTETE.actualMissionSegment" : ["Tactical1.actualMission"],
                                "GSStrat.Reconciled" : ["strategicPlanning.reconciledGroundStation", "outputParameter.final"],
                                "GSTact.predictedGroundStation" : ["Tactical.predictedGroundStation", "outputParameter.final"],
                                "GSControl.actualGroundStation" : ["DTETE.actualGroundStation", "outputParameter.final"],
                                "GSUplink.uplinkedFlightSystem" : ["GSFlight.uplinkedFlightSystem", "outputParameter.final"],
                                "GSFlight.downlinkedTimeline" : ["GSDownlink.downlinkedTimeline", "outputParameter.final"],
                                "GSDownlink.flightSystemTimeline" : ["DTETE.actualFlightSystem", "outputParameter.final"],
                                "sciSPA.scienceDataProducts" : ["sciCat.scienceDataProducts", "outputParameter.final"],
                                "sciSPA.reconciledScience1" : ["strategicAnalysis.reconciledScience"],
                                "sciSPA.predictedScience" : ["strategicPlanning.predictedScience"],
                                "sciSPA.predictedScienceSegment" : ["sciTPA.predictedScienceSegment"],
                                "sciTPA.predictedScience" : ["Tactical.predictedScience", "outputParameter.final"],
                                "sciTPA.reconciledScience" : ["Tactical1.reconciledScience"],
                                "sciTPA.reconciledScienceSegment" : ["sciSPA.reconciledScienceSegment"]}
    
  
    def strategicPlanning(self):
        """
        strategicPlanning action from model.
        """
        if self.inRange('strategicPlanning', 'controlInput') or\
          self.inRange('strategicPlanning', 'reconciledMission'):   # Required token inputs.
            if self.inRange('strategicPlanning', 'predictedScience') or\
              self.inRange('strategicPlanning', 'reconciledGroundMission'):  # Optional token inputs.
                r = self.__impl.strategicPlanningBehav(argument1=self.actdict['strategicPlanning']['reconciledMission']['tok_list'],
                                                       argument2=self.actdict['strategicPlanning']['predictedScience']['tok_list'],
                                                       argument3=self.actdict['strategicPlanning']['reconciledGroundStation']['tok_list'])
                r['status'] = ['MissionOpsWithDTE', 'strategicPlanning', 'on']
                
                self.clear_tok('strategicPlanning')
                self.parseResult(r)
                return True
        else:
            insuff_pins = self.wrong_tok('strategicPlanning')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "strategicPlanning", self.actdict['strategicPlanning'][pin]['tok_list']))
            return False 


    def strategicAnalysis(self):
        """
        strategicAnalysis action from model.
        """
        if self.inRange('strategicAnalysis', 'reconciledMissionSegment') and\
          self.inRange('strategicAnalysis', 'predictedMission'):
            if self.inRange('strategicAnalysis', 'reconciledScience'):
                r = self.__impl.strategicAnalysisBehav(argument1=self.actdict['strategicAnalysis']['reconciledMissionSegment']['tok_list'],
                                                       argument2=self.actdict['strategicAnalysis']['predictedMission']['tok_list'],
                                                       argument3=self.actdict['strategicAnalysis']['reconciledScience']['tok_list'])
                r['status'] = ['MissionOpsWithDTE', 'strategicAnalysis', 'on']
                
                self.clear_tok('strategicAnalysis')
                self.parseResult(r)
                return True
        else:
            insuff_pins = self.wrong_tok('strategicAnalysis')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "strategicAnalysis", self.actdict['strategicAnalysis'][pin]['tok_list']))
            return False
    
    
    def Tactical(self):
        """
        Tactical action from model.
        """
        if self.inRange('Tactical', 'predictedMissionSegment'):
            if self.inRange('Tactical', 'reconciledMissionSegment') or\
              self.inRange('Tactical', 'predictedScience') or\
              self.inRange('Tactical', 'predictedGroundStation'):
                counter = 1
#                while counter <= 2:
                r = self.__impl.tacticalBehav(argument1=self.actdict['Tactical']['predictedMissionSegment']['tok_list'],
                                              argument2=self.actdict['Tactical']['reconciledMissionSegment']['tok_list'],
                                              argument3=self.actdict['Tactical']['predictedScience']['tok_list'],
                                              argument4=self.actdict['Tactical']['predictedGroundStation']['tok_list'])
                r['status'] = ['MissionOpsWithDTE', 'Tactical', 'on']
                
                self.parseResult(r)
                counter = counter + 1
                self.clear_tok('Tactical')
                return True
        else:
            insuff_pins = self.wrong_tok('Tactical')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Tactical", self.actdict['Tactical'][pin]['tok_list']))
            return False
        
        
    def Tactical1(self):
        """
        Tactical1 action from model.
        """
        if self.inRange('Tactical1', 'actualMission') and\
          self.inRange('Tactical1', 'predictedMission'):
            if self.inRange('Tactical1', 'reconciledScience'):
                r = self.__impl.tactical1Behav(argument1=self.actdict['Tactical1']['actualMission']['tok_list'],
                                               argument2=self.actdict['Tactical1']['predictedMission']['tok_list'],
                                               argument3=self.actdict['Tactical1']['reconciledScience']['tok_list'])
                r['status'] = ['MissionOpsWithDTE', 'Tactical1', 'on']
                
                self.clear_tok('Tactical1')
                self.parseResult(r)
                return True
        else:
            insuff_pins = self.wrong_tok('Tactical1')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "Tactical1", self.actdict['Tactical1'][pin]['tok_list']))
            return False 
    
    
    def DTETE(self):
        """
        DTETE action from model.
        """
        if self.inRange('DTETE', 'approvedMissionSegment'):
            if self.inRange('DTETE', 'actualFlightSystem') and\
              self.inRange('DTETE', 'actualGroundStation'):
                r = self.__impl.DTETEBehav(argument1=self.actdict['DTETE']['approvedMissionSegment']['tok_list'],
                                           argument2=self.actdict['DTETE']['actualFlightSystem']['tok_list'],
                                           argument3=self.actdict['DTETE']['actualGroundStation']['tok_list'])
                r['status'] = ['MissionOpsWithDTE', 'DTETE', 'on']
                
                self.clear_tok('DTETE')
                self.parseResult(r)
                return True
        else:
            insuff_pins = self.wrong_tok('DTETE')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "DTETE", self.actdict['DTETE'][pin]['tok_list']))
            return False
    
    
    def GSStrat(self):  
        """
        GSStrat action from model.
        """
        if self.inRange('GSStrat', 'Requested'): 
            r = self.__impl.GSStratBehav(argument=self.actdict['GSStrat']['Requested']['tok_list'])
            r['status'] = ['MissionOpsWithDTE', 'GSStrat', 'on']
            
            self.clear_tok('GSStrat')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('GSStrat')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "GSStrat", self.actdict['GSStrat'][pin]['tok_list']))
            return False
        
    def GSTact(self):  
        """
        GSTact action from model.
        """
        if self.inRange('GSTact', 'scheduleNeeds'): 
            r = self.__impl.GSTactBehav(argument=self.actdict['GSTact']['scheduleNeeds']['tok_list'])
            r['status'] = ['MissionOpsWithDTE', 'GSTact', 'on']
            
            self.clear_tok('GSTact')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('GSTact')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "GSTact", self.actdict['GSTact'][pin]['tok_list']))
            return False
        
        
    def GSControl(self):  
        """
        GSControl action from model.
        """
        if self.inRange('GSControl', 'approvedGroundStation'): 
            r = self.__impl.GSControlBehav(argument=self.actdict['GSControl']['approvedGroundStation']['tok_list'])
            r['status'] = ['MissionOpsWithDTE', 'GSControl', 'on']
            
            self.clear_tok('GSControl')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('GSControl')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "GSControl", self.actdict['GSControl'][pin]['tok_list']))
            return False
        
        
    def GSUplink(self):  
        """
        GSUplink action from model.
        """
        if self.inRange('GSUplink', 'approvedFlightSystem'): 
            r = self.__impl.GSUplinkBehav(argument=self.actdict['GSUplink']['approvedFlightSystem']['tok_list'])
            r['status'] = ['MissionOpsWithDTE', 'GSUplink', 'on']
            
            self.clear_tok('GSUplink')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('GSUplink')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "GSUplink", self.actdict['GSUplink'][pin]['tok_list']))
            return False
        
        
    def GSFlight(self):  
        """
        GSFlight action from model.
        """
        if self.inRange('GSFlight', 'uplinkedFlightSystem'): 
            r = self.__impl.GSFlightBehav(argument=self.actdict['GSFlight']['uplinkedFlightSystem']['tok_list'])
            r['status'] = ['MissionOpsWithDTE', 'GSFlight', 'on']
            
            self.clear_tok('GSFlight')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('GSFlight')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "GSFlight", self.actdict['GSFlight'][pin]['tok_list']))
            return False
        
        
    def GSDownlink(self):  
        """
        GSDownlink action from model.
        """
        if self.inRange('GSDownlink', 'downlinkedTimeline'): 
            r = self.__impl.GSDownlinkBehav(argument=self.actdict['GSDownlink']['downlinkedTimeline']['tok_list'])
            r['status'] = ['MissionOpsWithDTE', 'GSDownlink', 'on']
            
            self.clear_tok('GSDownlink')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('GSDownlink')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "GSDownlink", self.actdict['GSDownlink'][pin]['tok_list']))
            return False   
    
    
    def sciSPA(self):  
        """ 
        sciSPA action from model.
        """
        if self.inRange('sciSPA', 'reconciledScienceSegment') and\
          self.inRange('sciSPA', 'reconciledScience'):
            if self.inRange('sciSPA', 'actualScience'):
                r = self.__impl.sciSPABehav(argument1=self.actdict['sciSPA']['actualScience']['tok_list'],
                                            argument2=self.actdict['sciSPA']['reconciledScienceSegment']['tok_list'],
                                            argument3=self.actdict['sciSPA']['reconciledScience']['tok_list'])
                r['status'] = ['MissionOpsWithDTE', 'sciSPA', 'on']
                
                self.clear_tok('sciSPA')
                self.parseResult(r)
                return True
        else:
            insuff_pins = self.wrong_tok('sciSPA')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "sciSPA", self.actdict['sciSPA'][pin]['tok_list']))
            return False
    
    
    def sciTPA(self):  
        """ 
        sciTPA action from model.
        """
        if self.inRange('sciTPA', 'predictedScienceSegment') and\
          self.inRange('sciTPA', 'actualScience'):
            if self.inRange('sciTPA', 'reconciledScience'):
                r = self.__impl.sciTPABehav(argument1=self.actdict['sciTPA']['predictedScienceSegment']['tok_list'],
                                            argument2=self.actdict['sciTPA']['reconciledScience']['tok_list'],
                                            argument3=self.actdict['sciTPA']['actualScience']['tok_list'])
                r['status'] = ['MissionOpsWithDTE', 'sciTPA', 'on']
                
                self.clear_tok('sciTPA')
                self.parseResult(r)
                return True
        else:
            insuff_pins = self.wrong_tok('sciTPA')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "sciTPA", self.actdict['sciTPA'][pin]['tok_list']))
            return False
    
    
    def sciCat(self):  
        """
        sciCat action from model.
        """
        if self.inRange('sciCat', 'scienceDataProducts'): 
            r = self.__impl.sciCatBehav(argument=self.actdict['sciCat']['scienceDataProducts']['tok_list'])
            r['status'] = ['MissionOpsWithDTE', 'sciCat', 'on']
            
            self.clear_tok('sciCat')
            self.parseResult(r)
            return True    
        else:
            insuff_pins = self.wrong_tok('sciCat')
            for pin in insuff_pins:
                LOG.debug('Incorrect token number on input %s of action %s:  %s'
                          % (pin, "sciCat", self.actdict['sciCat'][pin]['tok_list']))
            return False


if __name__ == "__main__":
    opt = ActivityBase.ActivityBase.main(LOG)
    # Instantiate the impl Activity class
    impl = MissionOpsWithDTEImpl.MissionOpsWithDTEImpl()
    # Instantiate the Activity main class and start it
    a = MissionOpsWithDTE(impl, opt.stepMode)
    a.run()
