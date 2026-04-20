import type { AxiosError } from "axios";

interface ErrorResponse {
  message?: string;
}

export const extractChatErrorMessage = (error: unknown): string => {
  const defaultMessage =
    "답변을 불러오는 중 오류가 발생했습니다.";

  const axiosError = error as AxiosError<ErrorResponse>;

  if (axiosError?.response?.data?.message) {
    return axiosError.response.data.message;
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return defaultMessage;
};