export interface CommunityContent {
    route: string;
    title: string;
    content: string;
    tags: string;
    userId: number; // 유저 ID는 통상적인 Long/number 사용
}

export interface CommunityResponse {
    // 12자리 ID는 데이터 유실 방지를 위해 string 권장 (프론트에서 출력 시 편리)
    id: string;                 // 번호 (col-id)
    route: string;              // 여정 (col-route)
    title: string;              // 제목 (col-title)
    content: string;            // 본문 (상세 보기용)
    authorNickname: string;     // 작성자 닉네임 (col-author)
    tags: string;               // 태그
    viewCount: number;          // 조회 (col-views)
    recommendCount: number;     // 추천 (col-stats)
    
    // 서버에서 오는 LocalDateTime은 문자열이므로 string으로 받고 
    // 화면 표시 직전에 가공하는 것이 편합니다.
    createdAt: string;          // 날짜 (col-date)
}

/**
 * 페이징 처리를 위한 인터페이스 (백엔드의 Page<T> 응답 대응)
 */
export interface CommunityPageResponse {
    content: CommunityResponse[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number; // 현재 페이지 번호
}