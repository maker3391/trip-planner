import { useState, useEffect, useMemo } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "../components/layout/Header.tsx";
import client from "../components/api/client.ts";
import { CommunityResponse, CommunityPageResponse } from "../types/community.ts";

import ShareIcon from '@mui/icons-material/Share';
import RemoveRedEyeIcon from '@mui/icons-material/RemoveRedEye';
import SearchIcon from "@mui/icons-material/Search";
import RestartAltIcon from "@mui/icons-material/RestartAlt";

import "./CommunityReadPage.css"; // 기존 CSS에 사이드바 스타일이 포함되어 있어야 합니다.
import { getCommunityPosts } from "./CommunityPage.tsx";

export const getPost = async (id: number) => {
    const res = await client.get(`/community/posts/${id}`);
    return res.data;
};

const RATING_ENABLED_CATEGORIES = ["맛집게시판", "사진게시판", "후기게시판"];
const PLAN_SHARE_ENABLED_CATEGORIES = ["여행플랜 공유", "당일치기 친구 찾기"];

export default function CommunityReadPage() {
    const { id } = useParams();
    const navigate = useNavigate();

    // 🔹 상태 관리
    const [post, setPost] = useState<CommunityResponse>();
    const [posts, setPosts] = useState<CommunityResponse[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    // 🔹 필터 및 검색 상태 (추가됨)
    const [selectedCategory, setSelectedCategory] = useState("전체보기");
    const [selectedRegion, setSelectedRegion] = useState<string | null>(null);
    const [searchType, setSearchType] = useState<"title" | "author">("title");
    const [keyword, setKeyword] = useState("");

    const categories = ["전체보기", "여행플랜 공유", "자유게시판", "질문게시판", "맛집게시판", "후기게시판", "공지게시판"];
    const regions = ["서울", "경기", "인천", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"];

    // 1. 상세 글 로드
    useEffect(() => {
        const fetchPost = async () => {
            if (!id) return;
            try {
                const data = await getPost(Number(id));
                setPost(data);
            } catch (error) {
                console.error("게시글 로드 실패:", error);
                alert("존재하지 않는 게시글입니다.");
                navigate(-1);
            }
        };
        fetchPost();
    }, [id, navigate]);

    // 2. 리스트 로드 (필터링 반영)
    const fetchPosts = async (pageNumber: number = 0) => {
        try {
            const data: CommunityPageResponse | undefined = await getCommunityPosts(
                pageNumber,
                selectedCategory,
                selectedRegion,
                searchType,
                keyword || null
            );
            const postsWithAuthor = (data?.content || []).map(p => ({
                ...p,
                authorNickname: p.authorNickname || "익명"
            }));
            setPosts(postsWithAuthor);
            setTotalPages(data?.totalPages || 0);
        } catch (error) {
            console.error("리스트 로드 실패:", error);
            setPosts([]);
        }
    };

    // 필터나 페이지 변경 시 리스트 다시 불러오기
    useEffect(() => {
        fetchPosts(page);
    }, [page, selectedCategory, selectedRegion]); // keyword는 엔터 시 호출

    const handleSearch = () => {
        setPage(0);
        fetchPosts(0);
    };

    const handleReset = () => {
        setSelectedCategory("전체보기");
        setSelectedRegion(null);
        setKeyword("");
        setPage(0);
    };

    const handleView = async (postId: number) => {
        try {
            await client.patch(`/community/posts/${postId}/view`);
        } catch (error) {
            console.error("조회수 증가 실패:", error);
        }
    };

    const handleShare = async () => {
        if (!post) return;

        const url = window.location.href;

        try {
            // 1. URL 복사
            await navigator.clipboard.writeText(url);

            // 2. 공유 수 증가 API 호출
            await client.patch(`/community/posts/${post.id}/recommend`);

            // 3. UI 즉시 반영 (상세글 상태 업데이트)
            setPost(prev => prev ? {
                ...prev,
                recommendCount: prev.recommendCount + 1
            } : prev);

            // 4. 🔥 하단 리스트에서도 해당 글의 공유수 즉시 업데이트
            setPosts(prevPosts => 
                prevPosts.map(item => 
                    Number(item.id) === Number(post.id) 
                    ? { ...item, recommendCount: item.recommendCount + 1 } 
                    : item
                )
            );

            alert("링크가 복사되었습니다!");
        } catch (error) {
            console.error("공유 실패:", error);
            alert("공유 실패");
        }
    };

    const goToPage = (p: number) => {
        if (p < 0 || p >= totalPages) return;
        setPage(p);
        document.getElementById("list-section")?.scrollIntoView({ behavior: "smooth" });
    };

    const pageNumbers = useMemo(() => {
        const range = 5;
        const start = Math.max(0, page - range);
        const end = Math.min(totalPages - 1, page + range);
        const pages = [];
        for (let i = start; i <= end; i++) pages.push(i);
        return pages;
    }, [page, totalPages]);

    return (
        <>
            <Header />
            <div className="community-page">
                
                <div className="community-container">
                    {/* 🔹 사이드바 추가 */}
                    <aside className="community-sidebar">
                        <div className="sidebar-section">
                            <h3>카테고리</h3>
                            <ul>
                                {categories.map((cat) => (
                                    <li
                                        key={cat}
                                        className={selectedCategory === cat ? "active" : ""}
                                        onClick={() => { setSelectedCategory(cat); setPage(0); }}
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
                                        onClick={() => { setSelectedRegion(reg); setPage(0); }}
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
                        {/* 게시글 상세 영역 */}
                        <article className="community-post-form">
                            <div className="form-row">
                                <div className="form-group">
                                    <label>분류</label>
                                    <div>{post?.category}</div>
                                </div>
                                <div className="form-group">
                                    <label>지역</label>
                                    <div>{post?.region}</div>
                                </div>
                                {RATING_ENABLED_CATEGORIES.includes(String(post?.category)) && (
                                    <div className="rating-display" style={{ marginLeft: "auto", display: "flex", alignItems: "center", gap: "5px" }}>
                                        <span style={{ fontSize: "13px", color: "#666" }}>⭐ 평점</span>
                                        <span style={{ color: "#FFBB00" }}>{"★".repeat(post?.rating || 0)}{"☆".repeat(5 - (post?.rating || 0))}</span>
                                    </div>
                                )}
                            </div>

                            {PLAN_SHARE_ENABLED_CATEGORIES.includes(String(post?.category)) && (
                                <div className="form-row route-inputs">
                                    <div className="form-group">
                                        <label>출발지</label>
                                        <div>{post?.departure}</div>
                                    </div>
                                    <div className="route-arrow">➔</div>
                                    <div className="form-group">
                                        <label>도착지</label>
                                        <div>{post?.arrival}</div>
                                    </div>
                                </div>
                            )}

                            <div className="form-main-area">
                                <div className="PostHeader">
                                    <h1 className="PostTitle">{post?.title}</h1>
                                    <div className="PostMeta">
                                        <span>작성자: <strong>{post?.authorNickname}</strong></span>
                                        <span> | {post?.createdAt?.slice(0, 10)}</span>
                                    </div>
                                </div>

                                <div className="PostContent">
                                    <div className="mainContent ql-editor" dangerouslySetInnerHTML={{ __html: post?.content || "" }} />
                                    {post?.tags && (
                                        <div className="tags">
                                            {post.tags.split(",").map((tag, idx) => (
                                                <span key={idx} className="tag">#{tag.trim().replace('#', '')}</span>
                                            ))}
                                        </div>
                                    )}
                                </div>

                                <div className="PostFooter">
                                    <div><RemoveRedEyeIcon /> 조회수: {post?.viewCount}</div>
                                    <div><button onClick={handleShare}><ShareIcon /></button> 공유하기: {post?.recommendCount}</div>
                                </div>
                            </div>
                        </article>

                        <div id="list-section" style={{ marginTop: "40px" }}>
                            {/* 검색바 */}
                            <div className="community-search-bar" style={{ marginBottom: "15px" }}>
                                <select value={searchType} onChange={(e) => setSearchType(e.target.value as any)}>
                                    <option value="title">제목</option>
                                    <option value="author">작성자</option>
                                </select>
                                <div className="search-input-box">
                                    <input
                                        type="text"
                                        placeholder="결과 내 검색"
                                        value={keyword}
                                        onChange={(e) => setKeyword(e.target.value)}
                                        onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                                    />
                                    <button onClick={handleSearch}><SearchIcon fontSize="small" /></button>
                                </div>
                            </div>

                            {/* 리스트 테이블 */}
                            <div className="community-board-container">
                                <div className="board-header-row">
                                    <div className="col-id">번호</div>
                                    <div className="col-route">기타</div>
                                    <div className="col-title">제목</div>
                                    <div className="col-author">작성자</div>
                                    <div className="col-date">날짜</div>
                                    <div className="col-views">조회</div>
                                    <div className="col-stats">공유</div>
                                </div>

                                <div className="board-body">
                                    {posts.map(item => (
                                        <div
                                            key={item.id}
                                            className={`board-item-row ${id === String(item.id) ? "active-row" : ""}`}
                                            onClick={async () => {
                                                await handleView(Number(item.id));
                                                navigate(`/community/${item.id}`);
                                                window.scrollTo(0, 0);
                                            }}
                                        >
                                            <div className="col-id">{item.id}</div>
                                            <div className="col-route">
                                                {item.departure || ""} {item.arrival ? `➔ ${item.arrival}` : ""}
                                            </div>
                                            <div className="col-title" style={{ fontWeight: id === String(item.id) ? "bold" : "normal" }}>
                                                {item.title} {id === String(item.id) && " (현재글)"}
                                            </div>
                                            <div className="col-author">{item.authorNickname}</div>
                                            <div className="col-date">{item.createdAt?.split("T")[0]}</div>
                                            <div className="col-views">{item.viewCount}</div>
                                            <div className="col-stats">
                                                <ShareIcon fontSize="inherit" /> {item.recommendCount} {/* ✅ item.recommendCount로 수정 */}
                                            </div>
                                        </div>
                                    ))}
                                </div>

                                {/* 페이지네이션 */}
                                <div className="pagination">
                                    <button onClick={() => goToPage(0)} disabled={page === 0}>{"<<"}</button>
                                    {pageNumbers.map(p => (
                                        <button
                                            key={p}
                                            onClick={() => goToPage(p)}
                                            className={page === p ? "active-page" : ""}
                                        >
                                            {p + 1}
                                        </button>
                                    ))}
                                    <button onClick={() => goToPage(totalPages - 1)} disabled={page === totalPages - 1}>{">>"}</button>
                                </div>
                            </div>
                        </div>

                        <div className="community-footer">
                            <button className="write-button" onClick={() => navigate("/community/write")}>글쓰기</button>
                        </div>
                    </main>
                </div>
            </div>
        </>
    );
}