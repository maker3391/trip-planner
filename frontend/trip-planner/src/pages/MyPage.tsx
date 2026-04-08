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

interface EditForm {
  email: string;
  name: string;
  nickname: string;
  phone: string;
  currentPassword: string;
  newPassword: string;
  newPasswordConfirm: string;
}

export default function MyPage() {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [form, setForm] = useState<EditForm>({
    email: "",
    name: "",
    nickname: "",
    phone: "",
    currentPassword: "",
    newPassword: "",
    newPasswordConfirm: "",
  });
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    const fetchUser = async () => {
      const token = localStorage.getItem("accessToken");

      if (!token || token === "undefined") {
        return;
      }

      try {
        const userData = await getMe();
        setUser(userData);

        setForm({
          email: userData.email || "",
          name: userData.name || "",
          nickname: userData.nickname || "",
          phone: userData.phone || "",
          currentPassword: "",
          newPassword: "",
          newPasswordConfirm: "",
        });
      } catch (error) {
        console.error("마이페이지 사용자 정보 조회 실패:", error);
      }
    };

    fetchUser();
  }, []);

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;

    setForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSave = async () => {
    if (!user || isSaving) return;

    const trimmedName = form.name.trim();
    const trimmedNickname = form.nickname.trim();
    const trimmedPhone = form.phone.trim();

    const hasPasswordInput =
      !!form.currentPassword || !!form.newPassword || !!form.newPasswordConfirm;

    if (!trimmedName) {
      alert("이름을 입력해주세요.");
      return;
    }

    if (!trimmedNickname) {
      alert("닉네임을 입력해주세요.");
      return;
    }

    if (hasPasswordInput) {
      if (!form.currentPassword) {
        alert("현재 비밀번호를 입력해주세요.");
        return;
      }

      if (!form.newPassword) {
        alert("새 비밀번호를 입력해주세요.");
        return;
      }

      if (!form.newPasswordConfirm) {
        alert("새 비밀번호 확인을 입력해주세요.");
        return;
      }

      if (form.newPassword !== form.newPasswordConfirm) {
        alert("새 비밀번호 확인이 일치하지 않습니다.");
        return;
      }
    }

    try {
      setIsSaving(true);

      await updateMe({
        name: trimmedName,
        nickname: trimmedNickname,
        phone: trimmedPhone,
        currentPassword: form.currentPassword || undefined,
        newPassword: form.newPassword || undefined,
      });

      const updatedUser = await getMe();
      setUser(updatedUser);

      setForm({
        email: updatedUser.email || "",
        name: updatedUser.name || "",
        nickname: updatedUser.nickname || "",
        phone: updatedUser.phone || "",
        currentPassword: "",
        newPassword: "",
        newPasswordConfirm: "",
      });

      alert("회원정보가 수정되었습니다.");
    } catch (error: any) {
      console.error("회원정보 수정 실패:", error);

      alert(
        error?.response?.data?.message ||
          "회원정보 수정 중 오류가 발생했습니다."
      );
    } finally {
      setIsSaving(false);
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
          <p className="mypage-email">{user?.email || form.email}</p>
          <p className="mypage-description">
            계정 정보를 확인하고 수정할 수 있습니다.
          </p>
        </section>

        <section className="mypage-edit-card">
          <h2 className="mypage-section-title">기본 정보</h2>

          <div className="mypage-edit-grid">
            <div className="mypage-form-group full">
              <label htmlFor="email">이메일</label>
              <input
                id="email"
                name="email"
                type="email"
                value={form.email}
                readOnly
              />
            </div>

            <div className="mypage-form-group">
              <label htmlFor="name">이름</label>
              <input
                id="name"
                name="name"
                type="text"
                value={form.name}
                onChange={handleChange}
                placeholder="이름을 입력하세요"
              />
            </div>

            <div className="mypage-form-group">
              <label htmlFor="nickname">닉네임</label>
              <input
                id="nickname"
                name="nickname"
                type="text"
                value={form.nickname}
                onChange={handleChange}
                placeholder="닉네임을 입력하세요"
              />
            </div>

            <div className="mypage-form-group full">
              <label htmlFor="phone">전화번호</label>
              <input
                id="phone"
                name="phone"
                type="text"
                value={form.phone}
                onChange={handleChange}
                placeholder="전화번호를 입력하세요"
              />
            </div>
          </div>

          <h2 className="mypage-section-title password-section-title">
            비밀번호 변경
          </h2>

          <div className="mypage-edit-grid">
            <div className="mypage-form-group full">
              <label htmlFor="currentPassword">현재 비밀번호</label>
              <input
                id="currentPassword"
                name="currentPassword"
                type="password"
                value={form.currentPassword}
                onChange={handleChange}
                placeholder="현재 비밀번호를 입력하세요"
              />
            </div>

            <div className="mypage-form-group">
              <label htmlFor="newPassword">새 비밀번호</label>
              <input
                id="newPassword"
                name="newPassword"
                type="password"
                value={form.newPassword}
                onChange={handleChange}
                placeholder="새 비밀번호를 입력하세요"
              />
            </div>

            <div className="mypage-form-group">
              <label htmlFor="newPasswordConfirm">새 비밀번호 확인</label>
              <input
                id="newPasswordConfirm"
                name="newPasswordConfirm"
                type="password"
                value={form.newPasswordConfirm}
                onChange={handleChange}
                placeholder="새 비밀번호를 다시 입력하세요"
              />
            </div>
          </div>

          <div className="mypage-edit-actions">
            <button
              type="button"
              className="mypage-save-btn"
              onClick={handleSave}
              disabled={isSaving}
            >
              {isSaving ? "저장 중..." : "저장"}
            </button>
          </div>
        </section>
      </main>
    </div>
  );
}