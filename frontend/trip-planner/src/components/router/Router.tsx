import { BrowserRouter, Routes, Route } from "react-router-dom";
import MainPage from "../../pages/MainPage";
import LoginPage from "../../pages/LoginPage";
import SignupPage from "../../pages/SignupPage";
import MyPage from "../../pages/MyPage";
import OAuth2CallbackPage from "../../pages/OAuth2CallbackPage";
import CommunityPage from "../../pages/CommunityPage";
<<<<<<< Updated upstream
=======
import CommunityWritePage from "../../pages/CommunityWritePage";
>>>>>>> Stashed changes

export default function Router() {
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
        <Route path="/community" element={<CommunityPage />} />
        <Route path="/community/write" element={<CommunityWritePage />} />
        <Route path="/mypage" element={<MyPage />} />
        <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />
      </Routes>
    </BrowserRouter>
  );
}