import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../components/layout/Header";

export default function GoogleCallbackPage() {
  const navigate = useNavigate();
  const isHandled = useRef(false);

  useEffect(() => {
    if (isHandled.current) return;
    isHandled.current = true;

    const params = new URLSearchParams(window.location.search);
    const accessToken = params.get("accessToken");

    if (accessToken) {
      localStorage.setItem("accessToken", accessToken);
      navigate("/", { replace: true });
      return;
    }

    alert("로그인에 실패했습니다.");
    navigate("/login", { replace: true });
  }, [navigate]);

  return (
    <div style={{ minHeight: "100vh", backgroundColor: "#f3f3f3" }}>
      <Header />
      <div
        style={{
          minHeight: "calc(100vh - 82px)",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          fontSize: "18px",
          fontWeight: 600,
          color: "#333",
        }}
      >
        구글 로그인 처리 중...
      </div>
    </div>
  );
}