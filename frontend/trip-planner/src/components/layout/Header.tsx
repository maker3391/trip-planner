import { AppBar, Toolbar, Button, Badge, Menu, MenuItem, Typography } from "@mui/material";
import NotificationsNoneOutlinedIcon from "@mui/icons-material/NotificationsNoneOutlined";
import { useNavigate, useLocation } from "react-router-dom";
import { useState, useEffect } from "react";
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
    
    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
    const isNotificationOpen = Boolean(anchorEl);
    const isAdmin = userRole === "ADMIN" || userRole === "ROLE_ADMIN";

    const getNotiKey = (userId: number) => `notificationHistory_${userId}`;

    useEffect(() => {
        let isMounted = true;
        const validateLogin = async () => {
            const token = localStorage.getItem("accessToken");
            if (!token || token === "undefined") {
                if (isMounted) {
                    setIsLoggedIn(false);
                    setCurrentUserId(null);
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
                if (isMounted) setIsLoggedIn(false);
            } finally {
                if (isMounted) setIsCheckingAuth(false);
            }
        };
        validateLogin();
        return () => { isMounted = false; };
    }, [location.pathname]);

    useEffect(() => {
        if (!isLoggedIn || !currentUserId) return;
        const fetchNotifications = async () => {
            try {
                const data = await getUnreadNotifications();
                const localItems = JSON.parse(localStorage.getItem(getNotiKey(currentUserId)) || "[]");
                const merged = [...data, ...localItems].filter(
                    (noti, index, self) => index === self.findIndex((item) => item.id === noti.id)
                );
                setNotifications(merged);
            } catch (error) {
                console.error("알림 조회 실패:", error);
            }
        };
        fetchNotifications();
    }, [isLoggedIn, currentUserId, setNotifications]);

    const handleReadNotification = async (id: number, targetUrl?: string) => {
        setNotifications((prev) => prev.filter((noti) => noti.id !== id));
        if (currentUserId) {
            const notiKey = getNotiKey(currentUserId);
            const existing = JSON.parse(localStorage.getItem(notiKey) || "[]");
            const updated = existing.filter((noti: any) => noti.id !== id);
            localStorage.setItem(notiKey, JSON.stringify(updated));
        }
        setAnchorEl(null);
        if (targetUrl) navigate(targetUrl);
        try { await readNotificationApi(id); } catch (e) {
            console.error(e);
        }
    };

    const handleReadAllNotifications = async () => {
        const currentNotis = [...notifications];
        setNotifications([]);
        setAnchorEl(null);
        if (currentUserId) localStorage.removeItem(getNotiKey(currentUserId));
        try { await Promise.all(currentNotis.map(n => readNotificationApi(n.id))); } catch (e) {
            console.error(e);
        }
    };

    const handleLogout = () => {
        localStorage.removeItem("accessToken");
        setIsLoggedIn(false);
        setNotifications([]);
        toast.success("로그아웃되었습니다.");
        navigate("/login");
    };

    return (
        <AppBar position="static" elevation={0} className="header">
            <Toolbar className="header-toolbar">
                <div className="header-logo" onClick={() => navigate("/")}>
                    <img src={tplanner} alt="TPlanner" className="header-logo-img" />
                </div>
                <nav className="header-nav">
                    <span onClick={() => navigate("/")}>여행 계획</span>
                    <span onClick={() => navigate("/trip-list")}>여행 목록</span>
                    <span onClick={() => navigate("/community")}>게시판</span>
                    <span onClick={() => setOpenTutorial(true)}>도움말</span>
                </nav>
                <div className="header-actions">
                    {!isCheckingAuth && isLoggedIn && (
                        <>
                            <button type="button" className="header-icon-btn" onClick={(e) => setAnchorEl(e.currentTarget)}>
                                <Badge badgeContent={notifications.length} color="error">
                                    <NotificationsNoneOutlinedIcon />
                                </Badge>
                            </button>
                            <Menu anchorEl={anchorEl} open={isNotificationOpen} onClose={() => setAnchorEl(null)}>
                                {notifications.length > 0 && (
                                    <MenuItem onClick={handleReadAllNotifications}>전체 읽음</MenuItem>
                                )}
                                {notifications.length === 0 ? (
                                    <MenuItem disabled>새로운 알림이 없습니다.</MenuItem>
                                ) : (
                                    notifications.map((noti) => (
                                        <MenuItem key={noti.id} onClick={() => handleReadNotification(noti.id, noti.targetUrl)}>
                                            <Typography variant="body2">{noti.message}</Typography>
                                        </MenuItem>
                                    ))
                                )}
                            </Menu>
                            <Button className="header-login-btn" onClick={() => navigate(isAdmin ? "/admin" : "/mypage")}>
                                {isAdmin ? "관리자" : "마이페이지"}
                            </Button>
                            <Button className="header-login-btn" onClick={handleLogout}>로그아웃</Button>
                        </>
                    )}
                </div>
            </Toolbar>
            <GuidePopup open={openGuidePopup} onClose={() => setOpenGuidePopup(false)} />
            <TutorialModal open={openTutorial} onClose={() => setOpenTutorial(false)} />
        </AppBar>
    );
}