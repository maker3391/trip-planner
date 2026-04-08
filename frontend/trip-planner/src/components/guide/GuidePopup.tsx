import { useState } from "react";
import "./GuidePopup.css";

interface GuidePopupProps {
  open: boolean;
  onClose: () => void;
}

export default function GuidePopup({ open, onClose }: GuidePopupProps) {
  const [dontShowToday, setDontShowToday] = useState(false);

  if (!open) return null;

  const handleClose = () => {
    if (dontShowToday) {
      const today = new Date().toISOString().split("T")[0];
      localStorage.setItem("hideGuidePopupDate", today);
    }
    onClose();
  };

  return (
    <div className="guide-popup-overlay">
      <div className="guide-popup">
        <div className="guide-popup-badge">TRAVEL GUIDE</div>

        <h2 className="guide-popup-title">지도로 여행 계획을 시작해보세요</h2>

        <p className="guide-popup-description">
          장소를 검색하거나 지도에서 직접 선택하고, 핀으로 위치를 확인한 뒤
          일정에 추가해 나만의 여행 동선을 만들어보세요.
        </p>

        <div className="guide-popup-highlight">
          <span className="guide-popup-highlight-icon">📍</span>
          검색 + 지도 클릭 + 일정 추가까지 한 번에 사용할 수 있어요
        </div>

        <div className="guide-popup-content">
          <div className="guide-popup-card">
            <h3>이렇게 사용해보세요</h3>
            <ul>
              <li>검색으로 원하는 장소를 빠르게 찾을 수 있어요.</li>
              <li>지도에서 직접 장소를 클릭하면 위치를 바로 확인할 수 있어요.</li>
              <li>선택한 장소는 핀으로 표시되어 동선을 쉽게 파악할 수 있어요.</li>
            </ul>
          </div>

          <div className="guide-popup-card guide-popup-card-accent">
            <h3>빠른 시작</h3>
            <ol>
              <li>장소를 검색하거나 지도에서 직접 클릭해보세요.</li>
              <li>표시된 핀으로 위치를 확인해보세요.</li>
              <li>마음에 드는 장소를 일정에 추가해보세요.</li>
              <li>여행 순서에 맞게 나만의 계획을 완성해보세요.</li>
            </ol>
          </div>
        </div>

        <div className="guide-popup-footer">
          <label className="guide-popup-checkbox">
            <input
              type="checkbox"
              checked={dontShowToday}
              onChange={(e) => setDontShowToday(e.target.checked)}
            />
            <span>오늘 하루 보지 않음</span>
          </label>

          <button className="guide-popup-close-btn" onClick={handleClose}>
            시작하기
          </button>
        </div>
      </div>
    </div>
  );
}