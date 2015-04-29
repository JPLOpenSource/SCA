
from optparse import OptionParser

usage = "usage: %prog [options] <State machine names>"
vers  = "%prog "
parser = OptionParser(usage, version=vers)

parser.add_option("-s", "--sm", dest="sm", type="string",
                      help="State machine to start",
                      action="store", default="")
parser.add_option("-n", "--nogui", dest="nogui_flag", 
                      help="Turn off trace gui displays.",
                      action="store_true", default=False)
parser.add_option("-i", "--noimpl", dest="noimpl_flag", 
                      help="Turn off hand coded implementation.",
                      action="store_true", default=False)  
(opt, args) = parser.parse_args()
    
print opt
print args