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

export default function Router() {
  const [openChatBot, setOpenChatBot] = useState(false);

  return (
    <BrowserRouter
      future={{
        v7_startTransition: true,
        v7_relativeSplatPath: true,
      }}
    >
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
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
    </BrowserRouter>
  );
}