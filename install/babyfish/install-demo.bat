:: The "choice" command can only be used in win7,
:: In order to support winxp, use "set /p".
:beginChoice
set /p database="Please choose database, o(Oracle, suggested) or h(HSQLDB): "
@echo off
(
    if /i "%database%" == "o" (goto :endChoice)
    if /i "%database%" == "oracle" (
        set database=o
        goto :endChoice
    )
    if /i "%database%" == "h" (goto :endChoice)
    if /i "%database%" == "hsqldb" (
        set database=h
        goto :endChoice
    )
)
@echo Invalid choice, please enter your choice again:
goto :beginChoice
:endChoice

set args=
if /i "%database%" == "h" (goto :endOracle)
cd ..\ojdbc
call install.bat
cd ..\babyfish
set /p hostName="Please enter the host name of Oracle(Nothing means 'localhost'): "
set /p port="Please enter the port of Oracle(Nothing means '1521'): "
set /p sid="Please enter the sid of Oracle(Nothing means 'babyfish') "
set /p user="Please enter the user name of Oracle(Nothing means 'babyfish_demo'): "
set /p password="Please enter the password of Oracle(Nothing means '123'): "
if "%hostName%" == "" (set hostName=localhost)
if "%port%" == "" (set port=1521)
if "%sid%" == "" (set sid=babyfish)
if "%user%" == "" (set user=babyfish_demo)
if "%password%" == "" (set password=123)
set args=-Doracle=jdbc:oracle:thin:@%hostName%:%port%:%sid% -Doracle.user=%user% -Doracle.password=%password%
:endOracle

cd ../../demo
echo Use maven to create eclipse projects with the arguments [%args%]
call mvn-wrapper.bat eclipse:eclipse %args%
echo Use maven to compile and test projects with the arguments [%args%]
call mvn-wrapper.bat clean install %args%
cd ../install/babyfish
pause
