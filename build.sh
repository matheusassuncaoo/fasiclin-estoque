# Render Build Script
# Este arquivo será executado automaticamente pelo Render durante o deploy

echo "🚀 Iniciando build do Fasiclin Estoque..."

# Garantir que tem permissão de execução no mvnw
chmod +x mvnw

# Limpar builds anteriores
echo "🧹 Limpando builds anteriores..."
./mvnw clean

# Build da aplicação (pulando testes para build mais rápido)
echo "🔨 Compilando aplicação..."
./mvnw package -DskipTests

echo "✅ Build concluído com sucesso!"
echo "📦 JAR gerado em: target/estoque-0.0.1-SNAPSHOT.jar"
