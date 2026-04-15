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

export interface MessageResponse {
  message: string;
}

export interface UpdateMeRequest {
  name?: string;
  nickname?: string;
  phone?: string;
  currentPassword?: string;
  newPassword?: string;
}

export const getMe = async (): Promise<UserMeResponse> => {
  const token = localStorage.getItem("accessToken");

  if (!token || token === "undefined") {
    throw new Error("accessToken이 없습니다.");
  }

  const response = await client.get<UserMeResponse>("/auth/me");
  return response.data;
};

export const updateMe = async (
  data: UpdateMeRequest
): Promise<MessageResponse> => {
  const response = await client.patch<MessageResponse>("/auth/me", data);
  return response.data;
};

export const loginApi = async (
  data: LoginRequest
): Promise<AuthResponse> => {
  const response = await client.post<AuthResponse>("/auth/login", data);
  return response.data;
};

export const withdrawApi = async (): Promise<MessageResponse> => {
  const response = await client.delete<MessageResponse>("/auth/withdraw");
  return response.data;
}