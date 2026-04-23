// =========================
// 🔹 게시글 요청 (작성)
// =========================
export interface CommunityRequest {
    category: string;
    region: string;
    title: string;
    content: string;
    departure?: string;
    arrival?: string;
    tags?: string;
    rating?: number;
    imageIds?: number[]; // 🔥 추가 (백엔드랑 맞춤)
}


// =========================
// 🔹 게시글 응답 (목록/상세 공통)
// =========================
export interface CommunityResponse {
    id: number;

    category: string;
    region: string;
    title: string;
    content: string;

    // 🔥 작성자
    authorId: number;
    authorNickname: string;

    tags?: string;

    viewCount: number;
    shareCount: number;
    likeCount: number;

    // 🔥 추가
    commentCount: number;

    likedByMe: boolean;

    createdAt: string;
    updatedAt: string;

    departure?: string;
    arrival?: string;
    rating?: number;

    imageIds: number[];

    tripPlan?: TripPlanResponse | null;
}

// =========================
// 🔹 페이징 응답
// =========================
export interface CommunityPageResponse {
    content: CommunityResponse[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
}


// =========================
// 🔹 상세 (동일 구조)
export type CommunityDetailResponse = CommunityResponse;


// =========================
// 🔹 좋아요 응답
// =========================
export interface LikeResponse {
    liked: boolean;
    likeCount: number;
}

export interface TripScheduleResponse {
    id: number;
    dayNumber: number;
    title: string;
    visitOrder: number;
    startTime?: string;
    endTime?: string;
    memo?: string;
    estimatedStayMinutes?: number;
    placeId?: number;
    placeName?: string;
    placeAddress?: string;
    latitude?: number;
    longitude?: number;
    googlePlaceId?: string;
    pinColor?: string;
    selectedPinColor?: string;
    lineColor?: string;
}

export interface TripPlanResponse {
    id: number;
    ownerId?: number;
    title: string;
    destination: string;
    startDate: string;
    endDate: string;
    status?: string;
    createdAt?: string;
    schedules?: TripScheduleResponse[];
}