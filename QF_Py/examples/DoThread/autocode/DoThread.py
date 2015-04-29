#
# Copyright 2009, 2010 California Institute of Technology.
# ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
#
"""
File: DoThread.py

Date Created:  7-Dec-2009 
Created By:    watney

Python class to support the Do thread in a state-machine
the Test Statechart model.
"""
# Python imports here
import time
import threading

class DoThread (threading.Thread):

    def __init__(self, doFunc):
        """
        Constructor
        """
        self.doFunc = doFunc
        self.__activeThread = None
        threading.Thread.__init__(self)
        
    def terminate(self):
        """
        Cause run() loop to terminate, thus terminating the Thread.
        """
        print "Terminating thread..."
        self.__activeThread = None
        self.join()

    def run(self):
        """
        Thread.run() loop, executes something and sleep, running as long as
        activeThread is the same as current thread.
        """
        self.__activeThread = self            
        while self.__activeThread == threading.currentThread():
            try:
                time.sleep(1.0)
                self.doFunc()
            except:
                print "*** Some Exception happened in the running of the thread"
                assert(0)
                
            
 