'''
Created on Jun 22, 2010

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
        self._actdict['act2']['input2']['list_tok2'] = 3
        self._actdict['act3']['input3']['list_tok3'] = 4         
        print 'act1 finished'            
        
    def act2Behav(self):   
        print 'act2 finished'
        
    def act3Behav(self):
        print 'act3 finished'
        


class XXActivity (ActivityBase.ActivityBase):
     
    def __init__(self, impl):
        '''
        Constructor
        '''
        ActivityBase.ActivityBase.__init__(self)

        self.__impl = impl
        self._actdict = {'userinput':'yes', 
                         'stepno':1,
                         'act1':{'input1':{'source1':'user input', 'state1':'false'}}, 
                         'act2':{'input2':{'source2':'act1', 'min_tok2':3, 'max_tok2':3, 'list_tok2':'no2'}}, 
                         'act3':{'input3':{'source3':'act1', 'min_tok3':4, 'max_tok3':4, 'list_tok3':'no3'}}}
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
            if self._actdict['act2']['input2']['list_tok2'] == self._actdict['act2']['input2']['min_tok2']:
                self._actdict['stepno'] = 3
                self.act2()
            else:
                print 'Insufficient input 2'
        
        if stepno == 3:
            if self._actdict['act3']['input3']['list_tok3'] == self._actdict['act3']['input3']['min_tok3']:
                self.act3()
            else:
                print 'Insufficient input 3'
    
    
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
    x = XXActivity(impl)
    x.run()

        