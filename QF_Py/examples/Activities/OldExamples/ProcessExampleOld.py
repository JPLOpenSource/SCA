'''
Created on Jun 28, 2010

@author: wye
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
        self._actdict['act2']['input2']['list_tok2'] = 1
        self._actdict['act3']['input3']['state3'] = True         
        print 'Generate Capability Metric finished'            
        
    def act2Behav(self):
        self._actdict['act4']['input4']['arg1'] = True    
        print 'Capability finished'
        
    def act3Behav(self):
        self._actdict['act4']['input4']['arg2'] = True 
        print 'Additional Capability finished'    
        
    def act4Behav(self):
        print 'Generate Predictions finished'
        

class ProcessEx(ActivityBase.ActivityBase):

    def __init__(self, impl):
        '''
        Constructor
        '''
        ActivityBase.ActivityBase.__init__(self)

        self.__impl = impl
        self._actdict = {'userinput':'no', 
                         'stepno':1,
                         'act1':{'input1':{'source1':'initial node', 'state1':'false'},
                                 'result1':'out1'}, 
                         'act2':{'input2':{'source2':'Generate Capability Metric', 'min_tok2':0, 'max_tok2':1, 'list_tok2':'no2'}}, 
                         'act3':{'input3':{'source3':'Generate Capability Metric', 'state3':'false'}}, 
                         'act4':{'input4':{'source4':'Additional Capability', 'arg1':'false', 'arg2':'false'}}}
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
            if self._actdict['act2']['input2']['list_tok2'] >= self._actdict['act2']['input2']['min_tok2']:
                self._actdict['stepno'] = 3
                self.act2()
            else:
                print 'Insufficient input 2'
                
        if stepno == 3:
            if self._actdict['act3']['input3']['state3'] == True:
                self._actdict['stepno'] = 4
                self.act3()        
            else:
                print 'Insufficient input 3'
        
        if stepno == 4:
            if self._actdict['act4']['input4']['arg1'] == True:
                if self._actdict['act4']['input4']['arg2'] == True:
                    self.act4()
                else:
                    print 'Insufficient input 4'
    
    
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
    

if __name__ == "__main__":
    impl = behav()
    x = ProcessEx(impl)
    x.run()

        