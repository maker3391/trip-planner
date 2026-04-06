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

  const navigate = useNavigate();

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

    try {
      const response = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });

      if (response.ok) {
        const data = await response.json();
        
        // 1. 토큰 저장 (가장 먼저!)
        localStorage.setItem("accessToken", data.accessToken);
        localStorage.setItem("isLoggedIn", "true");

        // 2. ⚡️ 가장 확실한 방법: 새로고침 이동
        // navigate("/") 대신 아래를 쓰면 Header가 처음부터 다시 시작하며 500 에러를 안 냅니다.
        window.location.href = "/"; 
    } else {
        console.error("로그인 실패");
        // 3. 로그인 실패 시 사용자에게 알림 (선택 사항)
        handleLoginFailure(response.status);
      }
    } catch (error) {
      console.error("네트워크 오류:", error);
    }
  };

  const handleLoginFailure = (status: number) => {
    let message = "로그인에 실패했습니다. 다시 시도해주세요.";

    switch (status) {
      case 400:
        message = "잘못된 요청입니다. 입력한 정보를 확인해주세요.";
        break;
      case 401:
        message = "이메일 또는 비밀번호가 올바르지 않습니다.";
        break;
      case 500:
        message = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        break;
      default:
        message = `로그인에 실패했습니다. (상태 코드: ${status})`;
    }
    alert(message);
  };
  

  // 🔥 핵심: 구글 로그인 이동 함수
  const handleGoogleLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/google";
  };

  const handleKakaoLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/kakao";
  }


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

          <button className="social-button" onClick={handleKakaoLogin}>
            <img src={KakaoIcon} alt="카카오 아이콘" className="social-icon"/>
            <span className="social-button-text">Kakao 계정으로 진행하기</span>
          </button>
          <button className="social-button" onClick={handleGoogleLogin}>
            <img src={GoogleIcon} alt="구글 아이콘" className="social-icon"/>
            <span className="social-button-text">Google 계정으로 진행하기</span>
          </button>

          <p className="login-signup">
            계정이 없으신가요? <span onClick={() => navigate("/signup")}>지금 가입하세요</span>
          </p>
        </div>
      </div>
    </div>
  );
}