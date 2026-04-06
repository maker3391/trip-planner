import React, { useState } from "react";
import Header from "../components/layout/Header.tsx";
import { useNavigate } from "react-router-dom";
import "./CommunityWritePage.css";

export default function CommunityWritePage() {
    const navigate = useNavigate();

    // 상태 관리
    const [formData, setFormData] = useState({
        category: "자유게시판",
        region: "서울",
        title: "",
        content: "",
        departure: "",
        arrival: "",
        tags: ""
    });

    const categories = ["여행플랜 공유", "당일치기 친구 찾기", "자유게시판", "질문게시판", "사진게시판", "맛집게시판"];
    const regions = ["서울", "경기", "인천", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"];

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        // 실제로는 여기서 API 호출 (POST /posts)
        alert('게시글이 성공적으로 등록되었습니다! (현재는 더미 로직입니다)');
        navigate("/community");
    };

    return (
        <div className="community-page">
            <Header />
            
            <div className="community-container">
                <main className="community-main-content">
                    <header className="community-content-header">
                        <div className="title-area">
                            <h1>글쓰기</h1>
                            <p>새로운 소식이나 여행 계획을 공유해보세요!</p>
                        </div>
                    </header>

                    <form className="community-post-form" onSubmit={handleSubmit}>
                        <div className="form-row">
                            {/* 카테고리 선택 */}
                            <div className="form-group">
                                <label>분류</label>
                                <select name="category" value={formData.category} onChange={handleChange}>
                                    {categories.map(cat => <option key={cat} value={cat}>{cat}</option>)}
                                </select>
                            </div>

                            {/* 지역 선택 */}
                            <div className="form-group">
                                <label>지역</label>
                                <select name="region" value={formData.region} onChange={handleChange}>
                                    {regions.map(reg => <option key={reg} value={reg}>{reg}</option>)}
                                </select>
                            </div>
                        </div>

                        {/* 여행플랜 공유일 때만 나타나는 여정 입력창 */}
                        {formData.category === "여행플랜 공유" && (
                            <div className="form-row route-inputs">
                                <div className="form-group">
                                    <label>출발지</label>
                                    <input 
                                        type="text" 
                                        name="departure" 
                                        placeholder="예: 서울" 
                                        value={formData.departure} 
                                        onChange={handleChange} 
                                    />
                                </div>
                                <div className="route-arrow">➔</div>
                                <div className="form-group">
                                    <label>도착지</label>
                                    <input 
                                        type="text" 
                                        name="arrival" 
                                        placeholder="예: 부산" 
                                        value={formData.arrival} 
                                        onChange={handleChange} 
                                    />
                                </div>
                            </div>
                        )}

                        <div className="form-main-area">
                            <input 
                                className="post-input-title" 
                                name="title"
                                type="text" 
                                placeholder="제목을 입력하세요" 
                                value={formData.title}
                                onChange={handleChange}
                                required
                            />
                            <textarea 
                                className="post-input-content" 
                                name="content"
                                placeholder="내용을 입력하세요. 여행 꿀팁이나 궁금한 점을 자유롭게 적어주세요!" 
                                value={formData.content}
                                onChange={handleChange}
                                required
                            />
                            <input 
                                className="post-input-tags" 
                                name="tags"
                                type="text" 
                                placeholder="#태그 입력 (쉼표로 구분)" 
                                value={formData.tags}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="community-footer">
                            <button type="button" className="cancel-button" onClick={() => navigate("/community")}>취소</button>
                            <button type="submit" className="write-button">게시하기</button>
                        </div>
                    </form>
                </main>
            </div>
        </div>
    );
}