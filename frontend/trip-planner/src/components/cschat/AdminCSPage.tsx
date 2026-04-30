import { useEffect, useState, useRef } from "react";
import { List, ListItem, ListItemText, Divider, TextField, Button } from "@mui/material";
import { useLocation } from "react-router-dom";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getCSRooms, CSRoomResponse, getCSMessages } from "./types/csChat";
import { getMe } from "../api/auth";
import { ChatMessage } from "../store/csStore";
import "./css/AdminCSPage.css";
import Header from "../layout/Header";

export default function AdminCSPage() {
  const [rooms, setRooms] = useState<CSRoomResponse[]>([]);
  const [selectedRoom, setSelectedRoom] = useState<CSRoomResponse | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputMsg, setInputMsg] = useState("");
  const [adminId, setAdminId] = useState<number>(0);

  const location = useLocation();
  const stompClient = useRef<Client | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const autoSelectedRoomIdRef = useRef<number | null>(null);
  const [isClosed, setIsClosed] = useState(false);

  const fetchRooms = async () => {
    try {
      const roomList = await getCSRooms();
      setRooms(roomList);
    } catch (error) {
      console.error("방 목록 로드 실패:", error);
    }
  };

  const handleSelectRoom = async (room: CSRoomResponse) => {
    setIsClosed(room.status === "CLOSED");
    if (stompClient.current) {
      stompClient.current.deactivate();
    }

    setSelectedRoom(room);
    setMessages([]);

    try {
      const history = await getCSMessages(room.id);
      setMessages(history);
    } catch (error) {
      console.error("채팅 기록을 불러오는데 실패했습니다.", error);
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(`${import.meta.env.VITE_API_URL}/ws-chat`),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/sub/chat/room/${room.id}`, (message) => {
          const receivedMsg = JSON.parse(message.body);

          if (receivedMsg.type === "CLOSE") {
            setMessages((prev) => [...prev, { ...receivedMsg, content: "문의가 종료되었습니다." }]);
            setIsClosed(true);
            return;
          }

          setMessages((prev) => [...prev, receivedMsg]);
        });
      },
    });

    stompClient.current = client;
    client.activate();
  };

  useEffect(() => {
    const init = async () => {
      try {
        const me = await getMe();
        setAdminId(me.id);
        fetchRooms();
      } catch (error) {
        console.error("데이터 로드 실패:", error);
        alert("관리자 로그인이 필요합니다.");
      }
    };

    init();

    const adminGlobalClient = new Client({
      webSocketFactory: () => new SockJS(`${import.meta.env.VITE_API_URL}/ws-chat`),
      reconnectDelay: 5000,
      onConnect: () => {
        adminGlobalClient.subscribe("/sub/chat/admin/new-room", () => {
          console.log("새로운 문의 방이 개설되었습니다!");
          fetchRooms();
        });
      },
    });

    adminGlobalClient.activate();

    return () => {
      adminGlobalClient.deactivate();

      if (stompClient.current) {
        stompClient.current.deactivate();
      }
    };
  }, []);

  useEffect(() => {
    if (rooms.length === 0) return;

    const params = new URLSearchParams(location.search);
    const roomId = Number(params.get("roomId"));

    if (!roomId) return;

    if (autoSelectedRoomIdRef.current === roomId) {
      return;
    }

    const targetRoom = rooms.find((room) => room.id === roomId);

    if (targetRoom) {
      autoSelectedRoomIdRef.current = roomId;
      handleSelectRoom(targetRoom);
    }
  }, [rooms, location.search]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const handleSendMessage = () => {
    if (inputMsg.trim() !== "" && stompClient.current?.connected && selectedRoom) {
      const chatMessage = {
        roomId: selectedRoom.id,
        senderId: adminId,
        content: inputMsg,
      };

      stompClient.current.publish({
        destination: "/pub/chat/message",
        body: JSON.stringify(chatMessage),
      });

      setInputMsg("");
    }
  };

  return (
    <>
      <Header />

      <div className="admin-cs-container">
        <div className="admin-cs-sidebar">
          <h2 className="admin-cs-sidebar-title">
            접수된 1:1 문의
          </h2>

          <List>
            {rooms.length === 0 ? (
              <p className="admin-cs-empty-text">대기 중인 문의가 없습니다.</p>
            ) : (
              rooms.map((room) => (
                <div key={room.id}>
                  <ListItem
                    button
                    selected={selectedRoom?.id === room.id}
                    onClick={() => handleSelectRoom(room)}
                  >
                    <ListItemText
                      primary={room.title}
                      secondary={`${room.userNickname} 님`}
                    />
                  </ListItem>
                  <Divider />
                </div>
              ))
            )}
          </List>
        </div>

        <div className="admin-cs-chat-area">
          {selectedRoom ? (
            <>
              <h2 className="admin-cs-chat-header">
                {selectedRoom.title} ({selectedRoom.userNickname})
              </h2>

              <div className="admin-cs-message-list">
                {messages.map((msg, idx) => {
                  const isAdminMessage = msg.senderId === adminId;

                  return (
                    <div
                      key={idx}
                      className={`admin-cs-message-wrapper ${isAdminMessage ? "admin" : "user"}`}
                    >
                      <span className="admin-cs-message-sender">
                        {isAdminMessage ? "나(관리자)" : msg.senderNickname}
                      </span>

                      <div className={`admin-cs-message-bubble ${isAdminMessage ? "admin" : "user"}`}>
                        {msg.content}
                      </div>
                    </div>
                  );
                })}

                <div ref={messagesEndRef} />
              </div>

              <div className="admin-cs-input-area">
                <TextField
                  disabled={isClosed} 
                  fullWidth
                  variant="outlined"
                  size="small"
                  placeholder={isClosed ? "종료된 문의입니다." : "고객에게 보낼 답변을 입력하세요..."}
                  value={inputMsg}
                  onChange={(e) => setInputMsg(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && handleSendMessage()}
                />

                <Button variant="contained" disableElevation onClick={handleSendMessage} disabled={isClosed} >
                  전송
                </Button>
              </div>
            </>
          ) : (
            <div className="admin-cs-empty-chat">
              <p>왼쪽에서 문의를 선택해주세요.</p>
            </div>
          )}
        </div>
      </div>
    </>
  );
}