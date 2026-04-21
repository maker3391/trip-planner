import { useState, FormEvent } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import Header from "../components/layout/Header";
import LogoIcon from "../assets/icons/logo.png";
import { confirmPasswordReset } from "../components/api/auth";
import "./LoginPage.css";

export default function ResetPasswordPage() {
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");

  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!token) {
      alert("유효하지 않은 접근입니다.");
      return;
    }

    if (newPassword !== confirmPassword) {
      alert("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
      return;
    }

    const passwordRegex = /^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\W)(?=\S+$).{8,16}$/;
    if (!passwordRegex.test(newPassword)) {
      alert("비밈ㄹ번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.");
      return;
    }

    try {
      const res = await confirmPasswordReset({token, newPassword});
      alert(res.message || "비밀번호가 성곡적으로 변경되었습니다.");
      navigate("/login");
    } catch (error: any) {
      alert(error?.response?.data?.message || "만료되었거나 잘못된 링크입니다.");
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-geader">
          <img src={LogoIcon} alt="로고" className="login-logo" />
        </div>
        <h1 className="login-title">새 비밀번호 설정</h1>
        <p className="login-subtitle">새롭게 사용할 비밀번호를 입력해 주세요.</p>

        <form onSubmit={handleSubmit}>
          <input 
            className="login-input"
            type="password" 
            placeholder="새 비밀번호 (8~16자 영문, 숫자, 특수문자)"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            />
            <input 
            className="login-input"
            type="text" 
            placeholder="새 비밀번호 확인"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            />
            <button type="submit" className="login-button">비밀번호 변경하기</button>
        </form>
      </div>
    </div>
  )
}