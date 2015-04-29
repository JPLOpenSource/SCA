Installing and running the Jython MDAnimation for Activity pattern examples.
6 Aug. 2010 (Cheng, Reder)

Setup:
1. Install MagicDraw's Jython over the original Jython by:
    a. Download Jython_installer_2.5.1.jar from www.jython.org
    b. Go to /Applications/"MagicDraw UML 16.8/plugins/com.nomagic.magicdraw.jpython/
        and change the jython2.5.1 folder name to jython2.5.1.old.
    c. Create a new jython2.5.1 folder and run jython installer to install into it.
2. Put the files from under QF_Py/examples/Activities/MDAnimator/Libs/ into
    the MagicDraw plugin  jython2.5.1/Lib/ folder.
3. Copy the MDAnimator folder into MagicDraw plugin jython2.5.1/scripts/ folder
    and remove the README.txt and the Libs folder .
4. Restart MagicDraw, and voila, menu items should show up under the File menu.

Using:
1. From under the examples/Activities/ folder in prompt, start up MdGuiServer.py
2. Load up an Activity model that has corresponding Activities executable.
3. Use the MD File menu items to start an activity, step, etc.
4. Make sure to choose File -> Terminate_Server to stop the MdGuiServer.
