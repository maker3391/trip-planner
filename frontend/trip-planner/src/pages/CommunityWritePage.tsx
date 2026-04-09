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

    // 🔥 이미지 업로드 핸들러 수정
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
                
                // 🔥 [수정 지점 1] res.data가 아닌 res.data.imageId를 추출해야 합니다.
                // 서버 응답이 { success: true, imageId: 2 } 형태이기 때문입니다.
                const imageId = res.data.imageId; 

                if (!imageId) {
                    alert("이미지 서버 저장에 실패했습니다.");
                    return;
                }

                // 2. 관리 목록에 숫자 ID만 추가
                setUploadedImageIds(prev => [...prev, imageId]);

                // 3. 에디터에 삽입
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

            // 🔹 백엔드 DTO 구조에 정확히 맞춘 payload
            const payload = {
                category: formData.category,
                region: formData.region,
                title: formData.title,
                content: formData.content,
                departure: formData.departure,
                arrival: formData.arrival,
                tags: formData.tags,
                rating: formData.rating,
                userId: userData.id, // 유저 객체가 아닌 ID 숫자값
                imageIds: uploadedImageIds // 위에서 수정한 숫자 배열
            };

            // 🔥 [디버깅 팁] 전송 전 데이터 구조 확인
            console.log("백엔드로 날아가는 데이터:", payload);

            const response = await client.post("/community/posts", payload);

            if (response.status === 201 || response.status === 200) {
                alert('게시글이 성공적으로 등록되었습니다!');
                navigate("/community");
            }
        } catch (error: any) {
            // 🔥 [디버깅 팁] 500 에러 시 서버의 메시지를 상세히 출력
            console.error("등록 실패 상세 로그:", error.response?.data);
            alert(`등록 실패: ${error.response?.data?.message || "서버 오류"}`);
        }
    };

    return (
        <div className="community-page">
            <Header />
            <div className="community-container">
                <main className="community-main-content">
                    <form className="community-post-form" onSubmit={handleSubmit}>
                        {/* 분류/지역 로우 */}
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

                        {/* 여행 플랜 입력 칸 */}
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

                            {/* 커스텀 툴바 */}
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
                                <button className="ql-bold" />
                                <button className="ql-italic" />
                                <button className="ql-underline" />
                                <div style={{ width: "1px", height: "20px", backgroundColor: "#eee" }} />
                                <button className="ql-image" type="button">
                                    <span style={{ fontSize: "18px" }}>📷</span>
                                </button>

                                {/* 평점 영역을 툴바 안으로 이동 (디자인에 따라 조절) */}
                                {RATING_ENABLED_CATEGORIES.includes(formData.category) && (
                                    <div className="rating-section" style={{ display: "flex", alignItems: "center", gap: "5px", marginLeft: "10px" }}>
                                        <span style={{ fontSize: "12px", color: "#666" }}>⭐</span>
                                        {[1, 2, 3, 4, 5].map(num => (
                                            <span 
                                                key={num} 
                                                onClick={() => handleRating(num)}
                                                style={{ cursor: "pointer", color: num <= formData.rating ? "#FFBB00" : "#e0e0e0" }}
                                            >
                                                ★
                                            </span>
                                        ))}
                                    </div>
                                )}
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