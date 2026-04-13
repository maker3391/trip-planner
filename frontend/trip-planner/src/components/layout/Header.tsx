import { AppBar, Toolbar, Button } from "@mui/material";
import ShoppingCartOutlinedIcon from "@mui/icons-material/ShoppingCartOutlined";
import { useNavigate, useLocation } from "react-router-dom";
import { useState, useEffect } from "react";
import TutorialModal from "../guide/TutorialModal";
import "./Header.css";
import { CalculatorService } from "./calculator";
import Calculator from "./Calculator.tsx";
import tplanner from "../../assets/icons/tplanner2.png";
import GuidePopup from "../guide/GuidePopup.tsx";
import { getMe } from "../api/auth.ts";

export default function Header() {
  const navigate = useNavigate();
  const location = useLocation();

  const [openTutorial, setOpenTutorial] = useState(false);
  const [openGuidePopup, setOpenGuidePopup] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isCheckingAuth, setIsCheckingAuth] = useState(true);

  const match = location.pathname.match(/\d+/);
  const currentTripId = match ? parseInt(match[0], 10) : 1;

  const clearAuth = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
  };

  useEffect(() => {
    const today = new Date().toLocaleDateString("sv-SE");
    const hideGuidePopupDate = localStorage.getItem("hideGuidePopupDate");

    if (hideGuidePopupDate !== today) {
      setOpenGuidePopup(true);
    }
  }, []);

  useEffect(() => {
    let isMounted = true;

    const validateLogin = async () => {
      const token = localStorage.getItem("accessToken");

      if (!token || token === "undefined") {
        if (isMounted) {
          setIsLoggedIn(false);
          setIsCheckingAuth(false);
        }
        return;
      }

      try {
        await getMe();

        if (isMounted) {
          setIsLoggedIn(true);
        }
      } catch (error) {
        clearAuth();

        if (isMounted) {
          setIsLoggedIn(false);
        }
      } finally {
        if (isMounted) {
          setIsCheckingAuth(false);
        }
      }
    };

    setIsCheckingAuth(true);
    validateLogin();

    return () => {
      isMounted = false;
    };
  }, [location.pathname]);

  const handleLogout = async () => {
    try {
      await fetch("http://localhost:8080/api/auth/logout", {
        method: "POST",
        credentials: "include",
      });
    } catch (error) {
      console.error("백엔드 로그아웃 요청 실패:", error);
    } finally {
      clearAuth();
      setIsLoggedIn(false);
      alert("로그아웃되었습니다.");
      navigate("/login");
    }
  };

  const handleTripListClick = () => {
    if (isCheckingAuth) return;

    if (!isLoggedIn) {
      alert("로그인 후 이용 가능합니다.");
      navigate("/login");
      return;
    }

    navigate("/trip-list");
  };

  const handleCommunityClick = () => {
    if (isCheckingAuth) return;

    if (!isLoggedIn) {
      alert("로그인 후 이용 가능합니다.");
      navigate("/login");
      return;
    }

    navigate("/community");
  };

  const handleCalculatorClick = () => {
    CalculatorService.openCalculator();
  };

  return (
    <>
      <AppBar position="static" elevation={0} className="header">
        <Toolbar className="header-toolbar">
          <div className="header-logo" onClick={() => navigate("/")}>
            <img src={tplanner} alt="TPlanner" className="header-logo-img" />
          </div>

          <nav className="header-nav">
            <span onClick={() => navigate("/")}>여행 계획</span>
            <span onClick={handleTripListClick}>여행 목록</span>
            <span onClick={handleCommunityClick}>게시판</span>
            <span onClick={() => setOpenTutorial(true)}>도움말</span>
          </nav>

          <div className="header-actions">
            <span className="header-icon">
              <button
                type="button"
                onClick={handleCalculatorClick}
                className="header-icon-btn"
              >
                <ShoppingCartOutlinedIcon />
              </button>
            </span>

            {!isCheckingAuth && isLoggedIn ? (
              <>
                <Button
                  className="header-login-btn"
                  onClick={() => navigate("/mypage")}
                >
                  마이페이지
                </Button>
                <Button className="header-login-btn" onClick={handleLogout}>
                  로그아웃
                </Button>
              </>
            ) : !isCheckingAuth ? (
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
            ) : null}
          </div>
        </Toolbar>
      </AppBar>

      <GuidePopup
        open={openGuidePopup}
        onClose={() => setOpenGuidePopup(false)}
      />

      <TutorialModal
        open={openTutorial}
        onClose={() => setOpenTutorial(false)}
      />

      {currentTripId && <Calculator tripId={currentTripId} />}
    </>
  );
}