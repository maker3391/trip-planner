import { AppBar, Toolbar, Button } from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import ShoppingCartOutlinedIcon from "@mui/icons-material/ShoppingCartOutlined";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import TutorialModal from "../guide/TutorialModal";
import "./Header.css";
import { CalculatorService } from "./calculator";
import Calculator from "./Calculator.tsx";

export default function Header() {
  const navigate = useNavigate();
  const [openTutorial, setOpenTutorial] = useState(false);

  return (
    <>
      <AppBar position="static" elevation={0} className="header">
        <Toolbar className="header-toolbar">
          <div className="header-logo" onClick={() => navigate("/")}>
            TPlanner
          </div>

          <nav className="header-nav">
            <span>여행 계획</span>
            <span>패스</span>
            <span>좌석 예약</span>
            <span onClick={() => setOpenTutorial(true)}>도움말</span>
          </nav>

          <div className="header-actions">
            <span className="header-icon">
              <SearchIcon />
            </span>
            <span className="header-icon">
              <button onClick={CalculatorService.openCalculator}
                style={{ background: "none", border: "none", cursor: "pointer" }}>
                <ShoppingCartOutlinedIcon />
              </button>
            </span>
            <Button className="header-login-btn" onClick={() => navigate("/login")}>
              로그인
            </Button>
          </div>
        </Toolbar>
      </AppBar>

      <TutorialModal
        open={openTutorial}
        onClose={() => setOpenTutorial(false)}
      />
      <Calculator />
    </>
  );
}