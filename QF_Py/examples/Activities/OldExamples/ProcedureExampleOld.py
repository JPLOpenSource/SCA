'''
Created on Jun 25, 2010

@author: wye

Accepts and produces integers
'''

from af import ActivityBaseOld

        
class Behav(object):
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
        

    def act1behav(self):
        
        '''user input'''
        in1 = self._actdict['act1']['input1']['tok_list'][0]        #user codes inputs in1, in2, in3...
        tok1 = int(in1) * 2                                         #user codes outputs tok1, tok2, tok3...
        resdict = {'out1':[tok1]}                                   #user fills in resdict with connections of tokens to outputs
        '''end user input'''                                            #can continue with 'out2':[tok2] or 'out1':[tok1, tok2]...
        
        self._actdict['act1']['output1']['tok_list'] = resdict['out1']
        self._actdict['act2']['input1']['tok_list'] = resdict['out1']
        
        print 'Step 1 input:', self._actdict['act1']['input1']['tok_list']
        print 'Step 1 output:', self._actdict['act1']['output1']['tok_list']
        

    def act2behav(self):
        
        '''user input'''
        in1 = self._actdict['act2']['input1']['tok_list'][0]
        tok1 = int(in1) + 3
        resdict = {'out1':[tok1]}
        '''end user input'''
        
        self._actdict['act2']['output1']['tok_list'] = resdict['out1']
        
        print 'Step 2 input:', self._actdict['act2']['input1']['tok_list']
        print 'Step 2 output:', self._actdict['act2']['output1']['tok_list']
        

class ProcedureEx (ActivityBaseOld.ActivityBase):
     
    def __init__(self, impl):
        '''
        Constructor
        '''
        ActivityBaseOld.ActivityBase.__init__(self)

        self.__impl = impl
        self._actdict = {'userinput':'yes', 
                         'stepno':1, 
                         'act1':{'input1':{'source':'input parameter', 'min_tok':1, 'max_tok':1, 'tok_list':[]},
                                 'output1':{'tok_list':[]}}, 
                         'act2':{'input1':{'source':'Step 1.out1', 'min_tok':1, 'max_tok':1, 'tok_list':[]},
                                 'output1':{'tok_list':[]}}}
       
        self.__impl.setActDict(self._actdict)
      
    
    def step(self, stepno):
        if stepno == 1:
            if self._actdict['act1']['input1']['min_tok'] <= len(self._actdict['act1']['input1']['tok_list']):
                if self._actdict['act1']['input1']['max_tok'] >= len(self._actdict['act1']['input1']['tok_list']): 
                    self._actdict['stepno'] = 2
                    if self._actdict['userinput'] == 'yes':
                        self._actdict['userinput'] = 'inputted'
                    self.act1()
                    
                else:
                    print 'Token overflow'
            else:
                print 'Insufficient input 1'
                
        if stepno == 2:
            if self._actdict['act2']['input1']['min_tok'] <= len(self._actdict['act2']['input1']['tok_list']):
                if self._actdict['act2']['input1']['max_tok'] >= len(self._actdict['act2']['input1']['tok_list']): 
                    self.act2()
                    
                else:
                    print 'Token Overflow'    
            else:
                print 'Insufficient input 2'
    
        
    def act1(self):
        self.__impl.act1behav()
        self.run()
        
    def act2(self):
        self.__impl.act2behav()
    

if __name__ == "__main__":
    impl = Behav()
    x = ProcedureEx(impl)
    x.run()
    

