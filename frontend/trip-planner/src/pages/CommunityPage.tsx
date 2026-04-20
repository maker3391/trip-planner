import { useEffect, useState, useMemo } from "react";
import { useNavigate, useLocation } from "react-router-dom"; // 🔥 다시 추가
import Header from "../components/layout/Header.tsx";
import CommunitySidebar from "../components/layout/CommunitySidebar.tsx";
import SearchIcon from "@mui/icons-material/Search";
import ShareIcon from "@mui/icons-material/Share";
import ArrowRightAltIcon from "@mui/icons-material/ArrowRightAlt";
import "./CommunityPage.css";
import client from "../components/api/client.ts";
import { CommunityResponse, CommunityPageResponse } from "../types/community.ts";

const RATING_ENABLED_CATEGORIES = ["맛집게시판", "후기게시판", "사진게시판"];
const PLAN_SHARE_ENABLED_CATEGORIES = ["여행플랜 공유"];
const ADMIN_ONLY_CATEGORIES = ["공지게시판"];

// 🔥 API 요청 함수
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
    params.searchType = searchType;
    params.keyword = keyword;
  }

  const response = await client.get("/community/posts", { params });
  return response.data;
};

export default function CommunityPage() {
  const navigate = useNavigate();
  const location = useLocation();

  // 🔥 이동 시 전달된 state
  const navState = location.state as { category?: string; region?: string };

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [posts, setPosts] = useState<CommunityResponse[]>([]);

  // 🔥 초기값 적용 (핵심)
  const [selectedCategory, setSelectedCategory] = useState(
    navState?.category || "전체보기"
  );

  const [selectedRegion, setSelectedRegion] = useState<string | null>(
    navState?.region || "전체"
  );

  const [searchType, setSearchType] = useState<"title" | "author">("title");
  const [keyword, setKeyword] = useState("");
  const [activeKeyword, setActiveKeyword] = useState("");

  // 🔥 이동 후 state 반영 (뒤로가기 대응)
  useEffect(() => {
    if (navState) {
      setSelectedCategory(navState.category || "전체보기");
      setSelectedRegion(navState.region || "전체");
      setPage(0);

      // 🔥 중복 적용 방지
      window.history.replaceState({}, document.title);
    }
  }, [location.key]);

  // 🔥 게시글 데이터 로딩
  useEffect(() => {
    const fetchPosts = async () => {
      try {
        const data: CommunityPageResponse | undefined = await getCommunityPosts(
          page,
          selectedCategory,
          selectedRegion,
          searchType,
          activeKeyword || null
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
  }, [page, selectedCategory, selectedRegion, searchType, activeKeyword]);

  const handleReset = () => {
    setSelectedCategory("전체보기");
    setSelectedRegion("전체");
    setKeyword("");
    setActiveKeyword("");
    setPage(0);
  };

  const handleSearch = () => {
    setActiveKeyword(keyword.trim());
    setPage(0);
  };

  const goToPage = (p: number) => {
    if (p < 0 || p >= totalPages) return;
    setPage(p);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const pageNumbers = useMemo(() => {
    const range = 5;
    const start = Math.max(0, page - range);
    const end = Math.min(totalPages - 1, page + range);
    const pages = [];
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  }, [page, totalPages]);

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

    if (post.category && ADMIN_ONLY_CATEGORIES.includes(post.category)) {
      return " 관리자 게시글 ";
    }


    return " - ";
  };

  return (
    <>
      <Header />
      <div className="community-page">
        <div className="community-container">

          <CommunitySidebar
            selectedCategory={selectedCategory}
            selectedRegion={selectedRegion}
            onCategoryChange={(cat) => {
              setSelectedCategory(cat);
              setPage(0);
            }}
            onRegionChange={(reg) => {
              setSelectedRegion(reg);
              setPage(0);
            }}
            onReset={handleReset}
          />

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
                    setPage(0);
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