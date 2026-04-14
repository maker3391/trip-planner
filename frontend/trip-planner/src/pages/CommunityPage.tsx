import { useEffect, useState, useMemo } from "react";
import Header from "../components/layout/Header.tsx";
import SearchIcon from "@mui/icons-material/Search";
import ShareIcon from "@mui/icons-material/Share";
import ArrowRightAltIcon from "@mui/icons-material/ArrowRightAlt";
import RestartAltIcon from "@mui/icons-material/RestartAlt";
import "./CommunityPage.css";
import { useNavigate } from "react-router-dom";
import client from "../components/api/client.ts";
import { CommunityResponse, CommunityPageResponse } from "../types/community.ts";

// =========================
// 카테고리 조건
// =========================
const RATING_ENABLED_CATEGORIES = ["맛집게시판", "후기게시판", "사진게시판"];
const PLAN_SHARE_ENABLED_CATEGORIES = ["여행플랜 공유"];

// 🔹 게시글 목록 API 호출 함수
export const getCommunityPosts = async (
  page = 0,
  category: string | null = null,
  region: string | null = null,
  searchType: "title" | "author" | null = null,
  keyword: string | null = null
) => {
  const params: any = { page };

  if (category && category !== "전체보기") params.category = category;
  if (region && region !== "전체") params.region = region;

  if (searchType && keyword) {
    if (searchType === "title") params.title = keyword;
    if (searchType === "author") params.author = keyword;
  }

  const response = await client.get("/community/posts", { params });
  return response.data;
};

export default function CommunityPage() {
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [posts, setPosts] = useState<CommunityResponse[]>([]);

  const [selectedCategory, setSelectedCategory] = useState("전체보기");
  const [selectedRegion, setSelectedRegion] = useState<string | null>("전체");

  const [searchType, setSearchType] = useState<"title" | "author">("title");
  const [keyword, setKeyword] = useState(""); // 입력용
  const [activeKeyword, setActiveKeyword] = useState(""); // 🔹 실제 검색용

  const navigate = useNavigate();

  const categories = [
    "전체보기", // 🔹 빈 값 수정
    "자유게시판",
    "질문게시판",
    "여행플랜 공유",
    "맛집게시판",
    "후기게시판",
    "사진게시판",
    "공지게시판"
  ];

  const regions = [
    "전체","서울","경기","인천","강원","충북",
    "충남","전북","전남","경북","경남","제주"
  ];

  // 🔥 게시글 데이터 로딩 (최적화됨)
  useEffect(() => {
    const fetchPosts = async () => {
      try {
        const data: CommunityPageResponse | undefined = await getCommunityPosts(
          page,
          selectedCategory,
          selectedRegion,
          searchType,
          activeKeyword || null // 🔹 keyword 대신 activeKeyword 사용
        );

        const postsWithAuthor = (data?.content || []).map((post) => ({
          ...post,
          authorNickname: post.authorNickname || "익명",
        }));

        setPosts(postsWithAuthor);
        setTotalPages(data?.totalPages || 0);
      } catch (error) {
        console.error("게시글 불러오기 실패:", error);
        setPosts([]);
      }
    };

    fetchPosts();
    // 🔹 keyword는 의존성 배열에서 제외하여 타이핑 시 API 호출 방지
  }, [page, selectedCategory, selectedRegion, searchType, activeKeyword]);

  const renderRouteOrRating = (post: CommunityResponse) => {
    if (post.category && RATING_ENABLED_CATEGORIES.includes(post.category)) {
      const rating = post.rating || 0;
      return (
        <div className="rating-stars" style={{ color: "#FFBB00", fontSize: "16px" }}>
          {Array.from({ length: 5 }).map((_, i) => (
            <span key={i}>{i < rating ? "★" : "☆"}</span>
          ))}
        </div>
      );
    }

    if (post.category && PLAN_SHARE_ENABLED_CATEGORIES.includes(post.category)) {
      if (!post.departure && !post.arrival) return " - ";
      return (
        <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "4px" }}>
          <span>{post.departure || "미정"}</span>
          <ArrowRightAltIcon fontSize="small" />
          <span>{post.arrival || "미정"}</span>
        </div>
      );
    }
    return " - ";
  };

  const pageNumbers = useMemo(() => {
    const range = 5;
    const start = Math.max(0, page - range);
    const end = Math.min(totalPages - 1, page + range);
    const pages = [];
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  }, [page, totalPages]);

  const goToPage = (p: number) => {
    if (p < 0 || p >= totalPages) return;
    setPage(p);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleReset = () => {
    setSelectedCategory("전체보기");
    setSelectedRegion("전체");
    setKeyword("");
    setActiveKeyword(""); // 🔹 검색 키워드도 초기화
    setPage(0);
  };

  // 🔹 검색 버튼 클릭/엔터 시 실행되는 핸들러
  const handleSearch = () => {
    const trimmed = keyword.trim(); // 공백 제거
    setActiveKeyword(trimmed);  
    setPage(0); // 첫 페이지로 이동
  };

  return (
    <>
      <Header />
      <div className="community-page">
        <div className="community-container">
          <aside className="community-sidebar">
            <div className="sidebar-section">
              <h3>카테고리</h3>
              <ul>
                {categories.map((cat) => (
                  <li
                    key={cat}
                    className={selectedCategory === cat ? "active" : ""}
                    onClick={() => {
                      setSelectedCategory(cat);
                      setPage(0);
                    }}
                  >
                    {cat}
                  </li>
                ))}
              </ul>
            </div>

            <div className="sidebar-section">
              <h3>지역별</h3>
              <div className="region-grid">
                {regions.map((reg) => (
                  <span
                    key={reg}
                    className={selectedRegion === reg ? "active" : ""}
                    onClick={() => {
                      setSelectedRegion(reg);
                      setPage(0);
                    }}
                  >
                    {reg}
                  </span>
                ))}
              </div>
            </div>

            <div className="filter-reset-area">
              <button className="filter-reset-btn" onClick={handleReset}>
                <RestartAltIcon fontSize="inherit" /> 필터 초기화
              </button>
            </div>
          </aside>

          <main className="community-main-content">
            <header className="community-content-header">
              <div className="title-area">
                <h1>게시판</h1>
                <p>여행 계획을 공유하고 소통하세요!</p>
              </div>

              <div className="community-search-bar">
                <select
                  value={searchType}
                  onChange={(e) => {
                    setSearchType(e.target.value as "title" | "author");
                    setPage(0); // 타입 변경 시 페이지 초기화
                  }}
                >
                  <option value="title">제목</option>
                  <option value="author">작성자</option>
                </select>

                <div className="search-input-box">
                  <input
                    type="text"
                    placeholder="검색어 입력"
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") handleSearch();
                    }}
                  />
                  <button onClick={handleSearch}>
                    <SearchIcon fontSize="small" />
                  </button>
                </div>
              </div>
            </header>

            <div className="community-board-container">
              <div className="board-header-row">
                <div className="col-id">번호</div>
                <div className="col-route">기타</div>
                <div className="col-title">제목</div>
                <div className="col-author">작성자</div>
                <div className="col-date">날짜</div>
                <div className="col-views">조회</div>
                <div className="col-stats">좋아요</div>
                <div className="col-share">공유</div>
              </div>

              <div className="board-body">
                {posts.length === 0 ? (
                  <div className="no-posts">게시글이 없습니다</div>
                ) : (
                  posts.map((post) => (
                    <div
                      key={post.id}
                      className="board-item-row"
                      onClick={() => navigate(`/community/${post.id}`)}
                    >
                      <div className="col-id">{post.id}</div>
                      <div className="col-route">{renderRouteOrRating(post)}</div>
                      <div className="col-title">{post.title}</div>
                      <div className="col-author">{post.authorNickname}</div>
                      <div className="col-date">{post.createdAt?.split("T")[0]}</div>
                      <div className="col-views">{post.viewCount}</div>
                      <div className="col-stats">{post.likeCount}</div>
                      <div className="col-share">
                        <ShareIcon fontSize="inherit" /> {post.shareCount}
                      </div>
                    </div>
                  ))
                )}
              </div>

              <div className="pagination">
                <button onClick={() => goToPage(0)} disabled={page === 0}>{"<<"}</button>
                <button onClick={() => goToPage(page - 1)} disabled={page === 0}>{"<"}</button>
                {pageNumbers.map((p) => (
                  <button
                    key={p}
                    onClick={() => goToPage(p)}
                    className={page === p ? "active-page" : ""}
                  >
                    {p + 1}
                  </button>
                ))}
                <button onClick={() => goToPage(page + 1)} disabled={page === totalPages - 1}>{">"}</button>
                <button onClick={() => goToPage(totalPages - 1)} disabled={page === totalPages - 1}>{">>"}</button>
              </div>
            </div>

            <div className="community-footer">
              <button className="write-button" onClick={() => navigate("/community/write")}>
                글쓰기
              </button>
            </div>
          </main>
        </div>
      </div>
    </>
  );
}