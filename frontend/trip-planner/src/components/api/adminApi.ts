import client from "./client";

export interface AdminUserResponse {
  id: number;
  nickname: string;
  email: string;
  role: string;
  status: string;
  bannedUntil: string | null;
  banReason: string | null;
}

export interface BanRequest {
  duration: number;
  reason: string;
}

export const getAllUsers = async (): Promise<AdminUserResponse[]> => {
  const response = await client.get<AdminUserResponse[]>("/admin/users");
  return response.data;
};

export const banUser = async (userId: number, data: BanRequest): Promise<void> => {
  await client.post(`/admin/user/${userId}/ban`, data);
};