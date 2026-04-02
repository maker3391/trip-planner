import { LoginRequest, AuthResponse } from "../../types/auth";

import client from "./client";

export interface UserMeResponse {
  id: number;
  email: string;
  name: string;
  role: string;
  status: string;
}

export const getMe = async (): Promise<UserMeResponse> => {

//   const response = await client.get<UserMeResponse>("/auth/me");
//   return response.data;
// };

  // 로컬 스토리지에서 방금 저장한 토큰을 꺼냅니다.
  const token = localStorage.getItem("accessToken");

  const response = await client.get<UserMeResponse>("/api/auth/me", {
    headers: {
      // ✅ Bearer 토큰 형식을 맞춰서 수동으로 넣어줍니다.
      Authorization: `Bearer ${token}`,
    },
  });
  return response.data;
};

export const loginApi = async (data: LoginRequest) : Promise<AuthResponse> => {
    const response = await client.post<AuthResponse>('/api/auth/login', data);
    return response.data;
}

