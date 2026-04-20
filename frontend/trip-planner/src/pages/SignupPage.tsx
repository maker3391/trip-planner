import { useState, ChangeEvent } from "react";
import { useNavigate } from "react-router-dom";
import toast, { Toaster } from "react-hot-toast"; // 1. toast 임포트
import type { SignupRequest } from "../types/auth";
import Header from "../components/layout/Header";
import KakaoIcon from "../assets/icons/Kakao.png";
import GoogleIcon from "../assets/icons/google.png";
import LogoIcon from "../assets/icons/logo.png";
import "./SignupPage.css";

export default function SignupPage() {
  const [formData, setFormData] = useState<SignupRequest>({
    email: "",
    password: "",
    name: "",
    nickname: "",
    phone: "",
  });
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const navigate = useNavigate();

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // --- 유효성 검사 (Toast 적용) ---
    if (!formData.email || !formData.password || !passwordConfirm || !formData.name) {
      toast.error("필수 내용을 모두 입력해주세요. ✍️");
      return;
    }

    if (passwordConfirm !== formData.password) {
      toast.error("비밀번호가 일치하지 않습니다. 🔒");
      return;
    }

    if (formData.password.length < 8) {
      toast.error("비밀번호는 최소 8자 이상이어야 합니다.");
      return;
    }

    if (!/\S+@\S+\.\S+/.test(formData.email)) {
      toast.error("올바른 이메일 형식이 아닙니다. @");
      return;
    }

    if (formData.nickname === "") {
      formData.nickname = formData.name;
    }

    // --- 서버 통신 ---
    try {
      const response = await fetch("http://localhost:8080/api/auth/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });

      if (response.ok) {
        const data = await response.json();

        if (data.accessToken && data.refreshToken) {
          localStorage.setItem("accessToken", data.accessToken);
          localStorage.setItem("refreshToken", data.refreshToken);
          toast.success("반가워요! 회원가입 및 로그인이 완료되었습니다! 🎉");
          setTimeout(() => navigate("/"), 1500); // 토스트를 보여줄 시간을 줍니다.
        } else {
          toast.success("회원가입 성공! 로그인 해주세요. 😊");
          setTimeout(() => navigate("/login"), 1500);
        }
      } else {
        const errorDetail = await response.json().catch(() => ({}));
        handleSignupFailure(response.status, errorDetail.message);
      }
    } catch (error) {
      console.error("네트워크 오류 발생:", error);
      toast.error("서버와 연결할 수 없습니다. 네트워크를 확인해주세요. 🌐");
    }
  };

  // 에러 메시지 처리 함수 (Toast 적용)
  const handleSignupFailure = (status: number, serverMessage?: string) => {
    if (serverMessage) {
      toast.error(`실패: ${serverMessage}`);
      return;
    }

    switch (status) {
      case 400:
        toast.error("입력 형식이 올바르지 않습니다.");
        break;
      case 409:
        toast.error("이미 사용 중인 이메일입니다. 📧");
        break;
      case 500:
        toast.error("서버 내부 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
        break;
      default:
        toast.error("회원가입에 실패했습니다.");
    }
  };

  return (
    <div className="signup-page">
      {/* 2. 토스트 컨테이너 배치 */}
      <Toaster position="top-center" reverseOrder={false} />
      <Header />

      <div className="signup-page-body">
        <div className="signup-card">
          <div className="signup-header">
            <img src={LogoIcon} alt="로고 아이콘" className="signup-logo" />
          </div>
          <h1 className="signup-title">회원가입</h1>
          <p className="signup-subtitle">이메일과 비밀번호를 이용해 회원가입하세요</p>

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
            placeholder="* 비밀번호 확인"
            value={passwordConfirm}
            onChange={(e) => setPasswordConfirm(e.target.value)}
          />
          <input
            className="signup-input"
            type="text"
            name="name"
            placeholder="* 이름"
            value={formData.name}
            onChange={handleChange}
          />
          <input
            className="signup-input"
            type="text"
            name="phone"
            placeholder="전화번호 (예: 010-1234-5678)"
            value={formData.phone}
            onChange={handleChange}
          />

          <button className="signup-button" onClick={handleSubmit}>
            회원가입
          </button>

          <div className="signup-divider">또는</div>

          <button className="social-button" onClick={() => toast("서비스 준비 중입니다! 🛠️")}>
            <img src={KakaoIcon} alt="카카오 아이콘" className="social-icon" />
            <span className="social-button-text">Kakao 계정으로 진행하기</span>
          </button>
          <button className="social-button" onClick={() => toast("서비스 준비 중입니다! 🛠️")}>
            <img src={GoogleIcon} alt="구글 아이콘" className="social-icon" />
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