import { useEffect, useState, useRef } from "react";
import { List, ListItem, ListItemText, Divider, TextField, Button } from "@mui/material";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getCSRooms, CSRoomResponse, getCSMessages } from "./types/csChat";
import { getMe } from "../api/auth";
import { ChatMessage } from "../store/csStore";
import './css/AdminCSPage.css';
import Header from "../layout/Header";

export default function AdminCSPage() {
  const [rooms, setRooms] = useState<CSRoomResponse[]>([]);
  const [selectedRoom, setSelectedRoom] = useState<CSRoomResponse | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputMsg, setInputMsg] = useState('');
  const [adminId, setAdminId] = useState<number>(0);

  const stompClient = useRef<Client | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const fetchRooms = async () => {
      try {
        const roomList = await getCSRooms();
        setRooms(roomList);
      } catch (error) {
        console.error("방 목록 로드 실패:", error);
      }
    };

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
      webSocketFactory: () => new SockJS('http://localhost:8080/ws-chat'),
      reconnectDelay: 5000,
      onConnect: () => {
        adminGlobalClient.subscribe('/sub/chat/admin/new-room', () => {
          console.log("새로운 문의 방이 개설되었습니다!");
          fetchRooms(); 
        });
      },
    });

    adminGlobalClient.activate();

    return () => {
      adminGlobalClient.deactivate();
    };
  }, []);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({behavior: 'smooth'});
  }, [messages]);

  const handleSelectRoom = async (room: CSRoomResponse) => {
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
        webSocketFactory: () => new SockJS('http://localhost:8080/ws-chat'),
        reconnectDelay: 5000,
        onConnect: () => {
            client.subscribe(`/sub/chat/room/${room.id}`, (message) => {
                const receivedMsg = JSON.parse(message.body);
                setMessages((prev) => [...prev, receivedMsg]);
            });
        },
    });

    stompClient.current = client;
    client.activate();
  };

  const handleSendMessage = () => {
    if(inputMsg.trim() !== '' && stompClient.current?.connected && selectedRoom) {
      const chatMessage = {
        roomId: selectedRoom.id,
        senderId: adminId,
        content: inputMsg,
      };
      stompClient.current.publish({
        destination: '/pub/chat/message',
        body: JSON.stringify(chatMessage),
      });
      setInputMsg('');
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
                    const isAdmin = msg.senderId === adminId;
                      return (
                        <div 
                          key={idx} 
                          className={`admin-cs-message-wrapper ${isAdmin ? 'admin' : 'user'}`}
                        >
                          <span className="admin-cs-message-sender">
                            {isAdmin ? '나(관리자)' : msg.senderNickname}
                          </span>
                          <div className={`admin-cs-message-bubble ${isAdmin ? 'admin' : 'user'}`}>
                            {msg.content}
                          </div>
                        </div>
                      );
                    })}
                    <div ref={messagesEndRef} />
                </div>

                <div className="admin-cs-input-area">
                    <TextField 
                      fullWidth 
                      variant="outlined" 
                      size="small" 
                      placeholder="고객에게 보낼 답변을 입력하세요..." 
                      value={inputMsg}
                      onChange={(e) => setInputMsg(e.target.value)}
                      onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                    />
                    <Button variant="contained" disableElevation onClick={handleSendMessage}>
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