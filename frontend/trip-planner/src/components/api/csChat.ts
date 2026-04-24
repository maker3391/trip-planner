import client from "./client";

export interface CSRoomRequest {
  title: string;
  content: string;
}

export interface CSRoomResponse {
  id: number;
  title: string;
  userNickname: string;
  status: string;
}

export const createCSRoom = async (data: CSRoomRequest): Promise<CSRoomResponse> => {
  const response = await client.post<CSRoomResponse>("/cs/room", data);
  return response.data;
};