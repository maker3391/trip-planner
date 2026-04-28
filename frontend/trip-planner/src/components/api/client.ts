import axios from "axios";

const client = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 30000,
});

let isRefreshing = false;
let refreshSubscribers: ((token: string) => void)[] = [];

<<<<<<< Updated upstream
=======
// 🔥 재요청 대기열 처리
>>>>>>> Stashed changes
const onRefreshed = (token: string) => {
  refreshSubscribers.forEach((callback) => callback(token));
  refreshSubscribers = [];
};

const addRefreshSubscriber = (callback: (token: string) => void) => {
  refreshSubscribers.push(callback);
};

<<<<<<< Updated upstream
const clearAuthAndRedirect = (message: string) => {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
  sessionStorage.setItem("authErrorMessage", message);

  if (window.location.pathname !== "/login") {
    window.location.replace("/login");
  }
};

const getErrorMessage = (error: any) => {
  const data = error?.response?.data;

  if (typeof data === "string") {
    return data;
  }

  if (data?.message) {
    return data.message;
  }

  return "인증이 만료되었습니다. 다시 로그인해주세요.";
};

=======
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
    if (status === 403) {
      clearAuthAndRedirect(getErrorMessage(error));
      return Promise.reject(error);
    }

    if (
      status === 401 &&
      originalRequest &&
      !originalRequest._retry &&
      !originalRequest.url?.includes("/auth/refresh")
    ) {
      originalRequest._retry = true;

=======
    // 🔥 401일 때 → 토큰 재발급 시도
    if (status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      // 이미 refresh 중이면 → 대기
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
        if (!refreshToken || refreshToken === "undefined") {
          clearAuthAndRedirect("로그인이 만료되었습니다. 다시 로그인해주세요.");
          return Promise.reject(error);
        }

=======
        // 🔥 refresh API 호출 (서버에 맞게 수정)
>>>>>>> Stashed changes
        const response = await client.post("/auth/refresh", {
          refreshToken,
        });

        const newAccessToken = response.data.accessToken;

<<<<<<< Updated upstream
        localStorage.setItem("accessToken", newAccessToken);

        onRefreshed(newAccessToken);

        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return client(originalRequest);
      } catch (refreshError) {
        clearAuthAndRedirect(getErrorMessage(refreshError));
=======
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

>>>>>>> Stashed changes
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
<<<<<<< Updated upstream
=======
    }

    // 🔥 403은 그냥 로그아웃 처리 유지
    if (status === 403) {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
>>>>>>> Stashed changes
    }

    return Promise.reject(error);
  }
);

export default client;