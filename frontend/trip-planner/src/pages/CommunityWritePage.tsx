import React, { useState, useRef, useMemo } from "react";
import Header from "../components/layout/Header.tsx";
import { useNavigate } from "react-router-dom";
import client from "../components/api/client.ts";
import ReactQuill, { Quill } from "react-quill";
import "react-quill/dist/quill.snow.css";
import "./CommunityWritePage.css";
import { getMe } from "../components/api/auth.ts";

const Size = Quill.import("attributors/style/size");
Size.whitelist = ["12px", "14px", "16px", "18px", "20px", "24px", "28px", "32px", "36px"]; 
Quill.register(Size, true);

const RATING_ENABLED_CATEGORIES = ["맛집게시판", "후기게시판", "사진게시판"]; 
const PLAN_SHARE_ENABLED_CATEGORIES = ["여행플랜 공유", "당일치기 친구 찾기"];

export default function CommunityWritePage() {
    const navigate = useNavigate();
    const quillRef = useRef<ReactQuill>(null);
    
    // 업로드된 이미지 ID들을 숫자로 저장 (상태 정의)
    const [uploadedImageIds, setUploadedImageIds] = useState<number[]>([]);

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

    const categories = ["여행플랜 공유", "자유게시판", "질문게시판", "맛집게시판", "후기게시판", "사진게시판", "공지게시판"];
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

    // 이미지 업로드 핸들러
    const handleImage = () => {
        const input = document.createElement("input");
        input.setAttribute("type", "file");
        input.setAttribute("accept", "image/*");
        input.click();

        input.onchange = async () => {
            const file = input.files ? input.files[0] : null;
            if (!file) return;

            if (file.size > 5 * 1024 * 1024) {
                alert("파일 크기는 5MB를 초과할 수 없습니다.");
                return;
            }

            try {
                const imageFormData = new FormData();
                imageFormData.append("file", file);

                const res = await client.post("/community/image", imageFormData, {
                    headers: { "Content-Type": "multipart/form-data" },
                    timeout: 30000 
                });
                
                const imageId = res.data.imageId; 

                if (!imageId) {
                    alert("이미지 서버 저장에 실패했습니다.");
                    return;
                }

                setUploadedImageIds(prev => [...prev, imageId]);

                const imageUrl = `${client.defaults.baseURL}/community/image/${imageId}`;
                const editor = quillRef.current?.getEditor();
                const range = editor?.getSelection(true);

                if (editor) {
                    const index = range ? range.index : editor.getLength();
                    editor.insertEmbed(index, "image", imageUrl);
                    editor.setSelection(index + 1, 0);
                }
            } catch (err: any) {
                console.error("이미지 업로드 실패:", err);
                alert("이미지 업로드 중 오류가 발생했습니다.");
            }
        };
    };

    const modules = useMemo(() => ({
        toolbar: {
            container: "#toolbar", 
            handlers: { image: handleImage }
        }
    }), []);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
            const userData = await getMe();

            if (!userData || !userData.id) {
                alert("로그인 세션이 만료되었습니다.");
                return;
            }

            const payload = {
                ...formData,
                userId: userData.id,
                imageIds: uploadedImageIds 
            };

            const response = await client.post("/community/posts", payload);

            if (response.status === 201 || response.status === 200) {
                alert('게시글이 성공적으로 등록되었습니다!');
                navigate("/community");
            }
        } catch (error: any) {
            alert("게시글 등록에 실패했습니다.");
        }
    };

    return (
        <div className="community-page">
            <Header />
            <div className="community-container">
                <main className="community-main-content">
                    <form className="community-post-form" onSubmit={handleSubmit}>
                        {/* 분류/지역 로우 (1행) */}
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

                        {/* 🌟 수정지점: 여행 플랜 또는 평점 입력 로우 (2행) */}
                        {PLAN_SHARE_ENABLED_CATEGORIES.includes(formData.category) ? (
                            // 여행플랜 공유 카테고리일 때 (출발지/도착지)
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
                        ) : RATING_ENABLED_CATEGORIES.includes(formData.category) ? (
                            // 맛집/후기/사진 게시판일 때 (평점 - 디자인 통일)
                            <div className="form-row rating-form-row">
                                <div className="form-group" style={{ flex: 1 }}> {/* 레이아웃 맞추기 위해 flex 설정 */}
                                    <label>평점</label>
                                    <div className="rating-input-container" style={{ 
                                        display: 'flex', 
                                        alignItems: 'center', 
                                        height: '40px', // 일반 input 높이와 통일 (CSS 파일 확인 필요)
                                        border: '1px solid #ddd', 
                                        borderRadius: '4px', 
                                        padding: '0 10px',
                                        backgroundColor: '#fff'
                                    }}>
                                        <div className="stars" style={{ display: "flex", gap: '8px' }}>
                                            {[1, 2, 3, 4, 5].map(num => (
                                                <span 
                                                    key={num} 
                                                    onClick={() => handleRating(num)}
                                                    style={{ 
                                                        cursor: "pointer", 
                                                        fontSize: '24px', // 별 크기 키움
                                                        lineHeight: '1',
                                                        color: num <= formData.rating ? "#FFBB00" : "#e0e0e0",
                                                        transition: 'color 0.2s'
                                                    }}
                                                >
                                                    {num <= formData.rating ? '★' : '☆'}
                                                </span>
                                            ))}
                                        </div>
                                        {formData.rating > 0 && (
                                            <span style={{ marginLeft: '12px', color: '#666', fontSize: '14px' }}>
                                                ({formData.rating}점 / 5점)
                                            </span>
                                        )}
                                    </div>
                                </div>
                                {/* 레이아웃 균형을 위한 빈 공간 (출발지➔도착지 3단 구조와 맞춤) */}
                                <div style={{ width: '30px' }} className="route-arrow-space"></div> 
                                <div className="form-group" style={{ flex: 1, visibility: 'hidden' }}></div>
                            </div>
                        ) : null}

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

                            <div id="toolbar" className="post-toolbar" style={{ 
                                display: "flex", alignItems: "center", flexWrap: "wrap", gap: "8px", 
                                padding: "10px 15px", border: "1px solid #ddd", borderBottom: "none",
                                backgroundColor: "#fff", borderRadius: "8px 8px 0 0"
                            }}>
                                <select className="ql-size" defaultValue="16px">
                                    {Size.whitelist.map((size: string) => (
                                        <option key={size} value={size}>{size}</option>
                                    ))}
                                </select>
                                <div style={{ width: "1px", height: "20px", backgroundColor: "#eee" }} />
                                
                                {/* 기본 스타일 버튼 */}
                                <button className="ql-bold" />
                                <button className="ql-italic" />
                                <button className="ql-underline" />
                                <div style={{ width: "1px", height: "20px", backgroundColor: "#eee" }} />

                                {/* 🌟 정렬 버튼 추가 (좌, 중, 우) */}
                                <button className="ql-align" value="" defaultChecked />       {/* 좌측 정렬 (기본값) */}
                                <button className="ql-align" value="center" /> {/* 중앙 정렬 */}
                                <button className="ql-align" value="right" />  {/* 우측 정렬 */}
                                <div style={{ width: "1px", height: "20px", backgroundColor: "#eee" }} />

                                <button className="ql-image" type="button">
                                    <span style={{ fontSize: "18px" }}>📷</span>
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