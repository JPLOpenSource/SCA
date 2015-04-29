
# Since part of the miros, It is licensed under the same terms as Python itself.
#
"""
NAME: event.py
DESCRIPTION: Emulates the QF Event class that provides infrastructure
             of abstract event class for the user to implement.
AUTHOR: Leonard J. Reder
EMAIL:  reder@jpl.nasa.gov
DATE CREATED:
"""


class Event(object):
    """
    Event base class. Abstract event class.

    Event represents events without parameters and serves as the base class
    for derivation of events with parameters.

    Note: All data members of the Event class must remain public to keep it
    an AGGREGATE.
    
    Note: The attribute QEvent::dynamic_ is not used in the Python port since
    all events are considered dynamic by the nature of the language.
    """
    signal = None
    def __init__(self, sig=None):
        """
        Constructor
        """
        self.signal = sig
        

class Quit(Event):
    """
    Special event object type that causes Active objects thread termination.
    """
    def __init__(self):
        """
        Constructor
        """
        self.signal = "quit"

