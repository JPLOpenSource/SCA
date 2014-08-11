#!/usr/bin/env python
#
# zoom_test5.py
# Integrate area zooming with text scaling ops.
# Integrates all into a class
# Adds a entry box to test notion of following view of active object.
#
from Tkinter import *

class ZoomCanvas(object):
    """
    Build a simple GUI

    Grid a canvas with scrollbars, and add a few control buttons.
    """
    def __init__(self, parent):
        """
        Constructor
        """
        self.id = 0
        self.zdepth = 1.0
        self.canvas = Canvas(parent, width=600, height=500)
        # Scroll bars:
        svert  = Scrollbar(parent, orient='vertical')
        shoriz = Scrollbar(parent, orient='horizontal')

        self.canvas.grid(row=0, column=0, columnspan=3, sticky="NEWS")
        svert.grid(row=0, column=3, columnspan=1, sticky="NS")
        shoriz.grid(row=1, column=0, columnspan=3, sticky="EW")
    
        parent.grid_columnconfigure(0, weight=1)
        parent.grid_columnconfigure(1, weight=1)
        parent.grid_columnconfigure(2, weight=1)

        parent.grid_rowconfigure(0, weight=1)
    
        svert.config(command=self.canvas.yview)
        shoriz.config(command=self.canvas.xview)    
        self.canvas.config(xscrollcommand=shoriz.set)
        self.canvas.config(yscrollcommand=svert.set)
               
        # Add a couple of zooming buttons
        self.factor = 1.1
        def zoomincb():
            self.zoom(self.factor)
        def zoomoutcb():
            self.zoom(1.0/self.factor)
        zoomin = Button(text="Zoom In", command=zoomincb)
        zoomout = Button(text="Zoom Out", command=zoomoutcb)
        zoomin.grid(row=2, column=0)
        zoomout.grid(row=2, column=1)
    
        # Add Entry to input number of rectangle to make active and see.
        self.entry = Entry(parent)
        self.entry.bind('<Return>', (lambda event: self.ActRect()))
        self.entry.grid(row=2,column=2)
        # Set up event bindings for canvas
        self.canvas.bind("<1>", self.zoomMark)
        self.canvas.bind("<B1-Motion>", self.zoomStroke)
        self.canvas.bind("<ButtonRelease-1>", self.zoomArea)
        
        self.tag_active = "current"

    #-------------------------------------------------------
    #  activate rect.
    #-------------------------------------------------------
    def ActRect(self):
        etag = self.entry.get()
        self.entry.delete(0,END)
        c = self.canvas
        region = c.cget('scrollregion')
        
        for i in c.find_all():
            if c.type(i) == "rectangle":
                # Find matching tag
                for tag in c.gettags(i):
                    if tag != "current" and tag == etag:
                        c.itemconfigure(self.tag_active,outline='black')
                        tag_coords = c.coords(tag)
                        c.itemconfigure(tag,outline="red")
                        self.tag_active = tag
                        c.config(scrollregion=c.bbox(tag))
                        # Bring back zoomed region
                        c.configure(scrollregion=region)

    #--------------------------------------------------------
    #  zoomMark
    #--------------------------------------------------------
    def zoomMark(self, event):
        """
        Mark the first (x,y) coordinate for zooming.
        """
        self.zoomArea = dict()
        x = event.x
        y = event.y
        self.zoomArea['x0'] = self.canvas.canvasx(x)
        self.zoomArea['y0'] = self.canvas.canvasy(y)
        self.canvas.create_rectangle(x, y, x, y, outline='black', tag="zoomArea")
    #--------------------------------------------------------
    #  zoomStroke
    #--------------------------------------------------------
    def zoomStroke(self, event):
        """
        Zoom in to the area selected by itemMark and itemStroke.
        """
        x = event.x
        y = event.y
        self.zoomArea['x1'] = self.canvas.canvasx(x)
        self.zoomArea['y1'] = self.canvas.canvasy(y)
        self.canvas.coords("zoomArea", self.zoomArea['x0'], self.zoomArea['y0'], self.zoomArea['x1'], self.zoomArea['y1'])
    #--------------------------------------------------------
    #  zoomArea
    #--------------------------------------------------------
    def zoomArea(self, event):
        """
        Zoom in to the area selected by itemMark and itemStroke.
        """
        x = event.x
        y = event.y
        #--------------------------------------------------------
        #  Get the final coordinates.
        #  Remove area selection rectangle
        #--------------------------------------------------------
        self.zoomArea['x1'] = self.canvas.canvasx(x)
        self.zoomArea['y1'] = self.canvas.canvasy(y)
        self.canvas.delete("zoomArea")
        #--------------------------------------------------------
        #  Check for zero-size area
        #--------------------------------------------------------
        if (self.zoomArea['x0'] == self.zoomArea['x1']) or (self.zoomArea['y0'] == self.zoomArea['y1']):
            return
        #--------------------------------------------------------
        #  Determine size and center of selected area
        #--------------------------------------------------------
        areaxlength = abs( self.zoomArea['x1'] - self.zoomArea['x0'] )
        areaylength = abs( self.zoomArea['y1'] - self.zoomArea['y0'] )
        xcenter = ( self.zoomArea['x0']+self.zoomArea['x1'] )/2.0
        ycenter = ( self.zoomArea['y0']+self.zoomArea['y1'] )/2.0
        #--------------------------------------------------------
        #  Determine size of current window view
        #  Note that canvas scaling always changes the coordinates
        #  into pixel coordinates, so the size of the current
        #  viewport is always the canvas size in pixels.
        #  Since the canvas may have been resized, ask the
        #  window manager for the canvas dimensions.
        #--------------------------------------------------------
        winxlength = self.canvas.winfo_width()
        winylength = self.canvas.winfo_height()
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
        self.zoom(factor, xcenter, ycenter, winxlength, winylength)
    #------------------------------------------------------------
    # zoomText
    #------------------------------------------------------------
    def zoomtext(self, zdepth):
        """
        Adjust fonts
        """
        c = self.canvas
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
            #print fontsize, newsize, zdepth
            if abs(newsize) >= 4:
                newfont = "%s %d" % (font.split()[0],newsize)
                c.itemconfigure(i,font=newfont,text=text)
            else:
                c.itemconfigure(i,text="")
    #--------------------------------------------------------
    #  zoom
    #--------------------------------------------------------
    def zoom(self, factor, xcenter=None, ycenter=None, winxlength="", winylength=""):
        """
        Zoom the canvas view, based on scale factor
        and center point and size of new viewport.
        If the center point is not provided, zoom
        in/out on the current window center point.
    
        This procedure uses the canvas scale function to
        change coordinates of all objects in the canvas.
        """
        #print "factor = %f" % factor
        self.zdepth = self.zdepth*factor
        #--------------------------------------------------------
        #  If (xcenter, ycenter) were not supplied,
        #  get the canvas coordinates of the center
        #  of the current view.  Note that canvas
        #  size may have changed, so ask the window
        #  manager for its size.
        #--------------------------------------------------------
        if xcenter == None or ycenter == None:
            winxlength = self.canvas.winfo_width()
            winylength = self.canvas.winfo_height()
            xcenter = self.canvas.canvasx (winxlength/2.0)
            ycenter = self.canvas.canvasy (winylength/2.0)
        #print "winxlength, winylength = %d, %d" % (winxlength, winylength)
        #--------------------------------------------------------
        #  Scale all objects in the canvas
        #  Adjust our viewport center point
        #--------------------------------------------------------
        self.canvas.scale("all",0,0,factor,factor)
        xcenter = xcenter * factor
        ycenter = ycenter * factor
        #print "xcenter, ycenter = %7.2f, %7.2f" % (xcenter,ycenter)

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
        canvas = self.canvas
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
        #print (ax0, ay0, ax1, ay1)
        while (ax0<x0) or (ay0<y0) or (ax1>x1) or (ay1>y1):
            # triple the scalable area size
            x0 = x0-xlength
            x1 = x1+xlength
            y0 = y0-ylength
            y1 = y1+ylength
            xlength = xlength*3.0
            ylength = ylength*3.0
        #print (x0, y0, x1, y1)
        #--------------------------------------------------------
        #  Now that we've finally got a region defined with
        #  the proper aspect ratio (of only the scalable items)
        #  but large enough to include all items, we can compute
        #  the xview/yview fractions and set our new viewport
        #  correctly.
        #--------------------------------------------------------
        newxleft = (xcenter-x0-(winxlength/2.0))/xlength
        newytop  = (ycenter-y0-(winylength/2.0))/ylength
        #print "xlength, ylength = %f,%f" % (xlength,ylength)
        #print newxleft,newytop
        canvas.configure(scrollregion = (x0,y0,x1,y1))
        canvas.xview_moveto(newxleft)
        canvas.yview_moveto(newytop)

        #
        # Scale the fonts
        def zoomt():
            self.zoomtext(self.zdepth)
        canvas.after_cancel(self.id)
        id = self.canvas.after_idle(zoomt)

        #--------------------------------------------------------
        #  Change the scroll region one last time, to fit the
        #  items on the canvas.
        #--------------------------------------------------------
        canvas.configure(scrollregion = canvas.bbox("all"))


if __name__ == "__main__":
    win = Tk()
    c = ZoomCanvas(win)
    can = c.canvas
    # Supply a little test data
    for i in range(10,500,30):
        for j in range(10,600,30):
            can.create_rectangle(i, j, i+10, j+10, tag="%d,%d"%(i,j))
            can.create_text(i, j, text="(%d,%d)" % (i,j), anchor=NE, font="Helvetica 6")
    mainloop()
    

