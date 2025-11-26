<div align="center">
  <img src="https://github.com/user-attachments/assets/20a36d8d-228f-410b-a4b8-992b18d334db" alt="Fasiclin Logo" width="450"/>
  <h1>📦 Fasiclin Estoque - Sistema de Ordem de Compra</h1>
  <p>
    <strong>Sistema completo de gerenciamento de Ordens de Compra com resilience offline, validações robustas e deploy automatizado</strong>
  </p>
  <p>
    <a href="#-sobre-o-projeto">Sobre</a> •
    <a href="#-diferenciais">Diferenciais</a> •
    <a href="#-funcionalidades">Funcionalidades</a> •
    <a href="#-arquitetura">Arquitetura</a> •
    <a href="#-tecnologias">Tecnologias</a> •
    <a href="#-como-executar">Como Executar</a> •
    <a href="#-deploy">Deploy</a>
  </p>

  ![Java](https://img.shields.io/badge/Java-24-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
  ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
  ![Maven](https://img.shields.io/badge/Maven-4.0.0-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
  ![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
  ![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)
  
  <p>
    <img src="https://img.shields.io/badge/Status-Production_Ready-success?style=for-the-badge" alt="Status">
    <img src="https://img.shields.io/badge/Offline_Support-✓-brightgreen?style=for-the-badge" alt="Offline">
    <img src="https://img.shields.io/badge/Cloud_Ready-Render-blueviolet?style=for-the-badge" alt="Cloud">
    <img src="https://img.shields.io/github/license/matheusassuncaoo/fasiclin-estoque?style=for-the-badge" alt="Licença">
  </p>
</div>

---

## 🎯 Sobre o Projeto

O **Fasiclin Estoque** é um sistema enterprise-grade de gerenciamento de **Ordens de Compra** que vai além de um simples CRUD. Desenvolvido com foco em **resilience**, **qualidade** e **produção**, o projeto implementa:

- ✅ **Sistema de Resilience Completo**: Funciona offline com cache LocalStorage e queue de operações
- ✅ **Validações Robustas**: Frontend + Backend com mensagens de erro claras
- ✅ **Status Workflow**: Controle de ciclo de vida (PEND → ANDA → CONC/CANC)
- ✅ **Deploy Automatizado**: Docker + Render com scripts prontos
- ✅ **Monitoramento em Tempo Real**: Health checks e status de conexão visual
- ✅ **Código Limpo**: Arquitetura modular, sem logs de debug, production-ready

Este projeto acadêmico demonstra **excelência técnica** e aplicação prática de padrões de mercado.

---

## 🌟 Diferenciais

### 🛡️ **Sistema de Resilience Enterprise**
```javascript
// Funciona MESMO quando a API cai!
- Cache inteligente com LocalStorage (30min-1h TTL)
- Queue persistente de operações (auto-sync quando online)
- Retry exponencial (1s → 2s → 4s)
- Indicador visual de conexão (verde/amarelo/vermelho)
```

### 🔒 **Validações em Múltiplas Camadas**
```java
// Backend + Frontend sincronizados
- Enum Status validado (PEND, ANDA, CONC, CANC)
- Bloqueio de edição em ordens concluídas/canceladas
- Data de entrega obrigatória
- Validação de fornecedor ativo
```

### 🚀 **Deploy Production-Ready**
```dockerfile
# Dockerfile multi-stage otimizado
- Imagem Alpine (< 200MB)
- Non-root user security
- Health checks automáticos
- Configuração via environment variables
```

### 📊 **Monitoramento e Health Checks**
```
GET /api/health          → Status geral
GET /api/health/detailed → Componentes (API, DB)
Frontend polling a cada 2min
```

---

## ✨ Funcionalidades Implementadas

### 📋 **Módulo de Ordens de Compra**
- [x] CRUD completo de Ordens de Compra
- [x] Workflow de status (Pendente → Andamento → Concluída/Cancelada)
- [x] Datas de criação/atualização automáticas
- [x] Associação com Fornecedor
- [x] Validação de regras de negócio

### 📦 **Módulo de Itens da Ordem**
- [x] CRUD de itens vinculados à ordem
- [x] Validação de quantidade e produto
- [x] Bloqueio de adição em ordens finalizadas
- [x] Relacionamento com Produto e Lote

### 🏭 **Módulo de Fornecedores**
- [x] CRUD completo de fornecedores
- [x] Controle de status ativo/inativo
- [x] Validação CNPJ e dados de contato
- [x] Relacionamento com ordens de compra

### 📊 **Módulo de Produtos**
- [x] CRUD completo de produtos
- [x] Controle de estoque mínimo
- [x] Relacionamento com itens e lotes
- [x] Status ativo/inativo

### 🗂️ **Módulo de Lotes**
- [x] Controle de lotes por produto
- [x] Rastreabilidade de entrada/saída
- [x] Data de validade e fabricação
- [x] Status do lote

### 💰 **Módulo de Movimentações Contábeis**
- [x] Registro de entradas/saídas financeiras
- [x] Vinculação com ordem de compra
- [x] Histórico de movimentações

### 🛡️ **Sistema de Resilience (Offline-First)**
- [x] **CacheManager**: Cache LocalStorage com TTL
- [x] **OfflineQueueManager**: Queue persistente com retry
- [x] **ConnectionStatusMonitor**: Indicador visual de status
- [x] **ApiManager**: Retry exponencial e fallback
- [x] Health check backend (/api/health)

---

## 🏗️ Arquitetura

### **Backend (Spring Boot 3.5.5)**
```
src/main/java/com/br/fasipe/estoque/
├── config/
│   ├── SecurityConfig.java      # CORS e segurança
│   └── WebConfig.java            # Configurações web
├── ordemcompra/
│   ├── controllers/              # REST endpoints
│   │   ├── OrdemCompraController
│   │   ├── ItemOrdemCompraController
│   │   ├── FornecedorController
│   │   ├── ProdutoController
│   │   ├── LoteController
│   │   ├── EstoqueController
│   │   ├── MovContabilController
│   │   └── HealthCheckController  # Monitoramento
│   ├── models/                    # Entidades JPA
│   ├── repository/                # Spring Data JPA
│   ├── services/                  # Lógica de negócio
│   └── dto/                       # Data Transfer Objects
```

### **Frontend (Vanilla JS Modular)**
```
frontend/
├── ordemcompra.html               # Interface principal
└── Assets/
    ├── js/
    │   ├── ApiManager.js          # HTTP client + resilience
    │   ├── CacheManager.js        # Cache LocalStorage
    │   ├── OfflineQueueManager.js # Queue de operações
    │   ├── ConnectionStatusMonitor.js # Indicador visual
    │   ├── OrdemCompraManager.js  # Lógica de ordens
    │   ├── OrdemCompraComponentsManager.js # UI components
    │   ├── FilterManager.js       # Filtros e busca
    │   ├── InputValidationManager.js # Validações
    │   └── NotificationManager.js # Toasts e notificações
    └── css/
        ├── global.css             # Estilos globais
        ├── ordemcompra.css        # Estilos específicos
        └── responsive.css         # Media queries
```

### **Fluxo de Resilience**
```
[Usuário] → [Frontend]
              ↓
         [ApiManager]
              ↓
    ┌─────────┴─────────┐
    ↓                   ↓
[Online?]           [Offline?]
    ↓                   ↓
[API Call]         [Cache GET]
    ↓                   ↓
[Success?]      [Queue POST/PUT/DELETE]
    ↓                   ↓
[Cache]          [LocalStorage]
    ↓                   ↓
[Retry 3x]      [Auto-sync quando online]
```

---

## 🚀 Tecnologias

### **Backend**
<div align="center">
  <img src="https://img.shields.io/badge/Java_24-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java"/>
  <img src="https://img.shields.io/badge/Spring_Boot_3.5.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Data"/>
  <img src="https://img.shields.io/badge/Hibernate_6.6-59666C?style=for-the-badge&logo=hibernate&logoColor=white" alt="Hibernate"/>
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven"/>
  <img src="https://img.shields.io/badge/MySQL_8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL"/>
</div>

### **Frontend**
<div align="center">
  <img src="https://img.shields.io/badge/JavaScript_ES6+-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black" alt="JavaScript"/>
  <img src="https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white" alt="HTML5"/>
  <img src="https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white" alt="CSS3"/>
  <img src="https://img.shields.io/badge/LocalStorage-FFA500?style=for-the-badge&logo=data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTEyIDJDNi40OCAyIDIgNi40OCAyIDEyczQuNDggMTAgMTAgMTAgMTAtNC40OCAxMC0xMFMxNy41MiAyIDEyIDJ6bTAgMThjLTQuNDEgMC04LTMuNTktOC04czMuNTktOCA4LTggOCAzLjU5IDggOC0zLjU5IDgtOCA4em0tMS00aDJ2LTJoLTJ2MnptMC00aDJWN2gtMnY1eiIgZmlsbD0iI2ZmZiIvPjwvc3ZnPg==" alt="LocalStorage"/>
</div>

### **DevOps & Deploy**
<div align="center">
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
  <img src="https://img.shields.io/badge/Render-46E3B7?style=for-the-badge&logo=render&logoColor=white" alt="Render"/>
  <img src="https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white" alt="Git"/>
  <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white" alt="GitHub"/>
</div>

### **Ferramentas**
<div align="center">
  <img src="https://img.shields.io/badge/VS_Code-007ACC?style=for-the-badge&logo=visualstudiocode&logoColor=white" alt="VSCode"/>
  <img src="https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white" alt="Postman"/>
  <img src="https://img.shields.io/badge/MySQL_Workbench-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="Workbench"/>
</div>

---

## 🛠️ Como Executar

### **Pré-requisitos**

Antes de começar, você vai precisar ter instalado:
-   [Java JDK 24](https://www.oracle.com/java/technologies/downloads/) ou superior
-   [Apache Maven](https://maven.apache.org/download.cgi) 3.9+
-   [MySQL Server](https://dev.mysql.com/downloads/mysql/) 8.0+
-   [Git](https://git-scm.com/downloads)
-   (Opcional) [Docker](https://www.docker.com/products/docker-desktop/) para execução containerizada

### **🐳 Execução com Docker (Recomendado)**

```bash
# 1. Clone o repositório
git clone https://github.com/matheusassuncaoo/fasiclin-estoque.git
cd fasiclin-estoque

# 2. Configure as variáveis de ambiente
# Crie um arquivo .env com:
DATABASE_URL=jdbc:mysql://seu-mysql:3306/fasiclin_db
DATABASE_USERNAME=seu_usuario
DATABASE_PASSWORD=sua_senha
CORS_ALLOWED_ORIGINS=http://localhost:3000

# 3. Build da imagem Docker
docker build -t fasiclin-estoque:latest .

# 4. Execute o container
docker run -p 8080:8080 \
  --env-file .env \
  fasiclin-estoque:latest

# ✅ API disponível em: http://localhost:8080
```

### **💻 Execução Local (Desenvolvimento)**

#### **1. Clone o repositório**
```bash
git clone https://github.com/matheusassuncaoo/fasiclin-estoque.git
cd fasiclin-estoque
```

#### **2. Configure o Banco de Dados**
Execute o script SQL para criar o banco de dados e tabelas:
```bash
# O arquivo fasiclin_db(1).sql está na raiz do projeto
mysql -u root -p < fasiclin_db(1).sql
```

#### **3. Configure a Conexão**
Edite `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fasiclin_db
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA
```

#### **4. Compile e Execute**
```bash
# Usando Maven Wrapper (recomendado)
./mvnw clean install
./mvnw spring-boot:run

# Ou usando Maven instalado
mvn clean install
mvn spring-boot:run
```

#### **5. Acesse a Aplicação**
```
Backend API: http://localhost:8080
Health Check: http://localhost:8080/api/health
Frontend: Abra frontend/ordemcompra.html no navegador
```

### **📡 Testando a API**

#### **Com cURL:**
```bash
# Listar todas as ordens de compra
curl http://localhost:8080/api/ordens-compra

# Health check
curl http://localhost:8080/api/health/detailed
```

#### **Com Postman:**
Importe a collection disponível em `/docs/postman_collection.json` (se disponível)

---

## ☁️ Deploy no Render

Este projeto está **100% pronto para deploy** no Render Cloud Platform.

### **📋 Pré-requisitos do Deploy**
1. Conta no [Render](https://render.com) (gratuita)
2. Banco de dados MySQL em nuvem ([Railway](https://railway.app) ou [PlanetScale](https://planetscale.com))
3. Repositório no GitHub

### **🚀 Deploy Rápido (5 minutos)**

#### **Passo 1: Configure o Banco de Dados**
```bash
# Opção A: Railway (Recomendado)
1. Acesse railway.app e crie um projeto MySQL
2. Copie a DATABASE_URL fornecida
3. Execute o script fasiclin_db(1).sql no Railway

# Opção B: PlanetScale
1. Crie um banco MySQL em planetscale.com
2. Copie a connection string
3. Execute o script de criação das tabelas
```

#### **Passo 2: Conecte ao GitHub**
```bash
# 1. Commit e push dos arquivos de deploy
git add Dockerfile build.sh start.sh application-prod.properties
git commit -m "🚀 Deploy: Add Render configuration"
git push origin main

# 2. No Render Dashboard:
# - New → Web Service
# - Connect Repository → fasiclin-estoque
# - Branch: main
```

#### **Passo 3: Configure o Render**
```yaml
# No painel do Render, configure:
Name: fasiclin-estoque
Region: Oregon (US West)
Branch: main
Build Command: chmod +x build.sh && ./build.sh
Start Command: chmod +x start.sh && ./start.sh

# Environment Variables:
DATABASE_URL=jdbc:mysql://railway-host:3306/fasiclin_db?sslMode=REQUIRED
DATABASE_USERNAME=seu_usuario
DATABASE_PASSWORD=sua_senha
CORS_ALLOWED_ORIGINS=https://seu-dominio.com
SPRING_PROFILES_ACTIVE=prod
```

#### **Passo 4: Deploy!**
```bash
# Clique em "Create Web Service"
# ⏱️ Aguarde 3-5 minutos para o build
# ✅ Acesse: https://fasiclin-estoque.onrender.com/api/health
```

### **📖 Documentação Completa de Deploy**
Para guia detalhado, consulte: [`DEPLOY_RENDER.md`](./DEPLOY_RENDER.md)

---

## 📊 Endpoints da API

### **Ordens de Compra**
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/ordens-compra` | Lista todas as ordens |
| GET | `/api/ordens-compra/{id}` | Busca por ID |
| POST | `/api/ordens-compra` | Cria nova ordem |
| PUT | `/api/ordens-compra/{id}` | Atualiza ordem |
| PUT | `/api/ordens-compra/{id}/status` | Atualiza status |
| DELETE | `/api/ordens-compra/{id}` | Remove ordem |

### **Itens da Ordem**
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/itens-ordem/{ordemId}` | Lista itens da ordem |
| POST | `/api/itens-ordem/{ordemId}` | Adiciona item |
| PUT | `/api/itens-ordem/{id}` | Atualiza item |
| DELETE | `/api/itens-ordem/{id}` | Remove item |

### **Fornecedores**
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/fornecedores` | Lista fornecedores |
| GET | `/api/fornecedores/ativos` | Lista ativos |
| POST | `/api/fornecedores` | Cria fornecedor |
| PUT | `/api/fornecedores/{id}` | Atualiza fornecedor |
| PUT | `/api/fornecedores/{id}/desativar` | Desativa |

### **Produtos**
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/produtos` | Lista produtos |
| GET | `/api/produtos/ativos` | Lista ativos |
| POST | `/api/produtos` | Cria produto |
| PUT | `/api/produtos/{id}` | Atualiza produto |

### **Health Check**
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/health` | Status básico |
| GET | `/api/health/detailed` | Status detalhado |

---

## 🛡️ Sistema de Resilience

### **Como Funciona?**

O sistema continua operacional mesmo quando:
- ❌ API está fora do ar
- ❌ Banco de dados está inacessível
- ❌ Conexão de internet instável

#### **Componentes:**

1. **CacheManager** - Cache inteligente
   - Armazena GETs no LocalStorage
   - TTL de 30min-1h por tipo de dado
   - Limpeza automática de cache expirado

2. **OfflineQueueManager** - Fila de operações
   - Enfileira POST/PUT/DELETE quando offline
   - Sincroniza automaticamente quando volta online
   - Retry com exponential backoff (1s → 2s → 4s)

3. **ConnectionStatusMonitor** - Indicador visual
   - 🟢 Verde: Online e funcionando
   - 🟡 Amarelo: Offline mas operacional (cache)
   - 🔴 Vermelho: Offline com limitações

4. **ApiManager** - Cliente HTTP resiliente
   - Tenta 3 vezes antes de desistir
   - Fallback para cache em caso de falha
   - Health check automático a cada 2 minutos

### **Testando a Resilience:**
```bash
# 1. Abra o frontend: ordemcompra.html
# 2. Desligue a API (Ctrl+C no terminal)
# 3. Observe o indicador ficar amarelo/vermelho
# 4. Tente listar ordens → Vem do cache!
# 5. Tente criar ordem → Vai para a fila!
# 6. Ligue a API novamente
# 7. Observe a sincronização automática ✨
```

---

## 📁 Estrutura do Projeto

```
fasiclin-estoque/
├── 📂 src/main/java/com/br/fasipe/estoque/
│   ├── EstoqueApplication.java          # Entry point
│   ├── config/
│   │   ├── SecurityConfig.java          # CORS e segurança
│   │   └── WebConfig.java
│   └── ordemcompra/
│       ├── controllers/                 # REST endpoints
│       │   ├── OrdemCompraController.java
│       │   ├── ItemOrdemCompraController.java
│       │   ├── FornecedorController.java
│       │   ├── ProdutoController.java
│       │   ├── LoteController.java
│       │   ├── EstoqueController.java
│       │   ├── MovContabilController.java
│       │   └── HealthCheckController.java
│       ├── models/                      # Entidades JPA
│       ├── repository/                  # Spring Data JPA
│       ├── services/                    # Business logic
│       └── dto/                         # DTOs
├── 📂 src/main/resources/
│   ├── application.properties           # Config desenvolvimento
│   └── application-prod.properties      # Config produção
├── 📂 frontend/
│   ├── ordemcompra.html                 # Interface principal
│   └── Assets/
│       ├── js/
│       │   ├── ApiManager.js            # HTTP + resilience
│       │   ├── CacheManager.js          # LocalStorage cache
│       │   ├── OfflineQueueManager.js   # Queue offline
│       │   ├── ConnectionStatusMonitor.js
│       │   ├── OrdemCompraManager.js
│       │   ├── OrdemCompraComponentsManager.js
│       │   ├── FilterManager.js
│       │   ├── InputValidationManager.js
│       │   └── NotificationManager.js
│       └── css/
│           ├── global.css
│           ├── ordemcompra.css
│           └── responsive.css
├── 📄 Dockerfile                        # Container multi-stage
├── 📄 build.sh                          # Script de build (Render)
├── 📄 start.sh                          # Script de start (Render)
├── 📄 fasiclin_db(1).sql               # Schema do banco
├── 📄 DEPLOY_RENDER.md                 # Guia de deploy
└── 📄 pom.xml                          # Maven dependencies
```

---

## 🎓 Aprendizados e Boas Práticas

Este projeto demonstra:

✅ **Arquitetura em Camadas** (Controller → Service → Repository)  
✅ **Separation of Concerns** (módulos frontend independentes)  
✅ **Error Handling** (try/catch, validações, mensagens claras)  
✅ **Clean Code** (nomenclatura, comentários, modularização)  
✅ **Resilience Patterns** (retry, fallback, circuit breaker)  
✅ **Production-Ready** (Docker, health checks, configuração por ambiente)  
✅ **Security** (CORS, non-root containers, sanitização)  
✅ **User Experience** (feedback visual, validações, notificações)

---

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## ✨ Contribuidores

Desenvolvido com 💙 por:

<a href="https://github.com/matheusassuncaoo/fasiclin-estoque/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=matheusassuncaoo/fasiclin-estoque" alt="Contribuidores"/>
</a>

---

<div align="center">
  <p>
    <strong>⭐ Se este projeto te ajudou, deixe uma estrela no repositório!</strong>
  </p>
  <p>
    Feito com ☕ e muito código
  </p>
</div>
