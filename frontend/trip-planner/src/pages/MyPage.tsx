import { useEffect, useState, ChangeEvent } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../components/layout/Header";
import { getMe, updateMe, withdrawApi } from "../components/api/auth.ts";
import "./MyPage.css";
import toast from "react-hot-toast"; // toast 임포트 추가

interface UserInfo {
  id: number;
  email: string;
  name?: string;
  nickname?: string;
  phone?: string;
  address?: string;
  profileImage?: string;
  role?: string;
  status?: string;
}

interface BasicForm {
  name: string;
  nickname: string;
  phone: string;
}

interface PasswordForm {
  currentPassword: string;
  newPassword: string;
  newPasswordConfirm: string;
}

export default function MyPage() {
  const navigate = useNavigate();
  const [user, setUser] = useState<UserInfo | null>(null);

  const [basicForm, setBasicForm] = useState<BasicForm>({
    name: "",
    nickname: "",
    phone: "",
  });

  const [passwordForm, setPasswordForm] = useState<PasswordForm>({
    currentPassword: "",
    newPassword: "",
    newPasswordConfirm: "",
  });

  const [isSavingBasic, setIsSavingBasic] = useState(false);
  const [isSavingPassword, setIsSavingPassword] = useState(false);
  const [isWithdrawing, setIsWithdrawing] = useState(false);

  const clearAuthAndMoveLogin = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("isLoggedIn");
    setUser(null);
    navigate("/login", { replace: true });
  };

  const fetchUser = async () => {
    const token = localStorage.getItem("accessToken");

    if (!token || token === "undefined") {
      clearAuthAndMoveLogin();
      return;
    }

    try {
      const userData = await getMe();
      setUser(userData);

      setBasicForm({
        name: userData.name || "",
        nickname: userData.nickname || "",
        phone: userData.phone || "",
      });
    } catch (error) {
      console.error("마이페이지 사용자 정보 조회 실패:", error);
      clearAuthAndMoveLogin();
    }
  };

  useEffect(() => {
    fetchUser();
  }, []);

  const handleBasicChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setBasicForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handlePasswordChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setPasswordForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSaveBasicInfo = async () => {
    if (!user || isSavingBasic) return;

    const trimmedName = basicForm.name.trim();
    const trimmedNickname = basicForm.nickname.trim();
    const trimmedPhone = basicForm.phone.trim();

    if (!trimmedName) {
      toast.error("이름을 입력해주세요.", { id: "basic-info-error" });
      return;
    }

    if (!trimmedNickname) {
      toast.error("닉네임을 입력해주세요.", { id: "basic-info-error" });
      return;
    }

    try {
      setIsSavingBasic(true);

      await updateMe({
        name: trimmedName,
        nickname: trimmedNickname,
        phone: trimmedPhone,
      });

      await fetchUser();
      toast.success("기본 정보가 수정되었습니다.", { id: "basic-info-success" });
    } catch (error: any) {
      console.error("기본 정보 수정 실패:", error);

      if (error?.response?.status === 401 || error?.response?.status === 403) {
        toast.error("로그인 정보가 만료되었습니다. 다시 로그인해주세요.", { id: "auth-expire" });
        clearAuthAndMoveLogin();
        return;
      }

      toast.error(
        error?.response?.data?.message || "기본 정보 수정 중 오류가 발생했습니다.",
        { id: "basic-info-error" }
      );
    } finally {
      setIsSavingBasic(false);
    }
  };

  const handleSavePassword = async () => {
    if (!user || isSavingPassword) return;

    if (!passwordForm.currentPassword) {
      toast.error("현재 비밀번호를 입력해주세요.", { id: "password-error" });
      return;
    }

    if (!passwordForm.newPassword) {
      toast.error("새 비밀번호를 입력해주세요.", { id: "password-error" });
      return;
    }

    if (!passwordForm.newPasswordConfirm) {
      toast.error("새 비밀번호 확인을 입력해주세요.", { id: "password-error" });
      return;
    }

    if (passwordForm.newPassword !== passwordForm.newPasswordConfirm) {
      toast.error("새 비밀번호 확인이 일치하지 않습니다.", { id: "password-error" });
      return;
    }

    try {
      setIsSavingPassword(true);

      await updateMe({
        name: user.name || "",
        nickname: user.nickname || "",
        phone: user.phone || "",
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      });

      await fetchUser();

      setPasswordForm({
        currentPassword: "",
        newPassword: "",
        newPasswordConfirm: "",
      });

      toast.success("비밀번호가 변경되었습니다.", { id: "password-success" });
    } catch (error: any) {
      console.error("비밀번호 변경 실패:", error);

      if (error?.response?.status === 401 || error?.response?.status === 403) {
        toast.error("로그인 정보가 만료되었습니다. 다시 로그인해주세요.", { id: "auth-expire" });
        clearAuthAndMoveLogin();
        return;
      }

      toast.error(
        error?.response?.data?.message || "비밀번호 변경 중 오류가 발생했습니다.",
        { id: "password-error" }
      );
    } finally {
      setIsSavingPassword(false);
    }
  };

  const handleWithdraw = async () => {
    if (!user || isWithdrawing) return;

    // 브라우저의 confirm 창은 그대로 유지하거나 커스텀 모달을 써야 함 (단순 알림이 아니기 때문)
    const confirmed = window.confirm(
      "정말 회원을 탈퇴 하시겠습니까?\n탈퇴 후에는 계정을 복구할 수 없습니다."
    );

    if (!confirmed) return;

    try {
      setIsWithdrawing(true);

      const response = await withdrawApi();

      toast.success(response.message || "회원탈퇴가 완료되었습니다.", { id: "withdraw-success" });

      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("isLoggedIn");
      setUser(null);

      navigate("/", { replace: true });
    } catch (error: any) {
      console.error("회원탈퇴 실패:", error);

      if (error?.response?.status === 401 || error?.response?.status === 403) {
        toast.error("로그인 정보가 만료되었습니다. 다시 로그인해주세요.", { id: "auth-expire" });
        clearAuthAndMoveLogin();
        return;
      }

      toast.error(
        error?.response?.data?.message || "회원탈퇴 중 오류가 발생했습니다.",
        { id: "withdraw-error" }
      );
    } finally {
      setIsWithdrawing(false);
    }
  };

  const displayName =
    user?.nickname?.trim() ||
    user?.name?.trim() ||
    user?.email?.split("@")[0] ||
    "사용자";

  return (
    <div className="mypage">
      <Header />
      <main className="mypage-body">
        {/* ... 섹션 구조 동일 ... */}
        <section className="mypage-intro-card">
          <span className="mypage-badge">MY PAGE</span>
          <h1 className="mypage-title">내 정보</h1>
          <p className="mypage-welcome">안녕하세요, {displayName}님</p>
          <p className="mypage-email">{user?.email || ""}</p>
          <p className="mypage-description">
            계정 정보를 확인하고 수정할 수 있습니다.
          </p>
        </section>

        <section className="mypage-view-card">
          <h2 className="mypage-section-title">기본 정보</h2>
          <div className="mypage-info-grid">
            <div className="mypage-info-item full">
              <span className="mypage-info-label">이메일</span>
              <div className="mypage-info-value">{user?.email || "-"}</div>
            </div>
            <div className="mypage-info-item">
              <span className="mypage-info-label">이름</span>
              <div className="mypage-info-value">{user?.name || "-"}</div>
            </div>
            <div className="mypage-info-item">
              <span className="mypage-info-label">닉네임</span>
              <div className="mypage-info-value">{user?.nickname || "-"}</div>
            </div>
            <div className="mypage-info-item full">
              <span className="mypage-info-label">전화번호</span>
              <div className="mypage-info-value">{user?.phone || "-"}</div>
            </div>
          </div>
        </section>

        <section className="mypage-edit-card">
          <h2 className="mypage-section-title">기본 정보 수정</h2>
          <div className="mypage-edit-grid">
            <div className="mypage-form-group">
              <label htmlFor="name">이름</label>
              <input
                id="name"
                name="name"
                type="text"
                value={basicForm.name}
                onChange={handleBasicChange}
                placeholder="이름을 입력하세요"
              />
            </div>
            <div className="mypage-form-group">
              <label htmlFor="nickname">닉네임</label>
              <input
                id="nickname"
                name="nickname"
                type="text"
                value={basicForm.nickname}
                onChange={handleBasicChange}
                placeholder="닉네임을 입력하세요"
              />
            </div>
            <div className="mypage-form-group full">
              <label htmlFor="phone">전화번호</label>
              <input
                id="phone"
                name="phone"
                type="text"
                value={basicForm.phone}
                onChange={handleBasicChange}
                placeholder="전화번호를 입력하세요"
              />
            </div>
          </div>
          <div className="mypage-edit-actions">
            <button
              type="button"
              className="mypage-save-btn"
              onClick={handleSaveBasicInfo}
              disabled={isSavingBasic}
            >
              {isSavingBasic ? "저장 중..." : "기본 정보 저장"}
            </button>
          </div>
        </section>

        <section className="mypage-edit-card password-card">
          <h2 className="mypage-section-title">비밀번호 변경</h2>
          <div className="mypage-edit-grid">
            <div className="mypage-form-group full">
              <label htmlFor="currentPassword">현재 비밀번호</label>
              <input
                id="currentPassword"
                name="currentPassword"
                type="password"
                value={passwordForm.currentPassword}
                onChange={handlePasswordChange}
                placeholder="현재 비밀번호를 입력하세요"
              />
            </div>
            <div className="mypage-form-group">
              <label htmlFor="newPassword">새 비밀번호</label>
              <input
                id="newPassword"
                name="newPassword"
                type="password"
                value={passwordForm.newPassword}
                onChange={handlePasswordChange}
                placeholder="새 비밀번호를 입력하세요"
              />
            </div>
            <div className="mypage-form-group">
              <label htmlFor="newPasswordConfirm">새 비밀번호 확인</label>
              <input
                id="newPasswordConfirm"
                name="newPasswordConfirm"
                type="password"
                value={passwordForm.newPasswordConfirm}
                onChange={handlePasswordChange}
                placeholder="새 비밀번호를 다시 입력하세요"
              />
            </div>
          </div>
          <div className="mypage-edit-actions">
            <button
              type="button"
              className="mypage-save-btn"
              onClick={handleSavePassword}
              disabled={isSavingPassword}
            >
              {isSavingPassword ? "변경 중..." : "비밀번호 변경"}
            </button>
          </div>
        </section>

        <section className="mypage-edit-card danger-zone">
          <h2 className="mypage-section-title">회원 탈퇴</h2>
          <p className="mypage-danger-text">
            회원탈퇴 시 계정 정보는 복구할 수 없습니다.
          </p>
          <div className="mypage-edit-actions">
            <button
              type="button"
              className="mypage-withdraw-btn"
              onClick={handleWithdraw}
              disabled={isWithdrawing}
            >
              {isWithdrawing ? "탈퇴 처리 중..." : "회원 탈퇴"}
            </button>
          </div>
        </section>
      </main>
    </div>
  );
}