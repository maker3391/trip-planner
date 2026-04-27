import { useState } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
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
        <button
          type="button"
          className="sidebar-toggle"
          onClick={toggleSidebar}
          aria-label={isCollapsed ? "사이드바 펼치기" : "사이드바 접기"}
        >
          {isCollapsed ? (
            <ChevronRight size={18} strokeWidth={2.2} />
          ) : (
            <ChevronLeft size={18} strokeWidth={2.2} />
          )}
        </button>
      </div>

      <div className="sidebar-content" aria-hidden={isCollapsed}>
        <div className="sidebar-intro">
          <div className="sidebar-illustration">🧳</div>

          <h3 className="sidebar-heading">어디로 떠나볼까요?</h3>

          <p className="sidebar-description">
            가까운 출발 지점을 선택하세요 <br />
            다음으로, 꼭 가봐야 할 명소를 추가하고 꿈꾸던 여행을 계획해 보세요
          </p>
        </div>

        <div className="sidebar-search-section">
          <label className="sidebar-label">검색어를 입력하세요</label>
          <input
            className="sidebar-input"
            type="text"
            placeholder="도시, 지역, 장소 등"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onKeyDown={handleKeyDown}
            tabIndex={isCollapsed ? -1 : 0}
          />
          <p className="sidebar-helper-text">
            Enter 키를 누르면 해당 위치를 검색할 수 있어요
          </p>
        </div>
      </div>
    </aside>
  );
}