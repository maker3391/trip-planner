import type {
  RefObject,
  TouchEvent as ReactTouchEvent,
  WheelEvent as ReactWheelEvent,
} from "react";
import type { ChatMessage, RecommendationPayload } from "../types/chatUi";
import TypingText from "./TypingText";
import LoadingBubble from "./LoadingBubble";
import ScrollToBottomButton from "./ScrollToBottomButton";
import ItineraryBubbleRenderer from "./ItineraryBubbleRenderer";
import RecommendationBubbleRenderer from "./RecommendationBubbleRenderer";

interface ChatMessageListProps {
  messages: ChatMessage[];
  isLoading: boolean;
  typingMessageId: number | null;
  animatedMessageIds: number[];
  chatBodyRef: RefObject<HTMLDivElement>;
  onTypingProgress: () => void;
  onTypingEnd: (messageId: number) => void;
  onScroll: () => void;
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

        const bubbleClassName = [
          "chat-bubble",
          isWelcomeMessage ? "welcome" : "",
          message.variant === "itinerary" ? "itinerary" : "",
          message.variant === "recommendation" ? "recommendation" : "",
          message.variant === "error" ? "error" : "",
        ]
          .filter(Boolean)
          .join(" ");

        const isItineraryMessage =
          message.role === "assistant" && message.variant === "itinerary";

        const recommendationPayload =
          message.role === "assistant" && message.variant === "recommendation"
            ? (message.payload as RecommendationPayload | undefined)
            : undefined;

        return (
          <div
            key={message.id}
            className={`chat-message ${
              message.role === "user" ? "user" : "assistant"
            }`}
          >
            <div className={bubbleClassName}>
              {isItineraryMessage ? (
                <ItineraryBubbleRenderer content={message.content} />
              ) : recommendationPayload ? (
                <RecommendationBubbleRenderer payload={recommendationPayload} />
              ) : message.role === "assistant" ? (
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