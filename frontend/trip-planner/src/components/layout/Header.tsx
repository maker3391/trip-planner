import { AppBar, Toolbar, Button, Badge, Menu, MenuItem, Typography } from "@mui/material";
import NotificationsNoneOutlinedIcon from "@mui/icons-material/NotificationsNoneOutlined";
import { useNavigate, useLocation } from "react-router-dom";
import { useState, useEffect } from "react";
import { fetchEventSource } from "@microsoft/fetch-event-source";
import TutorialModal from "../guide/TutorialModal";
import "./Header.css";

import tplanner from "../../assets/icons/tplanner2.png";
import GuidePopup from "../guide/GuidePopup";
import { getMe } from "../api/auth";
import { getUnreadNotifications, readNotificationApi } from "../api/Notification";
import { useNotificationStore } from "../store/notificationStore";
import toast from "react-hot-toast";

export default function Header() {
    const navigate = useNavigate();
    const location = useLocation();

    const [openTutorial, setOpenTutorial] = useState(false);
    const [openGuidePopup, setOpenGuidePopup] = useState(false);
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [isCheckingAuth, setIsCheckingAuth] = useState(true);
    const [userRole, setUserRole] = useState<string | null>(null);
    const [currentUserId, setCurrentUserId] = useState<number | null>(null);

    const notifications = useNotificationStore((state) => state.notifications);
    const setNotifications = useNotificationStore((state) => state.setNotifications);
    const addNotification = useNotificationStore((state) => state.addNotification);
    
    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
    const isNotificationOpen = Boolean(anchorEl);
    const isAdmin = userRole === "ADMIN" || userRole === "ROLE_ADMIN";

    const getNotiKey = (userId: number) => `notificationHistory_${userId}`;

    // 1. 유저 인증 상태 확인
    useEffect(() => {
        let isMounted = true;
        const validateLogin = async () => {
            const token = localStorage.getItem("accessToken");
            if (!token || token === "undefined") {
                if (isMounted) {
                    setIsLoggedIn(false);
                    setCurrentUserId(null);
                    setUserRole(null);
                    setIsCheckingAuth(false);
                }
                return;
            }
            try {
                const user = await getMe();
                if (isMounted) {
                    setIsLoggedIn(true);
                    setCurrentUserId(user.id);
                    setUserRole(user.role);

                    // ✅ 추가: 유저별 팝업 오픈 여부 체크
                    const hideDate = localStorage.getItem(`hideGuidePopupDate_${user.id}`);
                    const today = new Date().toLocaleDateString("sv-SE");
                    if (hideDate !== today) {
                        setOpenGuidePopup(true);
                    }
                }
            } catch (error) {
                if (isMounted) { setIsLoggedIn(false); setUserRole(null); }
            } finally {
                if (isMounted) setIsCheckingAuth(false);
            }
        };
        validateLogin();
        return () => { isMounted = false; };
    }, [location.pathname]);
    // 2. 실시간 알림 SSE 연결 (main의 핵심 기능)
    useEffect(() => {
        if (!isLoggedIn || !currentUserId) return;

        const token = localStorage.getItem("accessToken");
        const abortController = new AbortController();

        // 초기 알림 리스트 로드
        const fetchNotifications = async () => {
            try {
                const data = await getUnreadNotifications();
                const localItems = JSON.parse(localStorage.getItem(getNotiKey(currentUserId)) || "[]");
                const merged = [...data, ...localItems].filter(
                    (noti, index, self) => index === self.findIndex((item) => item.id === noti.id)
                );
                setNotifications(merged);
            } catch (e) { console.error("초기 알림 로드 실패", e); }
        };
        fetchNotifications();

        // 실시간 구독 시작
        const connectSSE = async () => {
            await fetchEventSource(`${import.meta.env.VITE_API_URL}/api/notifications/subscribe`, {
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
                        addNotification(newNoti);
                    } catch (error) { console.error("알림 파싱 오류:", error); }
                },
                onerror(err) { console.error("SSE 연결 에러:", err); throw err; },
            });
        };
        connectSSE();

        return () => abortController.abort();
    }, [isLoggedIn, currentUserId]);

    // 3. 로그아웃 (서버 세션 정리 포함)
    const handleLogout = async () => {
        try {
            await fetch(`${import.meta.env.VITE_API_URL}/api/auth/logout`, {
                method: "POST",
                credentials: "include",
            });
        } catch (error) {
            console.error("백엔드 로그아웃 요청 실패:", error);
        } finally {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            if (currentUserId) localStorage.removeItem(getNotiKey(currentUserId));
            
            setIsLoggedIn(false);
            setCurrentUserId(null);
            setNotifications([]);
            setAnchorEl(null);
            toast.success("로그아웃되었습니다.");
            navigate("/login");
        }
    };

    // 4. 네비게이션 가드 (로그인 체크 후 이동)
    const handleProtectedNavigation = (path: string) => {
        if (isCheckingAuth) return;
        if (!isLoggedIn) {
            toast.error("로그인 후 이용 가능합니다.");
            navigate("/login");
            return;
        }
        navigate(path);
    };

    return (
        <AppBar position="static" elevation={0} className="header">
            <Toolbar className="header-toolbar">
                <div className="header-logo" onClick={() => navigate("/")}>
                    <img src={tplanner} alt="TPlanner" className="header-logo-img" />
                </div>
                <nav className="header-nav">
                    <span onClick={() => navigate("/")}>여행 계획</span>
                    <span onClick={() => handleProtectedNavigation("/trip-list")}>여행 목록</span>
                    <span onClick={() => handleProtectedNavigation("/community")}>게시판</span>
                    <span onClick={() => setOpenTutorial(true)}>도움말</span>
                </nav>
                <div className="header-actions">
                    {!isCheckingAuth && (
                        isLoggedIn ? (
                            <>
                                <button type="button" className="header-icon-btn" onClick={(e) => setAnchorEl(e.currentTarget)}>
                                    <Badge badgeContent={notifications.length} color="error">
                                        <NotificationsNoneOutlinedIcon />
                                    </Badge>
                                </button>
                                <Menu anchorEl={anchorEl} open={isNotificationOpen} onClose={() => setAnchorEl(null)}>
                                    {notifications.length > 0 && (
                                        <MenuItem onClick={() => {
                                            const currentNotis = [...notifications];
                                            setNotifications([]);
                                            setAnchorEl(null);
                                            if (currentUserId) localStorage.removeItem(getNotiKey(currentUserId));
                                            currentNotis.forEach(n => readNotificationApi(n.id).catch(() => {}));
                                        }}>전체 읽음</MenuItem>
                                    )}
                                    {notifications.length === 0 ? (
                                        <MenuItem disabled>새로운 알림이 없습니다.</MenuItem>
                                    ) : (
                                        notifications.map((noti) => (
                                            <MenuItem key={noti.id} onClick={() => {
                                                setNotifications((prev) => prev.filter((n) => n.id !== noti.id));
                                                if (noti.targetUrl) navigate(noti.targetUrl);
                                                readNotificationApi(noti.id);
                                            }}>
                                                <Typography variant="body2">{noti.message}</Typography>
                                            </MenuItem>
                                        ))
                                    )}
                                </Menu>
                                <Button className="header-login-btn" onClick={() => navigate(isAdmin ? "/admin" : "/mypage")}>
                                    {isAdmin ? "관리자 페이지" : "마이페이지"}
                                </Button>
                                <Button className="header-login-btn" onClick={handleLogout}>로그아웃</Button>
                            </>
                        ) : (
                            <>
                                <Button className="header-login-signup-btn" onClick={() => navigate("/login")}>로그인</Button>
                                <Button className="header-login-signup-btn" onClick={() => navigate("/signup")}>회원가입</Button>
                            </>
                        )
                    )}
                </div>
            </Toolbar>
            <GuidePopup
            open={openGuidePopup}
            onClose={() => setOpenGuidePopup(false)}
            userId={currentUserId} // 추가
            />
            <TutorialModal open={openTutorial} onClose={() => setOpenTutorial(false)} />
        </AppBar>
    );
}
