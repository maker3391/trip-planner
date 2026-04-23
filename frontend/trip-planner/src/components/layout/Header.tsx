import { AppBar, Toolbar, Button, IconButton, Badge, Menu, MenuItem, Box, Typography } from "@mui/material";
import ShoppingCartOutlinedIcon from "@mui/icons-material/ShoppingCartOutlined";
import NotificationsNoneIcon from "@mui/icons-material/NotificationsNone"; // 종 아이콘 추가
import { useNavigate, useLocation } from "react-router-dom";
import { useState, useEffect } from "react";
import TutorialModal from "../guide/TutorialModal";
import "./Header.css";
import tplanner from "../../assets/icons/tplanner2.png";
import GuidePopup from "../guide/GuidePopup.tsx";
import { getMe } from "../api/auth.ts";
import toast from "react-hot-toast";

export default function Header() {
  const navigate = useNavigate();
  const location = useLocation();

  const [openTutorial, setOpenTutorial] = useState(false);
  const [openGuidePopup, setOpenGuidePopup] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isCheckingAuth, setIsCheckingAuth] = useState(true);

  // --- 알림(Notification) 관련 상태 추가 ---
  const [anchorEl, setAnchorEl] = useState(null);
  const openNotifications = Boolean(anchorEl);

  const handleNotificationClick = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleNotificationClose = () => {
    setAnchorEl(null);
  };
  // -------------------------------------

  const isMainPage = location.pathname === "/";
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
        if (isMounted) setIsLoggedIn(true);
      } catch (error) {
        clearAuth();
        if (isMounted) setIsLoggedIn(false);
      } finally {
        if (isMounted) setIsCheckingAuth(false);
      }
    };
    setIsCheckingAuth(true);
    validateLogin();
    return () => { isMounted = false; };
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
      navigate("/login");
    }
  };

  const handleTripListClick = () => {
    if (isCheckingAuth) return;
    if (!isLoggedIn) {
      toast.error("로그인 후 이용 가능합니다.", { id: "login-required" });
      navigate("/login");
      return;
    }
    navigate("/trip-list");
  };

  const handleCommunityClick = () => {
    if (isCheckingAuth) return;
    if (!isLoggedIn) {
      toast.error("로그인 후 이용 가능합니다.", { id: "login-required" });
      navigate("/login");
      return;
    }
    navigate("/community");
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
            {!isCheckingAuth && isLoggedIn ? (
              <>
                {/* 1. 알림 종 아이콘 (마이페이지 왼쪽) */}
                <IconButton 
                  onClick={handleNotificationClick}
                  sx={{ color: '#333', marginRight: '8px' }}
                >
                  <Badge badgeContent={0} color="error">
                    <NotificationsNoneIcon />
                  </Badge>
                </IconButton>

                {/* 2. 알림 드롭다운 메뉴 */}
                <Menu
                  anchorEl={anchorEl}
                  open={openNotifications}
                  onClose={handleNotificationClose}
                  PaperProps={{
                    sx: {
                      width: 280,
                      maxHeight: 400,
                      mt: 1.5,
                      boxShadow: '0px 5px 15px rgba(0,0,0,0.1)',
                      borderRadius: '10px'
                    }
                  }}
                  transformOrigin={{ horizontal: 'right', vertical: 'top' }}
                  anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
                >
                  <Box sx={{ p: 2, borderBottom: '1px solid #f0f0f0' }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>알림</Typography>
                  </Box>
                  <MenuItem sx={{ py: 3, justifyContent: 'center', cursor: 'default' }}>
                    <Typography variant="body2" color="text.secondary">
                      새로운 알림이 없습니다.
                    </Typography>
                  </MenuItem>
                </Menu>

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
                <Button className="header-login-signup-btn" onClick={() => navigate("/login")}>
                  로그인
                </Button>
                <Button className="header-login-signup-btn" onClick={() => navigate("/signup")}>
                  회원가입
                </Button>
              </>
            ) : null}
          </div>
        </Toolbar>
      </AppBar>

      <GuidePopup open={openGuidePopup} onClose={() => setOpenGuidePopup(false)} />
      <TutorialModal open={openTutorial} onClose={() => setOpenTutorial(false)} />
    </>
  );
}