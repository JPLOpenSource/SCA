'''
Created on Jun 23, 2010

@author: wye

Inputs and outputs lists
'''

from af import ActivityBase

        
class behav():
    '''
    Describes behavior of acts
    '''
    
    def __init__(self):
        '''
        Constructor
        '''
        self._actdict = None
        
    def setActDict(self, actdict):
        self._actdict = actdict
        
    def setResList(self, reslist):
        self._resdict = reslist
        

    def act1Behav(self):
        self._resdict['result1'] = list(self._actdict['act1']['input1']['state1']).pop()
        print 'Output 1:', self._resdict['result1']            
        
    def act2Behav(self):
        self._resdict['result2'] = self._resdict['result1'].append('24')
        print 'Output 2:', self._resdict['result2']
        
    def act3Behav(self):
        self._resdict['result3'] = self._resdict['result2'] * 2 
        print 'Output 3:', self._resdict['result3']
        
    def act4Behav(self):
        self._resdict['result4'] = self._resdict['result3'].pop()
        print 'Output 4:', self._resdict['result4']    
    
    def act5Behav(self):
        self._resdict['result5'] = self._resdict['result3'].pop()
        print 'Output 5:', self._resdict['result5']
        return self._resdict
        

class CapEx(ActivityBase.ActivityBase):

    def __init__(self, impl):
        '''
        Constructor
        '''
        ActivityBase.ActivityBase.__init__(self)

        self.__impl = impl
        self._actdict = {'userinput':'yes', 
                         'stepno':1,
                         'act1':{'input1':{'source1':'initial node', 'state1':'false'}}, 
                         'act2':{'input2':{'source2':'Generate Process Input Parameters', 'state2':list}}, 
                         'act3':{'input3':{'source3':'Process 1', 'state3':list}}, 
                         'act4':{'input4':{'source4':'Process 2', 'state4':list}}, 
                         'act5':{'input5':{'source5':'Process 2', 'state5':list}}}
        self.__impl.setActDict(self._actdict)
        
        self._resdict = {}
        self.__impl.setResList(self._resdict)
    
    
    def step(self, stepno):
        if stepno == 1:
            if type(self._actdict['act1']['input1']['state1']) == str:
                self._actdict['stepno'] = 2
                if self._actdict['userinput'] == 'yes':
                    self._actdict['userinput'] = 'inputted'
                self.act1()
            else:
                print 'Insufficient input 1'
                
        if stepno == 2:
            if type(self._actdict['act2']['input2']['state2']) == type(self._resdict['result1']):
                self._actdict['act2']['input2']['state2'] = self._resdict['result1']
                self._actdict['stepno'] = 3
                self.act2()
            else:
                print 'Insufficient input 2'
                
        if stepno == 3:
            if type(self._actdict['act3']['input3']['state3']) == type(self._resdict['result2']):
                self._actdict['act3']['input3']['state3'] = self._resdict['result2']
                self._actdict['stepno'] = 4
                self.act3()        
            else:
                print 'Insufficient input 3'
        
        if stepno == 4:
            if type(self._actdict['act4']['input4']['state4']) == type(self._resdict['result3']):
                self._actdict['act4']['input4']['state4'] = self._resdict['result3']
                self._actdict['act5']['input5']['state5'] = self._resdict['result3']
                self.act4()
                self.act5()
            else:
                print 'Insufficient inputs 4 and 5'
                
    
    def act1(self):
        self.__impl.act1Behav()
        self.run()
        
    def act2(self):
        self.__impl.act2Behav()
        self.run()
        
    def act3(self):
        self.__impl.act3Behav()
        self.run()
        
    def act4(self):
        self.__impl.act4Behav()
    
    def act5(self):
        self.__impl.act5Behav()
    

if __name__ == "__main__":
    impl = behav()
    x = CapEx(impl)
    x.run()

        