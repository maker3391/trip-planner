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
 * - "전체보기"는 모든 카테고리를 의미하는 특수값
 */
const CATEGORIES = [
  "전체보기",
  "자유게시판",
  "여행플랜",
  "후기게시판",
  "공지게시판"
];

/**
 * 📌 지역 목록
 * - "전체보기" : 모든 지역 포함
 * - "미정" : DB 상 region = "미정" 또는 null
 * 👉 UI에서는 "지정 없음"으로 표시
 */
const REGIONS = [
  "전체보기",
  "미정",
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
   * 📌 카테고리 토글 로직
   * 규칙 1: 카테고리 우선도 적용을 위해 백엔드로 다중 선택 배열을 전달합니다. (백엔드 로직에서 카테고리 조건 우선 평가)
   * 규칙 2: 일반 카테고리 선택 시 기존 상태를 유지하며 새로운 값을 추가/제거하여 OR 연산이 가능하도록 배열로 관리합니다.
   */
  const toggleCategory = (value: string) => {
    if (value === "전체보기") {
      onCategoryChange(["전체보기"]);
      return;
    }

    let updated = selectedCategories.includes(value)
      ? selectedCategories.filter(v => v !== value)
      : [...selectedCategories.filter(v => v !== "전체보기"), value];

    // 규칙 3: 토글로 지정 취소를 했을 때, 배열이 비게 되면 자동으로 "전체보기"가 지정되도록 복구합니다.
    if (updated.length === 0) updated = ["전체보기"];

    onCategoryChange(updated);
  };

  /**
   * 📌 지역 토글 로직
   * 규칙 2: 지역 역시 다중 선택 시 OR 조건 연산이 되도록 배열 형태로 추가 및 삭제를 진행합니다.
   */
  const toggleRegion = (value: string) => {
    if (value === "전체보기") {
      onRegionChange(["전체보기"]);
      return;
    }

    let updated = selectedRegions.includes(value)
      ? selectedRegions.filter(v => v !== value)
      : [...selectedRegions.filter(v => v !== "전체보기"), value];

    // 규칙 3: 토글 지정 취소 시 마지막 값이 없어지면 자동으로 "전체보기" 지정
    if (updated.length === 0) updated = ["전체보기"];

    onRegionChange(updated);
  };

  /**
   * 📌 페이지 이동 + 필터 적용 처리
   */
  const handleFilterClick = (type: "category" | "region", value: string) => {
    if (location.pathname === "/community") {
      if (type === "category") toggleCategory(value);
      else toggleRegion(value);
    } else {
      navigate("/community", {
        state: {
          // 규칙 5 준수: 기존 CommunityPage에서 navState가 string 타입을 받아 배열화([navState.category])하므로,
          // 인수의 구조가 깨지지 않도록 외부 페이지에서 접근 시 단일 문자열로 넘깁니다. 
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
          {REGIONS.map((reg) => (
            <span
              key={reg}
              className={selectedRegions.includes(reg) ? "active" : ""}
              onClick={() => handleFilterClick("region", reg)}
            >
              {/* 👉 "미정"은 UI에서 "지정 없음"으로 표시 (규칙 1과 관련된 휴먼 에러 완화 요소) */}
              {reg === "미정" ? "지정 없음" : reg}
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