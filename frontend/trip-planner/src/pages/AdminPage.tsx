import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../components/layout/Header";
import toast from "react-hot-toast";
import {
  AdminUserResponse,
  banUserApi,
  getAdminUsers,
} from "../components/api/admin";
import "./AdminPage.css";

export default function AdminPage() {
  const [keyword, setKeyword] = useState("");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [users, setUsers] = useState<AdminUserResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [banTarget, setBanTarget] = useState<AdminUserResponse | null>(null);
  const [duration, setDuration] = useState(7);
  const [reason, setReason] = useState("");

  const navigate = useNavigate();

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setIsLoading(true);
      const data = await getAdminUsers();
      setUsers(data);
    } catch (error) {
      console.error("회원 목록 조회 실패:", error);
      toast.error("회원 목록을 불러오지 못했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  const isBanned = (user: AdminUserResponse) => {
    if (!user.bannedUntil) {
      return false;
    }

    return new Date(user.bannedUntil) > new Date();
  };

  const handleSearch = () => {
    setSearchKeyword(keyword.trim());
  };

  const handleSearchKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      handleSearch();
    }
  };

  const filteredUsers = useMemo(() => {
    const lowerKeyword = searchKeyword.toLowerCase();

    if (!lowerKeyword) {
      return users;
    }

    return users.filter((user) => {
      const displayStatus = isBanned(user) ? "BANNED" : user.status;

      return (
        user.email.toLowerCase().includes(lowerKeyword) ||
        user.nickname?.toLowerCase().includes(lowerKeyword) ||
        user.role.toLowerCase().includes(lowerKeyword) ||
        displayStatus.toLowerCase().includes(lowerKeyword)
      );
    });
  }, [users, searchKeyword]);

  const totalUserCount = users.length;

  const activeUserCount = users.filter((user) => {
    return user.status === "ACTIVE" && !isBanned(user);
  }).length;

  const bannedUserCount = users.filter((user) => {
    return isBanned(user);
  }).length;

  const handleOpenBanModal = (user: AdminUserResponse) => {
    setBanTarget(user);
    setDuration(7);
    setReason(user.banReason ?? "");
  };

  const handleCloseBanModal = () => {
    setBanTarget(null);
    setDuration(7);
    setReason("");
  };

  const handleBanUser = async () => {
    if (!banTarget) {
      return;
    }

    if (!reason.trim()) {
      toast.error("정지 사유를 입력해주세요.");
      return;
    }

    if (duration <= 0) {
      toast.error("정지 기간은 1일 이상이어야 합니다.");
      return;
    }

    const confirmed = window.confirm(
      `${banTarget.nickname} 회원을 ${duration}일 정지하시겠습니까?`
    );

    if (!confirmed) {
      return;
    }

    try {
      const message = await banUserApi(banTarget.id, {
        duration,
        reason,
      });

      toast.success(message);
      handleCloseBanModal();
      fetchUsers();
    } catch (error) {
      console.error("회원 정지 실패:", error);
      toast.error("회원 정지 처리에 실패했습니다.");
    }
  };

  const formatDateTime = (dateTime: string | null) => {
    if (!dateTime) {
      return "-";
    }

    return dateTime.replace("T", " ").substring(0, 16);
  };

  return (
    <>
      <Header />

      <main className="admin-page">
        <section className="admin-container">
          <section className="admin-header-card">
            <div className="admin-header-top">
              <div>
                <span className="admin-badge">ADMIN</span>
                <h1>관리자 페이지</h1>
                <p>회원 정보를 확인하고 서비스 이용 상태를 관리할 수 있습니다.</p>
              </div>

              <button
                type="button"
                className="admin-cs-button"
                onClick={() => navigate("/admin/cs")}
              >
                1:1 문의 관리
              </button>
            </div>
          </section>

          <section className="admin-summary-grid">
            <div className="admin-summary-card">
              <span>전체 회원</span>
              <strong>{totalUserCount}</strong>
            </div>

            <div className="admin-summary-card">
              <span>활성 회원</span>
              <strong>{activeUserCount}</strong>
            </div>

            <div className="admin-summary-card">
              <span>정지 회원</span>
              <strong>{bannedUserCount}</strong>
            </div>
          </section>

          <div className="admin-panel">
            <div className="admin-panel-header">
              <h2>회원 관리</h2>
            </div>

            <div className="admin-search-row">
              <input
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                onKeyDown={handleSearchKeyDown}
                placeholder="이메일, 닉네임, 권한, 상태 검색"
              />
              <button type="button" onClick={handleSearch}>
                검색
              </button>
            </div>

            <div className="admin-table-wrap">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>이메일</th>
                    <th>닉네임</th>
                    <th>권한</th>
                    <th>상태</th>
                    <th>정지 종료</th>
                    <th>정지 사유</th>
                    <th>관리</th>
                  </tr>
                </thead>

                <tbody>
                  {isLoading ? (
                    <tr>
                      <td colSpan={8} className="admin-empty">
                        회원 목록을 불러오는 중입니다.
                      </td>
                    </tr>
                  ) : filteredUsers.length === 0 ? (
                    <tr>
                      <td colSpan={8} className="admin-empty">
                        조회된 회원이 없습니다.
                      </td>
                    </tr>
                  ) : (
                    filteredUsers.map((user) => (
                      <tr key={user.id}>
                        <td>{user.id}</td>
                        <td>{user.email}</td>
                        <td>{user.nickname}</td>
                        <td>{user.role}</td>
                        <td>{isBanned(user) ? "BANNED" : user.status}</td>
                        <td>{formatDateTime(user.bannedUntil)}</td>
                        <td>{user.banReason ?? "-"}</td>
                        <td>
                          {user.role === "ADMIN" || user.role === "ROLE_ADMIN" ? (
                            <span className="admin-disabled-text">관리자</span>
                          ) : isBanned(user) ? (
                            <button
                              type="button"
                              className="admin-edit-button"
                              onClick={() => handleOpenBanModal(user)}
                            >
                              수정
                            </button>
                          ) : (
                            <button
                              type="button"
                              className="admin-ban-button"
                              onClick={() => handleOpenBanModal(user)}
                            >
                              정지
                            </button>
                          )}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </section>
      </main>

      {banTarget && (
        <div className="admin-modal-backdrop">
          <div className="admin-modal">
            <h3>{isBanned(banTarget) ? "정지 수정" : "회원 정지"}</h3>

            <p className="admin-modal-user">
              대상 회원: <strong>{banTarget.nickname}</strong>
            </p>

            <label>
              정지 기간
              <select
                value={duration}
                onChange={(e) => setDuration(Number(e.target.value))}
              >
                <option value={1}>1일</option>
                <option value={3}>3일</option>
                <option value={7}>7일</option>
                <option value={14}>14일</option>
                <option value={30}>30일</option>
                <option value={365}>365일</option>
                <option value={36500}>영구 정지</option>
              </select>
            </label>

            <label>
              정지 사유
              <textarea
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder="정지 사유를 입력하세요."
              />
            </label>

            <div className="admin-modal-actions">
              <button
                type="button"
                className="admin-cancel-button"
                onClick={handleCloseBanModal}
              >
                취소
              </button>

              <button
                type="button"
                className="admin-confirm-button"
                onClick={handleBanUser}
              >
                {isBanned(banTarget) ? "수정 완료" : "정지 처리"}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}