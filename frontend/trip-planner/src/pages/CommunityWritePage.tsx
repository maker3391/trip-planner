import React, { useState, useRef, useMemo, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import client from "../components/api/client.ts";
import ReactQuill, { Quill } from "react-quill";
import "react-quill/dist/quill.snow.css";
import "./CommunityWritePage.css";
import Header from "../components/layout/Header.tsx";
import { UserMeResponse } from "../components/api/auth.ts";
import toast from "react-hot-toast";

// =====================================================================
// [요구사항 확인 및 안내]
// 1~3번 규칙(우선순위, 다중선택 OR 연산, 전체보기 자동전환)은
// 커뮤니티 목록 '조회' 사이드바 필터링에 해당하는 로직이므로 글쓰기 페이지에는 직접적인 영향이 없습니다.
//
// 규칙 4에 따라 코드 주석으로만 설명을 기재하고 전문을 반환합니다.
// 규칙 5에 따라 formData의 'region' 변수 타입(단일 문자열)과 폼 구조를 임의로 변경하지 않았습니다.
//
// ※ 만약 '하나의 게시글'에 여러 지역(예: 서울과 부산 동시 지정)을 등록할 수 있도록
// 다중 선택(Select Multiple) 기능이 추가로 필요하시다면 허가를 부탁드립니다.
// 현재는 기존의 단일 지역/카테고리 선택 구조를 그대로 유지했습니다.
// =====================================================================

type TripPlanItem = {
    id: number;
    title: string;
    destination: string;
    startDate: string;
    endDate: string;
    schedules?: unknown[];
};

export const getMe = async () => {
    const res = await client.get("/auth/me");
    return res.data;
};

// =========================
// 카테고리 조건
// =========================
const RATING_ENABLED_CATEGORIES = ["후기게시판"];
const PLAN_SHARE_ENABLED_CATEGORIES = ["여행플랜", "후기게시판"];

// =========================
// Quill 설정 (폰트 사이즈)
// =========================
// style 기반 size를 사용하므로 style attributor를 등록합니다.
const Size = Quill.import("attributors/style/size");
Size.whitelist = ["12px","14px","16px","18px","20px","24px","28px","32px","36px"];
Quill.register(Size, true);

export default function CommunityWritePage() {
    const [tripPlans, setTripPlans] = useState<TripPlanItem[]>([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [tripLoading, setTripLoading] = useState(false);
    const navigate = useNavigate();

    // 1. URL의 마지막 단어를 가져와서 ID인지 확인합니다.
    const pathSegments = window.location.pathname.split("/");
    const lastSegment = pathSegments[pathSegments.length - 1];

    // 마지막 단어가 숫자로 변환 가능하고 'write'가 아니면 ID로 인식
    const parsedId = parseInt(lastSegment);
    const isEditMode = !isNaN(parsedId) && lastSegment !== "write";
    const currentPostId = isEditMode ? lastSegment : null;

    // =========================
    // refs
    // =========================
    const quillRef = useRef<ReactQuill>(null);

    // =========================
    // 상태
    // =========================
    const [uploadedImageIds, setUploadedImageIds] = useState<number[]>([]);
    const [isLoading, setIsLoading] = useState<boolean>(isEditMode);
    const [me, setMe] = useState<UserMeResponse | null>(null);

    const isAdmin = me?.role === "ADMIN";

    const [formData, setFormData] = useState({
        category: "자유게시판",
        region: "미정",
        title: "",
        content: "",
        departure: "",
        arrival: "",
        tags: "",
        rating: 0,
        tripPlanId: ""
    });

    const categories_user = ["자유게시판", "여행플랜", "후기게시판"];

    const categories_admin = ["자유게시판", "여행플랜", "후기게시판", "공지게시판"];

    const categories = isAdmin ? categories_admin : categories_user;

    const regions = [
        "서울특별시",
        "부산광역시",
        "대구광역시",
        "인천광역시",
        "광주광역시",
        "대전광역시",
        "울산광역시",
        "세종특별자치시",
        "경기도",
        "강원특별자치도",
        "충청도",
        "전라도",
        "경상도",
        "제주특별자치도"
    ];

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
    // 수정 모드 - 기존 게시글 로딩
    // =========================
    useEffect(() => {
        if (!isEditMode || !currentPostId) {
            setIsLoading(false);
            return;
        }

        const fetchPost = async () => {
            try {
                setIsLoading(true);
                const res = await client.get(`/community/posts/${currentPostId}`);
                const data = res.data;

                setFormData({
                    category: data.category || "자유게시판",
                    region: data.region || "미정",
                    title: data.title || "",
                    content: data.content || "",
                    departure: data.departure || "",
                    arrival: data.arrival || "",
                    tags: data.tags || "",
                    rating: data.rating || 0,
                    tripPlanId: data.tripPlan?.id ? String(data.tripPlan.id) : ""
                });
                setUploadedImageIds(data.imageIds || []);
            } catch (err) {
                console.error(err);
                toast.error("게시글 정보를 불러오지 못했습니다.");
                navigate("/community");
            } finally {
                setIsLoading(false);
            }
        };

        fetchPost();
    }, [currentPostId, isEditMode, navigate]);

    useEffect(() => {
        const fetchTrips = async () => {
            try {
                setTripLoading(true);
                const res = await client.get("/trips");
                setTripPlans(res.data || []);
            } catch (err) {
                console.error("여행 계획 목록 불러오기 실패", err);
                setTripPlans([]);
            } finally {
                setTripLoading(false);
            }
        };

        fetchTrips();
    }, []);

    const selectedTrip = tripPlans.find(
        (trip) => String(trip.id) === String(formData.tripPlanId)
    );

    // =========================
    // input 변경
    // =========================
    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
    ) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    // =========================
    // Quill content 변경
    // =========================
    const handleContentChange = (content: string) => {
        setFormData(prev => ({ ...prev, content }));
    };

    // Quill HTML 보정 (font-size span 유지 보장)
    const normalizeQuillHtml = (html: string) => {
        if (!html) return "";

        // 혹시 Quill이 inline style 제거했을 경우 복구
        return html.replace(
            /<span([^>]*)data-size="([^"]+)"([^>]*)>/g,
            (match, p1, size, p2) => {
                return `<span${p1}${p2} style="font-size:${size}">`;
            }
        );
    };

    // =========================
    // rating 변경
    // =========================
    const handleRating = (rate: number) => {
        setFormData(prev => ({ ...prev, rating: rate }));
    };

    // =========================
    // 이미지 업로드 (Quill toolbar)
    // =========================
    const handleImage = () => {
        const input = document.createElement("input");
        input.type = "file";
        input.accept = "image/*";
        input.click();

        input.onchange = async () => {
            const file = input.files?.[0];
            if (!file) return;

            try {
                const form = new FormData();
                form.append("file", file);

                const res = await client.post("/community/image", form);
                const imageId = res.data.imageId;

                setUploadedImageIds(prev => [...prev, imageId]);

                const imageUrl = `${client.defaults.baseURL}/community/image/${imageId}`;
                const editor = quillRef.current?.getEditor();

                if (!editor) return;

                const range = editor.getSelection(true) || {
                    index: editor.getLength()
                };

                editor.insertEmbed(range.index, "image", imageUrl);
            } catch {
                toast.error("이미지 업로드 실패");
            }
        };
    };

    // =========================
    // Quill module / format 설정
    // =========================
    const modules = useMemo(() => ({
        toolbar: {
            container: "#toolbar",
            handlers: {
                image: handleImage
            }
        }
    }), []);

    const formats = useMemo(
        () => ["size", "bold", "italic", "underline", "align", "image"],
        []
    );

    // =========================
    // submit (수정/등록 분기 처리)
    // =========================
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (isSubmitting) return;

        if (!formData.title.trim()) {
            toast.error("제목을 입력해주세요.", { id: "validation-title" });
            return;
        }
        // 🔥 HTML → TEXT 변환해서 검사
        const plainText = formData.content
            .replace(/<[^>]*>/g, "")   // 태그 제거
            .replace(/&nbsp;/g, " ")   // 공백 처리
            .trim();

        if (!plainText) {
            toast.error("내용을 입력해주세요.", { id: "validation-content" });
            return;
        }

        setIsSubmitting(true);

        try {
            let finalRegion = formData.region;
            if (
                PLAN_SHARE_ENABLED_CATEGORIES.includes(formData.category) &&
                formData.region === "미정"
            ) {
                finalRegion = regions[0];
            }

            const fixedContent = normalizeQuillHtml(formData.content);

            console.log("최종 제출 데이터: ", {fixedContent});

            const payload = {
                ...formData,
                content: fixedContent,
                region: finalRegion,
                tripPlanId: formData.tripPlanId ? Number(formData.tripPlanId) : null,
                imageIds: uploadedImageIds
            };

            if (isEditMode && currentPostId) {
                await client.put(`/community/posts/${currentPostId}`, payload);
                toast.success("게시글이 수정되었습니다.");
            } else {
                await client.post("/community/posts", payload);
                toast.success("새 게시글이 등록되었습니다.", { id: "post-create-success" });
            }

            setTimeout(() => {
                navigate("/community");
            }, 1000);
        } catch (err) {
            console.error(err);
            toast.error("저장에 실패했습니다.");
            setIsSubmitting(false);
        }
    };

    // =========================
    // loading 처리
    // =========================
    if (isLoading) {
        return (
            <div style={{ padding: "50px", textAlign: "center" }}>
                불러오는 중...
            </div>
        );
    }

    return (
        <>
            <Header />
            <div className="community-page">
                <div className="community-container">
                    <main className="community-main-content">
                        <form className="community-post-form" onSubmit={handleSubmit}>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>분류</label>
                                    <select
                                        name="category"
                                        value={formData.category}
                                        onChange={handleChange}
                                        disabled={isEditMode}
                                    >
                                        {categories.map(cat => <option key={cat}>{cat}</option>)}
                                    </select>
                                </div>

                                {(formData.category === "여행플랜" || formData.category === "후기게시판") && (
                                    <div className="form-group">
                                        <label>지역</label>
                                        <select
                                            name="region"
                                            value={formData.region}
                                            onChange={handleChange}
                                        >
                                            {regions.map(r => (
                                                <option key={r} value={r}>
                                                    {r}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                )}
                            </div>

                            {PLAN_SHARE_ENABLED_CATEGORIES.includes(formData.category) && (
                                <div className="route-inputs form-row">
                                    <input
                                        name="departure"
                                        placeholder="출발지"
                                        value={formData.departure}
                                        onChange={handleChange}
                                    />
                                    <div className="route-arrow">→</div>
                                    <input
                                        name="arrival"
                                        placeholder="도착지"
                                        value={formData.arrival}
                                        onChange={handleChange}
                                    />
                                </div>
                            )}

                            {PLAN_SHARE_ENABLED_CATEGORIES.includes(formData.category) && (
                                <div className="trip-plan-select-section">
                                    <label>여행 계획 첨부 (선택)</label>
                                    <select
                                        name="tripPlanId"
                                        value={formData.tripPlanId}
                                        onChange={handleChange}
                                    >
                                        <option value="">첨부 안 함</option>
                                        {tripPlans.map((trip) => (
                                            <option key={trip.id} value={trip.id}>
                                                {trip.title} / {trip.destination}
                                            </option>
                                        ))}
                                    </select>

                                    {tripLoading && (
                                        <div className="trip-plan-help">
                                            여행 계획 불러오는 중...
                                        </div>
                                    )}

                                    {selectedTrip && (
                                        <div className="selected-trip-preview">
                                            <div><strong>선택한 여행:</strong> {selectedTrip.title}</div>
                                            <div><strong>여행지:</strong> {selectedTrip.destination}</div>
                                            <div>
                                                <strong>기간:</strong> {selectedTrip.startDate} ~ {selectedTrip.endDate}
                                            </div>
                                            <div>
                                                <strong>일정 개수:</strong> {selectedTrip.schedules?.length ?? 0}개
                                            </div>
                                        </div>
                                    )}
                                </div>
                            )}

                            {RATING_ENABLED_CATEGORIES.includes(formData.category) && (
                                <div className="rating-form-row">
                                    <div className="rating-input-container">
                                        <div className="stars">
                                            {[1, 2, 3, 4, 5].map(n => (
                                                <span
                                                    key={n}
                                                    onClick={() => handleRating(n)}
                                                    style={{ cursor: "pointer" }}
                                                >
                                                    {n <= formData.rating ? "★" : "☆"}
                                                </span>
                                            ))}
                                        </div>
                                        <div className="rating-text">{formData.rating}점</div>
                                    </div>
                                </div>
                            )}

                            <div className="form-main-area">
                                <input
                                    className="post-input-title"
                                    name="title"
                                    value={formData.title}
                                    onChange={handleChange}
                                    placeholder="제목을 입력하세요"
                                />

                                <div id="toolbar" className="post-toolbar">
                                    <select className="ql-size" defaultValue="">
                                        <option value="">기본</option>
                                        {Size.whitelist.map((size:string) => (
                                            <option key={size} value={size}>
                                                {size}
                                            </option>
                                        ))}
                                    </select>

                                    <div className="toolbar-divider" />
                                    <button type="button" className="ql-bold" />
                                    <button type="button" className="ql-italic" />
                                    <button type="button" className="ql-underline" />
                                    <div className="toolbar-divider" />
                                    <button type="button" className="ql-align" value="" />
                                    <button type="button" className="ql-align" value="center" />
                                    <button type="button" className="ql-align" value="right" />
                                    <div className="toolbar-divider" />
                                    <button type="button" className="ql-image" />
                                </div>

                                <ReactQuill
                                    ref={quillRef}
                                    theme="snow"
                                    value={formData.content}
                                    onChange={handleContentChange}
                                    modules={modules}
                                    formats={formats}
                                    placeholder="내용을 작성해주세요."
                                />

                                <input
                                    className="post-input-tags"
                                    name="tags"
                                    value={formData.tags}
                                    onChange={handleChange}
                                    placeholder="#태그를 입력하세요 (,으로 구분)"
                                />
                            </div>

                            <div className="community-footer">
                                <button
                                    type="button"
                                    className="cancel-button"
                                    onClick={() => navigate(-1)}
                                >
                                    취소
                                </button>
                                <button
                                    type="submit"
                                    className="write-button"
                                    disabled={isSubmitting}
                                >
                                    {isSubmitting
                                        ? "처리 중..."
                                        : (isEditMode ? "수정 완료" : "게시하기")}
                                </button>
                            </div>
                        </form>
                    </main>
                </div>
            </div>
        </>
    );
}