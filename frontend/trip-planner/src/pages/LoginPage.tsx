import Header from "../components/layout/Header";
import "./LoginPage.css";

export default function LoginPage() {
  return (
    <div className="login-page">
      <Header />

      <div className="login-page-body">
        <div className="login-card">
          <h1 className="login-title">로그인</h1>

          <p className="login-subtitle">
            이메일과 비밀번호를 이용해 로그인하세요
          </p>

          <input className="login-input" type="email" placeholder="이메일" />
          <input className="login-input" type="password" placeholder="비밀번호" />

          <button className="login-button">로그인</button>

          <a href="#" className="login-forgot">
            비밀번호를 잊으셨나요?
          </a>

          <div className="login-divider">또는</div>

          <button className="social-button">Kakao 계정으로 진행하기</button>
          <button className="social-button">Google 계정으로 진행하기</button>

          <p className="login-signup">
            계정이 없으신가요? <span>지금 가입하세요</span>
          </p>
        </div>
      </div>
    </div>
  );
}