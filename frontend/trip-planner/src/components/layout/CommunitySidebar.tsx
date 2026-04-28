import { useLocation, useNavigate } from "react-router-dom";
import RestartAltIcon from "@mui/icons-material/RestartAlt";
import "./CommunitySidebar.css";

interface CommunitySidebarProps {
  selectedCategories: string[];
  selectedRegions: string[];
  onCategoryChange: (categories: string[]) => void;
  onRegionChange: (regions: string[]) => void;
  onReset: () => void;
}

/**
 * 📌 카테고리 목록
 */
const CATEGORIES = [
  "전체보기",
  "자유게시판",
  "여행플랜",
  "후기게시판",
  "공지게시판"
];

/**
 * 📌 지역 목록 (❌ "미정" 제거됨)
 */
const REGIONS = [
  "전체보기",
  "서울특별시",
  "부산광역시",
  "대구광역시",
  "인천광역시",
  "광주광역시",
  "대전광역시",
  "울산광역시",
  "세종특별자치시",
  "경기도",
  "강원특별자치도",
  "충청도",
  "전라도",
  "경상도",
  "제주특별자치도"
];

export default function CommunitySidebar({
  selectedCategories,
  selectedRegions,
  onCategoryChange,
  onRegionChange,
  onReset
}: CommunitySidebarProps) {
  const navigate = useNavigate();
  const location = useLocation();

  /**
   * 📌 카테고리 토글
   */
  const toggleCategory = (value: string) => {
    if (value === "전체보기") {
      onCategoryChange(["전체보기"]);
      return;
    }

    let updated = selectedCategories.includes(value)
      ? selectedCategories.filter(v => v !== value)
      : [...selectedCategories.filter(v => v !== "전체보기"), value];

    if (updated.length === 0) updated = ["전체보기"];

    onCategoryChange(updated);
  };

  /**
   * 📌 지역 토글
   */
  const toggleRegion = (value: string) => {
    if (value === "전체보기") {
      onRegionChange(["전체보기"]);
      return;
    }

    let updated = selectedRegions.includes(value)
      ? selectedRegions.filter(v => v !== value)
      : [...selectedRegions.filter(v => v !== "전체보기"), value];

    if (updated.length === 0) updated = ["전체보기"];

    onRegionChange(updated);
  };

  /**
   * 📌 페이지 이동 + 필터 적용
   */
  const handleFilterClick = (type: "category" | "region", value: string) => {
    if (location.pathname === "/community") {
      if (type === "category") toggleCategory(value);
      else toggleRegion(value);
    } else {
      navigate("/community", {
        state: {
          [type]: value
        }
      });
    }
  };

  return (
    <aside className="community-sidebar">
      {/* ================= 카테고리 ================= */}
      <div className="sidebar-section">
        <h3>카테고리</h3>
        <ul>
          {CATEGORIES.map((cat) => (
            <li
              key={cat}
              className={selectedCategories.includes(cat) ? "active" : ""}
              onClick={() => handleFilterClick("category", cat)}
            >
              {cat}
            </li>
          ))}
        </ul>
      </div>

      {/* ================= 지역 ================= */}
      <div className="sidebar-section">
        <h3>지역별</h3>

        <div className="region-grid">
          {/* 🔥 전체보기 (2칸 강조) */}
          <span
            className={`region-all ${
              selectedRegions.includes("전체보기") ? "active" : ""
            }`}
            onClick={() => handleFilterClick("region", "전체보기")}
          >
            전체보기
          </span>

          {/* 🔥 나머지 지역 */}
          {REGIONS.filter((reg) => reg !== "전체보기").map((reg) => (
            <span
              key={reg}
              className={selectedRegions.includes(reg) ? "active" : ""}
              onClick={() => handleFilterClick("region", reg)}
            >
              {reg}
            </span>
          ))}
        </div>
      </div>

      {/* ================= 초기화 ================= */}
      <div className="filter-reset-area">
        <button className="filter-reset-btn" onClick={onReset}>
          <RestartAltIcon fontSize="inherit" /> 필터 초기화
        </button>
      </div>
    </aside>
  );
}