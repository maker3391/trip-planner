import { AppBar, Toolbar, Button } from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import ShoppingCartOutlinedIcon from "@mui/icons-material/ShoppingCartOutlined";
import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import TutorialModal from "../guide/TutorialModal";
import "./Header.css";
import { CalculatorService } from "./calculator";
import Calculator from "./Calculator.tsx";
import { getMe } from "../api/auth.ts";

interface UserInfo {
  id: number;
  email: string;
  name: string;
  role: string;
  status: string;
}

export default function Header() {
  // 초기화
  const navigate = useNavigate();
  const [openTutorial, setOpenTutorial] = useState(false);
  const [user, setUser] = useState<UserInfo | null>(null);


  useEffect(() => {
    const fetchUser = async () => {
      const token = localStorage.getItem("accessToken");

      if (!token) {
        setUser(null);
        return;
      } else {
        fetchUser();
      }

      try {
        const userData = await getMe();
        setUser(userData);
      } catch (error) {
        console.error("사용자 정보 조회 실패:", error);
        localStorage.removeItem("accessToken");
        setUser(null);
      }
    };

    fetchUser();
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("isLoggedIn");
    setUser(null);
    alert("로그아웃되었습니다.");
    navigate("/login");
  };

  return (
    <>
      <AppBar position="static" elevation={0} className="header">
        <Toolbar className="header-toolbar">
          <div className="header-logo" onClick={() => navigate("/")}>
            TPlanner
          </div>

          <nav className="header-nav">
            <span>여행 계획</span>
            <span>패스</span>
            <span>좌석 예약</span>
            <span onClick={() => setOpenTutorial(true)}>도움말</span>
          </nav>

          <div className="header-actions">
            <span className="header-icon">
              <SearchIcon />
            </span>
            <span className="header-icon">
              <button onClick={CalculatorService.openCalculator}
                style={{ background: "none", border: "none", cursor: "pointer" }}>
                <ShoppingCartOutlinedIcon />
              </button>
            </span>
            {localStorage.getItem("isLoggedIn") ? (
              <>
                <Button
                  className="header-login-btn"
                  onClick={() => navigate("/mypage")}
                >
                  {user?.name}님
                </Button>
                <Button className="header-login-btn" onClick={handleLogout}>
                  로그아웃
                </Button>
              </>
            ) : (
              <>
                <Button
                  className="header-login-btn"
                  onClick={() => navigate("/login")}
                >
                  로그인
                </Button>
                <Button
                  className="header-login-signup-btn"
                  onClick={() => navigate("/signup")}>
                  회원가입
                </Button>
              </>
            )}

          </div>
        </Toolbar>
      </AppBar>

      <TutorialModal
        open={openTutorial}
        onClose={() => setOpenTutorial(false)}
      />
      <Calculator />
    </>
  );
}