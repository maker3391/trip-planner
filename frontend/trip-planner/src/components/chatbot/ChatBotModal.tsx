import { useEffect, useState } from "react";
import "./ChatBot.css";
import ChatHeader from "./components/ChatHeader";
import ChatInput from "./components/ChatInput";
import ChatMessageList from "./components/ChatMessageList";
import useChatScroll from "./hooks/useChatScroll";
import useChatMessages from "./hooks/useChatMessages";
import CSChatWindow from "../cschat/CSChatWindow";
import CSChatList from "../cschat/CSChatList";
import { getMe } from "../api/auth";
import { createCSRoom, CSRoomResponse } from "../api/csChat";
import { useCSStore } from "../store/csStore";

interface ChatBotModalProps {
    open: boolean;
    onClose: () => void;
}

export default function ChatBotModal({ open, onClose }: ChatBotModalProps) {
    const [chatMode, setChatMode] = useState<"AI" | "LIST" | "CS">("AI");
    const { csInfo, setCsInfo } = useCSStore();
    const [isSwitching, setIsSwitching] = useState(false);

    const {
        messages,
        input,
        isLoading,
        typingMessageId,
        animatedMessageIds,
        setInput,
        sendMessage,
        handleTypingEnd,
    } = useChatMessages();

    const {
        chatBodyRef,
        showScrollToBottomButton,
        maybeAutoScrollToBottom,
        enableAutoScrollAndJumpToBottom,
        resetAutoScroll,
        handleBodyScroll,
        handleWheelCapture,
        handleTouchStart,
        handleTouchMove,
    } = useChatScroll(open);

    useEffect(() => {
        if (!open) return;
        maybeAutoScrollToBottom();
    }, [messages, isLoading, open, maybeAutoScrollToBottom]);

    const handleSend = async (): Promise<void> => {
        resetAutoScroll();
        await sendMessage(input);
    };

    const handleToggleMode = () => {
        if (chatMode === "AI") {
            setChatMode("LIST");
        } else {
            setChatMode("AI");
        }
    };

    const handleCreateNewRoom = async (title: string) => {
        setIsSwitching(true);

        try {
            const me = await getMe();

            const newRoom = await createCSRoom({
                title,
                content: "상담원 연결을 요청했습니다.",
            });

            setCsInfo({
                roomId: newRoom.id,
                senderId: me.id,
            });

            setChatMode("CS");
        } catch (error) {
            console.error(error);
            alert("상담원 연결을 위해 먼저 로그인을 해주세요.");
        } finally {
            setIsSwitching(false);
        }
    };

    const handleEnterExistingRoom = async (room: CSRoomResponse) => {
        try {
            const me = await getMe();

            setCsInfo({
                roomId: room.id,
                senderId: me.id,
            });

            setChatMode("CS");
        } catch (error) {
            alert("로그인이 필요합니다.");
        }
    };

    if (!open) return null;

    return (
        <div className="chatbot-overlay">
            <div className="chatbot-modal">
                <ChatHeader
                    onClose={onClose}
                    chatMode={chatMode === "AI" ? "AI" : "CS"}
                    onToggleMode={handleToggleMode}
                />

                {isSwitching ? (
                    <div className="chatbot-switching">
                        <p>상담원과 연결 중입니다...</p>
                    </div>
                ) : chatMode === "AI" ? (
                    <>
                        <ChatMessageList
                            messages={messages}
                            isLoading={isLoading}
                            typingMessageId={typingMessageId}
                            animatedMessageIds={animatedMessageIds}
                            chatBodyRef={chatBodyRef}
                            onTypingProgress={maybeAutoScrollToBottom}
                            onTypingEnd={handleTypingEnd}
                            onScroll={handleBodyScroll}
                            onWheelCapture={handleWheelCapture}
                            onTouchStart={handleTouchStart}
                            onTouchMove={handleTouchMove}
                            showScrollToBottomButton={showScrollToBottomButton}
                            onScrollToBottom={enableAutoScrollAndJumpToBottom}
                        />

                        <ChatInput
                            value={input}
                            isLoading={isLoading}
                            onChange={setInput}
                            onSend={handleSend}
                        />
                    </>
                ) : chatMode === "LIST" ? (
                    <CSChatList
                        onBack={() => setChatMode("AI")}
                        onCreateNew={handleCreateNewRoom}
                        onEnterRoom={handleEnterExistingRoom}
                    />
                ) : (
                    csInfo && (
                        <CSChatWindow
                            roomId={csInfo.roomId}
                            senderId={csInfo.senderId}
                            onBack={() => setChatMode("LIST")}
                        />
                    )
                )}
            </div>
        </div>
    );
}