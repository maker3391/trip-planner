import type { AxiosError } from "axios";

interface ErrorResponse {
  message?: string;
}

const normalizeErrorMessage = (raw?: string): string => {
  if (!raw) return "";

  if (raw.includes("여행 지역을 해석하지 못했습니다")) {
    return "지역을 조금 더 구체적으로 입력해주세요.\n\n아래처럼 입력해보세요\n\n• 제주도 2박 3일 여행 추천\n• 부산 해운대 맛집 추천\n• 서울 강남 숙소 추천";
  }

  if (raw.includes("지역은 반드시 앞에 포함")) {
    return "지역을 함께 입력해주세요.\n\n아래처럼 입력해보세요\n\n• 제주도 2박 3일 여행 추천\n• 부산 해운대 맛집 추천\n• 서울 강남 숙소 추천";
  }

  if (
    raw.includes("여행 일수") ||
    raw.includes("여행 기간을 해석하지 못했습니다")
  ) {
    return "여행 기간을 함께 입력해주세요.\n\n아래처럼 입력해보세요\n\n• 제주도 2박 3일 여행 추천\n• 부산 해운대 맛집 추천\n• 서울 강남 숙소 추천";
  }

  if (raw.includes("추천 장소가 충분하지 않습니다")) {
    return "조건에 맞는 장소가 부족합니다.\n\n아래처럼 입력해보세요\n\n• 제주도 2박 3일 여행 추천\n• 부산 해운대 맛집 추천\n• 서울 강남 숙소 추천";
  }

  if (
    raw.includes("여행 일정 추천 결과가 비어 있습니다") ||
    raw.includes("일정 개수가 맞지 않습니다")
  ) {
    return "일정을 생성하는 중 문제가 발생했습니다.\n잠시 후 다시 시도해주세요.";
  }

  if (
    raw.includes("JSON") ||
    raw.includes("파싱") ||
    raw.includes("LLM")
  ) {
    return "응답을 처리하는 중 문제가 발생했습니다.\n잠시 후 다시 시도해주세요.";
  }

  return "요청을 이해하지 못했습니다.\n\n아래처럼 입력해보세요\n\n• 제주도 2박 3일 여행 추천\n• 부산 해운대 맛집 추천\n• 서울 강남 숙소 추천";
};

export const extractChatErrorMessage = (error: unknown): string => {
  const defaultMessage =
    "일시적인 문제가 발생했습니다.\n잠시 후 다시 시도해주세요.";

  const axiosError = error as AxiosError<ErrorResponse>;

  if (axiosError?.response?.data?.message) {
    return normalizeErrorMessage(axiosError.response.data.message);
  }

  if (error instanceof Error) {
    return defaultMessage;
  }

  return defaultMessage;
}; 