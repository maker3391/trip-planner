// import { AppBar, Toolbar, Button } from "@mui/material";
// import ShoppingCartOutlinedIcon from "@mui/icons-material/ShoppingCartOutlined";
// import { useNavigate } from "react-router-dom";
// import { useState } from "react";
// import TutorialModal from "../guide/TutorialModal";
// import "./Header.css";
// import { CalculatorService } from "./calculator";
// import Calculator from "./Calculator.tsx";
// import tplanner from "../../assets/icons/tplanner2.png";

// export default function Header() {
//   const navigate = useNavigate();
//   const [openTutorial, setOpenTutorial] = useState(false);

//   const isLoggedIn = localStorage.getItem("isLoggedIn") === "true";

//   const handleLogout = () => {
//     localStorage.removeItem("accessToken");
//     localStorage.removeItem("refreshToken");
//     localStorage.setItem("isLoggedIn", "false");
//     alert("로그아웃되었습니다.");
//     navigate("/login");
//   };

//   const handleTripListClick = () => {
//     if (!isLoggedIn) {
//       alert("로그인 후 이용 가능합니다.");
//       localStorage.setItem("isLoggedIn","false");
//       navigate("/login");
//       return;
//     }

//     navigate("/trip-list");
//   };

//   const handleCommunityClick = () => {
//     if (!isLoggedIn) {
//       alert("로그인 후 이용 가능합니다.");
//       localStorage.setItem("isLoggedIn","false");
//       navigate("/login");
//       return;
//     }
//     navigate("/community");
//   }

//   return (
//     <>
//       <AppBar position="static" elevation={0} className="header">
//         <Toolbar className="header-toolbar">
//           <div className="header-logo" onClick={() => navigate("/")}>
//             <img src={tplanner} alt="TPlanner" className="header-logo-img" />
//           </div>

//           <nav className="header-nav">
//             <span onClick={() => navigate("/")}>여행 계획</span>
//             <span onClick={handleTripListClick}>여행 목록</span>
//             <span onClick={handleCommunityClick}>게시판</span>
//             <span onClick={() => setOpenTutorial(true)}>도움말</span>
//           </nav>

//           <div className="header-actions">
//             <span className="header-icon">
//               <button
//                 type="button"
//                 onClick={CalculatorService.openCalculator}
//                 className="header-icon-btn"
//               >
//                 <ShoppingCartOutlinedIcon />
//               </button>
//             </span>

//             {isLoggedIn ? (
//               <>
//                 <Button
//                   className="header-login-btn"
//                   onClick={() => navigate("/mypage")}
//                 >
//                   마이페이지
//                 </Button>
//                 <Button className="header-login-btn" onClick={handleLogout}>
//                   로그아웃
//                 </Button>
//               </>
//             ) : (
//               <>
//                 <Button
//                   className="header-login-signup-btn"
//                   onClick={() => navigate("/login")}
//                 >
//                   로그인
//                 </Button>
//                 <Button
//                   className="header-login-signup-btn"
//                   onClick={() => navigate("/signup")}
//                 >
//                   회원가입
//                 </Button>
//               </>
//             )}
//           </div>
//         </Toolbar>
//       </AppBar>

//       <TutorialModal
//         open={openTutorial}
//         onClose={() => setOpenTutorial(false)}
//       />
//       <Calculator />
//     </>
//   );
// }

import { AppBar, Toolbar, Button } from "@mui/material";
import ShoppingCartOutlinedIcon from "@mui/icons-material/ShoppingCartOutlined";
import { useNavigate, useLocation } from "react-router-dom"; // 🔥 useLocation 추가!
import { useState } from "react";
import TutorialModal from "../guide/TutorialModal";
import "./Header.css";
import { CalculatorService } from "./calculator";
import Calculator from "./Calculator.tsx";
import tplanner from "../../assets/icons/tplanner2.png";
import GuidePopup from "../guide/GuidePopup.tsx";

export default function Header() {
  const navigate = useNavigate();
  const location = useLocation(); // 🔥 현재 주소창 정보 가져오기
  const [openTutorial, setOpenTutorial] = useState(false);

  const isLoggedIn = localStorage.getItem("isLoggedIn") === "true";

  // 💡 URL에서 첫 번째로 발견되는 숫자(여행 ID)를 쏙 빼오는 마법의 정규식!
  // 예: "/plan/12" -> 12 추출 / 메인페이지("/") -> null
  const match = location.pathname.match(/\d+/);
  const currentTripId = match ? parseInt(match[0], 10) : 1;

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.setItem("isLoggedIn", "false");
    alert("로그아웃되었습니다.");
    navigate("/login");
  };

  const handleTripListClick = () => {
    if (!isLoggedIn) {
      alert("로그인 후 이용 가능합니다.");
      localStorage.setItem("isLoggedIn","false");
      navigate("/login");
      return;
    }
    navigate("/trip-list");
  };

  const handleCommunityClick = () => {
    if (!isLoggedIn) {
      alert("로그인 후 이용 가능합니다.");
      localStorage.setItem("isLoggedIn","false");
      navigate("/login");
      return;
    }
    navigate("/community");
  };

  // 💡 장바구니(계산기) 버튼 클릭 핸들러 추가
  const handleCalculatorClick = () => {
    // 🚨 너무 깐깐했던 문지기(알림창)는 일단 주석 처리!
    // if (!currentTripId) {
    //   alert("여행 계획 상세 페이지에서만 예산 계산기를 사용할 수 있습니다!");
    //   return;
    // }
    CalculatorService.openCalculator();
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
            <span className="header-icon">
              <button
                type="button"
                onClick={handleCalculatorClick} 
                className="header-icon-btn"
              >
                <ShoppingCartOutlinedIcon />
              </button>
            </span>

            {isLoggedIn ? (
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
            ) : (
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
            )}
          </div>
        </Toolbar>
      </AppBar>

      <TutorialModal
        open={openTutorial}
        onClose={() => setOpenTutorial(false)}
      />
      {currentTripId && <Calculator tripId={currentTripId} />}
    </>
  );
}