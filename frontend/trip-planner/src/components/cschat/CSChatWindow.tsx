import { useEffect, useState, useRef } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { useCSStore } from "../store/csStore";
import toast from "react-hot-toast";

interface CSChatWindowProps {
    roomId: number;
    senderId: number;
    nickname: string;
}

export default function CSChatWindow({
    roomId,
    senderId,
    nickname,
}: CSChatWindowProps) {
    const { messages, addMessage, clearCsInfo } = useCSStore();
    const [inputMsg, setInputMsg] = useState("");
    const [connected, setConnected] = useState(false);

    const stompClient = useRef<Client | null>(null);
    const messagesEndRef = useRef<HTMLDivElement>(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    const sendMessage = () => {
        if (!inputMsg.trim()) return;

        if (!stompClient.current?.connected) {
            toast.error("상담 서버와 연결되지 않았습니다.");
            return;
        }

        const chatMessage = {
            roomId,
            senderId,
            content: inputMsg,
        };

        stompClient.current.publish({
            destination: "/pub/chat/message",
            body: JSON.stringify(chatMessage),
        });

        setInputMsg("");
    };

    const handleEndChat = () => {
        if (stompClient.current) {
            stompClient.current.deactivate();
        }

        clearCsInfo();
        toast.success("상담이 종료되었습니다.");
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new SockJS("http://localhost:8080/ws-chat"),
            debug: (str) => console.log(str),
            reconnectDelay: 5000,
            onConnect: () => {
                setConnected(true);

                client.subscribe(`/sub/chat/room/${roomId}`, (message) => {
                    const receivedMsg = JSON.parse(message.body);
                    addMessage(receivedMsg);
                });
            },
            onDisconnect: () => {
                setConnected(false);
            },
            onStompError: (frame) => {
                console.error("Broker reported error: " + frame.headers["message"]);
                console.error("Additional details: " + frame.body);
                setConnected(false);
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
        <div className="cs-chat-window">
            <div className="cs-chat-info">
                <div>
                    <p className="cs-chat-title">1:1 상담</p>
                    <p className="cs-chat-room">문의 번호 #{roomId}</p>
                </div>

                <span
                    className={
                        connected
                            ? "cs-status connected"
                            : "cs-status disconnected"
                    }
                >
                    {connected ? "연결됨" : "연결 끊김"}
                </span>
            </div>

            <div className="cs-chat-notice">
                상담원이 확인 후 순차적으로 답변드립니다.
            </div>

            <div className="cs-chat-body">
                {messages.length === 0 ? (
                    <div className="cs-empty-message">
                        상담원 연결이 요청되었습니다.
                        <br />
                        문의 내용을 남겨주세요.
                    </div>
                ) : (
                    messages.map((msg, index) => {
                        const isMine = msg.senderNickname === nickname;

                        return (
                            <div
                                key={index}
                                className={
                                    isMine
                                        ? "cs-message mine"
                                        : "cs-message other"
                                }
                            >
                                <div className="cs-message-bubble">
                                    {!isMine && (
                                        <span className="cs-message-name">
                                            {msg.senderNickname}
                                        </span>
                                    )}
                                    <p>{msg.content}</p>
                                </div>
                            </div>
                        );
                    })
                )}

                <div ref={messagesEndRef} />
            </div>

            <div className="cs-chat-actions">
                <button
                    type="button"
                    className="cs-end-button"
                    onClick={handleEndChat}
                >
                    상담 종료
                </button>
            </div>

            <div className="cs-chat-input-area">
                <input
                    value={inputMsg}
                    onChange={(e) => setInputMsg(e.target.value)}
                    onKeyDown={(e) => {
                        if (e.key === "Enter") sendMessage();
                    }}
                    placeholder="메시지를 입력하세요..."
                    disabled={!connected}
                />

                <button
                    type="button"
                    onClick={sendMessage}
                    disabled={!connected || !inputMsg.trim()}
                >
                    전송
                </button>
            </div>
        </div>
    );
}