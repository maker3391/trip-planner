// 🔹 개별 게시글 데이터
export interface CommunityContent {
    route: string;
    title: string;
    authorNickname: string; // 작성자 닉네임
    content: string;
    tags: string;
    userId: number; // 유저 ID (프론트에서 필요 시 사용)
    viewCount: number; // 조회수
    recommendCount: number; // 좋아요 수
    createdAt: string; // 문자열 형태의 LocalDateTime
    updatedAt: string; // 수정된 날짜도 추가
    departure?: string; // 출발지 (선택)
    arrival?: string;   // 도착지 (선택)
    rating?: number;    // 평점 (선택)
}

// types/community.ts 추가
export interface CommunityRequest {
    category: string;
    region: string;
    title: string;
    content: string;
    departure?: string;
    arrival?: string;
    tags?: string;
    rating?: number;
    userId: number;          // 필수: 작성자 ID
    likedByUser?: boolean;  // 현재 로그인한 사용자가 좋아요를 눌렀는지
}

// 🔹 게시글 목록/페이징 응답
export interface CommunityResponse {
    id: string;                 // 게시글 ID
    category: string;           // 카테고리
    region: string;             // 지역
    title: string;              // 제목
    content: string;            // 본문
    authorNickname: string;     // 작성자 닉네임
    tags: string;               // 태그
    viewCount: number;          // 조회수
    recommendCount: number;     // 좋아요 수
    createdAt: string;          // 생성일
    updatedAt: string;          // 수정일
    departure?: string;         // 출발지
    arrival?: string;           // 도착지
    rating?: number;            // 평점
}

// 🔹 페이징 처리를 위한 인터페이스 (백엔드 Page<T> 대응)
export interface CommunityPageResponse {
    content: CommunityResponse[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number; // 현재 페이지 번호
}