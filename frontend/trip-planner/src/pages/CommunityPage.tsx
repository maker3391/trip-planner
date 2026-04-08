import { useEffect, useState } from "react";
import Header from "../components/layout/Header.tsx";
import SearchIcon from '@mui/icons-material/Search';
import ThumbUpOffAltIcon from '@mui/icons-material/ThumbUpOffAlt';
import RestartAltIcon from '@mui/icons-material/RestartAlt';
import "./CommunityPage.css";
import { useNavigate } from "react-router-dom";
import client from "../components/api/client.ts";
import { CommunityResponse, CommunityPageResponse } from "../types/community.ts"; // 🔹 타입 import

export const getCommunityPosts = async (page = 0) => {
  const response = await client.get(`/community/posts?page=${page}`);
  return response.data;
};

export default function CommunityPage() {
    

    const [selectedMenu, setSelectedMenu] = useState("전체보기");
    const [posts, setPosts] = useState<CommunityResponse[]>([]); // 🔹 타입 지정

    const categories = ["전체보기", "여행플랜 공유", "당일치기 친구 찾기", "자유게시판", "질문게시판", "사진게시판", "맛집게시판", "후기게시판"];
    const regions = ["서울", "경기", "인천", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"];

    const navigate = useNavigate();

    // 필터 초기화
    const handleReset = () => {
        setSelectedMenu("전체보기");
    };

    useEffect(() => {
        const fetchPosts = async () => {
            try {
                const data: CommunityPageResponse | undefined = await getCommunityPosts();

                // 🔹 data와 data.content 존재 확인
                const postsWithAuthor = (data?.content || []).map(post => ({
                    ...post,
                    authorNickname: post.authorNickname || "익명" // 작성자 닉네임 기본값
                }));

                setPosts(postsWithAuthor);
            } catch (error) {
                console.error("게시글 불러오기 실패:", error);
                setPosts([]); // fetch 실패 시 안전하게 빈 배열
            }
        };
        
        fetchPosts();
    }, []);

    return (
        <div className="community-page">
            <Header />
            
            <div className="community-container">
                {/* 왼쪽 사이드바 (슬림 버전) */}
                <aside className="community-sidebar">
                    <div className="sidebar-section">
                        <h3>카테고리</h3>
                        <ul>
                            {categories.map(cat => (
                                <li 
                                    key={cat} 
                                    className={selectedMenu === cat ? "active" : ""} 
                                    onClick={() => setSelectedMenu(cat)}
                                >
                                    {cat}
                                </li>
                            ))}
                        </ul>
                        <div className="filter-reset-area">
                            <button className="filter-reset-btn" onClick={handleReset}>
                                <RestartAltIcon fontSize="inherit" /> 필터 초기화
                            </button>
                        </div>
                    </div>

                    <div className="sidebar-section">
                        <h3>지역별</h3>
                        <div className="region-grid">
                            {regions.map(reg => (
                                <span 
                                    key={reg} 
                                    className={selectedMenu === reg ? "active" : ""} 
                                    onClick={() => setSelectedMenu(reg)}
                                >
                                    {reg}
                                </span>
                            ))}
                        </div>
                        <div className="filter-reset-area">
                            <button className="filter-reset-btn" onClick={handleReset}>
                                <RestartAltIcon fontSize="inherit" /> 필터 초기화
                            </button>
                        </div>
                    </div>
                </aside>

                {/* 메인 게시판 영역 (확장 버전) */}
                <main className="community-main-content">
                    <header className="community-content-header">
                        <div className="title-area">
                            <h1>커뮤니티</h1>
                            <p>여행 계획을 공유하고 다른 사람들과 소통하세요!</p>
                        </div>
                        
                        <div className="community-search-bar">
                            <select>
                                <option>제목</option>
                                <option>작성자</option>
                            </select>
                            <div className="search-input-box">
                                <input type="text" placeholder="검색어를 입력하세요" />
                                <button><SearchIcon fontSize="small" /></button>
                            </div>
                        </div>
                    </header>

                    <div className="community-board-container">
                        {/* 헤더 행 */}
                        <div className="board-header-row">
                            <div className="col-id">번호</div>
                            <div className="col-route">기타</div>
                            <div className="col-title">제목</div>
                            <div className="col-author">작성자</div>
                            <div className="col-date">날짜</div>
                            <div className="col-views">조회</div>
                            <div className="col-stats">추천</div>
                        </div>

                        {/* 리스트 본문 */}
                        <div className="board-body">
                            {posts.length === 0 ? (
                                <div className="no-posts">게시판에 글이 없습니다!</div>
                            ) : (
                                posts.map((post) => (
                                    <div className="board-item-row" key={post.id}>
                                        <div className="col-id">{post.id}</div>
                                        <div className="col-route">
                                            {post.departure ? `${post.departure}` : ""} - 
                                            {post.arrival ? `> ${post.arrival}` : ""}
                                        </div>
                                        <div className="col-title">
                                            <span className="post-title-text">{post.title}</span>
                                        </div>
                                        <div className="col-author">{post.authorNickname}</div>
                                        <div className="col-date">{post.createdAt?.split("T")[0]}</div>
                                        <div className="col-views">{post.viewCount}</div>
                                        <div className="col-stats">
                                            <span className="stat-item">
                                                <ThumbUpOffAltIcon fontSize="inherit" /> {post.recommendCount}
                                            </span>
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>

                    <div className="community-footer">
                        <button className="write-button" onClick={() => navigate("/community/write")}>글쓰기</button>
                    </div>
                </main>
            </div>
        </div>
    );
}