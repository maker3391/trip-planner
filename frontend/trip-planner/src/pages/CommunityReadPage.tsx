import { useState, useEffect, useMemo } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "../components/layout/Header.tsx";
import client from "../components/api/client.ts";
import { CommunityResponse, CommunityPageResponse } from "../types/community.ts";

// 아이콘 추가
import ShareIcon from '@mui/icons-material/Share';
import RemoveRedEyeIcon from '@mui/icons-material/RemoveRedEye';
import ThumbUpOffAltIcon from '@mui/icons-material/ThumbUpOffAlt';
import RestartAltIcon from "@mui/icons-material/RestartAlt";
import ArrowRightAltIcon from '@mui/icons-material/ArrowRightAlt';
import StarIcon from '@mui/icons-material/Star';

import "./CommunityReadPage.css";
import { getCommunityPosts } from "./CommunityPage.tsx";

export const getPost = async (id: number) => {
    const res = await client.get(`/community/posts/${id}`);
    return res.data;
};

const RATING_ENABLED_CATEGORIES = ["맛집게시판", "사진게시판", "후기게시판"];
const PLAN_SHARE_ENABLED_CATEGORIES = ["여행플랜 공유", "당일치기 친구 찾기"];

export default function CommunityReadPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    // --- 1. 상태 관리 ---
    const [post, setPost] = useState<CommunityResponse | null>(null);
    const [posts, setPosts] = useState<CommunityResponse[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [liked, setLiked] = useState(false);

    // 필터 상태
    const [selectedCategory, setSelectedCategory] = useState("전체보기");
    const [selectedRegion, setSelectedRegion] = useState<string | null>(null);

    const categories = ["전체보기", "여행플랜 공유", "자유게시판", "질문게시판", "맛집게시판", "후기게시판", "공지게시판"];
    const regions = ["서울", "경기", "인천", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"];

    // --- 2. 하단 리스트 로드 함수 (위치 이동) ---
    // 리스트를 먼저 정의해야 fetchPostDetail에서 이 상태를 참조하거나 수정하기 용이해.
    const fetchPosts = async (pageNumber: number = 0) => {
        try {
            const data: CommunityPageResponse | undefined = await getCommunityPosts(
                pageNumber,
                selectedCategory,
                selectedRegion
            );
            setPosts(data?.content || []);
            setTotalPages(data?.totalPages || 0);
        } catch (error) {
            console.error("리스트 로드 실패:", error);
        }
    };

    // 필터나 페이지 변경 시 리스트 호출
    useEffect(() => {
        fetchPosts(page);
    }, [page, selectedCategory, selectedRegion]);


    // --- 3. 상세 글 로드 및 "상태 동기화" 조회수 증가 ---
    useEffect(() => {
        const fetchPostDetail = async () => {
            if (!id) return;

            try {
                // 1. 상세 데이터 가져오기
                const data = await getPost(Number(id));
                setPost(data);
                setLiked(data.likedByMe);

                // 2. 서버 조회수 증가 API 호출
                await client.patch(`/community/posts/${id}/view`);
                
                // 3. [핵심] 상세 페이지 상태 업데이트 (+1)
                setPost(prev => prev ? { ...prev, viewCount: prev.viewCount + 1 } : null);

                // 4. [핵심] 하단 리스트(posts) 상태에서도 해당 글 조회수 업데이트 (+1)
                setPosts(prevPosts => 
                    prevPosts.map(p => 
                        p.id === Number(id) 
                        ? { ...p, viewCount: p.viewCount + 1 } 
                        : p
                    )
                );

            } catch (error) {
                console.error("데이터 로드 실패:", error);
                alert("존재하지 않는 게시글입니다.");
                navigate("/community");
            }
        };

        fetchPostDetail();
    }, [id]); // id가 바뀔 때마다 실행


    // --- 4. 핸들러 함수들 ---
    const handleLike = async () => {
        if (!post || !id) return;
        try {
            const res = await client.post(`/community/posts/${id}/like`);
            const { liked: isLiked, likeCount } = res.data;
            
            setLiked(isLiked);
            // 상세 뷰 좋아요 수 동기화
            setPost(prev => prev ? { ...prev, likeCount } : null);
            // 하단 리스트 좋아요 수 동기화
            setPosts(prevPosts => 
                prevPosts.map(p => p.id === Number(id) ? { ...p, likeCount } : p)
            );
        } catch (err) {
            console.error("좋아요 실패:", err);
        }
    };

    const handleShare = async () => {
        if (!post) return;
        try {
            await navigator.clipboard.writeText(window.location.href);
            await client.patch(`/community/posts/${post.id}/share`);
            alert("링크가 복사되었습니다!");
            // 공유수도 실시간 반영하고 싶다면 동일한 로직 추가 가능
        } catch (error) {
            alert("공유 실패");
        }
    };

    const handleReset = () => {
        setSelectedCategory("전체보기");
        setSelectedRegion(null);
        setPage(0);
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
                    {/* 사이드바 */}
                    <aside className="community-sidebar">
                        <div className="sidebar-section">
                            <h3>카테고리</h3>
                            <ul>
                                {categories.map((cat) => (
                                    <li key={cat} className={selectedCategory === cat ? "active" : ""}
                                        onClick={() => { setSelectedCategory(cat); setPage(0); }}>
                                        {cat}
                                    </li>
                                ))}
                            </ul>
                        </div>
                        <div className="sidebar-section">
                            <h3>지역별</h3>
                            <div className="region-grid">
                                {regions.map((reg) => (
                                    <span key={reg} className={selectedRegion === reg ? "active" : ""}
                                          onClick={() => { setSelectedRegion(reg); setPage(0); }}>
                                        {reg}
                                    </span>
                                ))}
                            </div>
                        </div>
                        <button className="filter-reset-btn" onClick={handleReset}>
                            <RestartAltIcon fontSize="inherit" /> 필터 초기화
                        </button>
                    </aside>

                    <main className="community-main-content">
                        {/* 상세 글 */}
                        <article className="community-post-form">
                            <div className="form-row">
                                <div className="form-group"><label>분류</label><div>{post?.category}</div></div>
                                <div className="form-group"><label>지역</label><div>{post?.region}</div></div>
                            </div>

                            <div className="post-extra-info">
                                {post?.category && PLAN_SHARE_ENABLED_CATEGORIES.includes(post.category) && (
                                    <div className="route-display">
                                        <strong>경로:</strong> {post.departure} <ArrowRightAltIcon /> {post.arrival}
                                    </div>
                                )}
                                {post?.category && RATING_ENABLED_CATEGORIES.includes(post.category) && (
                                    <div className="rating-display">
                                        <strong>평점:</strong>
                                        <span className="stars">
                                            {Array.from({ length: 5 }).map((_, i) => (
                                                <StarIcon key={i} style={{ color: i < (post.rating || 0) ? "#FFBB00" : "#e0e0e0" }} />
                                            ))}
                                        </span>
                                    </div>
                                )}
                            </div>

                            <div className="form-main-area">
                                <div className="PostHeader">
                                    <h1 className="PostTitle">{post?.title}</h1>
                                    <div className="PostMeta">
                                        <span>작성자: <strong>{post?.authorNickname}</strong></span>
                                        <span>| {post?.createdAt?.slice(0, 10)}</span>
                                    </div>
                                </div>
                                <div className="PostContent">
                                    <div className="ql-editor" dangerouslySetInnerHTML={{ __html: post?.content || "" }} />
                                    {post?.tags && (
                                        <div className="tags">
                                            {post.tags.split(",").map((tag, idx) => (
                                                <span key={idx} className="tag">#{tag.trim().replace('#', '')}</span>
                                            ))}
                                        </div>
                                    )}
                                </div>
                                <div className="PostFooter">
                                    <div className="footer-item"><RemoveRedEyeIcon /> {post?.viewCount}</div>
                                    <div className="footer-item">
                                        <button onClick={handleLike} className="icon-btn">
                                            <ThumbUpOffAltIcon style={{ color: liked ? "#1976d2" : "#aaa" }} />
                                        </button>
                                    </div>
                                    <div className="footer-item">
                                        <button onClick={handleShare} className="icon-btn"><ShareIcon /></button> 공유
                                    </div>
                                </div>
                            </div>
                        </article>

                        <hr />

                        {/* 하단 리스트 */}
                        <div id="list-section" className="community-board-container">
                            <div className="board-header-row">
                                <div className="col-id">번호</div>
                                <div className="col-route">기타</div>
                                <div className="col-title">제목</div>
                                <div className="col-author">작성자</div>
                                <div className="col-views">조회</div>
                                <div className="col-stats">좋아요</div>
                                <div className="col-share">공유</div>
                            </div>

                            <div className="board-body">
                                {posts.map(item => (
                                    <div key={item.id} 
                                         className={`board-item-row ${id === String(item.id) ? "active-row" : ""}`}
                                         onClick={() => navigate(`/community/${item.id}`)}>
                                        <div className="col-id">{item.id}</div>
                                        <div className="col-route">{item.departure || ""} - {item.arrival || ""}</div>
                                        <div className="col-title">{item.title}</div>
                                        <div className="col-author">{item.authorNickname || "익명"}</div>
                                        <div className="col-views">{item.viewCount}</div>
                                        <div className="col-stats">{item.likeCount}</div>
                                        <div className="col-share">{item.shareCount || 0}</div>
                                    </div>
                                ))}
                            </div>

                            <div className="pagination">
                                <button onClick={() => goToPage(0)} disabled={page === 0}>{"<<"}</button>
                                <button onClick={() => goToPage(page - 1)} disabled={page === 0}>{"<"}</button>
                                {pageNumbers.map(p => (
                                    <button key={p} onClick={() => goToPage(p)} className={page === p ? "active-page" : ""}>
                                        {p + 1}
                                    </button>
                                ))}
                                <button onClick={() => goToPage(page + 1)} disabled={page === totalPages - 1}>{">"}</button>
                                <button onClick={() => goToPage(totalPages - 1)} disabled={page === totalPages - 1}>{">>"}</button>
                            </div>
                        </div>
                    </main>
                </div>
            </div>
        </>
    );
}