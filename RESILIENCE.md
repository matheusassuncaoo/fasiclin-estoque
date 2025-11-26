# 🛡️ Sistema de Resiliência e Continuidade Operacional

## 📋 Visão Geral

O Sistema Fasiclin agora possui um robusto conjunto de mecanismos de resiliência que garantem operação contínua mesmo quando a API ou banco de dados estão indisponíveis.

## 🎯 Componentes de Resiliência

### 1. **CacheManager** - Cache Local Inteligente
Armazena dados críticos no navegador para acesso offline.

**Características:**
- ✅ Cache automático de dados GET (ordens, produtos, fornecedores)
- ✅ Expiração configurável por tipo de dado
- ✅ Limpeza automática de cache expirado
- ✅ Gerenciamento inteligente de quota do LocalStorage
- ✅ Versionamento de cache

**Tempos de Expiração:**
- Ordens de Compra: 30 minutos
- Produtos: 1 hora
- Fornecedores: 1 hora
- Itens: 30 minutos

### 2. **OfflineQueueManager** - Fila de Sincronização
Enfileira operações quando API está offline.

**Características:**
- ✅ Armazena operações POST/PUT/DELETE quando offline
- ✅ Sincronização automática quando conexão retorna
- ✅ Retry com backoff exponencial (até 5 tentativas)
- ✅ Persistência em LocalStorage
- ✅ Fila de operações falhadas separada
- ✅ Listeners de rede (online/offline)

### 3. **ApiManager Aprimorado** - Comunicação Resiliente
Sistema de requisições HTTP com fallback inteligente.

**Características:**
- ✅ Retry automático com backoff exponencial (1s, 2s, 4s)
- ✅ Fallback para cache em caso de falha
- ✅ Enfileiramento automático de operações de escrita
- ✅ Health check periódico da API
- ✅ Detecção de conectividade
- ✅ Cache automático de respostas GET

### 4. **ConnectionStatusMonitor** - Monitor Visual
Indicador visual do status da conexão.

**Características:**
- ✅ Indicador flutuante no canto inferior direito
- ✅ Status em tempo real (Online/Offline/Degradado)
- ✅ Painel de detalhes expandível
- ✅ Estatísticas de cache e fila
- ✅ Botão de sincronização manual
- ✅ Botão de limpeza de cache

## 🚀 Como Funciona

### Cenário 1: API Online
```
Usuário → Requisição → API → Resposta
                              ↓
                           Cache (salva)
```

### Cenário 2: API Offline (GET)
```
Usuário → Requisição → Cache → Dados Armazenados
                 ↓
            Aviso: "Usando dados em cache"
```

### Cenário 3: API Offline (POST/PUT/DELETE)
```
Usuário → Operação → Fila Offline → Aguarda Conexão
                          ↓
                   Aviso: "Operação enfileirada"
                          
Quando API Retorna:
Fila → Sincronização Automática → API → Sucesso
```

### Cenário 4: Tentativas de Reconexão
```
Tentativa 1 → Falha → Aguarda 1s
Tentativa 2 → Falha → Aguarda 2s
Tentativa 3 → Falha → Aguarda 4s
                     ↓
              Enfileira ou usa Cache
```

## 📊 Indicadores Visuais

### Status do Sistema
- 🟢 **Verde (Online)**: API disponível, conexão estável
- 🟡 **Amarelo (Degradado)**: Conexão OK mas com fila de sincronização
- 🔴 **Vermelho (Offline)**: Sem internet ou API indisponível

### Painel de Detalhes
Clique no indicador para ver:
- **Internet**: Status da conexão de rede
- **API**: Disponibilidade do backend
- **Fila**: Número de operações pendentes
- **Cache**: Quantidade de dados armazenados

## 🔧 Configuração

### Tempos de Timeout e Retry
```javascript
// ApiManager.js
this.timeout = 30000;        // 30 segundos
this.maxRetries = 3;          // 3 tentativas
this.retryDelay = 1000;       // 1 segundo inicial
```

### Expiração de Cache
```javascript
// CacheManager.js
this.CACHE_EXPIRY = {
  ordens: 30 * 60 * 1000,      // 30 minutos
  produtos: 60 * 60 * 1000,    // 1 hora
  fornecedores: 60 * 60 * 1000 // 1 hora
};
```

## 🎮 Uso

### Modo Automático
O sistema funciona automaticamente sem intervenção do usuário:
1. Detecta perda de conexão
2. Usa cache para leitura
3. Enfileira operações de escrita
4. Sincroniza quando conexão retorna
5. Notifica usuário em cada etapa

### Modo Manual
Através do monitor de conexão:
- **Sincronizar**: Força tentativa de sincronização
- **Limpar Cache**: Remove todos os dados em cache

## 🧪 Testando a Resiliência

### Teste 1: Simular API Offline
1. Parar o backend Spring Boot
2. Navegar no sistema
3. Ver dados em cache sendo usados
4. Tentar criar ordem (será enfileirada)
5. Reiniciar backend
6. Ver sincronização automática

### Teste 2: Simular Rede Lenta
1. Chrome DevTools → Network → Slow 3G
2. Fazer operações no sistema
3. Ver retries automáticos
4. Ver cache sendo usado

### Teste 3: Simular Perda Total
1. Desabilitar rede no SO
2. Sistema mostra indicador vermelho
3. Operações são enfileiradas
4. Reabilitar rede
5. Sincronização automática ocorre

## 📈 Métricas e Monitoramento

### Estatísticas de Cache
```javascript
window.cacheManager.getStats()
// Retorna: {
//   enabled: true,
//   totalItems: 25,
//   totalSize: 152340,
//   totalSizeKB: "148.77",
//   byType: {
//     ordens: { count: 10, size: 82400 },
//     produtos: { count: 15, size: 69940 }
//   }
// }
```

### Status da Fila
```javascript
window.offlineQueue.getStatus()
// Retorna: {
//   isOnline: true,
//   isSyncing: false,
//   queueLength: 3,
//   operations: [...]
// }
```

## 🔒 Segurança

- Cache é armazenado apenas no navegador do usuário
- Dados sensíveis seguem política de expiração
- Limpeza automática de dados antigos
- Sem transmissão de dados em cache para terceiros

## ⚡ Performance

- Cache em LocalStorage (até 5-10 MB)
- Acesso instantâneo a dados em cache
- Sincronização assíncrona (não bloqueia UI)
- Limpeza automática de cache antigo

## 🐛 Troubleshooting

### Cache não funciona
```javascript
// Verificar se LocalStorage está disponível
window.cacheManager.enabled // deve ser true
```

### Fila não sincroniza
```javascript
// Forçar sincronização manual
window.offlineQueue.sync()
```

### Limpar tudo e recomeçar
```javascript
window.cacheManager.clearAll()
window.offlineQueue.clear()
window.offlineQueue.clearFailedQueue()
```

## 📝 Logs e Debug

Todos os componentes logam no console:
- `[CacheManager]` - Operações de cache
- `[OfflineQueue]` - Fila e sincronização
- `[ApiManager]` - Requisições HTTP
- `[ConnectionMonitor]` - Status de conexão

## 🎓 Boas Práticas

1. **Não desabilitar cache** - Garante operação offline
2. **Monitorar fila** - Ver se operações estão sendo sincronizadas
3. **Verificar indicador** - Status visual mostra saúde do sistema
4. **Limpar cache periodicamente** - Evita dados obsoletos

## 🚨 Alertas ao Usuário

O sistema notifica automaticamente:
- ✅ "Conexão restabelecida! Sincronizando..."
- ⚠️ "API indisponível. Usando dados em cache."
- ⚠️ "Operação enfileirada para sincronização"
- ✅ "Operação sincronizada: criar ordem"
- ❌ "Falha ao sincronizar: operação"

## 📦 Arquivos do Sistema

```
frontend/Assets/js/
├── CacheManager.js              # Gerenciamento de cache
├── OfflineQueueManager.js       # Fila de sincronização
├── ConnectionStatusMonitor.js   # Monitor visual
└── ApiManager.js               # HTTP client (aprimorado)

backend/src/.../controllers/
└── HealthCheckController.java  # Endpoint de health check
```

## 🎯 Benefícios

✅ **Continuidade**: Sistema funciona mesmo offline  
✅ **Resiliência**: Falhas são tratadas automaticamente  
✅ **UX**: Usuário sempre informado do status  
✅ **Integridade**: Operações não são perdidas  
✅ **Performance**: Cache reduz chamadas à API  
✅ **Confiabilidade**: Retry automático de operações  

---

**Sistema desenvolvido para garantir operação 24/7 mesmo em condições adversas de rede ou infraestrutura.**
