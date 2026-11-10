@echo off
setlocal

set "ROOT=%~dp0"
set "CLASSES=%ROOT%target\classes"
set "TEST_CLASSES=%ROOT%target\test-classes"
set "MAIN_SOURCES=target\main-sources.txt"

if not exist "%CLASSES%" mkdir "%CLASSES%"
if not exist "%TEST_CLASSES%" mkdir "%TEST_CLASSES%"
cd /d "%ROOT%"

(for %%F in (src\main\java\*.java) do @echo src/main/java/%%~nxF) > "%MAIN_SOURCES%"
javac --release 21 -d "%CLASSES%" @"%MAIN_SOURCES%"
if errorlevel 1 exit /b %ERRORLEVEL%

javac --release 21 -d "%TEST_CLASSES%" -cp "%CLASSES%" src\test\java\PharmacyTest.java
if errorlevel 1 exit /b %ERRORLEVEL%
