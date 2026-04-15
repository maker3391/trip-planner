import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import "./TutorialModal.css";

interface TutorialModalProps {
  open: boolean;
  onClose: () => void;
}

export default function TutorialModal({
  open,
  onClose,
}: TutorialModalProps) {
  const navigate = useNavigate();

  const handleLoginStepClick = () => {
    const accessToken = localStorage.getItem("accessToken");

    if (accessToken && accessToken !== "undefined") {
      alert("이미 로그인된 상태입니다.");
      return;
    }

    onClose();
    navigate("/login");
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        className: "tutorial-paper",
      }}
    >
      <DialogTitle className="tutorial-title-wrap">
        <span className="tutorial-badge">GUIDE</span>
        <h2 className="tutorial-title">TPlanner 사용 가이드</h2>
        <p className="tutorial-subtitle">
          장소를 검색하거나 지도에서 직접 선택하고, 핀으로 위치를 확인한 뒤
          일정에 추가해 여행 계획을 완성해보세요.
        </p>
      </DialogTitle>

      <DialogContent dividers className="tutorial-dialog-content">
        <div className="tutorial-content">
          <div
            className="tutorial-step tutorial-step-clickable"
            onClick={handleLoginStepClick}
          >
            <span className="tutorial-step-number">1</span>
            <div className="tutorial-step-text">
              <h3>로그인하고 계획 저장하기</h3>
              <p>
                로그인하면 여행 계획을 저장하고, 나중에 다시 확인하거나 수정할 수
                있어요.
              </p>
            </div>
          </div>

          <div className="tutorial-step">
            <span className="tutorial-step-number">2</span>
            <div className="tutorial-step-text">
              <h3>검색으로 장소 찾기</h3>
              <p>
                사이드바에서 도시, 역, 관광지 등 원하는 장소를 검색해 빠르게
                위치를 찾아보세요.
              </p>
            </div>
          </div>

          <div className="tutorial-step">
            <span className="tutorial-step-number">3</span>
            <div className="tutorial-step-text">
              <h3>지도에서 직접 선택하기</h3>
              <p>
                지도를 클릭하면 해당 위치를 바로 확인할 수 있어서 원하는 장소를
                직관적으로 고를 수 있어요.
              </p>
            </div>
          </div>

          <div className="tutorial-step">
            <span className="tutorial-step-number">4</span>
            <div className="tutorial-step-text">
              <h3>핀으로 위치 확인하기</h3>
              <p>
                선택한 장소는 핀으로 표시되어 여행 동선과 위치를 한눈에 파악할 수
                있어요.
              </p>
            </div>
          </div>

          <div className="tutorial-step">
            <span className="tutorial-step-number">5</span>
            <div className="tutorial-step-text">
              <h3>일정에 추가해 여행 완성하기</h3>
              <p>
                마음에 드는 장소를 일정에 추가하고 순서를 정리해 나만의 여행
                계획을 완성해보세요.
              </p>
            </div>
          </div>
        </div>
      </DialogContent>

      <DialogActions className="tutorial-actions">
        <Button onClick={onClose} className="tutorial-close-btn">
          확인했어요
        </Button>
      </DialogActions>
    </Dialog>
  );
}