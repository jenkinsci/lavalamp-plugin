@echo off

set CP=lib\LavaLampServer.jar;lib\ftd2xxj-2.1.jar;lib\javax.util.property-2_0.jar
set NLP=native\win32

set JAVA32HOME="%ProgramFiles(x86)%\Java\jre6"
set JAVA32=%JAVA32HOME%\bin\java

%JAVA32% -Djava.library.path=%NLP% -cp %CP% com.ingenotech.lavalamp.LavaLampServer