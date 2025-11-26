#!/bin/bash
# ============================================
# Script de Deploy para Fasitech SRVAP-DEV
# ============================================
# Servidor: verticaltecnologia.net.br -p 47979
# Usuario: aluno4
# Porta da aplicacao: 5030
# ============================================

set -e

# Configuracoes
SERVER="verticaltecnologia.net.br"
PORT="47979"
USER="aluno4"
APP_PORT="5030"
APP_NAME="fasiclin-estoque"
REMOTE_DIR="/home/$USER/$APP_NAME"

echo "============================================"
echo "Deploy Fasiclin Estoque para Fasitech"
echo "============================================"

# 1. Build local
echo ""
echo "[1/5] Compilando aplicacao..."
./mvnw clean package -DskipTests -Dspring.profiles.active=fasitech

# 2. Verificar se o JAR foi gerado
JAR_FILE=$(ls target/*.jar 2>/dev/null | head -1)
if [ -z "$JAR_FILE" ]; then
    echo "ERRO: JAR nao encontrado em target/"
    exit 1
fi
echo "JAR gerado: $JAR_FILE"

# 3. Criar diretorio remoto
echo ""
echo "[2/5] Preparando servidor remoto..."
ssh -p $PORT $USER@$SERVER "mkdir -p $REMOTE_DIR"

# 4. Copiar JAR para servidor
echo ""
echo "[3/5] Enviando JAR para servidor..."
scp -P $PORT $JAR_FILE $USER@$SERVER:$REMOTE_DIR/app.jar

# 5. Copiar script de inicializacao
echo ""
echo "[4/5] Configurando script de inicializacao..."
cat << 'EOF' | ssh -p $PORT $USER@$SERVER "cat > $REMOTE_DIR/start.sh"
#!/bin/bash
# Script de inicializacao da aplicacao

APP_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
APP_NAME="fasiclin-estoque"
JAR_FILE="$APP_DIR/app.jar"
LOG_FILE="$APP_DIR/app.log"
PID_FILE="$APP_DIR/app.pid"

# Configuracoes
export SPRING_PROFILES_ACTIVE=fasitech
export SERVER_PORT=5030
export JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"

# Funcao para parar aplicacao
stop_app() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null 2>&1; then
            echo "Parando aplicacao (PID: $PID)..."
            kill $PID
            sleep 5
            if ps -p $PID > /dev/null 2>&1; then
                echo "Forcando parada..."
                kill -9 $PID
            fi
        fi
        rm -f $PID_FILE
    fi
}

# Funcao para iniciar aplicacao
start_app() {
    echo "Iniciando aplicacao..."
    nohup java $JAVA_OPTS -jar $JAR_FILE > $LOG_FILE 2>&1 &
    echo $! > $PID_FILE
    echo "Aplicacao iniciada (PID: $(cat $PID_FILE))"
    echo "Logs: tail -f $LOG_FILE"
}

case "$1" in
    start)
        start_app
        ;;
    stop)
        stop_app
        ;;
    restart)
        stop_app
        start_app
        ;;
    status)
        if [ -f "$PID_FILE" ]; then
            PID=$(cat $PID_FILE)
            if ps -p $PID > /dev/null 2>&1; then
                echo "Aplicacao rodando (PID: $PID)"
            else
                echo "Aplicacao nao esta rodando (PID stale)"
            fi
        else
            echo "Aplicacao nao esta rodando"
        fi
        ;;
    logs)
        tail -f $LOG_FILE
        ;;
    *)
        echo "Uso: $0 {start|stop|restart|status|logs}"
        exit 1
        ;;
esac
EOF

ssh -p $PORT $USER@$SERVER "chmod +x $REMOTE_DIR/start.sh"

# 6. Reiniciar aplicacao
echo ""
echo "[5/5] Reiniciando aplicacao..."
ssh -p $PORT $USER@$SERVER "$REMOTE_DIR/start.sh restart"

echo ""
echo "============================================"
echo "Deploy concluido com sucesso!"
echo "============================================"
echo ""
echo "Acesse: http://$SERVER:$APP_PORT"
echo ""
echo "Comandos uteis:"
echo "  ssh -p $PORT $USER@$SERVER '$REMOTE_DIR/start.sh status'"
echo "  ssh -p $PORT $USER@$SERVER '$REMOTE_DIR/start.sh logs'"
echo "  ssh -p $PORT $USER@$SERVER '$REMOTE_DIR/start.sh restart'"
echo ""

