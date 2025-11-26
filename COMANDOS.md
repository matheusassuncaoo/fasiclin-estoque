# 🚀 Guia Rápido de Comandos - Fasiclin Estoque

## 📦 Build e Execução Local

### Compilar o projeto
```bash
# Usando Maven Wrapper (recomendado)
./mvnw clean install

# Ou com Maven instalado
mvn clean install

# Compilar sem executar testes
./mvnw clean install -DskipTests
```

### Executar a aplicação
```bash
# Modo desenvolvimento
./mvnw spring-boot:run

# Ou executando o JAR diretamente
java -jar target/estoque-0.0.1-SNAPSHOT.jar

# Com profile de produção
java -jar target/estoque-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Executar apenas os testes
```bash
./mvnw test

# Testes com relatório de cobertura
./mvnw test jacoco:report
```

---

## 🐳 Docker

### Build da imagem
```bash
# Build simples
docker build -t fasiclin-estoque:latest .

# Build com tag específica
docker build -t fasiclin-estoque:1.0.0 .

# Build sem cache (force rebuild)
docker build --no-cache -t fasiclin-estoque:latest .
```

### Executar container
```bash
# Execução básica
docker run -p 8080:8080 fasiclin-estoque:latest

# Com variáveis de ambiente
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:mysql://localhost:3306/fasiclin_db \
  -e DATABASE_USERNAME=root \
  -e DATABASE_PASSWORD=senha123 \
  fasiclin-estoque:latest

# Com arquivo .env
docker run -p 8080:8080 --env-file .env fasiclin-estoque:latest

# Modo daemon (background)
docker run -d -p 8080:8080 --name fasiclin fasiclin-estoque:latest
```

### Gerenciar containers
```bash
# Listar containers em execução
docker ps

# Parar container
docker stop fasiclin

# Remover container
docker rm fasiclin

# Ver logs
docker logs fasiclin
docker logs -f fasiclin  # Follow mode

# Acessar shell do container
docker exec -it fasiclin sh
```

---

## ☁️ Deploy no Render

### Preparar para deploy
```bash
# 1. Commit das alterações
git add .
git commit -m "🚀 Deploy: Production ready"
git push origin main

# 2. Verificar se os scripts têm permissão (Linux/Mac)
chmod +x build.sh start.sh

# 3. Testar build localmente (simula Render)
./build.sh
./start.sh
```

### Comandos do Render (via dashboard)
```bash
# Build Command (configurar no Render)
chmod +x build.sh && ./build.sh

# Start Command (configurar no Render)
chmod +x start.sh && ./start.sh
```

---

## 🗄️ Banco de Dados

### MySQL Local
```bash
# Conectar ao MySQL
mysql -u root -p

# Criar banco de dados
CREATE DATABASE fasiclin_db;

# Importar script SQL
mysql -u root -p fasiclin_db < fasiclin_db(1).sql

# Verificar tabelas criadas
USE fasiclin_db;
SHOW TABLES;

# Ver estrutura de uma tabela
DESCRIBE ordem_compra;
```

### MySQL Railway (Produção)
```bash
# Conectar via Railway CLI
railway connect

# Ou via MySQL client com URL do Railway
mysql -h containers-us-west-XXX.railway.app -u root -p -P 6543

# Importar dados para Railway
mysql -h containers-us-west-XXX.railway.app -u root -p -P 6543 railway < fasiclin_db(1).sql
```

---

## 🧪 Testes e Validação

### Testar health check
```bash
# Local
curl http://localhost:8080/api/health
curl http://localhost:8080/api/health/detailed

# Produção (Render)
curl https://fasiclin-estoque.onrender.com/api/health
```

### Testar endpoints da API
```bash
# Listar ordens de compra
curl http://localhost:8080/api/ordens-compra

# Criar ordem (POST)
curl -X POST http://localhost:8080/api/ordens-compra \
  -H "Content-Type: application/json" \
  -d '{
    "dataEntrega": "2024-12-31",
    "observacoes": "Teste via cURL",
    "fornecedor": {"id": 1}
  }'

# Buscar por ID
curl http://localhost:8080/api/ordens-compra/1

# Atualizar status
curl -X PUT http://localhost:8080/api/ordens-compra/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "ANDA"}'

# Listar fornecedores ativos
curl http://localhost:8080/api/fornecedores/ativos
```

### Validar build do Docker
```bash
# Verificar tamanho da imagem
docker images fasiclin-estoque

# Inspecionar camadas da imagem
docker history fasiclin-estoque:latest

# Validar health check
docker inspect --format='{{.Config.Healthcheck}}' fasiclin-estoque
```

---

## 🔍 Debug e Troubleshooting

### Ver logs da aplicação
```bash
# Logs do Spring Boot (local)
tail -f logs/spring.log

# Logs do Docker
docker logs fasiclin -f

# Logs do Render
# Acessar via Dashboard > Logs
```

### Verificar portas em uso
```bash
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080
```

### Limpar cache do Maven
```bash
./mvnw clean
rm -rf ~/.m2/repository  # Limpar cache completo
```

### Recriar banco de dados
```bash
# MySQL
mysql -u root -p
DROP DATABASE fasiclin_db;
CREATE DATABASE fasiclin_db;
exit;

# Reimportar
mysql -u root -p fasiclin_db < fasiclin_db(1).sql
```

---

## 📊 Swagger UI

### Acessar documentação interativa
```bash
# Local
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs

# Produção
https://fasiclin-estoque.onrender.com/swagger-ui.html
```

---

## 🛠️ Comandos Git Úteis

### Workflow básico
```bash
# Verificar status
git status

# Adicionar alterações
git add .

# Commit
git commit -m "feat: Adicionar funcionalidade X"

# Push
git push origin main

# Ver histórico
git log --oneline --graph --all

# Criar nova branch
git checkout -b feature/nova-funcionalidade

# Voltar para main
git checkout main
```

### Deploy urgente
```bash
# Commit rápido + push
git add . && git commit -m "🚀 Deploy" && git push origin main

# Force push (cuidado!)
git push --force origin main
```

---

## 📝 Variáveis de Ambiente

### Arquivo .env de exemplo
```bash
# Crie um arquivo .env na raiz do projeto com:
DATABASE_URL=jdbc:mysql://localhost:3306/fasiclin_db
DATABASE_USERNAME=root
DATABASE_PASSWORD=sua_senha
CORS_ALLOWED_ORIGINS=http://localhost:3000
SPRING_PROFILES_ACTIVE=prod
```

### Exportar variáveis (Linux/Mac)
```bash
export DATABASE_URL=jdbc:mysql://localhost:3306/fasiclin_db
export DATABASE_USERNAME=root
export DATABASE_PASSWORD=senha123
```

### Exportar variáveis (Windows CMD)
```cmd
set DATABASE_URL=jdbc:mysql://localhost:3306/fasiclin_db
set DATABASE_USERNAME=root
set DATABASE_PASSWORD=senha123
```

### Exportar variáveis (Windows PowerShell)
```powershell
$env:DATABASE_URL="jdbc:mysql://localhost:3306/fasiclin_db"
$env:DATABASE_USERNAME="root"
$env:DATABASE_PASSWORD="senha123"
```

---

## 🎯 Comandos para Apresentação

### Antes de mostrar para o professor
```bash
# 1. Verificar se tudo compila
./mvnw clean compile

# 2. Rodar testes
./mvnw test

# 3. Build completo
./mvnw clean install

# 4. Iniciar aplicação
./mvnw spring-boot:run

# 5. Em outro terminal, testar health
curl http://localhost:8080/api/health

# 6. Abrir frontend
# Navegador: file:///C:/caminho/para/frontend/ordemcompra.html

# 7. Abrir Swagger
# Navegador: http://localhost:8080/swagger-ui.html
```

---

## 🚑 Comandos de Emergência

### Aplicação não inicia
```bash
# 1. Verificar Java
java -version  # Deve ser 17+

# 2. Limpar build
./mvnw clean

# 3. Rebuild
./mvnw install -DskipTests

# 4. Verificar banco
mysql -u root -p -e "SHOW DATABASES;"

# 5. Verificar porta 8080
netstat -ano | findstr :8080
```

### Build falha
```bash
# Atualizar dependências
./mvnw dependency:purge-local-repository

# Rebuild forçado
./mvnw clean install -U

# Pular testes se estiverem falhando
./mvnw install -DskipTests
```

### Docker não funciona
```bash
# Rebuild sem cache
docker build --no-cache -t fasiclin-estoque:latest .

# Remover containers antigos
docker rm -f $(docker ps -aq)

# Limpar imagens não usadas
docker image prune -a
```

---

## 📱 Testes Frontend (Resilience)

### Simular API offline
```bash
# 1. Iniciar aplicação normalmente
./mvnw spring-boot:run

# 2. Abrir frontend no navegador
# file:///C:/caminho/para/frontend/ordemcompra.html

# 3. Verificar que lista ordens (online - verde)

# 4. Parar o backend (Ctrl+C)

# 5. Tentar listar ordens → Deve vir do cache (amarelo)

# 6. Tentar criar ordem → Vai para fila (vermelho)

# 7. Reiniciar backend
./mvnw spring-boot:run

# 8. Observar sincronização automática
```

---

## 🎓 Comandos para Demonstração

### Sequência de demo perfeita
```bash
# 1. Mostrar build limpo
./mvnw clean install
# ✅ BUILD SUCCESS

# 2. Mostrar testes passando
./mvnw test
# ✅ Tests run: X, Failures: 0, Errors: 0

# 3. Executar aplicação
./mvnw spring-boot:run
# ✅ Started EstoqueApplication in X seconds

# 4. Em outro terminal, testar health
curl http://localhost:8080/api/health/detailed
# ✅ {"status":"UP","timestamp":"..."}

# 5. Abrir Swagger
# Navegador: http://localhost:8080/swagger-ui.html
# ✅ Mostrar interface interativa

# 6. Testar frontend
# file:///C:/caminho/frontend/ordemcompra.html
# ✅ Criar ordem, adicionar item, mostrar resilience

# 7. Mostrar app em produção
# https://fasiclin-estoque.onrender.com/api/health
# ✅ Funcionando na cloud!
```

---

**💡 Dica:** Mantenha este guia aberto em uma aba do navegador durante o desenvolvimento!
