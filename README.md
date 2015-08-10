JPL Statechart Autocoder (SCA) Rev. 2 Repository (NO FLIGHT CODE ALLOWED)
=======

The Statechart Autocoder (SCA) was developed at the Jet Propulsion Laboratory
in Pasadena, CA.  SCA is a tool that takes UML Statecharts (essentially Harel 
Statecharts) as input, in the form of XMI, and generates state machine code 
implementations in a variety of languages.  The tool is designed to receive UML
statecharts created in the [MagicDraw](http://www.nomagic.com/products/magicdraw.html)
CASE (Computer Aided Software Engineering) tool developed and sold by No Magic Inc.
MagicDraw is a commercially available CASE tool and not open source, however,
a trial version can be downloaded from [MagicDraw® Demo](https://www.magicdraw.com/main.php?ts=download&cmd_show_download=1&NMSESSID=107bd63d36ba787fbce7d140fb05d8be&group=1&menu=download_demo&c=5a19b9e8c05b71aaa19aedbc9376d9b3).
Note the current release of SCA only supports XMI produced by MagicDraw up to 
version 17.0.5.

SCA can generate C, C++, and Python implementations of state machines.  In addition
SCA can also generate [Promela](http://en.wikipedia.org/wiki/Promela) for verification
of designs.  The Promela is still very experimental.  The C and C++ state machines use 
the multi-threaded state machine framework called Quantum Framework and developed 
by Miro Samek.  We distribute older versions of the Quantum Framework, which are
known to work with the C and C++ code we generate, but the official source code and 
license information is from (http://www.state-machine.com/).  Our Python Framework
was developed at JPL is our own implementation of Samek's Quantum Framework in native
Python with some GUI hooks added which will be explained later.

Getting Started
===

Download the .zip archive and unzip, or `git clone` this repository.

Environment setup:
* Define `SCA_ROOT`: `setenv SCA_ROOT ./SCA` or `export SCA_ROOT=./SCA`
* If csh: `cd $SCA_ROOT/QF_Py/bin; source cshrc.sh`
* If bash: `cd $SCA_ROOT/QF_Py/bin; source bashrc.sh`

Next, compile autocoder
```
cd $SCA_ROOT/autocoder
ant clean all
```

Finally, autocode something; typically, create an `autocode` subdirectory and autocode model output into that path:
* C: `cd $SCA_ROOT/QF_C/test/Calculator/autocode ; java -jar $SCA_ROOT/autocoder/autocoder.jar -c ../Calculator.xml`
* C++: `cd $SCA_ROOT/QF_Cpp/test/Calculator/autocode ; java -jar $SCA_ROOT/autocoder/autocoder.jar -cpp ../Calculator.xml`
* Python: `cd $SCA_ROOT/QF_Py/test/Calculator/autocode ; java -jar $SCA_ROOT/autocoder/autocoder.jar -python ../Calculator.xml`
* Promela: `cd $SCA_ROOT/Spin/test/Calculator/autocode ; java -jar $SCA_ROOT/autocoder/autocoder.jar -promela ../Calculator.xml`

Directory Structure
===
To Be Completed.

History
===
To Be Completed.

Collaboration
===
To Be Completed.
