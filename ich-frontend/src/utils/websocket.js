// src/utils/websocket.js
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
    constructor() {
        this.client = null;
        this.connected = false;
        this.subscriptions = new Map();
    }

    /**
     * 连接 WebSocket
     * @param {Function} onAchievement 成就回调
     * @param {Function} onNotification 通知回调
     */
    connect(userId, onAchievement, onNotification) {
        if (!userId) {
            console.warn('WebSocket: 用户未登录，跳过连接');
            return;
        }

        if (this.client && this.connected) {
            console.log('WebSocket: 已连接');
            return;
        }

        this.client = new Client({
            webSocketFactory: () => new SockJS('/ws'),
            debug: (str) => {
                // console.log('WebSocket:', str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect: () => {
                console.log('WebSocket: 连接成功');
                this.connected = true;
                
                // 订阅成就通知
                const achievementSub = this.client.subscribe(`/topic/achievement/${userId}`, (message) => {
                    try {
                        const data = JSON.parse(message.body);
                        console.log('收到成就通知:', data);
                        if (onAchievement) {
                            onAchievement(data);
                        }
                    } catch (e) {
                        console.error('解析成就通知失败:', e);
                    }
                });
                this.subscriptions.set('achievement', achievementSub);
                
                // 订阅通用通知
                const notificationSub = this.client.subscribe(`/topic/notification/${userId}`, (message) => {
                    try {
                        const data = JSON.parse(message.body);
                        console.log('收到通知:', data);
                        if (onNotification) {
                            onNotification(data);
                        }
                    } catch (e) {
                        console.error('解析通知失败:', e);
                    }
                });
                this.subscriptions.set('notification', notificationSub);
            },
            onStompError: (frame) => {
                console.error('WebSocket STOMP 错误:', frame);
            },
            onWebSocketError: (error) => {
                console.error('WebSocket 错误:', error);
            },
            onDisconnect: () => {
                console.log('WebSocket: 断开连接');
                this.connected = false;
            }
        });
        
        this.client.activate();
    }

    /**
     * 断开连接
     */
    disconnect() {
        if (this.client && this.connected) {
            this.subscriptions.forEach((sub, key) => {
                if (sub) sub.unsubscribe();
            });
            this.subscriptions.clear();
            this.client.deactivate();
            this.connected = false;
            console.log('WebSocket: 已断开');
        }
    }

    /**
     * 检查连接状态
     */
    isConnected() {
        return this.connected;
    }
}

export default new WebSocketService();