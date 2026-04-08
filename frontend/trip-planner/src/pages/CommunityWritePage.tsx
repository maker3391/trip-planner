import React, { useState, useRef, useMemo } from "react";
import Header from "../components/layout/Header.tsx";
import { useNavigate } from "react-router-dom";
import client from "../components/api/client.ts";
import community from "../types/community.ts"
// npm install react-quill 해야합니다.
import ReactQuill, { Quill } from "react-quill";
import "react-quill/dist/quill.snow.css";
import "./CommunityWritePage.css";
import { getMe } from "../components/api/auth.ts";

// 💡 1. 폰트 크기를 숫자로 조절하기 위한 Quill 설정
const Size = Quill.import("attributors/style/size");
// 사용자가 선택할 수 있는 표준 폰트 사이즈 정의
Size.whitelist = ["12px", "14px", "16px", "18px", "20px", "24px", "28px", "32px", "36px"]; 
Quill.register(Size, true);

const RATING_ENABLED_CATEGORIES = ["맛집게시판", "사진게시판", "후기게시판"]; 
const PLAN_SHARE_ENABLED_CATEGORIES = ["여행플랜 공유", "당일치기 친구 찾기"];

interface UserInfo {
  id: number;
  email: string;
  name?: string;
  nickname?: string;
  phone?: string;
  address?: string;
  profileImage?: string;
  role?: string;
  status?: string;
}

export default function CommunityWritePage() {
    const navigate = useNavigate();
    const quillRef = useRef<ReactQuill>(null);
    const [ user, setUser ] = useState<UserInfo | null>(null);

    const [formData, setFormData] = useState({
        category: "자유게시판",
        region: "서울",
        title: "",
        content: "",
        departure: "",
        arrival: "",
        tags: "",
        rating: 0 
    });

    const categories = ["여행플랜 공유", "당일치기 친구 찾기", "자유게시판", "질문게시판", "사진게시판", "맛집게시판", "후기게시판" ];
    const regions = ["서울", "경기", "인천", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"];

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleContentChange = (content: string) => {
        setFormData(prev => ({ ...prev, content }));
    };

    const handleRating = (rate: number) => {
        setFormData(prev => ({ ...prev, rating: rate }));
    };

    const imageHandler = () => {
        const input = document.createElement("input");
        input.setAttribute("type", "file");
        input.setAttribute("accept", "image/*");
        input.click();

        input.onchange = async () => {
            const file = input.files ? input.files[0] : null;
            if (file) {
                const reader = new FileReader();
                reader.readAsDataURL(file);
                reader.onload = () => {
                    const editor = quillRef.current?.getEditor();
                    const range = editor?.getSelection();
                    if (editor && range) {
                        editor.insertEmbed(range.index, "image", reader.result);
                        editor.setSelection(range.index + 1, 0);
                    }
                };
            }
        };
    };

    const modules = useMemo(() => ({
        toolbar: {
            container: "#toolbar", 
            handlers: {
                image: imageHandler,
            }
        }
    }), []);

    const handleSubmit = async (e: React.FormEvent) => {
            e.preventDefault();

            if (!localStorage.getItem("isLoggedIn")) {
                alert("로그인이 필요한 서비스 입니다.");
                navigate("/api/auth/login");
                return;
            }

            try {
                // 🔹 userId 가져오기 (로컬스토리지에서)
                const userData = await getMe();
                setUser(userData);

                if (!userData.id) {
                    alert("유저 정보가 없습니다. 다시 로그인 해주세요.");
                    navigate("/login");
                    return;
                }
                const userId = userData.id;

                // 🔹 content가 비어 있는지 추가 체크
                if (!formData.content || formData.content.trim() === "<p><br></p>") {
                    alert("내용을 입력해주세요.");
                    return;
                }

                // 🔹 기존 formData에 userId 추가
                const payload: community.CommunityRequest = { ...formData, userId };

                const response = await client.post("/community/posts", payload);

                if (response.status === 201) {
                    alert('게시글이 성공적으로 등록되었습니다!');
                    window.location.href = "/community"; 
                } else {
                    alert("게시글 등록에 실패했습니다.");
                    console.error("등록 실패:", response);
                }

            } catch (error: any) {
                // 서버 에러 처리
                alert("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
                console.error("등록 실패:", error);
            }
        };

    return (
        <div className="community-page">
            <Header />
            <div className="community-container">
                <main className="community-main-content">
                    <form className="community-post-form" onSubmit={handleSubmit}>
                        <div className="form-row">
                            <div className="form-group">
                                <label>분류</label>
                                <select name="category" value={formData.category} onChange={handleChange}>
                                    {categories.map(cat => <option key={cat} value={cat}>{cat}</option>)}
                                </select>
                            </div>

                            <div className="form-group">
                                <label>지역</label>
                                <select name="region" value={formData.region} onChange={handleChange}>
                                    {regions.map(reg => <option key={reg} value={reg}>{reg}</option>)}
                                </select>
                            </div>
                        </div>

                        { PLAN_SHARE_ENABLED_CATEGORIES.includes(formData.category) && (
                            <div className="form-row route-inputs">
                                <div className="form-group">
                                    <label>출발지</label>
                                    <input type="text" name="departure" placeholder="예: 서울" value={formData.departure} onChange={handleChange} />
                                </div>
                                <div className="route-arrow">➔</div>
                                <div className="form-group">
                                    <label>도착지</label>
                                    <input type="text" name="arrival" placeholder="예: 부산" value={formData.arrival} onChange={handleChange} />
                                </div>
                            </div>
                        )}

                        {RATING_ENABLED_CATEGORIES.includes(formData.category) && (
                            <div className="toolbar-item rating-section" style={{ display: "flex", alignItems: "center", gap: "8px", marginLeft: "10px", borderLeft: "1px solid #eee", paddingLeft: "15px" }}>
                                <span style={{ fontSize: "13px", fontWeight: "600", color: "#666" }}>⭐ 평점</span>
                                <div className="stars" style={{ display: "flex" }}>
                                    {[1, 2, 3, 4, 5].map(num => (
                                        <span 
                                            key={num} 
                                            onClick={() => handleRating(num)}
                                            style={{ cursor: "pointer", fontSize: "20px", color: num <= formData.rating ? "#FFBB00" : "#e0e0e0" }}
                                        >
                                            {num <= formData.rating ? '★' : '☆'}
                                        </span>
                                    ))}
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

                            {/* 🛠 강화된 표준 툴바 디자인 */}
                            <div id="toolbar" className="post-toolbar" style={{ 
                                display: "flex", 
                                alignItems: "center", 
                                flexWrap: "wrap",
                                gap: "8px", 
                                padding: "10px 15px",
                                border: "1px solid #ddd",
                                borderBottom: "none",
                                backgroundColor: "#fff",
                                borderRadius: "8px 8px 0 0"
                            }}>
                                {/* 폰트 크기 선택 셀렉트 박스 */}
                                <select className="ql-size" defaultValue="16px" style={{ width: "100px", padding: "2px", border: "1px solid #ddd", borderRadius: "4px" }}>
                                    {Size.whitelist.map((size: string) => (
                                        <option key={size} value={size}>{size}</option>
                                    ))}
                                </select>

                                <div style={{ width: "1px", height: "20px", backgroundColor: "#eee", margin: "0 5px" }} />

                                {/* 텍스트 스타일 버튼 */}
                                <button className="ql-bold" title="굵게" />
                                <button className="ql-italic" title="기울임" />
                                <button className="ql-underline" title="밑줄" /> {/* 밑줄 추가 */}
                                <button className="ql-strike" title="취소선" />  {/* 취소선 추가 */}

                                <div style={{ width: "1px", height: "20px", backgroundColor: "#eee", margin: "0 5px" }} />

                                {/* 유틸리티: 이미지 및 별점 */}
                                <button className="ql-image" type="button" style={{ border: "none", background: "none", cursor: "pointer", display: "flex", alignItems: "center", gap: "4px" }}>
                                    <span style={{ fontSize: "18px" }}>📷</span>
                                    <span style={{ fontSize: "13px", fontWeight: "600", color: "#666" }}>사진 추가</span>
                                </button>
                            </div>

                            <ReactQuill 
                                ref={quillRef}
                                theme="snow"
                                value={formData.content}
                                onChange={handleContentChange}
                                modules={modules}
                                placeholder="글을 작성해보세요..."
                                style={{ height: "500px", marginBottom: "60px" }}
                            />
                            
                            <input 
                                className="post-input-tags" 
                                name="tags"
                                type="text" 
                                placeholder="#태그 입력" 
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