import { useState, ChangeEvent, FormEvent } from "react";
import type { LoginRequest } from "../types/auth";
import Header from "../components/layout/Header";
import KakaoIcon from "../assets/icons/Kakao.png";
import GoogleIcon from "../assets/icons/google.png";
import LogoIcon from "../assets/icons/logo.png";
import "./LoginPage.css";

export default function LoginPage() {
  const [formData, setFormData] = useState<LoginRequest>({
    email: "",
    password: "",
  });

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    try {
      const response = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });

      if (response.ok) {
        const data = await response.json();

        localStorage.setItem("accessToken", data.accessToken);
        localStorage.setItem("isLoggedIn", "true");

        window.location.href = "/";
      } else {
        console.error("로그인 실패");
      }
    } catch (error) {
      console.error("네트워크 오류:", error);
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/google";
  };

  const handleKakaoLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/kakao";
  };

  return (
    <div className="login-page">
      <Header />

      <div className="login-page-body">
        <div className="login-card">
          <div className="login-header">
            <img src={LogoIcon} alt="로고 아이콘" className="login-logo" />
          </div>

          <h1 className="login-title">로그인</h1>

          <p className="login-subtitle">
            이메일과 비밀번호를 이용해 로그인하세요
          </p>

          <form onSubmit={handleSubmit}>
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

            <button type="submit" className="login-button">
              로그인
            </button>
          </form>

          <a href="#" className="login-forgot">
            비밀번호를 잊으셨나요?
          </a>

          <div className="login-divider">또는</div>

          <button type="button" className="social-button" onClick={handleKakaoLogin}>
            <img src={KakaoIcon} alt="카카오 아이콘" className="social-icon" />
            <span className="social-button-text">Kakao 계정으로 진행하기</span>
          </button>

          <button type="button" className="social-button" onClick={handleGoogleLogin}>
            <img src={GoogleIcon} alt="구글 아이콘" className="social-icon" />
            <span className="social-button-text">Google 계정으로 진행하기</span>
          </button>

          <p className="login-signup">
            계정이 없으신가요? <span>지금 가입하세요</span>
          </p>
        </div>
      </div>
    </div>
  );
}