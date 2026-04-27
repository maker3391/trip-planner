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
const PLAN_SHARE_ENABLED_CATEGORIES = ["여행플랜"];
const ADMIN_ONLY_CATEGORIES = ["공지게시판"];

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
  categories: string[] | null = null,
  regions: string[] | null = null,
  searchType: SearchOption = null,
  keyword: string | null = null
) => {
  const params: any = { page };

  // 규칙 2: 다중선택 배열 전달 처리
  // 선택된 카테고리 배열을 그대로 params에 할당하여 전송 (백엔드에서 List<String> 형태로 받아 OR 연산)
  if (categories && !categories.includes("전체보기")) {
    params.categories = categories;
  }

  // 기존 코드의 "전체" 문자열 외에, UI상 표시되는 "전체보기"도 예외 처리
  if (regions && !regions.includes("전체") && !regions.includes("전체보기")) {
    params.regions = regions;
  }

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
  
  const [notices, setNotices] = useState<CommunityResponse[]>([]);
  const [posts, setPosts] = useState<CommunityResponse[]>([]);
  
  const [isNoticeExpanded, setIsNoticeExpanded] = useState(false);

  const [selectedCategories, setSelectedCategories] = useState<string[]>(["전체보기"]);
  const [selectedRegions, setSelectedRegions] = useState<string[]>(["전체보기"]);

  const [searchType, setSearchType] = useState<SearchOption>("title");
  const [keyword, setKeyword] = useState("");
  const [activeKeyword, setActiveKeyword] = useState("");

  useEffect(() => {
    if (navState) {
      setSelectedCategories(navState.category ? [navState.category] : ["전체보기"]);
      setSelectedRegions(navState.region ? [navState.region] : ["전체보기"]);
      setPage(0);
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [location.key, location.pathname, navigate, navState]);

  useEffect(() => {
    const fetchNotices = async () => {
      try {
        // 타입 에러 방지 및 다중선택 룰 적용을 위해 문자열 대신 배열 형태로 ["공지게시판"] 전달
        const data: CommunityPageResponse = await getCommunityPosts(0, ["공지게시판"], null, null, null);
        
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

  useEffect(() => {
    const fetchPosts = async () => {
      try {
        // 규칙 1: 카테고리 우선순위는 백엔드 QueryDSL 혹은 로직에서 처리되도록 위임.
        // 규칙 2: 다중선택 OR 연산을 위해 기존 selectedCategories[0] 형태에서 배열 전체 전달로 변경.
        const data: CommunityPageResponse | undefined = await getCommunityPosts(
          page,
          selectedCategories,
          selectedRegions,
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
  }, [page, selectedCategories, selectedRegions, searchType, activeKeyword]);

  const handleReset = () => {
    setSelectedCategories(["전체보기"]);
    setSelectedRegions(["전체보기"]);
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
            selectedCategories={selectedCategories}
            selectedRegions={selectedRegions}
            onCategoryChange={(cats) => {
              // 규칙 3: 토글 지정 취소 시, 배열이 비어있으면(마지막 지정 해제) 자동으로 '전체보기' 지정
              const newCategories = (!cats || cats.length === 0) ? ["전체보기"] : cats;
              setSelectedCategories(newCategories);
              setPage(0);
            }}
            onRegionChange={(regs) => {
              // 규칙 3: 토글 지정 취소 시, 배열이 비어있으면(마지막 지정 해제) 자동으로 '전체보기' 지정
              const newRegions = (!regs || regs.length === 0) ? ["전체보기"] : regs;
              setSelectedRegions(newRegions);
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