import { useLocation, useNavigate } from "react-router-dom";
import RestartAltIcon from "@mui/icons-material/RestartAlt";
import "./CommunitySidebar.css";

interface CommunitySidebarProps {
  selectedCategory: string;
  selectedRegion: string | null;
  onCategoryChange: (category: string) => void;
  onRegionChange: (region: string) => void;
  onReset: () => void;
}

const CATEGORIES = [
  "전체보기", "자유게시판", "질문게시판", "여행플랜 공유",
  "맛집게시판", "후기게시판", "사진게시판", "공지게시판"
];

const REGIONS = [
  "전체","서울","경기","인천","강원","충북",
  "충남","전북","전남","경북","경남","제주"
];

export default function CommunitySidebar({
  selectedCategory,
  selectedRegion,
  onCategoryChange,
  onRegionChange,
  onReset
}: CommunitySidebarProps) {
  const navigate = useNavigate();
  const location = useLocation();

  // 🔥 필터 클릭 핸들러 (단일 구조)
  const handleFilterClick = (type: "category" | "region", value: string) => {
    if (location.pathname === "/community") {
      // 👉 현재 페이지면 state만 변경 (API 재호출)
      if (type === "category") onCategoryChange(value);
      else onRegionChange(value);
    } else {
      // 👉 다른 페이지면 이동 + state 전달
      navigate("/community", {
        state: {
          [type]: value
        }
      });
    }
  };

  return (
    <aside className="community-sidebar">
      <div className="sidebar-section">
        <h3>카테고리</h3>
        <ul>
          {CATEGORIES.map((cat) => (
            <li
              key={cat}
              className={selectedCategory === cat ? "active" : ""}
              onClick={() => handleFilterClick("category", cat)}
            >
              {cat}
            </li>
          ))}
        </ul>
      </div>

      <div className="sidebar-section">
        <h3>지역별</h3>
        <div className="region-grid">
          {REGIONS.map((reg) => (
            <span
              key={reg}
              className={selectedRegion === reg ? "active" : ""}
              onClick={() => handleFilterClick("region", reg)}
            >
              {reg}
            </span>
          ))}
        </div>
      </div>

      <div className="filter-reset-area">
        <button className="filter-reset-btn" onClick={onReset}>
          <RestartAltIcon fontSize="inherit" /> 필터 초기화
        </button>
      </div>
    </aside>
  );
}