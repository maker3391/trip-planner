import { useState, ChangeEvent } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast"; // Toaster는 최상위(Router)에서 한 번만 쓰기로 했으므로 삭제
import type { SignupRequest } from "../types/auth";
import Header from "../components/layout/Header";
import KakaoIcon from "../assets/icons/Kakao.png";
import GoogleIcon from "../assets/icons/google.png";
import LogoIcon from "../assets/icons/logo.png";
import TermsModal from "../components/agree/TermsModal";
import TermsSecurityModal from "../components/agree/TermsSecurityModal";
import "./SignupPage.css";

export default function SignupPage() {
  const navigate = useNavigate();

  // 1. 회원가입 입력 데이터 상태
  const [formData, setFormData] = useState<SignupRequest>({
    email: "",
    password: "",
    name: "",
    nickname: "",
    phone: "",
  });
  const [passwordConfirm, setPasswordConfirm] = useState("");

  // 2. 약관 동의 상태
  const [agreements, setAgreements] = useState({
    all: false,
    terms: false,    // 이용약관 (필수)
    privacy: false,  // 개인정보 수집 (필수)
    marketing: false // 마케팅 수신 (선택)
  });

  // 3. 모달 상태 제어 ("terms" | "privacy" | null)
  const [activeModal, setActiveModal] = useState<"terms" | "privacy" | null>(null);

  // 입력 핸들러
  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // 체크박스 핸들러
  const handleAgreementChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, checked } = e.target;

    if (name === "all") {
      setAgreements({
        all: checked,
        terms: checked,
        privacy: checked,
        marketing: checked,
      });
    } else {
      const updatedAgreements = { ...agreements, [name]: checked };
      updatedAgreements.all = 
        updatedAgreements.terms && updatedAgreements.privacy && updatedAgreements.marketing;
      setAgreements(updatedAgreements);
    }
  };

  // 회원가입 제출 핸들러
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // 유효성 검사 - 공통 ID 'signup-validation' 사용
    if (!formData.email || !formData.password || !passwordConfirm || !formData.name) {
      // 예시: 모든 toast 호출부에 duration 추가
      toast.error("필수 내용을 모두 입력해주세요. ✍️", { 
        id: "signup-validation",
        duration: 3000 // 3초 후 삭제
      });
      return;
    }

    if (passwordConfirm !== formData.password) {
      toast.error("비밀번호가 일치하지 않습니다. 🔒", { id: "signup-validation" });
      return;
    }

    if (formData.password.length < 8) {
      toast.error("비밀번호는 최소 8자 이상이어야 합니다.", { id: "signup-validation" });
      return;
    }

    if (!agreements.terms || !agreements.privacy) {
      toast.error("필수 약관에 모두 동의해주세요. ✅", { id: "signup-validation" });
      return;
    }

    if (!/\S+@\S+\.\S+/.test(formData.email)) {
      toast.error("올바른 이메일 형식이 아닙니다. @", { id: "signup-validation" });
      return;
    }

    if (formData.nickname === "") {
      formData.nickname = formData.name;
    }

    try {
      const response = await fetch("${import.meta.env.VITE_API_URL}/api/auth/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });

      if (response.ok) {
        const data = await response.json();
        // 성공 시 기존 에러 토스트들을 지움
        toast.dismiss();

        if (data.accessToken && data.refreshToken) {
          localStorage.setItem("accessToken", data.accessToken);
          localStorage.setItem("refreshToken", data.refreshToken);
          setTimeout(() => navigate("/"), 1500);
        } else {
          toast.success("회원가입 성공! 로그인 해주세요. 😊", { id: "signup-success" });
          setTimeout(() => navigate("/login"), 1500);
        }
      } else {
        const errorDetail = await response.json().catch(() => ({}));
        handleSignupFailure(response.status, errorDetail.message);
      }
    } catch (error) {
      console.error("네트워크 오류 발생:", error);
      toast.error("서버와 연결할 수 없습니다. 네트워크를 확인해주세요. 🌐", { id: "signup-network-error" });
    }
  };

  const handleSignupFailure = (status: number, serverMessage?: string) => {
    if (serverMessage) {
      toast.error(`실패: ${serverMessage}`, { id: "signup-fail" });
      return;
    }
    switch (status) {
      case 400: toast.error("입력 형식이 올바르지 않습니다.", { id: "signup-fail" }); break;
      case 409: toast.error("이미 사용 중인 이메일입니다. 📧", { id: "signup-fail" }); break;
      case 500: toast.error("서버 내부 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", { id: "signup-fail" }); break;
      default: toast.error("회원가입에 실패했습니다.", { id: "signup-fail" });
    }
  };
    const handleGoogleLogin = () => {
    window.location.href = "${import.meta.env.VITE_API_URL}/oauth2/authorization/google";
  };

  const handleKakaoLogin = () => {
    window.location.href = "${import.meta.env.VITE_API_URL}/oauth2/authorization/kakao";
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
          
          <div className="signup-form-fields">
            <input className="signup-input" type="email" name="email" placeholder="* 이메일" value={formData.email} onChange={handleChange} />
            <input className="signup-input" type="text" name="nickname" placeholder="* 닉네임" value={formData.nickname} onChange={handleChange} />
            <input className="signup-input" type="password" name="password" placeholder="* 비밀번호" value={formData.password} onChange={handleChange} />
            <input className="signup-input" type="password" placeholder="* 비밀번호 확인" value={passwordConfirm} onChange={(e) => setPasswordConfirm(e.target.value)} />
            <input className="signup-input" type="text" name="name" placeholder="* 이름" value={formData.name} onChange={handleChange} />
            <input className="signup-input" type="text" name="phone" placeholder="전화번호 (예: 010-1234-5678)" value={formData.phone} onChange={handleChange} />
          </div>

          <div className="agreement-section">
            <div className="agreement-item all-agree">
              <label>
                <input type="checkbox" name="all" checked={agreements.all} onChange={handleAgreementChange} />
                <span>전체 동의합니다.</span>
              </label>
            </div>
            <div className="agreement-divider"></div>
            
            <div className="agreement-item">
              <label>
                <input type="checkbox" name="terms" checked={agreements.terms} onChange={handleAgreementChange} />
                <span>이용약관 동의 (필수)</span>
              </label>
              <span className="view-detail" onClick={() => setActiveModal("terms")}>보기</span>
            </div>

            <div className="agreement-item">
              <label>
                <input type="checkbox" name="privacy" checked={agreements.privacy} onChange={handleAgreementChange} />
                <span>개인정보 수집 및 이용 동의 (필수)</span>
              </label>
              <span className="view-detail" onClick={() => setActiveModal("privacy")}>보기</span>
            </div>

            <div className="agreement-item">
              <label>
                <input type="checkbox" name="marketing" checked={agreements.marketing} onChange={handleAgreementChange} />
                <span>마케팅 정보 수신 동의 (선택)</span>
              </label>
            </div>
          </div>

          <button className="signup-button" onClick={handleSubmit}>
            회원가입
          </button>

          <div className="signup-divider">또는</div>

          <button
            type="button"
            className="social-button"
            onClick={handleKakaoLogin}
          >
            <img src={KakaoIcon} alt="카카오 아이콘" className="social-icon" />
            <span className="social-button-text">Kakao 계정으로 진행하기</span>
          </button>
          <button
            type="button"
            className="social-button"
            onClick={handleGoogleLogin}
          >
            <img src={GoogleIcon} alt="구글 아이콘" className="social-icon" />
            <span className="social-button-text">Google 계정으로 진행하기</span>
          </button>

          <p className="signup-login">
            이미 계정이 있으신가요? <span onClick={() => navigate("/login")}>지금 로그인하세요</span>
          </p>
        </div>
      </div>

      <TermsModal 
        open={activeModal === "terms"} 
        onClose={() => setActiveModal(null)} 
        title="이용약관" 
      />
      <TermsSecurityModal 
        open={activeModal === "privacy"} 
        onClose={() => setActiveModal(null)} 
        title="개인정보 수집 및 이용 동의" 
      />
    </div>
  );
}