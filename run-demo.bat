@echo off
setlocal
cd /d "%~dp0"
chcp 65001 >nul

echo =======================================================
echo  FastKeychain Demo
echo =======================================================

call mvn clean compile -q
if not exist "target\cp.txt" (
    call mvn dependency:build-classpath -Dmdep.outputFile=target\cp.txt -q
)

set /p APP_CP=<target\cp.txt

javac -cp "target\classes;%APP_CP%" -d examples\Demo\target examples\Demo\src\main\java\fastkeychain\demo\Demo.java
java -cp "target\classes;examples\Demo\target;%APP_CP%" fastkeychain.demo.Demo
pause
