# Render Start Script
# Este arquivo será executado para iniciar a aplicação

echo "🚀 Iniciando Fasiclin Estoque..."

# Encontrar o JAR
JAR_FILE=$(find target -name "*.jar" -type f | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "❌ Erro: JAR não encontrado!"
    exit 1
fi

echo "📦 Executando: $JAR_FILE"

# Executar aplicação com profile de produção
java -Dspring.profiles.active=prod \
     -Xmx512m \
     -Xms256m \
     -jar "$JAR_FILE"
