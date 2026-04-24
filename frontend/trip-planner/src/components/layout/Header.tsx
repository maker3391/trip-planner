import { AppBar, Toolbar, Button, Badge, Menu, MenuItem, Typography, Box } from "@mui/material";
import NotificationsNoneOutlinedIcon from "@mui/icons-material/NotificationsNoneOutlined";
import { useNavigate, useLocation } from "react-router-dom";
import { useState, useEffect } from "react";
import TutorialModal from "../guide/TutorialModal";
import "./Header.css";

import tplanner from "../../assets/icons/tplanner2.png";
import GuidePopup from "../guide/GuidePopup.tsx";
import { getMe } from "../api/auth.ts";
import { getUnreadNotifications, NotificationResponseDto, readNotificationApi } from "../api/Notification.ts";
import { fetchEventSource } from "@microsoft/fetch-event-source";

// react-hot-toast를 사용 중이라면 임포트 (기존 alert 대체용)
import toast from "react-hot-toast";

export default function Header() {
  const navigate = useNavigate();
  const location = useLocation();

  const [openTutorial, setOpenTutorial] = useState(false);
  const [openGuidePopup, setOpenGuidePopup] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isCheckingAuth, setIsCheckingAuth] = useState(true);

  const[notifications, setNotifications] = useState<NotificationResponseDto[]>([]);
  const[anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const isNotificationOpen = Boolean(anchorEl);

  // 현재 페이지가 메인 페이지인지 확인 (경로가 "/" 인 경우)
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

  useEffect(() => {
    if (!isLoggedIn) return;

    const fetchNotifications = async () => {
      try {
        const data = await getUnreadNotifications();
        setNotifications(data);

        // ✅ [추가] 서버에서 가져온 미확인 알림들을 히스토리에 병합
        const existing = JSON.parse(localStorage.getItem("notificationHistory") || "[]");
        
        // 중복 제거 (이미 히스토리에 있는 ID는 제외)
        const newItems = data.filter(
          (serverNoti) => !existing.some((hist: any) => hist.id === serverNoti.id)
        ).map(noti => ({
          ...noti,
          receivedAt: noti.createdAt || new Date().toISOString() // 시간 데이터 없으면 현재시간
        }));

        if (newItems.length > 0) {
          const updated = [...newItems, ...existing].slice(0, 50);
          localStorage.setItem("notificationHistory", JSON.stringify(updated));
        }
      } catch (error) {
        console.error("알림 목록 조회 실패:", error);
      }
    };
    fetchNotifications();

    const token = localStorage.getItem("accessToken");
    const abortController = new AbortController();

    const connectSSE = async () => {
      await fetchEventSource("http://localhost:8080/api/notifications/subscribe", {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: "text/event-stream",
        },
        signal: abortController.signal,
        onmessage(ev) {
          if (ev.data.includes("EventStream Created")) return;
          
          try {
            const newNoti = JSON.parse(ev.data);

            toast(newNoti.message, { icon: "🔔", duration: 3000 });

            // 실시간 뱃지용
            setNotifications((prev) => [newNoti, ...prev]);

            // ↓ 이 부분 추가 - MyPage 히스토리에 즉시 저장
            const existing = JSON.parse(localStorage.getItem("notificationHistory") || "[]");
            const updated = [
              { ...newNoti, receivedAt: new Date().toISOString() },
              ...existing,
            ].slice(0, 50);
            localStorage.setItem("notificationHistory", JSON.stringify(updated));

          } catch (error) {
            console.error("알림 데이터 파싱 오류:", error);
          }
        },
        onerror(err) {
          console.error("SSE 연결 에러:", err);
          throw err;
        },
      });
    };

    connectSSE();

    return () => abortController.abort();
  }, [isLoggedIn]);

  const handleReadNotification = async (id: number, targetUrl?: string) => {
    try {
      await readNotificationApi(id); // 서버에 읽음 알림
      
      // ✅ 헤더 알림 목록(뱃지 숫자)에서만 제거
      setNotifications((prev) => prev.filter((noti) => noti.id !== id));
      setAnchorEl(null);

      // 여기서 localStorage를 건드리지 마! 그래야 MyPage 기록이 보존돼.
      if (targetUrl) {
        navigate(targetUrl);
      }
    } catch (error) {
      console.error("알람 읽음 처리 실패:", error);
    }
  };

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
      toast.success("로그아웃되었습니다.");
      navigate("/login");
    }
  };

  const handleTripListClick = () => {
    if (isCheckingAuth) return;
    if (!isLoggedIn) {
      toast.error("로그인 후 이용 가능합니다.");
      navigate("/login");
      return;
    }
    navigate("/trip-list");
  };

  const handleCommunityClick = () => {
    if (isCheckingAuth) return;
    if (!isLoggedIn) {
      toast.error("로그인 후 이용 가능합니다.");
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

            {!isCheckingAuth && isLoggedIn && (
              <>
                <span className="header-icon">
                  <button
                    type="button"
                    className="header-icon-btn"
                    onClick={(e) => setAnchorEl(e.currentTarget)}
                  >
                    <Badge badgeContent={notifications.length} color="error">
                      <NotificationsNoneOutlinedIcon />
                    </Badge>
                  </button>
                </span>

                <Menu
                  anchorEl={anchorEl}
                  open={isNotificationOpen}
                  onClose={() => setAnchorEl(null)}
                  PaperProps={{className: "notification-menu-paper"}}
                >
                  {notifications.length > 0 && (
                    <MenuItem
                      onClick={async () => {
                        // 1. 서버 API 호출
                        await Promise.all(notifications.map((n) => readNotificationApi(n.id)));
                        
                        // 2. 헤더 상태 비우기 (뱃지 사라짐)
                        setNotifications([]); 
                        setAnchorEl(null);
                        
                        // 💡 saveToHistory 호출 절대 금지! (이미 SSE 받을 때 저장됐음)
                      }}
                      className="notification-mark-all"
                    >
                      전체 읽음
                    </MenuItem>
                  )}
                     
                  {notifications.length === 0 ? (
                    <MenuItem disabled>새로운 알림이 없습니다.</MenuItem>
                  ) : (
                    notifications.map((noti) => (
                      <MenuItem
                        key={noti.id}
                        onClick={() => handleReadNotification(noti.id, noti.targetUrl)}
                        className="notification-item"
                      >
                        <Typography variant="body2">{noti.message}</Typography>
                      </MenuItem>
                    ))
                  )}
                </Menu>
              </>
            )}

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
    </>
  );
}