import { AppBar, Toolbar, Button } from "@mui/material";
import ShoppingCartOutlinedIcon from "@mui/icons-material/ShoppingCartOutlined";
import { useNavigate } from "react-router-dom";
import { useEffect, useRef, useState } from "react";
import TutorialModal from "../guide/TutorialModal";
import "./Header.css";
import { CalculatorService } from "./calculator";
import Calculator from "./Calculator.tsx";
import { getMe } from "../api/auth.ts";
import ProfileDropdown from "../profile/ProfileDropdown.tsx";

interface UserInfo {
  id: number;
  email: string;
  name?: string;
  nickname?: string;
  phone?: string;
  role?: string;
  status?: string;
}

export default function Header() {
  const navigate = useNavigate();
  const [openTutorial, setOpenTutorial] = useState(false);
  const [openProfile, setOpenProfile] = useState(false);
  const [user, setUser] = useState<UserInfo | null>(null);
  const profileRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const token = localStorage.getItem("accessToken");

        if (!token || token === "undefined") {
          setUser(null);
          return;
        }

        const userData = await getMe();
        console.log("getMe 응답:", userData);
        setUser(userData);
      } catch (error) {
        console.error("사용자 정보 조회 실패:", error);
        setUser(null);
      }
    };

    fetchUser();
  }, []);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        profileRef.current &&
        !profileRef.current.contains(event.target as Node)
      ) {
        setOpenProfile(false);
      }
    };

    if (openProfile) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [openProfile]);

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.setItem("isLoggedIn", "false");
    setUser(null);
    setOpenProfile(false);
    alert("로그아웃되었습니다.");
    navigate("/login");
  };

  const isLoggedIn = localStorage.getItem("isLoggedIn") === "true";

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
            <span onClick={() => navigate("/community")}>커뮤니티</span>
            <span onClick={() => setOpenTutorial(true)}>도움말</span>
          </nav>

          <div className="header-actions" ref={profileRef}>
            <span className="header-icon">
              <button
                onClick={CalculatorService.openCalculator}
                style={{ background: "none", border: "none", cursor: "pointer" }}
              >
                <ShoppingCartOutlinedIcon />
              </button>
            </span>

            {isLoggedIn ? (
              <>
                <Button
                  className="header-login-btn"
                  onClick={() => navigate("/mypage")}
                >
                  마이페이지
                </Button>

                <Button
                  className="header-login-btn"
                  onClick={() => setOpenProfile((prev) => !prev)}
                >
                  내 프로필
                </Button>

                {openProfile && (
                  <ProfileDropdown
                    user={user}
                    onClose={() => setOpenProfile(false)}
                  />
                )}

                <Button className="header-login-btn" onClick={handleLogout}>
                  로그아웃
                </Button>
              </>
            ) : (
              <>
                <Button
                  className="header-login-signup-btn"
                  onClick={() => navigate("/login")}
                >
                  로그인
                </Button>
                <Button
                  className="header-login-signup-btn"
                  onClick={() => navigate("/signup")}
                >
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