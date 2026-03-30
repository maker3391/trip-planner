import "./Sidebar.css";

export default function Sidebar() {
  return (
    <aside className="sidebar">
      <div className="sidebar-top">
        <h2 className="sidebar-title">새 여행</h2>
      </div>

      <div className="sidebar-content">
        <div className="sidebar-illustration">🧳</div>

        <h3 className="sidebar-heading">여행 계획을 시작하세요</h3>

        <p className="sidebar-description">
          가까운 출발 지점을 선택하세요. 다음으로, 꼭 가봐야 할 명소를 추가하고
          꿈꾸던 여행을 계획해 보세요.
        </p>

        <label className="sidebar-label">내 출발 지점</label>
        <input
          className="sidebar-input"
          type="text"
          placeholder="도시 또는 기차역"
        />

        <button className="sidebar-button">여행을 계획해 보세요</button>
      </div>
    </aside>
  );
}