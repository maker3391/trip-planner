export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  name: string;
  nickname: string;
  phone: string;
}

export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
}

export interface UserInfo {
    email: string;
    name: string;
    role: string;
}

export type SignupFormErrors = Partial<SignupRequest>;
export type LoginFormErrors = Partial<LoginRequest>;

export interface AuthContextType {
  // 1. 유저 정보 (UserInfo 타입은 이미 정의되어 있다고 가정)
  user: UserInfo | null;
  
  // 2. 로그인 함수 (토큰 두 개를 받는 규격 유지)
  login: (accessToken: string, refreshToken: string) => void;
  
  // 3. 로그아웃
  logout: () => void;
  
  // 4. 유틸리티 메서드
  getToken: () => string | null;
  
  // 5. 상태 값 (isLoggedIn 대신 isLoading을 추가하면 초기 로딩 처리가 쉬워집니다)
  isLoggedIn: boolean;
  isLoading?: boolean; // 초기 토큰 검증 시 사용
}