interface ChatBotButtonProps {
  onClick: () => void;
}

export default function ChatBotButton({ onClick }: ChatBotButtonProps) {
  return (
    <button
      className="chatbot-fab"
      onClick={onClick}
      aria-label="AI 여행 도우미 열기"
      type="button"
    >
      💬
    </button>
  );
}