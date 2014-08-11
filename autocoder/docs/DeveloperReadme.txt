Stats as of November 11, 209:

======== Source tree line count using Wheeler's SLOCCount tool ========
SLOC	Directory	SLOC-by-Language (Sorted)
11395   src-notmpl      java=11395
7204    src             java=7204
3706    test            ansic=1488,java=1321,python=897

Totals grouped by language (dominant language first):
java:         19920 (89.31%)
ansic:         1488 (6.67%)
python:         897 (4.02%)


Total Physical Source Lines of Code (SLOC)                = 22,305
Development Effort Estimate, Person-Years (Person-Months) = 5.21 (62.52)
 (Basic COCOMO model, Person-Months = 2.4 * (KSLOC**1.05))
Schedule Estimate, Years (Months)                         = 1.00 (12.03)
 (Basic COCOMO model, Months = 2.5 * (person-months**0.38))
Estimated Average Number of Developers (Effort/Schedule)  = 5.20
Total Estimated Cost to Develop                           = $ 703,824
 (average salary = $56,286/year, overhead = 2.40).
SLOCCount, Copyright (C) 2001-2004 David A. Wheeler
SLOCCount is Open Source Software/Free Software, licensed under the GNU GPL.
SLOCCount comes with ABSOLUTELY NO WARRANTY, and you are welcome to
redistribute it under certain conditions as specified by the GNU GPL license;
see the documentation for details.
Please credit this data as "generated using David A. Wheeler's 'SLOCCount'."


======= Velocity template line count ========
(total lines)   $ find src -name "*.vm" -exec cat {} \; | wc -l
    3388
(- comments)    $ find src -name "*.vm" -exec grep -e "^[#][#]" {} \; | wc -l
     843
(- blank lines) $ find src -name "*.vm" -exec grep -e "^\$" {} \; | wc -l
     172
(- blank #)     $ find src -name "*.vm" -exec grep -e "^[#]\$" {} \; | wc -l
       9
=>  2364 LoC Velocity

(Velocity code) $ find src -name "*.vm" -exec grep -e "^[#][*]" {} \; | wc -l
=>   976


======== Total lines of code ========
v2:  7204 + 2364 + 3706 test code ==>  13,274 SLOC

