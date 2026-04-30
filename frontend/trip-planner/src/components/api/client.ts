import axios from "axios";

const client = axios.create({
  baseURL: `${import.meta.env.VITE_API_URL}/api`,
  timeout: 30000,
});

let isRefreshing = false;
let refreshSubscribers: ((token: string) => void)[] = [];

// 🔥 재요청 대기열 처리
const onRefreshed = (token: string) => {
  refreshSubscribers.forEach((callback) => callback(token));
  refreshSubscribers = [];
};

const addRefreshSubscriber = (callback: (token: string) => void) => {
  refreshSubscribers.push(callback);
};

client.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem("accessToken");

    if (accessToken && accessToken !== "undefined") {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const status = error?.response?.status;
    const originalRequest = error.config;

    // 🔥 401일 때 → 토큰 재발급 시도
    if (status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      // 이미 refresh 중이면 → 대기
      if (isRefreshing) {
        return new Promise((resolve) => {
          addRefreshSubscriber((token: string) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            resolve(client(originalRequest));
          });
        });
      }

      isRefreshing = true;

      try {
        const refreshToken = localStorage.getItem("refreshToken");

        // 🔥 refresh API 호출 (서버에 맞게 수정)
        const response = await client.post("/auth/refresh", {
          refreshToken,
        });

        const newAccessToken = response.data.accessToken;

        // 🔥 토큰 저장
        localStorage.setItem("accessToken", newAccessToken);

        // 대기 중 요청들 처리
        onRefreshed(newAccessToken);

        // 원래 요청 재시도
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return client(originalRequest);
      } catch (refreshError) {
        // 🔥 refresh 실패 → 로그아웃 처리
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");

        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    // 🔥 403은 그냥 로그아웃 처리 유지
    if (status === 403) {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
    }

    return Promise.reject(error);
  }
);

export default client;