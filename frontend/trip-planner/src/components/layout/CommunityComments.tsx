import { useEffect, useState } from "react";
import client from "../api/client";
import "./CommunityComments.css";

// 🔥 백엔드에서 어떤 이름으로 데이터를 주더라도 다 받을 수 있게 설계
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
  isDeleted?: boolean; 
  deleted?: boolean;   
}

interface CommunityCommentResponse {
  result: CommentItem[];
  totalPages: number;
}

interface Props {
  postId: number;
  currentUserId: number; 
}

export default function CommunityComments({ postId, currentUserId }: Props) {
    const [comments, setComments] = useState<CommentItem[]>([]);
    const [newComment, setNewComment] = useState("");
    const [replyingTo, setReplyingTo] = useState<number | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const [editingId, setEditingId] = useState<number | null>(null);
    const [editContent, setEditContent] = useState("");

    // 1. 댓글 조회
    const fetchComments = async () => {
        try {
            const response = await client.get<any>(
                `/community/posts/${postId}/comments?page=0&size=100`
            );
            
            const fetchedData = response.data.comments || [];
            setComments(fetchedData);
        } catch (error) {
            console.error("댓글 로딩 실패:", error);
        }
    };

    useEffect(() => {
        if (postId) fetchComments();
    }, [postId]);

    // 2. 등록
    const handleSubmit = async () => {
        if (!newComment.trim() || isSubmitting) return;

        if (!currentUserId || currentUserId === 0) {
            alert("로그인 후 이용해주세요.");
            return;
        }

        setIsSubmitting(true);
        try {
            if (replyingTo) {
                // 대댓글 API
                await client.post(
                    `/community/posts/${postId}/comments/${replyingTo}?userId=${currentUserId}`,
                    { comment: newComment }
                );
            } else {
                // 일반 댓글 API
                await client.post(
                    `/community/posts/${postId}/comments?userId=${currentUserId}`,
                    { comment: newComment }
                );
            }

            setNewComment("");
            setReplyingTo(null);
            await fetchComments(); // 등록 후 목록 리로드
        } catch (error) {
            alert("댓글 등록에 실패했습니다.");
        } finally {
            setIsSubmitting(false);
        }
    };

    // 3. 삭제 (🔥 백엔드 컨트롤러에 맞춰 URL 수정됨!)
    const handleDelete = async (commentId: number) => {
        if (!window.confirm("정말 삭제하시겠습니까?")) return;
        try {
            // 수정된 URL: /community/comments/${commentId}?userId=${currentUserId}
            await client.delete(`/community/comments/${commentId}?userId=${currentUserId}`);
            await fetchComments();
        } catch (error) {
            alert("삭제 권한이 없거나 오류가 발생했습니다.");
        }
    };

    // 3-1. 수정 완료 처리 (🔥 백엔드에 PUT/PATCH 엔드포인트가 추가되어야 정상 작동함)
    const handleEditSubmit = async (commentId: number) => {
        if (!editContent.trim()) return;
        try {
            // 주의: 백엔드 CommunityController에는 아직 댓글 수정 API(@PutMapping 등)가 없습니다.
            // 백엔드 구현 형태에 따라 아래 URL은 변경되어야 합니다.
            await client.put(`/community/comments/${commentId}?userId=${currentUserId}`, {
                comment: editContent
            });
            setEditingId(null);
            setEditContent("");
            await fetchComments();
        } catch (error) {
            alert("수정 권한이 없거나 오류가 발생했습니다.");
        }
    };
    
    // 4. 단일 댓글 렌더링 헬퍼 함수
    const renderCommentCard = (item: CommentItem, isReply: boolean = false) => {
        // 🔥 논리적 삭제 처리
        if (item.isDeleted || item.deleted) {
            return (
                <div key={item.id} className={`comment-card ${isReply ? "reply" : "root"}`} style={{ padding: '15px', color: '#999', backgroundColor: '#f9f9f9' }}>
                    <p className="content" style={{ margin: 0, fontStyle: 'italic' }}>삭제된 댓글입니다.</p>
                </div>
            );
        }

        const displayUserId = item.userId || item.user_id || item.authorId;
        const displayName = item.nickname || item.userName || item.authorNickname || "사용자";
        const displayContent = item.comment || item.content || "내용이 없습니다.";
        
        const isEditing = editingId === item.id;

        return (
            <div 
                key={item.id} 
                className={`comment-card ${isReply ? "reply" : "root"}`}
            >
                <div className="comment-meta">
                    <span className="author">{displayName}</span>
                    <span className="date">{new Date(item.createdAt).toLocaleString()}</span>
                </div>
                
                {isEditing ? (
                    <div className="edit-input-group">
                        <textarea 
                            value={editContent} 
                            onChange={(e) => setEditContent(e.target.value)} 
                            style={{ width: "100%", minHeight: "60px", marginBottom: "10px" }}
                        />
                        <div style={{ display: "flex", gap: "5px" }}>
                            <button onClick={() => handleEditSubmit(item.id)}>저장</button>
                            <button onClick={() => { setEditingId(null); setEditContent(""); }}>취소</button>
                        </div>
                    </div>
                ) : (
                    <p className="content">{displayContent}</p>
                )}
                
                <div className="comment-actions">
                    {!isReply && !isEditing && (
                        <button onClick={() => setReplyingTo(item.id)}>답글달기</button>
                    )}
                    {displayUserId === currentUserId && !isEditing && (
                        <>
                            <button onClick={() => { 
                                setEditingId(item.id); 
                                setEditContent(displayContent); 
                            }}>수정</button>
                            <button onClick={() => handleDelete(item.id)} className="delete-btn">삭제</button>
                        </>
                    )}
                </div>
            </div>
        );
    };

    return (
        <div className="community-comments">
            <h3>댓글</h3>

            <div className="comment-list">
                {comments.length === 0 ? (
                    <div className="no-comment">등록된 댓글이 없습니다.</div>
                ) : (
                    comments.map((parent) => (
                        <div key={parent.id} className="comment-group">
                            {renderCommentCard(parent, false)}
                            
                            {(parent.children || parent.replies) && (parent.children || parent.replies)!.length > 0 && (
                                <div className="replies-container">
                                    {(parent.children || parent.replies)!.map(child => renderCommentCard(child, true))}
                                </div>
                            )}
                        </div>
                    ))
                )}
            </div>

            <div className="comment-form">
                {replyingTo && (
                    <div className="reply-indicator">
                        답글 작성 중... <button onClick={() => setReplyingTo(null)}>취소</button>
                    </div>
                )}
                <div className="input-group">
                    <textarea
                        value={newComment}
                        onChange={(e) => setNewComment(e.target.value)}
                        placeholder={replyingTo ? "답글을 입력하세요" : "댓글을 입력하세요"}
                        disabled={isSubmitting}
                    />
                    <button 
                        onClick={handleSubmit} 
                        disabled={isSubmitting || !newComment.trim()}
                    >
                        {isSubmitting ? "등록 중..." : "등록"}
                    </button>
                </div>
            </div>
        </div>
    );
}