import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "../components/layout/Header.tsx";
import client from "../components/api/client.ts";
import { CommunityResponse, CommunityPageResponse } from "../types/community.ts";
import {
    requestJoinTrip,
    getTripMembers,
    acceptTripMember,
    removeTripMember,
    TripMemberResponse,
} from "../components/api/tripMember.ts";

import ShareIcon from "@mui/icons-material/Share";
import RemoveRedEyeIcon from "@mui/icons-material/RemoveRedEye";
import ThumbUpOffAltIcon from "@mui/icons-material/ThumbUpOffAlt";
import ArrowRightAltIcon from "@mui/icons-material/ArrowRightAlt";
import StarIcon from "@mui/icons-material/Star";

import "./CommunityReadPage.css";
import { getCommunityPosts } from "./CommunityPage.tsx";
import CommunitySidebar from "../components/layout/CommunitySidebar.tsx";

export const getPost = async (id: number) => {
    const res = await client.get(`/community/posts/${id}`);
    return res.data;
};

export const getMe = async () => {
    const res = await client.get("/auth/me");
    return res.data;
};

const RATING_ENABLED_CATEGORIES = ["맛집게시판", "사진게시판", "후기게시판"];
const PLAN_SHARE_ENABLED_CATEGORIES = ["여행플랜 공유", "당일치기 친구 찾기"];

export default function CommunityReadPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    const [post, setPost] = useState<CommunityResponse | null>(null);
    const [posts, setPosts] = useState<CommunityResponse[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [liked, setLiked] = useState(false);
    const [me, setMe] = useState<{ id: number } | null>(null);
    const [members, setMembers] = useState<TripMemberResponse[]>([]);
    const [loadingMembers, setLoadingMembers] = useState(false);
    const [selectedCategory, setSelectedCategory] = useState("전체보기");
    const [selectedRegion, setSelectedRegion] = useState<string | null>("전체");

    const renderRouteOrRating = (post: CommunityResponse) => {
        if (post.category && RATING_ENABLED_CATEGORIES.includes(post.category)) {
            const rating = post.rating || 0;
            return (
                <div className="rating-stars" style={{ color: "#FFBB00", fontSize: "16px" }}>
                    {Array.from({ length: 5 }).map((_, i) => (
                        <span key={i}>{i < rating ? "★" : "☆"}</span>
                    ))}
                </div>
            );
        }

        if (post.category && PLAN_SHARE_ENABLED_CATEGORIES.includes(post.category)) {
            if (!post.departure && !post.arrival) return " - ";

            return (
                <div
                    style={{
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        gap: "4px",
                    }}
                >
                    <span>{post.departure || "미정"}</span>
                    <ArrowRightAltIcon fontSize="small" />
                    <span>{post.arrival || "미정"}</span>
                </div>
            );
        }

        return " - ";
    };

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

    const isAuthor = post?.authorId === me?.id;
    const isAdmin = me?.role === "ADMIN";
    const myMember = members.find((member) => member.userId === me?.id);
    const isJoined = !!myMember;
    const isPending = myMember?.role === "PENDING";
    const isMemberApproved =
        myMember?.role === "MEMBER" || myMember?.role === "OWNER";
    const pendingMembers = members.filter((member) => member.role === "PENDING");
    const approvedMembers = members.filter(
        (member) => member.role === "MEMBER" || member.role === "OWNER"
    );

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

    const fetchTripMembers = async (tripId: number) => {
        try {
            setLoadingMembers(true);
            const data = await getTripMembers(tripId);
            setMembers(data || []);
        } catch (error) {
            console.error("멤버 목록 조회 실패:", error);
            setMembers([]);
        } finally {
            setLoadingMembers(false);
        }
    };

    useEffect(() => {
        const fetchPostDetail = async () => {
            if (!id) return;

            try {
                await client.patch(`/community/posts/${id}/view`);

                const data = await getPost(Number(id));

                setPost(data);
                setLiked(data.likedByMe);

                if (data.tripPlan?.id) {
                    await fetchTripMembers(data.tripPlan.id);
                } else {
                    setMembers([]);
                }

                setPosts((prev) =>
                    prev.map((p) =>
                        p.id === Number(id) ? { ...p, viewCount: data.viewCount } : p
                    )
                );
            } catch (error) {
                console.error("데이터 로드 실패:", error);
            }
        };

        fetchPostDetail();
    }, [id]);

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

    const handleLike = async () => {
        if (!post || !id) return;

        try {
            const res = await client.post(`/community/posts/${id}/like`);
            const { liked: isLiked, likeCount } = res.data;

            setLiked(isLiked);

            setPost((prev) => (prev ? { ...prev, likeCount } : null));

            setPosts((prev) =>
                prev.map((p) => (p.id === Number(id) ? { ...p, likeCount } : p))
            );
        } catch (err) {
            console.error("좋아요 실패:", err);
        }
    };

    const handleShare = async () => {
        if (!post) return;

        try {
            await navigator.clipboard.writeText(window.location.href);
            const res = await client.patch(`/community/posts/${post.id}/share`);
            const newShareCount = res.data.shareCount ?? ((post.shareCount || 0) + 1);

            alert("링크가 복사되었습니다!");

            setPost((prev) => (prev ? { ...prev, shareCount: newShareCount } : null));

            setPosts((prev) =>
                prev.map((p) => (p.id === post.id ? { ...p, shareCount: newShareCount } : p))
            );
        } catch (error) {
            alert("공유 실패");
        }
    };

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

    const pageNumbers = Array.from({ length: Math.min(11, totalPages) }, (_, i) => i);

    const handleJoinTrip = async () => {
        if (!me) {
            alert("로그인 후 참가 신청이 가능합니다.");
            navigate("/login");
            return;
        }

        if (!post?.tripPlan?.id) {
            alert("연결된 여행 계획이 없습니다.");
            return;
        }

        try {
            const res = await requestJoinTrip(post.tripPlan.id);
            alert(res.message || "참가 신청이 완료되었습니다.");
            await fetchTripMembers(post.tripPlan.id);

            // ✅ 성공 후 TripListPage로 이동하면서 tripPlan 데이터 전달
            navigate("/trip-list", { state: { joinedTrip: post.tripPlan } });

        } catch (err: any) {
            const message = err?.response?.data?.message || "참가 신청에 실패했습니다.";
            alert(message);
        }
    };

    const handleAcceptMember = async (memberId: number) => {
        if (!post?.tripPlan?.id) return;

        try {
            const res = await acceptTripMember(post.tripPlan.id, memberId);
            alert(res.message || "참가가 수락되었습니다.");
            await fetchTripMembers(post.tripPlan.id);
        } catch (err: any) {
            const message = err?.response?.data?.message || "참가 수락에 실패했습니다.";
            alert(message);
        }
    };

    const handleRemoveMember = async (memberId: number) => {
        if (!post?.tripPlan?.id) return;

        try {
            const res = await removeTripMember(post.tripPlan.id, memberId);
            alert(res.message || "멤버가 삭제되었습니다.");
            await fetchTripMembers(post.tripPlan.id);
        } catch (err: any) {
            const message = err?.response?.data?.message || "멤버 삭제에 실패했습니다.";
            alert(message);
        }
    };

    return (
        <>
            <Header />
            <div className="community-page">
                <div className="community-container">
                    <CommunitySidebar
                        selectedCategory={selectedCategory}
                        selectedRegion={selectedRegion}
                        onCategoryChange={(cat) => {
                            setSelectedCategory(cat);
                            setPage(0);
                        }}
                        onRegionChange={(reg) => {
                            setSelectedRegion(reg);
                            setPage(0);
                        }}
                        onReset={handleReset}
                    />

                    <main className="community-main-content">
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

                            <div className="post-extra-info">
                                {post?.category &&
                                    PLAN_SHARE_ENABLED_CATEGORIES.includes(post.category) && (
                                        <div className="route-display">
                                            <strong>경로:</strong> {post.departure} <ArrowRightAltIcon />{" "}
                                            {post.arrival}
                                        </div>
                                    )}
                                {post?.category &&
                                    RATING_ENABLED_CATEGORIES.includes(post.category) && (
                                        <div className="rating-display">
                                            <strong>평점:</strong>
                                            <span className="stars">
                                                {Array.from({ length: 5 }).map((_, i) => (
                                                    <StarIcon
                                                        key={i}
                                                        style={{
                                                            color:
                                                                i < (post.rating || 0)
                                                                    ? "#FFBB00"
                                                                    : "#e0e0e0",
                                                        }}
                                                    />
                                                ))}
                                            </span>
                                        </div>
                                    )}
                            </div>

                            <div className="form-main-area">
                                <div className="PostHeader">
                                    <h1 className="PostTitle">{post?.title}</h1>
                                    <div className="PostMeta">
                                        <span>
                                            작성자: <strong>{post?.authorNickname}</strong>
                                        </span>
                                        <span>| {post?.createdAt?.slice(0, 10)}</span>
                                    </div>
                                </div>

                                {post?.tripPlan && (
                                    <div className="attached-trip-box">
                                        <h3>첨부된 여행 계획</h3>

                                        <div>
                                            <strong>여행 제목:</strong> {post.tripPlan.title}
                                        </div>
                                        <div>
                                            <strong>여행지:</strong> {post.tripPlan.destination}
                                        </div>
                                        <div>
                                            <strong>기간:</strong> {post.tripPlan.startDate} ~{" "}
                                            {post.tripPlan.endDate}
                                        </div>
                                        <div>
                                            <strong>일정 개수:</strong>{" "}
                                            {post.tripPlan.schedules?.length ?? 0}개
                                        </div>

                                        {post.tripPlan.schedules &&
                                            post.tripPlan.schedules.length > 0 && (
                                                <div className="attached-trip-schedule-list">
                                                    {post.tripPlan.schedules
                                                        .slice(0, 5)
                                                        .map((schedule) => (
                                                            <div
                                                                key={schedule.id}
                                                                className="attached-trip-schedule-item"
                                                            >
                                                                <div>
                                                                    <strong>{schedule.dayNumber}일차</strong> ·{" "}
                                                                    {schedule.title}
                                                                </div>
                                                                {(schedule.startTime ||
                                                                    schedule.endTime) && (
                                                                    <div className="schedule-time">
                                                                        {schedule.startTime || "--:--"} ~{" "}
                                                                        {schedule.endTime || "--:--"}
                                                                    </div>
                                                                )}
                                                            </div>
                                                        ))}
                                                </div>
                                            )}

                                        <div className="trip-button-group">
                                            <button
                                                type="button"
                                                className="load-trip-button"
                                                onClick={() =>
                                                    navigate("/", {
                                                        state: { tripId: post.tripPlan?.id },
                                                    })
                                                }
                                            >
                                                이 여행 계획 불러오기
                                            </button>

                                            {!isAuthor && (
                                                <button
                                                    type="button"
                                                    className="join-trip-button"
                                                    onClick={handleJoinTrip}
                                                    disabled={isJoined}
                                                >
                                                    {isPending
                                                        ? "참가 신청 완료"
                                                        : isMemberApproved
                                                        ? "이미 참가 중"
                                                        : "참가 신청"}
                                                </button>
                                            )}
                                        </div>

                                        {isAuthor && post?.tripPlan && (
                                            <div className="trip-member-manage-box">
                                                <h3>참가 신청 관리</h3>

                                                {loadingMembers ? (
                                                    <div>멤버 목록 불러오는 중...</div>
                                                ) : (
                                                    <>
                                                        <div className="member-count-row">
                                                            <strong>참가 중:</strong>{" "}
                                                            {approvedMembers.length}명
                                                        </div>

                                                        { approvedMembers.length != 0 && (
                                                            approvedMembers.map((member) => (
                                                                <div
                                                                    key={member.memberId}
                                                                    className="member-manage-row"
                                                                >
                                                                    <div>
                                                                        <strong>
                                                                            {member.nickname}
                                                                        </strong>{" "}
                                                                        ({member.name})
                                                                    </div>
                                                                </div>
                                                            ))
                                                        )}

                                                        <div className="member-count-row">
                                                            <strong>대기 중:</strong>{" "}
                                                            {pendingMembers.length}명
                                                        </div>

                                                        {pendingMembers.length === 0 ? (
                                                            <div>
                                                                대기 중인 신청자가 없습니다.
                                                            </div>
                                                        ) : (
                                                            pendingMembers.map((member) => (
                                                                <div
                                                                    key={member.memberId}
                                                                    className="member-manage-row"
                                                                >
                                                                    <div>
                                                                        <strong>
                                                                            {member.nickname}
                                                                        </strong>{" "}
                                                                        ({member.name})
                                                                    </div>

                                                                    <div className="member-action-buttons">
                                                                        <button
                                                                            className="accept-button"
                                                                            onClick={() =>
                                                                                handleAcceptMember(
                                                                                    member.memberId
                                                                                )
                                                                            }
                                                                        >
                                                                            수락
                                                                        </button>
                                                                        <button
                                                                            className="reject-button"
                                                                            onClick={() =>
                                                                                handleRemoveMember(
                                                                                    member.memberId
                                                                                )
                                                                            }
                                                                        >
                                                                            거절
                                                                        </button>
                                                                    </div>
                                                                </div>
                                                            ))
                                                        )}
                                                    </>
                                                )}
                                            </div>
                                        )}
                                    </div>
                                )}

                                <div className="PostContent">
                                    <div
                                        className="ql-editor"
                                        dangerouslySetInnerHTML={{
                                            __html: post?.content || "",
                                        }}
                                    />
                                    {post?.tags && (
                                        <div className="tags">
                                            {post.tags.split(",").map((tag, idx) => (
                                                <span key={idx} className="tag">
                                                    #{tag.trim().replace("#", "")}
                                                </span>
                                            ))}
                                        </div>
                                    )}
                                </div>

                                <div className="PostFooter">
                                    <div className="footer-item">
                                        <RemoveRedEyeIcon /> {post?.viewCount}
                                    </div>
                                    <div className="footer-item">
                                        <button onClick={handleLike} className="icon-btn">
                                            <ThumbUpOffAltIcon
                                                style={{ color: liked ? "#1976d2" : "#aaa" }}
                                            />
                                        </button>
                                        {post?.likeCount}
                                    </div>
                                    <div className="footer-item">
                                        <button onClick={handleShare} className="icon-btn">
                                            <ShareIcon />
                                        </button>
                                        공유
                                    </div>
                                </div>
                            </div>
                        </article>

                        <div className="community-footer">
                            <div className="footer-left">
                                <button
                                    className="to-list-button"
                                    onClick={() => navigate("/community")}
                                >
                                    목록으로
                                </button>
                            </div>

                            <div className="footer-right">
                                {(isAuthor) && (
                                    <>
                                        <button className="edit-button" onClick={handleUpdate}>
                                            수정하기
                                        </button>
                                    </>
                                )}

                                {(isAuthor || isAdmin) && (
                                    <>
                                        <button className="delete-button" onClick={handleDelete}>
                                            삭제하기
                                        </button>
                                    </>
                                )}
                                <button
                                    className="write-button"
                                    onClick={() => navigate("/community/write")}
                                >
                                    글쓰기
                                </button>
                            </div>
                        </div>

                        <hr />

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
                                {posts.map((item) => (
                                    <div
                                        key={item.id}
                                        className={`board-item-row ${
                                            id === String(item.id) ? "active-row" : ""
                                        }`}
                                        onClick={() => navigate(`/community/${item.id}`)}
                                    >
                                        <div className="col-id">{item.id}</div>
                                        <div className="col-route">
                                            {renderRouteOrRating(item)}
                                        </div>
                                        <div className="col-title">{item.title}</div>
                                        <div className="col-author">
                                            {item.authorNickname || "익명"}
                                        </div>
                                        <div className="col-views">{item.viewCount}</div>
                                        <div className="col-stats">{item.likeCount}</div>
                                        <div className="col-share">
                                            {item.shareCount || 0}
                                        </div>
                                    </div>
                                ))}
                            </div>

                            <div className="pagination">
                                <button onClick={() => goToPage(0)} disabled={page === 0}>
                                    {"<<"}
                                </button>
                                <button
                                    onClick={() => goToPage(page - 1)}
                                    disabled={page === 0}
                                >
                                    {"<"}
                                </button>
                                {pageNumbers.map((p) => (
                                    <button
                                        key={p}
                                        onClick={() => goToPage(p)}
                                        className={page === p ? "active-page" : ""}
                                    >
                                        {p + 1}
                                    </button>
                                ))}
                                <button
                                    onClick={() => goToPage(page + 1)}
                                    disabled={page === totalPages - 1}
                                >
                                    {">"}
                                </button>
                                <button
                                    onClick={() => goToPage(totalPages - 1)}
                                    disabled={page === totalPages - 1}
                                >
                                    {">>"}
                                </button>
                            </div>
                        </div>
                    </main>
                </div>
            </div>
        </>
    );
}