import { create } from "zustand";

export interface ChatMessage {
  senderId: number;
  senderNickname?: string;
  content: string;
}

interface CSInfo {
  roomId: number;
  senderId: number;
  nickname?: string;
  status?: string;
}

interface CSState {
  csInfo: CSInfo | null;
  messages: ChatMessage[];
  setCsInfo: (info: CSInfo) => void;
  addMessage: (msg: ChatMessage) => void;
  clearCsInfo: () => void;
  setMessages: (messages: ChatMessage[]) => void;
}

export const useCSStore = create<CSState>((set) => ({
  csInfo: null,
  messages: [],
  setCsInfo: (info) => set({ csInfo: info }),
  addMessage: (msg) => set((state) => ({ messages: [...state.messages, msg] })),
  clearCsInfo: () => set({ csInfo: null, messages: [] }), 
  setMessages: (messages) => set({ messages }),
}));