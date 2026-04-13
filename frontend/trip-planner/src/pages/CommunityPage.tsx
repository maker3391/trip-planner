import { useEffect, useState, useMemo } from "react";
import Header from "../components/layout/Header.tsx";
import SearchIcon from "@mui/icons-material/Search";
import ShareIcon from "@mui/icons-material/Share"; // 🔥 공유 아이콘
import RestartAltIcon from "@mui/icons-material/RestartAlt";
import "./CommunityPage.css";
import { useNavigate } from "react-router-dom";
import client from "../components/api/client.ts";
import { CommunityResponse, CommunityPageResponse } from "../types/community.ts";

// 🔹 게시글 목록 API 호출 함수
export const getCommunityPosts = async (
  page = 0,
  category: string | null = null,
  region: string | null = null,
  searchType: "title" | "author" | null = null,
  keyword: string | null = null
) => {
  const params: any = { page };

  // 🔹 카테고리 필터
  if (category && category !== "전체보기") params.category = category;

  // 🔹 지역 필터
  if (region) params.region = region;

  // 🔹 검색 필터
  if (searchType && keyword) {
    if (searchType === "title") params.title = keyword;
    if (searchType === "author") params.author = keyword;
  }

  // 🔹 GET 요청
  const response = await client.get("/community/posts", { params });
  return response.data;
};

export default function CommunityPage() {
  // 🔹 상태 관리
  const [page, setPage] = useState(0); // 현재 페이지
  const [totalPages, setTotalPages] = useState(0); // 전체 페이지 수
  const [posts, setPosts] = useState<CommunityResponse[]>([]); // 게시글 목록

  const [selectedCategory, setSelectedCategory] = useState("전체보기"); // 선택된 카테고리
  const [selectedRegion, setSelectedRegion] = useState<string | null>(null); // 선택된 지역

  const [searchType, setSearchType] = useState<"title" | "author">("title"); // 검색 타입
  const [keyword, setKeyword] = useState(""); // 검색 키워드

  const navigate = useNavigate();

  // 🔹 카테고리 목록
  const categories = [
    "전체보기","여행플랜 공유","자유게시판","질문게시판",
    "맛집게시판","후기게시판","공지게시판",
  ];

  // 🔹 지역 목록
  const regions = [
    "서울","경기","인천","강원","충북","충남",
    "전북","전남","경북","경남","제주"
  ];

  // 🔥 게시글 데이터 로딩
  useEffect(() => {
    const fetchPosts = async () => {
      try {
        const data: CommunityPageResponse | undefined =
          await getCommunityPosts(
            page,
            selectedCategory,
            selectedRegion,
            searchType,
            keyword || null
          );

        // 🔹 작성자 null 방지 처리
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
  }, [page, selectedCategory, selectedRegion, searchType, keyword]);

  // 🔥 조회수 증가 + URL 복사
  const handleView = async (postId: number) => {
    const url = window.location.origin + `/community/${postId}`;

    // 🔹 1. URL 복사 (실패해도 무시)
    try {
      await navigator.clipboard.writeText(url);
    } catch (e) {
      console.warn("클립보드 복사 실패 (무시됨)");
    }

    try {
      // 🔹 2. 조회수 증가 API 호출
      await client.patch(`/community/posts/${postId}/view`);

      // 🔹 3. UI 즉시 반영 (UX 향상)
      setPosts(prev =>
        prev.map(p =>
          Number(p.id) === postId
            ? { ...p, viewCount: p.viewCount + 1 }
            : p
        )
      );

    } catch (error) {
      console.error("조회수 증가 실패:", error);
    }
  };

  // 🔹 페이지 번호 계산
  const getPageNumbers = () => {
    const range = 5;
    const start = Math.max(0, page - range);
    const end = Math.min(totalPages - 1, page + range);

    const pages = [];
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  };

  const pageNumbers = useMemo(() => getPageNumbers(), [page, totalPages]);

  // 🔹 페이지 이동
  const goToPage = (p: number) => {
    if (p < 0 || p >= totalPages) return;
    setPage(p);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  // 🔹 필터 초기화
  const handleReset = () => {
    setSelectedCategory("전체보기");
    setSelectedRegion(null);
    setKeyword("");
    setPage(0);
  };

  // 🔹 검색 실행
  const handleSearch = () => setPage(0);

  return (
  <>
  <Header />
    <div className="community-page">
      

      <div className="community-container">

        {/* 🔹 사이드바 */}
        <aside className="community-sidebar">

          {/* 🔹 카테고리 */}
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

          {/* 🔹 지역 */}
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

          {/* 🔹 필터 초기화 */}
          <div className="filter-reset-area">
            <button className="filter-reset-btn" onClick={handleReset}>
              <RestartAltIcon fontSize="inherit" /> 필터 초기화
            </button>
          </div>
        </aside>

        {/* 🔹 메인 */}
        <main className="community-main-content">

          {/* 🔹 상단 */}
          <header className="community-content-header">
            <div className="title-area">
              <h1>커뮤니티</h1>
              <p>여행 계획을 공유하고 소통하세요!</p>
            </div>

            {/* 🔹 검색 */}
            <div className="community-search-bar">
              <select
                value={searchType}
                onChange={(e) =>
                  setSearchType(e.target.value as "title" | "author")
                }
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
                  onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                />
                <button onClick={handleSearch}>
                  <SearchIcon fontSize="small" />
                </button>
              </div>
            </div>
          </header>

          {/* 🔥 게시판 */}
          <div className="community-board-container">

            {/* 🔹 헤더 */}
            <div className="board-header-row">
              <div className="col-id">번호</div>
              <div className="col-route">기타</div>
              <div className="col-title">제목</div>
              <div className="col-author">작성자</div>
              <div className="col-date">날짜</div>
              <div className="col-views">조회</div>
              <div className="col-stats">공유</div>
            </div>

            {/* 🔹 리스트 */}
            <div className="board-body">
              {posts.length === 0 ? (
                <div className="no-posts">게시글이 없습니다</div>
              ) : (
                posts.map((post) => (
                  <div
                    key={post.id}
                    className="board-item-row"
                    // 🔥 클릭 시 조회수 증가 + 페이지 이동
                    onClick={async () => {
                      await handleView(Number(post.id)); // ✅ 조회수 증가 완료 후
                      navigate(`/community/${post.id}`); // ✅ 상세 이동
                    }}
                  >
                    <div className="col-id">{post.id}</div>

                    <div className="col-route">
                      {post.departure || ""} - {post.arrival || ""}
                    </div>

                    <div className="col-title">{post.title}</div>

                    <div className="col-author">{post.authorNickname}</div>

                    <div className="col-date">
                      {post.createdAt?.split("T")[0]}
                    </div>

                    <div className="col-views">{post.viewCount}</div>

                    {/* 🔥 공유 수 (recommendCount 재사용) */}
                    <div className="col-stats">
                      <ShareIcon fontSize="inherit" />{" "}
                      {post.recommendCount}
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* 🔹 페이지네이션 */}
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

          {/* 🔹 글쓰기 버튼 */}
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