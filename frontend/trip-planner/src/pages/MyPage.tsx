import { useEffect, useState, ChangeEvent } from "react";
import Header from "../components/layout/Header";
import { getMe } from "../components/api/auth.ts";
import { useTrips } from "../components/hooks/useTrip.ts";
import { TripPlanResponse } from "../types/trip.ts";
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
// 이제 TripPlanResponse 가져와서 사용할거라서 주석 했습니다.
// interface TripItem {
//   id: number;
//   title: string;
//   destination: string;
//   date: string;
// }

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
  const [openEdit, setOpenEdit] = useState(false);
  const {data: tripList, isLoading, isError} = useTrips();

  const [editForm, setEditForm] = useState({
  const [form, setForm] = useState<EditForm>({
    email: "",
    name: "",
    nickname: "",
    phone: "",
    address: "",
    currentPassword: "",
    newPassword: "",
    newPasswordConfirm: "",
  });


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

  const handleSave = () => {
    if (form.newPassword || form.newPasswordConfirm || form.currentPassword) {
      if (!form.currentPassword) {
        alert("현재 비밀번호를 입력해주세요.");
        return;
      }

      if (!form.newPassword) {
        alert("새 비밀번호를 입력해주세요.");
        return;
      }

      if (form.newPassword !== form.newPasswordConfirm) {
        alert("새 비밀번호 확인이 일치하지 않습니다.");
        return;
      }
    }

    console.log("수정할 회원정보:", form);
    alert("회원정보 수정 저장 기능은 아직 연결 전입니다.");
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
            >
              저장
            </button>
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
            {isLoading && <p>여행 데이터를 불러오는 중입니다... ✈️</p>}
            {isError && <p>데이터를 불러오는데 실패했습니다. 🥲</p>}

            {Array.isArray(tripList) ? (
              tripList.length === 0 ? (
                <p>아직 작성된 여행 계획이 없습니다. 지도를 클릭해 새 여행을 만들어보세요!</p>
              ) : (
                tripList.map((trip: TripPlanResponse) => (
                  <div key={trip.id} className="mypage-trip-card">
                    <h3>{trip.title}</h3>
                    <p>목적지 : {trip.destination}</p>
                    <p>여행 기간 : {trip.startDate} ~ {trip.endDate}</p>
                    <button className="mypage-detail-btn">상세보기</button>
                  </div>
                ))
              )
            ) : (
              !isLoading && <p style={{color: 'red'}}>현재 로그인이 만료되었거나 데이터를 불러올 수 없습니다. 다시 로그인해 주세요.</p>
            )}
          </div>
        </section>
      </main>
    </div>
  );
}