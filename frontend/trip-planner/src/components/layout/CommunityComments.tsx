import React, { useEffect, useState } from "react";
import client from "../api/client";
import "./CommunityComments.css";

interface CommentItem {
    id: number;
    userId?: number;
    user_id?: number;
    authorId?: number;
    nickname?: string;
    userName?: string;
    authorNickname?: string;
    comment?: string;
    content?: string;
    createdAt: string;
    children?: CommentItem[];
    replies?: CommentItem[];
}

interface Props {
    postId: number;
    currentUserId: number;
    currentUserRole: string; 
}

export default function CommunityComments({ postId, currentUserId, currentUserRole }: Props) {
    const [comments, setComments] = useState<CommentItem[]>([]);
    const [newComment, setNewComment] = useState(""); 
    const [replyingTo, setReplyingTo] = useState<number | null>(null);
    const [replyContent, setReplyContent] = useState(""); 
    const [editingId, setEditingId] = useState<number | null>(null);
    const [editContent, setEditContent] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);

    const fetchComments = async () => {
        try {
            const response = await client.get<any>(`/community/posts/${postId}/comments?page=0&size=100`);
            const fetchedData = response.data.comments || [];
            setComments(fetchedData);
        } catch (error) {
            console.error("댓글 로딩 실패:", error);
        }
    };

    useEffect(() => { if (postId) fetchComments(); }, [postId]);

    const handleMainSubmit = async () => {
        if (!newComment.trim() || isSubmitting) return;
        if (!currentUserId) return alert("로그인 후 이용해주세요.");
        setIsSubmitting(true);
        try {
            await client.post(`/community/posts/${postId}/comments?userId=${currentUserId}`, { comment: newComment });
            setNewComment("");
            await fetchComments();
        } catch (error) { alert("등록 실패"); } finally { setIsSubmitting(false); }
    };

    const handleReplySubmit = async (parentId: number) => {
        if (!replyContent.trim() || isSubmitting) return;
        setIsSubmitting(true);
        try {
            await client.post(`/community/posts/${postId}/comments/${parentId}?userId=${currentUserId}`, { comment: replyContent });
            setReplyContent("");
            setReplyingTo(null);
            await fetchComments();
        } catch (error) { alert("답글 등록 실패"); } finally { setIsSubmitting(false); }
    };

    const handleDelete = async (commentId: number) => {
        if (isDeleting || !window.confirm("정말 영구적으로 삭제하시겠습니까? (대댓글 포함 모두 삭제)")) return;
        setIsDeleting(true);
        try {
            await client.delete(`/community/comments/${commentId}`, { params: { userId: currentUserId } });
            await fetchComments(); 
        } catch (error) { alert("삭제 실패"); } finally { setIsDeleting(false); }
    };

    const handleEditSubmit = async (commentId: number) => {
        if (!editContent.trim()) return;
        try {
            await client.put(`/community/comments/${commentId}?userId=${currentUserId}`, { comment: editContent });
            setEditingId(null);
            await fetchComments();
        } catch (error) { alert("수정 실패"); }
    };

    const initiateReply = (item: CommentItem) => {
        const displayName = item.nickname || item.userName || item.authorNickname || "사용자";
        setEditingId(null); 
        setReplyingTo(item.id);
    };

    // --- 핵심 렌더링 로직 ---
    const renderCommentCard = (item: CommentItem, isReply: boolean = false) => {
        const displayUserId = item.userId || item.user_id || item.authorId;
        const displayName = item.nickname || item.userName || item.authorNickname || "사용자";
        const displayContent = item.comment || item.content || "";
        const isEditing = editingId === item.id;
        const isReplying = replyingTo === item.id;
        const isOwner = displayUserId === currentUserId;
        const isAdmin = currentUserRole === "ADMIN";

        return (
            <div key={item.id} className={`comment-card ${isReply ? "reply" : "root"}`}>
                <div className="comment-meta">
                    <span className="author">{displayName}</span>
                    <span className="date">{new Date(item.createdAt).toLocaleString()}</span>
                </div>
                
                {isEditing ? (
                    <div className="edit-input-group">
                        <textarea 
                            value={editContent} 
                            onChange={(e) => setEditContent(e.target.value)} 
                            className="inline-textarea"
                        />
                        <div className="button-row">
                            <button onClick={() => handleEditSubmit(item.id)} className="submit-btn">저장</button>
                            <button onClick={() => setEditingId(null)}>취소</button>
                        </div>
                    </div>
                ) : (
                    <p className="content">{displayContent}</p>
                )}
                
                <div className="comment-actions">
                    {!isEditing && (
                        <>
                            <button onClick={() => initiateReply(item)}>답글달기</button>
                            {(isOwner || isAdmin) && (
                                <>
                                    {isOwner && (
                                        <button onClick={() => { setEditingId(item.id); setEditContent(displayContent); setReplyingTo(null); }}>수정</button>
                                    )}
                                    <button onClick={() => handleDelete(item.id)} className="delete-btn">삭제</button>
                                </>
                            )}
                        </>
                    )}
                </div>

                {isReplying && (
                    <div className="reply-input-group">
                        <textarea 
                            value={replyContent} 
                            onChange={(e) => setReplyContent(e.target.value)} 
                            className="inline-textarea"
                            autoFocus
                        />
                        <div className="button-row">
                            <button onClick={() => handleReplySubmit(item.id)} className="submit-btn" disabled={isSubmitting}>등록</button>
                            <button onClick={() => setReplyingTo(null)}>취소</button>
                        </div>
                    </div>
                )}
            </div>
        );
    };

    // 대댓글을 계층에 상관없이 평면적으로 렌더링하는 재귀 함수
    const renderRepliesFlattened = (replies: CommentItem[]) => {
        return replies.map(reply => (
            <React.Fragment key={reply.id}>
                {renderCommentCard(reply, true)}
                {/* 자식의 자식이 있다면 다시 이 함수를 호출 (들여쓰기 수준은 유지됨) */}
                {(reply.children || reply.replies) && (reply.children || reply.replies)!.length > 0 && (
                    renderRepliesFlattened(reply.children || reply.replies || [])
                )}
            </React.Fragment>
        ));
    };

    return (
        <div className="community-comments">
            <h3 className="comment-count-title">댓글</h3>
            <div className="comment-list">
                {comments.length === 0 ? (
                    <div className="no-comment">등록된 댓글이 없습니다.</div>
                ) : (
                    comments.map((parent) => (
                        <div key={parent.id} className="comment-group">
                            {/* 최상위 부모 댓글 */}
                            {renderCommentCard(parent, false)}
                            
                            {/* 모든 하위 댓글들을 하나의 컨테이너에 담아 평면적으로 출력 */}
                            {(parent.children || parent.replies) && (parent.children || parent.replies)!.length > 0 && (
                                <div className="replies-container">
                                    {renderRepliesFlattened(parent.children || parent.replies || [])}
                                </div>
                            )}
                        </div>
                    ))
                )}
            </div>

            <div className="comment-form main-form">
                <div className="input-group">
                    <textarea
                        value={newComment}
                        onChange={(e) => setNewComment(e.target.value)}
                        placeholder="새로운 댓글을 남겨보세요"
                        disabled={isSubmitting}
                    />
                    <button onClick={handleMainSubmit} disabled={isSubmitting || !newComment.trim()}>
                        {isSubmitting ? "등록 중..." : "등록"}
                    </button>
                </div>
            </div>
        </div>
    );
}