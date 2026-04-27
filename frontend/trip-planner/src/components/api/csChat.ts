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
  createdAt: string;
}

export interface ChatMessageResponse {
  senderId: number;
  senderNickname?: string;
  content: string;
}

export const createCSRoom = async (data: CSRoomRequest): Promise<CSRoomResponse> => {
  const response = await client.post<CSRoomResponse>("/cs/room", data);
  return response.data;
};

export const getCSRooms = async (): Promise<CSRoomResponse[]> => {
  const response = await client.get<CSRoomResponse[]>("/cs/rooms");
  return response.data; 
};

export const getMyCSRooms = async (): Promise<CSRoomResponse[]> => {
  const response = await client.get<CSRoomResponse[]>("/cs/my-rooms");
  return response.data;
};

export const getCSMessages = async (roomId: number): Promise<ChatMessageResponse[]> => {
  const response = await client.get<ChatMessageResponse[]>(`/cs/room/${roomId}/messages`);
  return response.data;
};