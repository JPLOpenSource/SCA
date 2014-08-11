"""
Quick test of parsing the .h signals file that is auto generated.
"""
def parseStatechartSignalsH(file):
    """
    Parse a StatechartSignals.h file and generate
    an indexed dictionary of signal names.
    """
    # Read file
    f = open(file,"r")
    lines = f.readlines()
    #
    event_lines = []
    event_capture = False
    event_dict = dict()
    # Capture lines with user events
    for line in lines:
        if line.find("User defined signals") > -1:
            event_capture = True
        if event_capture == True:
            event_lines.append(line)
        if line.find("Timer Events") > -1 or line.find("Maximum signal id") > -1:
            break
    # Make list of string event names and enum event values    
    event_lines = map(lambda x: x.strip(), event_lines[1:-1])[:-1]
    event_names = map(lambda x: x.split(",")[0].strip(), event_lines)
    x = map(lambda x: x.split(",")[1].split("/*")[1].split("*/")[0].strip(), event_lines)
    event_nums = map(lambda x: int(x,16), x)
    # Create the dictionary
    for ev in enumerate(event_names):
        event_dict[event_nums[ev[0]]] = ev[1]
    return event_dict

event_dict = parseStatechartSignalsH("../../test_harness-C/autocode/StatechartSignals.h")
print event_dict

print event_dict[7]

