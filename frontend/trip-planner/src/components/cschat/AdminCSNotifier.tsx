import { useEffect } from "react";
import { Client, StompSubscription } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import toast from "react-hot-toast";
import { getMe } from "../api/auth";

let adminCSClient: Client | null = null;
let adminCSSubscription: StompSubscription | null = null;
let isConnecting = false;
let isAdminCSConnected = false;

const receivedRoomIds = new Set<number>();

export default function AdminCSNotifier() {
  useEffect(() => {
    const connect = async () => {
      if (isConnecting || isAdminCSConnected || adminCSClient) {
        return;
      }

      isConnecting = true;

      try {
        const me = await getMe();

        if (me.role !== "ADMIN" && me.role !== "ROLE_ADMIN") {
          isConnecting = false;
          return;
        }

        const client = new Client({
          webSocketFactory: () => new SockJS("http://localhost:8080/ws-chat"),
          reconnectDelay: 5000,

          onConnect: () => {
            isConnecting = false;
            isAdminCSConnected = true;

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
                  targetUrl: "/admin/cs",
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
            isAdminCSConnected = false;
            adminCSClient = null;
            adminCSSubscription = null;
          },

          onStompError: (frame) => {
            console.error("STOMP 에러:", frame);
          },

          onWebSocketError: (error) => {
            console.error("WebSocket 에러:", error);
          },
        });

        adminCSClient = client;
        client.activate();
      } catch (error) {
        console.error("관리자 문의 알림 연결 실패:", error);
        isConnecting = false;
        isAdminCSConnected = false;
        adminCSClient = null;
        adminCSSubscription = null;
      }
    };

    connect();

    return () => {
      adminCSSubscription?.unsubscribe();
      adminCSSubscription = null;

      adminCSClient?.deactivate();
      adminCSClient = null;

      isConnecting = false;
      isAdminCSConnected = false;
    };
  }, []);

  return null;
}