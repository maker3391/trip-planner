import { AppBar, Toolbar, Button, Badge, Menu, MenuItem, Typography } from "@mui/material";
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
  const [userRole, setUserRole] = useState<string | null>(null);
  const [currentUserId, setCurrentUserId] = useState<number | null>(null); // ✅ 추가

  const [notifications, setNotifications] = useState<NotificationResponseDto[]>([]);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const isNotificationOpen = Boolean(anchorEl);

  // const match = location.pathname.match(/\d+/);
  // const currentTripId = match ? parseInt(match[0], 10) : 1;

  const clearAuth = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    setUserRole(null);
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
          setUserRole(null);
          setIsCheckingAuth(false);
        }
        return;
      }

      try {
        const userData = await getMe();
        const user = await getMe();

        if (isMounted) {
          setIsLoggedIn(true);
          setCurrentUserId(userData.id);
          setUserRole(user.role);
        }
      } catch (error) {
        clearAuth();

        if (isMounted) {
          setIsLoggedIn(false);
          setCurrentUserId(null); // ✅ 로그인 실패 시 초기화
          setUserRole(null);
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
    if (!isLoggedIn || !currentUserId) return; // ✅ currentUserId도 체크

    const notiKey = `notificationHistory_${currentUserId}`; // ✅ 유저별 키

    const fetchNotifications = async () => {
      try {
        const data = await getUnreadNotifications();
        setNotifications(data);

        const existing = JSON.parse(localStorage.getItem(notiKey) || "[]");
        const newItems = data.filter(
          (serverNoti) => !existing.some((hist: any) => hist.id === serverNoti.id)
        ).map(noti => ({
          ...noti,
          receivedAt: noti.createdAt || new Date().toISOString()
        }));

        if (newItems.length > 0) {
          const updated = [...newItems, ...existing].slice(0, 50);
          localStorage.setItem(notiKey, JSON.stringify(updated)); // ✅ 유저별 키로 저장
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
            setNotifications((prev) => [newNoti, ...prev]);

            const existing = JSON.parse(localStorage.getItem(notiKey) || "[]");
            const updated = [
              { ...newNoti, receivedAt: new Date().toISOString() },
              ...existing,
            ].slice(0, 50);
            localStorage.setItem(notiKey, JSON.stringify(updated)); // ✅ 유저별 키로 저장

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
  }, [isLoggedIn, currentUserId]); // ✅ currentUserId 의존성 추가

  useEffect(() => {
  if (!isLoggedIn || !currentUserId || userRole !== "ADMIN") return;

  const notiKey = `notificationHistory_${currentUserId}`;

  const handleAdminCSNotification = (event: Event) => {
    const customEvent = event as CustomEvent<any>;
    const newNoti = customEvent.detail;

    setNotifications((prev) => {
      const alreadyExists = prev.some((noti) => noti.id === newNoti.id);

      if (alreadyExists) {
        return prev;
      }

      return [newNoti, ...prev];
    });

    const existing = JSON.parse(localStorage.getItem(notiKey) || "[]");

    const alreadySaved = existing.some((noti: any) => noti.id === newNoti.id);

    if (alreadySaved) {
      return;
    }

    const updated = [
      { ...newNoti, receivedAt: new Date().toISOString() },
      ...existing,
    ].slice(0, 50);

    localStorage.setItem(notiKey, JSON.stringify(updated));
  };

  window.addEventListener("admin-cs-notification", handleAdminCSNotification);

  return () => {
    window.removeEventListener("admin-cs-notification", handleAdminCSNotification);
  };
}, [isLoggedIn, currentUserId, userRole]);

  const handleReadNotification = async (id: number, targetUrl?: string) => {
    try {
      await readNotificationApi(id);
      setNotifications((prev) => prev.filter((noti) => noti.id !== id));
      setAnchorEl(null);

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
      setCurrentUserId(null); // ✅ 로그아웃 시 초기화
      toast.success("로그아웃되었습니다.");
      navigate("/login");
    }
  };

  const handleTripListClick = () => {
    if (isCheckingAuth) return;
    if (!isLoggedIn) {
      toast.error("로그인 후 이용 가능합니다.", { id: "community-auth" });
      navigate("/login");
      return;
    }
    navigate("/trip-list");
  };

  const handleCommunityClick = () => {
    if (isCheckingAuth) return;
    if (!isLoggedIn) {
      toast.error("로그인 후 이용 가능합니다.", { id: "community-auth" });
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
                  slotProps={{
                    paper: {
                      className: "notification-menu-paper",
                      style: { pointerEvents: "auto" }
                    }
                  }}
                  disableScrollLock  // ✅ 이것만 추가해도 스크롤 막힘은 해결됨
                >
                  {notifications.length > 0 && (
                    <MenuItem
                      onClick={async () => {
                        await Promise.all(notifications.map((n) => readNotificationApi(n.id)));
                        setNotifications([]);
                        setAnchorEl(null);
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
                {userRole === "ADMIN" ? (
                  <Button
                    className="header-login-btn"
                    onClick={() => navigate("/admin")}
                  >
                    관리자 페이지
                  </Button>
                ) : (
                  <Button
                    className="header-login-btn"
                    onClick={() => navigate("/mypage")}
                  >
                    마이페이지
                  </Button>
                )}

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