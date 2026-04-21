import { useState, FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../components/layout/Header";
import LogoIcon from "../assets/icons/logo.png"
import { requestPasswordReset } from "../components/api/auth";
import "./LoginPage.css";

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!email) {
      alert("이메일을 입력해주세요.");
      return;
    }

    setIsLoading(true);
    try {
      const res = await requestPasswordReset(email);
      alert(res.message || "이메일로 재설정 링크가 발송되었습니다.");
      navigate("/login");
    } catch (error: any) {
      alert(error?.response?.data?.message || "가입되지 않은 이메일이거나 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-page">
      <Header />
      <div className="login-page-body">
        <div className="login-card">
          <div className="login-geader">
            <img src={LogoIcon} alt="로고" className="login-logo" />
          </div>
          <h1 className="login-title">비밀번호 찾기</h1>
          <p className="login-subtitle">가입하신 이메일 주소를 입력해 주세요.</p>

          <form onSubmit={handleSubmit}>
            <input 
              className="login-input"
              type="email"
              placeholder="이메일"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={isLoading}
            />
            <button type="submit" className="login-button" disabled={isLoading}>
              {isLoading ? "발송 중..." : "재설정 링크 받기"}
            </button>
          </form>

          <p className="login-signup">
            <span onClick={() => navigate("/login")}>로그인 페이지로 돌아가기</span>
          </p>
        </div>
      </div>
    </div>
  )
}