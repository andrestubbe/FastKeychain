@echo off
setlocal
cd /d "%~dp0"
chcp 65001 >nul

echo =======================================================
echo  FastKeychain Latency ^& Throughput Benchmark
echo =======================================================

call mvn clean compile -q
if not exist "target\cp.txt" (
    call mvn dependency:build-classpath -Dmdep.outputFile=target\cp.txt -q
)

set /p APP_CP=<target\cp.txt

if not exist "examples\Benchmark\target" mkdir "examples\Benchmark\target"
javac -cp "target\classes;%APP_CP%" -d examples\Benchmark\target examples\Benchmark\src\main\java\fastkeychain\benchmark\Benchmark.java
java -cp "target\classes;examples\Benchmark\target;%APP_CP%" fastkeychain.benchmark.Benchmark
pause
