
from Tkinter import *

def zoominit(c, zfact=1.1):
	# save zoom state in a global variable
	# with the same name as the canvas handle
	global data
	data = dict()
	data['zdepth'] = 1.0
	data['idle'] = 0
	# add mouse bindings to canvas
	def zoom1(event):
		zoom(c,zfact,event.x,event.y)
	def zoom2(event):
		zoom(c,1.0/zfact,event.x,event.y)
	c.bind("<Button-1>", zoom1)
	c.bind("<Button-2>", zoom2)
		

def zoom(c, fact, x, y):
	global data
	# zoom at the current mouse position
	x = c.canvasx(x)
	y = c.canvasy(y)
	c.scale("all",x,y,fact,fact)
	# save new zoom depth
	data['zdepth'] = data['zdepth'] * fact
	# update fonts only after main zoom activity has ceased
	def zoomt():
		zoomtext(c)
	c.after_cancel(data['idle'])
	data['idle'] = c.after_idle(zoomt)


def zoomtext(c):
	"""
	adjust fonts
	"""
	global data
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
		newsize = int(int(fontsize) * data['zdepth'])
		print fontsize, newsize, data['zdepth']
		if abs(newsize) >= 4:
			newfont = "%s %d" % (font.split()[0],newsize)
			c.itemconfigure(i,font=newfont,text=text)
		else:
			c.itemconfigure(i,text="")
		
		# update canvas scrollregion
		bbox = c.bbox('all')
		if len(bbox):
			c.configure(scrollregion=bbox)
		else:
			c.configure(scrollregion=[-4,-4, c.cget('width')-4,c.cget('height')-4])


if __name__ == "__main__":
	# test code
	c = Canvas()
	c.pack(expand=True,fill=BOTH)
	zoominit(c)
	c.create_text(50,50,text="Hello, World!")
	c.create_rectangle(c.bbox('all'))
	mainloop()
