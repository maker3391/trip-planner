import { useEffect, useState } from "react";
import chatbot from "../../assets/icons/chatbot.webp";
import "./ChatBot.css";

interface ChatBotButtonProps {
  onClick: () => void;
}

export default function ChatBotButton({ onClick }: ChatBotButtonProps) {
  const [showTooltip, setShowTooltip] = useState(false);

  useEffect(() => {
    setShowTooltip(true);
  }, []);

  const handleClick = (): void => {
    onClick();
    setShowTooltip(false);
  };

  return (
    <div className="chatbot-fab-wrap">
      {showTooltip && (
        <div className="chatbot-tooltip">
          제가 도와드릴까요?
        </div>
      )}

      <button
        className="chatbot-fab"
        onClick={handleClick}
        aria-label="AI 여행 도우미 열기"
        type="button"
      >
        <img src={chatbot} alt="chatbot" />
      </button>
    </div>
  );
}