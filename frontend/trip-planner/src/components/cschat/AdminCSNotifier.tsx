import { useEffect } from "react";
import { Client, StompSubscription } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import toast from "react-hot-toast";

let adminCSClient: Client | null = null;
let adminCSSubscription: StompSubscription | null = null;
let isConnecting = false;

const receivedRoomIds = new Set<number>();

export default function AdminCSNotifier() {
    useEffect(() => {
        const connect = () => {
            if (isConnecting || adminCSClient?.connected) {
                return;
            }

            isConnecting = true;

            const client = new Client({
                webSocketFactory: () => new SockJS("http://localhost:8080/ws-chat"),
                reconnectDelay: 5000,

                onConnect: () => {
                    isConnecting = false;
                    console.log("관리자 문의 알림 WebSocket 연결됨");

                    if (adminCSSubscription) {
                        return;
                    }

                    adminCSSubscription = client.subscribe(
                        "/sub/chat/admin/new-room",
                        (message) => {
                            const room = JSON.parse(message.body);
                            const roomId = Number(room.id);

                            if (receivedRoomIds.has(roomId)) {
                                return;
                            }

                            receivedRoomIds.add(roomId);

                            const notification = {
                                id: roomId,
                                message: `새로운 문의가 들어왔습니다. (${room.title})`,
                                targetUrl: `/admin/cs?roomId=${roomId}`,
                                createdAt: new Date().toISOString(),
                            };

                            toast.success("새로운 문의가 들어왔습니다.", {
                                id: `admin-cs-${roomId}`,
                            });

                            window.dispatchEvent(
                                new CustomEvent("admin-cs-notification", {
                                    detail: notification,
                                })
                            );
                        }
                    );
                },

                onDisconnect: () => {
                    isConnecting = false;
                    adminCSClient = null;
                    adminCSSubscription = null;
                },

                onStompError: (frame) => {
                    console.error("STOMP 에러:", frame);
                    isConnecting = false;
                },

                onWebSocketError: (error) => {
                    console.error("WebSocket 에러:", error);
                    isConnecting = false;
                },
            });

            adminCSClient = client;
            client.activate();
        };

        connect();

        return () => {
            adminCSSubscription?.unsubscribe();
            adminCSSubscription = null;

            adminCSClient?.deactivate();
            adminCSClient = null;

            isConnecting = false;
        };
    }, []);

    return null;
}