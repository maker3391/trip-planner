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

        <h2 className="guide-popup-title">
          나만의 여행 일정을 쉽고 편리하게 계획해보세요
        </h2>

        <p className="guide-popup-description">
          출발지와 여행 정보를 입력하고, 목적지와 일정을 정리하여
          체계적인 여행 계획을 만들 수 있습니다.
        </p>

        <div className="guide-popup-content">
          <div className="guide-popup-card">
            <h3>이용 가이드</h3>
            <ul>
              <li>출발지와 여행 정보를 입력해 여행을 시작할 수 있습니다.</li>
              <li>목적지와 일정을 추가해 나만의 여행 플랜을 구성할 수 있습니다.</li>
              <li>로그인 후 여행 계획을 저장하고 관리할 수 있습니다.</li>
            </ul>
          </div>

          <div className="guide-popup-card">
            <h3>빠른 시작</h3>
            <ol>
              <li>로그인 후 서비스를 시작합니다.</li>
              <li>출발지와 여행 정보를 입력합니다.</li>
              <li>목적지와 일정을 추가합니다.</li>
              <li>완성된 계획을 확인합니다.</li>
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
            오늘 하루 보지 않음
          </label>

          <button className="guide-popup-close-btn" onClick={handleClose}>
            닫기
          </button>
        </div>
      </div>
    </div>
  );
}