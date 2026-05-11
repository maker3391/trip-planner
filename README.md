```md
# 🗺️ TPlanner

> 지도 위에 핀을 찍고 동선을 계획하는 여행 플래너 서비스

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat-square&logo=springboot)
![React](https://img.shields.io/badge/React-61DAFB?style=flat-square&logo=react&logoColor=black)
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=flat-square&logo=mariadb)

<br>

## 📌 프로젝트 소개

**Trip Planner**는 지도에 핀을 찍어 여행 동선을 시각적으로 계획하고, 상세 메모로 일정을 정리할 수 있는 여행 플래닝 서비스입니다.  
완성된 여행 계획은 커뮤니티 게시판에 공유하여 다른 사용자들과 나눌 수 있습니다.

<br>

## 🔗 Live Demo

[Trip Planner 바로가기](https://trip-planner-eight-zeta.vercel.app/)

<br>

## 📅 개발 기간

**2025.03.31 ~ 2025.05.07** (약 5주)

<br>

## 👥 팀원 소개

| 역할 | 이름 | 담당 기능 |
|------|------|----------|
| 팀장 | 김승일 | 지도 핀 & 동선 기능, 프로젝트 총괄 및 git 관리 |
| 팀원 | 최병현 | AI 챗봇 & OAuth2 로그인 · JWT 인증 |
| 팀원 | 최영 | 프론트엔드 전반, 마이페이지와 관리자페이지 기능 |
| 팀원 | 남수원 | 백엔드 전반, 소켓 기반 관리자 문의 기능 |
| 팀원 | 신원준 | 게시판전반의 백프론트엔드 |

<br>

## 🙋 개인 기여 - 최병현

### Frontend (UI / UX)

- 메인 페이지 UI 프로토타입 설계 및 구현
- 챗봇 기반 인터페이스 구성 (입력 → 응답 흐름 설계)
- 추천 결과 UI 구현
  - 여행 일정 렌더링
  - 맛집 / 숙소 / 명소 카드 형태 렌더링
- 상태 기반 UI 처리
  - 로딩 상태 처리
  - 에러 메시지 처리
  - 스크롤 UX 개선

### Backend

#### 인증 시스템

- JWT 기반 인증 구조 설계
- Redis 기반 Refresh Token 관리
- OAuth2 로그인 연동 (Kakao / Google)
- OAuth 계정 / 일반 계정 분리 정책 적용

#### 챗봇 API 구조

- REST API 기반 챗봇 엔드포인트 설계 (`/api/chat`)
- Controller → Service → Resolver 구조 설계
- 자연어 입력 기반 Intent 분석 및 서비스 분기 처리
- 지역명 파싱 및 정규화
- 자연어 키워드 정제 및 세부 지역 추출 로직 구현

#### 추천 시스템

##### 여행 일정 추천 (LLM 기반)

- LLM `tool_call` 기반 일정 생성 구조 구현
- 일정 구조 JSON 파싱 및 검증 로직 구현
- 사용자 입력 기반 동적 일정 생성 처리

##### 맛집 / 숙소 / 명소 추천

- Kakao Local API 연동
- 카테고리 기반 필터링 및 데이터 정제
- 자연어 키워드 기반 룰 처리

##### 통합 추천 (Combined Recommendation)

- 일정 + 맛집 + 숙소 통합 응답 구조 설계
- 단일 요청 기반 복합 추천 결과 반환

##### 성능 및 안정성 개선

- 추천 결과 TTL 기반 캐싱 처리
- 동일 요청 재호출 방지
- 외부 API 오류 및 추천 실패 상황 예외 처리

#### Backend 공통 처리

- `GlobalExceptionHandler` 기반 예외 처리 구조 구현
- 프론트엔드에서 처리 가능한 공통 에러 응답 구조 설계

## 🛠️ 기술 스택
 
### Frontend
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=flat-square&logo=javascript&logoColor=black)
![React](https://img.shields.io/badge/React-61DAFB?style=flat-square&logo=react&logoColor=black)
![React Query](https://img.shields.io/badge/React_Query-FF4154?style=flat-square&logo=reactquery&logoColor=white)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=flat-square&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=flat-square&logo=css3&logoColor=white)
 
### Backend
![Java](https://img.shields.io/badge/Java_21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white)
 
### Database
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=flat-square&logo=mariadb&logoColor=white)
 
<br>
## ✨ 주요 기능
 
### 🗺️ 지도 기반 여행 동선 계획
- 지도에 핀을 찍어 방문할 장소를 등록
- 핀 순서를 조정해 최적 동선 시각화
- 장소별 상세 메모로 세부 일정 기록
### 📋 게시판 공유
- 완성된 여행 계획을 커뮤니티 게시판에 공유
- 다른 사용자의 여행 플랜 탐색 및 참고
### 🤖 AI 챗봇
- 여행지 추천 및 일정 관련 질문을 AI 챗봇으로 해결
### 💬 실시간 관리자 문의
- WebSocket을 활용한 실시간 1:1 관리자 문의 기능
### 💰 경비 관리 (Dashboard)
- 여행별 경비 항목 추가 / 수정 / 삭제
- 경비 요약 및 전체 내역 조회로 예산 현황 파악
### 👤 마이페이지
- 내가 만든 여행 플랜 관리
- 프로필 및 개인 정보 수정
<br>

## 🚀 시작하기
 
### 사전 요구사항
 
- Node.js 18 이상
- Java 21
- MariaDB
### 프론트엔드 실행
 
```bash
# 저장소 클론
git clone https://github.com/korit-12-1team-fiveguys/trip-planner.git
 
# 프론트엔드 디렉토리 이동
cd frontend
 
# 패키지 설치
npm install
 
# 개발 서버 실행
npm run dev
```
 
### 백엔드 실행
 
```bash
# 백엔드 디렉토리 이동
cd backend
 
# 환경 변수 설정 (아래 환경 변수 섹션 참고)
cp .env.example .env
 
# 빌드 및 실행
./gradlew bootRun
```
 
<br>
## ⚙️ 환경 변수 설정
 
`backend/.env` 파일을 생성하고 아래 항목을 설정하세요.
 
```env
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=trip_planner
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
 
# JWT
JWT_SECRET=your_jwt_secret
 
# AWS
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_REGION=ap-northeast-2
AWS_S3_BUCKET=your_s3_bucket_name
 
# AI Chatbot (사용하는 서비스에 따라 수정)
AI_API_KEY=your_ai_api_key
```
 
<br>
## 📁 프로젝트 구조
 
```
trip-planner/
├── frontend/
│   ├── public/
│   └── src/
│       ├── components/      # 공통 컴포넌트
│       │    ├── agree/      # 개인정보 이용동의서
│       │    ├── api/        # API 호출 함수
│       │    ├── chatbot/    # AI 챗봇
│       │    ├── cschat/     # 관리자 문의 (WebSocket)
│       │    ├── guide/      # 서비스 이용 가이드
│       │    ├── hooks/      # 커스텀 훅 (React Query 등)
│       │    ├── layout/     # 헤더, 푸터 등 레이아웃 컴포넌트
│       │    ├── map/        # 여행 계획용 지도 페이지 컴포넌트
│       │    ├── router/     # 라우팅 설정
│       │    ├── store/      # 전역 상태 관리
│       │    └── trip/       # 메인 페이지 버튼
│       ├── pages/           # 페이지 컴포넌트
│       ├── styles/          # 전역 스타일
│       └── types/           # TypeScript 타입 정의
│
└── backend/
    └── src/main/java/
        ├── client/          # 외부 API 클라이언트 (AI, 지도 등)
        ├── config/          # 설정 파일 (Security, WebSocket 등)
        ├── controller/      # REST API 컨트롤러
        ├── dto/             # 데이터 전송 객체
        ├── entity/          # JPA 엔티티
        ├── exception/       # 커스텀 예외 처리
        ├── repository/      # DB 접근 계층
        ├── response/        # 공통 응답 형식
        └── service/         # 비즈니스 로직
```
 
<br>
## 📡 API 명세
 
> Base URL : `http://localhost:8080`  
> 인증 방식 : JWT Bearer Token (`Authorization: Bearer {accessToken}`)  
> 공통 응답 : 성공 시 객체 반환 / 실패 시 `ErrorResponse` 반환
 
### 🔐 1. 인증 및 사용자 관리 (Auth API)
 
| 기능 | Method | Endpoint | 요청 |
|------|--------|----------|------|
| 회원가입 | `POST` | /api/auth/signup | SignupRequest |
| 로그인 | `POST` | /api/auth/login | LoginRequest |
| 내 정보 조회 | `GET` | /api/auth/me | - |
| 토큰 갱신 | `POST` | /api/auth/refresh | RefreshTokenRequest |
| 로그아웃 | `POST` | /api/auth/logout | - |
| 비밀번호 / 닉네임 / 전화번호 / 이름 변경 | `PATCH` | /api/auth/me | UpdateMyInfoRequest |
| 회원탈퇴 | `DELETE` | /api/auth/withdraw | - |
 
### ✈️ 2. 여행 일정 관리 (Trip API)
 
| 기능 | Method | Endpoint | 요청 |
|------|--------|----------|------|
| 일정 생성 | `POST` | /api/trips | TripPlanRequestDto |
| 목록 조회 | `GET` | /api/trips | - |
| 상세 조회 | `GET` | /api/trips/{id} | - |
| 일정 수정 | `PATCH` | /api/trips/{id} | TripPlanRequestDto |
| 일정 삭제 | `DELETE` | /api/trips/{id} | - |
 
### 💬 3. 커뮤니티 API (Community API)
 
| 기능 | Method | Endpoint | 요청 |
|------|--------|----------|------|
| 게시글 등록 | `POST` | /api/community/posts | CommunityRequest |
| 게시글 목록 조회 | `GET` | /api/community/posts | - |
| 게시글 상세 조회 | `GET` | /api/community/posts/{id} | - |
| 조회수 증가 | `PATCH` | /api/community/posts/{id} | - |
| 좋아요 토글 | `POST` | /api/community/posts/{id} | - |
| 좋아요 상태 조회 | `GET` | /api/community/posts/{id} | - |
| 이미지 업로드 | `POST` | /api/community/image | ImageUploadRequest |
| 이미지 보기 | `GET` | /api/community/image/{id} | - |
| 게시글 수정 | `PUT` | /api/community/posts/{id} | CommunityRequest |
| 게시글 삭제 | `DELETE` | /api/community/posts/{id} | - |
 
### 🤖 4. 챗봇AI & 장소 검색 API (AI & Place API)
 
| 기능 | Method | Endpoint | 요청 |
|------|--------|----------|------|
| AI 여행 비서 채팅 | `POST` | /api/chat | ChatRequest |
| 구글 장소 검색 | `GET` | /api/google-places | keyword (Query) |
| 시스템 장소 등록 | `POST` | /api/places | PlaceRequestDto |
 
### 📊 5. 여행 예산 관리
 
| 기능 | Method | Endpoint | 요청 |
|------|--------|----------|------|
| 비용 요약 조회 | `GET` | /api/trips/{id}/expenses/summary | - |
| 비용 전체 조회 | `GET` | /api/trips/{id}/expenses | - |
| 비용 항목 추가 | `POST` | /api/trips/{id}/expenses | ExpenseRequest |
| 비용 항목 수정 | `PUT` | /api/expenses/{expenseId} | ExpenseRequest |
| 비용 항목 삭제 | `DELETE` | /api/expenses/{expenseId} | - |
 
### 👥 6. 여행 멤버 관리 (Trip Member API)
 
| 기능 | Method | Endpoint | 요청 |
|------|--------|----------|------|
| 참가 신청 | `POST` | /api/trips/{tripId}/members | - |
| 멤버 목록 조회 | `GET` | /api/trips/{tripId}/members | - |
| 참가 수락 | `PATCH` | /api/trips/{tripId}/members | - |
| 멤버 강퇴 / 거절 | `DELETE` | /api/trips/{tripId}/members | - |
| 스스로 나가기 | `DELETE` | /api/trips/{tripId}/members | - |
 
### 🔑 7. 비밀번호 재설정 (Password Reset API)
 
| 기능 | Method | Endpoint | 요청 |
|------|--------|----------|------|
| 재설정 링크 발송 | `POST` | /api/auth/password-reset/request | email (Query) |
| 비밀번호 변경 | `POST` | /api/auth/password-reset/confirm | PasswordResetConfirmRequest |
 
### 🔔 8. 알림 서비스 (Notification API)
 
| 기능 | Method | Endpoint | 요청 |
|------|--------|----------|------|
| 실시간 알림 | `GET` | /api/notifications/subscribe | - |
| 미확인 알림 조회 | `GET` | /api/notifications | - |
| 알림 읽음 처리 | `PATCH` | /api/notifications/{id}/read | - |
 
<br>
