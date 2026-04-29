import { useEffect, useState, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useCSStore } from '../store/csStore';
import { closeCSRoom, getCSMessages } from './types/csChat'; 
import './css/CSChatWindow.css';

interface CSChatWindowProps {
    roomId: number;
    senderId: number;
    onBack: () => void;
    status: string;
}

export default function CSChatWindow({ roomId, senderId, onBack, status }: CSChatWindowProps) {
    const messages = useCSStore((state) => state.messages);
    const addMessage = useCSStore((state) => state.addMessage);
    const setMessages = useCSStore((state) => state.setMessages);
    const clearCsInfo = useCSStore((state) => state.clearCsInfo);

    const [inputMsg, setInputMsg] = useState('');
    const [connected, setConnected] = useState(false);
    const [isClosed, setIsClosed] = useState(status === "CLOSED");
    
    const stompClient = useRef<Client | null>(null);
    const messagesEndRef = useRef<HTMLDivElement>(null);

    const scrollToBottom = useCallback(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, []);

    useEffect(() => {
        if (roomId) {
            getCSMessages(roomId)
                .then((history) => {
                    setMessages(Array.isArray(history) ? history : []);
                })
                .catch((err) => {
                    console.error('과거 메시지 조회 실패:', err);
                    setMessages([]);
                });
        }
        
    }, [roomId, setMessages]); 

    useEffect(() => {
        scrollToBottom();
    }, [messages, scrollToBottom]);

    useEffect(() => {
        if (!roomId) return;

        const client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws-chat'),
            reconnectDelay: 5000,
            onConnect: () => {
                setConnected(true);
                client.subscribe(`/sub/chat/room/${roomId}`, (message) => {
                    const receivedMsg = JSON.parse(message.body);
                    if (receivedMsg.type === "CLOSE") {
                        addMessage({ ...receivedMsg, content: "상담이 종료되었습니다." });
                        setIsClosed(true);
                        return;
                    }
                    addMessage(receivedMsg);
                });
            },
            onDisconnect: () => {
                setConnected(false);
            }
        });

        stompClient.current = client;
        client.activate();

        return () => {
            if (stompClient.current) {
                stompClient.current.deactivate();
            }
        };
    }, [roomId, addMessage]);

    const sendMessage = () => {
        if (inputMsg.trim() !== '' && stompClient.current?.connected) {
            const chatMessage = {
                roomId: roomId,
                senderId: senderId,
                content: inputMsg,
            };
            
            stompClient.current.publish({
                destination: '/pub/chat/message',
                body: JSON.stringify(chatMessage),
            });
            setInputMsg('');
        }
    };

    const handleEndChat = async () => {
        if (isClosed) return;  // 이미 종료된 경우 차단
        if (window.confirm("상담을 종료하시겠습니까?")) {
            try {
                await closeCSRoom(roomId);
                setIsClosed(true);  // ← CLOSE 브로드캐스트 기다리지 않고 바로 차단
                // onBack() 제거 - 그냥 입력만 막고 화면 유지
            } catch (error) {
                alert("상담 종료 처리에 실패했습니다.");
            }
        }
    };

    return (
        <div className="cs-chat-container">
            <div className="cs-chat-header">
                <div className="cs-chat-header-left">
                    <button 
                        className="cs-chat-back-btn"
                        onClick={() => {
                            clearCsInfo();
                            onBack();
                        }} 
                    >
                        ⬅️
                    </button>
                    <div>
                        <h3 className="cs-chat-title">1:1 문의 (방 번호: {roomId})</h3>
                        <span className={`cs-chat-status ${connected ? 'connected' : 'disconnected'}`}>
                            {connected ? '🟢 연결됨' : '🔴 연결 끊어짐'}
                        </span>
                    </div>
                </div>
                <button className="cs-chat-end-btn" onClick={handleEndChat}  disabled={isClosed}>
                    상담 종료
                </button>
            </div>

            <div className="cs-chat-messages">
                {(messages || []).map((msg, idx) => {
                    const isMe = msg.senderId === senderId;
                    return (
                        <div key={idx} className={`cs-chat-msg-wrapper ${isMe ? 'me' : 'other'}`}>
                            <span className="cs-chat-sender">
                                {isMe ? '나' : (msg.senderNickname || '관리자')}
                            </span>
                            <div className={`cs-chat-bubble ${isMe ? 'me' : 'other'}`}>
                                {msg.content}
                            </div>
                        </div>
                    );
                })}
                <div ref={messagesEndRef} />
            </div>

            <div className="cs-chat-input-area">
                <input
                    disabled={isClosed}
                    className="cs-chat-input"
                    type="text"
                    placeholder={isClosed ? "종료된 상담입니다." : "메시지를 입력하세요..."}
                    value={inputMsg}
                    onChange={(e) => setInputMsg(e.target.value)}
                    onKeyPress={(e) => e.key === "Enter" && !isClosed && sendMessage()}
                />
                <button className="cs-chat-send-btn"  disabled={isClosed} onClick={sendMessage}>
                    전송
                </button>
            </div>
        </div>
    );
}