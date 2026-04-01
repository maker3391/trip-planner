import { useState, ChangeEvent } from "react";
import axios from "axios";
import type { LoginRequest } from "../types/auth";
import Header from "../components/layout/Header";
import "./LoginPage.css";

export default function LoginPage() {
  // 1. 입력 데이터를 관리할 State 생성
  const [formData, setFormData] = useState<LoginRequest>({
    email: "",
    password: "",
  });

  // 2. 입력값이 변경될 때 호출되는 핸들러
  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // 3. 로그인 버튼 클릭 시 호출되는 핸들러
  const handleSubmit = async () => {
  if (!formData.email || !formData.password) {
    alert("이메일과 비밀번호를 모두 입력해주세요.");
    return;
  }

  try {
    const response = await axios.post("http://localhost:8080/api/auth/login", {
      email: formData.email,
      password: formData.password,
    });

    console.log("서버 응답:", response.data);

    // ✅ 토큰 저장 (핵심)
    if (response.data.token) {
      localStorage.setItem("token", response.data.token);
    }

    alert("로그인 성공 🎉");

  } catch (error: unknown) {
    console.error("로그인 실패:", error);

    if (axios.isAxiosError(error) && error.response) {
      const errorData = error.response.data as { message?: string };
      alert(errorData.message || "로그인 실패");
    } else {
      alert("서버 연결 실패");
    }
  }
};

  return (
    <div className="login-page">
      <Header />

      <div className="login-page-body">
        <div className="login-card">
          <h1 className="login-title">로그인</h1>

          <p className="login-subtitle">
            이메일과 비밀번호를 이용해 로그인하세요
          </p>

          {/* input에 name과 value, onChange를 연결했습니다. */}
          <input
            className="login-input"
            type="email"
            name="email"
            placeholder="이메일"
            value={formData.email}
            onChange={handleChange}
          />
          <input
            className="login-input"
            type="password"
            name="password"
            placeholder="비밀번호"
            value={formData.password}
            onChange={handleChange}
          />

          <button className="login-button" onClick={handleSubmit}>
            로그인
          </button>

          <a href="#" className="login-forgot">
            비밀번호를 잊으셨나요?
          </a>

          <div className="login-divider">또는</div>

          <button className="social-button" onClick={() => console.log("카카오 로그인 클릭")}>
            Kakao 계정으로 진행하기
          </button>
          <button className="social-button" onClick={() => console.log("구글 로그인 클릭")}>
            Google 계정으로 진행하기
          </button>

          <p className="login-signup">
            계정이 없으신가요? <span>지금 가입하세요</span>
          </p>
        </div>
      </div>
    </div>
  );
}