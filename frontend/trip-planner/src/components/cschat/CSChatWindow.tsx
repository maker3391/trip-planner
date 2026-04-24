import { useEffect, useState, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Box, TextField, Button, Typography, Paper, List, ListItem, ListItemText } from '@mui/material';
import { useCSStore } from '../store/csStore';


interface CSChatWindowProps {
    roomId: number;
    senderId: number;
    nickname: string;
}

export default function CSChatWindow({ roomId, senderId, nickname }: CSChatWindowProps) {
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

    const handleEndChat = () => {
        if (window.confirm("상담을 완전히 종료하시겠습니까? (대화 내용이 사라집니다)")) {
            if (stompClient.current) {
                stompClient.current.deactivate();
            }
            clearCsInfo(); 
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
        <Paper elevation={3} sx={{ width: 400, height: 600, display: 'flex', flexDirection: 'column', p: 2 }}>
            <Box sx={{ borderBottom: 1, borderColor: 'divider', pb: 1, mb: 1 }}>
                <Typography variant="h6">1:1 문의 채팅방 (방 번호: {roomId})</Typography>
                <Typography variant="caption" color={connected ? 'success.main' : 'error.main'}>
                    {connected ? '🟢 연결됨' : '🔴 연결 끊어짐'}
                </Typography>
            </Box>

            <Button variant="outlined" color="error" size="small" onClick={handleEndChat}>
              상담 종료
            </Button>
            <List sx={{ flexGrow: 1, overflow: 'auto', mb: 2, bgcolor: '#f5f5f5', borderRadius: 1, p: 1 }}>
                {messages.map((msg, index) => (
                    <ListItem key={index} sx={{ justifyContent: msg.senderNickname === nickname ? 'flex-end' : 'flex-start' }}>
                        <Paper sx={{ p: 1.5, maxWidth: '80%', bgcolor: msg.senderNickname === nickname ? '#e3f2fd' : '#ffffff' }}>
                            <ListItemText 
                                primary={<Typography variant="subtitle2" color="primary">{msg.senderNickname}</Typography>}
                                secondary={msg.content} 
                            />
                        </Paper>
                    </ListItem>
                ))}
                <div ref={messagesEndRef} />
            </List>

            <Box sx={{ display: 'flex', gap: 1 }}>
                <TextField 
                    fullWidth 
                    size="small" 
                    placeholder="메시지를 입력하세요..." 
                    value={inputMsg}
                    onChange={(e) => setInputMsg(e.target.value)}
                    onKeyPress={(e) => { if (e.key === 'Enter') sendMessage(); }}
                    disabled={!connected}
                />
                <Button variant="contained" onClick={sendMessage} disabled={!connected}>
                    전송
                </Button>
            </Box>
        </Paper>
    );
}