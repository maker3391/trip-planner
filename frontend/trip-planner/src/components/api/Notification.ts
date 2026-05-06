import client from "./client";

export interface NotificationResponseDto {
  id: number;
  message: string;
  type: string;
  isRead: boolean;
  createdAt: string;
  targetUrl: string;
}

export const getUnreadNotifications = async (): Promise<NotificationResponseDto[]> => {
  const response = await client.get<NotificationResponseDto[]>("/notifications");
  return response.data;
};

export const readNotificationApi = async (id: number): Promise<void> => {
  await client.patch(`/notifications/${id}/read`);
};

export const getAllNotifications = async (): Promise<NotificationResponseDto[]> => {
  const response = await client.get<NotificationResponseDto[]>("/notifications/history");
  return response.data;
};

export const deleteNotificationApi = async (id: number): Promise<void> => {
  await client.delete(`/notifications/${id}`);
};