# 🗺️ TPlanner

> 지도 위에 핀을 찍고 동선을 계획하는 여행 플래너 서비스

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat-square&logo=springboot)
![React](https://img.shields.io/badge/React-61DAFB?style=flat-square&logo=react&logoColor=black)
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=flat-square&logo=mariadb)
![AWS](https://img.shields.io/badge/AWS-FF9900?style=flat-square&logo=amazonaws&logoColor=white)

<br>

## 📌 프로젝트 소개

**Trip Planner**는 지도에 핀을 찍어 여행 동선을 시각적으로 계획하고, 상세 메모로 일정을 정리할 수 있는 여행 플래닝 서비스입니다.  
완성된 여행 계획은 커뮤니티 게시판에 공유하여 다른 사용자들과 나눌 수 있습니다.

<br>

## 📅 개발 기간

**2025.03.31 ~ 2025.05.07** (약 5주)

<br>

## 👥 팀원 소개

| 역할 | 이름 | 담당 기능 |
|------|------|----------|
| 팀장 | - | 지도 핀 & 동선 기능 |
| 팀원 | - | AI 챗봇 |
| 팀원 | - | 게시판 |
| 팀원 | - | 프론트엔드 전반, 마이페이지 |
| 팀원 | - | 백엔드 전반, 소켓 기반 관리자 문의 기능 |

<br>

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
![AWS](https://img.shields.io/badge/AWS-FF9900?style=flat-square&logo=amazonaws&logoColor=white)

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
│       ├── pages/           # 페이지 컴포넌트
│       ├── hooks/           # 커스텀 훅 (React Query)
│       ├── api/             # API 호출 함수
│       └── styles/          # 전역 스타일
│
└── backend/
    └── src/main/java/
        ├── controller/      # REST API 컨트롤러
        ├── service/         # 비즈니스 로직
        ├── repository/      # DB 접근 계층
        ├── entity/          # JPA 엔티티
        └── config/          # 설정 파일 (Security, WebSocket 등)
```

<br>

## 📄 License

This project is licensed under the MIT License.
