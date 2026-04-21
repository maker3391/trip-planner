interface ScrollToBottomButtonProps {
  onClick: () => void;
}

export default function ScrollToBottomButton({
  onClick,
}: ScrollToBottomButtonProps) {
  return (
    <button
      type="button"
      className="chat-scroll-bottom-btn"
      onClick={onClick}
      aria-label="최신 답변으로 이동"
      title="최신 답변으로 이동"
    >
      <svg
        className="chat-scroll-bottom-icon"
        viewBox="0 0 24 24"
        fill="none"
        aria-hidden="true"
      >
        <path
          d="M12 5V18"
          stroke="currentColor"
          strokeWidth="2.2"
          strokeLinecap="round"
        />
        <path
          d="M6.5 12.5L12 18L17.5 12.5"
          stroke="currentColor"
          strokeWidth="2.2"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
    </button>
  );
}