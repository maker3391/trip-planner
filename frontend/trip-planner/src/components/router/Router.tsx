import { Routes, Route, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";

import MainPage from "../../pages/MainPage";
import LoginPage from "../../pages/LoginPage";
import SignupPage from "../../pages/SignupPage";
import MyPage from "../../pages/MyPage";
import OAuth2CallbackPage from "../../pages/OAuth2CallbackPage";
import CommunityPage from "../../pages/CommunityPage";
import CommunityWritePage from "../../pages/CommunityWritePage";
import TripListPage from "../../pages/TripListPage";
import ProtectedRoute from "./ProtectedRoute";
import ChatBotButton from "../chatbot/ChatBotButton";
import ChatBotModal from "../chatbot/ChatBotModal";
import CommunityReadPage from "../../pages/CommunityReadPage";
import ForgotPasswordPage from "../../pages/ForgotPasswordPage";
import ResetPasswordPage from "../../pages/ResetPasswordPage";
import AdminCSPage from "../cschat/AdminCSPage";
import AdminPage from "../../pages/AdminPage";
import AdminRoute from "./AdminRoute";
import AdminCSNotifier from "../cschat/AdminCSNotifier";
import { getMe } from "../api/auth";

import { fetchEventSource } from "@microsoft/fetch-event-source";
import { useNotificationStore } from "../store/notificationStore";
import toast from "react-hot-toast";

import { NotificationResponseDto } from "../api/Notification";

export default function Router() {
    const [userRole, setUserRole] = useState<string | null>(null);
    const [currentUserId, setCurrentUserId] = useState<number | null>(null);
    const [openChatBot, setOpenChatBot] = useState(false);
    const location = useLocation();

    const addNotification = useNotificationStore((state) => state.addNotification);
    // User Summary에 근거한 관리자 권한 확인
    const isAdmin = userRole === "ADMIN" || userRole === "ROLE_ADMIN"; 
    const showChatBot = !isAdmin;

    const getNotiKey = (userId: number) => `notificationHistory_${userId}`;

    const addNotificationOnce = (newNoti: NotificationResponseDto) => {
        addNotification(newNoti);
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

    useEffect(() => {
        const checkUser = async () => {
            const token = localStorage.getItem("accessToken");
            if (!token || token === "undefined") {
                setUserRole(null);
                setCurrentUserId(null);
                return;
            }
            try {
                const user = await getMe();
                setUserRole(user.role);
                setCurrentUserId(user.id);
            } catch (error) {
                setUserRole(null);
                setCurrentUserId(null);
            }
        };
        checkUser();
    }, [location.pathname]);

    // 일반 알림 SSE 연결 (Router에서 전담)
    useEffect(() => {
        const token = localStorage.getItem("accessToken");
        if (!token || token === "undefined" || !currentUserId) return;

        const abortController = new AbortController();

        const connectSSE = async () => {
            try {
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
                            const currentNotis = useNotificationStore.getState().notifications;
                            const isDuplicate = currentNotis.some(n => String(n.id) === String(newNoti.id));

                            if (!isDuplicate) {
                                toast(newNoti.message, { icon: "🔔", duration: 3000 });
                                addNotification(newNoti);

                                const notiKey = `notificationHistory_${currentUserId}`;
                                const existing = JSON.parse(localStorage.getItem(notiKey) || "[]");
                                if (!existing.some((n: any) => String(n.id) === String(newNoti.id))) {
                                    localStorage.setItem(notiKey, JSON.stringify([newNoti, ...existing].slice(0, 50)));
                                }
                            }
                        } catch (e) { console.error("알림 파싱 에러", e); }
                    },
                    onerror(err) { throw err; }
                });
            } catch (e) { console.error("SSE 연결 실패", e); }
        };

        connectSSE();
        return () => abortController.abort();
    }, [currentUserId, addNotification]);

    useEffect(() => {
        if (isAdmin) setOpenChatBot(false);
    }, [isAdmin]);

    return (
        <>
            {/* 🔥 관리자 계정일 때만 실시간 문의 알림 활성화 */}
            {isAdmin && <AdminCSNotifier />}

            <Routes>
                <Route path="/" element={<MainPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/signup" element={<SignupPage />} />
                <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                <Route path="/reset-password" element={<ResetPasswordPage />} />
                <Route path="/mypage" element={<ProtectedRoute><MyPage /></ProtectedRoute>} />
                <Route path="/admin" element={<AdminRoute><AdminPage /></AdminRoute>} />
                <Route path="/community" element={<CommunityPage />} />
                <Route path="/community/write" element={<CommunityWritePage />} />
                <Route path="/community/write/:id" element={<CommunityWritePage />} />
                <Route path="/community/:id" element={<CommunityReadPage />} />
                <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />
                <Route path="/trip-list" element={<ProtectedRoute><TripListPage /></ProtectedRoute>} />
                <Route path="/admin/cs" element={<AdminRoute><AdminCSPage /></AdminRoute>} />
            </Routes>

            {showChatBot && (
                <>
                    <ChatBotButton onClick={() => setOpenChatBot((prev) => !prev)} />
                    <ChatBotModal open={openChatBot} onClose={() => setOpenChatBot(false)} />
                </>
            )}
        </>
    );
}