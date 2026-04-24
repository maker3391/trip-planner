import { useEffect, useState, useMemo } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import Header from "../components/layout/Header.tsx";
import CommunitySidebar from "../components/layout/CommunitySidebar.tsx";
import SearchIcon from "@mui/icons-material/Search";
import ArrowRightAltIcon from "@mui/icons-material/ArrowRightAlt";
import "./CommunityPage.css";
import client from "../components/api/client.ts";
import { CommunityResponse, CommunityPageResponse } from "../types/community.ts";
import CommunityList from "../components/layout/CommunityList.tsx";

const RATING_ENABLED_CATEGORIES = ["후기게시판"];
const PLAN_SHARE_ENABLED_CATEGORIES = ["여행플랜 공유"];
const ADMIN_ONLY_CATEGORIES = ["공지게시판"];

// ✅ 재사용성을 위해 검색 타입 분리
type SearchOption = 
  | "title"
  | "author"
  | "content"
  | "tag"
  | "title_author"
  | "title_content"
  | null;

export const getCommunityPosts = async (
  page = 0,
  category: string | null = null,
  region: string | null = null,
  searchType: SearchOption = null,
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

  const navState = location.state as { category?: string; region?: string } | null;

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  
  // 상태 분리: 공지사항과 일반 게시글을 따로 관리합니다.
  const [notices, setNotices] = useState<CommunityResponse[]>([]);
  const [posts, setPosts] = useState<CommunityResponse[]>([]);
  
  const [isNoticeExpanded, setIsNoticeExpanded] = useState(false);

  const [selectedCategory, setSelectedCategory] = useState(
    navState?.category || "전체보기"
  );

  const [selectedRegion, setSelectedRegion] = useState<string | null>(
    navState?.region || "전체"
  );

  const [searchType, setSearchType] = useState<SearchOption>("title");
  const [keyword, setKeyword] = useState("");
  const [activeKeyword, setActiveKeyword] = useState("");

  useEffect(() => {
    if (navState) {
      setSelectedCategory(navState.category || "전체보기");
      setSelectedRegion(navState.region || "전체");
      setPage(0);
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [location.key, location.pathname, navigate, navState]);

  // ✅ 1. 공지사항 전용 Fetch (상단 고정용)
  useEffect(() => {
    const fetchNotices = async () => {
      try {
        const data: CommunityPageResponse = await getCommunityPosts(0, "공지게시판", null, null, null);
        
        const noticesWithAuthor = (data?.content || []).map((notice) => ({
          ...notice,
          authorNickname: notice.authorNickname || "관리자", 
        }));

        setNotices(noticesWithAuthor);
      } catch (error) {
        console.error("공지사항 불러오기 실패:", error);
      }
    };

    fetchNotices();
  }, []);

  // ✅ 2. 일반 게시글 전용 Fetch (하단 리스트용)
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

        // 🔥 수정됨: 기존의 .filter() 로직을 제거하여 서버에서 온 10개의 데이터를 그대로 출력합니다.
        // 이렇게 하면 페이징 크기가 일정하게 유지되어 UI가 깨지지 않습니다.
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
      return "공지사항";
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
                  value={searchType || "title"}
                  onChange={(e) => {
                    setSearchType(e.target.value as SearchOption);
                    setPage(0);
                  }}
                >
                  <option value="title">제목</option>
                  <option value="content">내용</option>
                  <option value="author">작성자</option>
                  <option value="tag">태그</option>
                  <option value="title_author">제목+작성자</option>
                  <option value="title_content">제목+내용</option>
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

            <CommunityList
              posts={posts}
              notices={notices}
              page={page}
              totalPages={totalPages}
              goToPage={goToPage}
              pageNumbers={pageNumbers}
              navigate={navigate}
              renderRouteOrRating={renderRouteOrRating}
              isNoticeExpanded={isNoticeExpanded}
              setIsNoticeExpanded={setIsNoticeExpanded}
            />

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