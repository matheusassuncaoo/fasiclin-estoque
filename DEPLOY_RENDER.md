# 🚀 Deploy no Render - Guia Completo

## 📋 Pré-requisitos

1. Conta no [Render](https://render.com) (grátis)
2. Conta no GitHub (seu repositório já está lá)
3. Banco de dados MySQL (pode usar Render, Railway ou outro)

## 🗄️ Passo 1: Configurar Banco de Dados

### Opção A: Render PostgreSQL (Grátis)
⚠️ **Nota**: Render oferece PostgreSQL grátis, não MySQL. Precisamos adaptar.

### Opção B: Railway MySQL (Recomendado)
1. Acesse [Railway.app](https://railway.app)
2. Crie novo projeto
3. Adicione MySQL
4. Copie a **DATABASE_URL**

### Opção C: PlanetScale MySQL (Grátis)
1. Acesse [PlanetScale](https://planetscale.com)
2. Crie database
3. Obtenha connection string

## 🎯 Passo 2: Preparar Repositório

1. **Commit todos os arquivos novos:**
```bash
git add .
git commit -m "feat: preparar para deploy no Render com sistema de resiliência"
git push origin feature/ordemcompra
```

2. **Merge para main (se necessário):**
```bash
git checkout main
git merge feature/ordemcompra
git push origin main
```

## 🌐 Passo 3: Deploy no Render

### 3.1. Criar Web Service

1. Acesse [Render Dashboard](https://dashboard.render.com)
2. Clique **"New +"** → **"Web Service"**
3. Conecte seu repositório GitHub
4. Selecione o repositório **fasiclin-estoque**
5. Configure:

```yaml
Name: fasiclin-estoque
Region: Oregon (ou mais próximo)
Branch: main
Root Directory: (deixe vazio)
Runtime: Docker  # OU Java se preferir
Build Command: ./build.sh
Start Command: ./start.sh
```

### 3.2. Configurar Variáveis de Ambiente

Adicione estas variáveis em **Environment**:

```bash
# Database (Railway/PlanetScale)
DATABASE_URL=jdbc:mysql://HOST:PORT/DATABASE?useSSL=true
DATABASE_USERNAME=seu_usuario
DATABASE_PASSWORD=sua_senha

# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# CORS (depois que tiver a URL do Render)
CORS_ALLOWED_ORIGINS=https://seu-app.onrender.com

# Pool de Conexões
DB_POOL_SIZE=5

# JVM Options
JAVA_OPTS=-Xmx512m -Xms256m
```

### 3.3. Deploy!

1. Clique **"Create Web Service"**
2. Aguarde build (5-10 minutos na primeira vez)
3. ✅ Aplicação estará em: `https://fasiclin-estoque.onrender.com`

## 🎨 Passo 4: Deploy do Frontend

### Opção A: Servir pelo Spring Boot (Mais Simples)

Os arquivos HTML/JS/CSS já estão em `src/main/resources/frontend/`.
Acesse: `https://seu-app.onrender.com/ordemcompra.html`

### Opção B: Vercel/Netlify Separado (Mais Profissional)

1. **Criar pasta separada para frontend:**
```bash
# Copiar frontend para raiz
cp -r frontend/ deploy-frontend/
```

2. **Deploy no Vercel:**
   - Acesse [Vercel](https://vercel.com)
   - Importe repositório
   - Configure root directory: `deploy-frontend`
   - Deploy!

3. **Atualizar URLs no frontend:**
   - Editar `ApiManager.js`:
   ```javascript
   this.baseURL = "https://fasiclin-estoque.onrender.com/api";
   ```

## 🔧 Passo 5: Configurações Pós-Deploy

### 5.1. Atualizar CORS

No Render, adicione variável:
```bash
CORS_ALLOWED_ORIGINS=https://fasiclin-estoque.onrender.com,https://seu-frontend.vercel.app
```

### 5.2. Criar Tabelas no Banco

**Opção A: Flyway/Liquibase (Profissional)**
- Migrations automáticas
- (Precisa configurar)

**Opção B: Manual (Rápido)**
```sql
-- Execute o arquivo fasiclin_db(1).sql no seu banco Railway/PlanetScale
```

### 5.3. Configurar Custom Domain (Opcional)

1. No Render: Settings → Custom Domain
2. Adicione seu domínio
3. Configure DNS conforme instruções

## 📊 Passo 6: Monitoramento

### Health Check Automático
Render verifica: `https://seu-app.onrender.com/api/health`

### Logs
```bash
# Ver logs em tempo real no Render Dashboard
# Ou via CLI:
render logs
```

### Metrics
- Dashboard do Render mostra CPU, memória, requests
- Configurar alertas se quiser

## ⚡ Otimizações para Produção

### 1. Reduzir Tempo de Build
Adicionar no `pom.xml`:
```xml
<properties>
    <maven.test.skip>true</maven.test.skip>
</properties>
```

### 2. Habilitar Compressão
Já configurado em `application-prod.properties`

### 3. Cache de Dependências
Render faz automaticamente

## 🐛 Troubleshooting

### Erro: "Port already in use"
- Render injeta variável `PORT` automaticamente
- Já configurado em `application-prod.properties`

### Erro: "Database connection failed"
```bash
# Testar conexão local primeiro:
mysql -h HOST -P PORT -u USER -p DATABASE
```

### Erro: "Build failed"
```bash
# Dar permissão aos scripts:
chmod +x build.sh start.sh mvnw
git add .
git commit -m "fix: permissões de execução"
git push
```

### App muito lento (Render Free Tier)
- Normal: instâncias grátis "dormem" após 15min inatividade
- Primeira requisição demora ~30s (cold start)
- Soluções:
  - Upgrade para plano pago ($7/mês)
  - Usar cron job para manter ativo

## 🎯 Checklist Final

- [ ] Banco de dados criado e acessível
- [ ] Variáveis de ambiente configuradas no Render
- [ ] Scripts `build.sh` e `start.sh` com permissão de execução
- [ ] Código commitado e pushed para GitHub
- [ ] Web Service criado no Render
- [ ] Build completou com sucesso
- [ ] Health check respondendo: `/api/health`
- [ ] Frontend acessível
- [ ] CORS configurado corretamente
- [ ] Tabelas criadas no banco
- [ ] Testado criar/listar ordens de compra

## 🌟 URLs Finais

```
Backend API: https://fasiclin-estoque.onrender.com
Frontend: https://fasiclin-estoque.onrender.com/ordemcompra.html
Health Check: https://fasiclin-estoque.onrender.com/api/health
API Docs: https://fasiclin-estoque.onrender.com/swagger-ui.html
```

## 💡 Dica Pro

Adicione badge no README:
```markdown
[![Deploy](https://img.shields.io/badge/deploy-render-brightgreen)](https://fasiclin-estoque.onrender.com)
```

---

**🎓 Para o Professor**: Sistema está pronto para produção com monitoramento, health checks, logs estruturados e resiliência completa!
