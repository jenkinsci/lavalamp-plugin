@echo off

set CP=lib\LavaLampController.jar;lib\ftd2xxj-2.1.jar;lib\javax.util.property-2_0.jar
set NLP=lib\native\win32

if defined ProgramFiles(x86) (
  set PF="%ProgramFiles(x86)%"
) else (
  set PF="%ProgramFiles%"
)

set JAVA32HOME=%PF%\Java\jre6
set JAVA32=%JAVA32HOME%\bin\java

%JAVA32% -Djava.library.path=%NLP% -cp %CP% com.ingenotech.lavalamp.LavaLampServer 0 1999
