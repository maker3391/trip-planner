import { useEffect } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import toast from "react-hot-toast";
import { getMe } from "../api/auth";

let adminCSClient: Client | null = null;
let isAdminCSConnected = false;
const receivedRoomIds = new Set<number>();

export default function AdminCSNotifier() {
  useEffect(() => {
    const connect = async () => {
      if (isAdminCSConnected || adminCSClient) {
        return;
      }

      try {
        const me = await getMe();

        if (me.role !== "ADMIN" && me.role !== "ROLE_ADMIN") {
          return;
        }

        const client = new Client({
          webSocketFactory: () => new SockJS("http://localhost:8080/ws-chat"),
          reconnectDelay: 5000,

          onConnect: () => {
            isAdminCSConnected = true;
            console.log("관리자 문의 알림 WebSocket 연결됨");

            client.subscribe("/sub/chat/admin/new-room", (message) => {
              const room = JSON.parse(message.body);

              if (receivedRoomIds.has(room.id)) {
                return;
              }

              receivedRoomIds.add(room.id);

              const notification = {
                id: Date.now(),
                message: `새로운 문의가 들어왔습니다. (${room.title})`,
                targetUrl: "/admin/cs",
                createdAt: new Date().toISOString(),
              };

              toast.success("새로운 문의가 들어왔습니다.", {
                id: `admin-cs-${room.id}`,
              });

              window.dispatchEvent(
                new CustomEvent("admin-cs-notification", {
                  detail: notification,
                })
              );
            });
          },

          onDisconnect: () => {
            isAdminCSConnected = false;
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
        adminCSClient = null;
        isAdminCSConnected = false;
      }
    };

    connect();

    return () => {
      // 여기서 deactivate 하지 않음.
      // 페이지 이동/StrictMode 때문에 끊었다 붙으면서 중복 연결되는 걸 막기 위함.
    };
  }, []);

  return null;
}