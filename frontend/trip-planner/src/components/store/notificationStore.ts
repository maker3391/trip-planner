import { create } from "zustand";
import { NotificationResponseDto } from "../api/Notification";

interface NotificationState {
  notifications: NotificationResponseDto[];
  setNotifications: (notis: NotificationResponseDto[] | ((prev: NotificationResponseDto[]) => NotificationResponseDto[])) => void;
  addNotification: (noti: NotificationResponseDto) => void;
}

export const useNotificationStore = create<NotificationState>((set) => ({
  notifications: [],
  setNotifications: (notis) => set((state) => ({
    notifications: typeof notis === 'function' ? notis(state.notifications) : notis
  })),
  addNotification: (noti) => set((state) => {
    const isDuplicate = state.notifications.some(
      (n) => String(n.id) === String(noti.id)
    );
    if (isDuplicate) return state;
    return { notifications: [noti, ...state.notifications] };
  }),
}));