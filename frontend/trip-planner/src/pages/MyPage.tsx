import { useEffect, useState, ChangeEvent } from "react";
import Header from "../components/layout/Header";
import { getMe } from "../components/api/auth.ts";
import "./MyPage.css";

interface UserInfo {
  id: number;
  email: string;
  name?: string;
  nickname?: string;
  phone?: string;
  address?: string;
}

interface TripItem {
  id: number;
  title: string;
  destination: string;
  date: string;
}

export default function MyPage() {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [openEdit, setOpenEdit] = useState(false);

  const [editForm, setEditForm] = useState({
    phone: "",
    address: "",
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  const tripList: TripItem[] = [
    {
      id: 1,
      title: "부산 2박 3일",
      destination: "부산",
      date: "2026.04.10 - 2026.04.12",
    },
    {
      id: 2,
      title: "서울 당일치기",
      destination: "서울",
      date: "2026.04.18",
    },
    {
      id: 3,
      title: "제주도 가족여행",
      destination: "제주",
      date: "2026.05.01 - 2026.05.04",
    },
  ];

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const token = localStorage.getItem("accessToken");

        if (!token || token === "undefined") {
          return;
        }

        const userData = await getMe();
        setUser(userData);

        setEditForm((prev) => ({
          ...prev,
          phone: userData.phone || "",
          address: "",
        }));
      } catch (error) {
        console.error("마이페이지 사용자 정보 조회 실패:", error);
      }
    };

    fetchUser();
  }, []);

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;

    setEditForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSave = () => {
    if (
      editForm.newPassword &&
      editForm.newPassword !== editForm.confirmPassword
    ) {
      alert("비밀번호가 일치하지 않습니다.");
      return;
    }

    console.log("수정 데이터:", editForm);
    alert("현재는 UI만 구현된 상태입니다.");
  };

  const displayName = user?.nickname || user?.name || "";
  const email = user?.email || "";

  return (
    <div className="mypage">
      <Header />

      <main className="mypage-body">
        <section className="mypage-profile">
          <div className="mypage-profile-card">
            <div className="mypage-profile-header">
              <div>
                <span className="mypage-badge">MY PAGE</span>
                <h1 className="mypage-title">마이페이지</h1>
                <p className="mypage-welcome">
                  안녕하세요{displayName ? `, ${displayName}님` : ""}
                </p>
                <p className="mypage-email">{email}</p>
              </div>

              <button
                className="mypage-edit-btn"
                onClick={() => setOpenEdit((prev) => !prev)}
              >
                회원정보 수정
              </button>
            </div>
          </div>
        </section>

        {openEdit && (
          <section className="mypage-edit-section">
            <div className="mypage-edit-card">
              <h2 className="mypage-section-title">회원정보 수정</h2>

              <div className="mypage-edit-grid">
                <div className="mypage-form-group">
                  <label>이메일</label>
                  <input value={email} disabled />
                </div>

                <div className="mypage-form-group">
                  <label>전화번호</label>
                  <input
                    name="phone"
                    value={editForm.phone}
                    onChange={handleChange}
                  />
                </div>

                <div className="mypage-form-group full">
                  <label>주소</label>
                  <input
                    name="address"
                    value={editForm.address}
                    onChange={handleChange}
                  />
                </div>

                <div className="mypage-form-group">
                  <label>현재 비밀번호</label>
                  <input
                    type="password"
                    name="currentPassword"
                    value={editForm.currentPassword}
                    onChange={handleChange}
                  />
                </div>

                <div className="mypage-form-group">
                  <label>새 비밀번호</label>
                  <input
                    type="password"
                    name="newPassword"
                    value={editForm.newPassword}
                    onChange={handleChange}
                  />
                </div>

                <div className="mypage-form-group full">
                  <label>비밀번호 확인</label>
                  <input
                    type="password"
                    name="confirmPassword"
                    value={editForm.confirmPassword}
                    onChange={handleChange}
                  />
                </div>
              </div>

              <div className="mypage-edit-actions">
                <button
                  className="mypage-cancel-btn"
                  onClick={() => setOpenEdit(false)}
                >
                  취소
                </button>
                <button className="mypage-save-btn" onClick={handleSave}>
                  저장
                </button>
              </div>
            </div>
          </section>
        )}

        <section className="mypage-trips">
          <h2 className="mypage-section-title">내 여행 계획</h2>

          <div className="mypage-trip-list">
            {tripList.map((trip) => (
              <div key={trip.id} className="mypage-trip-card">
                <h3>{trip.title}</h3>
                <p>{trip.destination}</p>
                <p>{trip.date}</p>
                <button className="mypage-detail-btn">상세보기</button>
              </div>
            ))}
          </div>
        </section>
      </main>
    </div>
  );
}