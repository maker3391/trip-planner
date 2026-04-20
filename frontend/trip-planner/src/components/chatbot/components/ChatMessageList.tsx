import type {
  UIEvent,
  TouchEvent as ReactTouchEvent,
  WheelEvent as ReactWheelEvent,
} from "react";
import type { ChatMessage } from "../types/chatUi";
import TypingText from "./TypingText";
import LoadingBubble from "./LoadingBubble";
import ScrollToBottomButton from "./ScrollToBottomButton";

interface ChatMessageListProps {
  messages: ChatMessage[];
  isLoading: boolean;
  typingMessageId: number | null;
  animatedMessageIds: number[];
  chatBodyRef: React.RefObject<HTMLDivElement | null>;
  onTypingProgress: () => void;
  onTypingEnd: (messageId: number) => void;
  onScroll: (_event: UIEvent<HTMLDivElement>) => void;
  onWheelCapture: (event: ReactWheelEvent<HTMLDivElement>) => void;
  onTouchStart: (event: ReactTouchEvent<HTMLDivElement>) => void;
  onTouchMove: (event: ReactTouchEvent<HTMLDivElement>) => void;
  showScrollToBottomButton: boolean;
  onScrollToBottom: () => void;
}

const renderMessageLines = (content: string) => {
  return content.split("\n").map((line, index) => (
    <p key={index}>{line || "\u00A0"}</p>
  ));
};

export default function ChatMessageList({
  messages,
  isLoading,
  typingMessageId,
  animatedMessageIds,
  chatBodyRef,
  onTypingProgress,
  onTypingEnd,
  onScroll,
  onWheelCapture,
  onTouchStart,
  onTouchMove,
  showScrollToBottomButton,
  onScrollToBottom,
}: ChatMessageListProps) {
  return (
    <div
      className="chatbot-body"
      ref={chatBodyRef}
      onScroll={onScroll}
      onWheelCapture={onWheelCapture}
      onTouchStart={onTouchStart}
      onTouchMove={onTouchMove}
    >
      {messages.map((message) => {
        const isWelcomeMessage =
          message.role === "assistant" && message.id === 1;

        const shouldAnimate =
          message.role === "assistant" &&
          !isWelcomeMessage &&
          message.id === typingMessageId &&
          !animatedMessageIds.includes(message.id);

        return (
          <div
            key={message.id}
            className={`chat-message ${
              message.role === "user" ? "user" : "assistant"
            }`}
          >
            <div
              className={`chat-bubble ${isWelcomeMessage ? "welcome" : ""}`}
            >
              {message.role === "assistant" ? (
                <TypingText
                  content={message.content}
                  animate={shouldAnimate}
                  onTypingProgress={onTypingProgress}
                  onTypingEnd={() => onTypingEnd(message.id)}
                />
              ) : (
                renderMessageLines(message.content)
              )}
            </div>
          </div>
        );
      })}

      {isLoading && <LoadingBubble />}

      {showScrollToBottomButton && (
        <ScrollToBottomButton onClick={onScrollToBottom} />
      )}
    </div>
  );
}