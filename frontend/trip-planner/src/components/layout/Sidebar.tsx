import { useState } from "react";
import "./Sidebar.css";

interface SidebarProps {
  onSearch: (keyword: string) => void;
}

export default function Sidebar({ onSearch }: SidebarProps) {
  const [keyword, setKeyword] = useState("");
  const [isCollapsed, setIsCollapsed] = useState(false);

  const handleSearch = () => {
    const trimmedKeyword = keyword.trim();
    if (!trimmedKeyword) return;
    onSearch(trimmedKeyword);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      handleSearch();
    }
  };

  const toggleSidebar = () => {
    setIsCollapsed((prev) => !prev);
  };

  return (
    <aside className={`sidebar ${isCollapsed ? "collapsed" : ""}`}>
      <div className="sidebar-top">
        {!isCollapsed && <h2 className="sidebar-title">새 여행</h2>}

        <button
          type="button"
          className="sidebar-toggle"
          onClick={toggleSidebar}
          aria-label={isCollapsed ? "사이드바 펼치기" : "사이드바 접기"}
        >
          {isCollapsed ? "▶" : "◀"}
        </button>
      </div>

      {!isCollapsed && (
        <div className="sidebar-content">
          <div className="sidebar-intro">
            <div className="sidebar-illustration">🧳</div>

            <h3 className="sidebar-heading">여행 계획을 시작하세요</h3>

            <p className="sidebar-description">
              가까운 출발 지점을 선택하세요. 다음으로, 꼭 가봐야 할 명소를
              추가하고 꿈꾸던 여행을 계획해 보세요.
            </p>
          </div>

          <div className="sidebar-search-section">
            <label className="sidebar-label">검색어를 입력하세요.</label>
            <input
              className="sidebar-input"
              type="text"
              placeholder="도시, 지역, 장소 등"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyDown={handleKeyDown}
            />
            <p className="sidebar-helper-text">
              Enter 키를 누르면 해당 위치를 검색할 수 있어요.
            </p>
          </div>
        </div>
      )}
    </aside>
  );
}