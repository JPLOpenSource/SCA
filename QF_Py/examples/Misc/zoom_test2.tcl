proc zoominit {c {zfact {1.1}}} {
	# save zoom state in a global variable
	# with the same name as the canvas handle
    upvar #0 $c data
    set data(zdepth) 1.0
    set data(idle) {}
    # add mousewheel bindings to canvas
    bind $c <Button-1> "zoom $c $zfact"
    bind $c <Button-2> "zoom $c [expr {1.0/$zfact}]"
    bind $c <j> "zoom $c $zfact"
    bind $c <k> "zoom $c [expr {1.0/$zfact}]"
}

proc zoom {c fact} {
    upvar #0 $c data
    # zoom at the current mouse position
    set x [$c canvasx [expr {[winfo pointerx $c] - [winfo rootx $c]}]]
    set y [$c canvasy [expr {[winfo pointery $c] - [winfo rooty $c]}]]
    $c scale all $x $y $fact $fact
    # save new zoom depth
    set data(zdepth) [expr {$data(zdepth) * $fact}]
    # update fonts only after main zoom activity has ceased
    after cancel $data(idle)
    set data(idle) [after idle "zoomtext $c"]
}

proc zoomtext {c} {
	upvar #0 $c data
    # adjust fonts
    foreach {i} [$c find all] {
		if { ! [string equal [$c type $i] text]} {
			continue
		}
        set fontsize 0
        # get original fontsize and text from tags
        #   if they were previously recorded
        foreach {tag} [$c gettags $i] {
			puts $tag
        	scan $tag {_f%d} fontsize
            scan $tag "_t%\[^\0\]" text
        }
	    # if not, then record current fontsize and text
        # and use them
        set font [$c itemcget $i -font]
        if {!$fontsize} {
        	set text [$c itemcget $i -text]
            set fontsize [lindex $font 1]
            $c addtag _f$fontsize withtag $i
            $c addtag _t$text withtag $i
        }
        # scale font
        set newsize [expr {int($fontsize * $data(zdepth))}]
        if {abs($newsize) >= 4} {
             $c itemconfigure $i \
                				-font [lreplace $font 1 1 $newsize] \
                                -text $text
        } {
			# suppress text if too small
            $c itemconfigure $i -text {}
          }
        }
        # update canvas scrollregion
        set bbox [$c bbox all]
        if {[llength $bbox]} {
             $c configure -scrollregion $bbox
        } {
             $c configure -scrollregion [list -4 -4 \
                	[expr {[winfo width $c]-4}] \
                    [expr {[winfo height $c]-4}]]
          }
}


#  test code
pack [canvas .c] -expand true -fill both
zoominit .c
.c create text 50 50 -text "Hello, World!"
.c create rect [.c bbox all]
