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

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle className="tutorial-title">서비스 이용 튜토리얼</DialogTitle>

      <DialogContent dividers>
        <div className="tutorial-content">
          <div className="tutorial-step clickable"
            onClick={() => {
              onClose();
              navigate("/login");
            }}
          >
            <span className="tutorial-step-number">1</span>
            <div>
              <h3>로그인하기</h3>
              <p>로그인 후 여행 계획을 저장하고 관리할 수 있습니다.</p>
            </div>
          </div>

          <div className="tutorial-step">
            <span className="tutorial-step-number">2</span>
            <div>
              <h3>출발지 입력하기</h3>
              <p>사이드바에서 출발 도시 또는 기차역을 입력해 여행을 시작하세요.</p>
            </div>
          </div>

          <div className="tutorial-step">
            <span className="tutorial-step-number">3</span>
            <div>
              <h3>일정 구성하기</h3>
              <p>가고 싶은 목적지와 이동 계획을 추가해 여행 일정을 구성합니다.</p>
            </div>
          </div>

          <div className="tutorial-step">
            <span className="tutorial-step-number">4</span>
            <div>
              <h3>계획 확인하기</h3>
              <p>완성된 여행 일정을 확인하고 필요에 따라 수정합니다.</p>
            </div>
          </div>
        </div>
      </DialogContent>

      <DialogActions className="tutorial-actions">
        <Button onClick={onClose} className="tutorial-close-btn">
          닫기
        </Button>
      </DialogActions>
    </Dialog>
  );
}