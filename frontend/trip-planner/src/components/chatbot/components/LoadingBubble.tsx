export default function LoadingBubble() {
  return (
    <div className="chat-message assistant">
      <div className="chat-bubble loading">
        <div className="chat-loading">
          <span className="chat-spinner" aria-hidden="true" />
          <p>여행 정보를 정리하고 있어요</p>
        </div>
      </div>
    </div>
  );
}