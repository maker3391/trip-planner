import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "../components/layout/Header.tsx";
import client from "../components/api/client.ts";
import { CommunityResponse, CommunityPageResponse } from "../types/community.ts";

import ShareIcon from '@mui/icons-material/Share';
import RemoveRedEyeIcon from '@mui/icons-material/RemoveRedEye';

import "./CommunityReadPage.css";
import { getCommunityPosts } from "./CommunityPage.tsx";

// 단일 게시글 가져오기 API
export const getPost = async (id: number) => {
    const res = await client.get(`/community/posts/${id}`);
    return res.data;
};

// 평점 표시 카테고리
const RATING_ENABLED_CATEGORIES = ["맛집게시판", "사진게시판", "후기게시판"];
// 출발/도착지 표시 카테고리
const PLAN_SHARE_ENABLED_CATEGORIES = ["여행플랜 공유", "당일치기 친구 찾기"];

export default function CommunityReadPage() {
    const { id } = useParams();           // URL 파라미터에서 글 id 추출
    const navigate = useNavigate();       // 페이지 이동용

    // 🔹 단일 글 상태
    const [post, setPost] = useState<CommunityResponse>();
    // 🔹 리스트 상태 + 페이지 관리
    const [posts, setPosts] = useState<CommunityResponse[]>([]);
    const [page, setPage] = useState(0);          // 현재 페이지 (0부터)
    const [totalPages, setTotalPages] = useState(0); // 총 페이지 수

    // =========================
    // 🔹 단일 글 가져오기
    // =========================
    useEffect(() => {
        const fetchPost = async () => {
            if (!id) return;
            try {
                const data = await getPost(Number(id));
                setPost(data);
            } catch (error) {
                console.error("게시글을 불러오는데 실패했습니다.", error);
                alert("존재하지 않거나 삭제된 게시글입니다.");
                navigate(-1); // 에러 시 이전 페이지
            }
        };
        fetchPost();
    }, [id, navigate]);

    // =========================
    // 🔹 게시글 리스트 가져오기 + 페이지 관리
    // =========================
    const fetchPosts = async (pageNumber: number = 0) => {
        try {
            const data: CommunityPageResponse | undefined = await getCommunityPosts(pageNumber);
            const postsWithAuthor = (data?.content || []).map(post => ({
                ...post,
                authorNickname: post.authorNickname || "익명"
            }));
            setPosts(postsWithAuthor);
            setTotalPages(data?.totalPages || 0);
        } catch (error) {
            console.error("게시글 리스트 불러오기 실패:", error);
            setPosts([]);
        }
    };

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

    // 페이지 변경 함수
    const goToPage = (p: number) => {
        if (p < 0 || p >= totalPages) return;
        setPage(p);
        fetchPosts(p); // 페이지가 바뀌면 해당 페이지 데이터 호출
    };

    // 페이지 번호 계산 함수 (현 페이지 기준 +-5 범위)
    const getPageNumbers = () => {
        const range = 5;
        const start = Math.max(0, page - range);
        const end = Math.min(totalPages - 1, page + range);
        const pages = [];
        for (let i = start; i <= end; i++) pages.push(i);
        return pages;
    };

    // 🔹 공유 버튼 핸들러
    // 🔥 공유 버튼 핸들러 (좋아요 API 재사용)
    const handleShare = async () => {
        if (!post) return;

        const url = window.location.href;

        try {
            // 🔹 1. URL 복사
            await navigator.clipboard.writeText(url);

            // 🔹 2. 공유 수 증가 (좋아요 API 재사용)
            await client.patch(`/community/posts/${post.id}/recommend`);

            // 🔹 3. UI 즉시 반영 (UX 중요)
            setPost(prev => prev ? {
                ...prev,
                recommendCount: prev.recommendCount + 1
            } : prev);

            alert("링크 복사 완료!");
        } catch (error) {
            console.error("공유 실패:", error);
            alert("공유 실패");
        }
    };

    // 컴포넌트 첫 렌더 시 0페이지 게시글 가져오기
    useEffect(() => {
        fetchPosts(0);
    }, []);

    return (
        <div className="community-page">
            <Header />
            <div className="community-container">
                <main className="community-main-content">

                    {/* ========================= */}
                    {/* 🔹 읽는 글 영역 */}
                    {/* ========================= */}
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

                        {RATING_ENABLED_CATEGORIES.includes(String(post?.category)) && (
                            <div className="toolbar-item rating-section" style={{ display: "flex", alignItems: "center", gap: "8px", marginLeft: "10px", borderLeft: "1px solid #eee", paddingLeft: "15px" }}>
                                <span style={{ fontSize: "13px", fontWeight: "600", color: "#666" }}>⭐ 평점</span>
                                <div className="stars" style={{ display: "flex" }}>
                                    {[1,2,3,4,5].map(num => (
                                        <span key={num} style={{ fontSize: "20px", color: num <= Number(post?.rating) ? "#FFBB00" : "#e0e0e0" }}>
                                            {num <= Number(post?.rating) ? '★' : '☆'}
                                        </span>
                                    ))}
                                </div>
                            </div>
                        )}

                        <div className="form-main-area">
                            <div className="PostHeader">
                                <h1 className="PostTitle">{post?.title}</h1>
                                <div className="PostMeta">
                                    <span className="PostAuthor">작성자: <strong>{post?.authorNickname}</strong></span>
                                    <span className="PostDate">| {post?.createdAt?.slice(0,10)}</span>
                                </div>
                            </div>

                            <div className="PostContent">
                                <div className="mainContent ql-editor" dangerouslySetInnerHTML={{ __html: post?.content || "" }} />
                                {post?.tags && (
                                    <div className="tags">
                                        {post.tags.split(",").map((tag, idx) => (
                                            <span key={idx} className="tag">{tag.trim().startsWith('#') ? tag.trim() : `#${tag.trim()}`}</span>
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

                    <br />

                    {/* ========================= */}
                    {/* 🔹 게시글 리스트 영역 (읽는 글 아래) */}
                    {/* ========================= */}
                    <div className="community-board-container">
                        {/* 리스트 헤더 */}
                        <div className="board-header-row">
                            <div className="col-id">번호</div>
                            <div className="col-route">기타</div>
                            <div className="col-title">제목</div>
                            <div className="col-author">작성자</div>
                            <div className="col-date">날짜</div>
                            <div className="col-views">조회</div>
                            <div className="col-stats">추천</div>
                        </div>

                        {/* 게시글 행 */}
                        <div className="board-body">
                            {posts.length === 0 ? (
                                <div className="no-posts">게시판에 글이 없습니다!</div>
                            ) : (
                                posts.map(item => (
                                    <div
                                        key={item.id}
                                        className={`board-item-row ${id === item.id ? "active-row" : ""}`}
                                        // 🔥 클릭 시 조회수 증가 + 페이지 이동
                                        onClick={async () => {
                                        await handleView(Number(item.id)); // ✅ 조회수 증가 완료 후
                                        navigate(`/community/${item.id}`); // ✅ 상세 이동
                                        }}
                                    >
                                        <div className="col-id">{item.id}</div>
                                        <div className="col-route">{item.departure ? item.departure : ""} - {item.arrival ? `> ${item.arrival}` : ""}</div>
                                        <div className="col-title">{item.title}</div>
                                        <div className="col-author">{item.authorNickname}</div>
                                        <div className="col-date">{item.createdAt?.split("T")[0]}</div>
                                        <div className="col-views">{item.viewCount}</div>
                                        <div className="col-stats">
                                            <ShareIcon fontSize="inherit" />{" "}
                                            {post?.recommendCount}
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>

                        {/* ========================= */}
                        {/* 🔹 페이지네이션 */}
                        {/* ========================= */}
                        <div className="pagination">
                            {/* 맨 처음 페이지 */}
                            <button onClick={() => goToPage(0)} disabled={page === 0}>{"<<"}</button>
                            {/* 이전 페이지 */}
                            <button onClick={() => goToPage(page - 1)} disabled={page === 0}>{"<"}</button>

                            {/* 페이지 번호 리스트 */}
                            {getPageNumbers().map(p => (
                                <button
                                    key={p}
                                    onClick={() => goToPage(p)}
                                    className={page === p ? "active-page" : ""}
                                >
                                    {p + 1}
                                </button>
                            ))}

                            {/* 다음 페이지 */}
                            <button onClick={() => goToPage(page + 1)} disabled={page === totalPages - 1}>{">"}</button>
                            {/* 마지막 페이지 */}
                            <button onClick={() => goToPage(totalPages - 1)} disabled={page === totalPages - 1}>{">>"}</button>
                        </div>
                    </div>

                    {/* 글쓰기 버튼 */}
                    <div className="community-footer">
                        <button className="write-button" onClick={() => navigate("/community/write")}>글쓰기</button>
                    </div>

                </main>
            </div>
        </div>
    );
}