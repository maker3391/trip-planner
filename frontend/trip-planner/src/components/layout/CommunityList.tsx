import React from "react";
import { CommunityResponse } from "../../types/community.ts";
import "./CommunityList.css";

type Props = {
    posts: CommunityResponse[];
    notices: CommunityResponse[];
    page: number;
    totalPages: number;
    goToPage: (p: number) => void;
    pageNumbers: number[];
    navigate: (path: string, state?: any) => void;
    renderRouteOrRating: (post: CommunityResponse) => React.ReactNode;
    activePostId?: number | null;
    isNoticeExpanded: boolean;
    setIsNoticeExpanded: (v: boolean) => void;
    };

export default function CommunityList({
    posts, notices, page, totalPages, goToPage, pageNumbers,
    navigate, renderRouteOrRating, activePostId, isNoticeExpanded, setIsNoticeExpanded
    }: Props) {

    // 노출 개수 결정 (기본 3개 / 확장 시 전체)
    const MAX_NOTICE_LIMIT = 7;
    const noticeMaxCount = isNoticeExpanded ? MAX_NOTICE_LIMIT : 3;

    const truncateTitle = (title: string, maxLength = 30) => {
        if (!title) return "";
        return title.length > maxLength ? title.slice(0, maxLength) + "..." : title;
    };

    const formatCount = (num?: number) => (num && num > 999 ? "999+" : num || 0);

    const renderRow = (post: CommunityResponse, isNotice = false) => (
        <div
        key={post.id}
        className={`board-item-row ${activePostId === post.id ? "active-row" : ""} ${isNotice ? "notice-row" : ""}`}
        onClick={() => navigate(`/community/${post.id}`, { state: { fromPage: page } })}
        >
        <div className="col-category">{isNotice ? "최신 공지" : post.category}</div>
        <div className="col-title">
            <span className="title-text">{truncateTitle(post.title)}</span>
            {post.commentCount !== undefined && <span className="comment-count">[{post.commentCount}]</span>}
        </div>
        <div className="col-author">{post.authorNickname}</div>
        <div className="col-views">{formatCount(post.viewCount)}</div>
        <div className="col-stats">{formatCount(post.likeCount)}</div>
        <div className="col-share">{formatCount(post.shareCount)}</div>
        <div className="col-date">{post.createdAt?.split("T")[0]}</div>
        <div className="col-route">{renderRouteOrRating(post)}</div>
        </div>
    );

    return (
        <div className="community-board-container">
            <div className="board-header-row">
                <div className="col-category">분류</div><div className="col-title">제목</div>
                <div className="col-author">작성자</div><div className="col-views">조회</div>
                <div className="col-stats">좋아요</div><div className="col-share">공유</div>
                <div className="col-date">날짜</div><div className="col-route">비고</div>
            </div>

            <div className="board-body">
                <div className="notice-section">
                {notices.length === 0 ? (
                    <div className="no-notices">공지사항이 없습니다</div>
                ) : (
                    notices.slice(0, noticeMaxCount).map((notice) => renderRow(notice, true))
                )}
                </div>

                {notices.length > 3 && (
                <div className="notice-toggle-bar" onClick={() => setIsNoticeExpanded(!isNoticeExpanded)}>
                    {notices.length > 3 && (
                        <div onClick={() => setIsNoticeExpanded(!isNoticeExpanded)}>
                            {isNoticeExpanded 
                                ? "접기 ▲" 
                                : `공지 더보기 (감춰진 공지 ${Math.min(notices.length, MAX_NOTICE_LIMIT) - 3}건) ▼`
                            }
                        </div>
                    )}
                </div>
                )}

                <div className="posts-section">
                {posts.length === 0 ? <div className="no-posts">게시글이 없습니다</div> : posts.map((post) => renderRow(post))}
                </div>
            </div>

            <div className="pagination">
                <button onClick={() => goToPage(0)} disabled={page === 0}>{"<<"}</button>
                <button onClick={() => goToPage(page - 1)} disabled={page === 0}>{"<"}</button>
                {pageNumbers.map((p) => (
                <button
                    key={p}
                    onClick={() => goToPage(p)}
                    className={page === p ? "active-page" : ""}
                >
                    {p + 1}
                </button>
                ))}
                <button onClick={() => goToPage(page + 1)} disabled={page === totalPages - 1}>{">"}</button>
                <button onClick={() => goToPage(totalPages - 1)} disabled={page === totalPages - 1}>{">>"}</button>
            </div>
        </div>
    );
}