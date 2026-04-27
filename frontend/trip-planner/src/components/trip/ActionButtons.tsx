import React, { useState, useEffect } from 'react';
import ShoppingCartOutlinedIcon from "@mui/icons-material/ShoppingCartOutlined";
import { CalculatorService } from "../layout/calculator";
import { useNavigate } from "react-router-dom";

interface ActionButtonsProps {
  onOpenSaveModal: () => void;
  isLoading: boolean;
}

export default function ActionButtons({ onOpenSaveModal, isLoading }: ActionButtonsProps) {
  const [isCalcOpen, setIsCalcOpen] = useState(false);
  const navigate = useNavigate();

  const checkAuthAndExecute = (callback: () => void) => {
    const token = localStorage.getItem("accessToken");
    
    if (!token) {
      alert("로그인 후 이용 가능합니다.");
      navigate("/login"); // 보여주신 LoginPage 경로로 이동
      return;
    }
    
    callback(); // 로그인 되어 있으면 원래 하려던 함수 실행
  };

  const handleSaveClick = () => {
    checkAuthAndExecute(() => {
      onOpenSaveModal();
    });
  };

  // 외부(계산기 내부의 X 버튼 등)에서 발생한 닫기 이벤트를 감지
  useEffect(() => {
    const handleCloseEvent = () => setIsCalcOpen(false);
    const handleOpenEvent = () => setIsCalcOpen(true);

    window.addEventListener("CLOSE_CALCULATOR", handleCloseEvent);
    window.addEventListener("OPEN_CALCULATOR", handleOpenEvent);

    return () => {
      window.removeEventListener("CLOSE_CALCULATOR", handleCloseEvent);
      window.removeEventListener("OPEN_CALCULATOR", handleOpenEvent);
    };
  }, []);

  const handleCalculatorToggle = () => {
    if (isCalcOpen) {
      CalculatorService.closeCalculator();
      // setIsCalcOpen(false); // 이벤트 리스너가 처리하므로 생략 가능하지만 명시적으로 두어도 무방합니다.
    } else {
      CalculatorService.openCalculator();
      // setIsCalcOpen(true);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
      <button
        onClick={handleSaveClick}
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          padding: '0 20px',
          height: '46px',
          backgroundColor: '#1a1a1a',
          color: 'white',
          border: 'none',
          borderRadius: '23px',
          fontWeight: 'bold',
          fontSize: '14px',
          cursor: 'pointer',
          whiteSpace: 'nowrap',
          boxShadow: '0 2px 10px rgba(0,0,0,0.2)',
        }}
      >
        💾 계획 저장
      </button>

      <button
        type="button"
        onClick={handleCalculatorToggle}
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          padding: '0 20px',
          height: '46px',
          backgroundColor: isCalcOpen ? '#1a1a1a' : 'white',
          color: isCalcOpen ? 'white' : '#333',
          border: '1px solid #ddd',
          borderRadius: '23px',
          fontWeight: 'bold',
          fontSize: '14px',
          cursor: 'pointer',
          whiteSpace: 'nowrap',
          boxShadow: '0 2px 10px rgba(0,0,0,0.2)',
          transition: 'background-color 0.2s, color 0.2s',
        }}
      >
        <ShoppingCartOutlinedIcon style={{ fontSize: '20px' }} />
        예산 계산기
      </button>

      {isLoading && (
        <span style={{ fontSize: '12px', color: '#666', textAlign: 'center' }}>
          데이터 로딩 중...
        </span>
      )}
    </div>
  );
}