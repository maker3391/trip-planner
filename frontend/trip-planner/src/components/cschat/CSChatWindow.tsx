import { useEffect, useState, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useCSStore } from '../store/csStore';
import { closeCSRoom } from './types/csChat'; 
import './css/CSChatWindow.css';

interface CSChatWindowProps {
    roomId: number;
    senderId: number;
    onBack: () => void;
}

export default function CSChatWindow({ roomId, senderId, onBack}: CSChatWindowProps) {
    const { messages, addMessage, clearCsInfo } = useCSStore();
    const [inputMsg, setInputMsg] = useState('');
    const [connected, setConnected] = useState(false);
    
    const stompClient = useRef<Client | null>(null);
    const messagesEndRef = useRef<HTMLDivElement>(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

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
        if (window.confirm("상담을 종료하시겠습니까? (종료 후에도 목록에서 내역을 확인할 수 있습니다)")) {
            try {
                await closeCSRoom(roomId);
                
                if (stompClient.current) {
                    stompClient.current.deactivate();
                }
                clearCsInfo(); 
                onBack(); 
            } catch (error) {
                console.error("상담 종료 실패:", error);
                alert("상담 종료 처리에 실패했습니다.");
            }
        }
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws-chat'),
            debug: (str) => console.log(str),
            reconnectDelay: 5000,
            onConnect: () => {
                setConnected(true);
                client.subscribe(`/sub/chat/room/${roomId}`, (message) => {
                    const receivedMsg = JSON.parse(message.body);
                    addMessage(receivedMsg);
                });
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
            },
        });

        stompClient.current = client;
        client.activate();

        return () => {
            if (stompClient.current) {
                stompClient.current.deactivate();
            }
        };
    }, [roomId, addMessage]);

    return (
        <div className="cs-chat-container">
            <div className="cs-chat-header">
                <div className="cs-chat-header-left">
                    <button 
                        className="cs-chat-back-btn"
                        onClick={onBack} 
                        title="AI 챗봇 혹은 목록으로 돌아가기"
                    >
                        ⬅️
                    </button>
                    <div>
                        <h3 className="cs-chat-title">1:1 문의 채팅방 (방 번호: {roomId})</h3>
                        <span className={`cs-chat-status ${connected ? 'connected' : 'disconnected'}`}>
                            {connected ? '🟢 연결됨' : '🔴 연결 끊어짐'}
                        </span>
                    </div>
                </div>
                <button className="cs-chat-end-btn" onClick={handleEndChat}>
                    상담 종료
                </button>
            </div>

            <div className="cs-chat-messages">
                {messages.map((msg, idx) => {
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
                    className="cs-chat-input"
                    type="text"
                    placeholder="메시지를 입력하세요..." 
                    value={inputMsg}
                    onChange={(e) => setInputMsg(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
                />
                <button className="cs-chat-send-btn" onClick={sendMessage}>
                    전송
                </button>
            </div>
        </div>
    );
}