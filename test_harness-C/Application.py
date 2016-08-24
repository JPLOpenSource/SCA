
import sys
sys.path.append('autocode')

import Simple1

def update():
    mySm.win.update()

mySm = Simple1.Simple1("mySm:Simple1")
mySm.canvas.scale("all", 0.0, 0.0, 1.00, 1.00)
mySm.win.geometry('979x755+14+58')


mapCharts = {

    'mySm:Simple1': mySm,

}
