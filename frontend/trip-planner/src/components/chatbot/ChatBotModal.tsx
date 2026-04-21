import { useEffect } from "react";
import "./ChatBot.css";
import ChatHeader from "./components/ChatHeader";
import ChatInput from "./components/ChatInput";
import ChatMessageList from "./components/ChatMessageList";
import useChatScroll from "./hooks/useChatScroll";
import useChatMessages from "./hooks/useChatMessages";

interface ChatBotModalProps {
  open: boolean;
  onClose: () => void;
}

export default function ChatBotModal({
  open,
  onClose,
}: ChatBotModalProps) {
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

  if (!open) return null;

  return (
    <div className="chatbot-overlay">
      <div className="chatbot-modal">
        <ChatHeader onClose={onClose} />

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
      </div>
    </div>
  );
}