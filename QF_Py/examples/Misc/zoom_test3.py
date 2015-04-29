#!/usr/bin/env python
#
# zoom_test3.py to integrate area zooming with text scaling ops.
#
from Tkinter import *
#--------------------------------------------------------
#  zoomMark
#--------------------------------------------------------
def zoomMark(event):
    """
    Mark the first (x,y) coordinate for zooming.
    """
    global zoomArea
    zoomArea = dict()
    x = event.x
    y = event.y
    zoomArea['x0'] = c.canvasx(x)
    zoomArea['y0'] = c.canvasy(y)
    c.create_rectangle(x, y, x, y, outline='black', tag="zoomArea")
#--------------------------------------------------------
#  zoomStroke
#--------------------------------------------------------
def zoomStroke(event):
    """
    Zoom in to the area selected by itemMark and itemStroke.
    """
    global c
    global zoomArea
    x = event.x
    y = event.y
    zoomArea['x1'] = c.canvasx(x)
    zoomArea['y1'] = c.canvasy(y)
    c.coords("zoomArea", zoomArea['x0'], zoomArea['y0'], zoomArea['x1'], zoomArea['y1'])
#--------------------------------------------------------
#  zoomArea
#--------------------------------------------------------
def zoomArea(event):
    """
    Zoom in to the area selected by itemMark and itemStroke.
    """
    global c
    global zoomArea
    x = event.x
    y = event.y
    
    #--------------------------------------------------------
    #  Get the final coordinates.
    #  Remove area selection rectangle
    #--------------------------------------------------------
    zoomArea['x1'] = c.canvasx(x)
    zoomArea['y1'] = c.canvasy(y)
    c.delete("zoomArea")

    #--------------------------------------------------------
    #  Check for zero-size area
    #--------------------------------------------------------
    if (zoomArea['x0'] == zoomArea['x1']) or (zoomArea['y0'] == zoomArea['y1']):
        return

    #--------------------------------------------------------
    #  Determine size and center of selected area
    #--------------------------------------------------------
    areaxlength = abs( zoomArea['x1'] - zoomArea['x0'] )
    areaylength = abs( zoomArea['y1'] - zoomArea['y0'] )
    xcenter = ( zoomArea['x0']+zoomArea['x1'] )/2.0
    ycenter = ( zoomArea['y0']+zoomArea['y1'] )/2.0

    #--------------------------------------------------------
    #  Determine size of current window view
    #  Note that canvas scaling always changes the coordinates
    #  into pixel coordinates, so the size of the current
    #  viewport is always the canvas size in pixels.
    #  Since the canvas may have been resized, ask the
    #  window manager for the canvas dimensions.
    #--------------------------------------------------------
    winxlength = c.winfo_width()
    winylength = c.winfo_height()

    #--------------------------------------------------------
    #  Calculate scale factors, and choose smaller
    #--------------------------------------------------------
    xscale = winxlength/areaxlength
    yscale = winylength/areaylength
    if (xscale > yscale):
        factor = yscale
    else:
        factor = xscale

    #--------------------------------------------------------
    #  Perform zoom operation
    #--------------------------------------------------------
    zoom(c, factor, xcenter, ycenter, winxlength, winylength)
#------------------------------------------------------------
# zoomText
#------------------------------------------------------------
def zoomtext(c, zdepth):
    """
    Adjust fonts
    """
    for i in c.find_all():
        if c.type(i) != "text":
            continue
        fontsize = 0
        # get original fontsize and text from tags
        # if they were previously recorded
        for tag in c.gettags(i):
            if tag != "current":
                if tag.find('_f') == 0:
                    fontsize = tag.strip("_f")
                if tag.find('_t') == 0:
                    text     = tag.strip("_t")

        # if not, then record current fontsize and text
        # and use them
        font = c.itemcget(i,"font")
        if fontsize==0:
            text = c.itemcget(i,"text")
            fontsize = int(font.split()[1])
            c.addtag_withtag("_f%d" % fontsize,i)
            c.addtag_withtag("_t%s" % text,i)
        
        # scale font
        newsize = int(int(fontsize) * zdepth)
        print fontsize, newsize, zdepth
        if abs(newsize) >= 4:
            newfont = "%s %d" % (font.split()[0],newsize)
            c.itemconfigure(i,font=newfont,text=text)
        else:
            c.itemconfigure(i,text="")


#--------------------------------------------------------
#  zoom
#--------------------------------------------------------
def zoom(canvas, factor, xcenter=None, ycenter=None, winxlength="", winylength=""):
    """
    Zoom the canvas view, based on scale factor
    and center point and size of new viewport.
    If the center point is not provided, zoom
    in/out on the current window center point.
    
    This procedure uses the canvas scale function to
    change coordinates of all objects in the canvas.
    """
    global zdepth
    print "factor = %f" % factor
    zdepth = zdepth*factor
    #--------------------------------------------------------
    #  If (xcenter, ycenter) were not supplied,
    #  get the canvas coordinates of the center
    #  of the current view.  Note that canvas
    #  size may have changed, so ask the window
    #  manager for its size.
    #--------------------------------------------------------
    if xcenter == None or ycenter == None:
        winxlength = canvas.winfo_width()
        winylength = canvas.winfo_height()
        xcenter = canvas.canvasx (winxlength/2.0)
        ycenter = canvas.canvasy (winylength/2.0)
    print "winxlength, winylength = %d, %d" % (winxlength, winylength)
    #--------------------------------------------------------
    #  Scale all objects in the canvas
    #  Adjust our viewport center point
    #--------------------------------------------------------
    canvas.scale("all",0,0,factor,factor)
    xcenter = xcenter * factor
    ycenter = ycenter * factor
    print "xcenter, ycenter = %7.2f, %7.2f" % (xcenter,ycenter)

    #--------------------------------------------------------
    #  Get the size of all the items on the canvas.
    #
    #  This is *really easy* using
    #      $canvas bbox all
    #  but it is also wrong.  Non-scalable canvas
    #  items like text and windows now have a different
    #  relative size when compared to all the lines and
    #  rectangles that were uniformly scaled with the
    #  [$canvas scale] command.
    #
    #  It would be better to tag all scalable items,
    #  and make a single call to [bbox].
    #  Instead, we iterate through all canvas items and
    #  their coordinates to compute our own bbox.
    #--------------------------------------------------------
    x0 = 1.0e30
    x1 = -1.0e30
    y0 = 1.0e30
    y1 = -1.0e30
    for item in canvas.find_all():
        if canvas.type(item) == "arc":
            pass
        elif canvas.type(item) == "line":
            pass
        elif canvas.type(item) == "oval":
            pass
        elif canvas.type(item) == "polygon":
            pass
        elif canvas.type(item) == "rectangle":
            coords = canvas.coords(item)
            for i in range(0, len(coords), 2):
                x = coords[i]
                y = coords[i+1]
                if x < x0:
                    x0 = x
                if x > x1:
                    x1 = x
                if y < y0:
                    y0 = y
                if y > y1:
                    y1 = y

    #--------------------------------------------------------
    #  Now figure the size of the bounding box
    #--------------------------------------------------------
    xlength = x1-x0
    ylength = y1-y0

    #--------------------------------------------------------
    #  But ... if we set the scrollregion and xview/yview
    #  based on only the scalable items, then it is not
    #  possible to zoom in on one of the non-scalable items
    #  that is outside of the boundary of the scalable items.
    #
    #  So expand the [bbox] of scaled items until it is
    #  larger than [bbox all], but do so uniformly.
    #--------------------------------------------------------
    (ax0, ay0, ax1, ay1) = canvas.bbox("all")
    print (ax0, ay0, ax1, ay1)
    while (ax0<x0) or (ay0<y0) or (ax1>x1) or (ay1>y1):
        # triple the scalable area size
        x0 = x0-xlength
        x1 = x1+xlength
        y0 = y0-ylength
        y1 = y1+ylength
        xlength = xlength*3.0
        ylength = ylength*3.0
    print (x0, y0, x1, y1)
    #--------------------------------------------------------
    #  Now that we've finally got a region defined with
    #  the proper aspect ratio (of only the scalable items)
    #  but large enough to include all items, we can compute
    #  the xview/yview fractions and set our new viewport
    #  correctly.
    #--------------------------------------------------------
    newxleft = (xcenter-x0-(winxlength/2.0))/xlength
    newytop  = (ycenter-y0-(winylength/2.0))/ylength
    print "xlength, ylength = %f,%f" % (xlength,ylength)
    print newxleft,newytop
    canvas.configure(scrollregion = (x0,y0,x1,y1))
    canvas.xview_moveto(newxleft)
    canvas.yview_moveto(newytop)

    #
    # Scale the fonts
    global id
    def zoomt():
        zoomtext(c,zdepth)
    canvas.after_cancel(id)
    id = c.after_idle(zoomt)

    #--------------------------------------------------------
    #  Change the scroll region one last time, to fit the
    #  items on the canvas.
    #--------------------------------------------------------
    canvas.configure(scrollregion = canvas.bbox("all"))


if __name__ == "__main__":
    #--------------------------------------------------------
    #  Build a simple GUI
    #
    #  Grid a canvas with scrollbars, and add a few
    #  control buttons.
    #--------------------------------------------------------
    global id, zdepth
    id = 0
    zdepth = 1.0
    win = Tk()
    c = Canvas(win, width=600, height=500)
    # Scroll bars:
    svert  = Scrollbar(win, orient='vertical')
    shoriz = Scrollbar(win, orient='horizontal')

    c.grid(row=0, column=0, columnspan=3, sticky="NEWS")
    svert.grid(row=0, column=3, columnspan=1, sticky="NS")
    shoriz.grid(row=1, column=0, columnspan=3, sticky="EW")
    
    win.grid_columnconfigure(0, weight=1)
    win.grid_columnconfigure(1, weight=1)
    win.grid_columnconfigure(2, weight=1)
    win.grid_rowconfigure(0, weight=1)
    
    svert.config(command=c.yview)
    shoriz.config(command=c.xview)    
    c.config(xscrollcommand=shoriz.set)
    c.config(yscrollcommand=svert.set)
    
    #c.config(height=600/4, width=500/4)
    #fullsize = (0, 0, 600, 500)
    #c.config(scrollregion=fullsize)
               
    # Add a couple of zooming buttons
    factor = 1.1
    def zoomincb():
        zoom(c,factor)
    def zoomoutcb():
        zoom(c,1.0/factor)
    zoomin = Button(text="Zoom In", command=zoomincb)
    zoomout = Button(text="Zoom Out", command=zoomoutcb)
    zoomin.grid(row=2, column=0)
    zoomout.grid(row=2, column=1)
    
    # Set up event bindings for canvas
    c.bind("<1>", zoomMark)
    c.bind("<B1-Motion>", zoomStroke)
    c.bind("<ButtonRelease-1>", zoomArea)

    # Supply a little test data
    for i in range(10,500,30):
        for j in range(10,600,30):
            c.create_rectangle(i, j, i+10, j+10)
            c.create_text(i, j, text="(%d,%d)" % (i,j), anchor=NE, font="Helvetica 6")
    mainloop()
    

