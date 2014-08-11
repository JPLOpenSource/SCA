import DoEx

def update():
    doex.win.update()

def windowsReady():
    #print "%s window state: %s" % (doex.win.title(), doex.win.state())
    return doex.win.state() == 'normal'

doex = DoEx.DoEx("DoEx", big_name=True)
doex.canvas.scale("all", 0.0, 0.0, 1.00, 1.00)
doex.win.geometry('1507x1201+30+30')

mapCharts = {
'doex': doex,
}
