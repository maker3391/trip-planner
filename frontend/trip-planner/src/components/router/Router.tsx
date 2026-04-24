import { BrowserRouter, Routes, Route } from "react-router-dom";
import { useState } from "react";

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
import toast ,{ Toaster } from "react-hot-toast";
import { useLocation } from "react-router-dom";
import { useEffect } from "react";

export default function Router() {
  const [openChatBot, setOpenChatBot] = useState(false);
  const { pathname } = useLocation(); // 현재 경로 감지

  // 페이지 경로(URL)가 바뀔 때마다 모든 토스트를 지움
  useEffect(() => {
    toast.dismiss(); 
  }, [pathname]);

  return (
    <>
      <Toaster 
        position="bottom-center" 
        reverseOrder={false} 
        toastOptions={{
          // 여러 번 클릭해도 중복 방지를 위한 기본 세팅
          duration: 3000,
        }}
      />
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />}/>
        <Route path="/reset-password" element={<ResetPasswordPage />}/>
        <Route
          path="/mypage"
          element={
            <ProtectedRoute>
              <MyPage />
            </ProtectedRoute>
          }
        />
        <Route path="/community" element={<CommunityPage />} />
        <Route path="/community/write" element={<CommunityWritePage />} />
        <Route path="/community/write/:id" element={<CommunityWritePage />} />
        <Route path="/community/:id" element={<CommunityReadPage />} />
        <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />
        <Route
          path="/trip-list"
          element={
            <ProtectedRoute>
              <TripListPage />
            </ProtectedRoute>
          }
        />
      </Routes>
      
      <ChatBotButton onClick={() => setOpenChatBot((prev) => !prev)} />

      <ChatBotModal
        open={openChatBot}
        onClose={() => setOpenChatBot(false)}
      />
    </>
  );
}