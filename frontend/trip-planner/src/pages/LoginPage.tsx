import { useState, ChangeEvent } from "react";
import type { LoginRequest } from "../types/auth"; 
import Header from "../components/layout/Header";
import kakaoIcon from "../assets/icons/kakao.png";
import googleIcon from "../assets/icons/google.png";
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
  const handleSubmit = () => {
    if (!formData.email || !formData.password) {
      alert("이메일과 비밀번호를 모두 입력해주세요.");
      return;
    }
    
    console.log("로그인 시도 데이터:", formData);
    alert(`로그인 시도: ${formData.email}`);
    // 여기서 실제 API 호출(axios 등)을 진행하면 됩니다.
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
            <img src={kakaoIcon} alt="카카오 아이콘" className="social-icon"/>
            <span className="social-button-text">Kakao 계정으로 진행하기</span>
          </button>

          <button className="social-button" onClick={() => console.log("구글 로그인 클릭")}>
            <img src={googleIcon} alt="구글 아이콘" className="social-icon"/>
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