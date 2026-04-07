import React, { useState } from "react";
import Header from "../components/layout/Header.tsx";
import SearchIcon from '@mui/icons-material/Search';
import ThumbUpOffAltIcon from '@mui/icons-material/ThumbUpOffAlt';
import RestartAltIcon from '@mui/icons-material/RestartAlt';
import "./CommunityPage.css";
import { useNavigate } from "react-router-dom";

// 임시 게시글 데이터
const dummyPosts = [
    { 
        id: "260406-0001", 
        title: "이번 주말 부산 바다 보러 가실 분!", 
        author: "여행고수", 
        date: "2026.04.06", 
        views: 125, 
        likes: 12, 
        departure: "서울", 
        arrival: "부산", 
        comments: 5 
    },
    { 
        id: "260405-0002", 
        title: "기내수하물 캐리어 사이즈 질문 (국내선/국제선)", 
        author: "프로봇짐러", 
        date: "2026.04.05", 
        views: 89, 
        likes: 3, 
        departure: null, 
        arrival: null, 
        comments: 14 
    },
    { 
        id: "260404-0003", 
        title: "제주도 3박 4일 렌트카 쉐어하실 1인 구합니다", 
        author: "베스트드라이버", 
        date: "2026.04.04", 
        views: 210, 
        likes: 8, 
        departure: "김포", 
        arrival: "제주", 
        comments: 2 
    },
    { 
        id: "260403-0004", 
        title: "충북 단양 패러글라이딩 후기 공유해요", 
        author: "하늘날다", 
        date: "2026.04.03", 
        views: 156, 
        likes: 22, 
        departure: null, 
        arrival: null, 
        comments: 7 
    }
];

export default function CommunityPage() {
    const [selectedMenu, setSelectedMenu] = useState("전체보기");

    const categories = ["전체보기", "여행플랜 공유", "당일치기 친구 찾기", "자유게시판", "질문게시판", "사진게시판", "맛집게시판", "후기게시판"];
    const regions = ["서울", "경기", "인천", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"];
    
    const navigate = useNavigate();

    // 필터 초기화
    const handleReset = () => {
        setSelectedMenu("전체보기");
    };

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
                            <div className="col-route">여정</div>
                            <div className="col-title">제목</div>
                            <div className="col-author">작성자</div>
                            <div className="col-date">날짜</div>
                            <div className="col-views">조회</div>
                            <div className="col-stats">추천</div>
                        </div>

                        {/* 리스트 본문 */}
                        <div className="board-body">
                            {dummyPosts.map((post) => (
                                <div className="board-item-row" key={post.id}>
                                    <div className="col-id">{post.id.split('-')[1]}</div>
                                    <div className="col-route">
                                        {post.departure ? `${post.departure} ➔ ${post.arrival}` : "-"}
                                    </div>
                                    <div className="col-title">
                                        <span className="post-title-text">{post.title}</span>
                                        {post.comments > 0 && <span className="comment-count">[{post.comments}]</span>}
                                    </div>
                                    <div className="col-author">{post.author}</div>
                                    <div className="col-date">{post.date}</div>
                                    <div className="col-views">{post.views}</div>
                                    <div className="col-stats">
                                        <span className="stat-item"><ThumbUpOffAltIcon fontSize="inherit" /> {post.likes}</span>
                                    </div>
                                </div>
                            ))}
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