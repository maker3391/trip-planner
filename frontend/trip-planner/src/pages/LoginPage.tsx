import { useState, ChangeEvent } from "react";
import type { LoginRequest } from "../types/auth";
import Header from "../components/layout/Header";
import KakaoIcon from "../assets/icons/Kakao.png";
import GoogleIcon from "../assets/icons/google.png";
import LogoIcon from "../assets/icons/logo.png";
import "./LoginPage.css";
import { useNavigate } from "react-router-dom";

export default function LoginPage() {
  // 1. 입력 데이터를 관리할 State 생성
  const [formData, setFormData] = useState<LoginRequest>({
    email: "",
    password: "",
  });

  const navigator = useNavigate();

  // 2. 입력값이 변경될 때 호출되는 핸들러
  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // 3. 로그인 버튼 클릭 시 호출되는 핸들러
  const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();

  const loginData = {
    email: formData.email,
    password: formData.password,
  };

  try {
    const response = await fetch("http://localhost:8080/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(loginData),
    });

    // 1. 에러 처리 (실패 시)
    if (!response.ok) {
      // response.json()은 한 번만 호출할 수 있으므로 주의!
      const errorDetail = await response.json();
      console.error("로그인 실패:", errorDetail);
      alert("로그인 정보가 올바르지 않습니다."); // 사용자 알림 추가
      return; // 함수 종료
    }

    if (response.ok) {
      const data = await response.json();
      
      // 1. 로컬 스토리지에 토큰 저장 (가장 먼저!)
      localStorage.setItem("accessToken", data.accessToken);
      localStorage.setItem("isLoggedIn", "true");

      // 2. 메인 페이지로 이동
      // 이동하면 Header 컴포넌트가 다시 렌더링되면서 getMe를 호출합니다.
      navigator("/"); 
    }
    
    // 3. 페이지 이동
    // Header 컴포넌트가 새로운 토큰을 인식하도록 메인으로 보냅니다.
    navigator("/"); 
    
    // 💡 만약 Header가 즉시 업데이트되지 않는다면 아래 방법을 고려하세요.
    // window.location.href = "/"; 

  } catch (error) {
    console.error("네트워크 오류:", error);
  }
};

  // 🔥 핵심: 구글 로그인 이동 함수
  const handleGoogleLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/google";
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
            <img src={KakaoIcon} alt="카카오 아이콘" className="social-icon"/>
            <span className="social-button-text">Kakao 계정으로 진행하기</span>
          </button>
          <button className="social-button" onClick={handleGoogleLogin}>
            <img src={GoogleIcon} alt="구글 아이콘" className="social-icon"/>
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