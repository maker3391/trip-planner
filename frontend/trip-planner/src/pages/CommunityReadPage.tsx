import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "../components/layout/Header.tsx";
import client from "../components/api/client.ts";
import { CommunityResponse } from "../types/community.ts";

import ThumbUpIcon from '@mui/icons-material/ThumbUp';
import RemoveRedEyeIcon from '@mui/icons-material/RemoveRedEye';

// 💡 CSS 파일 import (ReactQuill css는 제거했습니다)
import "./CommunityReadPage.css";

export const getPost = async (id: number) => {
    const res = await client.get(`/community/posts/${id}`);
    return res.data;
};

const RATING_ENABLED_CATEGORIES = ["맛집게시판", "사진게시판", "후기게시판"]; 
const PLAN_SHARE_ENABLED_CATEGORIES = ["여행플랜 공유", "당일치기 친구 찾기"];

export default function CommunityReadPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    
    // 💡 읽기 전용 화면이므로 useRef<ReactQuill>은 제거했습니다.

    const [post, setPost] = useState<CommunityResponse>();

    useEffect(() => {
        const fetchPost = async () => {
            if (!id) return;
            
            try {
                const data = await getPost(Number(id));
                setPost(data);
            } catch (error) {
                console.error("게시글을 불러오는데 실패했습니다.", error);
                alert("존재하지 않거나 삭제된 게시글입니다.");
                navigate(-1); // 에러 발생 시 이전 페이지로 돌려보냄
            }
        };

        fetchPost();
    }, [id, navigate]);
    

    return (
        <div className="community-page">
            <Header />
            <div className="community-container">
                <main className="community-main-content">
                    {/* 💡 form 대신 글을 담는 시맨틱 태그인 article 사용 */}
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
                                    {[1, 2, 3, 4, 5].map(num => (
                                        <span 
                                            key={num}
                                            /* 💡 클릭할 필요가 없으므로 cursor: "pointer" 제거 */
                                            style={{ fontSize: "20px", color: num <= Number(post?.rating) ? "#FFBB00" : "#e0e0e0" }}
                                        >
                                            {num <= Number(post?.rating) ? '★' : '☆'}
                                        </span>
                                    ))}
                                </div>
                            </div>
                        )}
                        
                        <div className="form-main-area">
                            {/* 헤더 영역: 제목 + 작성자/날짜 등 메타 정보 */}
                            <div className="PostHeader">
                                <h1 className="PostTitle">{post?.title}</h1>
                                <div className="PostMeta">
                                    <span className="PostAuthor">작성자: <strong>{post?.authorNickname}</strong></span>
                                    <span className="PostDate">| {post?.createdAt?.slice(0, 10)}</span>
                                </div>
                            </div>

                            <div className="PostContent">
                                <div 
                                    className="mainContent"
                                    dangerouslySetInnerHTML={{ __html: post?.content || "" }}
                                />
                                
                                {post?.tags && (
                                    <div className="tags">
                                        {post.tags.split(",").map((tag, idx) => (
                                            <span key={idx} className="tag">
                                                {tag.trim().startsWith('#') ? tag.trim() : `#${tag.trim()}`}
                                            </span>
                                        ))}
                                    </div>
                                )}
                            </div>
                            
                            {/* 푸터에는 조회수와 좋아요만 남겨서 깔끔하게 유지 */}
                            <div className="PostFooter">
                                <div><RemoveRedEyeIcon /> 조회수: {post?.viewCount}</div>
                                <div><button><ThumbUpIcon /></button>좋아요: {post?.recommendCount}</div>
                            </div>
                        </div>

                        {/* 💡 읽기 화면에 어울리는 하단 네비게이션 버튼 추가 */}
                        <div className="community-footer" style={{ display: "flex", justifyContent: "flex-end", marginTop: "20px" }}>
                            <button className="cancel-button" onClick={() => navigate(-1)}>
                                목록으로
                            </button>
                        </div>
                    </article>
                </main>
            </div>
        </div>
    );
}