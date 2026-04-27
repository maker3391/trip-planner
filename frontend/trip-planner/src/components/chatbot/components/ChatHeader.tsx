import logo from "../../../assets/icons/tplanner2.png";

interface ChatHeaderProps {
  onClose: () => void;
  chatMode: "AI" | "CS";
  onToggleMode: () => void;
}

export default function ChatHeader({ onClose, chatMode, onToggleMode }: ChatHeaderProps) {
  return (
    <div className="chatbot-header">
      <div className="chatbot-title-row">
        <img
          src={logo}
          alt="T Planner Logo"
          className="chatbot-title-logo"
        />
        <button
          onClick={onToggleMode}
          style={{
            padding: '4px 8px', fontSize: '12px', borderRadius: '4px',
            border: '1px solid #ccc', backgroundColor: '#fff', cursor: 'pointer'
          }}
        >
          {chatMode === "AI" ? "🙋‍♂️ 상담원 연결" : "🤖 AI 챗봇"}
        </button>
      </div>

      <button
        className="chatbot-close"
        onClick={onClose}
        type="button"
        aria-label="챗봇 닫기"
      >
        <svg
          className="chatbot-close-svg"
          viewBox="0 0 24 24"
          fill="none"
          aria-hidden="true"
        >
          <path
            d="M6 12H18"
            stroke="currentColor"
            strokeWidth="3"
            strokeLinecap="round"
          />
        </svg>
      </button>
    </div>
  );
}