import logo from "../../../assets/icons/tplanner2.png";

interface ChatHeaderProps {
  onClose: () => void;
}

export default function ChatHeader({ onClose }: ChatHeaderProps) {
  return (
    <div className="chatbot-header">
      <div className="chatbot-title-row">
        <img
          src={logo}
          alt="T Planner Logo"
          className="chatbot-title-logo"
        />
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