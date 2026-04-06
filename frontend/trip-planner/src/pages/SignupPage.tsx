import { useState, ChangeEvent } from "react";
import type { SignupRequest } from "../types/auth";
import Header from "../components/layout/Header";
import KakaoIcon from "../assets/icons/Kakao.png";
import GoogleIcon from "../assets/icons/google.png";
import LogoIcon from "../assets/icons/logo.png";
import "./SignupPage.css";
import { useNavigate } from "react-router-dom";

export default function SignupPage() {
  // 1. 입력 데이터를 관리할 State 생성
  const [formData, setFormData] = useState<SignupRequest>({
    email: "",
    password: "",
    name: "",
    nickname: "",
    phone: "",
  });
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const navigate = useNavigate();

  // 2. 입력값이 변경될 때 호출되는 핸들러
  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // 3. 회원가입 버튼 클릭 시 호출되는 핸들러
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.email || !formData.password || !passwordConfirm || !formData.name) {
      alert("필수 내용을 모두 입력해주세요.");
      return;
    }

    if (passwordConfirm !== formData.password) {
      alert("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
      return;
    }

    if (formData.nickname === "") {
      formData.nickname = formData.name; // 닉네임이 비어있으면 이름으로 대체
    }
    
    if (formData.password.length < 8) {
      alert("비밀번호는 최소 8자 이상이어야 합니다.");
      return;
    }

    if (!/\S+@\S+\.\S+/.test(formData.email)) {
      alert("이메일 형식이 올바르지 않습니다.");
      return;
    }

    // [Step 2] 백엔드 서버와 통신 시작
    try {
      const response = await fetch("http://localhost:8080/api/auth/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData), // passwordConfirm 제외, formData만 전송
      });

      if (response.ok) {
        // ✅ 회원가입 성공 처리
        const data = await response.json(); 

        // 서버에서 토큰을 함께 내려주는 경우 (자동 로그인 처리)
        if (data.accessToken && data.refreshToken) {
          localStorage.setItem("accessToken", data.accessToken);
          localStorage.setItem("refreshToken", data.refreshToken);
          
          alert("회원가입 및 로그인이 완료되었습니다!");
          window.location.href = "/"; // 가입 후 메인 페이지로 이동
        } else {
          // 토큰이 없는 경우 (로그인 페이지로 유도)
          alert("회원가입이 성공했습니다! 로그인 해주세요.");
          window.location.href = "/login";
        }
      } else {
        // ❌ 서버 에러 응답 처리 (400, 409, 500 등)
        // 서버에서 보내주는 구체적인 에러 메시지가 있는지 확인합니다.
        try {
          const errorDetail = await response.json();
          alert(`실패: ${errorDetail.message || "입력 정보를 확인해주세요."}`);
        } catch {
          handleSignupFailure(response.status);
        }
      }
    } catch (error) {
      // ❌ 네트워크 연결 오류 (서버 통신 불가)
      console.error("네트워크 오류 발생:", error);
      handleSignupFailure(0);
    }
  };

  /**
   * HTTP 상태 코드에 따른 에러 메시지 처리
   */
  const handleSignupFailure = (status: number) => {
    let message = "회원가입에 실패했습니다. 다시 시도해주세요.";

    switch (status) {
      case 400:
        message = "입력 형식이 올바르지 않습니다. 모든 항목을 올바르게 채웠는지 확인해주세요.";
        break;
      case 409:
        message = "이미 사용 중인 이메일입니다. 다른 이메일을 사용해주세요.";
        break;
      case 500:
        message = "서버 내부에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
        break;
      case 0:
        message = "서버와 연결할 수 없습니다. 인터넷 연결이나 서버 상태를 확인해주세요.";
        break;
    }
    alert(message);
  };

  return (
    <div className="signup-page">
      <Header />

      <div className="signup-page-body">
        <div className="signup-card">
          <div className="signup-header">
            <img src={LogoIcon} alt="로고 아이콘" className="signup-logo" />
          </div>
          <h1 className="signup-title">회원가입</h1>

          <p className="signup-subtitle">
            이메일과 비밀번호를 이용해 회원가입하세요
          </p>

          {/* input에 name과 value, onChange를 연결했습니다. */}
          <input
            className="signup-input"
            type="email"
            name="email"
            placeholder="* 이메일"
            value={formData.email}
            onChange={handleChange}
          />
          <input
            className="signup-input"
            type="text"
            name="nickname"
            placeholder="* 닉네임"
            value={formData.nickname}
            onChange={handleChange}
          />
          <input
            className="signup-input"
            type="password"
            name="password"
            placeholder="* 비밀번호"
            value={formData.password}
            onChange={handleChange}
          />
          <input
            className="signup-input"
            type="password"
            name="password-confirm"
            placeholder="* 비밀번호 확인"
            value={passwordConfirm}
            onChange={(e) => setPasswordConfirm(e.target.value)}
          />
          <input
            className="signup-input"
            type="string"
            name="name"
            placeholder="* 이름"
            value={formData.name}
            onChange={handleChange}
          />
          <input
            className="signup-input"
            type="string"
            name="phone"
            placeholder="전화번호 (예: 010-1234-5678)"
            value={formData.phone}
            onChange={handleChange}
          />

          <button className="signup-button" onClick={handleSubmit}>
            회원가입
          </button>

          <div className="signup-divider">또는</div>

          <button className="social-button" onClick={() => console.log("카카오 회원가입 클릭")}>
            <img src={KakaoIcon} alt="카카오 아이콘" className="social-icon"/>
            <span className="social-button-text">Kakao 계정으로 진행하기</span>
          </button>
          <button className="social-button" onClick={() => console.log("구글 회원가입 클릭")}>
            <img src={GoogleIcon} alt="구글 아이콘" className="social-icon"/>
            <span className="social-button-text">Google 계정으로 진행하기</span>
          </button>

          <p className="signup-login">
            이미 계정이 있으신가요? <span onClick={() => navigate("/login")}>지금 로그인하세요</span>
          </p>
        </div>
      </div>
    </div>
  );
}