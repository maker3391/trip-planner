import client from "./client";

export interface UserMeResponse {
  id: number;
  email: string;
  name: string;
  role: string;
  status: string;
}

export const getMe = async (): Promise<UserMeResponse> => {
  const response = await client.get<UserMeResponse>("/auth/me");
  return response.data;
};