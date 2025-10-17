/**
 * NotificationManager - Sistema de Notificações
 * Gerencia notificações visuais para feedback do usuário
 * Suporta diferentes tipos: success, error, warning, info
 */
class NotificationManager {
  constructor() {
    this.notifications = new Map();
    this.defaultDuration = 5000; // 5 segundos
    this.maxNotifications = 5;
    this.container = null;

    this.initializeContainer();
    this.setupStyles();
  }

  /**
   * Inicializa o container de notificações
   */
  initializeContainer() {
    // Verificar se já existe um container
    this.container = document.getElementById("notification-container");

    if (!this.container) {
      this.container = document.createElement("div");
      this.container.id = "notification-container";
      this.container.className = "notification-container";
      document.body.appendChild(this.container);
    }
  }

  /**
   * Configura os estilos do container se necessário
   */
  setupStyles() {
    // Verificar se os estilos já foram injetados
    if (document.getElementById("notification-styles")) {
      return;
    }

    const styles = document.createElement("style");
    styles.id = "notification-styles";
    styles.textContent = `
            .notification-container {
                position: fixed;
                top: 20px;
                right: 20px;
                z-index: 10000;
                pointer-events: none;
                max-width: 400px;
            }

            .notification-item {
                display: flex;
                align-items: center;
                gap: 12px;
                padding: 16px 20px;
                margin-bottom: 10px;
                border-radius: 8px;
                color: white;
                font-weight: 500;
                box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
                animation: slideInRight 0.3s ease-out;
                pointer-events: auto;
                position: relative;
                overflow: hidden;
                backdrop-filter: blur(10px);
                word-wrap: break-word;
            }

            .notification-item::before {
                content: '';
                position: absolute;
                bottom: 0;
                left: 0;
                height: 3px;
                background-color: rgba(255, 255, 255, 0.3);
                animation: progressBar var(--duration, 5000ms) linear;
            }

            @keyframes progressBar {
                from { width: 100%; }
                to { width: 0%; }
            }

            @keyframes slideInRight {
                from {
                    opacity: 0;
                    transform: translateX(100%);
                }
                to {
                    opacity: 1;
                    transform: translateX(0);
                }
            }

            @keyframes slideOutRight {
                from {
                    opacity: 1;
                    transform: translateX(0);
                }
                to {
                    opacity: 0;
                    transform: translateX(100%);
                }
            }

            .notification-item.removing {
                animation: slideOutRight 0.3s ease-in forwards;
            }

            .notification-success {
                background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
            }

            .notification-error {
                background: linear-gradient(135deg, #dc3545 0%, #fd7e14 100%);
            }

            .notification-warning {
                background: linear-gradient(135deg, #ffc107 0%, #fd7e14 100%);
                color: #212529;
            }

            .notification-info {
                background: linear-gradient(135deg, #17a2b8 0%, #007bff 100%);
            }

            .notification-icon {
                flex-shrink: 0;
                width: 20px;
                height: 20px;
            }

            .notification-content {
                flex: 1;
                min-width: 0;
            }

            .notification-title {
                font-weight: 600;
                margin-bottom: 4px;
                font-size: 14px;
            }

            .notification-message {
                font-size: 13px;
                opacity: 0.9;
                line-height: 1.4;
            }

            .notification-close {
                background: none;
                border: none;
                color: inherit;
                cursor: pointer;
                padding: 4px;
                border-radius: 4px;
                opacity: 0.7;
                transition: opacity 0.2s ease;
                flex-shrink: 0;
            }

            .notification-close:hover {
                opacity: 1;
                background-color: rgba(255, 255, 255, 0.1);
            }

            .notification-close svg {
                width: 16px;
                height: 16px;
            }

            @media (max-width: 480px) {
                .notification-container {
                    top: 10px;
                    right: 10px;
                    left: 10px;
                    max-width: none;
                }

                .notification-item {
                    padding: 12px 16px;
                    font-size: 14px;
                }
            }
        `;
    document.head.appendChild(styles);
  }

  /**
   * Mostra uma notificação de sucesso
   * @param {string} message - Mensagem a ser exibida
   * @param {Object} options - Opções adicionais
   */
  success(message, options = {}) {
    return this.show(message, { ...options, type: "success" });
  }

  /**
   * Mostra uma notificação de erro
   * @param {string} message - Mensagem a ser exibida
   * @param {Object} options - Opções adicionais
   */
  error(message, options = {}) {
    return this.show(message, { ...options, type: "error", duration: 7000 });
  }

  /**
   * Mostra uma notificação de aviso
   * @param {string} message - Mensagem a ser exibida
   * @param {Object} options - Opções adicionais
   */
  warning(message, options = {}) {
    return this.show(message, { ...options, type: "warning" });
  }

  /**
   * Mostra uma notificação informativa
   * @param {string} message - Mensagem a ser exibida
   * @param {Object} options - Opções adicionais
   */
  info(message, options = {}) {
    return this.show(message, { ...options, type: "info" });
  }

  /**
   * Mostra uma notificação de carregamento
   * @param {string} message - Mensagem a ser exibida
   * @param {Object} options - Opções adicionais
   */
  loading(message, options = {}) {
    return this.show(message, {
      ...options,
      type: "info",
      persistent: true,
      icon: this.getLoadingIcon(),
    });
  }

  /**
   * Método principal para mostrar notificações
   * @param {string} message - Mensagem a ser exibida
   * @param {Object} options - Opções da notificação
   * @returns {string} - ID da notificação
   */
  show(message, options = {}) {
    const config = {
      type: "info",
      title: null,
      duration: this.defaultDuration,
      persistent: false,
      closable: true,
      icon: null,
      onClick: null,
      onClose: null,
      allowHtml: false,
      ...options,
    };

    // Validar mensagem
    if (!message || typeof message !== "string") {
      console.error(
        "[NotificationManager] Mensagem é obrigatória e deve ser uma string"
      );
      return null;
    }

    // Gerar ID único
    const id = this.generateId();

    // Limitar número de notificações
    this.enforceMaxNotifications();

    // Criar elemento da notificação
    const notificationElement = this.createElement(id, message, config);

    // Adicionar ao container
    this.container.appendChild(notificationElement);

    // Salvar referência
    this.notifications.set(id, {
      element: notificationElement,
      config,
      timestamp: Date.now(),
    });

    // Configurar auto-dismiss se não for persistente
    if (!config.persistent && config.duration > 0) {
      setTimeout(() => {
        this.hide(id);
      }, config.duration);
    }

    // Callback de onClick
    if (config.onClick) {
      notificationElement.addEventListener("click", (e) => {
        if (!e.target.closest(".notification-close")) {
          config.onClick(id);
        }
      });
    }

    console.log(`[NotificationManager] Notificação criada: ${id}`);
    return id;
  }

  /**
   * Esconde uma notificação específica
   * @param {string} id - ID da notificação
   */
  hide(id) {
    const notification = this.notifications.get(id);
    if (!notification) {
      return;
    }

    const { element, config } = notification;

    // Adicionar classe de remoção
    element.classList.add("removing");

    // Remover após animação
    setTimeout(() => {
      if (element.parentNode) {
        element.parentNode.removeChild(element);
      }
      this.notifications.delete(id);

      // Callback de onClose
      if (config.onClose) {
        config.onClose(id);
      }

      console.log(`[NotificationManager] Notificação removida: ${id}`);
    }, 300);
  }

  /**
   * Esconde todas as notificações
   */
  hideAll() {
    const ids = Array.from(this.notifications.keys());
    ids.forEach((id) => this.hide(id));
  }

  /**
   * Remove notificações excessivas
   */
  enforceMaxNotifications() {
    const notifications = Array.from(this.notifications.entries()).sort(
      (a, b) => a[1].timestamp - b[1].timestamp
    );

    while (notifications.length >= this.maxNotifications) {
      const [oldestId] = notifications.shift();
      this.hide(oldestId);
    }
  }

  /**
   * Cria o elemento DOM da notificação
   * @param {string} id - ID da notificação
   * @param {string} message - Mensagem
   * @param {Object} config - Configuração
   * @returns {HTMLElement} - Elemento da notificação
   */
  createElement(id, message, config) {
    const element = document.createElement("div");
    element.className = `notification-item notification-${config.type}`;
    element.setAttribute("data-notification-id", id);

    if (config.duration > 0 && !config.persistent) {
      element.style.setProperty("--duration", `${config.duration}ms`);
    }

    // Ícone
    const icon = config.icon || this.getDefaultIcon(config.type);

    // Título e mensagem
    const contentHtml = config.title
      ? `<div class="notification-title">${
          config.allowHtml ? config.title : this.escapeHtml(config.title)
        }</div>
               <div class="notification-message">${
                 config.allowHtml ? message : this.escapeHtml(message)
               }</div>`
      : `<div class="notification-message">${
          config.allowHtml ? message : this.escapeHtml(message)
        }</div>`;

    // Botão de fechar
    const closeButton = config.closable
      ? `<button class="notification-close" type="button" aria-label="Fechar notificação">
                 <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                   <line x1="18" y1="6" x2="6" y2="18"></line>
                   <line x1="6" y1="6" x2="18" y2="18"></line>
                 </svg>
               </button>`
      : "";

    element.innerHTML = `
            ${icon ? `<div class="notification-icon">${icon}</div>` : ""}
            <div class="notification-content">${contentHtml}</div>
            ${closeButton}
        `;

    // Event listener para fechar
    if (config.closable) {
      const closeBtn = element.querySelector(".notification-close");
      closeBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        this.hide(id);
      });
    }

    return element;
  }

  /**
   * Obtém o ícone padrão para cada tipo
   * @param {string} type - Tipo da notificação
   * @returns {string} - HTML do ícone
   */
  getDefaultIcon(type) {
    const icons = {
      success: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="20,6 9,17 4,12"></polyline>
                      </svg>`,
      error: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="10"></circle>
                      <line x1="15" y1="9" x2="9" y2="15"></line>
                      <line x1="9" y1="9" x2="15" y2="15"></line>
                    </svg>`,
      warning: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
                        <line x1="12" y1="9" x2="12" y2="13"></line>
                        <line x1="12" y1="17" x2="12.01" y2="17"></line>
                      </svg>`,
      info: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                     <circle cx="12" cy="12" r="10"></circle>
                     <line x1="12" y1="16" x2="12" y2="12"></line>
                     <line x1="12" y1="8" x2="12.01" y2="8"></line>
                   </svg>`,
    };
    return icons[type] || icons.info;
  }

  /**
   * Obtém ícone de carregamento animado
   * @returns {string} - HTML do ícone de loading
   */
  getLoadingIcon() {
    return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="loading-spinner">
                  <style>
                    .loading-spinner { animation: spin 1s linear infinite; }
                    @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
                  </style>
                  <circle cx="12" cy="12" r="10"></circle>
                  <path d="M22 12a10 10 0 0 0-10-10"></path>
                </svg>`;
  }

  /**
   * Gera um ID único para a notificação
   * @returns {string} - ID único
   */
  generateId() {
    return `notification-${Date.now()}-${Math.random()
      .toString(36)
      .substr(2, 9)}`;
  }

  /**
   * Escapa HTML para prevenir XSS
   * @param {string} text - Texto a ser escapado
   * @returns {string} - Texto escapado
   */
  escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
  }

  /**
   * Verifica se uma notificação existe
   * @param {string} id - ID da notificação
   * @returns {boolean} - Se a notificação existe
   */
  exists(id) {
    return this.notifications.has(id);
  }

  /**
   * Obtém informações de uma notificação
   * @param {string} id - ID da notificação
   * @returns {Object|null} - Dados da notificação
   */
  get(id) {
    return this.notifications.get(id) || null;
  }

  /**
   * Obtém todas as notificações ativas
   * @returns {Array} - Array com todas as notificações
   */
  getAll() {
    return Array.from(this.notifications.entries()).map(([id, data]) => ({
      id,
      ...data,
    }));
  }

  /**
   * Limpa todas as notificações
   */
  clear() {
    this.hideAll();
  }

  /**
   * Configura a duração padrão das notificações
   * @param {number} duration - Duração em milissegundos
   */
  setDefaultDuration(duration) {
    this.defaultDuration = Math.max(1000, duration);
  }

  /**
   * Configura o número máximo de notificações
   * @param {number} max - Número máximo
   */
  setMaxNotifications(max) {
    this.maxNotifications = Math.max(1, max);
  }
}

// Criar instância global do NotificationManager
const notificationManager = new NotificationManager();

// Alias para facilitar o uso
const notify = {
  success: (message, options) => notificationManager.success(message, options),
  error: (message, options) => notificationManager.error(message, options),
  warning: (message, options) => notificationManager.warning(message, options),
  info: (message, options) => notificationManager.info(message, options),
  loading: (message, options) => notificationManager.loading(message, options),
  hide: (id) => notificationManager.hide(id),
  hideAll: () => notificationManager.hideAll(),
  clear: () => notificationManager.clear(),
};

// Exportar para uso em outros módulos
if (typeof module !== "undefined" && module.exports) {
  module.exports = NotificationManager;
}

console.log("[NotificationManager] Inicializado com sucesso");
