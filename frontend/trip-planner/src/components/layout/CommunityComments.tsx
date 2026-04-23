import { useEffect, useState } from "react";
import client from "../api/client";
import "./CommunityComments.css";

// 🔥 백엔드 DTO 구조에 맞춘 인터페이스 정의
interface CommentItem {
  id: number;
  userId: number;
  authorNickname: string;
  comment: string;
  createdAt: string;
  children?: CommentItem[]; // 백엔드에서 담아주는 대댓글 리스트 (DTO 필드명에 따라 'replies'일 수도 있음)
}

interface CommunityCommentResponse {
  result: CommentItem[]; // 백엔드 생성자 new CommunityCommentResponse(result, totalPages) 기준
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

    // 1. 댓글 조회
    const fetchComments = async () => {
        try {
        // 제네릭을 any로 두거나 새로 맞춘 인터페이스를 사용하세요
        const response = await client.get<any>(
            `/community/posts/${postId}/comments?page=0&size=100`
        );
        
        // 🔥 드디어 찾은 진짜 이름표 'comments'를 사용합니다!
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

    // 3. 삭제
    const handleDelete = async (commentId: number) => {
        if (!window.confirm("정말 삭제하시겠습니까?")) return;
        try {
        await client.delete(`/community/posts/${postId}/comments/${commentId}?userId=${currentUserId}`);
        await fetchComments();
        } catch (error) {
        alert("삭제 권한이 없거나 오류가 발생했습니다.");
        }
    };

    // 4. 단일 댓글 렌더링 헬퍼 함수
    const renderCommentCard = (item: CommentItem, isReply: boolean = false) => (
        <div 
        key={item.id} 
        className={`comment-card ${isReply ? "reply" : "root"}`}
        >
        <div className="comment-meta">
            <span className="author">{item.authorNickname || `사용자${item.userId}`}</span>
            <span className="date">{new Date(item.createdAt).toLocaleString()}</span>
        </div>
        <p className="content">{item.comment}</p>
        
        <div className="comment-actions">
            {/* 원댓글에만 '답글달기' 노출 */}
            {!isReply && (
            <button onClick={() => setReplyingTo(item.id)}>답글달기</button>
            )}
            {/* 본인 댓글만 '삭제' 노출 */}
            {item.userId === currentUserId && (
            <button onClick={() => handleDelete(item.id)} className="delete-btn">삭제</button>
            )}
        </div>
        </div>
    );

    return (
        <div className="community-comments">
            <h3>댓글</h3>

            <div className="comment-list">
                {comments.length === 0 ? (
                <div className="no-comment">등록된 댓글이 없습니다.</div>
                ) : (
                comments.map((parent) => (
                    <div key={parent.id} className="comment-group">
                    {/* 1. 부모 댓글 렌더링 */}
                    {renderCommentCard(parent, false)}
                    
                    {/* 2. 해당 부모의 대댓글들 렌더링 (있는 경우) */}
                    {parent.children && parent.children.length > 0 && (
                        <div className="replies-container">
                        {parent.children.map(child => renderCommentCard(child, true))}
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