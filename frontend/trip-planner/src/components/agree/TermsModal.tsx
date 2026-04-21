import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box
} from "@mui/material";
import { TERMS_CONTENT } from "./TermsData";

interface TermsModalProps {
  open: boolean;
  onClose: () => void;
  title: string;
}

export default function TermsModal({ open, onClose, title }: TermsModalProps) {
  return (
    <Dialog 
      open={open} 
      onClose={onClose}
      maxWidth="md" // 약관은 글자가 많으므로 조금 넓게 설정
      fullWidth
      scroll="paper" // 내용이 길면 다이얼로그 내부에서 스크롤 발생
    >
      <DialogTitle sx={{ borderBottom: '1px solid #eee', fontWeight: 'bold' }}>
        {title}
      </DialogTitle>
      
      <DialogContent dividers>
        <Box sx={{ whiteSpace: 'pre-line', color: '#333', fontSize: '14px', lineHeight: '1.6' }}>
          {/* pre-line을 사용하면 텍스트의 줄바꿈이 그대로 유지됩니다. */}
          <Typography variant="body2">
            {TERMS_CONTENT}
          </Typography>
        </Box>
      </DialogContent>

      <DialogActions sx={{ padding: '15px' }}>
        <Button 
          onClick={onClose} 
          variant="contained" 
          sx={{ backgroundColor: '#4a90e2', '&:hover': { backgroundColor: '#357abd' } }}
        >
          확인
        </Button>
      </DialogActions>
    </Dialog>
  );
}