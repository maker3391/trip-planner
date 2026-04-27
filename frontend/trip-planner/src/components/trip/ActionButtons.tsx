import React, { useEffect, useState } from "react";
import SaveOutlinedIcon from "@mui/icons-material/SaveOutlined";
import CalculateOutlinedIcon from "@mui/icons-material/CalculateOutlined";
import { CalculatorService } from "../layout/calculator";

interface ActionButtonsProps {
  onOpenSaveModal: () => void;
  isLoading: boolean;
}

export default function ActionButtons({
  onOpenSaveModal,
  isLoading,
}: ActionButtonsProps) {
  const [isCalcOpen, setIsCalcOpen] = useState(false);

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
      return;
    }

    CalculatorService.openCalculator();
  };

  const baseButtonStyle: React.CSSProperties = {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    gap: "8px",
    width: "138px",
    height: "46px",
    padding: "0 18px",
    borderRadius: "23px",
    fontWeight: 700,
    fontSize: "14px",
    cursor: "pointer",
    whiteSpace: "nowrap",
    boxShadow: "0 4px 14px rgba(0, 0, 0, 0.18)",
    transition: "all 0.2s ease",
  };

  return (
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          gap: "10px",
          alignItems: "center",
        }}
      >
      <button
        type="button"
        onClick={onOpenSaveModal}
        style={{
          ...baseButtonStyle,
          backgroundColor: "#1a1a1a",
          color: "#fff",
          border: "1px solid #1a1a1a",
        }}
      >
        <SaveOutlinedIcon style={{ fontSize: "19px" }} />
        계획 저장
      </button>

      <button
        type="button"
        onClick={handleCalculatorToggle}
        style={{
          ...baseButtonStyle,
          backgroundColor: isCalcOpen ? "#1a1a1a" : "#fff",
          color: isCalcOpen ? "#fff" : "#333",
          border: isCalcOpen ? "1px solid #1a1a1a" : "1px solid #e0e0e0",
        }}
      >
        <CalculateOutlinedIcon style={{ fontSize: "20px" }} />
        예산 계산기
      </button>

      {isLoading && (
        <span
          style={{
            width: "100%",
            fontSize: "12px",
            color: "#666",
            textAlign: "center",
          }}
        >
          데이터 로딩 중...
        </span>
      )}
    </div>
  );
}