import { create } from "zustand";
import client from "../api/client.ts"

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

import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";

// 대기열 관리를 위한 변수
let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
    failedQueue.forEach((prom) => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

client.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

        // 1. 401 에러이고 재시도한 적이 없는 경우
        if (error.response?.status === 401 && !originalRequest._retry) {
            
            // 리프레시 토큰 요청 중이면 큐에 저장하고 대기
            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                })
                    .then((token) => {
                        if (originalRequest.headers) {
                            originalRequest.headers['Authorization'] = `Bearer ${token}`;
                        }
                        return client(originalRequest);
                    })
                    .catch((err) => Promise.reject(err));
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                const refreshToken = localStorage.getItem('refreshToken');
                
                // 2. 리프레시 토큰으로 새로운 액세스 토큰 요청
                // 주의: 이 요청은 인터셉터에 걸리지 않는 별도의 axios 인스턴스나 경로를 사용해야 합니다.
                const response = await axios.post('/api/auth/refresh', {
                    refreshToken: refreshToken
                });

                const { accessToken, refreshToken: newRefreshToken } = response.data;

                // 3. 새로운 토큰 저장
                localStorage.setItem('accessToken', accessToken);
                if (newRefreshToken) {
                    localStorage.setItem('refreshToken', newRefreshToken);
                }

                // 4. API 헤더 업데이트 및 대기 중인 요청 처리
                if (originalRequest.headers) {
                    originalRequest.headers['Authorization'] = `Bearer ${accessToken}`;
                }
                
                processQueue(null, accessToken);
                return client(originalRequest); // 현재 요청 재시도

            } catch (refreshError) {
                // 5. 리프레시 토큰도 만료되었거나 오류 발생 시 완전히 로그아웃
                processQueue(refreshError, null);
                
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('isLoggedIn');
                
                window.location.href = '/login';
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }

        return Promise.reject(error);
    }
);