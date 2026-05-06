# 🧳 TRIP PLANNER - Backend

Spring Boot 기반의 여행 관리 웹 서비스의 백엔드 API 서버입니다.  
사용자는 여행 계획을 생성 및 관리하고, 커뮤니티 게시판 및 채팅 기능을 통해 정보를 공유할 수 있습니다.

---

## 🛠️ Tech Stack

- **Framework**: Spring Boot
- **Security**: Spring Security, JWT, OAuth2 (Google, Kakao)
- **ORM**: JPA / Hibernate
- **Database**: MariaDB
- **Cache / Token Store**: Redis
- **Build Tool**: Gradle

---

## 🧩 주요 기능 (API)

### 👤 인증 / 사용자
- 회원가입 / 로그인
- OAuth2 로그인 (Google, Kakao)
- JWT 기반 인증 및 인가 처리
- Refresh Token 관리 (Redis)

---

### 🧳 여행 계획 관리
- 여행 계획 생성 / 조회 / 수정 / 삭제
- 여행 일정(day) 및 세부 스케줄 관리
- 장소(Place) 기반 일정 구성
- 예산(Budget) 및 지출(Expense) 관리

---

### 📝 커뮤니티 게시판
- 게시글 CRUD (작성 / 조회 / 수정 / 삭제)
- 카테고리 및 지역 기반 필터링
- 키워드 검색
- 댓글 및 대댓글 기능
- 좋아요 기능
- 이미지 업로드

---

### 💬 채팅 및 기타 기능
- 채팅방 생성 및 메시지 송수신
- 사용자 알림(Notification) 기능
- 챗봇 기능 지원

---

## 🗄️ Database Structure

관계형 데이터베이스 기반으로 설계되었으며, 주요 엔티티는 다음과 같습니다.

### 핵심 도메인

- **User**: 사용자 정보 및 인증
- **Trip_Plan**: 여행 계획
- **Trip_Day / Trip_Schedule**: 여행 일정 및 상세 스케줄
- **Place**: 장소 정보

### 커뮤니티

- **Community**: 게시글
- **Community_Comment**: 댓글 및 대댓글
- **Community_Like**: 좋아요
- **Community_Image**: 이미지 데이터

### 기타

- **Chat_Room / Chat_Message**: 채팅 기능
- **Notification**: 사용자 알림
- **Refresh_Token**: 토큰 관리 (Redis 연동)

※ 전체 구조는 ERD 이미지를 참고

---

## 🔐 인증 구조

- JWT Access Token 기반 인증
- Redis를 활용한 Refresh Token 관리
- OAuth2 로그인 지원 (Google, Kakao)

---

## 🔄 API 통신 구조

클라이언트는 REST API를 통해 서버와 통신합니다.


---

## ⚙️ 서버 실행 방법

```bash
./gradlew bootRun
```