import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
} from "@mui/material";

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
      maxWidth="md"
      fullWidth
      scroll="paper"
      PaperProps={{
        style: { borderRadius: 12, overflow: "hidden" },
      }}
    >
      {/* 헤더 섹션 */}
      <DialogTitle
        sx={{
          backgroundColor: "#1a2744",
          color: "#fff",
          padding: "22px 28px",
        }}
      >
        <Typography variant="h6" sx={{ fontWeight: 700, fontSize: "17px" }}>
          {title}
        </Typography>
        <Typography sx={{ color: "#a8b8d8", fontSize: "12px", mt: 0.5 }}>
          국내여행 상품 예약 및 서비스 제공을 위한 개인정보 처리 안내
        </Typography>
      </DialogTitle>

      <DialogContent dividers sx={{ padding: "24px 28px", backgroundColor: "#fff" }}>
        {/* 안내 배너 */}
        <Box
          sx={{
            backgroundColor: "#eef2fb",
            borderLeft: "3px solid #1a2744",
            padding: "12px 16px",
            fontSize: "12px",
            color: "#3a4a6a",
            mb: 3,
            lineHeight: 1.6,
          }}
        >
          <strong>Tplanner</strong>는 「개인정보 보호법」 및 관계 법령에 따라 아래와 같이 개인정보를 수집·이용합니다.
        </Box>

        {/* 제1조 섹션 */}
        <SectionTitle title="제1조 개인정보 수집·이용" badge="필수" badgeType="required" />
        <Typography sx={{ fontSize: "12px", fontWeight: 600, mb: 1, color: "#3a4a6a" }}>
          ① 여행 예약 및 계약 체결
        </Typography>
        <TermsTable
          headers={["처리 목적", "처리 항목", "법적 근거", "보유기간"]}
          rows={[
            ["여행상품 상담", "성명(국문), 연락처, 이메일", "개인정보보호법 제15조①4호", "상담 종료 후 1개월"],
            ["예약·계약 체결", "성명, 생년월일, 성별, 연락처, 이메일, 주소", "개인정보보호법 제15조①4호", "여행 종료 후 5년"],
            ["숙박 예약", "[여행자] 이름, 생년월일, 성별 등", "개인정보보호법 제15조①4호", "여행 종료 후 5년"],
            ["결제 처리", "카드 정보, 계좌 정보, 환불 계좌", "개인정보보호법 제15조①4호", "결제 완료 후 5년"],
          ]}
        />

        {/* 제2조 섹션 */}
        <Box sx={{ mt: 4 }}>
          <SectionTitle title="제2조 개인정보 제3자 제공" badge="필수" badgeType="required" />
          <TermsTable
            headers={["제공받는 자", "제공 목적 / 항목", "보유·이용기간"]}
            rows={[
              ["숙박업체", "객실 예약 및 투숙객 확인 / 성명, 연락처", "체크아웃 완료 시까지"],
              ["보험사", "여행자보험 가입 / 성명, 생년월일", "계약 만료 후 3년"],
              ["렌터카 회사", "차량 배정 / 성명, 연락처, 면허번호", "여행 종료 후 30일"],
            ]}
          />
        </Box>

        {/* 권리 안내 섹션 */}
        <Box sx={{ mt: 4 }}>
          <Typography sx={{ fontSize: "13px", fontWeight: 700, color: "#1a2744", borderBottom: "2px solid #1a2744", pb: 1, mb: 1.5 }}>
            제5조 정보주체의 권리·행사방법
          </Typography>
          <Box sx={{ display: "flex", gap: 1, flexWrap: "wrap", mb: 2 }}>
            {["열람", "정정·삭제", "처리정지", "동의 철회"].map((text) => (
              <Box key={text} sx={{ backgroundColor: "#e8efff", color: "#2c4a8c", fontSize: "11px", px: 1.5, py: 0.5, borderRadius: 10 }}>
                {text}
              </Box>
            ))}
          </Box>
          <Box sx={{ backgroundColor: "#f4f6fc", p: 2, borderRadius: 1.5, fontSize: "12px", lineHeight: 1.7 }}>
            담당자: 개인정보 보호책임자 | 전화: 02-0000-0000 | 이메일: privacy@tplanner.com
          </Box>
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

// 내부 헬퍼 컴포넌트: 섹션 타이틀
function SectionTitle({ title, badge, badgeType }: { title: string; badge: string; badgeType: "required" | "optional" }) {
  return (
    <Box sx={{ display: "flex", alignItems: "center", gap: 1, borderBottom: "2px solid #1a2744", pb: 1, mb: 1.5 }}>
      <Typography sx={{ fontSize: "13px", fontWeight: 700, color: "#1a2744" }}>{title}</Typography>
      <Box
        sx={{
          fontSize: "10px",
          fontWeight: 600,
          px: 1,
          py: 0.2,
          borderRadius: 5,
          backgroundColor: badgeType === "required" ? "#e8efff" : "#f0f4f0",
          color: badgeType === "required" ? "#2c4a8c" : "#4a6a4a",
        }}
      >
        {badge}
      </Box>
    </Box>
  );
}

// 내부 헬퍼 컴포넌트: 약관 테이블
function TermsTable({ headers, rows }: { headers: string[]; rows: string[][] }) {
  return (
    <TableContainer component={Paper} variant="outlined" sx={{ mb: 2, borderRadius: 1 }}>
      <Table size="small">
        <TableHead>
          <TableRow sx={{ backgroundColor: "#1a2744" }}>
            {headers.map((header) => (
              <TableCell key={header} align="center" sx={{ color: "#fff", fontSize: "11px", fontWeight: 600, py: 1 }}>
                {header}
              </TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row, index) => (
            <TableRow key={index} sx={{ "&:nth-of-type(even)": { backgroundColor: "#f7f9fd" } }}>
              {row.map((cell, idx) => (
                <TableCell key={idx} sx={{ fontSize: "11px", color: "#2a3550", py: 1, lineHeight: 1.5 }}>
                  {cell}
                </TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}