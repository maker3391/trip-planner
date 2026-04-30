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

    const [notifications, setNotifications] = useState<NotificationResponseDto[]>([]);
    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

    const isNotificationOpen = Boolean(anchorEl);
    const isAdmin = userRole === "ADMIN" || userRole === "ROLE_ADMIN";

    const getNotiKey = (userId: number) => `notificationHistory_${userId}`;

    const addNotificationOnce = (newNoti: NotificationResponseDto) => {
        setNotifications((prev) => {
            const exists = prev.some((noti) => noti.id === newNoti.id);

            if (exists) {
                return prev;
            }

            return [newNoti, ...prev];
        });
    };

    const saveNotificationToLocal = (newNoti: NotificationResponseDto) => {
        if (!currentUserId) return;

        const notiKey = getNotiKey(currentUserId);
        const existing = JSON.parse(localStorage.getItem(notiKey) || "[]");

        const exists = existing.some((noti: NotificationResponseDto) => noti.id === newNoti.id);

        if (exists) {
            return;
        }

        const updated = [
            {
                ...newNoti,
                receivedAt: new Date().toISOString(),
            },
            ...existing,
        ].slice(0, 50);

        localStorage.setItem(notiKey, JSON.stringify(updated));
    };

    useEffect(() => {
        const handleAdminCSNotification = (event: Event) => {
            const customEvent = event as CustomEvent<NotificationResponseDto>;

            addNotificationOnce(customEvent.detail);
            saveNotificationToLocal(customEvent.detail);
        };

        window.addEventListener("admin-cs-notification", handleAdminCSNotification);

        return () => {
            window.removeEventListener("admin-cs-notification", handleAdminCSNotification);
        };
    }, [currentUserId]);

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
                }
            } catch (error) {
                clearAuth();

                if (isMounted) {
                    setIsLoggedIn(false);
                    setCurrentUserId(null);
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
        if (!isLoggedIn) return;

        const intervalId = window.setInterval(async () => {
            try {
                await getMe();
            } catch (error) {
                clearAuth();
                setIsLoggedIn(false);
                setCurrentUserId(null);
                setUserRole(null);
                toast.error("계정이 정지되었습니다.");
                navigate("/login");
            }
        }, 5000);

        return () => {
            window.clearInterval(intervalId);
        };
    }, [isLoggedIn]);

    useEffect(() => {
        if (!isLoggedIn || !currentUserId) return;

        const notiKey = getNotiKey(currentUserId);

        const fetchNotifications = async () => {
            try {
                const data = await getUnreadNotifications();
                const localItems = JSON.parse(localStorage.getItem(notiKey) || "[]");

                const merged = [...data, ...localItems].filter(
                    (noti, index, self) =>
                        index === self.findIndex((item) => item.id === noti.id)
                );

                setNotifications(merged);
            } catch (error) {
                console.error("알림 목록 조회 실패:", error);
            }
        };

        fetchNotifications();

        const token = localStorage.getItem("accessToken");
        const abortController = new AbortController();

        const connectSSE = async () => {
            await fetchEventSource('${import.meta.env.VITE_API_URL}/api/notifications/subscribe', {
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

                        toast(newNoti.message, {
                            icon: "🔔",
                            duration: 3000,
                        });

                        addNotificationOnce(newNoti);

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

        return () => {
            abortController.abort();
        };
    }, [isLoggedIn, currentUserId]);

    const handleReadNotification = async (id: number, targetUrl?: string) => {
        setNotifications((prev) => prev.filter((noti) => noti.id !== id));

        if (currentUserId) {
            const notiKey = getNotiKey(currentUserId);
            const existing = JSON.parse(localStorage.getItem(notiKey) || "[]");
            const updated = existing.filter((noti: NotificationResponseDto) => noti.id !== id);
            localStorage.setItem(notiKey, JSON.stringify(updated));
        }

        setAnchorEl(null);

        if (targetUrl) {
            navigate(targetUrl);
        }

        if (targetUrl?.startsWith("/admin/cs")) {
            return;
        }

        try {
            await readNotificationApi(id);
        } catch (error) {
            console.error("알람 읽음 처리 실패:", error);
        }
    };

    const handleReadAllNotifications = async () => {
        const currentNotifications = [...notifications];

        setNotifications([]);
        setAnchorEl(null);

        if (currentUserId) {
            localStorage.removeItem(getNotiKey(currentUserId));
        }

        try {
            const serverNotifications = currentNotifications.filter(
                (noti) => !noti.targetUrl?.startsWith("/admin/cs")
            );

            await Promise.all(
                serverNotifications.map((noti) => readNotificationApi(noti.id))
            );
        } catch (error) {
            console.error("전체 알림 읽음 처리 실패:", error);
        }
    };

    const handleLogout = async () => {
        try {
            await fetch('${import.meta.env.VITE_API_URL}/api/auth/logout', {
                method: "POST",
                credentials: "include",
            });
        } catch (error) {
            console.error("백엔드 로그아웃 요청 실패:", error);
        } finally {
            clearAuth();
            setIsLoggedIn(false);
            setCurrentUserId(null);
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
                                    slotProps={{
                                        paper: {
                                            className: "notification-menu-paper",
                                            style: { pointerEvents: "auto" },
                                        },
                                    }}
                                    disableScrollLock
                                >
                                    {notifications.length > 0 && (
                                        <MenuItem
                                            onClick={handleReadAllNotifications}
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
                                                onClick={() =>
                                                    handleReadNotification(noti.id, noti.targetUrl)
                                                }
                                                className="notification-item"
                                            >
                                                <Typography variant="body2">
                                                    {noti.message}
                                                </Typography>
                                            </MenuItem>
                                        ))
                                    )}
                                </Menu>
                            </>
                        )}

                        {!isCheckingAuth && isLoggedIn ? (
                            <>
                                {isAdmin ? (
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