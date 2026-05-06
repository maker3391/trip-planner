# 🧳 TRIP PLANNER - Frontend

React 기반으로 구현된 여행 관리 웹 서비스의 프론트엔드 애플리케이션입니다.  
사용자는 여행 계획을 생성 및 관리하고, 커뮤니티 게시판을 통해 정보를 공유할 수 있습니다.

---

## 🛠️ Tech Stack

- **Framework**: React
- **Language**: TypeScript
- **Routing**: React Router
- **HTTP Client**: Axios
- **Editor**: React Quill
- **UI Library**: MUI (Material UI)
- **State Management**: Zustand

---

## 📄 주요 페이지

- 메인 페이지 (여행 계획 생성 및 저장)
- 여행 계획 조회 페이지
- 내 페이지 (관리자 페이지는 별도 분리)
- 로그인 / 회원가입 / 비밀번호 초기화 페이지
- 게시판 페이지
- 게시글 작성 페이지
- 게시글 상세 페이지

---

## 🧩 주요 기능

### 🔍 게시판
- 카테고리 / 지역 필터링
- 키워드 검색
- 게시글 목록 및 상세 조회
- 댓글 / 대댓글 UI

### ✍️ 에디터
- React Quill 기반 리치 텍스트 작성
- 이미지 업로드 지원

### 🔐 인증
- 로그인 / 회원가입
- 비밀번호 재설정 요청

---

## 🗂️ 프로젝트 구조

```text
src/
 ├── assets/         # 정적 리소스 (이미지 등)
 ├── components/     # 기능 단위 컴포넌트 모음
 │    ├── api/       # API 호출 관련 로직
 │    ├── chatbot/   # 챗봇 UI 및 로직
 │    ├── cschat/    # 채팅 관련 컴포넌트
 │    ├── guide/     # 가이드 UI
 │    ├── hooks/     # 커스텀 훅
 │    ├── layout/    # 레이아웃 컴포넌트
 │    ├── map/       # 지도 관련 기능
 │    ├── router/    # 라우팅 설정
 │    ├── store/     # Zustand 상태 관리
 │    └── trip/      # 여행 계획 관련 핵심 기능 컴포넌트
 │
 ├── pages/          # 페이지 단위 컴포넌트
 ├── styles/         # 전역 스타일
 ├── types/          # 타입 정의
 ├── App.tsx
 └── main.tsx
```
## ⚙️ 실행 방법
```
npm run dev
```