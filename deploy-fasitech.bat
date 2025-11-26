@echo off
REM ============================================
REM Script de Deploy para Fasitech SRVAP-DEV (Windows)
REM ============================================
REM Servidor: verticaltecnologia.net.br -p 47979
REM Usuario: aluno4
REM Porta da aplicacao: 5030
REM ============================================

setlocal enabledelayedexpansion

set SERVER=verticaltecnologia.net.br
set PORT=47979
set USER=aluno4
set APP_PORT=5030
set APP_NAME=fasiclin-estoque
set REMOTE_DIR=/home/%USER%/%APP_NAME%

echo ============================================
echo Deploy Fasiclin Estoque para Fasitech
echo ============================================
echo.

REM 1. Build local
echo [1/4] Compilando aplicacao...
call mvnw.cmd clean package -DskipTests -Dspring.profiles.active=fasitech
if errorlevel 1 (
    echo ERRO: Falha na compilacao!
    exit /b 1
)

REM Encontrar o JAR
for %%f in (target\*.jar) do set JAR_FILE=%%f
if not defined JAR_FILE (
    echo ERRO: JAR nao encontrado em target\
    exit /b 1
)
echo JAR gerado: %JAR_FILE%

echo.
echo ============================================
echo Build concluido! Agora faca o deploy manual:
echo ============================================
echo.
echo 1. Conecte ao servidor via SSH:
echo    ssh -p %PORT% %USER%@%SERVER%
echo.
echo 2. Crie o diretorio (se nao existir):
echo    mkdir -p %REMOTE_DIR%
echo.
echo 3. Em outro terminal, copie o JAR:
echo    scp -P %PORT% %JAR_FILE% %USER%@%SERVER%:%REMOTE_DIR%/app.jar
echo.
echo 4. No servidor, inicie a aplicacao:
echo    cd %REMOTE_DIR%
echo    export SPRING_PROFILES_ACTIVE=fasitech
echo    export SERVER_PORT=5030
echo    nohup java -Xmx512m -jar app.jar ^> app.log 2^>^&1 ^&
echo.
echo 5. Verifique se esta rodando:
echo    curl http://localhost:%APP_PORT%/api/health
echo.
echo ============================================
echo Acesso: http://%SERVER%:%APP_PORT%
echo ============================================
echo.

pause

