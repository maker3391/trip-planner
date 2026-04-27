import { useState } from "react";
import { ChevronLeft, ChevronRight, Luggage } from "lucide-react";
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
          <div className="sidebar-illustration" aria-hidden="true">
            <Luggage size={54} strokeWidth={1.9} />
          </div>

          <h3 className="sidebar-heading">여행 경로를 만들어 보세요</h3>

          <p className="sidebar-description">
            출발지나 여행지를 검색해 주세요 선택한 장소를 기준으로 여행
            경로를 만들 수 있어요
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