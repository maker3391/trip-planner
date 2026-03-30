import { create } from "zustand";

type AuthState = {
  isLogin: boolean;
  token: string | null;
  login: (token: string) => void;
  logout: () => void;
};

export const useAuthStore = create<AuthState>((set) => ({
  isLogin: false,
  token: null,
  login: (token: string) => set({ isLogin: true, token }),
  logout: () => set({ isLogin: false, token: null }),
}));