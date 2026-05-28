@echo off
:: Fuerza a la ventana de la consola (CMD) a usar la codificación UTF-8 nativa
chcp 65001 > nul

if "%%~1"=="" goto error
if "%%~2"=="" goto error

set NOMBRE=%~1
set EXTENSION=%~2

echo ==================================================
echo [BAT] Procesando Proyecto con Package: %NOMBRE%.%EXTENSION%
echo ==================================================

echo [1/2] Compilando desde la raiz del paquete...
:: Compilamos apuntando al archivo, manteniendo el classpath en la carpeta actual
javac -encoding UTF-8 -cp ".;jakarta.websocket-api-2.2.0.jar;jakarta.websocket-client-api-2.2.0.jar" %NOMBRE%.%EXTENSION%

if errorlevel 1 (
    echo.
    echo [ERROR] La compilacion ha fallado. Revisa tu codigo Java.
    pause
    exit /b %errorlevel%
)

echo [OK] Compilacion exitosa.
echo.
echo [2/2] Ejecutando aplicacion respetando la jerarquia de paquetes...
echo --------------------------------------------------

:: Truco de magia: Nos movemos temporalmente dos carpetas hacia atras (C:\Aplicaciones\PROYECTOS\Programas)
cd ..\..

:: Ejecutamos usando el nombre completo del paquete y mapeando las librerias JAR en su ruta correcta
java -Dfile.encoding=UTF-8 -Duser.language=es -cp ".;java/socket1;java/socket1/jakarta.websocket-api-2.2.0.jar;java/socket1/jakarta.websocket-client-api-2.2.0.jar" java.socket1.%NOMBRE%

:: Al terminar o cerrar el servidor, regresamos la consola a tu carpeta original para comodidad tuya
cd %~dp0
exit /b 0

:error
echo [ERROR] Falta el nombre del archivo o la extension.
echo.
pause
