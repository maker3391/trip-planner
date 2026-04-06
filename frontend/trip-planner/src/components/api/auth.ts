import { LoginRequest, AuthResponse } from "../../types/auth";
import client from "./client";

export interface UserMeResponse {
  id: number;
  email: string;
  name?: string;
  nickname?: string;
  phone?: string;
  role: string;
  status: string;
}

export const getMe = async (): Promise<UserMeResponse> => {
  const token = localStorage.getItem("accessToken");

  if (!token || token === "undefined") {
    throw new Error("accessToken이 없습니다.");
  }

  const response = await client.get<UserMeResponse>("/auth/me", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return response.data;
};

export const loginApi = async (
  data: LoginRequest
): Promise<AuthResponse> => {
  const response = await client.post<AuthResponse>("/auth/login", data);
  return response.data;
};