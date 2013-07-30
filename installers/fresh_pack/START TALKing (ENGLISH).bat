@ECHO OFF
SET WIN7_PATH="c:\Program Files (x86)\Java\jre6\bin"
SET WINXP_PATH="c:\Program Files\Java\jre6\bin"
IF EXIST %WIN7_PATH% SET JAVA_HOME=%WIN7_PATH%
IF EXIST %WINXP_PATH% SET JAVA_HOME=%WINXP_PATH%

%JAVA_HOME%\java -jar TalkAbroadLogKeeper.jar
ECHO Press any key to exit.
SET /p input=