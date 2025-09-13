/**
 * Notification Manager - Sistema de notificações toast
 * Funcionalidades: Success, error, warning, info notifications
 */

class NotificationManager {
    constructor() {
        this.container = null;
        this.notifications = [];
        this.init();
    }

    init() {
        // Criar container de notificações
        this.container = document.createElement('div');
        this.container.id = 'notification-container';
        this.container.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 10000;
            pointer-events: none;
        `;
        document.body.appendChild(this.container);
    }

    /**
     * Mostrar notificação genérica
     */
    show(message, type = 'info', duration = 4000) {
        const notification = this.createNotification(message, type);
        this.container.appendChild(notification);
        this.notifications.push(notification);

        // Animar entrada
        requestAnimationFrame(() => {
            notification.classList.add('show');
        });

        // Auto-remover após duration
        if (duration > 0) {
            setTimeout(() => {
                this.remove(notification);
            }, duration);
        }

        return notification;
    }

    /**
     * Criar elemento de notificação
     */
    createNotification(message, type) {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        
        // Configurar ícone baseado no tipo
        const iconMap = {
            success: 'check-circle',
            error: 'x-circle',
            warning: 'alert-triangle',
            info: 'info'
        };

        notification.innerHTML = `
            <div class="notification-content">
                <i data-feather="${iconMap[type] || 'info'}"></i>
                <span class="notification-message">${message}</span>
                <button class="notification-close" onclick="notificationManager.remove(this.closest('.notification'))">
                    <i data-feather="x"></i>
                </button>
            </div>
        `;

        // Aplicar estilos
        notification.style.cssText = `
            background: white;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            margin-bottom: 10px;
            padding: 16px;
            pointer-events: all;
            transform: translateX(100%);
            transition: all 0.3s ease;
            opacity: 0;
            max-width: 400px;
            border-left: 4px solid ${this.getTypeColor(type)};
        `;

        // Estilos do conteúdo
        const content = notification.querySelector('.notification-content');
        content.style.cssText = `
            display: flex;
            align-items: center;
            gap: 12px;
        `;

        // Estilo do ícone
        const icon = content.querySelector('i[data-feather]');
        icon.style.cssText = `
            color: ${this.getTypeColor(type)};
            width: 20px;
            height: 20px;
        `;

        // Estilo da mensagem
        const messageEl = content.querySelector('.notification-message');
        messageEl.style.cssText = `
            flex: 1;
            font-size: 14px;
            color: #333;
            line-height: 1.4;
        `;

        // Estilo do botão fechar
        const closeBtn = content.querySelector('.notification-close');
        closeBtn.style.cssText = `
            background: none;
            border: none;
            cursor: pointer;
            padding: 2px;
            border-radius: 4px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #666;
            transition: background-color 0.2s ease;
        `;

        closeBtn.onmouseover = () => closeBtn.style.backgroundColor = '#f5f5f5';
        closeBtn.onmouseout = () => closeBtn.style.backgroundColor = 'transparent';

        // Inicializar ícones Feather
        if (window.feather) {
            feather.replace();
        }

        return notification;
    }

    /**
     * Remover notificação
     */
    remove(notification) {
        if (!notification || !notification.parentNode) return;

        notification.style.transform = 'translateX(100%)';
        notification.style.opacity = '0';

        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
            
            const index = this.notifications.indexOf(notification);
            if (index > -1) {
                this.notifications.splice(index, 1);
            }
        }, 300);
    }

    /**
     * Obter cor do tipo
     */
    getTypeColor(type) {
        const colors = {
            success: '#10b981',
            error: '#ef4444',
            warning: '#f59e0b',
            info: '#3b82f6'
        };
        return colors[type] || colors.info;
    }

    /**
     * Métodos de conveniência
     */
    success(message, duration = 4000) {
        return this.show(message, 'success', duration);
    }

    error(message, duration = 6000) {
        return this.show(message, 'error', duration);
    }

    warning(message, duration = 5000) {
        return this.show(message, 'warning', duration);
    }

    info(message, duration = 4000) {
        return this.show(message, 'info', duration);
    }

    /**
     * Limpar todas as notificações
     */
    clear() {
        this.notifications.forEach(notification => {
            this.remove(notification);
        });
    }
}

// CSS para animações (inserido dinamicamente)
const notificationStyles = `
    .notification.show {
        transform: translateX(0) !important;
        opacity: 1 !important;
    }
    
    .notification-close i {
        width: 16px !important;
        height: 16px !important;
    }
`;

// Inserir estilos
const styleSheet = document.createElement('style');
styleSheet.textContent = notificationStyles;
document.head.appendChild(styleSheet);

// Exportar instância singleton
const notificationManager = new NotificationManager();

export default notificationManager;