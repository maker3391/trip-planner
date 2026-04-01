import axios from "axios";

const client = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 5000,
});

client.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem("accessToken");

    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

export default client;