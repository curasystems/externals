@echo off

if "%~2"=="" goto usage

rem mongodb-win32-x86_64-1.6.5
rem http://fastdl.mongodb.org/win32/

set _name=%~1
set _zipname=%_name%.zip

set _url=%~2%_zipname%

set oldPATH=%PATH%
@set PATH=..\..\tools;%PATH%

del /q %_name%
curl -C - -O %_url%
rd /s /q %_name%

7z x %_zipname%
del %_zipname%

@set path=%oldPATH%


:clean
set _name=
set _zipname=
set _url=

goto finally

:usage
echo [Name] [BaseUrl] 

:finally
