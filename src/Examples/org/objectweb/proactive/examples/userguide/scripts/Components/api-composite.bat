@echo off
echo.
echo --- User Guide: API Composite ------------------------------------------

goto doit

:usage
echo.
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION

call ..\init.bat

set JAVA_CMD=%JAVA_CMD% -Dgcm.provider="org.objectweb.proactive.core.component.Fractive"

%JAVA_CMD% org.objectweb.proactive.examples.userguide.components.api.composite.Main %*
ENDLOCAL

:end
pause
echo.
echo ---------------------------------------------------------