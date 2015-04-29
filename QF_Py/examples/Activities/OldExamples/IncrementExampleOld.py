'''
Created on Jun 28, 2010

@author: wye

True/false inputs and pring outputs
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

    def act1Behav(self):
        self._actdict['act2']['input2']['state2'] = True         
        print 'Process 1 finished'            
        
    def act2Behav(self):
        self._actdict['act3']['input3']['state3'] = self._actdict['act3']['input3']['state3'] + 1
        print self._actdict['act3']['input3']['state3']
        
    def act3Behav(self):
        print 'Process 2 finished'    
        

class CapEx(ActivityBase.ActivityBase):

    def __init__(self, impl):
        '''
        Constructor
        '''
        ActivityBase.ActivityBase.__init__(self)

        self.__impl = impl
        self._actdict = {'userinput':'no', 
                         'stepno':1,
                         'act1':{'input1':{'source1':'initial node', 'state1':'false'}}, 
                         'act2':{'input2':{'source2':'Count = 0', 'state2':'false'}}, 
                         'act3':{'input3':{'source3':'Count = Count + 1', 'state3':0}}}
        self.__impl.setActDict(self._actdict)
        
        
    def step(self, stepno):
        if stepno == 1:
            if self._actdict['act1']['input1']['state1'] == True:
                self._actdict['stepno'] = 2
                if self._actdict['userinput'] == 'yes':
                    self._actdict['userinput'] = 'inputted'
                self.act1()
            else:
                print 'Insufficient input 1'
                
        if stepno == 2:
            if self._actdict['act2']['input2']['state2'] == True:
                self._actdict['stepno'] = 3
                self.act2()
            else:
                print 'Insufficient input 2'
                
        if stepno == 3:
            if self._actdict['act3']['input3']['state3'] == 10:
                self.act3()        
            else:
                self.act2()
                
    
    def act1(self):
        self.__impl.act1Behav()
        self.run()
        
    def act2(self):
        self.__impl.act2Behav()
        self.run()
        
    def act3(self):
        self.__impl.act3Behav()
    

if __name__ == "__main__":
    impl = behav()
    x = CapEx(impl)
    x.run()

        