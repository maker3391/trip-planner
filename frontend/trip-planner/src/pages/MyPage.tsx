import { useEffect, useState, ChangeEvent } from "react";
import Header from "../components/layout/Header";
import { getMe, updateMe } from "../components/api/auth.ts";
import "./MyPage.css";

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

  const fetchUser = async () => {
    const token = localStorage.getItem("accessToken");

    if (!token || token === "undefined") {
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
      alert("이름을 입력해주세요.");
      return;
    }

    if (!trimmedNickname) {
      alert("닉네임을 입력해주세요.");
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

      alert("기본 정보가 수정되었습니다.");
    } catch (error: any) {
      console.error("기본 정보 수정 실패:", error);

      alert(
        error?.response?.data?.message ||
          "기본 정보 수정 중 오류가 발생했습니다."
      );
    } finally {
      setIsSavingBasic(false);
    }
  };

  const handleSavePassword = async () => {
    if (!user || isSavingPassword) return;

    if (!passwordForm.currentPassword) {
      alert("현재 비밀번호를 입력해주세요.");
      return;
    }

    if (!passwordForm.newPassword) {
      alert("새 비밀번호를 입력해주세요.");
      return;
    }

    if (!passwordForm.newPasswordConfirm) {
      alert("새 비밀번호 확인을 입력해주세요.");
      return;
    }

    if (passwordForm.newPassword !== passwordForm.newPasswordConfirm) {
      alert("새 비밀번호 확인이 일치하지 않습니다.");
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

      alert("비밀번호가 변경되었습니다.");
    } catch (error: any) {
      console.error("비밀번호 변경 실패:", error);

      alert(
        error?.response?.data?.message ||
          "비밀번호 변경 중 오류가 발생했습니다."
      );
    } finally {
      setIsSavingPassword(false);
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
      </main>
    </div>
  );
}