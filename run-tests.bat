@echo off
setlocal

set "ROOT=%~dp0"
set "CLASSES=%ROOT%target\classes"
set "TEST_CLASSES=%ROOT%target\test-classes"

cd /d "%ROOT%"
call build.bat
if errorlevel 1 exit /b %ERRORLEVEL%

java -cp "%CLASSES%;%TEST_CLASSES%" PharmacyTest
exit /b %ERRORLEVEL%
