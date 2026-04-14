import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "../components/layout/Header.tsx";
import client from "../components/api/client.ts";
import { CommunityResponse, CommunityPageResponse } from "../types/community.ts";

// 아이콘
import ShareIcon from '@mui/icons-material/Share';
import RemoveRedEyeIcon from '@mui/icons-material/RemoveRedEye';
import ThumbUpOffAltIcon from '@mui/icons-material/ThumbUpOffAlt';
import RestartAltIcon from "@mui/icons-material/RestartAlt";
import ArrowRightAltIcon from '@mui/icons-material/ArrowRightAlt';
import StarIcon from '@mui/icons-material/Star';

import "./CommunityReadPage.css";
import { getCommunityPosts } from "./CommunityPage.tsx";

// =========================
// API
// =========================
export const getPost = async (id: number) => {
    const res = await client.get(`/community/posts/${id}`);
    return res.data;
};

export const getMe = async () => {
    const res = await client.get("/auth/me");
    return res.data;
};

// =========================
// 카테고리 설정
// =========================
const RATING_ENABLED_CATEGORIES = ["맛집게시판", "사진게시판", "후기게시판"];
const PLAN_SHARE_ENABLED_CATEGORIES = ["여행플랜 공유", "당일치기 친구 찾기"];

export default function CommunityReadPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    // =========================
    // 상태
    // =========================
    const [post, setPost] = useState<CommunityResponse | null>(null);
    const [posts, setPosts] = useState<CommunityResponse[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [liked, setLiked] = useState(false);
    const [me, setMe] = useState<{ id: number } | null>(null);

    const [selectedCategory, setSelectedCategory] = useState("전체보기");
    const [selectedRegion, setSelectedRegion] = useState<string | null>("전체");

    const categories = [
        "전체보기",
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

    const renderRouteOrRating = (post: CommunityResponse) => {
        // 1. 별점 렌더링 (맛집, 후기 등) 부분 수정
        if (post.category && RATING_ENABLED_CATEGORIES.includes(post.category)) {
          const rating = post.rating || 0;
          return (
            <div className="rating-stars" style={{ color: "#FFBB00", fontSize: "16px" }}>
              {Array.from({ length: 5 }).map((_, i) => (
                <span key={i}>
                  {i < rating ? "★" : "☆"}
                </span>
              ))}
            </div>
          );
        }

        // 2. 경로 렌더링 (여행플랜 등)
        if (post.category && PLAN_SHARE_ENABLED_CATEGORIES.includes(post.category)) {
        // 출발/도착이 모두 없으면 하이픈 반환
        if (!post.departure && !post.arrival) return " - ";
        
        return (
            <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "4px" }}>
            <span>{post.departure || "미정"}</span>
            <ArrowRightAltIcon fontSize="small" />
            <span>{post.arrival || "미정"}</span>
            </div>
        );
        }

        // 3. 둘 다 해당 안 될 경우
        return " - ";
    };

    // =========================
    // 로그인 유저 가져오기
    // =========================
    useEffect(() => {
        const fetchMe = async () => {
            try {
                const data = await getMe();
                setMe(data);
            } catch (err) {
                console.error("유저 정보 불러오기 실패", err);
            }
        };

        fetchMe();
    }, []);

    // =========================
    // 작성자 여부
    // =========================
    const isAuthor = post?.authorId === me?.id;

    // =========================
    // 게시글 리스트
    // =========================
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

    useEffect(() => {
        fetchPosts(page);
    }, [page, selectedCategory, selectedRegion]);

    // =========================
    // 상세 게시글 로드
    // =========================
    useEffect(() => {
        const fetchPostDetail = async () => {
            if (!id) return;

            try {
                await client.patch(`/community/posts/${id}/view`);

                const data = await getPost(Number(id));

                setPost(data);
                setLiked(data.likedByMe);

                setPosts(prev =>
                    prev.map(p =>
                        p.id === Number(id)
                            ? { ...p, viewCount: data.viewCount }
                            : p
                    )
                );
            } catch (error) {
                console.error("데이터 로드 실패:", error);
            }
        };

        fetchPostDetail();
    }, [id]);

    // =========================
    // 수정 / 삭제
    // =========================
    const handleUpdate = () => {
        navigate(`/community/write/${id}`);
    };

    const handleDelete = async () => {
        if (!id) return;
        if (!window.confirm("정말 삭제하시겠습니까?")) return;

        try {
            await client.delete(`/community/posts/${id}`);
            alert("삭제되었습니다.");
            navigate("/community");
        } catch (err) {
            alert("삭제 실패");
        }
    };

    // =========================
    // 좋아요
    // =========================
    const handleLike = async () => {
        if (!post || !id) return;

        try {
            const res = await client.post(`/community/posts/${id}/like`);
            const { liked: isLiked, likeCount } = res.data;

            setLiked(isLiked);

            setPost(prev =>
                prev ? { ...prev, likeCount } : null
            );

            setPosts(prev =>
                prev.map(p =>
                    p.id === Number(id)
                        ? { ...p, likeCount }
                        : p
                )
            );
        } catch (err) {
            console.error("좋아요 실패:", err);
        }
    };

    // =========================
    // 공유
    // =========================
    const handleShare = async () => {
        if (!post) return;

        try {
            await navigator.clipboard.writeText(window.location.href);
            const res = await client.patch(`/community/posts/${post.id}/share`);
            const newShareCount = res.data.shareCount ?? (post.shareCount || 0) + 1

            alert("링크가 복사되었습니다!");

            setPost(prev => prev ? { ...prev, shareCount: newShareCount } : null);

            // 4. 하단 리스트(posts)에서 해당 게시글의 공유수 연동 업데이트
            setPosts(prev =>
                prev.map(p =>
                    p.id === post.id
                        ? { ...p, shareCount: newShareCount }
                        : p
                )
            );
        } catch (error) {
            alert("공유 실패");
        }
    };

    // =========================
    // 필터 초기화
    // =========================
    const handleReset = () => {
        setSelectedCategory("전체보기");
        setSelectedRegion("전체");
        setPage(0);
    };

    const goToPage = (p: number) => {
        if (p < 0 || p >= totalPages) return;
        setPage(p);
        document.getElementById("list-section")?.scrollIntoView({ behavior: "smooth" });
    };

    const pageNumbers = Array.from(
        { length: Math.min(11, totalPages) },
        (_, i) => i
    );

    // =========================
    // UI
    // =========================
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
                                        {post?.likeCount}
                                    </div>
                                    <div className="footer-item">
                                        <button onClick={handleShare} className="icon-btn"><ShareIcon /></button> 공유
                                    </div>
                                </div>
                            </div>
                        </article>

                        <div className="community-footer">
                                {/* 왼쪽 영역 */}
                                <div className="footer-left">
                                    <button className="to-list-button" onClick={() => navigate("/community")}>
                                        목록으로
                                    </button>
                                </div>

                                {/* 오른쪽 영역 */}
                                <div className="footer-right">
                                    {/* ✅ 작성자만 보이게 */}
                                    {isAuthor && (
                                        <>
                                            <button className="edit-button" onClick={handleUpdate}>
                                                수정하기
                                            </button>
                                            <button className="delete-button" onClick={handleDelete}>
                                                삭제하기
                                            </button>
                                        </>
                                    )}
                                    <button className="write-button" onClick={() => navigate("/community/write")}>
                                        글쓰기
                                    </button>
                                </div>
                            </div>
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
                                        <div className="col-route">{renderRouteOrRating(item)}</div>
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